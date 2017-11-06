package view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

/**
 * 加载动画的三个小球
 */
public class FloatPointView extends View {
    private Paint mPaint1;
    private Paint mPaint2;
    private Paint mPaint3;
    private int mColor = Color.BLUE;

    /**
     * 小球跳起的高度
     */
    private int mOffsetY = 30;
    /**
     * 每个小球现在应该增加的高度
     */
    private float mCurrentY1 = 0;
    private float mCurrentY2 = 0;
    private float mCurrentY3 = 0;

    private ValueAnimator mUpAnim;
    private ValueAnimator mDownAnim;

    public FloatPointView(Context context) {
        this(context, null);
    }

    public FloatPointView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FloatPointView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mPaint1 = new Paint();
        mPaint1.setColor(mColor);
        mPaint1.setAntiAlias(true);
        mPaint2 = new Paint();
        mPaint2.setColor(mColor);
        mPaint2.setAntiAlias(true);
        mPaint3 = new Paint();
        mPaint3.setColor(mColor);
        mPaint3.setAntiAlias(true);
        anim1();
    }

    private void anim1() {
        //mCurrentY1从0到mOffsetY增长
        mUpAnim = ValueAnimator.ofFloat(0, mOffsetY);
        mUpAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                mCurrentY1 = (float) valueAnimator.getAnimatedValue();
                //如果高度大于了预期的一般，则改变画笔的颜色
                if (mCurrentY1 > mOffsetY / 2) {
                    mPaint1.setColor(Color.RED);
                }
                //不断刷新view
                postInvalidate();
            }
        });
        //这边是反过来，小球下降
        mDownAnim = ValueAnimator.ofFloat(mOffsetY, 0);
        mDownAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                mCurrentY1 = (float) valueAnimator.getAnimatedValue();
                if (mCurrentY1 < mOffsetY / 2) {
                    mPaint1.setColor(Color.BLUE);
                }
                postInvalidate();
            }
        });

        AnimatorSet set = new AnimatorSet();
        set.setDuration(2000);
        set.play(mUpAnim).before(mDownAnim);
        set.start();
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                //如果这个小球上升下降的动画完成，则执行第二个小球的动画
                anim2();
            }
        });
    }

    private void anim2() {
        mUpAnim = ValueAnimator.ofFloat(0, mOffsetY);
        mUpAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                mCurrentY2 = (float) valueAnimator.getAnimatedValue();
                if (mCurrentY2 > mOffsetY / 2) {

                    mPaint2.setColor(Color.RED);
                }
                postInvalidate();
            }
        });
        mDownAnim = ValueAnimator.ofFloat(mOffsetY, 0);
        mDownAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                mCurrentY2 = (float) valueAnimator.getAnimatedValue();
                if (mCurrentY2 < mOffsetY / 2) {
                    mPaint2.setColor(Color.BLUE);
                }
                postInvalidate();
            }
        });

        AnimatorSet set = new AnimatorSet();
        set.setDuration(2000);
        set.play(mUpAnim).before(mDownAnim);
        set.start();
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                anim3();
            }
        });
    }

    private void anim3() {
        mUpAnim = ValueAnimator.ofFloat(0, mOffsetY);
        mUpAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                mCurrentY3 = (float) valueAnimator.getAnimatedValue();
                if (mCurrentY3 > mOffsetY / 2) {
                    mPaint3.setColor(Color.RED);
                }
                postInvalidate();
            }
        });
        mDownAnim = ValueAnimator.ofFloat(mOffsetY, 0);
        mDownAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                mCurrentY3 = (float) valueAnimator.getAnimatedValue();
                if (mCurrentY3 < mOffsetY / 2) {
                    mPaint3.setColor(Color.BLUE);
                }
                postInvalidate();
            }
        });

        AnimatorSet set = new AnimatorSet();
        set.setDuration(2000);
        set.play(mUpAnim).before(mDownAnim);
        set.start();
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                anim1();
            }
        });
    }

    private int mPointRadius = 10;//每个小球的半径
    private int mOffsetX = 30;//每个小球的隔开的间距

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int height = getHeight();
        int width = getWidth();

        canvas.drawCircle(width / 2 - mOffsetX, height / 2 - mCurrentY1, mPointRadius, mPaint1);
        canvas.drawCircle(width / 2, height / 2 - mCurrentY2, mPointRadius, mPaint2);
        canvas.drawCircle(width / 2 + mOffsetX, height / 2 - mCurrentY3, mPointRadius, mPaint3);
    }
}
