package com.example.inventoryapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.inventoryapp.data.ProductContract.ProductEntry;
import com.example.inventoryapp.data.ProductCursorAdaptor;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class CatalogActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = CatalogActivity.class.toString();
    private static final int PRODUCT_LOADER_ID = 0;

    private FloatingActionButton fab;
    private ListView listView_product;
    private ProductCursorAdaptor mCursorAdapter;
    private TextView emptyView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);

        getViews();
        setClickFunctionToFloatingButton();
        setClickFunctionToListItem();

        listView_product.setEmptyView(emptyView);

        mCursorAdapter = new ProductCursorAdaptor(this, null); //Instantiate CursorAdapter.
        listView_product.setAdapter(mCursorAdapter);

        LoaderManager.getInstance(this).initLoader(PRODUCT_LOADER_ID, null, this);
    }

    private void getViews(){
        fab = (FloatingActionButton)findViewById(R.id.floatingActionButton);
        listView_product = (ListView)findViewById(R.id.listView_product);
        emptyView = (TextView)findViewById(R.id.emptyView);
    }

    private void setClickFunctionToFloatingButton(){
        fab.setOnClickListener(new FloatingActionButton.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });
    }

    private void setClickFunctionToListItem(){
        listView_product.setOnItemClickListener(new ListView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
                Uri uri = ContentUris.withAppendedId(ProductEntry.CONTENT_URI, id);
                intent.setData(uri);
                startActivity(intent);
            }
        });
    }

    //Create menu in the activity.
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_catalog, menu);
        return true;
    }

    //Set action when user click on the menu options.
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_delete_all:
                showDeleteConfirmationDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Show the dialog for user to confirm deletion of the product
     */
    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.delete_all_dialog_message));
        builder.setPositiveButton(getString(R.string.delete), new AlertDialog.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                int rowdeleted = getContentResolver().delete(ProductEntry.CONTENT_URI, null, null); //Delete all product once the user click "Delete".
                Log.v(TAG, "The number of row deleted = " + rowdeleted);
            }
        });
        builder.setNegativeButton(getString(R.string.cancel), new AlertDialog.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (dialog != null) {
                    dialog.dismiss(); //Close the dialog once the user click "Cancel".
                }
            }
        });

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
                ProductEntry.CONTENT_URI,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        //Update {@link PetCursorAdapter} with this new cursor containing updated pet data
        mCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        //Callback called when the data needs to be deleted
        mCursorAdapter.swapCursor(null);
    }
}
