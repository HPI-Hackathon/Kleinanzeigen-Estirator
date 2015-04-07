package com.steppschuh.estirator;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

public class FragmentEstimator extends Fragment {

    SeekBar seekBar;
    View contentFragment;
    TextView estimatedPriceValue;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        contentFragment = inflater.inflate(R.layout.fragment_estimate_item, container, false);

        getActivity().setTitle(getString(R.string.estimate_price));

        setupUi();

        return contentFragment;
    }

    private void setupUi() {
        estimatedPriceValue = (TextView) contentFragment.findViewById(R.id.estimatePriceValue);

        seekBar = (SeekBar) contentFragment.findViewById(R.id.seekBar);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progressChanged = 0;

            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                progressChanged = progress;
                estimatedPriceValue.setText(String.valueOf(progressChanged) + "€");
            }

            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

}
