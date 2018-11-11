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
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.android.abnd_project8_inventoryapp.data.InventoryContract.InventoryEntry;

public class EditActivity extends AppCompatActivity implements LoaderManager
        .LoaderCallbacks<Cursor> {


    private static final int INVENTORY_LOADER_DETAIL = 0;

    private Uri currentItemUri;
    private EditText editProductNameEditText;
    private EditText editProductPriceEditText;
    private EditText editQuantityEditText;
    private EditText editSupplierNameEditText;
    private EditText editSupplierPhoneEditText;

    private boolean itemChanged = false;
    private View.OnTouchListener touchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            itemChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);
        Intent intent = getIntent();
        currentItemUri = intent.getData();

        if (currentItemUri == null) {
            // New item so set the title to "Add Item"
            setTitle(getString(R.string.edit_activity_title_new));

            invalidateOptionsMenu();

        } else {

            // New item so set the title to "Edit Item"
            setTitle(getString(R.string.edit_activity_title_existing));

            getLoaderManager().initLoader(INVENTORY_LOADER_DETAIL, null, this);

        }

        editProductNameEditText = findViewById(R.id.product_name_edit);
        editProductPriceEditText = findViewById(R.id.product_price_edit);
        editQuantityEditText = findViewById(R.id.quantity_edit);
        editSupplierNameEditText = findViewById(R.id.supplier_name_edit);
        editSupplierPhoneEditText = findViewById(R.id.supplier_phone_edit);

        editProductNameEditText.setOnTouchListener(touchListener);
        editProductPriceEditText.setOnTouchListener(touchListener);
        editQuantityEditText.setOnTouchListener(touchListener);
        editSupplierNameEditText.setOnTouchListener(touchListener);
        editSupplierPhoneEditText.setOnTouchListener(touchListener);


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

            editProductNameEditText.setText(productName);
            editProductPriceEditText.setText(Float.toString(productPrice));
            editQuantityEditText.setText(Integer.toString(quantity));
            editSupplierNameEditText.setText(supplierName);
            editSupplierPhoneEditText.setText(supplierPhone);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

        editProductNameEditText.setText("");
        editProductPriceEditText.setText("0");
        editQuantityEditText.setText("0");
        editSupplierNameEditText.setText("");
        editSupplierPhoneEditText.setText("");


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.editor_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        if (currentItemUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {


        switch (item.getItemId()) {
            case R.id.action_save:
                if (saveProduct()) {
                    finish();
                }
                return true;
            case R.id.action_delete:
                showDeleteConfirmationDialog();
                return true;

            case android.R.id.home:

                if (!itemChanged) {
                    if (currentItemUri != null) {
                        Intent detailIntent = new Intent(EditActivity.this, DetailActivity
                                .class);
                        detailIntent.setData(currentItemUri);
                        startActivity(detailIntent);
                    } else {
                        NavUtils.navigateUpFromSameTask(EditActivity.this);
                    }

                    return true;
                }

                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Yes" button, navigate to parent activity.
                                if (currentItemUri != null) {
                                    Intent detailIntent = new Intent(EditActivity.this, DetailActivity
                                            .class);
                                    detailIntent.setData(currentItemUri);
                                    startActivity(detailIntent);
                                } else {
                                    NavUtils.navigateUpFromSameTask(EditActivity.this);
                                }
                            }
                        };

                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private boolean saveProduct() {

        String productName = editProductNameEditText.getText().toString().trim();
        String productPrice = editProductPriceEditText.getText().toString().trim();
        String productQuantity = editQuantityEditText.getText().toString().trim();
        String supplierName = editSupplierNameEditText.getText().toString().trim();
        String supplierPhone = editSupplierPhoneEditText.getText().toString().trim();

        if (currentItemUri == null &&
                TextUtils.isEmpty(productName) &&
                TextUtils.isEmpty(productPrice) &&
                TextUtils.isEmpty(productQuantity) &&
                TextUtils.isEmpty(supplierName) &&
                TextUtils.isEmpty(supplierPhone)) {
            Toast.makeText(this, getString(R.string.no_values),
                    Toast.LENGTH_SHORT).show();
            return false;
        }

        if (TextUtils.isEmpty(productName)) {
            Toast.makeText(this, getString(R.string.productName_missing),
                    Toast.LENGTH_SHORT).show();
            return false;
        }

        if (TextUtils.isEmpty(productPrice)) {
            Toast.makeText(this, getString(R.string.productPrice_missing),
                    Toast.LENGTH_SHORT).show();
            return false;
        }

        if (TextUtils.isEmpty(productQuantity)) {
            Toast.makeText(this, getString(R.string.productQty_missing),
                    Toast.LENGTH_SHORT).show();
            return false;
        }

        if (TextUtils.isEmpty(supplierName)) {
            Toast.makeText(this, getString(R.string.productSupplier_missing),
                    Toast.LENGTH_SHORT).show();
            return false;
        }

        if (TextUtils.isEmpty(supplierPhone)) {
            Toast.makeText(this, getString(R.string.productSupplierPhone_missing),
                    Toast.LENGTH_SHORT).show();
            return false;
        }

        ContentValues itemValues = new ContentValues();
        itemValues.put(InventoryEntry.COLUMN_PRODUCT_NAME, productName);
        itemValues.put(InventoryEntry.COLUMN_PRODUCT_SUPPLIER, supplierName);
        itemValues.put(InventoryEntry.COLUMN_PRODUCT_SUPPLIER_PHONE, supplierPhone);


        float productPriceFloat = 0;
        if (!TextUtils.isEmpty(productPrice)) {
            productPriceFloat = Float.parseFloat(productPrice);
        }
        itemValues.put(InventoryEntry.COLUMN_PRODUCT_PRICE, productPriceFloat);

        int quantityInt = 0;
        if (!TextUtils.isEmpty(productQuantity)) {
            quantityInt = Integer.parseInt(productQuantity);
        }
        itemValues.put(InventoryEntry.COLUMN_PRODUCT_QUANTITY, quantityInt);

        // New item so do an insert
        if (currentItemUri == null) {
            Uri newItemUri = getContentResolver().insert(InventoryEntry.CONTENT_URI, itemValues);

            if (newItemUri == null) {
                Toast.makeText(this, getString(R.string.edit_insert_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.edit_insert_successful),
                        Toast.LENGTH_SHORT).show();

            }
            // Existing item so do an update
        } else {
            int rowsAffected = getContentResolver().update(currentItemUri, itemValues, null, null);

            if (rowsAffected == 0) {
                Toast.makeText(this, getString(R.string.edit_update_failed),
                        Toast.LENGTH_SHORT).show();
                return true;
            } else {
                Toast.makeText(this, getString(R.string.edit_update_successful),
                        Toast.LENGTH_SHORT).show();
                return true;
            }
        }
        // If we are at this point, we simply want to return the user to the previous
        // activity so we're going to return true regardless of if the add or update
        // was successful.
        return true;
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
                Intent mainActivityIntent = new Intent(EditActivity.this, MainActivity.class);
                NavUtils.navigateUpTo(EditActivity.this, mainActivityIntent);
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

    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the Yes and No buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_msg);
        builder.setPositiveButton(R.string.Yes, discardButtonClickListener);
        builder.setNegativeButton(R.string.No, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "No" button, so dismiss the dialog
                // and continue editing the item.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

}
