package com.steppschuh.estirator;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.graphics.Palette;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

public class FragmentEstimator extends Fragment {

    MobileApp app;

    SeekBar seekBar;
    View contentFragment;
    TextView estimatedPriceValue;
    TextView itemTitle;
    RelativeLayout itemTitleContainer;
    ImageView itemImage;
    Button submitItem;
    Button skipItem;

    EbayItem currentItem;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        contentFragment = inflater.inflate(R.layout.fragment_estimator, container, false);

        app = (MobileApp) getActivity().getApplicationContext();
        getActivity().setTitle(getString(R.string.title_estimate));

        setupUi();

        return contentFragment;
    }

    private void setupUi() {
        skipItem = (Button) contentFragment.findViewById(R.id.skipItem);
        submitItem = (Button) contentFragment.findViewById(R.id.submitItem);

        itemTitle = (TextView) contentFragment.findViewById(R.id.itemNameLabel);
        itemTitleContainer = (RelativeLayout) contentFragment.findViewById(R.id.itemNameContainer);
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
        currentItem = item;
        //showItem(item);
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
            app.getItemByID(currentItem.getId()).submitEstimation();
        }

        if (app.getEstimatedItemsCount() >= MobileApp.ESTIMATED_ITEMS_COUNT) {
            Toast.makeText(getActivity(), getString(R.string.message_enough_estimations), Toast.LENGTH_LONG).show();
            ((MainActivity) getActivity()).showEstimationResults();
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
                .load(item.getImageUrl())
                .setCallback(new FutureCallback<ImageView>() {
                    @Override
                    public void onCompleted(Exception e, ImageView result) {
                        try {
                            Bitmap bitmap = ((BitmapDrawable) result.getDrawable()).getBitmap();
                            updateColors(bitmap);
                        } catch (Exception ex) {

                        }
                    }
                });

        currentItem = item;

        // reset seek bar
        seekBar.setProgress(50);
        updatePriceValue(50);
    }

    private void updatePriceValue(int seekValue) {
        if (currentItem != null) {
            int price = currentItem.percentageToPrice(seekValue);
            currentItem.setEstimatedPrice(price);
            estimatedPriceValue.setText(String.valueOf(price) + "€");
        }
    }

    private void updateColors(Bitmap bitmap) {
        //Bitmap bitmap = ((BitmapDrawable) itemImage.getDrawable()).getBitmap();
        Palette.generateAsync(bitmap, new Palette.PaletteAsyncListener() {
            public void onGenerated(Palette palette) {
                Palette.Swatch swatch = palette.getVibrantSwatch();

                int rgbColor = swatch.getRgb();
                // Gets the HSL values
                // Hue between 0 and 360
                // Saturation between 0 and 1
                // Lightness between 0 and 1
                float[] hslValues = swatch.getHsl();
                // Gets the number of pixels represented by this swatch
                int pixelCount = swatch.getPopulation();
                // Gets an appropriate title text color
                int titleTextColor = swatch.getTitleTextColor();
                // Gets an appropriate body text color
                int bodyTextColor = swatch.getBodyTextColor();

                int backgroundColor = palette.getDarkMutedColor(Color.BLACK);

                itemTitleContainer.setBackgroundColor(backgroundColor);
                itemTitle.setTextColor(titleTextColor);
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        showSampleItem();
    }
}
