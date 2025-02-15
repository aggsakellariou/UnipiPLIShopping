package com.unipi.unipiplishopping.models;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Map;

public class Products {

    private int id;
    private String title;
    private String description;
    private String imageURL;
    private String releaseDate;
    private double price;
    private Map<String, Double> location;

    public Products(int id, String title, String description, String imageURL, String releaseDate, double price, Map<String, Double> location) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.imageURL = imageURL;
        this.releaseDate = releaseDate;
        this.price = price;
        this.location = location;
    }

    public Products() {
    }

    public int getId() {
        return id;
    }
    public String getTitle() {
        return title;
    }
    public String getDescription() {
        return description;
    }
    public String getImageURL() {
        return imageURL;
    }
    public String getReleaseDate() {
        return releaseDate;
    }
    public double getPrice() {
        return price;
    }
    public Map<String, Double> getLocation() {
        return location;
    }
}