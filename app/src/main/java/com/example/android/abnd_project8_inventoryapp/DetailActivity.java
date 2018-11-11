package com.example.android.abnd_project8_inventoryapp;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.abnd_project8_inventoryapp.data.InventoryContract.InventoryEntry;

public class DetailActivity extends AppCompatActivity implements LoaderManager
        .LoaderCallbacks<Cursor> {

    private static final int INVENTORY_LOADER_DETAIL = 0;
    private static final String ADD_INVENTORY = "ADD";
    private static final String SUB_INVENTORY = "SUBTRACT";

    private Uri currentItemUri;
    private TextView detailProductNameTextView;
    private TextView detailProductPriceTextView;
    private TextView detailQuantityTextView;
    private TextView detailSupplierNameTextView;
    private TextView detailSupplierPhoneTextView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        final Intent intent = getIntent();
        currentItemUri = intent.getData();
        Button detailAddInventoryButton;
        Button detailSubtractInventoryButton;


        if (currentItemUri != null) {

            getLoaderManager().initLoader(INVENTORY_LOADER_DETAIL, null, this);

        }

        detailProductNameTextView = findViewById(R.id.detail_product_name);
        detailProductPriceTextView = findViewById(R.id.detail_product_price);
        detailQuantityTextView = findViewById(R.id.detail_quantity);
        detailSupplierNameTextView = findViewById(R.id.detail_supplier_name);
        detailSupplierPhoneTextView = findViewById(R.id.detail_supplier_phone);
        detailAddInventoryButton = findViewById(R.id.detail_add_quantity);
        detailSubtractInventoryButton = findViewById(R.id.detail_subtract_quantity);
        FloatingActionButton editFab = findViewById(R.id.detail_editFab);
        ImageButton callSupplierButton = findViewById(R.id.call_button);

        // When they click on the Edit Floating Action button, we want to start
        // our Editor intent.  This allows them to Edit the item shown.
        editFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent editIntent = new Intent(DetailActivity.this, EditActivity.class);

                editIntent.setData(currentItemUri);
                startActivity(editIntent);
            }
        });

        // This listener is triggered when the + or - button is pressed to increase or
        // decrease the quantity
        View.OnClickListener inventoryButtonListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.detail_add_quantity:
                        AdjustInventory(ADD_INVENTORY);
                        break;
                    case R.id.detail_subtract_quantity:
                        AdjustInventory(SUB_INVENTORY);
                        break;
                }
            }
        };

        detailSubtractInventoryButton.setOnClickListener(inventoryButtonListener);
        detailAddInventoryButton.setOnClickListener(inventoryButtonListener);

        // This listener is triggered when the phone button is pressed to call the supplier
        View.OnClickListener callSupplierButtonListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Code below based on https://stackoverflow.com/questions/4275678/how-to-make-a-phone-call-using-intent-in-android
                String supplierPhone = detailSupplierPhoneTextView.getText().toString().trim();
                String phoneUri = "tel:";
                if (!(TextUtils.isEmpty(supplierPhone))) {
                    phoneUri = phoneUri + supplierPhone.trim();
                    Intent phoneIntent = new Intent(Intent.ACTION_DIAL);
                    phoneIntent.setData(Uri.parse(phoneUri));
                    startActivity(phoneIntent);
                }
            }
        };
        callSupplierButton.setOnClickListener(callSupplierButtonListener);
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] inventoryProjection = {
                InventoryEntry.COLUMN_PRODUCT_NAME,
                InventoryEntry.COLUMN_PRODUCT_PRICE,
                InventoryEntry.COLUMN_PRODUCT_QUANTITY,
                InventoryEntry.COLUMN_PRODUCT_SUPPLIER,
                InventoryEntry.COLUMN_PRODUCT_SUPPLIER_PHONE
        };

        return new CursorLoader(this,
                currentItemUri,
                inventoryProjection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        if (data == null || data.getCount() < 1) {
            return;
        }

        if (data.moveToFirst()) {
            int productNameColIndex = data.getColumnIndexOrThrow(InventoryEntry
                    .COLUMN_PRODUCT_NAME);
            int productPriceColIndex = data.getColumnIndexOrThrow(InventoryEntry
                    .COLUMN_PRODUCT_PRICE);
            int productQuantityColIndex = data.getColumnIndexOrThrow(InventoryEntry
                    .COLUMN_PRODUCT_QUANTITY);
            int productSupplierNameColIndex = data.getColumnIndexOrThrow(InventoryEntry
                    .COLUMN_PRODUCT_SUPPLIER);
            int productSupplierPhoneColIndex = data.getColumnIndexOrThrow(InventoryEntry
                    .COLUMN_PRODUCT_SUPPLIER_PHONE);

            String productName = data.getString(productNameColIndex);
            int quantity = data.getInt(productQuantityColIndex);
            Float productPrice = data.getFloat(productPriceColIndex);
            String supplierName = data.getString(productSupplierNameColIndex);
            String supplierPhone = data.getString(productSupplierPhoneColIndex);

            detailProductNameTextView.setText(productName);
            detailProductPriceTextView.setText(Float.toString(productPrice));
            detailQuantityTextView.setText(Integer.toString(quantity));
            detailSupplierNameTextView.setText(supplierName);
            detailSupplierPhoneTextView.setText(supplierPhone);
        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.detail_menu, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_delete:
                showDeleteConfirmationDialog();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.Yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Yes" button
                deleteItem();
            }
        });

        builder.setNegativeButton(R.string.No, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "No" button, so dismiss the dialog
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void deleteItem() {
        // Only perform the delete if this is an existing product
        if (currentItemUri != null) {

            int rowsDeleted = getContentResolver().delete(currentItemUri, null, null);

            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this, getString(R.string.edit_delete_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.edit_delete_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }

        // Close the activity
        finish();
    }

    private void AdjustInventory(String action) {

        int currentQuantity;

        currentQuantity = Integer.parseInt(detailQuantityTextView.getText().toString());

        switch (action) {
            case ADD_INVENTORY:
                currentQuantity += 1;
                break;
            case SUB_INVENTORY:
                currentQuantity -= 1;
                break;
        }

        if (currentQuantity < 0) {
            Toast.makeText(this, R.string.quanity_less_than_zero, Toast.LENGTH_SHORT).show();
            currentQuantity = 0;
        }

        // Update the quantity in the table
        ContentValues itemValues = new ContentValues();
        itemValues.put(InventoryEntry.COLUMN_PRODUCT_QUANTITY, currentQuantity);

        int rowsAffected = getContentResolver().update(currentItemUri, itemValues, null, null);

        // If the update wasn't successful, then we don't want to change the value shown.  If the
        // update was successful, then set the value in the TextView to the current quantity.
        if (rowsAffected == 0) {
            Toast.makeText(this, getString(R.string.edit_update_failed),
                    Toast.LENGTH_SHORT).show();
        } else {
            detailQuantityTextView.setText(Integer.toString(currentQuantity));
            Toast.makeText(this, getString(R.string.edit_update_successful),
                    Toast.LENGTH_SHORT).show();
        }

    }

}
