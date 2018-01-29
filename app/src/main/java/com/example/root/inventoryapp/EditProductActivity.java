package com.example.root.inventoryapp;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NavUtils;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.root.inventoryapp.data.BooksContract;

public class EditProductActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private Uri uri;

    private EditText productNameEditText;
    private EditText priceEditText;
    private EditText supplierNameEditText;
    private TextView quantityTextView;
    private EditText supplierEmailEditText;
    private EditText supplierPhoneEditText;
    private int quantity;

    private boolean productChanged;

    private View.OnTouchListener touchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            productChanged = true;
            view.performClick();
            return false;
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_product);

        productChanged = false;
        uri = getIntent().getData();
        assert uri != null;
        getSupportLoaderManager().initLoader(0, null, this);


        productNameEditText = findViewById(R.id.product_name);
        priceEditText = findViewById(R.id.price);
        quantityTextView = findViewById(R.id.quantity);
        supplierNameEditText = findViewById(R.id.supplier_name);
        supplierEmailEditText = findViewById(R.id.supplier_email);
        supplierPhoneEditText = findViewById(R.id.supplier_phone_number);

        productNameEditText.setOnTouchListener(touchListener);
        priceEditText.setOnTouchListener(touchListener);
        supplierNameEditText.setOnTouchListener(touchListener);
        supplierEmailEditText.setOnTouchListener(touchListener);
        supplierPhoneEditText.setOnTouchListener(touchListener);

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
                productChanged = true;
            }
        });

        Button increase = findViewById(R.id.increment);
        increase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                quantity++;
                quantityTextView.setText(String.format("%d", quantity));
                productChanged = true;
            }
        });
    }

    private void saveBook() {
        productChanged = true;
        ContentValues values = new ContentValues();
        String name = productNameEditText.getText().toString();
        String price = priceEditText.getText().toString();
        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(price)) {
            Toast.makeText(getApplicationContext(), "Name and Price cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }
        String quantity = quantityTextView.getText().toString();
        if (TextUtils.isEmpty(quantity))
            quantity = "0";
        values.put(BooksContract.BooksEntry.COLUMN_PRODUCT_NAME, name);
        values.put(BooksContract.BooksEntry.COLUMN_PRICE, Float.parseFloat(price));
        values.put(BooksContract.BooksEntry.COLUMN_QUANTITY, Integer.parseInt(quantity));
        values.put(BooksContract.BooksEntry.COLUMN_SUPPLIER_NAME, supplierNameEditText.getText().toString());
        values.put(BooksContract.BooksEntry.COLUMN_SUPPLIER_EMAIL, supplierEmailEditText.getText().toString());
        values.put(BooksContract.BooksEntry.COLUMN_SUPPLIER_PHONE_NUMBER, supplierPhoneEditText.getText().toString());


        int row = getContentResolver().update(uri, values, null, null);
        if (row == 0) {
            // If no rows were affected, then there was an error with the update.
            Toast.makeText(this, "No Update.",
                    Toast.LENGTH_SHORT).show();
        } else {
            // Otherwise, the update was successful and we can display a toast.
            Toast.makeText(this, "Product Saved!",
                    Toast.LENGTH_SHORT).show();
            productChanged = false;
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

        MenuItem edit = menu.findItem(R.id.edit);
        edit.setVisible(false);
        return true;
    }

    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Products are not saved");
        builder.setPositiveButton("Discard", discardButtonClickListener);
        builder.setNegativeButton("Keep Editing", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
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
        NavUtils.navigateUpFromSameTask(this);


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.done:
                saveBook();
                if (!productChanged)
                    NavUtils.navigateUpFromSameTask(this);
                break;
            case R.id.delete:
                showDeleteConfirmationDialog();
                break;
            case android.R.id.home:
                if (!productChanged) {
                    NavUtils.navigateUpFromSameTask(EditProductActivity.this);
                    return true;
                }

                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(EditProductActivity.this);
                            }
                        };

                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        if (!productChanged) {
            finish();
            return;
        }
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
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

            productNameEditText.setText(name);
            priceEditText.setText(String.format("%f", price));
            quantityTextView.setText(String.format("%d", quantity));
            supplierNameEditText.setText(supplierName);
            supplierEmailEditText.setText(supplierEmail);
            supplierPhoneEditText.setText(supplierPhone);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        productNameEditText.setText("");
        priceEditText.setText("");
        quantityTextView.setText("");
        supplierNameEditText.setText("");
        supplierEmailEditText.setText("");
        supplierPhoneEditText.setText("");
    }

}
