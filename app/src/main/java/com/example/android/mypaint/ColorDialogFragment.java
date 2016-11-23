package com.example.android.mypaint;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.SeekBar;

/** позволяет пользователю выбрать цвет для линий */
public class ColorDialogFragment extends DialogFragment {

    private SeekBar mAlphaSeekBar;
    private SeekBar mRedSeekBar;
    private SeekBar mGreenSeekBar;
    private SeekBar mBlueSeekBar;
    private View mColorView;
    private int mColor;

    /** создает AlertDialog и возвращает его */
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View colorDialogView = getActivity().getLayoutInflater().inflate(R.layout.fragment_color, null);
        builder.setView(colorDialogView);
        builder.setTitle(R.string.title_color_dialog);

        mAlphaSeekBar = (SeekBar) colorDialogView.findViewById(R.id.alpha_seek_bar);
        mRedSeekBar = (SeekBar) colorDialogView.findViewById(R.id.red_seek_bar);
        mGreenSeekBar = (SeekBar) colorDialogView.findViewById(R.id.green_seek_bar);
        mBlueSeekBar = (SeekBar) colorDialogView.findViewById(R.id.blue_seek_bar);
        mColorView = colorDialogView.findViewById(R.id.color_view);

        mAlphaSeekBar.setOnSeekBarChangeListener(colorChangeListener);
        mRedSeekBar.setOnSeekBarChangeListener(colorChangeListener);
        mGreenSeekBar.setOnSeekBarChangeListener(colorChangeListener);
        mBlueSeekBar.setOnSeekBarChangeListener(colorChangeListener);

        //используем текущий цвет чтобы установить значения SeekBar
        final PaintView paintView = getPaintFragment().getPaintView();
        mColor = paintView.getDrawingColor();
        mAlphaSeekBar.setProgress(Color.alpha(mColor));
        mRedSeekBar.setProgress(Color.red(mColor));
        mGreenSeekBar.setProgress(Color.green(mColor));
        mBlueSeekBar.setProgress(Color.blue(mColor));

        builder.setPositiveButton(R.string.button_set_color,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        paintView.setDrawingColor(mColor);
                    }
                });

        return builder.create();
    }

    /** получает ссылку на MainActivityFragment */
    private MainActivityFragment getPaintFragment(){
        return (MainActivityFragment) getFragmentManager().findFragmentById(R.id.my_paint_fragment);
    }

    /** сообщает MainActivityFragment что диалог сейчас показывается */
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        MainActivityFragment fragment = getPaintFragment();

        if (fragment != null) {
            fragment.setDialogOnScreen(true);
        }
    }

    /** сообщает MainActivityFragment что диалог больше не показывается */
    @Override
    public void onDetach() {
        super.onDetach();
        MainActivityFragment fragment = getPaintFragment();

        if (fragment != null) {
            fragment.setDialogOnScreen(false);
        }
    }

    /** слушатель для SeekBars */
    private final SeekBar.OnSeekBarChangeListener colorChangeListener =
            new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                    if (b) { //если юзер, а не программа, изменила SeekBar
                        mColor = Color.argb(mAlphaSeekBar.getProgress(), mRedSeekBar.getProgress(),
                                mGreenSeekBar.getProgress(), mBlueSeekBar.getProgress());
                        mColorView.setBackgroundColor(mColor);
                    }
                }
                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {}

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {}
            };
}
