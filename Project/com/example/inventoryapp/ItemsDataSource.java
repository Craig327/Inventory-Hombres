package com.example.inventoryapp;


import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class ItemsDataSource {

  // Database fields
  private SQLiteDatabase database;
  private MySQLiteHelper dbHelper;
  private String[] allColumns = { MySQLiteHelper.COLUMN_ID,
      MySQLiteHelper.COLUMN_NAME };

  public ItemsDataSource(Context context) {
    dbHelper = new MySQLiteHelper(context);
  }

  public void open() throws SQLException {
    database = dbHelper.getWritableDatabase();
  }

  public void close() {
    dbHelper.close();
  }

  public String getdbpath(){
	  return database.getPath();
  }
  
  public Item createItem(String name) {
    ContentValues values = new ContentValues();
    values.put(MySQLiteHelper.COLUMN_NAME, name);
    long insertId = database.insert(MySQLiteHelper.TABLE_ITEMS, null,
        values);
    Cursor cursor = database.query(MySQLiteHelper.TABLE_ITEMS,
        allColumns, MySQLiteHelper.COLUMN_ID + " = " + insertId, null,
        null, null, null);
    cursor.moveToFirst();
    Item newItem = cursorToItem(cursor);
    cursor.close();
    return newItem;
  }

  public void deleteItem(Item item) {
    long id = item.getId();
    System.out.println("Item deleted with id: " + id);
    database.delete(MySQLiteHelper.TABLE_ITEMS, MySQLiteHelper.COLUMN_ID
        + " = " + id, null);
  }

  public List<Item> getEachTable(){
	  List<Item> tables = new ArrayList<Item>();
	  Cursor cursor = database.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);
	  cursor.moveToFirst();
	  while (!cursor.isAfterLast()) {
	      Item item = cursorToItem(cursor);
	      tables.add(item);
	      cursor.moveToNext();
	    }
	    // make sure to close the cursor
	    cursor.close();
	    return tables;
  }
  
  public List<Item> getAllItems() {
    List<Item> items = new ArrayList<Item>();

    Cursor cursor = database.query(MySQLiteHelper.TABLE_ITEMS,
        allColumns, null, null, null, null, null);

    cursor.moveToFirst();
    while (!cursor.isAfterLast()) {
      Item item = cursorToItem(cursor);
      items.add(item);
      cursor.moveToNext();
    }
    // make sure to close the cursor
    cursor.close();
    return items;
  }

  private Item cursorToItem(Cursor cursor) {
    Item item = new Item();
    item.setId(cursor.getLong(0));
    item.setName(cursor.getString(1));
    return item;
  }
  
  public List<Item> getAllItemsFromTable(String TABLENAME) {
	    List<Item> items = new ArrayList<Item>();

	    Cursor cursor = database.query(MySQLiteHelper.TABLE_ITEMS,
	        allColumns, null, null, null, null, null);

	    cursor.moveToFirst();
	    while (!cursor.isAfterLast()) {
	      Item item = cursorToItem(cursor);
	      items.add(item);
	      cursor.moveToNext();
	    }
	    // make sure to close the cursor
	    cursor.close();
	    return items;
	  }
}
