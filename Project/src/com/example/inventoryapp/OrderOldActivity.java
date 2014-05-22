package com.example.inventoryapp;


import android.os.Bundle;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.AdapterView.AdapterContextMenuInfo;

public class OrderOldActivity extends ListActivity {
	private static final int ACTIVITY_NEW = 1;
	private static final int ACTIVITY_DATE_DIALOG = 4;
	private static final int ACTIVITY_DELETE_DIALOG = 5;
	private static final int ACTIVITY_ADD_RECEIVED = 6;

	
	private static final int DELETE_ID = Menu.FIRST + 1;
	private static final int EDIT_ORDERED = Menu.FIRST + 2;

	private static final int VIEW_TYPE_GROUP_START = 0;
    private static final int VIEW_TYPE_GROUP_CONT = 1;
    private static final int VIEW_TYPE_COUNT = 2;
    
    private static String date;
    ListView listView;
    int itemidSelected;
    
	//variable to keep track of which item actions are being performed on in dialog
	private int contextPosition;

    //lower part
	TextView tvTitle, tvId, tvType, tvSupplier, tvMin, tvMax, tvInvOn, tvInvCount, tvOrderedOn, tvOrderedCount;
	
	private ItemsDataSource datasource;
	private ImportDatabase importer;
	private MySimpleCursorAdapter adapter;

//--------------------------------------------Create-----------------------------------------------//
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.d("xxx", "Old Orders Activity onCreate");
		super.onCreate(savedInstanceState);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

		setContentView(R.layout.list_lower_detail_plain);
		Bundle extras = getIntent().getExtras();
		if (extras != null) date = extras.getString("date");
		setTitle(ItemsDataSource.dateSqlToStd(date) + " Order");
		listView = (ListView) findViewById(android.R.id.list);
		itemidSelected = -1;
		
		//lower part
		tvTitle = (TextView) findViewById(R.id.itemtitle);
		tvId = (TextView) findViewById(R.id.text_id);
		tvType = (TextView) findViewById(R.id.text_type);
		tvSupplier = (TextView) findViewById(R.id.text_supplier);
		tvMin = (TextView) findViewById(R.id.text_min);
		tvMax = (TextView) findViewById(R.id.text_max);
		tvInvOn = (TextView) findViewById(R.id.text_last_inventory_date);
		tvInvCount = (TextView) findViewById(R.id.text_last_inventory_count);
		tvOrderedOn = (TextView) findViewById(R.id.text_last_order_date);
		tvOrderedCount = (TextView) findViewById(R.id.text_last_order_count);
	
		importer = new ImportDatabase(this);
		datasource = new ItemsDataSource(this);
		datasource.database = importer.getWritableDatabase();
		registerForContextMenu(getListView());
		fillData();
	}
	
//--------------------------------------------Data-----------------------------------------------//

	private void fillData() {
		Cursor cursor = datasource.fetchOrdersWithDate(date);
		Context context = this;
		int rowLayout = R.layout.row_old_orders_without_header;
		String[] fromColumns = {DatabaseHelper.SI_ITEM_ID};
		int[] toViews = {R.id.label};
		int flags = 0;
		adapter = new MySimpleCursorAdapter(context, rowLayout, cursor,
				fromColumns, toViews, flags);
		setListAdapter(adapter);
	}
	
	private void updateCursor(){
	    Cursor cursor = datasource.fetchOrdersWithDate(date);
	    adapter.changeCursor(cursor);
	}

//--------------------------------------------Options Menu-----------------------------------------------//	

		// Create the menu based on the XML definition
		@Override
		public boolean onCreateOptionsMenu(Menu menu) {
			MenuInflater inflater = getMenuInflater();
			inflater.inflate(R.menu.order_old_option_menu, menu);
			return true;
		}

		// Reaction to the menu selection
		@Override
		public boolean onOptionsItemSelected(MenuItem item) {
			switch (item.getItemId()) {
			case R.id.enter_received:
				Intent i = new Intent(this, DateDialogActivity.class);
				i.putExtra("title", "Date Received");
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
			menu.add(0, EDIT_ORDERED, 0, R.string.menu_edit_ordered);
		}

		@Override
		public boolean onContextItemSelected(MenuItem item) {//////listview selectrow? menu.row? list to update
			AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo(); 
			contextPosition = (int) info.position;
			switch (item.getItemId()) {
			case DELETE_ID:
				//create an alert dialog for a delete confirmation
	    		new AlertDialog.Builder(this)
	    	    .setTitle("Delete entry")
	    	    .setMessage("Are you sure you want to delete this entry?")
	    	    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
	    	        public void onClick(DialogInterface dialog, int which) {
	    	        	//intermediary because of scope
	    	        	deleteItem(contextPosition);
	    	        }
	    	     })
	    	    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
	    	        public void onClick(DialogInterface dialog, int which) { 
	    	            // do nothing
	    	        }
	    	     })
	    	     .show();
				return true;
			case EDIT_ORDERED:
				goToEditNewActivity(date);
				return true;
			}

				
			return super.onContextItemSelected(item);
		}
	
//--------------------------------------------On Click-----------------------------------------------//	

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		Log.d("xxx", "onListItemClick");
		//currently selected
		Cursor cursor = adapter.getCursor();
		cursor.moveToPosition(position);
		int itemid = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.SI_ITEM_ID));
		String itemName = cursor.getString(cursor.getColumnIndex(DatabaseHelper.SI_NAME));
		String itemType = cursor.getString(cursor.getColumnIndex(DatabaseHelper.SI_TYPE));
		String itemSupplier = cursor.getString(cursor.getColumnIndex(DatabaseHelper.SI_SUPPLIER));
		float itemMin = cursor.getFloat(cursor.getColumnIndex(DatabaseHelper.SI_MIN));
		float itemMax = cursor.getFloat(cursor.getColumnIndex(DatabaseHelper.SI_MAX));
		String itemLastInvDate = cursor.getString(cursor.getColumnIndex(DatabaseHelper.AS_LAST_I_DATE));
		float itemLastInvCount = cursor.getFloat(cursor.getColumnIndex(DatabaseHelper.AS_LAST_I_COUNT));
		String itemLastOrderDate = cursor.getString(cursor.getColumnIndex(DatabaseHelper.AS_LAST_O_DATE));
		float itemLastOrderCount = cursor.getFloat(cursor.getColumnIndex(DatabaseHelper.AS_LAST_O_COUNT));

		//set the text on the lower view
		itemidSelected = itemid;
		tvTitle.setText(itemName);
		tvId.setText(String.valueOf(itemid));
		tvType.setText(itemType);
		tvSupplier.setText(itemSupplier);
		tvMin.setText(String.valueOf(itemMin));
		tvMax.setText(String.valueOf(itemMax));
		if (itemLastInvDate != null && itemLastInvDate.length() > 0) itemLastInvDate = ItemsDataSource.dateSqlToStd(itemLastInvDate);
		tvInvOn.setText(itemLastInvDate);
		if (itemLastInvCount > 0) tvInvCount.setText(String.valueOf(itemLastInvCount));
		else tvInvCount.setText("");
		if (itemLastOrderDate != null && itemLastOrderDate.length() > 0) itemLastOrderDate = ItemsDataSource.dateSqlToStd(itemLastOrderDate);
		tvOrderedOn.setText(itemLastOrderDate);
		if (itemLastOrderCount > 0) tvOrderedCount.setText(String.valueOf(itemLastOrderCount));
		else tvOrderedCount.setText("");

		super.onListItemClick(l, v, position, id);	
	}
	
//--------------------------------------------Activity Result-----------------------------------------------//	

	@Override
	protected void onActivityResult(int requestCode, int resultCode,
			Intent intent) {
		Bundle extras = intent.getExtras();
		switch (requestCode) {
		case ACTIVITY_DATE_DIALOG:
			if (extras != null) {
				String dateChosen = extras.getString("dateChosen");
				Intent i = new Intent(this, OrderReceivedActivity.class);
				i.putExtra("dateOrdered", date);
				i.putExtra("dateReceived", dateChosen);
				startActivityForResult(i, ACTIVITY_ADD_RECEIVED);
			}
			break;
		case ACTIVITY_ADD_RECEIVED:
		case ACTIVITY_NEW:
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
	
	private void goToEditReceived(String dateChosen){
		Intent i = new Intent(this, OrderReceivedActivity.class);
		i.putExtra("dateOrdered", date);
		i.putExtra("dateReceived", date);
		startActivityForResult(i, ACTIVITY_ADD_RECEIVED);

	}
//--------------------------------------------Database Updates-----------------------------------------------//	

	private void deleteItem(int position) {
		Cursor cursor = adapter.getCursor();
		cursor.moveToPosition(position);
		int itemid = cursor.getInt(cursor
				.getColumnIndex(DatabaseHelper.O_ITEM_ID));
		String table = DatabaseHelper.TABLE_ORDERS;
		String whereClause = DatabaseHelper.O_ITEM_ID + " = ? AND " + DatabaseHelper.O_DATE + " = ?";
		String[] whereArgs = new String[] {String.valueOf(itemid), date};
		datasource.delete(this, table, whereClause, whereArgs);
		updateCursor();
	}
	
	private int getPositionForId(int itemid){
		Log.d("xxx", "getPositionForId");
		int position = -1;
	    for (int i = 0; i < adapter.getCount(); i++)
	    {
	    	Cursor cursor = adapter.getCursor();
	    	cursor.moveToFirst();
	    	do {
				int id = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.O_ITEM_ID));
				if (id == itemid) {
					position = cursor.getPosition();
					break;
				}
	    	} while (cursor.moveToNext());
	    }
	    return position;	
	}

//--------------------------------------------MySimpleCursorAdapter-----------------------------------------------//	

	private class MySimpleCursorAdapter extends SimpleCursorAdapter {

		public MySimpleCursorAdapter(Context context, int layout, Cursor c,
				String[] from, int[] to, int flags) {
			super(context, layout, c, from, to, flags);
		}

		public void bindView(View v, Context context, Cursor c) {
			int id = c.getInt(c.getColumnIndex(DatabaseHelper.SI_ITEM_ID));
			String itemId = String.valueOf(id);
			TextView textView = (TextView) v.findViewById(R.id.item_id);
			if (textView != null) textView.setText(itemId);
			
			String itemName = c.getString(c.getColumnIndex(DatabaseHelper.SI_NAME));
			textView = (TextView) v.findViewById(R.id.item_name);
			if (textView != null) textView.setText(itemName);
			
			float orderCount = c.getFloat(c.getColumnIndex(DatabaseHelper.O_COUNT));
			String orderCountString = String.valueOf(orderCount);
			textView = (TextView) v.findViewById(R.id.order_count);
			if (textView != null) textView.setText(orderCountString);

			float recCount = c.getFloat(c.getColumnIndex(DatabaseHelper.O_RECEIVED_COUNT));
			String recCountString = String.valueOf(recCount);
			if (recCount<=0) recCountString = "---";
			textView = (TextView) v.findViewById(R.id.order_received_count);
			if (textView != null) textView.setText(recCountString);
			
			String recDate = c.getString(c.getColumnIndex(DatabaseHelper.O_RECEIVED_DATE));
			if (recDate == null) recDate = "---";
			else recDate = ItemsDataSource.dateSqlToStd(recDate);
			textView = (TextView) v.findViewById(R.id.order_received_date);
			if (textView != null) textView.setText(recDate);
			
			//set the text in the header
			textView = (TextView) v.findViewById(R.id.header);
			if (textView != null) {
				String supplierName = c.getString(c.getColumnIndex(DatabaseHelper.SI_SUPPLIER));
				textView.setText(supplierName);
				
				textView = (TextView) v.findViewById(R.id.item_id_col);
				if (textView != null) textView.setText(R.string.col_id);
				
				textView = (TextView) v.findViewById(R.id.item_name_col);
				if (textView != null) textView.setText(R.string.col_name);
				
				textView = (TextView) v.findViewById(R.id.order_count_col);
				if (textView != null) textView.setText(R.string.col_ordered);
				
				textView = (TextView) v.findViewById(R.id.order_received_count_col);
				if (textView != null) textView.setText(R.string.col_received);
				
				textView = (TextView) v.findViewById(R.id.order_received_date_col);
				if (textView != null) textView.setText(R.string.col_on_date);
			}
		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
            int position = cursor.getPosition();
            int viewType;
 
            if (position == 0) {
                // Group header for position 0
            	viewType = VIEW_TYPE_GROUP_START;
            } else {
                // For other positions, decide based on data
                boolean newGroup = isNewGroup(cursor, position);
                if (newGroup) {
                	viewType = VIEW_TYPE_GROUP_START;
                } else {
                	viewType = VIEW_TYPE_GROUP_CONT;
                }
            }
            View v;
            LayoutInflater inflater = LayoutInflater.from(context);
            
            if (viewType == VIEW_TYPE_GROUP_START) {
                // Inflate a layout to start a new group
                v = inflater.inflate(R.layout.row_old_orders_with_header, parent, false);
            } else {
                // Inflate a layout for "regular" items
                v = inflater.inflate(R.layout.row_old_orders_without_header, parent, false);
            }
            return v;
		}

		@Override
		public int getItemViewType(int position) {
            // There is always a group header for the first data item
            if (position == 0) {
                return VIEW_TYPE_GROUP_START;
            }
 
            // For other items, decide based on current data
            Cursor cursor = getCursor();
            cursor.moveToPosition(position);
            boolean newGroup = isNewGroup(cursor, position);
 
            // Check item grouping
            if (newGroup) {
                return VIEW_TYPE_GROUP_START;
            } else {
                return VIEW_TYPE_GROUP_CONT;
            }
		}

		@Override
		public int getViewTypeCount() {
			return VIEW_TYPE_COUNT;
		}
		
		private boolean isNewGroup(Cursor cursor, int position) {
            // Get date values for current and previous data items
            String supplier = cursor.getString(cursor
				.getColumnIndex(DatabaseHelper.SI_SUPPLIER));
            
            for(int i = position-1; i>-1; i--){
                cursor.moveToPosition(i);
                String previousSupplier = cursor.getString(cursor
        				.getColumnIndex(DatabaseHelper.SI_SUPPLIER));
            
                // Restore cursor position
                cursor.moveToPosition(position);
                // return true if its a new supplier
                Log.d("xxx", "supplier: " + supplier + " previousSupplier: " + previousSupplier);
                if (supplier.equals(previousSupplier)) return false;
            }
            return true;
        }
	}
	
//--------------------------------------------Life Cycle-----------------------------------------------//	

	@Override
	public void onBackPressed() {
		Log.d("xxx", "onBackPressed Called");
		Intent i = new Intent();
		i.putExtra("update", true);//////////
		setResult(RESULT_OK, i);
		finish();
	}
	
	private void startDeleteDialog(){
		Intent i = new Intent(this, DeleteDialogActivity.class);
    	startActivityForResult(i, ACTIVITY_DELETE_DIALOG);
	}
	
//--------------------------------------------Bottom half below here-----------------------------------------------//	

}