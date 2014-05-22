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
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.AdapterView.AdapterContextMenuInfo;

public class InventoryActivity extends ListActivity{
	private static final int ACTIVITY_NEW = 1;
	private static final int ACTIVITY_EDIT = 2;
	private static final int ACTIVITY_VIEW = 3;
	private static final int ACTIVITY_DATE_DIALOG = 4;
	
	private static final int DELETE_ID = Menu.FIRST + 1;
	private static final int EDIT_DATE = Menu.FIRST + 2;
	List<Item> values;
	private MySimpleCursorAdapter adapter;
	
	//variable to keep track of which item actions are being performed on in dialog
	private int position;
	
	private ImportDatabase importer;
	private ItemsDataSource datasource;
	
//--------------------------------------------Create-----------------------------------------------//
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.d("xxx", "Inventory Activity onCreate");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.basic_list);
		
		importer = new ImportDatabase(this);
		datasource = new ItemsDataSource(this);
		
		fillData();
		registerForContextMenu(getListView());
	}
	
//--------------------------------------------Data-----------------------------------------------//

	private void fillData() {
		datasource.database = importer.getWritableDatabase();
		Cursor cursor = datasource.fetchInventoryDates();
		Context context = this;
		int rowLayout = R.layout.inventory_display;
		String[] fromColumns = {DatabaseHelper.I_COUNT};	//overridden by mysimplecursoradapter
		int[] toViews = {R.id.label};								//but needs a value here
		int flags = 0;
		adapter = new MySimpleCursorAdapter(context, rowLayout, cursor,
				fromColumns, toViews, flags);
		setListAdapter(adapter);
	}
	
	private void updateCursor(){
	    Cursor cursor = datasource.fetchInventoryDates();
	    adapter.changeCursor(cursor);
	}

//--------------------------------------------Options Menu-----------------------------------------------//	

	// Create the menu based on the XML definition
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.inventory_activity_actions, menu);
		return true;
	}

	// Reaction to the menu selection
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.newinventory:
			Intent i = new Intent(this, DateDialogActivity.class);
			startActivityForResult(i, ACTIVITY_DATE_DIALOG);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
//--------------------------------------------Context Menu-----------------------------------------------//	
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		menu.add(0, DELETE_ID, 0, R.string.menu_delete);
		menu.add(0, EDIT_DATE, 0, R.string.menu_edit_date);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo(); 
		position = (int) info.position;
		switch (item.getItemId()) {
		case DELETE_ID:
			//create an alert dialog for a delete confirmation
    		new AlertDialog.Builder(this)
    	    .setTitle("Delete entry")
    	    .setMessage("Are you sure you want to delete this entry?")
    	    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
    	        public void onClick(DialogInterface dialog, int which) {
    				Log.d("xxx", "positon: " + position);
    	        	//intermediary because of scope
    	        	deleteItem(position);
    	        }
    	     })
    	    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
    	        public void onClick(DialogInterface dialog, int which) { 
    	            // do nothing
    	        }
    	     })
    	     .show();
			return true;
		case EDIT_DATE:
			Cursor cursor = adapter.getCursor();
			cursor.moveToPosition(position);
			String date = cursor.getString(cursor
					.getColumnIndex(DatabaseHelper.I_DATE));
			Intent i = new Intent(this, DateDialogActivity.class);
			i.putExtra("defaultDate", date);
			i.putExtra("isEditNotNew", true);
			startActivityForResult(i, ACTIVITY_DATE_DIALOG);
			return true;
		}
		return super.onContextItemSelected(item);
	}
	
	public void showDatePickerDialog(View v) {
		DatePickerFragment newFragment = new DatePickerFragment();
		newFragment.show(getFragmentManager(), "datePicker");
	}

//--------------------------------------------On Click-----------------------------------------------//	

	// Opens the second activity if an entry is clicked
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		Cursor cursor = adapter.getCursor();
		cursor.moveToPosition(position);
		String date = cursor.getString(cursor.getColumnIndex(DatabaseHelper.I_DATE));
		Intent i = new Intent(this, NewTableView.class); //CONFIGURE TABLE VIEW TO WORK -- 4/10/2014
		if (date != null)Log.d("I_A_date", date);
		else Log.d("I_A_date", "date is null");
		i.putExtra("date_selected", date);
		startActivity(i);
		finish();
	}
		
//--------------------------------------------Activity Result-----------------------------------------------//	

	@Override
	protected void onActivityResult(int requestCode, int resultCode,
			Intent intent) {
		Log.d("xxx", " requestCode: " + requestCode + " resultCode: "
				+ resultCode + " intent: " + intent.toString());
		Bundle extras = intent.getExtras();
		switch (requestCode){
		case ACTIVITY_DATE_DIALOG:
			if (extras != null) {
				String defaultDate = extras.getString("defaultDate");
				String dateChosen = extras.getString("dateChosen");
				boolean isEditNotNew = extras.getBoolean("isEditNotNew");
				Log.d("xxx", "dateChosen: " + dateChosen + "isEditNotNew: "
						+ String.valueOf(isEditNotNew));
				Log.d("xxy", " requestCode: " + requestCode + " resultCode: "
						+ resultCode + " intent: " + intent.toString());
				if (isEditNotNew) {
					String table = DatabaseHelper.TABLE_INVENTORY;
					ContentValues values = new ContentValues();
					values.put(DatabaseHelper.I_DATE, dateChosen);
					String whereClause = DatabaseHelper.I_DATE + " = ?";
					String[] whereArgs = new String[] { defaultDate };
					datasource.update(this, table, values, whereClause, whereArgs);
					updateCursor();


				} else {
					Intent i = new Intent(this, NewTableView.class); // CONFIGURE TABLE VIEW TO WORK -- 4/10/2014
					i.putExtra("date_selected", dateChosen);
					startActivityForResult(i, ACTIVITY_VIEW);
					
					finish();
				}
			}
			break;
		case ACTIVITY_NEW:
			Log.d("fromMain", "Coming from Main");
			break;
		case ACTIVITY_EDIT:
			if (extras != null) {
				boolean update = extras.getBoolean("update");
				if (update) updateCursor();
			}
			break;
		}
		super.onActivityResult(requestCode, resultCode, intent);
	}

//--------------------------------------------Database Updates-----------------------------------------------//	
	private void deleteItem(int position) {
		Cursor cursor = adapter.getCursor();
		cursor.moveToPosition(position);
		String date = cursor.getString(cursor
				.getColumnIndex(DatabaseHelper.I_DATE));
		 String table = DatabaseHelper.TABLE_INVENTORY;
		 String whereClause = DatabaseHelper.I_DATE + "=?";
		 String[] whereArgs = new String[]{date};
		 datasource.delete(this, table, whereClause, whereArgs);
    	updateCursor();
	}
	
//--------------------------------------------MySimpleCursorAdapter-----------------------------------------------//	

	private class MySimpleCursorAdapter extends SimpleCursorAdapter {

		public MySimpleCursorAdapter(Context context, int layout, Cursor c,
				String[] from, int[] to, int flags) {
			super(context, layout, c, from, to, flags);
		}

		public void bindView(View v, Context context, Cursor c) {
			String date = c.getString(c.getColumnIndex(DatabaseHelper.I_DATE));
			boolean isReceivedNull = c.isNull(c
					.getColumnIndex(DatabaseHelper.I_COUNT));
			Log.d("xxx",
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
		Intent intent = new Intent(this, MainActivity.class);
		startActivity(intent);
	}

}