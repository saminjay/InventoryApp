package com.example.root.inventoryapp;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
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

import com.example.root.inventoryapp.data.BooksContract.BooksEntry;

public class AddProductActivity extends AppCompatActivity {


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
        setContentView(R.layout.activity_add_product);

        productChanged = false;

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
        values.put(BooksEntry.COLUMN_PRODUCT_NAME, name);
        values.put(BooksEntry.COLUMN_PRICE, Float.parseFloat(price));
        values.put(BooksEntry.COLUMN_QUANTITY, Integer.parseInt(quantity));
        values.put(BooksEntry.COLUMN_SUPPLIER_NAME, supplierNameEditText.getText().toString());
        values.put(BooksEntry.COLUMN_SUPPLIER_EMAIL, supplierEmailEditText.getText().toString());
        values.put(BooksEntry.COLUMN_SUPPLIER_PHONE_NUMBER, supplierPhoneEditText.getText().toString());

        Uri uri = getContentResolver().insert(BooksEntry.CONTENT_URI, values);

        if (uri != null) {
            productChanged = false;
            Toast.makeText(this, "Product Saved!", Toast.LENGTH_SHORT).show();
        } else
            Toast.makeText(this, "Error Occurred.", Toast.LENGTH_SHORT).show();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_details, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        MenuItem item = menu.findItem(R.id.delete);
        item.setVisible(false);
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.done:
                saveBook();
                if (!productChanged)
                    NavUtils.navigateUpFromSameTask(this);
                break;
            case android.R.id.home:
                if (!productChanged) {
                    NavUtils.navigateUpFromSameTask(AddProductActivity.this);
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
                                NavUtils.navigateUpFromSameTask(AddProductActivity.this);
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
            NavUtils.navigateUpFromSameTask(this);
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
}
