package com.example.inventoryapp;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {
	private static DatabaseHelper sInstance;
	private SQLiteDatabase database;
	
	public static final String TABLE_ITEMS ="StockItem";
	public static final String SI_ITEM_ID ="_id";
	public static final String SI_NAME ="name";
	public static final String SI_TYPE ="type";
	public static final String SI_SUPPLIER ="supplier";
	public static final String SI_MIN ="min";
	public static final String SI_MAX ="max";
	
	public static final String TABLE_INVENTORY ="Inventory";
	public static final String I_ITEM_ID ="_id";
	public static final String I_DATE ="idate";
	public static final String I_COUNT ="icount";

	public static final String TABLE_ORDERS ="Orders";
	public static final String O_ITEM_ID ="_id";
	public static final String O_DATE ="odate";
	public static final String O_COUNT ="ocount";
	public static final String O_RECEIVED_DATE ="rdate";
	public static final String O_RECEIVED_COUNT ="rcount";
	public static final String AS_LAST_I_DATE ="lastidate";
	public static final String AS_LAST_I_COUNT ="lasticount";
	public static final String AS_LAST_O_DATE ="lastodate";
	public static final String AS_LAST_O_COUNT ="lastocount";
	
	private static final String DATABASE_NAME = "inventory.db";
	private static final int DATABASE_VERSION = 1;
	
	
// ------------------------------Create Database-------------------------------
	private static final String DATABASE_CREATE_STOCK_ITEMS = "CREATE TABLE IF NOT EXISTS "
			+ TABLE_ITEMS + "(" 
				  + SI_ITEM_ID + " INTEGER, "
				  + SI_NAME + " TEXT, "
				  + SI_TYPE + " TEXT, "
				  + SI_SUPPLIER + " TEXT, "
				  + SI_MIN + " REAL, "
				  + SI_MAX + " REAL, "
				  + "UNIQUE (" + SI_NAME + "), PRIMARY KEY (" + SI_ITEM_ID + ")); ";
	private static final String DATABASE_CREATE_INVENTORY = "CREATE TABLE IF NOT EXISTS " + TABLE_INVENTORY + "(" 
				  + I_ITEM_ID + " INTEGER, "
				  + I_DATE + " TEXT, "
				  + I_COUNT + " REAL, "
				  + "PRIMARY KEY (" + I_ITEM_ID + ", " + I_DATE + "), FOREIGN KEY (" + I_ITEM_ID
				  + ") REFERENCES " + SI_ITEM_ID + " (" + SI_ITEM_ID + ") ON DELETE NO ACTION ON UPDATE NO ACTION); ";
	private static final String DATABASE_CREATE_ORDERS = "CREATE TABLE IF NOT EXISTS " + TABLE_ORDERS + "(" 
				  + O_ITEM_ID + " INTEGER, "
				  + O_DATE + " TEXT, "
				  + O_COUNT + " REAL, "
				  + O_RECEIVED_DATE + " TEXT, "
				  + O_RECEIVED_COUNT + " REAL, "
				  + "PRIMARY KEY (" + O_ITEM_ID + ", " + O_DATE + "), FOREIGN KEY (" + O_ITEM_ID
				  + ") REFERENCES " + SI_ITEM_ID + " (" + SI_ITEM_ID + ") ON DELETE NO ACTION ON UPDATE NO ACTION);";
	
// ------------------------------Sample Database-------------------------------
	
	private static final String DATABASE_SAMPLE_STOCK_ITEMS = "INSERT INTO StockItem(_id, name, type, supplier, min, max) " +
			"VALUES (1100, 'Amber', 'Beer', 'Acme Alcohol', 40, 100), " +
			"(1201, 'Pale Ale', 'Beer', 'Acme Alcohol', 40, 100), " +
			"(1350, 'Stout', 'Beer', 'Acme Alcohol', 40, 100), " +
			"(2200, 'Red Wine', 'Wine', 'Cell Cellars', 12, 24), " +
			"(2300, 'White Wine', 'Wine', 'Cell Cellars', 12, 24); ";
	private static final String DATABASE_SAMPLE_INVENTORY = "INSERT INTO Inventory(_id, idate, icount) VALUES (1100, '2014-02-17', 33), " +
			"(1201, '2014-02-17', 44), (1350, '2014-02-17', 18), (2200, '2014-02-17', 20), " +
			"(2300, '2014-02-17', 8); INSERT INTO Inventory(_id, idate, icount) " +
			"VALUES (1100, '2014-03-17', 27), (1201, '2014-03-17', 42), (1350, '2014-03-17', 16), (2200, '2014-03-17', 20), " +
			"(2300, '2014-03-17', 5); ";
	private static final String DATABASE_SAMPLE_ORDERS = "INSERT INTO Orders(_id, odate, ocount) VALUES (1100, '2014-02-20', 50), " +
			"(1201, '2014-02-20', 50), (1350, '2014-02-20', 50), (2200, '2014-02-20', 50), " +
			"(2300, '2014-02-20', 50), (2200, '2014-02-21', 50), (2200, '2014-02-22', 50), (2200, '2014-02-23', 50); ";
	private static final String DATABASE_SAMPLE_ORDERS_RECEIVED = "UPDATE Orders SET rcount = 50, rdate = '2014-02-25' WHERE _id = 1100 AND odate = '2014-02-20'; " +
			"UPDATE Orders SET rcount = 48, rdate = '2014-02-25' WHERE _id = 1201 AND odate = '2014-02-20'; " +
			"UPDATE Orders SET rcount = 50, rdate = '2014-02-25' WHERE _id = 1350 AND odate = '2014-02-20'; " +
			"UPDATE Orders SET rcount = 50, rdate = '2014-02-25' WHERE _id = 2200 AND odate = '2014-02-20';";

// ------------------------------Singleton/Setup-------------------------------

	public static DatabaseHelper getInstance(Context context) {
	    // Use the application context, which will ensure that you 
	    // don't accidentally leak an Activity's context.
	    // See this article for more information: http://bit.ly/6LRzfx
		if (sInstance == null) {
			sInstance = new DatabaseHelper(context.getApplicationContext());
		}
		return sInstance;
	} 
	
	private DatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		Log.d("xxx", "1..............DatabaseHelper made");
	}
	
	// Method is called during creation of the database
	@Override
	public void onCreate(SQLiteDatabase database) {
		Log.d("xxx", "2.................Database Helper onCreate");
		this.database = database;

		//tables need to be created separately for content provider
	    database.execSQL(DATABASE_CREATE_STOCK_ITEMS);
	    database.execSQL(DATABASE_CREATE_INVENTORY);
	    database.execSQL(DATABASE_CREATE_ORDERS);
	    database.execSQL(DATABASE_SAMPLE_STOCK_ITEMS);	
	    database.execSQL(DATABASE_SAMPLE_INVENTORY);	
	    database.execSQL(DATABASE_SAMPLE_ORDERS);
	    database.execSQL(DATABASE_SAMPLE_ORDERS_RECEIVED);
	    }

	// Method is called during an upgrade of the database,
	// e.g. if you increase the database version
	@Override
	public void onUpgrade(SQLiteDatabase database, int oldVersion,
			int newVersion) {
	    Log.w(DatabaseHelper.class.getName(),
	            "Upgrading database from version " + oldVersion + " to "
	                + newVersion + ", which will destroy all old data");
		database.execSQL("DROP TABLE IF EXISTS " + TABLE_ORDERS);
		database.execSQL("DROP TABLE IF EXISTS " + TABLE_INVENTORY);
	    database.execSQL("DROP TABLE IF EXISTS " + TABLE_ITEMS);
	    onCreate(database);	
	}
// ------------------------------Get Data-------------------------------


}