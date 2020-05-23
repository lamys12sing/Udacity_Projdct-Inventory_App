package com.example.inventoryapp.data;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.cursoradapter.widget.CursorAdapter;

import com.example.inventoryapp.R;
import com.example.inventoryapp.data.ProductContract.ProductEntry;

import java.text.NumberFormat;

public class ProductCursorAdaptor extends CursorAdapter {
    public ProductCursorAdaptor(Context context, Cursor c) {
        super(context, c, 0);
    }

    /**
     *
     * @param context app context
     * @param cursor The cursor from which to get data.
     * @param parent The parent to which the new view is attached to.
     * @return the newly created list item view.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, null, false);
    }

    /**
     *
     * @param view Existing vew which returned early from newView() method.
     * @param context app context
     * @param cursor The cursor from whcih to get data.
     */
    @Override
    public void bindView(final View view, final Context context, Cursor cursor) {
        //Find individual views that we want to modify in the list item layout
        TextView textName = (TextView) view.findViewById(R.id.textView_name);
        TextView textPrice = (TextView) view.findViewById(R.id.textView_price);
        TextView textQuantity = (TextView) view.findViewById(R.id.textView_quantity);

        Button sales = (Button) view.findViewById(R.id.button_sale);

        //Find the columns of pet attributes
        int nameColumn = cursor.getColumnIndexOrThrow(ProductEntry.COLUMN_NAME);
        int priceColumn = cursor.getColumnIndexOrThrow(ProductEntry.COLUMN_PRICE);
        int quantityColumn = cursor.getColumnIndexOrThrow(ProductEntry.COLUMN_QUANTITY);
        int idColumn = cursor.getColumnIndexOrThrow(ProductEntry._ID);

        String name = cursor.getString(nameColumn);
        String price = cursor.getString(priceColumn);
        final String quantity = cursor.getString(quantityColumn);
        int productId = cursor.getInt(idColumn); //Use this to find current Content URI

        //Set the format for price
        NumberFormat dollarFormat = NumberFormat.getCurrencyInstance();
        price = dollarFormat.format(Double.parseDouble(price));

        textName.setText(name);
        textPrice.setText(price);
        textQuantity.setText(quantity);

        final Uri currentUri = ContentUris.withAppendedId(ProductEntry.CONTENT_URI, productId); //This URI will pass to ContentResolver to update the quantity.

        //Set click listener to button.
        sales.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View v) {
                int adjustQuantity = Integer.parseInt(quantity);

                //Set the logic for the quantity reduce
                if (adjustQuantity < 0){
                    adjustQuantity = 0;
                }
                else {
                    adjustQuantity--;
                }

                ContentValues values = new ContentValues();
                values.put(ProductEntry.COLUMN_QUANTITY, adjustQuantity);

                //Update the quantity in database via ContentResolver
                context.getContentResolver().update(currentUri, values, null, null);
            }
        });
    }
}
