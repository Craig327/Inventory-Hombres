package com.example.inventoryapp;


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import au.com.bytecode.opencsv.CSVWriter;

public class ItemsDataSource {

	// Database fields
	public SQLiteDatabase database;
	private DatabaseHelper dbHelper;
	private String[] allColumns = { DatabaseHelper.SI_ITEM_ID,
			DatabaseHelper.SI_NAME };

	public ItemsDataSource(Context context) {
		Log.d("xxx", "ItemsDataSource");
		dbHelper = DatabaseHelper.getInstance(context);
		System.out.printf("xxx dbHelper:%s ", dbHelper);

	}

  public void close() {
	  database.close();
  }
  public void open() {
//	  database.open();
  }
  public boolean itemExists(String id){
	  Cursor temp = database.rawQuery("SELECT * " +
	  								  "FROM StockItem " +
	  								  "WHERE _id = " + "\"" + id + "\";"
	  								  , null);
	  if(temp != null && temp.getCount()>0)
		  return true;
	  else
		  return false;
  }
  public void newItem(String id, String type, String name, String supplier, String min, String max, String current){
	  database.execSQL("INSERT INTO StockItem (_id, type, name, supplier, min, max, current)" +
	  				   "VALUES (" + id + ", \"" +  type + "\" , \"" + name + "\", " + "\"" + supplier 
	  				              + "\" , " + min + ", " + max + ", " + current + ");");
  }

  public void deleteItem(Item item) {
	String name = item.getName();
	Log.d("xxx", "Name = " + name);
    database.execSQL("DELETE FROM StockItem " +
    		         "WHERE name=" + "\"" + name + "\"");
  }
  public List<Item> getAllItems() {
    List<Item> items = new ArrayList<Item>();

    Cursor cursor = database.query(DatabaseHelper.TABLE_ITEMS,
        allColumns, null, null, null, null, "name");

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
 public Item getItemByName(String n){
	 Item item = null;
	 Cursor temp = database.rawQuery("SELECT * FROM StockItem" +
	                                " WHERE name = ?", new String[] {n});	
	 if(temp.moveToFirst()) {
		 long id = temp.getLong(temp.getColumnIndex("_id"));
		 String name = temp.getString(temp.getColumnIndex("name"));
		 String type = temp.getString(temp.getColumnIndex("type"));
		 String supplier = temp.getString(temp.getColumnIndex("supplier"));
		 double min = temp.getDouble(temp.getColumnIndex("min"));
		 double max = temp.getDouble(temp.getColumnIndex("max"));
		 item = new Item(id, name, type, min, max, supplier, 0);
		 }
		else {
		    /* SQL Query returned no result ... */
		}
	 
	 temp.close();
	 return item;
 }
 public void updateItemByName(String id, String type, String name, String supplier, String min, String max, String current, String oldName){
	 database.execSQL("UPDATE StockItem " +
			 		  "SET name = " + "\"" + name + "\"" + 
			 			", _id = " + "\"" + id + "\"" +
			 		    ", type = " + "\"" + type + "\"" +
			 			", supplier = " + "\"" + supplier + "\"" +
			 		    ", min = " + "\"" + min + "\"" +
			 			", max = " + "\"" + max + "\"" +
			 		   " WHERE name = " + "\"" + oldName + "\";");
 }
  private Item cursorToItem(Cursor cursor) {
    Item item = new Item();
    item.setId(cursor.getLong(0));
    item.setName(cursor.getString(1));
    return item;
  }
  public List<Item> getItemsOfType(String type){
	  List<Item> items = new ArrayList<Item>();
	  
	  Cursor cursor = database.query(DatabaseHelper.TABLE_ITEMS,
		        allColumns, "type = ?", new String[] {type}, null, null, "name");
	  
	  //Cursor cursor = database.rawQuery("SELECT * FROM Items " +
	  							        //"WHERE type = ?", new String[] {type});
	  
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
  public List<Item> getAllItemsFromTable(String TABLENAME) {
	    List<Item> items = new ArrayList<Item>();

	    Cursor cursor = database.query(DatabaseHelper.TABLE_ITEMS,
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
  
  //-----------------------------------------------ROSELLES SECTION--------------------------//
  //-----------------------------------------------------------------------------------------//
  
  public Cursor fetchOrderDates() { // used by OrdersActivity
		String sql = "Select _id, odate, rcount, MIN(COALESCE(rcount, -1)) FROM Orders GROUP BY odate ORDER BY odate DESC;";
		return database.rawQuery(sql, null);
	}

	public boolean isOrderAlready(String date){// used by OrdersActivity
		boolean isOrder = false;
		final String table = DatabaseHelper.TABLE_ORDERS;
		final String[] columns = {DatabaseHelper.O_ITEM_ID, DatabaseHelper.O_DATE};
		final String selection = DatabaseHelper.O_DATE + " = ?";
		final String[] selectionArgs = {date};
		final String groupBy = DatabaseHelper.O_DATE;
		final String having = null;
		final String orderBy = DatabaseHelper.O_DATE;
		final String limit = null;
		Cursor cursor = database.query(table, columns, selection, selectionArgs, groupBy, having, orderBy, limit);
		if (cursor.getCount()>0) isOrder = true;
		return isOrder;
	}
	
	public Cursor fetchOrdersWithDate(String date){//used by OrderOldActivity and OrderReceivedActivity
		String sql = "SELECT SI.*"
				+ ", I." + DatabaseHelper.I_COUNT + " AS " + DatabaseHelper.AS_LAST_I_COUNT 
				+ ", I." + DatabaseHelper.I_DATE + " AS " + DatabaseHelper.AS_LAST_I_DATE 
				+ " , O." + DatabaseHelper.O_COUNT+ " AS " + DatabaseHelper.AS_LAST_O_COUNT
				+ " , O." + DatabaseHelper.O_DATE+ " AS " + DatabaseHelper.AS_LAST_O_DATE
				+ " , O3.*"
				
				+ " FROM " + DatabaseHelper.TABLE_ITEMS + " SI"
				+ " LEFT OUTER JOIN " + DatabaseHelper.TABLE_INVENTORY + " I"
				+ " ON SI." + DatabaseHelper.SI_ITEM_ID + " = I." + DatabaseHelper.I_ITEM_ID
				+ " AND I." + DatabaseHelper.I_DATE + " = (SELECT MAX(I2." + DatabaseHelper.I_DATE
				+ ")FROM " + DatabaseHelper.TABLE_INVENTORY + " I2)"
				+ " LEFT OUTER JOIN " + DatabaseHelper.TABLE_ORDERS + " O"
				+ " ON SI." + DatabaseHelper.I_ITEM_ID + " = O." + DatabaseHelper.I_ITEM_ID
				+ " AND O." + DatabaseHelper.O_DATE + " = ( SELECT MAX(O2." + DatabaseHelper.O_DATE
				+ ") FROM " + DatabaseHelper.TABLE_ORDERS + " O2)"
				
				+ " JOIN " + DatabaseHelper.TABLE_ORDERS + " O3"
				+ " ON SI." + DatabaseHelper.I_ITEM_ID + " = O3." + DatabaseHelper.I_ITEM_ID
				+ " AND O3." + DatabaseHelper.O_DATE + " = ?"
				+ " ORDER BY + SI." + DatabaseHelper.SI_SUPPLIER;

		String[] selectionArgs = {date};
		Log.d("xxx", sql);
		return database.rawQuery(sql, selectionArgs);
	}
	
	public Cursor fetchAllItemsWithLastAndDate(String date){//used by OrderNewActivity
		String sql = "SELECT SI.*"
				+ ", I." + DatabaseHelper.I_COUNT + " AS " + DatabaseHelper.AS_LAST_I_COUNT 
				+ ", I." + DatabaseHelper.I_DATE + " AS " + DatabaseHelper.AS_LAST_I_DATE 
				+ " , O." + DatabaseHelper.O_COUNT+ " AS " + DatabaseHelper.AS_LAST_O_COUNT
				+ " , O." + DatabaseHelper.O_DATE+ " AS " + DatabaseHelper.AS_LAST_O_DATE
				+ " , O3." + DatabaseHelper.O_COUNT
				
				+ " FROM " + DatabaseHelper.TABLE_ITEMS + " SI"
				+ " LEFT OUTER JOIN " + DatabaseHelper.TABLE_INVENTORY + " I"
				+ " ON SI." + DatabaseHelper.SI_ITEM_ID + " = I." + DatabaseHelper.I_ITEM_ID
				+ " AND I." + DatabaseHelper.I_DATE + " = (SELECT MAX(I2." + DatabaseHelper.I_DATE
				+ ")FROM " + DatabaseHelper.TABLE_INVENTORY + " I2)"
				+ " LEFT OUTER JOIN " + DatabaseHelper.TABLE_ORDERS + " O"
				+ " ON SI." + DatabaseHelper.I_ITEM_ID + " = O." + DatabaseHelper.I_ITEM_ID
				+ " AND O." + DatabaseHelper.O_DATE + " = ( SELECT MAX(O2." + DatabaseHelper.O_DATE
				+ ") FROM " + DatabaseHelper.TABLE_ORDERS + " O2)"
				
				+ " LEFT OUTER JOIN " + DatabaseHelper.TABLE_ORDERS + " O3"
				+ " ON SI." + DatabaseHelper.I_ITEM_ID + " = O3." + DatabaseHelper.I_ITEM_ID
				+ " AND O3." + DatabaseHelper.O_DATE + " = ?"
				+ " ORDER BY + SI." + DatabaseHelper.SI_SUPPLIER;

		String[] selectionArgs = {date};
		Log.d("xxx", sql);
		return database.rawQuery(sql, selectionArgs);
	}
	
	public Cursor fetchAllItemsWithLast() {//Craig is using?
		String sql = "SELECT SI.*"
				+ ", I." + DatabaseHelper.I_COUNT + " AS " + DatabaseHelper.AS_LAST_I_COUNT 
				+ ", I." + DatabaseHelper.I_DATE + " AS " + DatabaseHelper.AS_LAST_I_DATE 
				+ " , O." + DatabaseHelper.O_COUNT+ " AS " + DatabaseHelper.AS_LAST_O_COUNT
				+ " , O." + DatabaseHelper.O_DATE+ " AS " + DatabaseHelper.AS_LAST_O_DATE
				+ " FROM " + DatabaseHelper.TABLE_ITEMS + " SI"
				+ " LEFT OUTER JOIN " + DatabaseHelper.TABLE_INVENTORY + " I"
				+ " ON SI." + DatabaseHelper.SI_ITEM_ID + " = I." + DatabaseHelper.I_ITEM_ID
				+ " AND I." + DatabaseHelper.I_DATE + " = (SELECT MAX(I2." + DatabaseHelper.I_DATE
				+ ")FROM " + DatabaseHelper.TABLE_INVENTORY + " I2)"
				+ " LEFT OUTER JOIN " + DatabaseHelper.TABLE_ORDERS + " O"
				+ " ON SI." + DatabaseHelper.I_ITEM_ID + " = O." + DatabaseHelper.I_ITEM_ID
				+ " AND O." + DatabaseHelper.O_DATE + " = ( SELECT MAX(O2." + DatabaseHelper.O_DATE
				+ ") FROM " + DatabaseHelper.TABLE_ORDERS + " O2)"
				//changing next line!!! possibly need to delete
				+ " ORDER BY + SI." + DatabaseHelper.SI_SUPPLIER;
		String[] selectionArgs = null;
		Log.d("xxx", sql);
		return database.rawQuery(sql, selectionArgs);
	}

	public void delete(Context context, String table, String whereClause, String[] whereArgs){
		database.delete(table, whereClause, whereArgs);// used by OrdersActivity and OrderNewActivity and OrderOldActivity
		try{
			database.delete(table, whereClause, whereArgs);
		} catch(SQLException exception) {
			new AlertDialog.Builder(context)
  	    .setTitle("Delete Failed")
  	    .setMessage(exception.toString())
  	    .show();
		}
	}
	
	public int update(Context context, String table, ContentValues values, String whereClause, String[] whereArgs){
		Log.d("xxx", "update"); //used by OrdersActivity and OrderNewActivity
		int i = -1;
		try{
		i = database.update(table, values, whereClause, whereArgs);
		Log.d("xxx", "i: " + i);
		} catch(SQLException exception) {
			new AlertDialog.Builder(context)
  	    .setTitle("Update Failed")
  	    .setMessage(exception.toString())
  	    .show();
		}
		if (i <= 0){
			new AlertDialog.Builder(context)
  	    .setTitle("Update Failed")
  	    .setMessage("0 rows were updated")
  	    .show();
		}
		return i;
	}
	
	public void insert(Context context, String table, ContentValues values){//used by OrderNewActivity
		Log.d("xxx", "datasource insert");
		try{
			database.insertOrThrow(table, null, values);
		} catch(SQLException exception) {
			new AlertDialog.Builder(context)
  	    .setTitle("Update Failed")
  	    .setMessage(exception.toString())
  	    .show();
		}
	}
	// ----------------------_Date Converter/Configure, something--------------------------------------//	

		public static String dateSqlToStd(String sqlString){
			String stdString = null;
			Date date = null;
			try {
				date = new SimpleDateFormat("yyyy-MM-dd").parse(sqlString);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			SimpleDateFormat sdf = new SimpleDateFormat("M/d/yyyy");
			if (date != null)
				stdString = sdf.format(date);
			return stdString;
		}
		
		public static String dateStdToSql(String stdString){
			Log.d("xxx", "dateStdToSql");
			String sqlString = null;
			Date date = null;
			try {
				date = new SimpleDateFormat("MM/dd/yyyy").parse(stdString);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			if (date != null)
				sqlString = sdf.format(date);
			Log.d("xxx", "stdString: " + stdString + " sqlString: " + sqlString);

			return sqlString;
		}
//-------------------------------------Craig's Stuff-------------------------------------------------------//
		public void updateInventory(String date, String id, String count){
			String sql = "UPDATE " + DatabaseHelper.TABLE_INVENTORY  +
					" SET "  + DatabaseHelper.I_COUNT + " = \"" + count + "\"" +
					" WHERE " + DatabaseHelper.I_DATE + " = \"" + date + "\"" + " AND " + DatabaseHelper.I_ITEM_ID + " = \"" + id + "\"" + ";";
			//String[] selectionArgs = {date, count, id};
			Log.d("xxx", sql);
			//database.rawQuery(sql, selectionArgs);
			database.execSQL(sql);
		}
		
		public Cursor fetchInventoryWithDate(String date){
			String sql = "SELECT * " +
					"FROM " + DatabaseHelper.TABLE_INVENTORY + " I" +
					" LEFT OUTER JOIN " + DatabaseHelper.TABLE_ITEMS + " SI" +
					" ON I." + DatabaseHelper.I_ITEM_ID + " = SI." + DatabaseHelper.SI_ITEM_ID +
					" WHERE I." + DatabaseHelper.I_DATE + " = ?;";
			String[] selectionArgs = {date};
			Log.d("xxx", sql);
			return database.rawQuery(sql, selectionArgs);
		}
		
		public Cursor fetchInventoryWithDateandType(String date, String type){
			String sql = "SELECT * " +
					"FROM " + DatabaseHelper.TABLE_INVENTORY + " I" +
					" LEFT OUTER JOIN " + DatabaseHelper.TABLE_ITEMS + " SI" +
					" ON I." + DatabaseHelper.I_ITEM_ID + " = SI." + DatabaseHelper.SI_ITEM_ID +
					" WHERE I." + DatabaseHelper.I_DATE + " = ?"
					+ " AND " + DatabaseHelper.SI_TYPE + "= ?" +
					" ORDER BY + SI." + DatabaseHelper.SI_SUPPLIER;
					
			String[] selectionArgs = {date, type};
			Log.d("xxx", sql);
			return database.rawQuery(sql, selectionArgs);
		}
		
		public Cursor fetchAllItemsWithLastbyType(String type, String date) {
			String sql = "SELECT SI.*"
					+ ", I." + DatabaseHelper.I_COUNT + " AS " + DatabaseHelper.AS_LAST_I_COUNT 
					+ ", I." + DatabaseHelper.I_DATE + " AS " + DatabaseHelper.AS_LAST_I_DATE 
					+ " , O." + DatabaseHelper.O_COUNT+ " AS " + DatabaseHelper.AS_LAST_O_COUNT
					+ " , O." + DatabaseHelper.O_DATE+ " AS " + DatabaseHelper.AS_LAST_O_DATE
					+ " , I3." + DatabaseHelper.I_COUNT
					
					+ " FROM " + DatabaseHelper.TABLE_ITEMS + " SI"
					+ " LEFT OUTER JOIN " + DatabaseHelper.TABLE_INVENTORY + " I"
					+ " ON SI." + DatabaseHelper.SI_ITEM_ID + " = I." + DatabaseHelper.I_ITEM_ID
					+ " AND I." + DatabaseHelper.I_DATE + " = (SELECT MAX(I2." + DatabaseHelper.I_DATE
					+ ")FROM " + DatabaseHelper.TABLE_INVENTORY + " I2)"
					+ " LEFT OUTER JOIN " + DatabaseHelper.TABLE_ORDERS + " O"
					+ " ON SI." + DatabaseHelper.I_ITEM_ID + " = O." + DatabaseHelper.I_ITEM_ID
					+ " AND O." + DatabaseHelper.O_DATE + " = ( SELECT MAX(O2." + DatabaseHelper.O_DATE
					+ ") FROM " + DatabaseHelper.TABLE_ORDERS + " O2)"
					
					+ " LEFT OUTER JOIN " + DatabaseHelper.TABLE_INVENTORY + " I3"
					+ " ON SI." + DatabaseHelper.I_ITEM_ID + " = I3." + DatabaseHelper.I_ITEM_ID
					+ " AND I3." + DatabaseHelper.I_DATE + " = ?"
				//changing next line!!! possibly need to delete
					
					+ " WHERE SI." + DatabaseHelper.SI_TYPE + " = ?"
					+ " ORDER BY + SI." + DatabaseHelper.SI_SUPPLIER;
					
			String[] selectionArgs = {date, type};
			Log.d("xxx", sql);
			return database.rawQuery(sql, selectionArgs);
		}
		
		public Cursor fetchInventoryTypes() {
			final String table = DatabaseHelper.TABLE_ITEMS;
			final String[] columns = { DatabaseHelper.SI_ITEM_ID, DatabaseHelper.SI_TYPE};
			final String selection = "";
			final String[] selectionArgs = null;
			final String groupBy = DatabaseHelper.SI_TYPE;
			final String having = null;
			final String orderBy = DatabaseHelper.SI_TYPE + " ASC";
			final String limit = null;
		    return database.query(table, columns, selection, selectionArgs, groupBy, having, orderBy, limit); 
		}
		
		public Cursor fetchInventoryDates() {
			final String table = DatabaseHelper.TABLE_INVENTORY;
			final String[] columns = { DatabaseHelper.I_ITEM_ID, DatabaseHelper.I_DATE, DatabaseHelper.I_COUNT};
			final String selection = "";
			final String[] selectionArgs = null;
			final String groupBy = DatabaseHelper.I_DATE;
			final String having = null;
			final String orderBy = DatabaseHelper.I_COUNT + " ASC";
			final String limit = null;
		    return database.query(table, columns, selection, selectionArgs, groupBy, having, orderBy, limit); 
		}
		public boolean isInventoryAlready(String date){
			boolean isInventory = false;
			final String table = DatabaseHelper.TABLE_INVENTORY;
			final String[] columns = {DatabaseHelper.I_ITEM_ID, DatabaseHelper.I_DATE};
			final String selection = DatabaseHelper.I_DATE + " = ?";
			final String[] selectionArgs = {date};
			final String groupBy = DatabaseHelper.I_DATE;
			final String having = null;
			final String orderBy = DatabaseHelper.I_DATE;
			final String limit = null;
			Cursor cursor = database.query(table, columns, selection, selectionArgs, groupBy, having, orderBy, limit);
			if (cursor.getCount()>0) isInventory = true;
			return isInventory;
		}
		public boolean isitemInventoryAlready(String date, String id){
			boolean isInventory = false;
			final String table = DatabaseHelper.TABLE_INVENTORY;
			final String[] columns = { DatabaseHelper.I_DATE, DatabaseHelper.I_ITEM_ID};
			final String selection = DatabaseHelper.I_DATE + " = ?" + " AND " + DatabaseHelper.I_ITEM_ID + " = ?";
			final String[] selectionArgs = {date, id};
			final String groupBy = DatabaseHelper.I_DATE;
			final String having = null;
			final String orderBy = DatabaseHelper.I_DATE;
			final String limit = null;
			Cursor cursor = database.query(table, columns, selection, selectionArgs, groupBy, having, orderBy, limit);
			if (cursor.getCount()>0) isInventory = true;
			return isInventory;
		}
//------------------------------------Create CSV----------------------------------------------------//
		
		//create a list of string arrays to store each row of the cursor generated by SQL query
		private List<String[]> fromCursorToStringList(Cursor c){
		    List<String[]> result = new ArrayList<String[]>();
		    c.moveToFirst();
		    for(int i = 0; i < c.getCount(); i++){
		    	String[] row = new String[5];
		    	row[0] = c.getString(c.getColumnIndex(DatabaseHelper.SI_SUPPLIER));
		    	row[1] = c.getString(c.getColumnIndex(DatabaseHelper.SI_ITEM_ID));
		    	row[2] = c.getString(c.getColumnIndex(DatabaseHelper.SI_NAME));
		    	row[3] = c.getString(c.getColumnIndex(DatabaseHelper.O_COUNT));
		    	row[4] = c.getString(c.getColumnIndex(DatabaseHelper.SI_TYPE));
		        result.add(row);     
		        c.moveToNext();
		    }
		    return result;
		}
		public void createCSV(String date, File file){
			
			try{
				//Convert the cursor generated into an list of string arrays
				List<String[]> results = fromCursorToStringList(fetchAllItemsWithLastAndDate(date));
				//Initialize a CSV writer and write the string array list to the file
				CSVWriter writer = new CSVWriter(new FileWriter(file));
				writer.writeAll(results);
				writer.close();
			}
			catch (Exception e){
				e.printStackTrace();
			}
		}
}
