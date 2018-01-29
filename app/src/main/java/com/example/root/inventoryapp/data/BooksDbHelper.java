package com.example.root.inventoryapp.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import static com.example.root.inventoryapp.data.BooksContract.BooksEntry;

public class BooksDbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "Books.db";
    private static final int DATABASE_VERSION = 1;

    BooksDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String SQL_CREATE_BOOKS_TABLE = "CREATE TABLE " + BooksEntry.TABLE_NAME + "( " +
                BooksEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                BooksEntry.COLUMN_PRODUCT_NAME + " TEXT NOT NULL, " +
                BooksEntry.COLUMN_PRICE + " REAL NOT NULL, " +
                BooksEntry.COLUMN_QUANTITY + " INTEGER DEFAULT 0, " +
                BooksEntry.COLUMN_SUPPLIER_NAME + " TEXT, " +
                BooksEntry.COLUMN_SUPPLIER_EMAIL + " TEXT, " +
                BooksEntry.COLUMN_SUPPLIER_PHONE_NUMBER + " TEXT);";

        db.execSQL(SQL_CREATE_BOOKS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (db != null)
            db.execSQL("DROP TABLE " + BooksEntry.TABLE_NAME + ";");
        onCreate(db);
    }
}
