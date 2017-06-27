package com.library.battery;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.LinearInterpolator;

import battery.single.com.library.R;

/**
 * Created by xiangcheng on 2016/3/8.
 * 电量显示的view
 */
public class BatteryView extends View {

    private static final String TAG = BatteryView.class.getSimpleName();
    private int mWidth, mHeight;
    //外圈的半径
    private int mRadius;
    //弧度的描述
    private float radian;
    //波浪的路径
    private Path mBatteryPath;
    //波浪的画笔
    private Paint mBatteryPaint;
    //一个周期的波浪长度
    private int mWaveWidth;
    //整个屏幕占据的几个波浪
    private int fullCount;
    //第一个波浪在横轴上平移的位置
    private float firstWaveOffset;
    //第一个波浪在x正半轴平移的动画
    private ValueAnimator firstWaveBehind;
    //第一个波浪在x负半轴平移的动画
    private ValueAnimator firstWaveFront;
    //第二个波浪在x负半轴平移的动画
    private ValueAnimator secondWaveFront;
    //第二个波浪在x正半轴平移的动画
    private ValueAnimator secondWaveBehind;
    //第二个波浪在横轴上平移的位置
    private float secondWaveOffset;
    //电量比
    private int batteryLevel;
    //最大电量数
    private int maxLevel;
    //圆的外圈宽度
    private int circleWidth;
    //外圈的画笔
    private Paint mBgPaint;
    //文字部分的画笔
    private Paint mLevelPaint;
    //外圆的path
    private Path mBgPath;
    //用于裁剪的path,画出来的波浪由于是方形的，因此按照此path来进行裁剪
    private Path mClipPath;
    //电量的高度显示，通过圆的直径比例来计算该高度
    private float mLevelHeight;
    //文字的动画，对alpha的操作
    private ValueAnimator textAnim;
    //文字的透明度
    private float textAlpha;
    //电量显示的text
    private String levelText;
    //电量状态的画笔
    private Paint mStatusPaint;
    //电量状态的text
    private String statusText;
    // 充电中
    public static final int STATUS_CHARGING = 1;
    //未充电中
    public static final int STATUS_UNCHARGING = 2;
    //当前状态
    private int mStatus = STATUS_UNCHARGING;
    //status的text
    private String charging_text = "充电中...";
    private String fill_text = "充电已完成";
    private String using_text = "使用中";
    private String lowpower_text = "低电量了";

    private boolean isLowPower;
    //低电量时的动画,出现一闪一闪的动画
    private ValueAnimator lowPowerAnimator;
    // 只有在低电量的情况下才会替换该值的
    private boolean isShowStatusAndLevel;

    private int waveCycleTime;
    //电量是否充满
    private boolean isFull;

    private int waveColor;
    private int statusColor;
    private float statusTextSize;
    private int levelColor;
    private float levelTextSize;

    private int lowPowerColor;

    private float lowperPercent;

    //add 2017/6/27,充电的时候波浪滚动的方向
    private int charginDirection;

    public BatteryView(Context context) {
        this(context, null);
    }

    public BatteryView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BatteryView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initArgus(context, attrs);
        init();
    }

    private void initArgus(Context context, AttributeSet attrs) {
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.BatteryView);
        try {
            circleWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, getContext().getResources().getDisplayMetrics());
            circleWidth = (int) array.getDimension(R.styleable.BatteryView_ring_stroke_width, circleWidth);
            mRadius = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 140, getContext().getResources().getDisplayMetrics());
            mRadius = (int) array.getDimension(R.styleable.BatteryView_ring_radius, mRadius);
            mWaveWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 100, getContext().getResources().getDisplayMetrics());
            mWaveWidth = (int) array.getDimension(R.styleable.BatteryView_wave_width, mWaveWidth);
            radian = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, getContext().getResources().getDisplayMetrics());
            radian = array.getDimension(R.styleable.BatteryView_wave_peek, radian);
            waveCycleTime = 1500;
            waveCycleTime = array.getInt(R.styleable.BatteryView_wave_cycle_time, waveCycleTime);
            waveColor = Color.parseColor("#aeeae7");
            waveColor = array.getColor(R.styleable.BatteryView_wave_color, waveColor);
            statusColor = Color.WHITE;
            statusColor = array.getColor(R.styleable.BatteryView_battery_status_color, statusColor);
            statusTextSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 20, getContext().getResources().getDisplayMetrics());
            statusTextSize = array.getDimension(R.styleable.BatteryView_battery_status_size, statusTextSize);
            levelColor = Color.WHITE;
            levelColor = array.getColor(R.styleable.BatteryView_battery_level_color, levelColor);
            levelTextSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 40, getContext().getResources().getDisplayMetrics());
            levelTextSize = array.getDimension(R.styleable.BatteryView_battery_level_size, levelTextSize);
            lowPowerColor = Color.RED;
            lowPowerColor = array.getColor(R.styleable.BatteryView_battery_lowpower_color, lowPowerColor);
            charging_text = array.getString(R.styleable.BatteryView_battery_charging_text) == null ? charging_text : "";
            fill_text = array.getString(R.styleable.BatteryView_battery_fill_text) == null ? fill_text : "";
            using_text = array.getString(R.styleable.BatteryView_battery_using_text) == null ? using_text : "";
            lowpower_text = array.getString(R.styleable.BatteryView_battery_lowpower_text) == null ? lowpower_text : "";
            lowperPercent = 0.2f;
            lowperPercent = array.getFraction(R.styleable.BatteryView_battery_lowpower_percnet, 1, 1, lowperPercent);
            charginDirection = 1;
            charginDirection = array.getInt(R.styleable.BatteryView_charging_direction, charginDirection);
            if (lowperPercent > 1) {
                throw new IllegalArgumentException("lowperPercent must be less than 1");
            }
        } finally {
            array.recycle();
        }

    }

    private void init() {
        mBatteryPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBatteryPaint.setColor(waveColor);
        mBatteryPaint.setStyle(Paint.Style.FILL);

        mBgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBgPaint.setColor(Color.WHITE);
        mBgPaint.setStyle(Paint.Style.STROKE);
        mBgPaint.setStrokeWidth(circleWidth);

        mLevelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mLevelPaint.setColor(levelColor);
        mLevelPaint.setStyle(Paint.Style.FILL);
        mLevelPaint.setStrokeWidth(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 0.5f, getContext().getResources().getDisplayMetrics()));
        mLevelPaint.setTextSize(levelTextSize);

        mStatusPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mStatusPaint.setColor(statusColor);
        mStatusPaint.setStyle(Paint.Style.FILL);
        mStatusPaint.setStrokeWidth(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, getContext().getResources().getDisplayMetrics()));
        mStatusPaint.setTextSize(statusTextSize);

        maxLevel = 100;
        mBatteryPath = new Path();
        mBgPath = new Path();
        mClipPath = new Path();
    }

    private void initStatus() {
        //充电情况下
        if (STATUS_CHARGING == mStatus) {
            isLowPower = false;
            if (lowPowerAnimator != null && lowPowerAnimator.isRunning()) {
                lowPowerAnimator.end();
            }
            if (batteryLevel < maxLevel) {
                isFull = false;
                statusText = charging_text;
                if (firstWaveBehind != null && !firstWaveBehind.isRunning() && !firstWaveHasStart) {
                    Log.d(TAG, "firstWaveBehind启动了");
                    firstWaveBehind.start();
                }
                if (secondWaveFront != null && !secondWaveFront.isRunning() && !secondWaveHasStart) {
                    Log.d(TAG, "secondWaveFront启动了");
                    secondWaveFront.start();
                }
            } else {
                isFull = true;
                statusText = fill_text;
                firstWaveOffset = 0;
                secondWaveOffset = -(mWaveWidth + fullCount * mWaveWidth);
                if (firstWaveBehind != null && firstWaveBehind.isRunning()) {
                    firstWaveBehind.end();
                }
                if (secondWaveFront != null && secondWaveFront.isRunning()) {
                    secondWaveFront.end();
                }
                if (firstWaveFront != null && firstWaveFront.isRunning()) {
                    firstWaveFront.end();
                }
                if (secondWaveBehind != null && secondWaveBehind.isRunning()) {
                    secondWaveBehind.end();
                }
                firstWaveHasStart = false;
                secondWaveHasStart = false;
            }

        } else {
            firstWaveOffset = 0;
            secondWaveOffset = -(mWaveWidth + fullCount * mWaveWidth);
            isFull = false;
            firstWaveHasStart = false;
            secondWaveHasStart = false;
            if (firstWaveBehind != null && firstWaveBehind.isRunning()) {
                firstWaveBehind.end();
            }
            if (secondWaveFront != null && secondWaveFront.isRunning()) {
                secondWaveFront.end();
            }
            if (firstWaveFront != null && firstWaveFront.isRunning()) {
                firstWaveFront.end();
            }
            if (secondWaveBehind != null && secondWaveBehind.isRunning()) {
                secondWaveBehind.end();
            }

            if (batteryLevel <= maxLevel * lowperPercent) {
                statusText = lowpower_text;
                isLowPower = true;
                if (lowPowerAnimator != null && !lowPowerAnimator.isRunning()) {
                    lowPowerAnimator.start();
                }
            } else {
                statusText = using_text;
                isLowPower = false;
                if (lowPowerAnimator != null && lowPowerAnimator.isRunning()) {
                    lowPowerAnimator.end();
                }
                invalidate();
            }

        }
    }

    public void setChanges(int mStatus, int batteryLevel) {
        Log.d("TAG", "setChanges");
        if (mStatus != this.mStatus || batteryLevel != this.batteryLevel) {
            this.mStatus = mStatus;
            this.batteryLevel = batteryLevel;
            calculatePosition();
            initStatus();
        }
    }

    /**
     * 用于计算相关的点
     */
    private void calculatePosition() {
        //下面多余的部分坐标
        float topRemain = mHeight / 2 - getPaddingTop() - (mRadius + circleWidth);
        float bottomRemain = mHeight / 2 - getPaddingBottom() - (mRadius + circleWidth);
        //通过电量比算出此时波浪占据的高度
        if (batteryLevel <= 70) {
            mLevelHeight = 2 * mRadius * (maxLevel - batteryLevel) / maxLevel;
            mLevelHeight += topRemain + circleWidth;
        } else {
            mLevelHeight = 2 * mRadius * (maxLevel - 70) / maxLevel;
            mLevelHeight += topRemain + circleWidth;
        }
        Log.d("BatteryView", "mLevelHeight:" + mLevelHeight);
        levelText = batteryLevel + "%";
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        Log.d("TAG", "onSizeChanged");
        mWidth = getWidth();
        mHeight = getHeight();
        //背景path
        mBgPath.addCircle(mWidth / 2, mHeight / 2, mRadius, Path.Direction.CCW);
        //被裁剪的path画的圆的半径是外圆半径-外圆宽度的/2
        mClipPath.addCircle(mWidth / 2, mHeight / 2, mRadius - circleWidth / 2, Path.Direction.CCW);
        fullCount = mWidth / mWaveWidth;
        initAim();
        initTextAnim();
        textAnim.start();
    }

    private void initTextAnim() {
        textAnim = ValueAnimator.ofFloat(0, 1);
        textAnim.setDuration(2000);
        textAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                textAlpha = (float) animation.getAnimatedValue();
                invalidate();
            }
        });
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawPath(mBgPath, mBgPaint);
        drawWave(canvas);
        if (isLowPower) {
            mLevelPaint.setColor(lowPowerColor);
            mStatusPaint.setColor(lowPowerColor);
            if (isShowStatusAndLevel) {
                drawStatusAndLevel(canvas);
            }
        } else {
            mLevelPaint.setColor(levelColor);
            mStatusPaint.setColor(statusColor);
            drawStatusAndLevel(canvas);
        }

    }

    /**
     * 绘制波浪
     *
     * @param canvas
     */
    private void drawWave(Canvas canvas) {
        mBatteryPaint.setAlpha((int) (textAlpha * 255));
        canvas.save();
        //先将画布按照内圆进行裁剪
        canvas.clipPath(mClipPath);

        Log.d("TAG", "firstWaveOffset:" + firstWaveOffset);

        //第一个波浪
        mBatteryPath.reset();
        mBatteryPath.moveTo(firstWaveOffset, mLevelHeight);
        for (int i = 0; i < fullCount; i++) {
            float startX = firstWaveOffset + mWaveWidth / 2 + i * mWaveWidth;
            float endx = firstWaveOffset + mWaveWidth + i * mWaveWidth;
            float height = i % 2 == 0 ? mLevelHeight + radian : mLevelHeight - radian;
            mBatteryPath.quadTo(startX, height, endx, mLevelHeight);
        }
        if (fullCount % 2 == 0) {
            mBatteryPath.quadTo(firstWaveOffset + mWaveWidth / 2 + fullCount * mWaveWidth, mLevelHeight + radian, firstWaveOffset + mWaveWidth + fullCount * mWaveWidth, mLevelHeight);
        } else {
            mBatteryPath.quadTo(firstWaveOffset + mWaveWidth / 2 + fullCount * mWaveWidth, mLevelHeight - radian, firstWaveOffset + mWaveWidth + fullCount * mWaveWidth, mLevelHeight);
        }
        mBatteryPath.lineTo(firstWaveOffset + mWaveWidth + fullCount * mWaveWidth, mHeight);
        mBatteryPath.lineTo(firstWaveOffset, mHeight);
        mBatteryPath.lineTo(firstWaveOffset, mLevelHeight);
        canvas.drawPath(mBatteryPath, mBatteryPaint);

        //第二个波浪
        mBatteryPath.reset();
        mBatteryPath.moveTo(secondWaveOffset, mLevelHeight);
        for (int i = fullCount + 1; i < 2 * fullCount + 1; i++) {
            float startX = secondWaveOffset + mWaveWidth / 2 + (i - fullCount - 1) * mWaveWidth;
            float endx = secondWaveOffset + mWaveWidth + (i - fullCount - 1) * mWaveWidth;
            float height = i % 2 == 0 ? mLevelHeight + radian : mLevelHeight - radian;
            mBatteryPath.quadTo(startX, height, endx, mLevelHeight);
        }
        if ((2 * fullCount + 1) % 2 == 0) {
            mBatteryPath.quadTo(secondWaveOffset + mWaveWidth / 2 + fullCount * mWaveWidth, mLevelHeight + radian, secondWaveOffset + mWaveWidth + fullCount * mWaveWidth, mLevelHeight);
        } else {
            mBatteryPath.quadTo(secondWaveOffset + mWaveWidth / 2 + fullCount * mWaveWidth, mLevelHeight - radian, secondWaveOffset + mWaveWidth + fullCount * mWaveWidth, mLevelHeight);
        }
        mBatteryPath.lineTo(secondWaveOffset + mWaveWidth + fullCount * mWaveWidth, mHeight);
        mBatteryPath.lineTo(secondWaveOffset, mHeight);
        mBatteryPath.lineTo(secondWaveOffset, mLevelHeight);
        canvas.drawPath(mBatteryPath, mBatteryPaint);
        canvas.restore();
    }

    private boolean firstWaveHasStart;
    private boolean secondWaveHasStart;

    /**
     * 绘制充电状态和当前显示的电量百分数
     *
     * @param canvas
     */
    private void drawStatusAndLevel(Canvas canvas) {
        if (!TextUtils.isEmpty(levelText) && !TextUtils.isEmpty(statusText)) {
            mLevelPaint.setAlpha((int) (textAlpha * 255));
            canvas.drawText(levelText, mWidth / 2 - mLevelPaint.measureText(levelText) / 2, mLevelHeight + (mLevelPaint.descent() + mLevelPaint.ascent()), mLevelPaint);
            mStatusPaint.setAlpha((int) (textAlpha * 255));
            canvas.drawText(statusText, mWidth / 2 - mStatusPaint.measureText(statusText) / 2, mLevelHeight - (mStatusPaint.descent() + mStatusPaint.ascent()) / 2, mStatusPaint);
        }
    }

    private void initAim() {
        //left2right
        if (charginDirection == 1) {
            firstWaveBehind = ValueAnimator.ofFloat(0, mWaveWidth + fullCount * mWaveWidth);
        } else {
            firstWaveBehind = ValueAnimator.ofFloat(0, -(mWaveWidth + fullCount * mWaveWidth));
        }

        firstWaveBehind.setDuration(waveCycleTime);
        firstWaveBehind.setInterpolator(new LinearInterpolator());
        firstWaveBehind.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                firstWaveOffset = (float) animation.getAnimatedValue();
                invalidate();
            }
        });
        firstWaveBehind.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (mStatus == STATUS_CHARGING && !isFull)
                    firstWaveFront.start();
            }

            @Override
            public void onAnimationStart(Animator animation) {
                firstWaveHasStart = true;
            }
        });
        if (charginDirection == 1) {
            firstWaveFront = ValueAnimator.ofFloat(-(mWaveWidth + fullCount * mWaveWidth), 0);
        } else {
            firstWaveFront = ValueAnimator.ofFloat(mWaveWidth + fullCount * mWaveWidth, 0);
        }

        firstWaveFront.setDuration(waveCycleTime);
        firstWaveFront.setInterpolator(new LinearInterpolator());
        firstWaveFront.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                firstWaveOffset = (float) animation.getAnimatedValue();
                invalidate();
            }
        });
        firstWaveFront.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (mStatus == STATUS_CHARGING && !isFull)
                    firstWaveBehind.start();
            }
        });

        if (charginDirection == 1) {
            secondWaveFront = ValueAnimator.ofFloat(-(mWaveWidth + fullCount * mWaveWidth), 0);
        } else {
            secondWaveFront = ValueAnimator.ofFloat(mWaveWidth + fullCount * mWaveWidth, 0);
        }

        secondWaveFront.setDuration(waveCycleTime);
        secondWaveFront.setInterpolator(new LinearInterpolator());
        secondWaveFront.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                secondWaveOffset = (float) animation.getAnimatedValue();
                invalidate();
            }
        });
        secondWaveFront.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (mStatus == STATUS_CHARGING && !isFull)
                    secondWaveBehind.start();
            }

            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                secondWaveHasStart = true;
            }
        });

        if (charginDirection == 1) {
            secondWaveBehind = ValueAnimator.ofFloat(0, mWaveWidth + fullCount * mWaveWidth);
        } else {
            secondWaveBehind = ValueAnimator.ofFloat(0, -(mWaveWidth + fullCount * mWaveWidth));
        }
        secondWaveBehind.setDuration(waveCycleTime);
        secondWaveBehind.setInterpolator(new LinearInterpolator());
        secondWaveBehind.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                secondWaveOffset = (float) animation.getAnimatedValue();
                invalidate();
            }
        });
        secondWaveBehind.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (mStatus == STATUS_CHARGING && !isFull)
                    secondWaveFront.start();
            }
        });

        lowPowerAnimator = ValueAnimator.ofInt(0, 2);
        lowPowerAnimator.setDuration(500);
        lowPowerAnimator.setInterpolator(new LinearInterpolator());
        lowPowerAnimator.setRepeatCount(ValueAnimator.INFINITE);
        lowPowerAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                isShowStatusAndLevel = ((int) animation.getAnimatedValue()) == 1 ? true : false;
                invalidate();
            }
        });
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        if (widthMode != MeasureSpec.EXACTLY) {
            widthSize = 2 * mRadius + getPaddingLeft() + 2 * circleWidth + getPaddingRight();
        } else {
            //这里测量的值取大的值
            widthSize = Math.max(widthSize, 2 * mRadius + getPaddingLeft() + 2 * circleWidth + getPaddingRight());
        }
        if (heightMode != MeasureSpec.EXACTLY) {
            heightSize = 2 * mRadius + getPaddingTop() + 2 * circleWidth + getPaddingBottom();
        } else {
            //这里测量的值取大的值
            heightSize = Math.max(heightSize, 2 * mRadius + getPaddingTop() + 2 * circleWidth + getPaddingBottom());
        }
        setMeasuredDimension(widthSize, heightSize);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        Log.d(TAG, "onDetachedFromWindow");
        if (firstWaveFront != null && firstWaveFront.isRunning()) {
            firstWaveFront.end();
        }
        if (firstWaveBehind != null && firstWaveBehind.isRunning()) {
            firstWaveBehind.end();
        }
        if (secondWaveFront != null && secondWaveFront.isRunning()) {
            secondWaveFront.end();
        }
        if (secondWaveBehind != null && secondWaveBehind.isRunning()) {
            secondWaveBehind.end();
        }
        if (lowPowerAnimator != null && lowPowerAnimator.isRunning()) {
            lowPowerAnimator.end();
        }
        if (textAnim != null && textAnim.isRunning()) {
            textAnim.end();
        }
    }
}
