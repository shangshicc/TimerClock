package com.shangshicc.android.timerclock;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by shangshicc on 2016/4/19.
 */
public class TimerClockView extends View implements View.OnClickListener{
    private Context mContext;
    private Paint mCirclePaint;
    private Paint mTextPaint;
    private int mClockColor;
    private int mSeconds;
    private float mCircleRad;
    private int mTextSize;
    private int mTextColor;
    private String mText;

    private ClockHandler mHandler;
    private Timer mTimer;

    private int mTimerClockState;
    public static final int TIMER_CLOCK_STOP = 0;
    public static final int TIMER_CLOCK_START = 1;

    public static final int UPDATE_CLOCK_WHAT = 0;
    public TimerClockView(Context context) {
        super(context);
        mContext = context;

        mCirclePaint = new Paint();
        mTextPaint = new Paint();

        configData();
        configPaint();
        setOnClickListener(this);
    }

    public TimerClockView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TimerClockView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mCirclePaint = new Paint();
        mTextPaint = new Paint();

        mContext = context;
        TypedArray ta = context.obtainStyledAttributes(attrs,R.styleable.TimerClockView);
        parseAttrs(ta);
        configPaint();
        setOnClickListener(this);
    }

    public void configData(){
        mClockColor = ContextCompat.getColor(mContext,R.color.circle_color);
        mSeconds = 60;
        mCircleRad = 200;
        mTextSize = 60;
        mText = mContext.getString(R.string.start_text);
        mTextColor = Color.BLACK;
    }

    public void parseAttrs(TypedArray ta){
        mClockColor = ta.getColor(R.styleable.TimerClockView_clock_color,
                ContextCompat.getColor(mContext,R.color.circle_color));
        mSeconds = ta.getInt(R.styleable.TimerClockView_start_seconds, 60);
        mCircleRad = ta.getFloat(R.styleable.TimerClockView_circle_radius, 200);
        mTextSize = ta.getInt(R.styleable.TimerClockView_text_size, 60);
        mText = ta.getString(R.styleable.TimerClockView_start_text);
        mTextColor = ta.getColor(R.styleable.TimerClockView_text_color,Color.BLACK);
        if(mText == null || mText.equals("")){
            mText = mContext.getString(R.string.start_text);
        }

        ta.recycle();
    }

    public void configPaint(){
        configClockPaint();
        configTextPaint();
    }

    public void configClockPaint(){
        mCirclePaint.setAntiAlias(true);
        mCirclePaint.setStyle(Paint.Style.STROKE);
        mCirclePaint.setStrokeWidth(5);
        mCirclePaint.setTextSize(mTextSize);
        mCirclePaint.setColor(mClockColor);
    }

    public void configTextPaint(){
        mTextPaint.setAntiAlias(true);
        mTextPaint.setTextAlign(Paint.Align.LEFT);
        mTextPaint.setTextSize(mTextSize);
        mTextPaint.setColor(mTextColor);
    }

    @Override
    protected void onDraw(final Canvas canvas) {
        super.onDraw(canvas);
        canvas.translate(getMeasuredWidth() / 2, getMeasuredHeight() / 2);
        canvas.drawCircle(0, 0, mCircleRad, mCirclePaint);
        drawText(mTextPaint, canvas, mText);
    }

    private void drawText(Paint paint, Canvas canvas, String text){
        Rect bounds = new Rect();
        paint.getTextBounds(text, 0, text.length(), bounds);
        canvas.drawText(text, -bounds.width() / 2, bounds.height() / 2, paint);
    }

    @Override
    public void onClick(View v) {
        if(mTimerClockState == TIMER_CLOCK_STOP) {
            mTimer = new Timer(true);
            mTimer.schedule(new ClockTimerTask(),1000,1000);
            invalidate();
            mTimerClockState = TIMER_CLOCK_START;
        }else{
            mTimer.cancel();
            mTimer = null;
            mTimerClockState = TIMER_CLOCK_STOP;
        }
    }

    private final class ClockTimerTask extends TimerTask{
        public ClockTimerTask(){
        }
        @Override
        public void run() {
            mHandler = new ClockHandler();
            mHandler.sendEmptyMessage(UPDATE_CLOCK_WHAT);

        }
    }

    private final class ClockHandler extends Handler{
        public ClockHandler(){
            super(Looper.getMainLooper());
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(msg.what == UPDATE_CLOCK_WHAT) {
                if(mSeconds < 0) {
                    mTimer.cancel();
                    mTimerClockState = TIMER_CLOCK_STOP;
                    return;
                }
                mText = String.valueOf(mSeconds);
                mSeconds--;
                invalidate();
            }
        }
    }

    public void removeHandler(){
        mHandler.removeCallbacksAndMessages(null);
    }


}