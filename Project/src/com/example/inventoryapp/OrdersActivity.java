package com.example.inventoryapp;

//change edit date to edit order date, add edit order activity to context menu
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.AdapterView.AdapterContextMenuInfo;

public class OrdersActivity extends ListActivity{
	private static final int ACTIVITY_NEW = 1;
	private static final int ACTIVITY_EDIT = 2;
	private static final int ACTIVITY_OLD = 3;
	private static final int ACTIVITY_DATE_DIALOG = 4;
	private static final int ACTIVITY_DELETE_DIALOG = 5;

	private ItemsDataSource datasource;
	private ImportDatabase importer;
	private static final int DELETE_ID = Menu.FIRST + 1;
	private static final int EDIT_DATE = Menu.FIRST + 2;
	private static final int EDIT_ORDER = Menu.FIRST + 3;
	private static final int EXPORT_ORDER = Menu.FIRST + 4;

	List<Item> values;
	private MySimpleCursorAdapter adapter;
	
	//variable to keep track of which item actions are being performed on in dialog
	private int position;
	
	//variable for editing date
	String dateChosen;
	
	
//--------------------------------------------Create-----------------------------------------------//
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.d("xxx", "Orders Activity onCreate");
		super.onCreate(savedInstanceState);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		setContentView(R.layout.basic_list);
		importer = new ImportDatabase(this);
		datasource = new ItemsDataSource(this);
		datasource.database = importer.getWritableDatabase();
		fillData();
		registerForContextMenu(getListView());
		dateChosen = "";
	}
	
//--------------------------------------------Data-----------------------------------------------//

	private void fillData() {
		Cursor cursor = datasource.fetchOrderDates();
		Context context = this;
		int rowLayout = R.layout.row_checkmark;
		String[] fromColumns = {DatabaseHelper.O_RECEIVED_COUNT};	//overridden by mysimplecursoradapter
		int[] toViews = {R.id.label};								//but needs a value here
		int flags = 0;
		adapter = new MySimpleCursorAdapter(context, rowLayout, cursor,
				fromColumns, toViews, flags);
		setListAdapter(adapter);
	}
	
	private void updateCursor(){
	    Cursor cursor = datasource.fetchOrderDates();
	    adapter.changeCursor(cursor);
	}

//--------------------------------------------Options Menu-----------------------------------------------//	

	// Create the menu based on the XML definition
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.order_activity_actions, menu);
		return true;
	}

	// Reaction to the menu selection
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.neworder:
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
		menu.add(0, EDIT_ORDER, 0, R.string.menu_edit_ordered);
		menu.add(0, EXPORT_ORDER, 0, R.string.menu_export_odered);
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
		case EDIT_DATE:{
			Cursor cursor = adapter.getCursor();
			cursor.moveToPosition(position);
			String date = cursor.getString(cursor
					.getColumnIndex(DatabaseHelper.O_DATE));
			Intent i = new Intent(this, DateDialogActivity.class);
			i.putExtra("defaultDate", date);
			i.putExtra("isEditNotNew", true);
			startActivityForResult(i, ACTIVITY_DATE_DIALOG);
			return true;
		}
		case EDIT_ORDER:{
			Cursor cursor = adapter.getCursor();
			cursor.moveToPosition(position);
			String date = cursor.getString(cursor
					.getColumnIndex(DatabaseHelper.O_DATE));
        	goToEditNewActivity(date);
			return true;
		}
		case EXPORT_ORDER:{
			Cursor cursor = adapter.getCursor();
			cursor.moveToPosition(position);
			String date = cursor.getString(cursor.getColumnIndex(DatabaseHelper.O_DATE));
			String filename = date + ".csv";
			File file = null;
			//Get file Path
			File root = Environment.getExternalStorageDirectory();
			//Check to see if path exists and is writable
			if(root.canWrite()){
				//make a new file directory to store the orders
				File dir    =   new File (root.getAbsolutePath() + "/Orders");
			    dir.mkdirs();
			    //make the file that stores the order to be saved
			    file   =   new File(dir, filename);
			    //create the CSV and store it in the file
				datasource.createCSV(date, file);
				//store as an attachment and send email
				Intent i = new Intent(Intent.ACTION_SEND);
				i.putExtra(Intent.EXTRA_SUBJECT, "Order For " + date);
				i.putExtra(Intent.EXTRA_TEXT, "Comments");
				i.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
				i.setType("text/plain");
				startActivity(Intent.createChooser(i, "Your email id"));
			}
		}
		}
		return super.onContextItemSelected(item);
	}

//--------------------------------------------On Click-----------------------------------------------//	

	// Opens the second activity if an entry is clicked
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		Cursor cursor = adapter.getCursor();
		cursor.moveToPosition(position);
		String date = cursor.getString(cursor.getColumnIndex(DatabaseHelper.O_DATE));
		Intent i = new Intent(this, OrderOldActivity.class);
		i.putExtra("date", date);
		startActivityForResult(i, ACTIVITY_OLD);
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
				dateChosen = extras.getString("dateChosen");
				boolean isEditNotNew = extras.getBoolean("isEditNotNew");
				if (isEditNotNew && defaultDate != dateChosen) {//edits the date
					if (datasource.isOrderAlready(dateChosen)){/////////
						new AlertDialog.Builder(this)
			    	    .setMessage("There is already an order for " + ItemsDataSource.dateSqlToStd(dateChosen) + ". Choose another date.")
			    	    .show();
					} else {
						String table = DatabaseHelper.TABLE_ORDERS;
						ContentValues values = new ContentValues();
						values.put(DatabaseHelper.O_DATE, dateChosen);
						String whereClause = DatabaseHelper.O_DATE + " = ?";
						String[] whereArgs = new String[] { defaultDate };
						datasource.update(this, table, values, whereClause,
								whereArgs);
						updateCursor();
					}
				} else {//takes the date and goes to next activity
					if (datasource.isOrderAlready(dateChosen)){
						new AlertDialog.Builder(this)
//			    	    .setTitle("Editing Order")
			    	    .setMessage("There is already an order for " + ItemsDataSource.dateSqlToStd(dateChosen))
			    	    .setPositiveButton("Edit it", new DialogInterface.OnClickListener() {
			    	        public void onClick(DialogInterface dialog, int which) {
			    	        	goToEditNewActivity(dateChosen);
			    	        }
			    	     })
			    	    .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
			    	        public void onClick(DialogInterface dialog, int which) { 
			    	            // do nothing
			    	        }
			    	     })
			    	     .show();
					} else {
						Intent i = new Intent(this, OrderNewActivity.class);
						i.putExtra("date", dateChosen);
						i.putExtra("isNew", true);
						startActivityForResult(i, ACTIVITY_NEW);
					}
				}
			}
			break;
		case ACTIVITY_NEW:
			if (extras != null) {
				boolean update = extras.getBoolean("update");
				if (update) updateCursor();
			}
			break;
		case ACTIVITY_EDIT:
			if (extras != null) {
				boolean update = extras.getBoolean("update");
				if (update) updateCursor();
			}
			break;
		
		case ACTIVITY_OLD:
			if (extras != null) {
				boolean update = extras.getBoolean("update");
				if (update) updateCursor();
			}
			break;
		}
		super.onActivityResult(requestCode, resultCode, intent);
		
	}
	private void goToEditNewActivity(String dateChosen){
		Intent i = new Intent(this, OrderNewActivity.class);
		i.putExtra("date", dateChosen);
		i.putExtra("isNew", false);
		startActivityForResult(i, ACTIVITY_NEW);
	}

//--------------------------------------------Database Updates-----------------------------------------------//	
	private void deleteItem(int position) {
		Cursor cursor = adapter.getCursor();
		cursor.moveToPosition(position);
		String date = cursor.getString(cursor
				.getColumnIndex(DatabaseHelper.O_DATE));
		 String table = DatabaseHelper.TABLE_ORDERS;
		 String whereClause = DatabaseHelper.O_DATE + "=?";
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
			String date = c.getString(c.getColumnIndex(DatabaseHelper.O_DATE));
			boolean isReceivedNull = c.isNull(c
					.getColumnIndex(DatabaseHelper.O_RECEIVED_COUNT));
			Log.d("xxx",
					"isReceivedNull: "
							+ isReceivedNull
							+ " date: "
							+ date
							+ "getInt"
							+ c.getInt(c
									.getColumnIndex(DatabaseHelper.O_RECEIVED_COUNT)));
			TextView textView = (TextView) v.findViewById(R.id.label);
			if (textView != null) {
				textView.setText(ItemsDataSource.dateSqlToStd(date));
			}
			ImageView imageView = (ImageView) v.findViewById(R.id.icon);
			if (isReceivedNull) imageView.setVisibility(View.INVISIBLE);
			else imageView.setVisibility(View.VISIBLE);
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

}


			
