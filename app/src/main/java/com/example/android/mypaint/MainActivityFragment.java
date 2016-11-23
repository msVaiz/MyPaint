package com.example.android.mypaint;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

public class MainActivityFragment extends Fragment {

    private PaintView mPaintView; //управляет событиями касаний и отрисовки
    private float mAcceleration;
    private float mCurrentAcceleration;
    private float mLastAcceleration;
    private boolean mDialogOnScreen = false;

    //используется, чтобы определить, встряхнул ли пользователь устройство
    private static final int ACCELERATION_THRESHOLD = 100000;

    //используется для идентификации запроса разрешения на запись
    private static final int SAVE_IMAGE_PERMISSION_REQUEST_CODE = 1;

    private final SensorEventListener sensorEventListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {
            if (!mDialogOnScreen) {

                float x = sensorEvent.values[0];
                float y = sensorEvent.values[1];
                float z = sensorEvent.values[2];

                //сохраняем предыдущее значение ускорения
                mLastAcceleration = mCurrentAcceleration;
                //вычисляем текущее ускорение
                mCurrentAcceleration = x*x + y*y + z*z;
                //вычисляем изменение ускорения
                mAcceleration = mCurrentAcceleration * (mCurrentAcceleration - mLastAcceleration);

                if (mAcceleration > ACCELERATION_THRESHOLD) {
                    confirmErase();
                }
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {}
    };

    /** подтверждает должно ли изображение быть стерто */
    private void confirmErase() {
        EraseImageDialogFragment fragment = new EraseImageDialogFragment();
        fragment.show(getFragmentManager(), "erase dialog");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);

        setHasOptionsMenu(true); //этот фрагмент имеет пункты меню для отображения
        mPaintView = (PaintView) view.findViewById(R.id.my_paint_view);

        mAcceleration = 0.00f;
        mCurrentAcceleration = SensorManager.GRAVITY_EARTH;
        mLastAcceleration = SensorManager.GRAVITY_EARTH;

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        //начинаем слушать события shake
        enableAccelerometerListening();
    }

    private void enableAccelerometerListening() {
        //получаем SensorManager
        SensorManager sensorManager = (SensorManager) getActivity().getSystemService(
                Context.SENSOR_SERVICE);

        //регистрируем слушатель для событий акселерометра
        sensorManager.registerListener(sensorEventListener,
                sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void onPause() {
        super.onPause();
        //прекращаем слушать события shake
        disableAccelerometerListening();
    }

    private void disableAccelerometerListening() {
        SensorManager sensorManager = (SensorManager) getActivity().getSystemService(
                Context.SENSOR_SERVICE);

        //прекращаем слушать события акселерометра
        sensorManager.unregisterListener(sensorEventListener,
                sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER));
    }

    /** отображает элементы меню */
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu, menu);
    }

    /** управляет выбором из опций меню */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.color:
                ColorDialogFragment colorDialog = new ColorDialogFragment();
                colorDialog.show(getFragmentManager(), "color dialog");
                return true; //уничтожить собитие-меню
            case R.id.line_width:
                LineWidthDialogFragment widthDialog = new LineWidthDialogFragment();
                widthDialog.show(getFragmentManager(), "line width dialog");
                return true;
            case R.id.delete_drawing:
                confirmErase(); //запрашиваем подтверждение на удаление
                return true;
            case R.id.save:
                saveImage(); //проверяет разрешения и сохраняет текущее изображение
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /** запрашивает разрешение или сохраняет изображение, если разрешение получено */
    private void saveImage() {
        //проверяем если приложение не имееет нужное разрешение
        if (getContext().checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED) {
            //показать разъяснение почему разрешение нужно
            if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage(R.string.permission_explanation);
                builder.setPositiveButton(android.R.string.ok,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                requestPermissions(
                                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                        SAVE_IMAGE_PERMISSION_REQUEST_CODE);

                            }
                        }
                );
                builder.create().show();
            } else {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        SAVE_IMAGE_PERMISSION_REQUEST_CODE);
            }
        } else { //если разрешение уже получено
            mPaintView.saveImage();
        }
    }

    /** вызывается системой когда юзер принимает или отказывает разрешению */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case SAVE_IMAGE_PERMISSION_REQUEST_CODE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mPaintView.saveImage();
                }
                return;
        }
    }

    public PaintView getPaintView() {
        return mPaintView;
    }

    /** указывает отображается диалоговое окно или нет */
    public void setDialogOnScreen(boolean visible) {
        mDialogOnScreen = visible;
    }
}
