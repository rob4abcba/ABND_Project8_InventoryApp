package com.example.android.abnd_project8_inventoryapp;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.abnd_project8_inventoryapp.data.InventoryContract.InventoryEntry;

public class InventoryCursorAdapter extends CursorAdapter {

    public InventoryCursorAdapter(Context context, Cursor c) {
        super(context, c);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.inventory_list_item,parent,false);
    }

    @Override
    public void bindView(final View view, final Context context, final Cursor cursor) {
        TextView productNameTextView = view.findViewById(R.id.list_item_product_name);
        final TextView quantityTextView = view.findViewById(R.id.list_item_quantity);
        TextView productPriceTextView = view.findViewById(R.id.list_item_product_price);

        int productNameColIndex = cursor.getColumnIndexOrThrow(InventoryEntry.COLUMN_PRODUCT_NAME);
        int quantityColIndex = cursor.getColumnIndexOrThrow(InventoryEntry.COLUMN_PRODUCT_QUANTITY);
        int productPriceColIndex = cursor.getColumnIndexOrThrow(InventoryEntry
                .COLUMN_PRODUCT_PRICE);


        String productName = cursor.getString(productNameColIndex);
        final String quantity = String.valueOf(cursor.getInt(quantityColIndex));
        String productPrice = String.valueOf(cursor.getFloat(productPriceColIndex));

        productNameTextView.setText(productName);
        quantityTextView.setText(quantity);
        productPriceTextView.setText(productPrice);

        Button saleButton = view.findViewById(R.id.list_item_saleButton);
        final int cursorPosition = cursor.getPosition();

        saleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Code below based on https://stackoverflow.com/questions/43479897/how-to-update-rows-in-sqlitedbandroid-inside-a-cursor-adapter-on-button-click
                ContentValues itemValues = new ContentValues();
                cursor.moveToPosition(cursorPosition);
                int idColumnIndex = cursor.getColumnIndexOrThrow(InventoryEntry._ID);
                int id = cursor.getInt(idColumnIndex);
                int currentQuantity;
                currentQuantity = Integer.parseInt(quantity);
                currentQuantity -= 1;
                if (currentQuantity < 0) {
                    Toast.makeText(context, R.string.quanity_less_than_zero, Toast
                            .LENGTH_SHORT)
                            .show();
                    currentQuantity = 0;
                }
                itemValues.put(InventoryEntry.COLUMN_PRODUCT_QUANTITY, currentQuantity);

                Uri currentItemUri = ContentUris.withAppendedId(InventoryEntry.CONTENT_URI, id);
                int rowsAffected = context.getContentResolver().update(currentItemUri, itemValues, null,
                        null);

                // If the update wasn't successful, then we don't want to change the value shown.  If the
                // update was successful, then set the value in the TextView to the current quantity.
                if (rowsAffected > 0) {
                    quantityTextView.setText(Integer.toString(currentQuantity));
                }


            }
        });

    }



}
