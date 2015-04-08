package com.steppschuh.estirator;

import android.graphics.Color;
import android.text.Html;
import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.ByteArrayBuffer;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

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

    public int getColorIndocator() {
        int difference = getRelativeDifference();
        int color;

        if (difference <= 100) {
            color = Color.argb(200,0,255,0);

        } else {
            color = Color.argb(200,255,0,0);
        }

        return color;
    }

    public int getRelativeDifference() {
        return (int) Math.round((100 * estimatedPrice) / price);
    }

    public String getRelativeDifferenceString() {
        int difference = getRelativeDifference();
        String differenceString = "";

        if (difference < 100) {
            differenceString += "-";
        } else if (difference > 100) {
            differenceString += "+";
        }

        differenceString += String.valueOf(difference) + "%";

        return differenceString;
    }

    public void submitEstimation() {
        (new Thread() {
            public void run() {
                HttpClient httpclient = new DefaultHttpClient();
                HttpGet httpget = new HttpGet(generateSubmitURL());
                HttpResponse response;
                try {
                    response = httpclient.execute(httpget);
                    HttpEntity entity = response.getEntity();
                    Log.d(MobileApp.TAG, "Submitted estimation: " + generateSubmitURL());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public String generateSubmitURL() {
        String baseUrl = "http://estirator.appspot.com/estimate/";
        baseUrl += getId() + "/";
        baseUrl += String.valueOf((int) getEstimatedPrice()) + "/";

        return baseUrl;
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
