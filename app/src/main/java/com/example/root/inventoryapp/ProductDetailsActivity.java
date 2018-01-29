package com.example.root.inventoryapp;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.root.inventoryapp.data.BooksContract;

public class ProductDetailsActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private TextView productNameTextView;
    private TextView priceTextView;
    private TextView supplierNameTextView;
    private TextView quantityTextView;
    private TextView supplierEmailTextView;
    private TextView supplierPhoneTextView;

    private int quantity;
    private Uri uri;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_details);

        uri = getIntent().getData();

        productNameTextView = findViewById(R.id.product_name);
        priceTextView = findViewById(R.id.price);
        quantityTextView = findViewById(R.id.quantity);
        supplierNameTextView = findViewById(R.id.supplier_name);
        supplierEmailTextView = findViewById(R.id.supplier_email);
        supplierPhoneTextView = findViewById(R.id.supplier_phone_number);

        getSupportLoaderManager().initLoader(0, null, this);

        Button orderButton = findViewById(R.id.order);
        orderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateBook();
                Intent intent;
                String email = supplierEmailTextView.getText().toString();
                String phone = supplierPhoneTextView.getText().toString();
                if (!TextUtils.isEmpty(email)) {
                    intent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", email, null));
                    String name = productNameTextView.getText().toString();
                    String price = priceTextView.getText().toString();
                    intent.putExtra(Intent.EXTRA_SUBJECT, "Product Order");
                    intent.putExtra(Intent.EXTRA_TEXT, name + "\n" + price);
                    startActivity(Intent.createChooser(intent, "Send email..."));
                } else if (!TextUtils.isEmpty(phone)) {
                    intent = new Intent(Intent.ACTION_DIAL);
                    intent.setData(Uri.parse("tel:" + phone));
                    startActivity(intent);
                } else {
                    Toast.makeText(getApplicationContext(), "Cannot make order without email address/phone number", Toast.LENGTH_SHORT).show();
                }
            }
        });

        Button decrease = findViewById(R.id.decrement);
        decrease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (quantity > 0) {
                    quantity--;
                    quantityTextView.setText(String.format("%d", quantity));
                } else {
                    Toast.makeText(getApplicationContext(), "Quantity cannot be negative", Toast.LENGTH_SHORT).show();
                }
            }
        });

        Button increase = findViewById(R.id.increment);
        increase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                quantity++;
                quantityTextView.setText(String.format("%d", quantity));
            }
        });
    }

    private void updateBook() {
        ContentValues values = new ContentValues();
        values.put(BooksContract.BooksEntry.COLUMN_QUANTITY, quantity);

        int row = getContentResolver().update(uri, values, null, null);
        if (row == 0) {
            // If no rows were affected, then there was an error with the update.
            Toast.makeText(this, "No Update.",
                    Toast.LENGTH_SHORT).show();
        } else {
            // Otherwise, the update was successful and we can display a toast.
            Toast.makeText(this, "Product Saved!",
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_details, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        MenuItem edit = menu.findItem(R.id.delete);
        edit.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                updateBook();
                finish();
                break;
            case R.id.done:
                updateBook();
                finish();
                break;
            case R.id.delete:
                showDeleteConfirmationDialog();
                break;
            case R.id.edit:
                Intent intent = new Intent(this, EditProductActivity.class);
                intent.setData(uri);
                startActivity(intent);
                break;
        }
        return true;
    }

    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("You sure you wanna delete?");
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the Book.
                deleteBook();
            }
        });
        builder.setNegativeButton("no", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the Book.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void deleteBook() {
        // Call the ContentResolver to delete the Book at the given content URI.
        // content URI already identifies the Book that we want.
        int rowsDeleted = getContentResolver().delete(uri, null, null);

        // Show a toast message depending on whether or not the delete was successful.
        if (rowsDeleted == 0) {
            // If no rows were deleted, then there was an error with the delete.
            Toast.makeText(this, "Error Occurred",
                    Toast.LENGTH_SHORT).show();
        } else {
            // Otherwise, the delete was successful and we can display a toast.
            Toast.makeText(this, "Product Deleted",
                    Toast.LENGTH_SHORT).show();
        }
        finish();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this, uri, null, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data.moveToFirst()) {
            String name = data.getString(data.getColumnIndex(BooksContract.BooksEntry.COLUMN_PRODUCT_NAME));
            float price = data.getFloat(data.getColumnIndex(BooksContract.BooksEntry.COLUMN_PRICE));
            quantity = data.getInt(data.getColumnIndex(BooksContract.BooksEntry.COLUMN_QUANTITY));
            String supplierName = data.getString(data.getColumnIndex(BooksContract.BooksEntry.COLUMN_SUPPLIER_NAME));
            String supplierEmail = data.getString(data.getColumnIndex(BooksContract.BooksEntry.COLUMN_SUPPLIER_EMAIL));
            String supplierPhone = data.getString(data.getColumnIndex(BooksContract.BooksEntry.COLUMN_SUPPLIER_PHONE_NUMBER));

            productNameTextView.setText(name);
            priceTextView.setText(String.format("%f", price));
            quantityTextView.setText(String.format("%d", quantity));
            supplierNameTextView.setText(supplierName);
            supplierEmailTextView.setText(supplierEmail);
            supplierPhoneTextView.setText(supplierPhone);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        productNameTextView.setText("");
        priceTextView.setText("");
        quantityTextView.setText("");
        supplierNameTextView.setText("");
        supplierEmailTextView.setText("");
        supplierPhoneTextView.setText("");
    }

}
