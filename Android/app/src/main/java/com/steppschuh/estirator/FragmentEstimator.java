package com.steppschuh.estirator;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.koushikdutta.ion.Ion;

public class FragmentEstimator extends Fragment {

    SeekBar seekBar;
    View contentFragment;
    TextView estimatedPriceValue;
    TextView itemTitle;
    ImageView itemImage;

    EbayItem currentItem;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        contentFragment = inflater.inflate(R.layout.fragment_estimate_item, container, false);

        getActivity().setTitle(getString(R.string.title_estimate));

        setupUi();

        return contentFragment;
    }

    private void setupUi() {
        itemTitle = (TextView) contentFragment.findViewById(R.id.itemNameLabel);
        estimatedPriceValue = (TextView) contentFragment.findViewById(R.id.estimatePriceValue);
        itemImage = (ImageView) contentFragment.findViewById(R.id.itemImage);

        seekBar = (SeekBar) contentFragment.findViewById(R.id.seekBar);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int seekValue = 0;

            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                seekValue = progress;

                int price = currentItem.percentageToPrice(seekValue);
                estimatedPriceValue.setText(String.valueOf(price) + "€");
            }

            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    private void showSampleItem() {
        EbayItem item = new EbayItem("1234", "Sample Item", "Description", "http://i.ebayimg.com/00/s/NjgxWDEwMjQ=/z/6nMAAOSwBLlVI-cr/$_45.JPG", 80.00);
        showItem(item);
    }

    private void showItem(EbayItem item) {
        currentItem = item;
        itemTitle.setText(item.getTitle());

        // load image
        Ion.with(itemImage)
            .placeholder(R.drawable.placeholder)
            .error(R.drawable.error)
            //.animateLoad(spinAnimation)
            //.animateIn(fadeInAnimation)
            .load(item.getImageUrl());

        currentItem = item;

        // reset seek bar
        seekBar.setProgress(50);
    }

    @Override
    public void onStart() {
        super.onStart();
        showSampleItem();
    }
}
