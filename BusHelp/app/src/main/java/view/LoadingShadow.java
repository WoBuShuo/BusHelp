package view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

import com.hkbushelp.apps.R;

/**
 * Created by Hello on 2017/9/4.
 */

public class LoadingShadow extends View {

    private ValueAnimator mDownAnim;
    private ValueAnimator mUpAnim;

    public LoadingShadow(Context context) {
        this(context, null);
    }

    public LoadingShadow(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LoadingShadow(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private Bitmap mBitmap;
    private Paint mPaint;
    private RectF mOvalF;
    private int upDownDistance = 35;
    private float currentDistance;

    private int ovalWidth;
    private int ovalHeight;
    private float currentOvalWidth;
    private float currentOvalHeight;

    private void init() {
        mBitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.station_point);
        ovalWidth = mBitmap.getWidth() / 2;
        ovalHeight = mBitmap.getHeight() / 4;
        mPaint = new Paint();
        mPaint.setColor(Color.parseColor("#dedeeb"));
        mPaint.setAntiAlias(true);
        mPaint.setStrokeWidth(3);
        mPaint.setStyle(Paint.Style.FILL);
        mOvalF = new RectF();

        initAnim();
    }

    private void initAnim() {
        mUpAnim = ValueAnimator.ofFloat(0f, 1f);
        mUpAnim.setDuration(700);
        mUpAnim.setInterpolator(new DecelerateInterpolator());
        mUpAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float fraction = (float) animation.getAnimatedValue();
                currentOvalWidth = fraction * ovalWidth;
                currentOvalHeight = fraction * ovalHeight;
                currentDistance = fraction * upDownDistance;
                invalidate();
            }
        });


        mDownAnim = ValueAnimator.ofFloat(1f, 0f);
        mDownAnim.setInterpolator(new AccelerateInterpolator());
        mDownAnim.setDuration(700);
        mDownAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float fraction = (float) animation.getAnimatedValue();
                currentOvalWidth = fraction * ovalWidth;
                currentOvalHeight = fraction * ovalHeight;
                currentDistance = fraction * upDownDistance;
                invalidate();
            }
        });
        final AnimatorSet set = new AnimatorSet();
        set.play(mUpAnim).after(mDownAnim);
        set.start();
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                set.start();
            }
        });

    }


    @Override
    protected void onDraw(Canvas canvas) {
        int bodyY = getHeight() / 3;
        int bodyX = getWidth() / 2;
        //椭圆的左上右下
        mOvalF.set(bodyX - mBitmap.getWidth() / 2 + currentOvalWidth / 2,
                bodyY + mBitmap.getHeight() + currentOvalHeight / 2,
                bodyX + mBitmap.getWidth() / 2 - currentOvalWidth / 2,
                bodyY + mBitmap.getHeight() + 18 - currentOvalHeight / 2);
        //绘制椭圆
        canvas.drawOval(mOvalF, mPaint);
        canvas.drawBitmap(mBitmap, bodyX - mBitmap.getWidth() / 2, bodyY + 9 - currentDistance, null);

    }
}
