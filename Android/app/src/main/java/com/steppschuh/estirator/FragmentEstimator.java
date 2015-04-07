package com.steppschuh.estirator;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.koushikdutta.ion.Ion;

public class FragmentEstimator extends Fragment {

    MobileApp app;

    SeekBar seekBar;
    View contentFragment;
    TextView estimatedPriceValue;
    TextView itemTitle;
    ImageView itemImage;
    Button submitItem;
    Button skipItem;

    EbayItem currentItem;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        contentFragment = inflater.inflate(R.layout.fragment_estimate_item, container, false);

        app = (MobileApp) getActivity().getApplicationContext();
        getActivity().setTitle(getString(R.string.title_estimate));

        setupUi();

        return contentFragment;
    }

    private void setupUi() {
        skipItem = (Button) contentFragment.findViewById(R.id.skipItem);
        submitItem = (Button) contentFragment.findViewById(R.id.submitItem);

        itemTitle = (TextView) contentFragment.findViewById(R.id.itemNameLabel);
        estimatedPriceValue = (TextView) contentFragment.findViewById(R.id.estimatePriceValue);
        itemImage = (ImageView) contentFragment.findViewById(R.id.itemImage);

        seekBar = (SeekBar) contentFragment.findViewById(R.id.seekBar);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int seekValue = 0;

            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                seekValue = progress;
                updatePriceValue(seekValue);
            }

            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            public void onStopTrackingTouch(SeekBar seekBar) {
                int price = currentItem.percentageToPrice(seekValue);
                currentItem.setEstimatedPrice(price);
            }
        });

        skipItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                skipItem();
            }
        });

        submitItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitItem();
            }
        });
    }

    private void showSampleItem() {
        EbayItem item = new EbayItem("1234", "Sample Item", "Description", "http://i.ebayimg.com/00/s/NjgxWDEwMjQ=/z/6nMAAOSwBLlVI-cr/$_45.JPG", 80.00);
        showItem(item);
    }

    private void skipItem() {
        EbayItem item = app.getItemByID(currentItem.getId());
        if (item != null) {
            app.getItemByID(currentItem.getId()).setItemSkipped(true);
        }

        showNextItem();
    }

    private void submitItem() {
        EbayItem item = app.getItemByID(currentItem.getId());
        if (item != null) {
            app.getItemByID(currentItem.getId()).setEstimatedPrice(currentItem.getEstimatedPrice());
        }

        if (app.getEstimatedItemsCount() >= MobileApp.ESTIMATED_ITEMS_COUNT) {
            Toast.makeText(getActivity(), getString(R.string.message_enough_estimations), Toast.LENGTH_LONG).show();
        } else {
            showNextItem();
        }
    }

    private void showNextItem() {
        EbayItem nextItem = app.getNextItem();
        if (nextItem != null) {
            showItem(nextItem);
        } else {
            Toast.makeText(getActivity(), getString(R.string.message_no_more_items), Toast.LENGTH_LONG).show();
        }
    }

    private void showItem(EbayItem item) {
        Log.d(MobileApp.TAG, "Showing item: " + item.getTitle());

        currentItem = item;

        itemTitle.setText(item.getTitle());

        // load image
        Ion.with(itemImage)
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.error)
                .load(item.getImageUrl());

        currentItem = item;

        // reset seek bar
        seekBar.setProgress(50);
        updatePriceValue(50);
    }

    private void updatePriceValue(int seekValue) {
        int price = currentItem.percentageToPrice(seekValue);
        currentItem.setEstimatedPrice(price);
        estimatedPriceValue.setText(String.valueOf(price) + "€");
    }

    @Override
    public void onStart() {
        super.onStart();
        showSampleItem();
    }
}
