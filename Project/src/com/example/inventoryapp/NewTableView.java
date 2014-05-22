package com.example.inventoryapp;

import java.util.List;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class NewTableView extends ListActivity{
	private static final int ACTIVITY_COUNT = 1;
	private static final int ACTIVITY_VIEW = 3;
	private static final int ACTIVITY_DATE_DIALOG = 4;
	
	private ImportDatabase importer;
	private ItemsDataSource datasource;
	
	String date;
	List<Item> values;
	private SimpleCursorAdapter adapter;
	Intent intent;
	
	
//--------------------------------------------Create-----------------------------------------------//
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.d("xxx", "New Table View Activity onCreate");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.basic_list);
		datasource = new ItemsDataSource(this);
		importer = new ImportDatabase(this);
		fillData();
		registerForContextMenu(getListView());
	
		intent = getIntent();
		Bundle extras = intent.getExtras();
		date = extras.getString("date_selected");
		if(date != null)Log.d("date!", "Activity Result " + date);
		else Log.d("date!", "Activity Result DATE IS NULL");
		getActionBar().setTitle(date);
	}
	
//--------------------------------------------Data-----------------------------------------------//

	private void fillData() {
		datasource.database = importer.getWritableDatabase();
		Cursor cursor = datasource.fetchInventoryTypes();
		Context context = this;
		int rowLayout = R.layout.inventory_display;
		String[] fromColumns = {DatabaseHelper.SI_TYPE};								//overridden by mysimplecursoradapter
		int[] toViews = {R.id.label};								//but needs a value here
		int flags = 0;
		adapter = new SimpleCursorAdapter(context, rowLayout, cursor,
				fromColumns, toViews, flags);
		setListAdapter(adapter);
	}
	
	private void updateCursor(){
	    Cursor cursor = datasource.fetchInventoryTypes();
	    adapter.changeCursor(cursor);
	}

//--------------------------------------------On Click-----------------------------------------------//	

	// Opens the second activity if an entry is clicked
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		Cursor cursor = adapter.getCursor();
		cursor.moveToPosition(position);
		String type = cursor.getString(cursor.getColumnIndex(DatabaseHelper.SI_TYPE));
		Intent i = new Intent(this, New_Inventory_Count.class);
		i.putExtra("type_selected", type);
		if(date == null)Log.d("datesNULL", "date is null, new table view");
		else Log.d("date??" , date);
		i.putExtra("date", date);
		startActivityForResult(i, ACTIVITY_VIEW);
	}
		
//--------------------------------------------Activity Result-----------------------------------------------//	
//??????????????????????????????
	@Override
	protected void onActivityResult(int requestCode, int resultCode,
			Intent intent) {
		Log.d("TableViewActivityResult", " requestCode: " + requestCode + " resultCode: "
				+ resultCode + " intent: " + intent.toString());
		Bundle extras = intent.getExtras();
		//date = extras.getString("date_selected");
		//if(date != null)Log.d("date!", "Activity Result " + date);
	//	else Log.d("date!", "Activity Result DATE IS NULL");
		switch (requestCode){
		case ACTIVITY_DATE_DIALOG:
			if (extras != null) {
				String defaultDate = extras.getString("defaultDate");
				date = extras.getString("dateChosen");
				if(date != null)Log.d("date!", "Activity Result in switch" + date);
				else Log.d("date!", "Activity Result in switchDATE IS NULL");
				boolean isEditNotNew = extras.getBoolean("isEditNotNew");
				if (isEditNotNew) {
					String table = DatabaseHelper.TABLE_INVENTORY;
					ContentValues values = new ContentValues();
					values.put(DatabaseHelper.I_DATE, date);
					String whereClause = DatabaseHelper.I_DATE + " = ?";
					String[] whereArgs = new String[] { defaultDate };
					datasource.update(this, table, values, whereClause, whereArgs);
					updateCursor();
				} else {
					if (datasource.isInventoryAlready(date)){
						new AlertDialog.Builder(this)
			    	    .setMessage("There is already an Inventory item for " + date)
			    	    .setPositiveButton("Edit it", new DialogInterface.OnClickListener() {
			    	        public void onClick(DialogInterface dialog, int which) {
			    	        	goToEditNewActivity(date);
			    	        }
			    	     })
			    	    .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
			    	        public void onClick(DialogInterface dialog, int which) { 
			    	            // do nothing
			    	        }
			    	     })
			    	     .show();
					} else {
						Intent i = new Intent(this, New_Inventory_Count.class);
						i.putExtra("date", date);
						i.putExtra("isNew", true);
						startActivityForResult(i, ACTIVITY_COUNT);
					}
				}
			}
			break;
		case ACTIVITY_VIEW://-----------
		}
		super.onActivityResult(requestCode, resultCode, intent);
	}
	
	private void goToEditNewActivity(String dateChosen){
		Intent i = new Intent(this, New_Inventory_Count.class);
		i.putExtra("date", dateChosen);
		i.putExtra("isNew", false);
		startActivityForResult(i, ACTIVITY_COUNT);
	}

//--------------------------------------------MySimpleCursorAdapter-----------------------------------------------//	

	private class MySimpleCursorAdapter extends SimpleCursorAdapter {

		public MySimpleCursorAdapter(Context context, int layout, Cursor c,
				String[] from, int[] to, int flags) {
			super(context, layout, c, from, to, flags);
		}

		public void bindView(View v, Context context, Cursor c) {
			 date = c.getString(c.getColumnIndex(DatabaseHelper.I_DATE));
			boolean isReceivedNull = c.isNull(c
					.getColumnIndex(DatabaseHelper.I_COUNT));
			Log.d("NewTableView",
					"isReceivedNull: "
							+ isReceivedNull
							+ " date: "
							+ date
							+ "getInt"
							+ c.getInt(c
									.getColumnIndex(DatabaseHelper.I_COUNT)));
			TextView textView = (TextView) v.findViewById(R.id.label);
			if (textView != null) {
				textView.setText(ItemsDataSource.dateSqlToStd(date));
			}
			if (isReceivedNull) {
				ImageView imageView = (ImageView) v.findViewById(R.id.icon);
				imageView.setVisibility(View.INVISIBLE);
			}
		}
	}
	
//--------------------------------------------Life Cycle-----------------------------------------------//	

	@Override
	protected void onResume() {
		Log.d("xxx", "onResume");
//		datasource.open();
		super.onResume();
	}

	@Override
	protected void onPause() {
		Log.d("xxx", "onPause");
//		datasource.close();
		super.onPause();
	}
	
	@Override
	public void onBackPressed(){
		Intent i = new Intent();
		setResult(RESULT_OK, i);
		finish();
		//Needs to go to "Save/Don't Save" Context Menu 4-15-2014
		super.onBackPressed();
	}

}