package com.steppschuh.estirator;

import android.app.Activity;
import android.app.Application;
import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import java.util.ArrayList;
import java.util.List;

public class MobileApp extends Application {

    public static final String TAG = "estimator";

    public static final String API_USER = "hpi_hackathon";
    public static final String API_PASS = "dsk38a1l";

    public static final int PRICE_MINIMUM = 5;
    public static final int PRICE_MAXIMUM = 15000;

    public static final int ESTIMATED_ITEMS_COUNT = 5; // request X estimations before showing the ranking

    public boolean isInitialized = false;
    private Activity contextActivity;

    private List<EbayItem> items;
    private int currentPageIndex = 0;

    /**
     * Methods for initializing the app
     */
    public void initialize(Activity contextActivity) {
        Log.d(TAG, "Initializing app");

        this.contextActivity = contextActivity;

        try	{
            initializeHelpers();

            //Invoke asynchronous initialization
            initializeAsync();

            Log.d(TAG, "Initialization done");
            isInitialized = true;
        } catch (Exception ex) {
            Log.e(TAG, "Error during initialization!");
            ex.printStackTrace();
            isInitialized = false;
        }
    }

    private void initializeHelpers() throws Exception {
        Log.d(TAG, "Initializing helpers");

    }

    /**
     * Methods for initializing the app asynchronously
     */
    public void	initializeAsync() {
        (new Thread() {
            @Override
            public void run() {
                Log.d(TAG, "Initializing asynchronously");

                requestItems();

                Log.d(TAG, "Asynchronously initialization done");
            }
        }).start();
    }

    public void requestItems() {
        Log.d(TAG, "Requesting new items to estimate");
        items = new ArrayList<EbayItem>();

        try {
            Ion.with(contextActivity)
                .load(getItemRequestURL())
                .setTimeout(5000)
                .basicAuthentication(API_USER, API_PASS)
                .asJsonObject()
                .setCallback(new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception e, JsonObject result) {
                        if (e != null) {
                            e.printStackTrace();
                            return;
                        }

                        JsonObject ads = result.getAsJsonObject("{http://www.ebayclassifiedsgroup.com/schema/ad/v1}ads");
                        JsonArray adArray = ads.getAsJsonObject("value").getAsJsonArray("ad");

                        for (JsonElement adElement : adArray) {
                            try {
                                items.add(EbayItem.parseFromJson((JsonObject) adElement));
                            } catch (Exception ex) {
                                Log.e(TAG, "Unable to parse item: " + ex.getMessage());
                                //ex.printStackTrace();
                            }
                        }

                        Log.d(TAG, "Items received: " + items.size());
                    }
                });
        } catch (Exception ex) {
            Log.e(TAG, "Error while requesting items");
            ex.printStackTrace();
        }
    }

    public EbayItem getItemByID(String id) {
        if (items.size() > 0) {
            for (EbayItem item : items) {
                if (item.getId().equals(id)) {
                    return item;
                }
            }
        }
        return null;
    }

    public EbayItem getNextItem() {
        Log.d(MobileApp.TAG, "Searching for next item (out of " + items.size() + ")");
        if (items.size() > 0) {
            for (EbayItem item : items) {
                if (!item.itemSkipped() && !item.hasEstimatedPrice()) {
                    return item;
                }
            }
            // all items estimated
            requestItems();
        }
        Log.w(MobileApp.TAG, "No item available");
        return null;
    }

    public int getEstimatedItemsCount() {
        int count = 0;
        for (EbayItem item : items) {
            if (item.hasEstimatedPrice()) {
                count++;
            }
        }
        return count;
    }

    private String getItemRequestURL() {
        String baseUrl = "https://api.ebay-kleinanzeigen.de/api/ads.json";

        baseUrl += "?pictureRequired=true";
        baseUrl += "&minPrice=" + PRICE_MINIMUM;
        baseUrl += "&maxPrice=" + PRICE_MAXIMUM;
        baseUrl += "&priceType=" + "SPECIFIED_AMOUNT";
        baseUrl += "&page=" + currentPageIndex;
        baseUrl += "&size=" + "20";

        return baseUrl;
    }

    /**
     * Getter & Setter
     */
    public Activity getContextActivity() {
        return contextActivity;
    }

    public void setContextActivity(Activity contextActivity) {
        this.contextActivity = contextActivity;
    }

    public boolean isInitialized() {
        return isInitialized;
    }

    public void setInitialized(boolean isInitialized) {
        this.isInitialized = isInitialized;
    }

    public List<EbayItem> getItems() {
        return items;
    }

    public void setItems(List<EbayItem> items) {
        this.items = items;
    }
}
