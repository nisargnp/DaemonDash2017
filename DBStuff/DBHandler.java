package com.example.matthew.databasetest2;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;
import java.util.List;
/**
 * Created by Matthew on 1/28/2017.
 */

public class DBHandler extends SQLiteOpenHelper {
    // Database Version
    private static final int DATABASE_VERSION = 1;
    // Database name
    private static final String DATABASE_NAME = "shopsInfo";
    // Contacts table name
    private static final String TABLE_SHOPS = "shops";
    // Shops Table Columns names
    private static final String KEY_ID = "id";
    private static final String KEY_NAME = "name";
    private static final String KEY_PRODUCTS = "products";
    private static final String KEY_PRICES = "prices";
    private static final String KEY_REVIEWER = "reviewer";
    private static final String KEY_RATING = "rating";
    private static final String KEY_STOCK = "stock";

    public DBHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db) {
        String CREATE_CONTACTS_TABLE = "CREATE TABLE " + TABLE_SHOPS + "("
                + KEY_ID + " INTEGER PRIMARY KEY," + KEY_NAME + " TEXT,"
                + KEY_PRODUCTS + " TEXT," + KEY_PRICES + " TEXT,"
                + KEY_REVIEWER + " TEXT," + KEY_RATING + " TEXT,"
                + KEY_STOCK + " TEXT" + ")";
        db.execSQL(CREATE_CONTACTS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS" + TABLE_SHOPS);
    }

    // Adding new shop
    public void addShop(Shop shop) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_NAME, shop.getName()); // Shop Name
        values.put(KEY_SH_ADDR, shop.getAddresss()); // Shop Address
        // Inserting Rows
        db.insert(TABLE_SHOPS, null, values);
        db.close(); // Closing database connection
    }

    public Shop getShop(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_SHOPS, new String[] {
                KEY_ID, KEY_NAME, KEY_PRODUCTS, KEY_PRICES,
                KEY_REVIEWER, KEY_RATING, KEY_STOCK}, KEY_ID + "=?",
                new String[] { String.valueOf(id) },
                null, null, null, null);
        if (cursor != null)
            cursor.moveToFirst();
        Shop contact = new Shop(Integer.parseInt(cursor.getString(0)),
                cursor.getString(1), cursor.getString(2), cursor.getString(3),
                cursor.getString(4), cursor.getString(5), cursor.getString(6));
        // Return shop
        return contact;
    }

    // Getting All Shops
    public List<Shop> getAllShops() {
        List<Shop> shopList = new ArrayList<Shop>();

        String selectQuery = "SELECT * FROM " + TABLE_SHOPS;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // Looping through all rows and adding to list
         if (cursor.moveToFirst()) {
             do {
                 Shop shop = new Shop();
                 shop.setId(Integer.parseInt(cursor.getString(0)));
                 shop.setName(cursor.getString(1));
                 shop.setProducts(cursor.getString(2));
                 shop.setPrices(cursor.getString(3));
                 shop.setReviewer(cursor.getString(4));
                 shop.setRating(cursor.getString(5));
                 shop.setStockID(cursor.getString(6));

                 // Adding contact to list
                 shopList.add(shop);
             } while (cursor.moveToNext());
         }

        // Return contact list
        return shopList;
    }

    // Getting number of shops
    public int getShopsCount() {
        String countQuery = "SELECT * FROM " + TABLE_SHOPS;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        cursor.close();

        // Return count
        return cursor.getCount();
    }

    // Update a shop
    public int updateShop(Shop shop) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_NAME, shop.getName());
        values.put(KEY_PRODUCTS, shop.getProducts());
        values.put(KEY_PRICES, shop.getPrices());
        values.put(KEY_REVIEWER, shop.getReviewer());
        values.put(KEY_RATING, shop.getRating());
        values.put(KEY_STOCK, shop.getStockID());

        // Updating row
        return db.update(TABLE_SHOPS, values, KEY_ID + " = ?",
                new String[] {String.valueOf(shop.getId())});
    }

    // Delete a shop
    public void deleteShop(Shop shop) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_SHOPS, KEY_ID + " = ?",
                new String[] { String.valueOf(shop.getId())});
        db.close();
    }
}
