package com.rockidog.networkdemo;

import com.rockidog.networkdemo.utils.Graphics;

import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;

public class PanelView extends SurfaceView implements Runnable, Callback {
    private static final int INVALID_POINTER_ID = -1;
    private static final int INVALID_JOYSTICK_ID = -1;

    private float mJoystickRadius;
    private float mJoystickWheelRadius;
    private PointF mInitJoystickPositionL;
    private PointF mJoystickPositionL;
    private PointF mInitJoystickPositionR;
    private PointF mJoystickPositionR;

    private boolean isPaused = false;
    private Paint mPaint = new Paint();
    private MaskFilter mBlur = new BlurMaskFilter(16, BlurMaskFilter.Blur.SOLID);
    private Thread mThread = new Thread(this);
    private SurfaceHolder mHolder = getHolder();
    private int mJoystickIdL = INVALID_JOYSTICK_ID;
    private int mJoystickIdR = INVALID_JOYSTICK_ID;
    private int mActivePointerId = INVALID_POINTER_ID;
    private int mJoystick = JoystickListener.INVALID;

    private JoystickListener mJoystickListener = null;
    private SlidingListener mSlidingListener = null;

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

    public void setJoystickListener(JoystickListener listener) {
        mJoystickListener = listener;
    }

    public void setSlidingListener(SlidingListener listener) {
        mSlidingListener = listener;
    }

    @Override
    public void run() {
        Canvas canvas = null;
        while (false == isPaused) {
            canvas = mHolder.lockCanvas();
            canvas.drawColor(Color.WHITE, PorterDuff.Mode.SRC);
            mPaint.setColor(Color.GRAY);
            mPaint.setMaskFilter(null);
            canvas.drawCircle(mInitJoystickPositionL.x, mInitJoystickPositionL.y, mJoystickWheelRadius, mPaint);
            canvas.drawCircle(mInitJoystickPositionR.x, mInitJoystickPositionR.y, mJoystickWheelRadius, mPaint);
            mPaint.setColor(Color.GREEN);
            if (INVALID_JOYSTICK_ID != mJoystickIdL)
                mPaint.setMaskFilter(mBlur); else
                mPaint.setMaskFilter(null);
            canvas.drawCircle(mJoystickPositionL.x, mJoystickPositionL.y, mJoystickRadius, mPaint);
            if (INVALID_JOYSTICK_ID != mJoystickIdR)
                mPaint.setMaskFilter(mBlur);
            else
                mPaint.setMaskFilter(null);
            canvas.drawCircle(mJoystickPositionR.x, mJoystickPositionR.y, mJoystickRadius, mPaint);
            if (null != canvas)
                mHolder.unlockCanvasAndPost(canvas);
            
            try {
                Thread.sleep(30);
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
        mInitJoystickPositionL = new PointF(w / 4, h / 3 * 2);
        mJoystickPositionL = new PointF(mInitJoystickPositionL.x, mInitJoystickPositionL.y);
        mInitJoystickPositionR = new PointF(w / 4 * 3, h / 3 * 2);
        mJoystickPositionR = new PointF(mInitJoystickPositionR.x, mInitJoystickPositionR.y);
        mJoystickWheelRadius = w / 8;
        mJoystickRadius = w / 16;
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
        float distanceToLeft = Graphics.distance(new PointF(x, y), mInitJoystickPositionL);
        float distanceToRight = Graphics.distance(new PointF(x, y), mInitJoystickPositionR);
        mActivePointerId = event.getPointerId(pointerIndex);
        
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN: {
                if (mJoystickWheelRadius < distanceToLeft && mJoystickWheelRadius < distanceToRight) {
                    mJoystickIdL = INVALID_JOYSTICK_ID;
                    mJoystickIdR = INVALID_JOYSTICK_ID;
                    return false;
                }
                else if (mJoystickWheelRadius > distanceToLeft) {
                    mJoystickIdL = mActivePointerId;
                    mJoystickIdR = INVALID_JOYSTICK_ID;
                    mJoystick = JoystickListener.LEFT;
                    mJoystickPositionL.set(x, y);
                }
                else if (mJoystickWheelRadius > distanceToRight) {
                    mJoystickIdR = mActivePointerId;
                    mJoystickIdL = INVALID_JOYSTICK_ID;
                    mJoystick = JoystickListener.RIGHT;
                    mJoystickPositionR.set(x, y);
                }
                break;
            }
            
            case MotionEvent.ACTION_POINTER_DOWN: {
                if (mJoystickWheelRadius < distanceToLeft && mJoystickWheelRadius < distanceToRight)
                    return false;
                else if (mJoystickWheelRadius > distanceToLeft) {
                    if (INVALID_JOYSTICK_ID == mJoystickIdL) {
                        mJoystickIdL = mActivePointerId;
                        mJoystick = JoystickListener.LEFT;
                        mJoystickPositionL.set(x, y);
                    }
                    else
                        return false;
                }
                else if (mJoystickWheelRadius > distanceToRight) {
                    if (INVALID_JOYSTICK_ID == mJoystickIdR) {
                        mJoystickIdR = mActivePointerId;
                        mJoystick = JoystickListener.RIGHT;
                        mJoystickPositionR.set(x, y);
                    }
                    else
                        return false;
                }
                break;
            }
            
            case MotionEvent.ACTION_MOVE: {
                int pointerCount = event.getPointerCount();
                for (pointerIndex = 0; pointerCount != pointerIndex; ++pointerIndex) {
                    x = event.getX(pointerIndex);
                    y = event.getY(pointerIndex);
                    distanceToLeft = Graphics.distance(new PointF(x, y), mInitJoystickPositionL);
                    distanceToRight = Graphics.distance(new PointF(x, y), mInitJoystickPositionR);
                    mActivePointerId = event.getPointerId(pointerIndex);
                    if (mActivePointerId == mJoystickIdL) {
                        mJoystick = JoystickListener.LEFT;
                        if (mJoystickWheelRadius < distanceToLeft)
                            mJoystickPositionL = Graphics.borderPoint(new PointF(x, y), mInitJoystickPositionL, mJoystickWheelRadius);
                        else
                            mJoystickPositionL.set(x, y);
                    }
                    else if (mActivePointerId == mJoystickIdR) {
                        mJoystick = JoystickListener.RIGHT;
                        if (mJoystickWheelRadius < distanceToRight)
                            mJoystickPositionR = Graphics.borderPoint(new PointF(x, y), mInitJoystickPositionR, mJoystickWheelRadius);
                        else
                            mJoystickPositionR.set(x, y);
                    }
                }
                if (1 > pointerCount)
                    mJoystick = JoystickListener.BOTH;
                break;
            }
            
            case MotionEvent.ACTION_POINTER_UP: {
                if (mActivePointerId == mJoystickIdL) {
                    mJoystickIdL = INVALID_JOYSTICK_ID;
                    mJoystick = JoystickListener.LEFT;
                    mJoystickPositionL.set(mInitJoystickPositionL.x, mInitJoystickPositionL.y);
                }
                else if (mActivePointerId ==  mJoystickIdR) {
                    mJoystickIdR = INVALID_JOYSTICK_ID;
                    mJoystick = JoystickListener.RIGHT;
                    mJoystickPositionR.set(mInitJoystickPositionR.x, mInitJoystickPositionR.y);
                }
                break;
            }
            
            case MotionEvent.ACTION_UP: {
                if (mActivePointerId == mJoystickIdL) {
                    mJoystickIdL = INVALID_JOYSTICK_ID;
                    mJoystick = JoystickListener.LEFT;
                    mJoystickPositionL.set(mInitJoystickPositionL.x, mInitJoystickPositionL.y);
                }
                else if (mActivePointerId ==  mJoystickIdR) {
                    mJoystickIdR = INVALID_JOYSTICK_ID;
                    mJoystick = JoystickListener.RIGHT;
                    mJoystickPositionR.set(mInitJoystickPositionR.x, mInitJoystickPositionR.y);
                }
                break;
            }
        }
        
        if (null != mJoystickListener) {
            Graphics.Vector v;
            switch (mJoystick) {
                case JoystickListener.LEFT:
                    v = new Graphics.Vector(mInitJoystickPositionL, mJoystickPositionL);
                    mJoystickListener.onJoystickPositionChanged(JoystickListener.LEFT, v.dir() * 10000, v.mod() / mJoystickWheelRadius * 100);
                    break;
                case JoystickListener.RIGHT:
                    v = new Graphics.Vector(mInitJoystickPositionR, mJoystickPositionR);
                    mJoystickListener.onJoystickPositionChanged(JoystickListener.RIGHT, v.dir() * 10000, v.mod() / mJoystickWheelRadius * 100);
                    break;
                case JoystickListener.BOTH:
                    v = new Graphics.Vector(mInitJoystickPositionL, mJoystickPositionL);
                    mJoystickListener.onJoystickPositionChanged(JoystickListener.LEFT, v.dir() * 10000, v.mod() / mJoystickWheelRadius * 100);
                    v = new Graphics.Vector(mInitJoystickPositionR, mJoystickPositionR);
                    mJoystickListener.onJoystickPositionChanged(JoystickListener.RIGHT, v.dir() * 10000, v.mod() / mJoystickWheelRadius * 100);
                    break;
            }
            mJoystick = JoystickListener.INVALID;
        }
        
        return true;
    }

    public interface JoystickListener {
        public static final int INVALID = -1;
        public static final int LEFT = 0;
        public static final int RIGHT = 1;
        public static final int BOTH = 2;
        public void onJoystickPositionChanged(int joystick, float radian, float speed);
    }

    public interface SlidingListener {
        public static final int INVALID = -1;
        public static final int SLIDE_UP = 0;
        public static final int SLIDE_DOWN = 0;
        public void onSlidedAway(int direction, float speed);
    }
}
