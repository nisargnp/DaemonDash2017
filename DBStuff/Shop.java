package com.example.matthew.databasetest2;

/**
 * Created by Matthew on 1/28/2017.
 */

public class Shop {
    private int id;
    private String name;
    private String products;
    private String prices;
    private String reviewer;
    private String rating;
    private String stockID;

    public Shop() {

    }

    public Shop(int id, String name, String products, String prices,
                String reviewer, String rating, String stockID) {
        this.id = id;
        this.name = name;
        this.products = products;
        this.prices = prices;
        this.reviewer = reviewer;
        this.rating = rating;
        this.stockID = stockID;
    }

    public void setId(int id) { this.id = id; }

    public void setName(String name) { this.name = name; }

    public void setProducts(String products) { this.products = products; }

    public void setPrices(String prices) { this.prices = prices; }

    public void setReviewer(String reviewer) { this.reviewer = reviewer; }

    public void setRating(String rating) { this.rating = rating; }

    public void setStockID(String stockID) { this.stockID = stockID; }

    public int getId(int id) { return id; }

    public String getName(String name) { return name; }

    public String getProducts(String products) { return products; }

    public String getPrices(String prices) { return prices; }

    public String getReviewer(String reviewer) { return reviewer; }

    public String getRating(String rating) { return rating; }

    public String getStockID(String stockID) { return stockID; }
}
