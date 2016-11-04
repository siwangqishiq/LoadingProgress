package com.xinlan.loadingprogress;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;

/**
 * Created by panyi on 2016/11/2.
 */
public class CustomLoadingView extends View {
    private Context mContext;

    private static final int STATUS_NORMAL = 1;
    private static final int STATUS_RECOVER = 2;
    private static final int STATUS_LOADING = 3;

    private int mStatus = STATUS_NORMAL;

    private float mDistanceRatio = 0;

    private float mAngle;

    private PointF[] keyPoints = new PointF[6];

    private static final float SQUARE_3 = (float) Math.sqrt(3);

    private float left_shape_x;
    private float left_shape_y;

    private float right_shape_x;
    private float right_shape_y;

    public static final float CUBE_SCALE_DEFAULT = 0.8f;
    private float cubeLen = 0;
    private float cubeScale = CUBE_SCALE_DEFAULT;
    private long time;
    private Path mPath = new Path();

    private Paint mLeftPaint;
    private Paint mRightPaint;
    private int loading_shape_status;
    private final long T = 120;
    private final long wT = 38;


    public CustomLoadingView(Context context) {
        super(context);
        initView(context, null, 0, 0);
    }

    public CustomLoadingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context, attrs, 0, R.style.LoadingView);
    }

    public CustomLoadingView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context, attrs, defStyleAttr, R.style.LoadingView);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public CustomLoadingView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initView(context, attrs, defStyleAttr, R.style.LoadingView);
    }

    private void initView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        this.mContext = context;

        mLeftPaint = createPaint(Color.RED);
        mRightPaint = createPaint(Color.BLUE);

        for (int i = 0; i < keyPoints.length; i++) {
            keyPoints[i] = new PointF(0, 0);
        }//end for i
    }

    private static Paint createPaint(int color) {
        Paint p = new Paint();
        p.setAntiAlias(true);
        p.setStyle(Paint.Style.FILL);
        p.setStrokeJoin(Paint.Join.ROUND);
        p.setStrokeCap(Paint.Cap.ROUND);
        p.setColor(color);
        CornerPathEffect corEffect = new CornerPathEffect(7.0f);
        p.setPathEffect(corEffect);
        return p;
    }

    private void calShapeData() {
        int screen_width = getMeasuredWidth();
        int screen_height = getMeasuredHeight();

        float r = Math.min(screen_width, screen_height) / 2;
        cubeLen = r * cubeScale;

        float center_x = screen_width / 2;
        float center_y = screen_height / 2;

        keyPoints[0].set(center_x - SQUARE_3 * r / 3, center_y);
        keyPoints[1].set(center_x - SQUARE_3 * r / 6, center_y - r / 2);
        keyPoints[2].set(center_x + SQUARE_3 * r / 6, center_y - r / 2);
        keyPoints[3].set(center_x + SQUARE_3 * r / 3, center_y);
        keyPoints[4].set(center_x + SQUARE_3 * r / 6, center_y + r / 2);
        keyPoints[5].set(center_x - SQUARE_3 * r / 6, center_y + r / 2);

        resetShapPosition();
    }

    private void resetShapPosition() {
        mDistanceRatio = 0;
        time = 0;
        mAngle = 0;

        loading_shape_status = 0;

        left_shape_x = keyPoints[0].x;
        left_shape_y = keyPoints[0].y;

        right_shape_x = keyPoints[3].x;
        right_shape_y = keyPoints[3].y;
    }

    private void drawLeftTrigle(Canvas canvas) {
        mPath.reset();
        mPath.moveTo(left_shape_x + SQUARE_3 * cubeLen / 3, left_shape_y);
        mPath.lineTo(left_shape_x - SQUARE_3 * cubeLen / 6, left_shape_y + cubeLen / 2);
        mPath.lineTo(left_shape_x - SQUARE_3 * cubeLen / 6, left_shape_y - cubeLen / 2);
        mPath.close();
        canvas.drawPath(mPath, mLeftPaint);
    }

    private void drawRightTrigle(Canvas canvas) {
        mPath.reset();
        mPath.moveTo(right_shape_x - SQUARE_3 * cubeLen / 3, right_shape_y);
        mPath.lineTo(right_shape_x + SQUARE_3 * cubeLen / 6, right_shape_y + cubeLen / 2);
        mPath.lineTo(right_shape_x + SQUARE_3 * cubeLen / 6, right_shape_y - cubeLen / 2);
        mPath.close();
        canvas.drawPath(mPath, mRightPaint);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        calShapeData();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        switch (mStatus) {
            case STATUS_NORMAL:
            case STATUS_RECOVER:
                drawShapes(canvas);
                break;
            case STATUS_LOADING:
                drawLoading(canvas);
                break;
        }//end switch
    }

    private void drawShapes(Canvas canvas) {
        drawLeftTrigle(canvas);
        drawRightTrigle(canvas);
    }

    private void drawLoading(Canvas canvas) {
        canvas.save();
        canvas.rotate(mAngle, getWidth() / 2, getHeight() / 2);
        drawLeftTrigle(canvas);
        drawRightTrigle(canvas);
        canvas.restore();
        updateLoadingShape();

        postInvalidate();
    }

    /**
     * 更新载入图形位置坐标
     */
    private void updateLoadingShape() {
        float p1_x = 0, p1_y = 0, p2_x = 0, p2_y = 0;
        float b_p1_x = 0, b_p1_y = 0, b_p2_x = 0, b_p2_y = 0;

        switch (loading_shape_status) {
            case 0:
                p1_x = keyPoints[0].x;
                p1_y = keyPoints[0].y;

                p2_x = keyPoints[4].x;
                p2_y = keyPoints[4].y;

                b_p1_x = keyPoints[3].x;
                b_p1_y = keyPoints[3].y;

                b_p2_x = keyPoints[1].x;
                b_p2_y = keyPoints[1].y;
                break;
            case 1:
                p1_x = keyPoints[4].x;
                p1_y = keyPoints[4].y;

                p2_x = keyPoints[2].x;
                p2_y = keyPoints[2].y;

                b_p1_x = keyPoints[1].x;
                b_p1_y = keyPoints[1].y;

                b_p2_x = keyPoints[5].x;
                b_p2_y = keyPoints[5].y;
                break;
            case 2:
                p1_x = keyPoints[2].x;
                p1_y = keyPoints[2].y;

                p2_x = keyPoints[0].x;
                p2_y = keyPoints[0].y;

                b_p1_x = keyPoints[5].x;
                b_p1_y = keyPoints[5].y;

                b_p2_x = keyPoints[3].x;
                b_p2_y = keyPoints[3].y;
                break;
            default:
                break;
        }//end switch


        if (time <= wT) {
            left_shape_x = p1_x;
            left_shape_y = p1_y;

            right_shape_x = b_p1_x;
            right_shape_y = b_p1_y;
        } else if (time > wT && time <= T - wT) {
            long t = T - wT - wT;
            long ti = time - wT;

            left_shape_x = ((-2) * (p2_x - p1_x) * ti * ti * ti) / (t * t * t) + (3 * (p2_x - p1_x) * ti * ti) / (t * t) + p1_x;
            left_shape_y = ((-2) * (p2_y - p1_y) * ti * ti * ti) / (t * t * t) + (3 * (p2_y - p1_y) * ti * ti) / (t * t) + p1_y;

            right_shape_x = ((-2) * (b_p2_x - b_p1_x) * ti * ti * ti) / (t * t * t) + (3 * (b_p2_x - b_p1_x) * ti * ti) / (t * t) + b_p1_x;
            right_shape_y = ((-2) * (b_p2_y - b_p1_y) * ti * ti * ti) / (t * t * t) + (3 * (b_p2_y - b_p1_y) * ti * ti) / (t * t) + b_p1_y;
        } else {
            left_shape_x = p2_x;
            left_shape_y = p2_y;

            right_shape_x = b_p2_x;
            right_shape_y = b_p2_y;
        }

        //rotate
        //float v_min = 0.2f;
        //float angle = (v_min - 2) * time * time * time / (T * T) + (3 - 2 * v_min) * time * time / T + v_min * time;
        //mAngle = T * shape_status + (int) angle;
        if (time <= T / 2) {
            mAngle -= 1.5f + time / T;
        } else {
            mAngle -= 1.5f + ((1 / 2) * time - time / T);
        }

        //更新时间
        time += 2f;

        if (time > T) {
            time = 0;
            loading_shape_status = (loading_shape_status + 1) % 3;
        }
    }

    private void updateShapeOffset() {
        int radius = Math.min(getWidth(), getHeight()) / 2;
        int len = (getWidth() - radius) / 2;

        float offset = len * mDistanceRatio;
        left_shape_x = keyPoints[0].x - offset;
        right_shape_x = keyPoints[3].x + offset;

        left_shape_y = keyPoints[0].y;
        right_shape_y = keyPoints[3].y;

        postInvalidate();
    }

    //------------------public -------------------------

    /**
     * @param r
     */
    public void setDistanceRatio(float r) {
        //System.out.println("r = "+r);
        if (r < 0)
            r = 0;

        mStatus = STATUS_RECOVER;
        this.mDistanceRatio = (float) ((2 / Math.PI) * Math.atan(r));
        updateShapeOffset();
        //System.out.println("mDistanceRatio = "+mDistanceRatio);
    }

    public void recover(final boolean startLoading) {
        this.mStatus = STATUS_RECOVER;
        ValueAnimator animator = ValueAnimator.ofFloat(mDistanceRatio, 0);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.setDuration(200);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float curDistanceRatio = (float) valueAnimator.getAnimatedValue();
                mDistanceRatio = curDistanceRatio;
                //System.out.println("DistanceRatio = "+mDistanceRatio);
                updateShapeOffset();
            }
        });
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (startLoading) {
                    startLoading();
                }
            }
        });
        animator.start();
    }

    /**
     * 开始载入动画
     */
    public void startLoading() {
        this.mStatus = STATUS_LOADING;
        resetShapPosition();
        postInvalidate();
    }

    public void reset() {
        mStatus = STATUS_NORMAL;
        resetShapPosition();
    }

    /**
     * 隐藏
     *
     * @param isAnimation
     */
    public void hide(final boolean isAnimation) {
        if (getVisibility() != View.VISIBLE)
            return;

        if (isAnimation) {
            final AlphaAnimation animation = new AlphaAnimation(1, 0);
            animation.setDuration(250);
            startAnimation(animation);
            animation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    setVisibility(View.GONE);
                    reset();
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });
        } else {
            setVisibility(View.GONE);
            reset();
        }
    }
}//end class
