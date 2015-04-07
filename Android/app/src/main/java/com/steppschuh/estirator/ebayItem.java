package com.steppschuh.estirator;

import android.text.Html;
import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

public class EbayItem {

    public static final String IMAGE_ID = "{imageId}";
    public static final String IMAGE_ID_LARGE = "45";

    private String id;
    private String title;
    private String description;
    private String imageUrl;
    private double price;
    private double estimatedPrice = -1;
    private double pricePercentage = -1;

    private boolean itemSkipped = false;

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

    public static EbayItem parseFromJson(JsonObject jsonObject) throws Exception{
        EbayItem item = new EbayItem();

        //Log.d(MobileApp.TAG, "Parsing JSON item: " + jsonObject);

        item.setId(jsonObject.getAsJsonPrimitive("id").getAsString());
        String title = jsonObject.getAsJsonObject("title").getAsJsonPrimitive("value").getAsString();
        item.setTitle(Html.fromHtml(title).toString());

        Log.d(MobileApp.TAG, "Parsing item: " + item.getTitle());

        String description = jsonObject.getAsJsonObject("description").getAsJsonPrimitive("value").getAsString();
        item.setDescription(Html.fromHtml(description).toString());

        JsonPrimitive price = jsonObject.getAsJsonObject("price").getAsJsonObject("amount").getAsJsonPrimitive("value");
        if (price != null) {
            item.setPrice(price.getAsDouble());
        } else {
            throw new Exception("No price");
        }

        // image URL
        JsonArray pictures = jsonObject.getAsJsonObject("pictures").getAsJsonArray("picture");
        if (pictures != null && pictures.size() > 0) {
            JsonArray pictureLinks = ((JsonObject) pictures.get(0)).getAsJsonArray("link");
            for (JsonElement pictureLink : pictureLinks) {
                // looks like this: http://i.ebayimg.com/00/s/NTUwWDQ2MQ==/z/wWsAAOSwEeFVJAuJ/$_{imageId}.JPG
                String href = ((JsonObject) pictureLink).getAsJsonPrimitive("href").getAsString();
                if (href.contains(IMAGE_ID)) {
                    item.setImageUrl(href.replace(IMAGE_ID, IMAGE_ID_LARGE));
                    break;
                }
            }
        } else {
            throw new Exception("No photo");
        }

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
        Log.d(MobileApp.TAG, "Item price estimated: " + estimatedPrice);
        this.estimatedPrice = estimatedPrice;
    }

    public boolean hasEstimatedPrice() {
        return estimatedPrice >= 0;
    }

    public boolean itemSkipped() {
        return itemSkipped;
    }

    public void setItemSkipped(boolean itemSkipped) {
        if (itemSkipped) {
            Log.d(MobileApp.TAG, "Item skipped: " + getTitle());
        }
        this.itemSkipped = itemSkipped;
    }
}
