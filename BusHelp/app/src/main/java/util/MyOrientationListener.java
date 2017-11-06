package util;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

/**
 * Created by sos on 2017/2/25.
 */
public class MyOrientationListener implements SensorEventListener {
    private Context mContext;
    private SensorManager mSensorManager;

    private Sensor mSensor;

    public MyOrientationListener(Context context) {
        this.mContext = context;
    }

    public void start() {
        mSensorManager = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);
        if (mSensorManager != null) {
            mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            if (mSensor != null) {
                mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_UI);
            }
        }
    }

    public void stop() {
        mSensorManager.unregisterListener(this);
    }

    private float xLast;

    @Override
    public void onSensorChanged(SensorEvent event) {


        //判断返回的传感器类型是不是方向传感器

        //只获取x的值
        float x = event.values[SensorManager.DATA_X];
        //为了防止经常性的更新
        if (Math.abs(x - xLast) > 1.0) {
            if (mOnOrientationListener != null) {
                mOnOrientationListener.onOrientationChange(x);
            }
        }
        xLast = x;

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }


    private OnOrientationListener mOnOrientationListener;

    public void setOnOrientationListener(OnOrientationListener onOrientationListener) {
        mOnOrientationListener = onOrientationListener;
    }

    public interface OnOrientationListener {
        void onOrientationChange(float x);
    }

}
