package com.rockidog.networkdemo;

import com.rockidog.networkdemo.utils.Graphics;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PointF;
import android.graphics.PorterDuff.Mode;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;

public class PanelView extends SurfaceView implements Runnable, Callback {
    private boolean isPaused = false;
    private Paint mPaint = new Paint();
    private Thread mThread = new Thread(this);
    private SurfaceHolder mHolder = getHolder();
    private float mJoystickRadius = 80;
    private float mJoystickWheelRadius = 150;
    private PointF mInitJoystickPosition = new PointF(300, 300);
    private PointF mJoystickPosition = new PointF(mInitJoystickPosition.x, mInitJoystickPosition.y);
    private JoystickListener mJoystickListener = null;

    {
        mHolder.addCallback(this);
        mHolder.setFormat(PixelFormat.TRANSPARENT);
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
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

    @Override
    public void run() {
        Canvas canvas = null;
        while (false == isPaused) {
            canvas = mHolder.lockCanvas();
            canvas.drawColor(Color.TRANSPARENT, Mode.CLEAR);
            mPaint.setColor(Color.GRAY);
            canvas.drawCircle(mInitJoystickPosition.x, mInitJoystickPosition.y, mJoystickWheelRadius, mPaint);
            mPaint.setColor(Color.RED);
            canvas.drawCircle(mJoystickPosition.x, mJoystickPosition.y, mJoystickRadius, mPaint);
            if (null != canvas)
                mHolder.unlockCanvasAndPost(canvas);
            
            try {
                Thread.sleep(30);
            }
            catch(InterruptedException e)
            {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
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
        float x = event.getX();
        float y = event.getY();
        float distanceToCenter = Graphics.distance(new PointF(x, y), mInitJoystickPosition);
        switch (event.getAction()) {
            case (MotionEvent.ACTION_DOWN):
                if (distanceToCenter > mJoystickWheelRadius)
                    return false;
                return true;
            case (MotionEvent.ACTION_MOVE):
                if (distanceToCenter > mJoystickWheelRadius)
                    mJoystickPosition = Graphics.borderPoint(new PointF(x, y), mInitJoystickPosition, mJoystickWheelRadius);
                else
                    mJoystickPosition.set(x, y);
                if (null != mJoystickListener) {
                    Graphics.Vector v = new Graphics.Vector(mInitJoystickPosition, mJoystickPosition);
                    mJoystickListener.onJoystickPositionChanged(v.dir(), v.mod() / mJoystickWheelRadius * 100);
                }
                return true;
            case (MotionEvent.ACTION_UP):
                mJoystickPosition.set(mInitJoystickPosition.x, mInitJoystickPosition.y);
                return true;
            default:
                return false;
        }
    }

    public interface JoystickListener {
        public void onJoystickPositionChanged(float radian, float speed);
    }
}
