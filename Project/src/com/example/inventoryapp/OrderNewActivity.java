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
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;


public class OrderNewActivity extends ListActivity {
	private static final int ACTIVITY_DELETE_DIALOG = 5;

	private static final int VIEW_TYPE_GROUP_START = 0;
    private static final int VIEW_TYPE_GROUP_CONT = 1;
    private static final int VIEW_TYPE_COUNT = 2;
    private class OrderPair{
    	float numToOrder = -1;
    	boolean isChecked = false;
    	OrderPair(float numToOrder, boolean isChecked){
    		this.numToOrder = numToOrder;
    		this.isChecked = isChecked;
    	}
    }
    
    //stores order data to save at end
    SparseArray<OrderPair> toOrder = new SparseArray<OrderPair>();
    private static String date;
    ListView listView;
    int itemidSelected;

    //used if editing, stores previous data
    SparseArray<Float> alreadyOrdered = new SparseArray<Float>();

    //can be new or edit old
    boolean isNew;
    
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
			date = extras.getString("date");
			isNew = extras.getBoolean("isNew");
		}
		Log.d("xxx", "date: " + date + "isNew: " + isNew);
		
		setTitle("New Order on " + ItemsDataSource.dateSqlToStd(date));
		listView = (ListView) findViewById(android.R.id.list);
		itemidSelected = -1;
		Log.d("xxx", "Title Set");
		
		//lower part
		tvTitle = (TextView) findViewById(R.id.itemtitle);
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
	}
	
//--------------------------------------------Data-----------------------------------------------//

	private void fillData() {
		Cursor cursor;
//		if (isNew) cursor = datasource.fetchAllItemsWithLast();
//		else cursor = datasource.fetchAllItemsWithLastAndDate(date);
		cursor = datasource.fetchAllItemsWithLastAndDate(date);
		Context context = this;
		int rowLayout = R.layout.row_new_orders_without_header;
		//String[] fromColumns = { DatabaseHelper.SI_ITEM_ID };
		//int[] toViews = { R.id.label };
		String[] fromColumns = { DatabaseHelper.SI_SUPPLIER };
		int[] toViews = { R.id.label };
		int flags = 0;
		adapter = new MySimpleCursorAdapter(context, rowLayout, cursor,
				fromColumns, toViews, flags);
		setListAdapter(adapter);
		Log.d("xxx", "Data Filled");
	}
	
//--------------------------------------------On Click-----------------------------------------------//	

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		Log.d("xxx", "onListItemClick");

		addToOrder();

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
	private void addToOrder(){
		//if previous selection, save numToOrder to toOrder
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
				toOrder.get(lastItemid).numToOrder = toCount;
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
		}
	}
			
//--------------------------------------------Database Updates-----------------------------------------------//	
	private void deleteUnchecked(){
		Log.d("xxx", "deleteUnChecked");
		for(int i = 0; i < alreadyOrdered.size(); i++) {
			int key = alreadyOrdered.keyAt(i);
			if (!toOrder.get(key).isChecked){
				 String table = DatabaseHelper.TABLE_ORDERS;
				 String whereClause = DatabaseHelper.O_DATE + " = ? AND " + DatabaseHelper.O_ITEM_ID + " = ?";
				 String[] whereArgs = new String[]{date, String.valueOf(key)};
				 Log.d("xxx", "deleting........date: " + date + "key" + key);
				datasource.delete(this, table, whereClause, whereArgs);
			}
		}
	}
	private void updateOrders(){
		Log.d("xxx", "updateOrders");
		for(int i = 0; i < toOrder.size(); i++) {
			int key = toOrder.keyAt(i);
			// get the object by the key.
			OrderPair pair = toOrder.get(key);
			Log.d("xxx", "key: " + key + "isChecked: " + pair.isChecked);

			Float value = alreadyOrdered.get(key);
			if (value == null) value = (float) -1;
			Log.d("xxx", "numToOrder: " + pair.numToOrder + "value: " + value);

			if (pair.isChecked && pair.numToOrder != value) {///////
				if (value == -1){
					int id = key;
					float count = pair.numToOrder;
					String table = DatabaseHelper.TABLE_ORDERS;
					ContentValues values = new ContentValues();
					values.put(DatabaseHelper.O_ITEM_ID, id);
					values.put(DatabaseHelper.O_DATE, date);
					values.put(DatabaseHelper.O_COUNT, count);
					Log.d("xxx", "table: " + table + "values: " + values);
					datasource.insert(this, table, values);
				}
				else{
					int id = key;
					float count = pair.numToOrder;
					String table = DatabaseHelper.TABLE_ORDERS;
					ContentValues values = new ContentValues();
					values.put(DatabaseHelper.O_COUNT, count);
					String whereClause = DatabaseHelper.O_ITEM_ID + " = ? AND "
							+ DatabaseHelper.O_DATE + " = ?";
					String[] whereArgs = new String[] {String.valueOf(id), String.valueOf(date)};
					Log.d("xxx", "table: " + table + " values: " + values + " whereClause: " + whereClause + " whereArgs1: " + whereArgs[0]  + whereClause + " whereArgs2: " + whereArgs[1]);
					Log.d("xxx", "id: " + String.valueOf(id) + " date: " + date);
					Log.d("xxx", "count: " + count);
					datasource.update(this, table, values, whereClause, whereArgs);
				}
			}
		}
		deleteUnchecked();
	}
	
	private void saveOrders(){
		Log.d("xxx", "saveOrders");
		int key = 0;
		for(int i = 0; i < toOrder.size(); i++) {
		   key = toOrder.keyAt(i);
		   // get the object by the key.
		   OrderPair pair = toOrder.get(key);
			Log.d("xxx", "key: " + key + "isChecked: " + pair.isChecked);

		   if (pair.isChecked){
				int id = key;
				float count = pair.numToOrder;
				String table = DatabaseHelper.TABLE_ORDERS;
				ContentValues values = new ContentValues();
				values.put(DatabaseHelper.O_ITEM_ID, id);
				values.put(DatabaseHelper.O_DATE, date);
				values.put(DatabaseHelper.O_COUNT, count);
				Log.d("xxx", "table: " + table + "values: " + values);
				datasource.insert(this, table, values);
			}
		}
	}
	private boolean existsOrderToSave(){
		Log.d("xxx", "existsOrderToSave: ");
		boolean isSave = false;
		
		//check for isChecked
		for(int i = 0; i < toOrder.size(); i++) {
		   int key = toOrder.keyAt(i);
		   OrderPair pair = toOrder.get(key);
		   if (isNew){
			   if (pair.isChecked){
				   isSave = true;
					break;
			   }
		   }
		   else{
			   Float value = alreadyOrdered.get(key);
			   if (value == null) value = (float) -1;
			   if (pair.isChecked && pair.numToOrder != value){
					isSave = true;
					break;
			   }
		   }
		}
		
		//check if anything to delete if editing
		if (isSave == false){
			for(int i = 0; i < alreadyOrdered.size(); i++) {
				int key = alreadyOrdered.keyAt(i);
				if (!toOrder.get(key).isChecked){
					 isSave = true;
					 break;
				}
			}
		}

		Log.d("xxx", "isSave" + isSave);
		return isSave;
	}
	private int getPositionForId(int itemid){
		Log.d("xxx", "getCount(): " + adapter.getCount());
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
	private void checkmarkRowWithIdAndUpdateCount(int id, boolean isChecked)
	{
		Log.d("xxx", "checkmarkRowWithId");
	    int count = listView.getChildCount();
	    for (int i = 0; i < count; i++)
	    {
	        View v = listView.getChildAt(i);
	        if ((Integer)v.getTag() == id)
	        {
	        	float numToOrder = toOrder.get(id).numToOrder;
        		CheckBox check = (CheckBox) v.findViewById(R.id.check);
	    		TextView tvCount = (TextView) v.findViewById(R.id.order_count);
//	        	if (isChecked && numToOrder > 0){
//		        	check.setChecked(true);
//		    		tvCount.setText(Float.toString(numToOrder));
//	        	}
	        	if (isChecked){
		        	check.setChecked(true);
		    		tvCount.setText(Float.toString(numToOrder));
	        	}
	        	else{
	        		check.setChecked(false);
		    		tvCount.setText("---");
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
		        	String s = "---";
		        	Log.d("xxx", "toOrder.get(id).isChecked" + toOrder.get(id).isChecked);
		        	if (toOrder.get(id).isChecked){
		        		float numToOrder = toOrder.get(id).numToOrder;
			        	Log.d("xxx", "numToOrder" + numToOrder);
		        		if (numToOrder <= 0) {
		        			success = false;
		        			break;
		        		}
		        		else s = String.valueOf(numToOrder);
		        	}
		        	TextView text = (TextView) v.findViewById(R.id.order_count);
		            text.setText(s);
		        }
		    }
		    return success;
		}

		public void bindView(View v, Context context, Cursor c) {
			int id = c.getInt(c.getColumnIndex(DatabaseHelper.SI_ITEM_ID));
			float lastInv = c.getFloat(c.getColumnIndex(DatabaseHelper.AS_LAST_I_COUNT));
			float min = c.getFloat(c.getColumnIndex(DatabaseHelper.SI_MIN));
			float max = c.getFloat(c.getColumnIndex(DatabaseHelper.SI_MAX));
			float sug = 0;
			float ordered = -1;

			if (lastInv < min) sug = max - lastInv;
			if (toOrder.get(id) == null){
				OrderPair pair;
				Log.d("xxx", "isNew:::::::" + isNew);
				if (isNew){
					pair = new OrderPair(sug, false);
				}
				else{
					ordered = c.getFloat(c.getColumnIndex(DatabaseHelper.O_COUNT));
					Log.d("xxx", "ordered:::::" + ordered);
					if (ordered <= 0){//////
						pair = new OrderPair(sug, false);
					}
					else{
						pair = new OrderPair(ordered, true);
						alreadyOrdered.put(id, ordered);
					}
				}
				toOrder.put(id, pair);
			}
			
			v.setTag(Integer.valueOf(id));
			CheckBox checkBox = (CheckBox) v.findViewById(R.id.check);
			checkBox.setTag(Integer.valueOf(id));
			checkBox.setChecked(toOrder.get(id).isChecked);
			Log.d("xxx", "checkBox getId: " + checkBox.getId());
			checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton buttonView,
						boolean isChecked) {
					Integer id = (Integer)buttonView.getTag();
					if (isChecked){
						toOrder.get(id).isChecked = true;

					} else {
						toOrder.get(id).isChecked = false;
					}
					if (!updateNumToOrderWithId(id)){
						buttonView.setChecked(false);
						toOrder.get(id).isChecked = !toOrder.get(id).isChecked;
					}
				}
			});
			
			String orderCount = "---";
			OrderPair order = toOrder.get(Integer.valueOf(id));
			if (order != null && order.isChecked) orderCount = String.valueOf(order.numToOrder);
			else if (ordered > 0) orderCount = String.valueOf(ordered);
			TextView textView = (TextView) v.findViewById(R.id.order_count);
			if (textView != null) textView.setText(orderCount);

			String orderSuggested = String.valueOf(sug);
			textView = (TextView) v.findViewById(R.id.order_suggested);
			if (textView != null) textView.setText(orderSuggested);

			String lastIntentoryCount = String.valueOf(lastInv);
			textView = (TextView) v.findViewById(R.id.last_inventory_count);
			if (textView != null) textView.setText(lastIntentoryCount);
			
			String itemMin = String.valueOf(min);
			textView = (TextView) v.findViewById(R.id.order_min);
			if (textView != null) textView.setText(itemMin);
			
			String itemMax = String.valueOf(max);
			textView = (TextView) v.findViewById(R.id.order_max);
			if (textView != null) textView.setText(itemMax);
			
			String itemId = String.valueOf(id);
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
				
				textView = (TextView) v.findViewById(R.id.order_count_col);
				if (textView != null) textView.setText(R.string.col_num);
				
				textView = (TextView) v.findViewById(R.id.order_suggested_col);
				if (textView != null) textView.setText(R.string.col_sug);
				
				textView = (TextView) v.findViewById(R.id.last_inventory_count_col);
				if (textView != null) textView.setText(R.string.col_inv);
				
				textView = (TextView) v.findViewById(R.id.order_min_col);
				if (textView != null) textView.setText(R.string.col_min);
				
				textView = (TextView) v.findViewById(R.id.order_max_col);
				if (textView != null) textView.setText(R.string.col_max);
				
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
 Log.d("xxx", "viewType: " + viewType);
            View v;
            LayoutInflater inflater = LayoutInflater.from(context);
            
            if (viewType == VIEW_TYPE_GROUP_START) {
                // Inflate a layout to start a new group
                v = inflater.inflate(R.layout.row_new_orders_with_header, parent, false);
            } else {
                // Inflate a layout for "regular" items
                v = inflater.inflate(R.layout.row_new_orders_without_header, parent, false);
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
            	Log.d("xxx", "position: " + position + "supplier: " + supplier + "previousSupplier" + previousSupplier);
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
		addToOrder();
		if (existsOrderToSave()){
			new AlertDialog.Builder(this)
			.setPositiveButton(R.string.btn_save, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
								Log.d("xxx", "setPositiveButton onClick: ");
								if (isNew) saveOrders();
								else updateOrders();
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
		if (itemidSelected >= 0) {
			TextView text = (TextView) getSelectedViewToEdit().findViewById(R.id.order_count);
            text.setText("---");
		}
	}

		public void restorevalue(View view) {
			if (itemidSelected >=0){
				OrderPair pair = toOrder.get(itemidSelected);
				float num = 0;
				if (toOrder != null) num = pair.numToOrder;
				if (num != 0) memento = String.valueOf(num);
				TextView text = (TextView) getSelectedViewToEdit().findViewById(R.id.order_count);
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
			TextView text = (TextView) getSelectedViewToEdit().findViewById(R.id.order_count);
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