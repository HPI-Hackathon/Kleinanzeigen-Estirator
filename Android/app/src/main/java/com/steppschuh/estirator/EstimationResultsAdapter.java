package com.steppschuh.estirator;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import java.util.ArrayList;

public class EstimationResultsAdapter extends ArrayAdapter<EbayItem> {

    ArrayList<EbayItem> items = new ArrayList<>();
    Activity context;

    public EstimationResultsAdapter(Activity context, int resource, ArrayList<EbayItem> items) {
        super(context, resource, items);
        this.context = context;
        this.items = items;
    }

    @Override
    public EbayItem getItem(int position) {
        return items.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = convertView;

        EbayItem currentItem = getItem(position);

        // reuse views
        if (rowView == null) {
            LayoutInflater inflater = context.getLayoutInflater();
            rowView = inflater.inflate(R.layout.estimation_result_item, null);
            rowView.setTag(currentItem.getId());
        }

        // fill data
        ((TextView) rowView.findViewById(R.id.itemNameLabel)).setText(currentItem.getTitle());
        ((TextView) rowView.findViewById(R.id.itemPriceValue)).setText(((int) currentItem.getPrice()) + "€");
        ((TextView) rowView.findViewById(R.id.itemDifferenceRelative)).setText(currentItem.getRelativeDifferenceString());
        ((TextView) rowView.findViewById(R.id.itemDifferenceAbsolute)).setText((int) (currentItem.getEstimatedPrice() - currentItem.getPrice()) + "€");

        ((TextView) rowView.findViewById(R.id.itemDifferenceRelative)).setTextColor(currentItem.getColorIndocator());

        // load image
        ImageView itemImage = (ImageView) rowView.findViewById(R.id.itemImage);
        Ion.with(itemImage)
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.error)
                .load(currentItem.getImageUrl());

        return rowView;
    }

    @Override
    public int getCount() {
        return items.size();
    }
}
