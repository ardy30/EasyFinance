package com.androidcollider.easyfin.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


public class DbHelper extends SQLiteOpenHelper {

    Context context;
    private static final String DATABASE_NAME = "FinU.db";
    private static final int DATABASE_VERSION = 1;

    public DbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SqlQueries.create_account_table);
        db.execSQL(SqlQueries.create_transactions_table);
        db.execSQL(SqlQueries.create_trans_btw_accounts_table);
    }
    // Method for update database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
