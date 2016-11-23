package com.example.android.mypaint;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.provider.MediaStore;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;

/** обрабатывает тачи пользователя и отрисовывает соответстующие линии */
public class PaintView extends View {

    //используется чтобы определить: пользователь переместил палец достаточно чтобы отрисовать
    private static final float TOUCH_TOLERANCE = 10;

    private Bitmap mBitmap; //область рисования для отображаения и сохранения
    private Canvas mBitmapCanvas; //используется для рисования на Bitmap
    private final Paint mPaintScreen; //используется для отрисовки Bitmap на экран
    private final Paint mPaintLine; //используется для рисования линий на Bitmap

    private final Map<Integer, Path> mPathMap = new HashMap<>();
    private final Map<Integer, Point> mPreviousPointMap = new HashMap<>();

    public PaintView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mPaintScreen = new Paint();
        //устанавливаем настройки для рисующих линий
        mPaintLine = new Paint();
        mPaintLine.setAntiAlias(true); //сглаживание краев линии
        mPaintLine.setColor(Color.BLACK); // цвет линий по умолчанию
        mPaintLine.setStyle(Paint.Style.STROKE); //сплошная линия
        mPaintLine.setStrokeWidth(5); //ширина линии по умолчанию
        mPaintLine.setStrokeCap(Paint.Cap.ROUND); //скгругление концов линий
    }

    /** Создает Bitmap и Canvas на основе размеров View */
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        mBitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        mBitmapCanvas = new Canvas(mBitmap);
        mBitmap.eraseColor(Color.WHITE);
    }

    /** выполняет кастомную отрисовку когда View обновляется */
    @Override
    protected void onDraw(Canvas canvas) {
        //отрисовываем background
        canvas.drawBitmap(mBitmap, 0, 0, mPaintScreen);

        for (Integer key : mPathMap.keySet()) {
            canvas.drawPath(mPathMap.get(key), mPaintLine); //рисуем линию
        }
    }

    /** управляет событиями тачей */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getActionMasked(); //тип события
        int actionIndex = event.getActionIndex(); //указатель(т.е. палец)

        //определяет тач начался, закончился или двигается
        if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_POINTER_DOWN) {
            touchStarted(event.getX(actionIndex), event.getY(actionIndex),
                    event.getPointerId(actionIndex));

        } else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_POINTER_UP) {
            touchEnded(event.getPointerId(actionIndex));
        } else {
            touchMoved(event);
        }

        invalidate();
        return true;
    }

    /** вызывается когда пользователь касается экрана */
    private void touchStarted(float x, float y, int lineId) {
        Path path;
        Point point;

        //если path уже существует для lineId
        if (mPathMap.containsKey(lineId)) {
            path = mPathMap.get(lineId); //получаем path
            path.reset(); //сбрасываем path потому что началось новое касание
            point = mPreviousPointMap.get(lineId); //получаем предыдущую точку path
        } else {
            path = new Path();
            mPathMap.put(lineId, path);
            point = new Point();
            mPreviousPointMap.put(lineId, point);
        }

        //переходим к координатам касания
        path.moveTo(x, y);
        point.x = (int) x;
        point.y = (int) y;
    }

    /** вызывается когда юзер водит пальцем по экрану */
    private void touchMoved(MotionEvent event) {
        //для каждого из пальцев в данном MotionEvent
        for (int i = 0; i < event.getPointerCount(); i++) {
            //получаем ID и индекс пальца
            int pointerId = event.getPointerId(i);
            int pointerIndex = event.findPointerIndex(pointerId);

            //если существует path связанный с пальцем
            if (mPathMap.containsKey(pointerId)) {
                //получаем новые координаты для пальца
                float newX = event.getX(pointerIndex);
                float newY = event.getY(pointerIndex);

                //получаем path и предыдущую точку связанные с этим пальцем
                Path path = mPathMap.get(pointerId);
                Point point = mPreviousPointMap.get(pointerId);

                //вычисляем как далеко юзер сдвинул палец с последнего обновления
                float deltaX = Math.abs(newX - point.x);
                float deltaY = Math.abs(newY - point.y);

                //если расстояние достаточное для реагирования
                if (deltaX >= TOUCH_TOLERANCE || deltaY >= TOUCH_TOLERANCE) {
                    //передвигаем path в новое местоположение
                    path.quadTo(point.x, point.y, (newX + point.x)/2, (newY + point.y)/2);

                    //сохраним новые координаты
                    point.x = (int) newX;
                    point.y = (int) newY;
                }
            }
        }
    }

    /** вызывается когда пользователь закончил качание */
    private void touchEnded(int lineId) {
        Path path = mPathMap.get(lineId); //получаем соответствующий path
        mBitmapCanvas.drawPath(path, mPaintLine); //рисуем
        path.reset(); //сбрасываем path
    }

    public void clear() {
        mPathMap.clear();
        mPreviousPointMap.clear();
        mBitmap.eraseColor(Color.WHITE);
        invalidate();
    }

    public void setDrawingColor(int color) {
        mPaintLine.setColor(color);
    }

    public int getDrawingColor() {
        return mPaintLine.getColor();
    }

    public void setLineWidth(int width) {
        mPaintLine.setStrokeWidth(width);
    }

    public int getLineWidth() {
        return (int) mPaintLine.getStrokeWidth();
    }

    /** сохраняет текущее изображение в галерею */
    public void saveImage() {
        final String name = "MyPaint" + System.currentTimeMillis() + ".jpg";

        //сохраняем изображение на девайс
        String location = MediaStore.Images.Media.insertImage(getContext().getContentResolver(),
                mBitmap, name, "MyPaint Drawing");

        if (location != null) {
            //отображает сообщение, что изображение было охранено
            Toast message = Toast.makeText(getContext(), R.string.message_saved, Toast.LENGTH_SHORT);
            message.setGravity(Gravity.CENTER, message.getXOffset()/2, message.getYOffset()/2);
            message.show();
        } else {
            //отображает сообщение, что возникла ошибка сохранения
            Toast message = Toast.makeText(getContext(), R.string.message_error_saving, Toast.LENGTH_SHORT);
            message.setGravity(Gravity.CENTER, message.getXOffset()/2, message.getYOffset()/2);
            message.show();
        }
    }
}
