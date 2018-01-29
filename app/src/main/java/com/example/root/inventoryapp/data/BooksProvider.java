package com.example.root.inventoryapp.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.root.inventoryapp.data.BooksContract.BooksEntry;


public class BooksProvider extends ContentProvider {

    private static final int BOOKS = 0;
    private static final int BOOKS_ID = 1;
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sUriMatcher.addURI(BooksContract.CONTENT_AUTHORITY, BooksContract.CONTENT_PATH, BOOKS);
        sUriMatcher.addURI(BooksContract.CONTENT_AUTHORITY, BooksContract.CONTENT_PATH + "/#", BOOKS_ID);
    }

    private BooksDbHelper dbHelper;

    @Override
    public boolean onCreate() {
        dbHelper = new BooksDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor;
        switch (sUriMatcher.match(uri)) {
            case BOOKS:
                cursor = db.query(BooksEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case BOOKS_ID:
                selection = BooksEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                cursor = db.query(BooksEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot Query unknown uri : " + uri);
        }
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        int match = sUriMatcher.match(uri);
        switch (match) {
            case BOOKS:
                return BooksEntry.CONTENT_LIST_TYPE;
            case BOOKS_ID:
                return BooksEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalArgumentException("Unknown uri " + uri + "with match " + match);
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {

        int match = sUriMatcher.match(uri);
        switch (match) {
            case BOOKS:
                return insertRow(uri, values);
            default:
                throw new IllegalArgumentException("Can't insert row for uri: " + uri);
        }
    }

    private Uri insertRow(Uri uri, ContentValues values) {

        String name = values.getAsString(BooksEntry.COLUMN_PRODUCT_NAME);
        if (name == null)
            throw new IllegalArgumentException("Product Requires a name.");

        Integer price = values.getAsInteger(BooksEntry.COLUMN_PRICE);
        if (price == null || price < 0)
            throw new IllegalArgumentException("Price can't be null or negative");

        int quantity = values.getAsInteger(BooksEntry.COLUMN_QUANTITY);
        if (quantity < 0)
            throw new IllegalArgumentException("Quantity can't be negative");

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        long rowId = db.insert(BooksEntry.TABLE_NAME, null, values);

        if (rowId == -1) {
            Log.e("BooksProvider:", "Failed to insert a new row");
            return null;
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return ContentUris.withAppendedId(uri, rowId);
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {

        int match = sUriMatcher.match(uri);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int rowDeleted;
        switch (match) {
            case BOOKS:
                rowDeleted = db.delete(BooksEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case BOOKS_ID:
                selection = BooksEntry._ID + "=?";
                selectionArgs = new String[]{"" + ContentUris.parseId(uri)};
                rowDeleted = db.delete(BooksEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Cannot delete for uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return rowDeleted;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {

        int match = sUriMatcher.match(uri);
        switch (match) {
            case BOOKS:
                return updateRow(uri, values, selection, selectionArgs);
            case BOOKS_ID:
                selection = BooksEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateRow(uri, values, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Can't update the data for uri: " + uri);
        }
    }

    private int updateRow(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        if (values.containsKey(BooksEntry.COLUMN_PRODUCT_NAME))
            if (values.getAsString(BooksEntry.COLUMN_PRODUCT_NAME) == null)
                throw new IllegalArgumentException("Name cant be null");

        if (values.containsKey(BooksEntry.COLUMN_QUANTITY)) {
            int quantity = values.getAsInteger(BooksEntry.COLUMN_QUANTITY);
            if (quantity < 0)
                throw new IllegalArgumentException("Quantity can't be negative");
        }

        if (values.containsKey(BooksEntry.COLUMN_PRICE)) {
            Integer price = values.getAsInteger(BooksEntry.COLUMN_PRICE);
            if (price == null || price < 0)
                throw new IllegalArgumentException("Price cannot be null or negative");
        }

        if (values.size() == 0)
            return 0;

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int rowUpdated = db.update(BooksEntry.TABLE_NAME, values, selection, selectionArgs);
        if (rowUpdated != 0)
            getContext().getContentResolver().notifyChange(uri, null);

        return rowUpdated;
    }
}
