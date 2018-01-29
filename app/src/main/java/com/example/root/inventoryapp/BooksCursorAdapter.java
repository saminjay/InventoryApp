package com.example.root.inventoryapp;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.root.inventoryapp.data.BooksContract.BooksEntry;

import java.text.MessageFormat;


public class BooksCursorAdapter extends CursorAdapter {

    BooksCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    @Override
    public void bindView(View view, final Context context, Cursor cursor) {

        TextView nameTextView = view.findViewById(R.id.name);
        TextView priceTextView = view.findViewById(R.id.price);
        TextView quantityTextView = view.findViewById(R.id.quantity);

        String name = cursor.getString(cursor.getColumnIndex(BooksEntry.COLUMN_PRODUCT_NAME));
        float price = cursor.getFloat(cursor.getColumnIndex(BooksEntry.COLUMN_PRICE));
        final int quantity = cursor.getInt(cursor.getColumnIndex(BooksEntry.COLUMN_QUANTITY));

        nameTextView.setText(name);
        priceTextView.setText(MessageFormat.format("Price: ${0}", price));
        quantityTextView.setText(MessageFormat.format("Quantity: {0}", quantity));

        int id = cursor.getInt(cursor.getColumnIndex(BooksEntry._ID));
        final Uri uri = ContentUris.withAppendedId(BooksEntry.CONTENT_URI, id);


        Button sale = view.findViewById(R.id.sale);
        sale.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (quantity > 0) {
                    int q = quantity;
                    q--;
                    ContentValues values = new ContentValues();
                    values.put(BooksEntry.COLUMN_QUANTITY, q);
                    int row = v.getContext().getContentResolver().update(uri, values, null, null);
                    context.getContentResolver().notifyChange(uri, null);
                } else {
                    Toast.makeText(context, "Not in stock", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
