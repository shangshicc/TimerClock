package com.shangshicc.android.timerclock;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
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
    private int mSecondsCache;
    private float mCircleRad;
    private int mTextSize;
    private int mTextColor;
    private String mText;
    private RectF mRectF;
    private float mUpdateAngle;
    private volatile float mAngle;
    private ClockHandler mHandler;
    private Timer mTimer;

    private int mTimerClockState;
    public static final int TIMER_CLOCK_STOP = 0;
    public static final int TIMER_CLOCK_START = 1;
    public static final int TIMER_CLOCK_PAUSE = 2;

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

        mSecondsCache = mSeconds;

        mAngle = 0;
        mUpdateAngle = 360 / mSeconds;
        mRectF = new RectF(-(mCircleRad + 10),-(mCircleRad + 10),mCircleRad + 10,mCircleRad + 10);
    }

    public void configData(){
        mClockColor = ContextCompat.getColor(mContext,R.color.circle_color);
        mSeconds = 60;
        mCircleRad = 200;
        mTextSize = 60;
        mText = mContext.getString(R.string.start_text);
        mTextColor = Color.BLACK;

        mSecondsCache = mSeconds;

        mUpdateAngle = 360 / mSeconds;
        mAngle = 0;
        mRectF = new RectF(-(mCircleRad + 10),-(mCircleRad + 10),mCircleRad + 10,mCircleRad + 10);
    }

    public void parseAttrs(TypedArray ta){
        mClockColor = ta.getColor(R.styleable.TimerClockView_clock_color,
                ContextCompat.getColor(mContext,R.color.circle_color));
        mSeconds = ta.getInt(R.styleable.TimerClockView_start_seconds, 60);
        mCircleRad = ta.getFloat(R.styleable.TimerClockView_circle_radius, 200);
        mTextSize = ta.getInt(R.styleable.TimerClockView_text_size, 60);
        mText = ta.getString(R.styleable.TimerClockView_start_text);
        mTextColor = ta.getColor(R.styleable.TimerClockView_text_color, Color.BLACK);
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
        mCirclePaint.setColor(mClockColor);
        mCirclePaint.setStrokeWidth(10);
    }

    public void configTextPaint(){
        mTextPaint.setAntiAlias(true);
        mTextPaint.setTextAlign(Paint.Align.LEFT);
        mTextPaint.setTextSize(mTextSize);
        mTextPaint.setColor(mTextColor);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int measureWidth = measureLength(widthMeasureSpec);
        int measureHeight = measureLength(heightMeasureSpec);
        setMeasuredDimension(measureWidth,measureHeight);
    }


    private int measureLength(int measureSpec){
        int measureMode = MeasureSpec.getMode(measureSpec);
        int measureLength = MeasureSpec.getSize(measureSpec);

        if(measureMode == MeasureSpec.EXACTLY){
            return measureLength;
        }else{
            int result = 450;
            if(measureMode == MeasureSpec.AT_MOST){
                result = Math.min(result,measureLength);
            }

            return result;
        }
    }

    @Override
    protected void onDraw(final Canvas canvas) {
        super.onDraw(canvas);
        canvas.translate(getMeasuredWidth() / 2, getMeasuredHeight() / 2);
        canvas.drawCircle(0, 0, mCircleRad, mCirclePaint);
        canvas.drawCircle(0, 0, mCircleRad + 20, mCirclePaint);

        if(mTimerClockState == TIMER_CLOCK_START) {
            canvas.drawArc(mRectF, 0, mAngle, false, mCirclePaint);
        }else if(mTimerClockState == TIMER_CLOCK_PAUSE){
            mAngle = mUpdateAngle * (mSecondsCache - mSeconds);
            canvas.drawArc(mRectF, 0, mAngle, false, mCirclePaint);
        }

        Rect bounds = new Rect();
        mTextPaint.getTextBounds(mText, 0, mText.length(), bounds);
        canvas.drawText(mText, -bounds.width() / 2, bounds.height() / 2, mTextPaint);
    }

    @Override
    public void onClick(View v) {
        if(mTimerClockState == TIMER_CLOCK_STOP) {
            mTimer = new Timer(true);
            mTimer.schedule(new ClockTimerTask(), 1000, 1000);
            mSeconds = mSecondsCache;
            mTimerClockState = TIMER_CLOCK_START;
            mAngle = 0;
        }else if(mTimerClockState == TIMER_CLOCK_START){
            if(mSeconds == 0){

                mTimerClockState = TIMER_CLOCK_STOP;
            }else{
                mTimerClockState = TIMER_CLOCK_PAUSE;
            }
            mTimer.cancel();
            mTimer = null;
        }else if(mTimerClockState == TIMER_CLOCK_PAUSE){
            mTimer = new Timer(true);
            mTimer.schedule(new ClockTimerTask(), 1000, 1000);
            mTimerClockState = TIMER_CLOCK_START;
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
                mAngle += mUpdateAngle;
                invalidate();
            }
        }
    }

    public void removeHandler(){
        mHandler.removeCallbacksAndMessages(null);
    }



}
