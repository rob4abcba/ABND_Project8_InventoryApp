package com.example.android.abnd_project8_inventoryapp;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.Toast;


import com.example.android.abnd_project8_inventoryapp.data.InventoryContract.InventoryEntry;
import com.example.android.abnd_project8_inventoryapp.data.InventoryDBHelper;

public class MainActivity extends AppCompatActivity implements LoaderManager
        .LoaderCallbacks<Cursor>{

    private static final int INVENTORY_LOADER = 0;

    InventoryCursorAdapter inventoryCursorAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // When they click on the Floating Action button from the list, we want to start
        // our Editor intent.  This allows them to add an item to inventory.
        FloatingActionButton addFab = findViewById(R.id.fab);
        addFab.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent detailIntent = new Intent(MainActivity.this,EditActivity.class);
                startActivity(detailIntent);
            }
        });

        ListView inventoryListView = findViewById(R.id.inventory_list);

        View emptyListView = findViewById(R.id.empty_view);

        inventoryListView.setEmptyView(emptyListView);

        inventoryCursorAdapter = new InventoryCursorAdapter(this, null);

        inventoryListView.setAdapter(inventoryCursorAdapter);

        inventoryListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Intent editorIntent = new Intent(MainActivity.this, DetailActivity.class);
                Uri currentItemUri = ContentUris.withAppendedId(InventoryEntry.CONTENT_URI, id);

                editorIntent.setData(currentItemUri);
                startActivity(editorIntent);
            }
        });


        getLoaderManager().initLoader(INVENTORY_LOADER, null, this);
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        String[] inventoryProjection = {
            InventoryEntry._ID,
            InventoryEntry.COLUMN_PRODUCT_NAME,
            InventoryEntry.COLUMN_PRODUCT_QUANTITY,
            InventoryEntry.COLUMN_PRODUCT_PRICE  };


        return new CursorLoader(this,
                InventoryEntry.CONTENT_URI,
                inventoryProjection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        inventoryCursorAdapter.swapCursor(data);

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

        inventoryCursorAdapter.swapCursor(null);

    }

}
