package com.xinlan.loadingprogress;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.animation.ValueAnimatorCompat;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;

/**
 * Created by panyi on 2016/10/27.
 */
public class LoadingView extends View {
    public static final int STATUS_SHOW_PREVIEW = 1;
    public static final int STATUS_SHOW_LOADING = 2;

    private static final double SQRT_THREE_HALF = Math.sqrt(3) / 2;
    private static final double SQUARE_2 = Math.sqrt(2);
    private static final float SQUARE_3 = (float) Math.sqrt(3);

    public static final int TRIGLE_MARGIN_DEFAULT = 5;
    public static final int TRIAGE_PADDING = 5;

    public static final float CUBE_SCALE_DEFAULT = 0.8f;

    private Context mContext;

    private int mMinWidth;
    private int mMinHeight;

    protected int mStatus;

    protected Drawable mPreviewDrawable;

    private float cubeLen = 0;

    private int mUpTriglePosX = 0;
    private int mUpTriglePosY = 0;
    private int mDownTriglePosX = 0;
    private int mDownTriglePosY = 0;

    private Paint mUpTriglePaint;
    private Paint mDownTriglePaint;

    private int translateMin = 0;
    private int translateMax = 0;

    private float cubeScale = CUBE_SCALE_DEFAULT;

    private Path mPath = new Path();

    private AnimatorSet mLoadAnimator = new AnimatorSet();

    private long mAngle = 0;

    private float down_shape_x, down_shape_y;
    private float up_shape_x, up_shape_y;
    private int shape_status = 0;

    private long time;

    private PointF[] keyPoints = new PointF[6];

    private static final int T = 120;//运动时间周期  120

    public LoadingView(Context context) {
        super(context);
        initView(context, null, 0, 0);
    }

    public LoadingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context, attrs, 0, R.style.LoadingView);
    }

    public LoadingView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context, attrs, defStyleAttr, R.style.LoadingView);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public LoadingView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initView(context, attrs, defStyleAttr, R.style.LoadingView);
    }

    private void initView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        this.mContext = context;
        readAttributeAndSet(context, attrs, 0, 0);
        mStatus = STATUS_SHOW_PREVIEW;

        mUpTriglePaint = createPaint();
        mUpTriglePaint.setColor(Color.RED);
        mDownTriglePaint = createPaint();
        mDownTriglePaint.setColor(Color.BLUE);

        for (int i = 0; i < keyPoints.length; i++) {
            keyPoints[i] = new PointF(0, 0);
        }//end for i
    }

    private static Paint createPaint() {
        Paint p = new Paint();
        p.setAntiAlias(true);
        p.setStyle(Paint.Style.FILL);
        p.setStrokeJoin(Paint.Join.ROUND);
        p.setStrokeCap(Paint.Cap.ROUND);
        //CornerPathEffect corEffect = new CornerPathEffect(10.0f);
        //p.setPathEffect(corEffect);
        return p;
    }

    private void readAttributeAndSet(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        final TypedArray a = context.obtainStyledAttributes(
                attrs, R.styleable.LoadingView, defStyleAttr, defStyleRes);
        mPreviewDrawable = a.getDrawable(R.styleable.LoadingView_previewSrc);
        if (mPreviewDrawable != null) {
            mPreviewDrawable.setBounds(0, 0, mPreviewDrawable.getIntrinsicWidth(), mPreviewDrawable.getIntrinsicHeight());
        }
        a.recycle();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int dw = 0;
        int dh = 0;
        dw += getPaddingLeft() + getPaddingRight();
        dh += getPaddingTop() + getPaddingBottom();
        if (mPreviewDrawable != null) {
            dw += Math.max(mMinWidth, mPreviewDrawable.getIntrinsicWidth());
            dh += Math.max(mMinHeight, mPreviewDrawable.getIntrinsicHeight());
        }

        final int measuredWidth = Math.max(resolveSizeAndState(dw, widthMeasureSpec, 0), mMinWidth);
        final int measuredHeight = Math.max(resolveSizeAndState(dh, heightMeasureSpec, 0), mMinHeight);
        setMeasuredDimension(measuredWidth, measuredHeight);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        switch (mStatus) {
            case STATUS_SHOW_PREVIEW:
                drawPreview(canvas);
                break;
            case STATUS_SHOW_LOADING:
                drawLoading(canvas);
                break;
        }//end switch
    }

    private void drawPreview(Canvas canvas) {
        final int saveCount = canvas.save();
        canvas.translate(getPaddingLeft(), getPaddingTop());
        if (mPreviewDrawable != null) {
            mPreviewDrawable.draw(canvas);
        }
        canvas.restoreToCount(saveCount);
    }

    private void drawLoading(Canvas canvas) {
        canvas.save();
        mAngle += 1;
        canvas.rotate(mAngle, getWidth() / 2, getHeight() / 2);
        drawUpTrigle(canvas);
        drawDownTrigle(canvas);
        canvas.restore();

        updateTrigle();

        postInvalidate();
    }

    private void calTrigleCube() {
        int screen_width = getWidth();
        int screen_height = getHeight();

        float r = Math.min(screen_width, screen_height) / 2;
        cubeLen = r * cubeScale;

        float center_x = screen_width / 2;
        float center_y = screen_height / 2;

        keyPoints[0].set(center_x - r / 2, center_y - SQUARE_3 * r / 6);
        keyPoints[1].set(center_x, center_y - SQUARE_3 * r / 3);
        keyPoints[2].set(center_x + r / 2, center_y - SQUARE_3 * r / 6);
        keyPoints[3].set(center_x + r / 2, center_y + SQUARE_3 * r / 6);
        keyPoints[4].set(center_x, center_y + SQUARE_3 * r / 3);
        keyPoints[5].set(center_x - r / 2, center_y + SQUARE_3 * r / 6);

        time = 0;
        shape_status = 0;

        down_shape_x = keyPoints[5].x;
        down_shape_y = keyPoints[5].y;

        up_shape_x = keyPoints[2].x;
        up_shape_y = keyPoints[2].y;
    }

    private void updateTrigle() {
        //System.out.println("status = "+shape_status);
        float p1_x = 0, p1_y = 0, p2_x = 0, p2_y = 0;
        float b_p1_x = 0, b_p1_y = 0, b_p2_x = 0, b_p2_y = 0;

        switch (shape_status) {
            case 0:
                p1_x = keyPoints[5].x;
                p1_y = keyPoints[5].y;

                p2_x = keyPoints[1].x;
                p2_y = keyPoints[1].y;

                b_p1_x = keyPoints[2].x;
                b_p1_y = keyPoints[2].y;

                b_p2_x = keyPoints[4].x;
                b_p2_y = keyPoints[4].y;
                break;
            case 1:
                p1_x = keyPoints[1].x;
                p1_y = keyPoints[1].y;

                p2_x = keyPoints[3].x;
                p2_y = keyPoints[3].y;

                b_p1_x = keyPoints[4].x;
                b_p1_y = keyPoints[4].y;

                b_p2_x = keyPoints[0].x;
                b_p2_y = keyPoints[0].y;
                break;
            case 2:
                p1_x = keyPoints[3].x;
                p1_y = keyPoints[3].y;

                p2_x = keyPoints[5].x;
                p2_y = keyPoints[5].y;

                b_p1_x = keyPoints[0].x;
                b_p1_y = keyPoints[0].y;

                b_p2_x = keyPoints[2].x;
                b_p2_y = keyPoints[2].y;
                break;
            default:
                break;
        }//end switch


        down_shape_x = ((-2) * (p2_x - p1_x) * time * time * time) / (T * T * T) + (3 * (p2_x - p1_x) * time * time) / (T * T) + p1_x;
        down_shape_y = ((-2) * (p2_y - p1_y) * time * time * time) / (T * T * T) + (3 * (p2_y - p1_y) * time * time) / (T * T) + p1_y;

        up_shape_x = ((-2) * (b_p2_x - b_p1_x) * time * time * time) / (T * T * T) + (3 * (b_p2_x - b_p1_x) * time * time) / (T * T) + b_p1_x;
        up_shape_y = ((-2) * (b_p2_y - b_p1_y) * time * time * time) / (T * T * T) + (3 * (b_p2_y - b_p1_y) * time * time) / (T * T) + b_p1_y;


        time += 2;
        if (time > T) {
            time = 0;
            shape_status = (shape_status + 1) % 3;
        }
    }

//    private void startLoadingAnimation() {
//        final int maxAngle = 180;
//        ValueAnimator mRotateLeftAnimator = ValueAnimator.ofInt(0, 180);
//        mRotateLeftAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
//        mRotateLeftAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
//            @Override
//            public void onAnimationUpdate(ValueAnimator valueAnimator) {
//                mAngle = (int) valueAnimator.getAnimatedValue();
////                final int half = maxAngle / 2;
////                if (mAngle <= half) {
////                    mUpTriglePosX = translateMin + (translateMax - translateMin) * mAngle / half;
////                    mDownTriglePosX = translateMax - (translateMax - translateMin) * (mAngle) / half;
////                } else {
////                    mUpTriglePosX = translateMax - (translateMax - translateMin) * (mAngle-half) / half;
////                    mDownTriglePosX = translateMin + (translateMax - translateMin) *( mAngle-half) / half;
////                }
//                //mUpTriglePosX = translateMin;
//                //mDownTriglePosX = translateMax;
//                //mUpTriglePosX = ((translateMax - translateMin) * (180 + mAngle)) / maxAngle;
//                //mUpTriglePosX = translateMin +(int)mAngle;
//                //mDownTriglePosX = translateMax - (int)mAngle;
//
//                mUpTriglePosX = translateMin + (translateMax - translateMin) * mAngle / maxAngle;
//                mDownTriglePosX = translateMax - (translateMax - translateMin) * (mAngle) / maxAngle;
//                postInvalidate();
//            }
//        });
//
//        ValueAnimator mRotateRightAnimator = ValueAnimator.ofInt(180, 360);
//        mRotateRightAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
//        mRotateRightAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
//            @Override
//            public void onAnimationUpdate(ValueAnimator valueAnimator) {
//                //mAngle = (int) valueAnimator.getAnimatedValue();
//                mUpTriglePosY = translateMax - (translateMax - translateMin) * (mAngle - maxAngle) / maxAngle;
//                mDownTriglePosY = translateMin + (translateMax - translateMin) * (mAngle - maxAngle) / maxAngle;
//
////                mUpTriglePosX = translateMin + (translateMax - translateMin) * mAngle / maxAngle;
////                mDownTriglePosX = translateMax - (translateMax - translateMin) * (mAngle) / maxAngle;
//
//                postInvalidate();
//            }
//        });
//
//
//        mLoadAnimator.playSequentially(mRotateLeftAnimator, mRotateRightAnimator);
//        mLoadAnimator.setDuration(1000);
//
//        mLoadAnimator.addListener(new Animator.AnimatorListener() {
//            @Override
//            public void onAnimationStart(Animator animator) {
//
//            }
//
//            @Override
//            public void onAnimationEnd(Animator animator) {
//                mLoadAnimator.start();
//            }
//
//            @Override
//            public void onAnimationCancel(Animator animator) {
//
//            }
//
//            @Override
//            public void onAnimationRepeat(Animator animator) {
//
//            }
//        });
//        //set.start();
//        mLoadAnimator.start();
//    }

    private void drawUpTrigle(Canvas canvas) {
        mPath.reset();
        mPath.moveTo(up_shape_x, up_shape_y - SQUARE_3 * cubeLen / 3);
        mPath.lineTo(up_shape_x - cubeLen / 2, up_shape_y + SQUARE_3 * cubeLen / 6);
        mPath.lineTo(up_shape_x + cubeLen / 2, up_shape_y + SQUARE_3 * cubeLen / 6);
        mPath.close();
        canvas.drawPath(mPath, mUpTriglePaint);
    }

    private void drawDownTrigle(Canvas canvas) {
        mPath.reset();
        mPath.moveTo(down_shape_x - cubeLen / 2, down_shape_y - SQUARE_3 * cubeLen / 6);
        mPath.lineTo(down_shape_x + cubeLen / 2, down_shape_y - SQUARE_3 * cubeLen / 6);
        mPath.lineTo(down_shape_x, down_shape_y + SQUARE_3 * cubeLen / 3);
        mPath.close();
        canvas.drawPath(mPath, mDownTriglePaint);
    }

    //--------public --------
    public void startLoading() {
        mStatus = STATUS_SHOW_LOADING;
        calTrigleCube();
        postInvalidate();
    }

    public void restart() {
        mStatus = STATUS_SHOW_PREVIEW;
        calTrigleCube();
        mLoadAnimator.cancel();
        postInvalidate();
    }
}//end class
