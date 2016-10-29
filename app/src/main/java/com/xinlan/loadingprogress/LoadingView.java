package com.xinlan.loadingprogress;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by panyi on 2016/10/27.
 */
public class LoadingView extends View {
    public static final int STATUS_SHOW_PREVIEW = 1;
    public static final int STATUS_SHOW_LOADING = 2;

    private static final double SQRT_THREE_HALF = Math.sqrt(3) / 2;

    public static final int TRIGLE_MARGIN = 20;
    public static final int TRIAGE_PADDING = 5;

    private Context mContext;

    private int mMinWidth;
    private int mMinHeight;

    protected int mStatus;

    protected Drawable mPreviewDrawable;

    private int mTrigleCube = 0;

    private int mUpTriglePosX = 0;
    private int mUpTriglePosY = 0;
    private int mDownTriglePosX = 0;
    private int mDownTriglePosY = 0;

    private Paint mUpTriglePaint;
    private Paint mDownTriglePaint;

    private int translateMin = 0;
    private int translateMax = 0;

    private Path mPath = new Path();

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

    float angle = 0;

    private void drawLoading(Canvas canvas) {
        canvas.save();
        canvas.rotate(angle, getWidth() / 2, getHeight() / 2);
        drawUpTrigle(canvas);
        drawDownTrigle(canvas);
        angle += 0.5f;
        canvas.restore();

        postInvalidate();
    }

    private void calTrigleCube() {
        int screen_width = getWidth();
        int screen_height = getHeight();

        if (screen_width <= screen_height) {
            mTrigleCube = (screen_width - TRIGLE_MARGIN) / 2;

            translateMin = 0;
            translateMax =  screen_width - mTrigleCube;

            mUpTriglePosX = screen_width/2 - mTrigleCube / 2;
            mDownTriglePosX = screen_width/2 - mTrigleCube / 2;
        } else {

            mTrigleCube = (screen_height - TRIGLE_MARGIN) / 2;
            translateMin = 0;
            translateMax =  screen_height - mTrigleCube;

        }//end if

        mUpTriglePosY = screen_height / 2 - TRIGLE_MARGIN / 2;
        mDownTriglePosY = screen_height / 2 + TRIGLE_MARGIN / 2;

//        int width = Math.min(screen_width, screen_height) - TRIGLE_MARGIN;
//        mTrigleCube = (int) ((double) (width) / 2) - (TRIAGE_PADDING << 1);
//        //System.out.println("mTrigleCube = " + mTrigleCube);
//
//
//        int marginHalf = TRIGLE_MARGIN >> 1;
//        if (screen_width > screen_height) {
//            mUpTriglePosX = screen_width / 2 - width / 2 - marginHalf;
//            mDownTriglePosX = screen_width - mTrigleCube - marginHalf - (screen_width / 2 - width / 2);
//            mUpTriglePosY = (screen_height >> 1) - marginHalf;
//            mDownTriglePosY = (screen_height >> 1) + marginHalf;
//        } else {
//            mUpTriglePosX = marginHalf;
//            mDownTriglePosX =
//        }
//
//
//
//
//
//        mA = (width - (TRIAGE_PADDING << 1) - mTrigleCube) / 2;
    }

    private void drawUpTrigle(Canvas canvas) {
        mPath.reset();
        mPath.moveTo(mUpTriglePosX, mUpTriglePosY);
        mPath.lineTo(mUpTriglePosX + mTrigleCube, mUpTriglePosY);
        mPath.lineTo(mUpTriglePosX + (mTrigleCube >> 1), mUpTriglePosY - (int) (SQRT_THREE_HALF * mTrigleCube));
        mPath.close();
        canvas.drawPath(mPath, mUpTriglePaint);
    }

    private void drawDownTrigle(Canvas canvas) {
        mPath.reset();
        mPath.moveTo(mDownTriglePosX, mDownTriglePosY);
        mPath.lineTo(mDownTriglePosX + mTrigleCube, mDownTriglePosY);
        mPath.lineTo(mDownTriglePosX + (mTrigleCube >> 1), mDownTriglePosY + (int) (SQRT_THREE_HALF * mTrigleCube));
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
        postInvalidate();
    }
}//end class
