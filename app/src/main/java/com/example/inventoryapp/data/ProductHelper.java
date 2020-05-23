package com.example.inventoryapp.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.inventoryapp.data.ProductContract.ProductEntry;

public class ProductHelper extends SQLiteOpenHelper {
    private static final String TAG = ProductHelper.class.toString();

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "Inventory.db";

    public ProductHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TALBE = "CREATE TABLE " + ProductEntry.TABLE_NAME + "(" +
                ProductEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                ProductEntry.COLUMN_NAME + " TEXT, " +
                ProductEntry.COLUMN_PRICE + " REAL, " +
                ProductEntry.COLUMN_QUANTITY + " INTEGER);";

        db.execSQL(CREATE_TALBE); //execute SQL statement to create a table

        Log.v(TAG, "The SQL command of create table is: " + CREATE_TALBE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
