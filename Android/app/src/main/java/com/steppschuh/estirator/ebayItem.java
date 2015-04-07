package com.steppschuh.estirator;

public class ebayItem {

    private String id;
    private String title;
    private String description;
    private String imageUrl;
    private double price;
    private double estimatedPrice;
    private double pricePercentage = -1;

    private boolean hasEstimatedPrice;

    public ebayItem(String id, String title, String description, String imageUrl, double price) {
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

        return (int) Math.round((100 * price) / pricePercentage);
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
