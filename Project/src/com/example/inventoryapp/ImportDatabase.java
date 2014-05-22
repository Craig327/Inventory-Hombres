//Function that imports the DB stored in assets and ports it to the phone.

package com.example.inventoryapp;

import android.content.Context;
import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

public class ImportDatabase extends SQLiteAssetHelper {

	private static final String DATABASE_NAME = "barInventory.db";
    private static final int DATABASE_VERSION = 1;

    public ImportDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
}