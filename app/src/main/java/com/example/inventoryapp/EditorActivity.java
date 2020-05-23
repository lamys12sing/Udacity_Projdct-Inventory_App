package com.example.inventoryapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.inventoryapp.data.ProductContract.ProductEntry;

public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = EditorActivity.class.toString();
    private static final int PRODUCT_LOADER_ID = 0;

    private EditText editText_product, editText_price, editText_quantity;
    private Button button_order;
    private ImageButton imageButton_add, imageButton_less;
    private Uri mCurrentProductUri; //Content URI for the existing product (null if it's a new product)
    private int mQuantity = 0;

    //Check if the product has changed by user
    private boolean mProductHasChange = false;
    private View.OnTouchListener mOnTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            mProductHasChange = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        Intent intent = getIntent();
        mCurrentProductUri = intent.getData();
        if (mCurrentProductUri == null){
            setTitle(getString(R.string.add_a_product));
            invalidateOptionsMenu(); //Call this method in order to hide delete button.
        }
        else {
            setTitle(getString(R.string.edit_product));
            LoaderManager.getInstance(this).initLoader(PRODUCT_LOADER_ID, null, this);
        }

        getViews();
        setOnTouchListenerToEditText();
        setOnClickListenerToButton();
    }

    private void getViews(){
        editText_product = (EditText)findViewById(R.id.editText_product);
        editText_price = (EditText)findViewById(R.id.editText_price);
        editText_quantity = (EditText)findViewById(R.id.editText_quantity);

        imageButton_add = (ImageButton) findViewById(R.id.imageButton_add);
        imageButton_less = (ImageButton) findViewById(R.id.imageButton_less);

        button_order = (Button)findViewById(R.id.button_order);
    }

    private void setOnTouchListenerToEditText(){
        editText_product.setOnTouchListener(mOnTouchListener);
        editText_price.setOnTouchListener(mOnTouchListener);
        editText_quantity.setOnTouchListener(mOnTouchListener);
    }

    private void setOnClickListenerToButton(){
        imageButton_add.setOnClickListener(adjustQuantity);
        imageButton_less.setOnClickListener(adjustQuantity);

        button_order.setOnClickListener(makeCallToSupplierFormMakingOrder);
    }

    private ImageButton.OnClickListener adjustQuantity = new ImageButton.OnClickListener(){
        @Override
        public void onClick(View v) {
            String stringQuantity = editText_quantity.getText().toString(); //Get the quantity from quantity field.
            if (!TextUtils.isEmpty(stringQuantity)){ //Check if quantity field is inputted
                mQuantity = Integer.parseInt(stringQuantity);
            }

            switch (v.getId()){
                case R.id.imageButton_add:
                    mQuantity++;
                    editText_quantity.setText(String.valueOf(mQuantity));
                    break;

                case R.id.imageButton_less:
                    mQuantity--;
                    if (mQuantity < 0){
                        mQuantity = 0;
                    }
                    editText_quantity.setText(String.valueOf(mQuantity));
                    break;

                default:
                    Log.e(TAG, "Error with the quantity adjustment.");
            }
        }
    };

    private Button.OnClickListener makeCallToSupplierFormMakingOrder = new Button.OnClickListener(){
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(Intent.ACTION_CALL_BUTTON);
            startActivity(intent);
        }
    };

    //Create menu in the activity.
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.men_editor, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (mCurrentProductUri == null){
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false); //Hide the delete button.
        }
        return true;
    }

    //Set action when user click on the menu options.
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_save: //add save product function
                saveProduct();
                return true;

            case R.id.action_delete: //add delete product function
                showDeleteConfirmationDialog();
                return true;

            case android.R.id.home: //This is the up button
                if (!mProductHasChange){
                    NavUtils.navigateUpFromSameTask(EditorActivity.this); //Go to home page if user change nothing.
                    return true;
                }
                DialogInterface.OnClickListener discardButtonClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        NavUtils.navigateUpFromSameTask(EditorActivity.this); //Go to home page if user click "Discard" button.
                    }
                };
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Helper method to save product
     */
    private void saveProduct(){
        // Get values from the edit field in the activity.
        String name = editText_product.getText().toString();
        String stringPrice = editText_price.getText().toString();
        String stringQuantity = editText_quantity.getText().toString();

        //Check if the name is blank when adding a new product
        if (mCurrentProductUri == null &&
                TextUtils.isEmpty(name)){
            return;
        }

        //Initiate ContentValues and add values to it.
        ContentValues values = new ContentValues();
        values.put(ProductEntry.COLUMN_NAME, name);

        //Check whether the price is inputted by the user. If not, assign 0.00 for the value.
        double price = 0.00;
        if (!TextUtils.isEmpty(stringPrice)){
            price = Double.parseDouble(stringPrice);
        }
        values.put(ProductEntry.COLUMN_PRICE, price);

        //Check whether the quantity is inputted by the user. If not, assign 0 for the value.
        mQuantity = 0;
        if (!TextUtils.isEmpty(stringQuantity)){
            mQuantity = Integer.parseInt(stringQuantity);
        }
        values.put(ProductEntry.COLUMN_QUANTITY, mQuantity);

        //Check the uri for how the user go to this activity.
        if (mCurrentProductUri == null){
            //Save new product by using insert() method
            Uri newUri = getContentResolver().insert(ProductEntry.CONTENT_URI, values);

            //Check if the insert is successful.
            if (newUri == null){
                Toast.makeText(this, getString(R.string.save_error), Toast.LENGTH_SHORT).show();
            }
            else {
                Toast.makeText(this, getString(R.string.save_success), Toast.LENGTH_SHORT).show();
            }
        }
        else {
            //Update product by using update() method
            int rowAffected = getContentResolver().update(mCurrentProductUri, values, null, null);

            //Check if the update is successful.
            if (rowAffected == 0){
                Toast.makeText(this, getString(R.string.save_error), Toast.LENGTH_SHORT).show();
            }
            else {
                Toast.makeText(this, getString(R.string.save_success), Toast.LENGTH_SHORT).show();
            }
        }
        finish(); //close the activity once the save is done.
    }

    /**
     * Show the dialog for user to confirm deletion of the product
     */
    private void showDeleteConfirmationDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.delete_dialog_message));
        builder.setPositiveButton(getString(R.string.delete), new AlertDialog.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteProduct(); //Delete the product once the user click "Delete".
            }
        });
        builder.setNegativeButton(getString(R.string.cancel), new AlertDialog.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (dialog != null){
                    dialog.dismiss(); //Close the dialog once the user click "Cancel".
                }
            }
        });

        //Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Helper method to delete product
     */
    private void deleteProduct(){
        //Only perform the delete if this is an existing product
        if (mCurrentProductUri != null){
            int rowDeleted = getContentResolver().delete(mCurrentProductUri, null, null);
            //Check whether the delete is successful.
            if (rowDeleted == 0){
                Toast.makeText(this, getString(R.string.delete_error), Toast.LENGTH_SHORT).show();
            }
            else {
                Toast.makeText(this, getString(R.string.delete_success), Toast.LENGTH_SHORT).show();
            }
        }
        finish(); //close the activity once the delete is done.
    }

    /**
     * Hook up the showUnsavedChangesDialog() method with back press.
     */
    @Override
    public void onBackPressed() {
        // If the pet hasn't changed, continue with handling back button press
        if (!mProductHasChange){
            super.onBackPressed();
            return;
        }

        AlertDialog.OnClickListener discardButtonClickListener = new AlertDialog.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Close the activity when the user click "Discard" button.
                finish();
            }
        };
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    /**
     * Method that show dialog if the user change something and leave
     *
     * @param discardButtonClickListener is the click listener for what to do when
     *                                   the user confirms they want to discard their changes
     */
    private void showUnsavedChangesDialog(DialogInterface.OnClickListener discardButtonClickListener){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.unsaved_change_dialog_message));
        builder.setPositiveButton(getString(R.string.discard), discardButtonClickListener);
        builder.setNegativeButton(getString(R.string.keep_editing), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (dialog != null){
                    dialog.dismiss();
                }
            }
        });

        //Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        String[] projection = new String[]{
                ProductEntry._ID,
                ProductEntry.COLUMN_NAME,
                ProductEntry.COLUMN_PRICE,
                ProductEntry.COLUMN_QUANTITY};

        return new CursorLoader(this,
                mCurrentProductUri,        // Current product
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        // Return early if the cursor have nothing or less then 1 record.
        if (data == null || data.getCount() < 1){
            return;
        }

        while (data.moveToNext()){
            int nameColumnIndex = data.getColumnIndexOrThrow(ProductEntry.COLUMN_NAME);
            int namePriceIndex = data.getColumnIndexOrThrow(ProductEntry.COLUMN_PRICE);
            int nameQuantityIndex = data.getColumnIndexOrThrow(ProductEntry.COLUMN_QUANTITY);

            String name = data.getString(nameColumnIndex);
            double price = data.getDouble(namePriceIndex);
            mQuantity = data.getInt(nameQuantityIndex);

            editText_product.setText(name);
            editText_price.setText(String.valueOf(price));
            editText_quantity.setText(String.valueOf(mQuantity));
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {

    }
}
