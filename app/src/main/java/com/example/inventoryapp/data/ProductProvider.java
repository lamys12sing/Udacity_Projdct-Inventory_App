package com.example.inventoryapp.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import com.example.inventoryapp.data.ProductContract.ProductEntry;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class ProductProvider extends ContentProvider {
    private static final String TAG = ProductProvider.class.toString();

    private static final int PRODUCT = 100;
    private static final int PRODUCT_ID = 101;

    private static  final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sUriMatcher.addURI(ProductContract.CONTENT_AUTHORITY, ProductContract.PATH_PRODUCTS, PRODUCT);
        sUriMatcher.addURI(ProductContract.CONTENT_AUTHORITY, ProductContract.PATH_PRODUCTS + "/#", PRODUCT_ID);
    }

    private ProductHelper mDbHelper;

    @Override
    public boolean onCreate() {
        mDbHelper = new ProductHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        Cursor cursor;
        int match = sUriMatcher.match(uri);
        switch (match){
            case PRODUCT: //Query for all data
                cursor = db.query(ProductEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;

            case PRODUCT_ID:
                selection = ProductEntry._ID + "=?"; //Set the selection is the id of data
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))}; //Find the data's id from the last part of the uri
                cursor = db.query(ProductEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;

            default:
                throw new IllegalArgumentException("Cannot query unknown URI: " + uri);
        }
        //Set notification URI on the Cursor, so that the cursor will update when the URI is updated.
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        final int match = sUriMatcher.match(uri);
        switch (match){
            case PRODUCT:
                return insertProduct(uri, values);

            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    /**
     * Helper method to insert new product.
     */
    private Uri insertProduct(Uri uri, ContentValues values){
        //Sanity check
        if (values.containsKey(ProductEntry.COLUMN_NAME)){
            String name = values.getAsString(ProductEntry.COLUMN_NAME);
            if (name == null){
                throw new IllegalArgumentException("Product requires a name");
            }
        }

        if (values.containsKey(ProductEntry.COLUMN_PRICE)){
            Double price = values.getAsDouble(ProductEntry.COLUMN_PRICE);
            if (price != null && price < 0){
                throw new IllegalArgumentException("Product requires valid price");
            }
        }

        if (values.containsKey(ProductEntry.COLUMN_QUANTITY)){
            Integer quantity = values.getAsInteger(ProductEntry.COLUMN_QUANTITY);
            if (quantity != null && quantity < 0){
                throw new IllegalArgumentException("product requires valid quantity");
            }
        }

        // If there are no values to update, then don't try to update the database
        if (values.size() == 0){
            return null;
        }

        // Get writeable database to update the data
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        long newRowId = db.insert(ProductEntry.TABLE_NAME, null, values);

        if (newRowId == -1){ //Check if the insertion is successful
            Log.e(TAG,"Failed to insert row for " + uri);
            return null;
        }
        //Notify all listeners that the data has changed for the pet content URI
        getContext().getContentResolver().notifyChange(uri, null);

        return ContentUris.withAppendedId(uri, newRowId); //Return uri that contain the new inserted row id in the end of the path.
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match){
            case PRODUCT:
                return updateProduct(uri, values, selection, selectionArgs);

            case PRODUCT_ID:
                selection = ProductEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateProduct(uri, values, selection, selectionArgs);

            default:
                throw new IllegalArgumentException("Cannot update unknown URI: " + uri);
        }
    }

    /**
     *  Helper method to update product
     */
    private int updateProduct(Uri uri, ContentValues values, String selection, String[] selectionArgs){
        //Sanity check
        if (values.containsKey(ProductEntry.COLUMN_NAME)){
            String name = values.getAsString(ProductEntry.COLUMN_NAME);
            if (name == null){
                throw new IllegalArgumentException("Product requires a name");
            }
        }

        if (values.containsKey(ProductEntry.COLUMN_PRICE)){
            double price = values.getAsDouble(ProductEntry.COLUMN_PRICE);
            if (price < 0){
                throw new IllegalArgumentException("Product requires valid price");
            }
        }

        if (values.containsKey(ProductEntry.COLUMN_QUANTITY)){
            int quantity = values.getAsInteger(ProductEntry.COLUMN_QUANTITY);
            if (quantity < 0){
                throw new IllegalArgumentException("product requires valid quanitiy");
            }
        }

        // If there are no values to update, then don't try to update the database
        if (values.size() == 0){
            return 0;
        }

        // Get writable database to update product
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        int rowUpdated = db.update(ProductEntry.TABLE_NAME, values, selection, selectionArgs);

        if (rowUpdated != 0){
            //Notify all listeners that the data has changed for the pet content URI
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowUpdated;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        int rowDeleted;

        final int match = sUriMatcher.match(uri);
        switch (match){
            case PRODUCT:
                rowDeleted = db.delete(ProductEntry.TABLE_NAME, selection, selectionArgs);
                break;

            case PRODUCT_ID:
                selection = ProductEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};

                rowDeleted = db.delete(ProductEntry.TABLE_NAME, selection, selectionArgs);
                break;

            default:
                throw new IllegalArgumentException("Deletion is not support for " + uri);
        }

        if (rowDeleted != 0){
            //Notify all listeners that the data has changed for the pet content URI
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowDeleted;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        int match = sUriMatcher.match(uri);
        switch (match){
            case PRODUCT:
                return ProductEntry.CONTENT_LIST_TYPE;

            case PRODUCT_ID:
                return ProductEntry.CONTENT_ITEM_TYPE;

            default:
                throw new IllegalArgumentException("Unknown URI " + uri + " with match " + match);
        }
    }
}
