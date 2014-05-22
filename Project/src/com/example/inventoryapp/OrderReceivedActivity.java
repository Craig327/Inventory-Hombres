package com.example.inventoryapp;

import java.util.List;


import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.util.SparseArray;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.AdapterView.AdapterContextMenuInfo;


public class OrderReceivedActivity extends ListActivity {
	private static final int ACTIVITY_DELETE_DIALOG = 5;
	private static final int ACTIVITY_DATE_DIALOG = 4;

	private static final int EDIT_RECEIVED_DATE = Menu.FIRST + 1;

	private static final int VIEW_TYPE_GROUP_START = 0;
    private static final int VIEW_TYPE_GROUP_CONT = 1;
    private static final int VIEW_TYPE_COUNT = 2;
    private class ToUpdate{
    	String date = "";
    	float numReceived = 0;
    	boolean isChecked = false;
    	ToUpdate(String date, float numReceived, boolean isChecked){
    		this.date = date;
    		this.numReceived = numReceived;
    		this.isChecked = isChecked;
    	}
    }
    
    //stores order data to save at end
    SparseArray<ToUpdate> toMarkReceived = new SparseArray<ToUpdate>();
    private static String dateOrdered;
    private static String dateReceived;
    
    ListView listView;
    int itemidSelected;
    int itemidToEditReceivedDate;

    //used if editing, stores previous data
    SparseArray<ToUpdate> alreadyReceived = new SparseArray<ToUpdate>();
    
    //lower part
	LinearLayout countBoxLayout;
	TextView tvTitle, tvId, tvType, tvSupplier, tvMin, tvMax, tvInvOn, tvInvCount, tvOrderedOn, tvOrderedCount, etCount;
	String memento;
	
	private ItemsDataSource datasource;
	private ImportDatabase importer;
	
	List<Item> values;
	private MySimpleCursorAdapter adapter;

//--------------------------------------------Create-----------------------------------------------//
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.d("xxx", "New Orders Activity onCreate");
		super.onCreate(savedInstanceState);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

		setContentView(R.layout.list_lower_detail);
		Bundle extras = getIntent().getExtras();
		if (extras != null){
			dateOrdered = extras.getString("dateOrdered");
			dateReceived = extras.getString("dateReceived");
		}
		Log.d("xxx", "dateOrdered: " + dateOrdered + "dateReceived: " + dateReceived);
		
		setTitle("Entering Received on " + ItemsDataSource.dateSqlToStd(dateReceived));
		listView = (ListView) findViewById(android.R.id.list);
		itemidSelected = -1;
		itemidToEditReceivedDate = -1;
		
		//lower part
		tvTitle = (TextView) findViewById(R.id.itemtitle);
//		etCount =	(EditText) findViewById(R.id.countbox);
		etCount =	(TextView) findViewById(R.id.countbox);
		countBoxLayout = (LinearLayout)this.findViewById(R.id.countlayout);
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
		
		fillData();
		registerForContextMenu(getListView());
	}
	
//--------------------------------------------Data-----------------------------------------------//

	private void fillData() {
		Cursor cursor = datasource.fetchOrdersWithDate(dateOrdered);
		Context context = this;
		int rowLayout = R.layout.row_received_orders_without_header;
		String[] fromColumns = { DatabaseHelper.SI_ITEM_ID };
		int[] toViews = { R.id.label };
		int flags = 0;
		adapter = new MySimpleCursorAdapter(context, rowLayout, cursor,
				fromColumns, toViews, flags);
		setListAdapter(adapter);
		Log.d("xxx", "Data Filled");
	}
	private void updateCursor(){
	    Cursor cursor = datasource.fetchOrdersWithDate(dateOrdered);
	    adapter.changeCursor(cursor);
	}
		
//--------------------------------------------Context Menu-----------------------------------------------//	
		
			@Override
			public void onCreateContextMenu(ContextMenu menu, View v,
					ContextMenuInfo menuInfo) {
				super.onCreateContextMenu(menu, v, menuInfo);
				menu.add(0, EDIT_RECEIVED_DATE, 0, R.string.menu_edit_received_date);
			}

			@Override
			public boolean onContextItemSelected(MenuItem item) {
				AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo(); 
				int position = (int) info.position;
				switch (item.getItemId()) {
				case EDIT_RECEIVED_DATE:{
					Cursor cursor = adapter.getCursor();
					cursor.moveToPosition(position);
//					String date = cursor.getString(cursor
//							.getColumnIndex(DatabaseHelper.O_RECEIVED_DATE));
					int itemid = cursor.getInt(cursor
							.getColumnIndex(DatabaseHelper.O_ITEM_ID));
					String date = toMarkReceived.get(itemid).date;
					if (date != null && date.length() > 0){//change this so that it uses toMarkReceived.get(itemid).date instead 
						itemidToEditReceivedDate = itemid;
						Intent i = new Intent(this, DateDialogActivity.class);
						i.putExtra("defaultDate", date);
						i.putExtra("isEditNotNew", true);
						startActivityForResult(i, ACTIVITY_DATE_DIALOG);
						return true;
					}
					else{
						new AlertDialog.Builder(this)
			    	    .setMessage("This date needs to be saved first")
						.show();
					}
				}	
				}
				return super.onContextItemSelected(item);
			}
//--------------------------------------------On Click-----------------------------------------------//	

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		Log.d("xxx", "onListItemClick");

		addToMarkReceived();

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
		Log.d("xxx", "onListItemClick");

		//set the text on the lower view
		itemidSelected = itemid;
		tvTitle.setText(itemName);
		etCount.setText("");
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
		Log.d("xxx", "onListItemClick");

		Log.d("xxx", "itemid: " + itemid);		
		super.onListItemClick(l, v, position, id);		
	}
	private void addToMarkReceived(){
		//if previous selection, save numReceived to toMarkReceived
		if (itemidSelected > 0) {
			Cursor cursor = adapter.getCursor();
			cursor.moveToPosition(getPositionForId(itemidSelected));

			int lastItemid = cursor.getInt(cursor
					.getColumnIndex(DatabaseHelper.SI_ITEM_ID));

			Log.d("xxx", "lastItemid: " + lastItemid);
			String text = etCount.getText().toString();
			float toCount = -1;
			if (text.length() > 0) {
				try {
					toCount = Float.valueOf(text);
				} catch (NumberFormatException e) {
					e.printStackTrace();
				}
			}
			Log.d("xxx", "toCount: " + toCount);
			if (toCount > 0){
				toMarkReceived.get(lastItemid).numReceived = toCount;
				checkmarkRowWithIdAndUpdateCount(lastItemid, true);
			} else if (toCount == 0){
				checkmarkRowWithIdAndUpdateCount(lastItemid, false);
			}
		}
		Log.d("xxx", "onListItemClick");
	}
	
//--------------------------------------------Activity Result-----------------------------------------------//	

	@Override
	protected void onActivityResult(int requestCode, int resultCode,
			Intent intent) {
		Bundle extras = intent.getExtras();
		switch (requestCode) {
		case ACTIVITY_DELETE_DIALOG:
			if (extras != null) {
				boolean isDelete = extras.getBoolean("isDelete");
				if (isDelete) {
					Intent i = new Intent();
					setResult(RESULT_OK, i);
				}
				finish();
			}
			break;
		case ACTIVITY_DATE_DIALOG:{
			if (extras != null) {
				String defaultDate = extras.getString("defaultDate");
				String dateChosen = extras.getString("dateChosen");
				Log.d("xxx", "!!!!!!!! " + defaultDate + " " + dateChosen);

				boolean isEditNotNew = extras.getBoolean("isEditNotNew");
				if (isEditNotNew && defaultDate != dateChosen) {
					String table = DatabaseHelper.TABLE_ORDERS;
					ContentValues values = new ContentValues();
					values.put(DatabaseHelper.O_RECEIVED_DATE, dateChosen);
					String whereClause = DatabaseHelper.O_ITEM_ID + " = ? AND " + DatabaseHelper.O_DATE + " = ?";
					String[] whereArgs = new String[] {String.valueOf(itemidToEditReceivedDate), dateOrdered };
					Log.d("xxx", "table: " + table + "values: " + values + "whereClause" + whereClause + "whereArgs: " + whereArgs[0] + " " + whereArgs[1]);

					int ii = datasource.update(this, table, values, whereClause, whereArgs);
					Log.d("xxx", "ii: " + ii);
					if (ii == 1) toMarkReceived.get(itemidToEditReceivedDate).date = dateChosen;
					updateCursor();
					itemidToEditReceivedDate = -1;
				}
			}
			break;}
		}
	}
			
//--------------------------------------------Database Updates-----------------------------------------------//	
	private void deleteUnchecked(){
		Log.d("xxx", "deleteUnChecked");
		for(int i = 0; i < alreadyReceived.size(); i++) {
			int key = alreadyReceived.keyAt(i);
			ToUpdate pair = toMarkReceived.get(key);
			if (!pair.isChecked){
				int id = key;
				Log.d("xxx", "id: " + id);
				float count = pair.numReceived;
				String table = DatabaseHelper.TABLE_ORDERS;
				ContentValues values = new ContentValues();
				values.putNull(DatabaseHelper.O_RECEIVED_DATE);
				values.putNull(DatabaseHelper.O_RECEIVED_COUNT);
				String whereClause = DatabaseHelper.O_ITEM_ID + " = ? AND "
						+ DatabaseHelper.O_DATE + " = ?";
				String[] whereArgs = new String[] {String.valueOf(id), String.valueOf(dateOrdered)};
				datasource.update(this, table, values, whereClause, whereArgs);
			}
		}
	}
	private void updateWithReceived(){
		Log.d("xxx", "updateWithReceived");
		for(int i = 0; i < toMarkReceived.size(); i++) {
			int key = toMarkReceived.keyAt(i);
			// get the object by the key.
			ToUpdate pair = toMarkReceived.get(key);
			ToUpdate oldPair = alreadyReceived.get(key);
			Log.d("xxx", "updateWithReceived");

			Float oldOrder = (float)-1;
			String oldDate = "";
			if (oldPair != null){
				oldOrder = oldPair.numReceived;
				oldDate = oldPair.date;
			}

			String date = pair.date;
			Log.d("xxx", "updateWithReceived");

			Log.d("xxx", "key: " + key + "isChecked: " + pair.isChecked + "pair.numReceived" + pair.numReceived + "oldOrder" + oldOrder);
			if (pair.isChecked && (pair.numReceived != oldOrder ||pair.date != oldDate)) {
				int id = key;
				Log.d("xxx", "id: " + id);
				float count = pair.numReceived;
				String table = DatabaseHelper.TABLE_ORDERS;
				ContentValues values = new ContentValues();
//				if (date == null || date.length() == 0) values.put(DatabaseHelper.O_RECEIVED_DATE, dateReceived);
//				if (date != null && date.length() > 0) values.put(DatabaseHelper.O_RECEIVED_DATE, date);
//				else values.put(DatabaseHelper.O_RECEIVED_DATE, dateReceived);	
				values.put(DatabaseHelper.O_RECEIVED_DATE, date);
				values.put(DatabaseHelper.O_RECEIVED_COUNT, count);
				String whereClause = DatabaseHelper.O_ITEM_ID + " = ? AND "
						+ DatabaseHelper.O_DATE + " = ?";
				String[] whereArgs = new String[] {String.valueOf(id), String.valueOf(dateOrdered)};
				Log.d("xxx", "table: " + table + " values: " + values + " whereClause: " + whereClause + " whereArgs1: " + whereArgs[0]  + whereClause + " whereArgs2: " + whereArgs[1]);
				Log.d("xxx", "id: " + String.valueOf(id) + " dateOrdered: " + dateOrdered);
				Log.d("xxx", "count: " + count);
				datasource.update(this, table, values, whereClause, whereArgs);
			}
		}
		deleteUnchecked();
	}

	private boolean existsReceivedToSave(){
		Log.d("xxx", "existsReceivedToSave: ");
		boolean isSave = false;
		
		//check for isChecked
		for(int i = 0; i < toMarkReceived.size(); i++) {
			int key = toMarkReceived.keyAt(i);
			ToUpdate pair = toMarkReceived.get(key);
			ToUpdate oldPair = alreadyReceived.get(key);
			if (oldPair == null){
				if (pair.isChecked){
					isSave = true;
					break;
			    }
			}
			else{
			    Float oldNum = oldPair.numReceived;
			    String oldDate = oldPair.date;
			    if (oldNum == null) oldNum = (float) -1;
			    if (pair.isChecked && (pair.numReceived != oldNum || pair.date != oldDate)){
					isSave = true;
					break;
			    }
			}

		}

		//check if anything to delete if editing
		if (isSave == false){
			for(int i = 0; i < alreadyReceived.size(); i++) {
				int key = alreadyReceived.keyAt(i);
				if (!toMarkReceived.get(key).isChecked){
					 isSave = true;
					 break;
				}
			}
		}
		return isSave;
	}
	
	private int getPositionForId(int itemid){
		Log.d("xxx", "getPositionForId");
		int position = -1;
		Log.d("xxx", "1 getPositionForId");
	    for (int i = 0; i < adapter.getCount(); i++)
	    {
			Log.d("xxx", "2 getPositionForId");

	    	Cursor cursor = adapter.getCursor();
			Log.d("xxx", "3 getPositionForId");

	    	cursor.moveToFirst();
			Log.d("xxx", "4 getPositionForId");

	    	do {
	    		Log.d("xxx", "5 getPositionForId");

				int id = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.O_ITEM_ID));
				Log.d("xxx", "6 getPositionForId");
				if (id == itemid) {
					Log.d("xxx", "7 getPositionForId");

					position = cursor.getPosition();
					Log.d("xxx", "count: " + cursor.getCount());

					Log.d("xxx", "8 getPositionForId position: " + position);

					break;
				}
	    	} while (cursor.moveToNext());
	    }
	    return position;	
	}
	private void checkmarkRowWithIdAndUpdateCount(int id, boolean isChecked)
	{
		Log.d("xxx", "checkmarkRowWithIdAndUpdateCount");
	    int count = listView.getChildCount();
	    for (int i = 0; i < count; i++)
	    {
	        View v = listView.getChildAt(i);
	        if ((Integer)v.getTag() == id)
	        {
	        	float numReceived = toMarkReceived.get(id).numReceived;
	        	String date = toMarkReceived.get(id).date;
        		CheckBox check = (CheckBox) v.findViewById(R.id.check);
	    		TextView tvCount = (TextView) v.findViewById(R.id.order_received);
	    		TextView tvDate = (TextView) v.findViewById(R.id.order_received_on);
	        	if (isChecked){
		        	check.setChecked(true);
		    		tvCount.setText(Float.toString(numReceived));
		    		tvDate.setText(ItemsDataSource.dateSqlToStd(date));
	        	}
	        	else{
	        		check.setChecked(false);
		    		tvCount.setText("---");
		    		tvDate.setText("---");
	        	}
	        	
	        }
	    }
	}

//--------------------------------------------MySimpleCursorAdapter-----------------------------------------------//	

	private class MySimpleCursorAdapter extends SimpleCursorAdapter {

		public MySimpleCursorAdapter(Context context, int layout, Cursor c,
				String[] from, int[] to, int flags) {
			super(context, layout, c, from, to, flags);
		}
		
		private boolean updateNumToOrderWithId(int id)
		{
			boolean success = true;
			Log.d("xxx", "updateNumToOrderWithId");
		    int count = listView.getChildCount();
		    for (int i = 0; i < count; i++)
		    {
		        View v = listView.getChildAt(i);
		        if ((Integer)v.getTag() == id)
		        {
		        	String receivedString = "---";
		        	String dateString = "---";
		        	Log.d("xxx", "toMarkReceived.get(id).isChecked" + toMarkReceived.get(id).isChecked);
		        	if (toMarkReceived.get(id).isChecked){
		        		float numReceived = toMarkReceived.get(id).numReceived;
			        	Log.d("xxx", "numReceived" + numReceived);
		        		if (numReceived <= 0) {
		        			success = false;
		        			break;
		        		}
		        		else{
		        			receivedString = String.valueOf(numReceived);
		        			dateString = toMarkReceived.get(id).date;
		        		}
		        	}
		        	TextView text = (TextView) v.findViewById(R.id.order_received);
		            text.setText(receivedString);
		            text = (TextView) v.findViewById(R.id.order_received_on);
		            text.setText(ItemsDataSource.dateSqlToStd(dateString));
		        }
		    }
		    return success;
		}

		public void bindView(View v, Context context, Cursor c) {
			Log.d("xxx", "bindView");
			int id = c.getInt(c.getColumnIndex(DatabaseHelper.SI_ITEM_ID));
			float ordered = c.getFloat(c.getColumnIndex(DatabaseHelper.O_COUNT));
			float received = c.getFloat(c.getColumnIndex(DatabaseHelper.O_RECEIVED_COUNT));
			String date = c.getString(c.getColumnIndex(DatabaseHelper.O_RECEIVED_DATE));
			if (date == null || date.length() == 0) date = dateReceived;
			if (toMarkReceived.get(id) == null){////////
				ToUpdate pair;
				if (received <= 0){
					pair = new ToUpdate(date, ordered, false);
				}
				else{
					pair = new ToUpdate(date, received, true);
					ToUpdate oldPair = new ToUpdate(date, received, false);
					alreadyReceived.put(id, oldPair);
				}
				toMarkReceived.put(id, pair);
			}
						
			v.setTag(Integer.valueOf(id));
			CheckBox checkBox = (CheckBox) v.findViewById(R.id.check);
			checkBox.setTag(Integer.valueOf(id));
			checkBox.setChecked(toMarkReceived.get(id).isChecked);
			checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton buttonView,
						boolean isChecked) {
					Integer id = (Integer)buttonView.getTag();
					if (isChecked){
						toMarkReceived.get(id).isChecked = true;
					} else {
						toMarkReceived.get(id).isChecked = false;
					}
					if (!updateNumToOrderWithId(id)){
						buttonView.setChecked(false);
						toMarkReceived.get(id).isChecked = !toMarkReceived.get(id).isChecked;//or set to false?
					}
				}
			});
			
			String receivedCount = "---";
			ToUpdate toUpdate = toMarkReceived.get(Integer.valueOf(id));
			if (toUpdate != null && toUpdate.isChecked) receivedCount = String.valueOf(toUpdate.numReceived);
			else if (received > 0) receivedCount = String.valueOf(received);
			TextView textView = (TextView) v.findViewById(R.id.order_received);
			if (textView != null) textView.setText(receivedCount);
			
			String receivedOn = c.getString(c.getColumnIndex(DatabaseHelper.O_RECEIVED_DATE));
			if (toUpdate != null && toUpdate.isChecked == true && toUpdate.date.length() >= 0) 
				receivedOn = ItemsDataSource.dateSqlToStd(toUpdate.date);
			else if (receivedOn == null) receivedOn = "---";
			textView = (TextView) v.findViewById(R.id.order_received_on);
			if (textView != null) textView.setText(receivedOn);
			

			String orderCount = String.valueOf(ordered);
			textView = (TextView) v.findViewById(R.id.order_count);
			if (textView != null) textView.setText(orderCount);
			
			String itemId = c.getString(c.getColumnIndex(DatabaseHelper.SI_ITEM_ID));
			textView = (TextView) v.findViewById(R.id.item_id);
			if (textView != null) textView.setText(itemId);
			
			String itemName = c.getString(c.getColumnIndex(DatabaseHelper.SI_NAME));
			textView = (TextView) v.findViewById(R.id.item_name);
			if (textView != null) textView.setText(itemName);
			
			//set the text in the header
			textView = (TextView) v.findViewById(R.id.header);
			if (textView != null) {
				String supplierName = c.getString(c.getColumnIndex(DatabaseHelper.SI_SUPPLIER));
				textView.setText(supplierName);
				
				textView = (TextView) v.findViewById(R.id.order_received_col);
				if (textView != null) textView.setText("received");
				
				textView = (TextView) v.findViewById(R.id.order_received_on_col);
				if (textView != null) textView.setText("received on");
				
				textView = (TextView) v.findViewById(R.id.order_count_col);
				if (textView != null) textView.setText("ordered");
				
				textView = (TextView) v.findViewById(R.id.item_id_col);
				if (textView != null) textView.setText(R.string.col_id);
				
				textView = (TextView) v.findViewById(R.id.item_name_col);
				if (textView != null) textView.setText(R.string.col_name);
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
                v = inflater.inflate(R.layout.row_received_orders_with_header, parent, false);
            } else {
                // Inflate a layout for "regular" items
                v = inflater.inflate(R.layout.row_received_orders_without_header, parent, false);
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
			
            // Get dateOrdered values for current and previous data items
            String supplier = cursor.getString(cursor
				.getColumnIndex(DatabaseHelper.SI_SUPPLIER));
            
            for(int i = position-1; i>-1; i--){
            	cursor.moveToPosition(i);
            	String previousSupplier = cursor.getString(cursor
            			.getColumnIndex(DatabaseHelper.SI_SUPPLIER)); 
            
            	// Restore cursor position
            	cursor.moveToPosition(position);
            	
            	// return true if its a new supplier
            	if (supplier.equals(previousSupplier)) return false;
            }
            return true;
        }
	}
	
//--------------------------------------------Life Cycle-----------------------------------------------//	

	@Override
	public void onBackPressed() {
		Log.d("xxx", "onBackPressed Called");
		addToMarkReceived();
		if (existsReceivedToSave()){
			new AlertDialog.Builder(this)
			.setPositiveButton(R.string.btn_save, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
								Log.d("xxx", "setPositiveButton onClick: ");
								updateWithReceived();
								Log.d("xxx", "setPositiveButton onClick: ");

								Intent i = new Intent();
								i.putExtra("update", true);
								setResult(RESULT_OK, i);
								finish();	
				}
			})
			.setNegativeButton(R.string.btn_dont_save, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) { 
					startDeleteDialog();
	    			}
			})
			.show();
		}
		else{
			Intent i = new Intent();
			setResult(RESULT_OK, i);
			finish();
		}
	}
	
	private void startDeleteDialog(){
		Intent i = new Intent(this, DeleteDialogActivity.class);
    	startActivityForResult(i, ACTIVITY_DELETE_DIALOG);
	}
	
//--------------------------------------------Bottom half-----------------------------------------------//	


//--------------------------------------------Count-----------------------------------------------//	

	public void clearCount(View view) {
		memento = (String) etCount.getText();
		etCount.setText("");
		if (itemidSelected >=0){
			TextView text = (TextView) getSelectedViewToEdit().findViewById(R.id.order_received);
            text.setText("---");
		}
	}

	public void restorevalue(View view) {
		if (itemidSelected >=0){
			ToUpdate toUpdate = toMarkReceived.get(itemidSelected);
			float numReceived = 0;
			if (toUpdate != null) numReceived = toUpdate.numReceived;
			if (numReceived != 0) memento = String.valueOf(numReceived);
			TextView text = (TextView) getSelectedViewToEdit().findViewById(R.id.order_received);
            text.setText(memento);
            etCount.setText(memento);
		}
	}

	public void setCount(View view) {
		if (itemidSelected >= 0) {
			String content = (String) etCount.getText();
			switch (view.getId()) {
			case R.id.decimal:
				content += '.';
				break;
			case R.id.one:
				content += '1';
				break;
			case R.id.two:
				content += '2';
				break;
			case R.id.three:
				content += '3';
				break;
			case R.id.four:
				content += '4';
				break;
			case R.id.five:
				content += '5';
				break;
			case R.id.six:
				content += '6';
				break;
			case R.id.seven:
				content += '7';
				break;
			case R.id.eight:
				content += '8';
				break;
			case R.id.nine:
				content += '9';
				break;
			case R.id.zero:
				content += '0';
				break;
			}
			TextView text = (TextView) getSelectedViewToEdit().findViewById(R.id.order_received);
            text.setText(content);
            etCount.setText(content);
			
			
			
		}
	}
	private View getSelectedViewToEdit(){
		View returnView = null;
	    int count = listView.getChildCount();
	    for (int i = 0; i < count; i++)
	    {
	        View v = listView.getChildAt(i);
	        if ((Integer)v.getTag() == itemidSelected)
	        {
	        	returnView = v;
	        	break;
	        }
	    }
	    return returnView;
	}
}