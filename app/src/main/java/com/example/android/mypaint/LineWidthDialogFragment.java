package com.example.android.mypaint;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;

/** позволяет пользователю выбрать толщину линий */
public class LineWidthDialogFragment extends DialogFragment {

    private ImageView mWidthImageView;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View lineWidthDialogView = getActivity().getLayoutInflater()
                .inflate(R.layout.fragment_line_width, null);
        builder.setView(lineWidthDialogView);
        builder.setTitle(R.string.title_line_width_dialog);

        mWidthImageView = (ImageView) lineWidthDialogView.findViewById(R.id.width_image_view);

        //конфигурируем SeekBar
        final PaintView paintView = getPaintFragment().getPaintView();
        final SeekBar widthSeekBar = (SeekBar) lineWidthDialogView.findViewById(R.id.width_seek_bar);
        widthSeekBar.setOnSeekBarChangeListener(lineWidthChangeListener);
        widthSeekBar.setProgress(paintView.getLineWidth());

        builder.setPositiveButton(R.string.button_set_line_width,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        paintView.setLineWidth(widthSeekBar.getProgress());
                    }
                });

        return builder.create();
    }

    private MainActivityFragment getPaintFragment() {
        return (MainActivityFragment) getFragmentManager().findFragmentById(R.id.my_paint_fragment);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        MainActivityFragment fragment = getPaintFragment();

        if (fragment != null) {
            fragment.setDialogOnScreen(true);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        MainActivityFragment fragment = getPaintFragment();

        if (fragment != null) {
            fragment.setDialogOnScreen(false);
        }
    }

    private final SeekBar.OnSeekBarChangeListener lineWidthChangeListener =
            new SeekBar.OnSeekBarChangeListener() {
                final Bitmap bitmap = Bitmap.createBitmap(400, 100, Bitmap.Config.ARGB_8888);
                final Canvas canvas = new Canvas(bitmap);

                @Override
                public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                    Paint paint = new Paint();
                    paint.setColor(getPaintFragment().getPaintView().getDrawingColor());
                    paint.setStrokeCap(Paint.Cap.ROUND);
                    paint.setStrokeWidth(i);

                    bitmap.eraseColor(getResources().getColor(android.R.color.transparent,
                            getContext().getTheme()));
                    canvas.drawLine(30, 50, 370, 50, paint);
                    mWidthImageView.setImageBitmap(bitmap);
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {}

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {}
            };
}
