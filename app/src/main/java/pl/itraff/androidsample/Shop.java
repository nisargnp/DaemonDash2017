package pl.itraff.androidsample;

import android.renderscript.Sampler;

import java.util.HashMap;
import java.util.Scanner;

/**
 * Created by Matthew on 1/28/2017.
 */

public class Shop {
    private int id = -1;
    private String name = "";
    private String products = "";
    private String prices = "";
    private String reviewer = "";
    private String rating = "";
    private String stockID = "";

    private HashMap<String, String> productPrices;
    private HashMap<String, String> reviews;

    public Shop() {
        productPrices = new HashMap<String, String>();
        reviews = new HashMap<String, String>();
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

        productPrices = new HashMap<String, String>();
        reviews = new HashMap<String, String>();
    }

    public void setId(int id) { this.id = id; }

    public void setName(String name) { this.name = name; }

    public void setProducts(String products) { this.products = products; }

    public void setPrices(String prices) { this.prices = prices; }

    public void setReviewer(String reviewer) { this.reviewer = reviewer; }

    public void setRating(String rating) { this.rating = rating; }

    public void setStockID(String stockID) { this.stockID = stockID; }

    public int getId() { return id; }

    public String getName() { return name; }

    public String getProducts() { return products; }

    public String getPrices() { return prices; }

    public String getReviewer() { return reviewer; }

    public String getRating() { return rating; }

    public String getStockID() { return stockID; }

    public String toString() {
        StringBuilder value = new StringBuilder("");
        value.append("Name: " + name + "\n");
        value.append("Products: ");
        for (String key : productPrices.keySet())
            value.append("\n\t" + key + ": " + productPrices.get(key));

        value.append("\nReviews: ");

        for (String key : reviews.keySet())
            value.append("\n\t" + key + ": " + reviews.get(key));
        value.append("\nStock Symbol: " + stockID);

        return value.toString();
    }

    public void genHashMaps() {
        Scanner productScanner = new Scanner(products);
        productScanner.useDelimiter(",");
        Scanner priceScanner = new Scanner(prices);
        priceScanner.useDelimiter(",");
        Scanner reviewerScanner = new Scanner(reviewer);
        reviewerScanner.useDelimiter(",");
        Scanner ratingScanner = new Scanner(rating);
        ratingScanner.useDelimiter(",");

        while (productScanner.hasNext())
            productPrices.put(productScanner.next().trim(), priceScanner.next().trim());
        productScanner.close();
        priceScanner.close();

        while (reviewerScanner.hasNext())
            reviews.put(reviewerScanner.next().trim(), ratingScanner.next().trim());
        reviewerScanner.close();
        ratingScanner.close();
    }
}
