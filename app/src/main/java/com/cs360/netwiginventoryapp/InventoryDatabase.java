package com.cs360.netwiginventoryapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/*******************************************************************
 Author      : Chad Netwig
 App Name    : Chad's Inventory App
 Version     : 1.0
 Date        : March 31, 2022
 Updated     : April 14, 2022
             :
 Description : Simple inventory app
             : Model logic for SQLite database CRUD access
             : Comments have been added throughout to explain logic
********************************************************************/

public class InventoryDatabase extends SQLiteOpenHelper {

    private static final int VERSION = 1;
    // constant for the name of the SQLite database
    private static final String DATABASE_NAME = "inventory.db";

    private static InventoryDatabase mInventoryDb;

    // getInstance is used to return Singleton instance of InventoryDatabase class
    public static InventoryDatabase getInstance(Context context) {
        if (mInventoryDb == null) {
            mInventoryDb = new InventoryDatabase(context);
        }
        return mInventoryDb;
    }

    // private constructor for Singleton of the SQLite database
    private InventoryDatabase(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }

    private static final class UserTable {
        private static final String TABLE = "users";
        private static final String COL_ID = "_id";
        private static final String COL_USERNAME = "username";
        private static final String COL_PASSWORD = "password";
    }

    private static final class InventoryTable {
        private static final String TABLE = "inventory";
        private static final String COL_ID = "_id";
        private static final String COL_ITEM_NAME = "itemname";
        private static final String COL_ITEM_QTY = "itemquantity";
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        // Create the user table
        String createUserTable = "CREATE TABLE " + UserTable.TABLE + " ("
                + UserTable.COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + UserTable.COL_USERNAME + " TEXT NOT NULL, "
                + UserTable.COL_PASSWORD + " TEXT NOT NULL)";

        // method to execute above sql query
        db.execSQL(createUserTable);

        // Create the inventory table
        String createInventoryTable = "CREATE TABLE " + InventoryTable.TABLE + " ("
                + InventoryTable.COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + InventoryTable.COL_ITEM_NAME + " TEXT NOT NULL, "
                + InventoryTable.COL_ITEM_QTY + " INTEGER NOT NULL)";

        // method to execute above sql query
        db.execSQL(createInventoryTable);

    } // end onCreate()

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table if exists " + UserTable.TABLE);
        db.execSQL("drop table if exists " + InventoryTable.TABLE);
        onCreate(db);
    }

    /*
    ***************************************
    * *   SQLite methods for Login handling
    * *************************************
     */
    public void addUser(String uname, String pw) {
        // getWriteableDatabase() is called to obtain a writeable SQLiteDatabase object
        SQLiteDatabase db = getWritableDatabase();
        // Holds the table columns and associated data to be inserted into a table
        ContentValues values = new ContentValues();

        values.put(UserTable.COL_USERNAME, uname);
        values.put(UserTable.COL_PASSWORD, pw);

        db.insert(UserTable.TABLE, null, values);

        db.close();
    }

    public boolean userLookup(String uname) {

        SQLiteDatabase db = getReadableDatabase();

        String sql = "SELECT * FROM " + UserTable.TABLE + " WHERE UPPER(" + UserTable.COL_USERNAME + ") = \"" + uname.toUpperCase() + "\"";
        Log.i("SQL", sql);

        Cursor cursor = db.rawQuery(sql, null);

        if (cursor.moveToFirst()) {
            cursor.close();
            return true;
        }
        cursor.close();
        return false;
    }

    public boolean pwLookup(String uname, String pw){
        SQLiteDatabase db = getReadableDatabase();

        String sql = "SELECT * FROM " + UserTable.TABLE + " WHERE UPPER(" + UserTable.COL_USERNAME + ") = \"" + uname.toUpperCase() + "\""
                + " AND " + UserTable.COL_PASSWORD + " = \"" + pw + "\"";
        Log.i("SQL", sql);

        Cursor cursor = db.rawQuery(sql, null);

        if (cursor.moveToFirst()) {
            cursor.close();
            return true;
        }
        cursor.close();
        return false;

    }

    /*
     ************************************************
     * *   SQLite methods for Inventory CRUD handling
     * **********************************************
     */
    public void addInventoryItem(String item_name, int qty) {
        // getWriteableDatabase() is called to obtain a writeable SQLiteDatabase object
        SQLiteDatabase db = getWritableDatabase();
        // Holds the table columns and associated data to be inserted into a table
        ContentValues values = new ContentValues();

        values.put(InventoryTable.COL_ITEM_NAME, item_name);
        values.put(InventoryTable.COL_ITEM_QTY, qty);

        db.insert(InventoryTable.TABLE, null, values);

        db.close();
    }

    // reads all fields from the Inventory Table and builds and returns an ArrayList of Strings
    public List<String> readInventory() {

        List<String> inventory = new ArrayList<String>();
        SQLiteDatabase db = getReadableDatabase();

        String sql = "SELECT * FROM " + InventoryTable.TABLE;
        Log.i("SQL", sql);

        Cursor cursor = db.rawQuery(sql, null);

        if (cursor.moveToFirst()) {
            do {
                inventory.add(cursor.getString(1));
                inventory.add(cursor.getString(2));
            } while (cursor.moveToNext());
        }
        cursor.close();
    return inventory;

    }

    public boolean deleteInventoryItem(int id) {
        // getWriteableDatabase() is called to obtain a writeable SQLiteDatabase object
        SQLiteDatabase db = getWritableDatabase();

        int rowsDeleted = db.delete(InventoryTable.TABLE,InventoryTable.COL_ID + " = ?",
                new String[] { Integer.toString(id)});

        db.close();
        return rowsDeleted > 0;
    }

    public boolean updateInventoryItemQuantity(int id, int new_qty) {
        // getWriteableDatabase() is called to obtain a writeable SQLiteDatabase object
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(InventoryTable.COL_ITEM_QTY, new_qty);

        int rowsUpdated = db.update(InventoryTable.TABLE, values,InventoryTable.COL_ID + " = ?",
                new String[] { Integer.toString(id)});

        db.close();
        return rowsUpdated > 0;
    }

    // returns true if item_name is found in the Inventory Table, else false
    public boolean inventoryItemLookup(String item_name) {

        SQLiteDatabase db = getReadableDatabase();

        String sql = "SELECT * FROM " + InventoryTable.TABLE + " WHERE UPPER(" + InventoryTable.COL_ITEM_NAME + ") = \"" + item_name.toUpperCase() + "\"";
        Log.i("SQL", sql);

        Cursor cursor = db.rawQuery(sql, null);

        if (cursor.moveToFirst()) {
            cursor.close();
            return true;
        }
        cursor.close();
        return false;
    }

    // returns primary key for item_name is it is found in the Inventory Table, else -1
    public int inventoryPrimaryKeyLookup(String item_name) {
        int key = -1;
        SQLiteDatabase db = getReadableDatabase();
        //Toast.makeText(InventoryDatabase.this, "item_name value: " + item_name, Toast.LENGTH_SHORT).show();
        Log.i("LOOKUP", item_name);
        String sql = "SELECT rowid, * FROM " + InventoryTable.TABLE + " WHERE UPPER(" + InventoryTable.COL_ITEM_NAME + ") = \"" + item_name.toUpperCase() + "\"";
        Log.i("SQL", sql);

        Cursor cursor = db.rawQuery(sql, null);

        if (cursor.moveToFirst()) {
            key = cursor.getInt(0);
            cursor.close();
            return key;
        }
        cursor.close();
        return key; // returns -1 if cannot find the primary key
    }

} // end InventoryDatabase()