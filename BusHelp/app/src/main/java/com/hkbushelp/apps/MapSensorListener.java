package com.hkbushelp.apps;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

/**
 * Created by Hello on 2017/8/23.
 */

public class MapSensorListener implements SensorEventListener {
    private SensorManager mSensorManager;
    private Context mContext;
    private Sensor mSensor;
    private float lastX;

    public MapSensorListener(Context context) {
        mContext = context;
    }

    public void start()
    {
        mSensorManager= (SensorManager) mContext
                .getSystemService(Context.SENSOR_SERVICE);
        if(mSensorManager!= null)
        {
            //获得方向传感器
            mSensor=mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        }
        //判断是否有方向传感器
        if(mSensor!=null)
        {
            //注册监听器
            mSensorManager.registerListener(this,mSensor,SensorManager.SENSOR_DELAY_UI);

        }


    }
    public void stop()
    {
        mSensorManager.unregisterListener(this);

    }
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType()==Sensor.TYPE_ORIENTATION){
            float x=event.values[SensorManager.DATA_X];
            if (Math.abs(x-lastX)>1.0f){
                if (mListener!=null){
                    mListener.onOrientationChanged(x);
                }
            }
            lastX=x;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private OnOrientationListener mListener;
    public interface OnOrientationListener {
        void onOrientationChanged(float x);
    }

    public void setListener(OnOrientationListener listener) {
        mListener = listener;
    }
}
