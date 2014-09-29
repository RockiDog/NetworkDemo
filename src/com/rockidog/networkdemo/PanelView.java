package com.rockidog.networkdemo;

import com.rockidog.networkdemo.utils.Graphics;

import android.content.Context;
import android.graphics.*;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;

public class PanelView extends SurfaceView implements Runnable, Callback {
    private static final int INVALID_POINTER_ID = -1;
    private static final int INVALID_JOYSTICK_ID = -1;
    private static final int INVALID_BUTTON_ID = -1;
    private static final long LONG_PRESSED_TIME_THRESHOLD = 300;
    private static final long TIME_PER_FRAME = 28; // In millisecond, FPS being 35 or so

    private boolean isPaused = false;
    private boolean isJoystickLocked = true;

    private float mJoystickRadius;
    private float mJoystickWheelRadius;
    private float mButtonRadius;
    private PointF mInitJoystickPositionL;
    private PointF mJoystickPositionL;
    private PointF mInitJoystickPositionR;
    private PointF mJoystickPositionR;
    private PointF mShootingButtonPosition;
    private PointF mDribblingButtonPosition;

    private Bitmap mBitmap = null;
    private Canvas mCanvas = new Canvas();
    private Paint mPaint = new Paint();
    private MaskFilter mBlur = new BlurMaskFilter(16, BlurMaskFilter.Blur.SOLID);
    private Thread mThread = new Thread(this);
    private SurfaceHolder mHolder = getHolder();

    private int mJoystickIdL = INVALID_JOYSTICK_ID;
    private int mJoystickIdR = INVALID_JOYSTICK_ID;
    private int mActivePointerId = INVALID_POINTER_ID;
    private int mButtonPressedId = INVALID_BUTTON_ID;
    private int mButton = ActionListener.INVALID;
    private ActionListener mActionListener = null;
    private long mButtonPressedTime = 0;
    private long mButtonDownTime = 0;
    private int mPowerBar = 0;
    private int mPowerBarStep = 0;

    {
        mHolder.addCallback(this);
        mHolder.setFormat(PixelFormat.TRANSPARENT);
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        setFocusable(true);
        setFocusableInTouchMode(true);
        setZOrderOnTop(true);
    }

    public PanelView(Context context) {
        super(context);
    }

    public PanelView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setActionListener(ActionListener listener) {
        mActionListener = listener;
    }

    @Override
    public void run() {
        Canvas canvas = null;
        while (false == isPaused) {
            long startTime = SystemClock.uptimeMillis();
            
            try {
                mCanvas.drawColor(Color.LTGRAY, PorterDuff.Mode.SRC);
                
                // Draw the backgounds of joysticks
                mPaint.setColor(Color.TRANSPARENT);
                mPaint.setAlpha(30);
                mPaint.setMaskFilter(null);
                mCanvas.drawCircle(mInitJoystickPositionL.x, mInitJoystickPositionL.y, mJoystickWheelRadius, mPaint);
                RectF rect = new RectF(mInitJoystickPositionR.x - mJoystickWheelRadius,
                                        mInitJoystickPositionR.y - mJoystickRadius / 8,
                                        mInitJoystickPositionR.x + mJoystickWheelRadius,
                                        mInitJoystickPositionR.y + mJoystickRadius / 8);
                mCanvas.drawRoundRect(rect, mJoystickRadius / 8, mJoystickRadius / 8, mPaint);
                
                // Draw the joysticks
                mPaint.setColor(Color.BLACK);
                mPaint.setAlpha(255);
                if (INVALID_JOYSTICK_ID != mJoystickIdL)
                    mPaint.setMaskFilter(mBlur);
                else
                    mPaint.setMaskFilter(null);
                mCanvas.drawCircle(mJoystickPositionL.x, mJoystickPositionL.y, mJoystickRadius, mPaint);
                if (INVALID_JOYSTICK_ID != mJoystickIdR)
                    mPaint.setMaskFilter(mBlur);
                else
                    mPaint.setMaskFilter(null);
                mCanvas.drawCircle(mJoystickPositionR.x, mJoystickPositionR.y, mJoystickRadius, mPaint);
                
                // Draw the buttons
                mPaint.setColor(Color.DKGRAY);
                if (ActionListener.INVALID != mButton && INVALID_BUTTON_ID != mButtonPressedId) { // Button being pressed
                    if (ActionListener.SHOOTING == mButton) { // Shoot
                        mPaint.setMaskFilter(mBlur);
                        mCanvas.drawCircle(mShootingButtonPosition.x, mShootingButtonPosition.y, mButtonRadius, mPaint);
                        mPaint.setMaskFilter(null);
                        mCanvas.drawCircle(mDribblingButtonPosition.x, mDribblingButtonPosition.y, mButtonRadius, mPaint);
                    }
                    else if (ActionListener.DRIBBLING == mButton) { // Dribble
                        mPaint.setMaskFilter(mBlur);
                        mCanvas.drawCircle(mDribblingButtonPosition.x, mDribblingButtonPosition.y, mButtonRadius, mPaint);
                        mPaint.setMaskFilter(null);
                        mCanvas.drawCircle(mShootingButtonPosition.x, mShootingButtonPosition.y, mButtonRadius, mPaint);
                    }
                }
                else { // Button not being pressed
                    mPaint.setMaskFilter(null);
                    mCanvas.drawCircle(mDribblingButtonPosition.x, mDribblingButtonPosition.y, mButtonRadius, mPaint);
                    mCanvas.drawCircle(mShootingButtonPosition.x, mShootingButtonPosition.y, mButtonRadius, mPaint);
                }
                
                // Draw the power bar
                if (INVALID_BUTTON_ID != mButtonPressedId) { // Button being pressed
                    if (ActionListener.INVALID != mButton) { // mPowerBar locked to this thread til mButton assigned
                        mButtonPressedTime = SystemClock.uptimeMillis() - mButtonDownTime;
                        if (LONG_PRESSED_TIME_THRESHOLD > mButtonPressedTime)
                            mPowerBar = 100; // Power being full
                        else {
                            mPowerBar = (mPowerBar + mPowerBarStep) % 100;
                            if (99 == mPowerBar || 0 == mPowerBar)
                                mPowerBarStep = -mPowerBarStep;
                        }
                    }
                    if (100 != mPowerBar) {
                        // Draw the bar background
                        mPaint.setColor(Color.BLACK);
                        mPaint.setMaskFilter(null);
                        mCanvas.drawRect(mInitJoystickPositionL.x - 1,
                                            mInitJoystickPositionR.y / 5 * 1 - 1,
                                            mInitJoystickPositionR.x + 1,
                                            mInitJoystickPositionR.y / 5 * 2 + 1,
                                            mPaint);
                        
                        // Draw the bar foreground
                        mPaint.setColor(Color.RED);
                        mPaint.setMaskFilter(mBlur);
                        mCanvas.drawRect(mInitJoystickPositionL.x,
                                            mInitJoystickPositionR.y / 5 * 1,
                                            mInitJoystickPositionL.x + (mInitJoystickPositionR.x - mInitJoystickPositionL.x) * mPowerBar / 100.0f,
                                            mInitJoystickPositionR.y / 5 * 2,
                                            mPaint);
                    }
                }
                
                // Draw the true canvas
                canvas = mHolder.lockCanvas();
                canvas.drawBitmap(mBitmap, 0, 0, null);
                if (null != canvas)
                    mHolder.unlockCanvasAndPost(canvas);
            }
            catch (NullPointerException e) {
                e.printStackTrace();
            }
            
            long endTime = SystemClock.uptimeMillis();
            long diffTime = endTime - startTime;
            try {
                if (TIME_PER_FRAME > diffTime)
                    Thread.sleep(TIME_PER_FRAME - diffTime);
            }
            catch(InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        float w = width;
        float h = height;
        mJoystickWheelRadius = w / 6;
        mJoystickRadius = mJoystickWheelRadius / 3;
        mButtonRadius = mJoystickRadius;
        mInitJoystickPositionL = new PointF(w / 4, h / 2);
        mInitJoystickPositionR = new PointF(w / 4 * 3, h / 3);
        mJoystickPositionL = new PointF(mInitJoystickPositionL.x, mInitJoystickPositionL.y);
        mJoystickPositionR = new PointF(mInitJoystickPositionR.x, mInitJoystickPositionR.y);
        mShootingButtonPosition = new PointF(mInitJoystickPositionR.x - (mJoystickWheelRadius + mButtonRadius) * 0.707f,
                                             mInitJoystickPositionR.y + (mJoystickWheelRadius + mButtonRadius) * 0.707f);
        mDribblingButtonPosition = new PointF(mInitJoystickPositionR.x, mInitJoystickPositionR.y + mJoystickWheelRadius + mButtonRadius);
        
        mBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        mCanvas.setBitmap(mBitmap);
        return;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mThread.start();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        isPaused = true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int pointerIndex = event.getActionIndex();
        float x = event.getX(pointerIndex);
        float y = event.getY(pointerIndex);
        float distanceToShootingButton = Graphics.distance(new PointF(x, y), mShootingButtonPosition);
        float distanceToDribblingButton = Graphics.distance(new PointF(x, y), mDribblingButtonPosition);
        float distanceToLeft = Graphics.distance(new PointF(x, y), mInitJoystickPositionL);
        float distanceToRight = Graphics.distance(new PointF(x, y), mInitJoystickPositionR);
        mActivePointerId = event.getPointerId(pointerIndex);
        
        switch (event.getActionMasked()) {
            
            // Frist finger pressing down
            // 1. Reset the state of the control component activated
            // 2. Set the joystick position if a joystick is activated
            // 3. Do not send joystick messages till the first moving action(Prevent touching by mistake)
            // 4. Start to count the pressed time if a button is pressed
            // 5. Joysticks disabled till the first moving action
            case MotionEvent.ACTION_DOWN: {
                if (mJoystickWheelRadius >= distanceToLeft) {
                    mJoystickIdL = mActivePointerId;
                    isJoystickLocked = true;
                    mJoystickPositionL.set(x, y);
                }
                else if (mJoystickWheelRadius >= distanceToRight) {
                    mJoystickIdR = mActivePointerId;
                    isJoystickLocked = true;
                    mJoystickPositionR.set(x, mInitJoystickPositionR.y);
                }
                else if (mButtonRadius >= distanceToShootingButton) {
                    mButtonPressedId = mActivePointerId;
                    mPowerBar = 0;
                    mPowerBarStep = 3;
                    mButtonDownTime = SystemClock.uptimeMillis();
                    mButton = ActionListener.SHOOTING;
                }
                else if (mButtonRadius >= distanceToDribblingButton) {
                    mButtonPressedId = mActivePointerId;
                    mPowerBar = 0;
                    mPowerBarStep = 3;
                    mButtonDownTime = SystemClock.uptimeMillis();
                    mButton = ActionListener.DRIBBLING;
                }
                break;
            }
            
            // Second finger pressing down
            // 1. Reset the state of the second control component activated
            // 2. Set the joystick position if another joystick is activated
            // 3. Do not send joystick messages till the first moving action(Prevent touching by mistake)
            // 4. Two joysticks can be activated simultaneously
            // 5. Restart to count the pressed time if another button is pressed
            // 6. There is no way that two buttons are pressed simultaneously
            case MotionEvent.ACTION_POINTER_DOWN: {
                if (mJoystickWheelRadius >= distanceToLeft) {
                    if (INVALID_JOYSTICK_ID == mJoystickIdL) {
                        mJoystickIdL = mActivePointerId;
                        isJoystickLocked = true;
                        mJoystickPositionL.set(x, y);
                    }
                }
                else if (mJoystickWheelRadius >= distanceToRight) {
                    if (INVALID_JOYSTICK_ID == mJoystickIdR) {
                        mJoystickIdR = mActivePointerId;
                        isJoystickLocked = true;
                        mJoystickPositionR.set(x, mInitJoystickPositionR.y);
                    }
                }
                else if (mButtonRadius >= distanceToShootingButton) {
                    mButtonPressedId = mActivePointerId;
                    mPowerBar = 0;
                    mPowerBarStep = 3;
                    mButtonDownTime = SystemClock.uptimeMillis();
                    mButton = ActionListener.SHOOTING;
                }
                else if (mButtonRadius >= distanceToDribblingButton) {
                    mButtonPressedId = mActivePointerId;
                    mPowerBar = 0;
                    mPowerBarStep = 3;
                    mButtonDownTime = SystemClock.uptimeMillis();
                    mButton = ActionListener.DRIBBLING;
                }
                break;
            }
            
            // Finger moving on the screen
            // 1. Google did not handle the case of multi-movement so we have to traverse the moving pointers(Is this a bug?)
            // 2. Unlock the moving joystick(s) to send position message(s)
            case MotionEvent.ACTION_MOVE: {
                int pointerCount = event.getPointerCount();
                for (pointerIndex = 0; pointerCount != pointerIndex; ++pointerIndex) {
                    x = event.getX(pointerIndex);
                    y = event.getY(pointerIndex);
                    distanceToShootingButton = Graphics.distance(new PointF(x, y), mShootingButtonPosition);
                    distanceToDribblingButton = Graphics.distance(new PointF(x, y), mDribblingButtonPosition);
                    distanceToLeft = Graphics.distance(new PointF(x, y), mInitJoystickPositionL);
                    distanceToRight = Graphics.distance(new PointF(x, y), mInitJoystickPositionR);
                    mActivePointerId = event.getPointerId(pointerIndex);
                    
                    if (mActivePointerId == mJoystickIdL) {
                        isJoystickLocked = false;
                        if (mJoystickWheelRadius < distanceToLeft)
                            mJoystickPositionL = Graphics.borderPoint(new PointF(x, y), mInitJoystickPositionL, mJoystickWheelRadius);
                        else
                            mJoystickPositionL.set(x, y);
                    }
                    else if (mActivePointerId == mJoystickIdR) {
                        isJoystickLocked = false;
                        if (mJoystickWheelRadius < distanceToRight) {
                            if (mInitJoystickPositionR.x < x)
                                mJoystickPositionR.set(mInitJoystickPositionR.x + mJoystickWheelRadius, mInitJoystickPositionR.y);
                            else if (mInitJoystickPositionR.x > x)
                                mJoystickPositionR.set(mInitJoystickPositionR.x - mJoystickWheelRadius, mInitJoystickPositionR.y);
                        }
                        else
                            mJoystickPositionR.set(x, mInitJoystickPositionR.y);
                    }
                }
                break;
            }
            
            // Penultimate finger released
            // Disable the released control component
            case MotionEvent.ACTION_POINTER_UP: {
                if (mActivePointerId == mJoystickIdL) {
                    mJoystickIdL = INVALID_JOYSTICK_ID;
                    mJoystickPositionL.set(mInitJoystickPositionL.x, mInitJoystickPositionL.y);
                }
                else if (mActivePointerId ==  mJoystickIdR) {
                    mJoystickIdR = INVALID_JOYSTICK_ID;
                    mJoystickPositionR.set(mInitJoystickPositionR.x, mInitJoystickPositionR.y);
                }
                else if (mActivePointerId == mButtonPressedId) {
                    mButtonPressedId = INVALID_BUTTON_ID;
                }
                break;
            }
            
            // Last finger released
            // Disable the released control component
            case MotionEvent.ACTION_UP: {
                if (mActivePointerId == mJoystickIdL) {
                    mJoystickIdL = INVALID_JOYSTICK_ID;
                    mJoystickPositionL.set(mInitJoystickPositionL.x, mInitJoystickPositionL.y);
                }
                else if (mActivePointerId ==  mJoystickIdR) {
                    mJoystickIdR = INVALID_JOYSTICK_ID;
                    mJoystickPositionR.set(mInitJoystickPositionR.x, mInitJoystickPositionR.y);
                }
                else if (mActivePointerId == mButtonPressedId) {
                    mButtonPressedId = INVALID_BUTTON_ID;
                }
                break;
            }
        }
        
        // Send the messages through socket
        if (null != mActionListener) {
            if (false == isJoystickLocked) {
                Graphics.Vector v;
                v = new Graphics.Vector(mInitJoystickPositionL, mJoystickPositionL);
                mActionListener.onJoystickPositionChanged(ActionListener.LEFT, v.dir() * 10000, v.mod() / mJoystickWheelRadius * 100);
                v = new Graphics.Vector(mInitJoystickPositionR, mJoystickPositionR);
                mActionListener.onJoystickPositionChanged(ActionListener.RIGHT, v.dir() * 10000, v.modX() / mJoystickWheelRadius * 100);
            }
            
            if (ActionListener.INVALID != mButton && INVALID_BUTTON_ID == mButtonPressedId) { // Button been pressed and released now
                mActionListener.onButtonClicked(mButton, mPowerBar);
                mButton = ActionListener.INVALID;
            }
        }
        
        super.onTouchEvent(event);
        return true;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return true;
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        return true;
    }

    public interface ActionListener {
        public static final int INVALID = 2;
        public static final int LEFT = 0;
        public static final int RIGHT = 1;
        public static final int SHOOTING = 0;
        public static final int DRIBBLING = 1;
        public void onJoystickPositionChanged(int joystick, float radian, float speed);
        public void onButtonClicked(int button, int power);
        public void onVolunmKeyDown(int keyCode, KeyEvent event);
    }
}
