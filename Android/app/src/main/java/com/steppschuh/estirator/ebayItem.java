package com.steppschuh.estirator;

import android.util.Log;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class EbayItem {

    private String id;
    private String title;
    private String description;
    private String imageUrl;
    private double price;
    private double estimatedPrice;
    private double pricePercentage = -1;

    private boolean hasEstimatedPrice;

    public EbayItem() {
    }

    public EbayItem(String id, String title, String description, String imageUrl, double price) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.imageUrl = imageUrl;
        this.price = price;
    }

    public void generatePricePercentage() {
        pricePercentage = 50 - (Math.floor(Math.random() * 60) - 30);
    }

    public int percentageToPrice(int percentage) {
        if (pricePercentage < 0) {
            generatePricePercentage();
        }

        return (int) Math.round((percentage * price) / pricePercentage);
    }

    public static EbayItem parseFromJson(JsonObject jsonObject) {
        EbayItem item = new EbayItem();

        //Log.d(MobileApp.TAG, "Parsing JSON item: " + jsonObject);

        item.setId(jsonObject.getAsJsonPrimitive("id").getAsString());
        item.setTitle(jsonObject.getAsJsonObject("title").getAsJsonPrimitive("value").getAsString());


        Log.d(MobileApp.TAG, "Item id: " + item.getId());
        return item;
    }

    /**
     * Getter & Setter
     */
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public double getEstimatedPrice() {
        return estimatedPrice;
    }

    public void setEstimatedPrice(double estimatedPrice) {
        this.estimatedPrice = estimatedPrice;
    }

    public boolean isHasEstimatedPrice() {
        return hasEstimatedPrice;
    }

    public void setHasEstimatedPrice(boolean hasEstimatedPrice) {
        this.hasEstimatedPrice = hasEstimatedPrice;
    }
}
