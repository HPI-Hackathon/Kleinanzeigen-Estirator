package com.steppschuh.estirator;

import android.app.Activity;
import android.app.Application;
import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MobileApp extends Application {

    public static final String TAG = "estimator";
    public static final String API_USER = "hpi_hackathon";
    public static final String API_PASS = "dsk38a1l";

    public boolean isInitialized = false;
    private Activity contextActivity;

    private List<EbayItem> items;

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
                                Log.e(TAG, "Unable to parse item");
                                ex.printStackTrace();
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

    private String getItemRequestURL() {
        return "https://api.ebay-kleinanzeigen.de/api/ads.json";
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
}
