package com.example.inventoryapp;


import java.util.List;
import android.support.v4.widget.SimpleCursorAdapter;
import android.text.Editable;
import android.util.Log;
import android.util.SparseArray;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class New_Inventory_Count extends ListActivity {
	private static final int ACTIVITY_DELETE_DIALOG = 5;
	private static final int VIEW_TYPE_GROUP_START = 0;
    private static final int VIEW_TYPE_GROUP_CONT = 1;
    private static final int VIEW_TYPE_COUNT = 2;
    
    private class inventorypair{
    	float newcount= -1;
    	boolean isChecked = false;
    	inventorypair(float newcount, boolean isChecked){
    		this.newcount = newcount;
    		this.isChecked = isChecked;
    	}
    }
    
    SparseArray<inventorypair> inventoryarray = new SparseArray<inventorypair>();
    private static String date;
    ListView listView;
    int itemidSelected;

    //lower partf
	LinearLayout countBoxLayout;
	TextView tvTitle, tvId, tvType, tvSupplier, tvMin, tvMax, tvInvOn, tvInvCount, tvOrderedOn, tvOrderedCount;
	EditText etCount, etToOrder;
	String memento;
	String[] SelectionArgs;
	String type;
	
	
	
	private ItemsDataSource datasource;
	private ImportDatabase importer;
	
	List<Item> values;
	private MySimpleCursorAdapter adapter;

//--------------------------------------------Create-----------------------------------------------//
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.d("xxx", "New Inventory Count Activity onCreate");
		super.onCreate(savedInstanceState);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

		setContentView(R.layout.inventory_detail);
		Bundle extras = getIntent().getExtras();
		if (extras != null) date = extras.getString("date");
		else Log.d("null", "date is pretty null");
		Log.d("date??", "date is: " + date);
		type = extras.getString("type_selected");
		setTitle("Editing Inventory on " + ItemsDataSource.dateSqlToStd(date));
		listView = (ListView) findViewById(android.R.id.list);
		itemidSelected = -1;
		SelectionArgs = new String[]{type, date};
		//lower part
		tvTitle = (TextView) findViewById(R.id.itemtitle);
		etCount =	(EditText) findViewById(R.id.countbox);
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

		datasource = new ItemsDataSource(this);
		importer = new ImportDatabase(this);
		
		fillData();
	Cursor c = adapter.getCursor();
	
		
	}
	
//--------------------------------------------Data-----------------------------------------------//

	private void fillData() {
		Log.d("xxx", "Fill Data Called");
		datasource.database = importer.getWritableDatabase();
		Cursor cursor;
		//if(datasource.isInventoryAlready(date)){
		//	Log.d("xxx", "Inventory exists already");
		//	cursor = datasource.fetchInventoryWithDateandType(date, type);
		//}
		//else 
		cursor = datasource.fetchAllItemsWithLastbyType(type, date);
		
		Log.d("xxx", "Cursor Received");
		Context context = this;
		int rowLayout = R.layout.row_new_inventory;
		String[] fromColumns = { DatabaseHelper.SI_ITEM_ID };
		int[] toViews = { R.id.label };
		int flags = 0;
		adapter = new MySimpleCursorAdapter(context, rowLayout, cursor,
				fromColumns, toViews, flags);
		setListAdapter(adapter);
	}
	
//--------------------------------------------On Click-----------------------------------------------//	

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		Log.d("xxx", "onListItemClick");

		//if previous selection, save numToOrder to toOrder
		if (itemidSelected > 0) {
			Cursor cursor = adapter.getCursor();
			cursor.moveToPosition(getPositionForId(itemidSelected));
			Log.d("xxx", "1");

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
				
				inventoryarray.get(lastItemid).newcount = toCount;
			
				checkmarkRowWithIdAndUpdateCount(lastItemid, true);
			} else if (toCount == 0){
				checkmarkRowWithIdAndUpdateCount(lastItemid, false);
			}
		}
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
		//String itemLastInvDate = cursor.getString(cursor.getColumnIndex(DatabaseHelper.I_DATE));
		//float itemLastInvCount = cursor.getFloat(cursor.getColumnIndex(DatabaseHelper.I_COUNT));
		//String itemLastOrderDate = cursor.getString(cursor.getColumnIndex(DatabaseHelper.O_DATE));
		//float itemLastOrderCount = cursor.getFloat(cursor.getColumnIndex(DatabaseHelper.O_COUNT));
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
		//tvInvOn.setText(itemLastInvDate);
		//if (itemLastInvCount > 0) tvInvCount.setText(String.valueOf(itemLastInvCount));
		//else tvInvCount.setText("");
		//tvOrderedOn.setText(itemLastOrderDate);
		//if (itemLastOrderCount > 0) tvOrderedCount.setText(String.valueOf(itemLastOrderCount));
		//else tvOrderedCount.setText("");
		Log.d("xxx", "onListItemClick");

		Log.d("xxx", "itemid: " + itemid);		
		super.onListItemClick(l, v, position, id);		
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
	
	private void saveInventory(){
		Log.d("xxx", "saveInventory");
		int key = 0;
		for(int i = 0; i < inventoryarray.size(); i++) {
		   key = inventoryarray.keyAt(i);
		   Log.d("xxx", "i: " + i);
		   Log.d("xxx", "array key = " + key);
		   Log.d("xxx", "array count = " + inventoryarray.get(key).newcount);
		   // get the object by the key.
		   inventorypair pair = inventoryarray.get(key);
		  ContentValues temp = new ContentValues();
		  //ERROR ON NEXT LINE
		  if(inventoryarray.get(key).newcount > 0){
		   if (!datasource.isitemInventoryAlready(date, Integer.toString(key))){
			   Log.d("XXX", "its not in inventory already");
		//   if (pair.newcount < 0){
				int id = key;
				float count = pair.newcount;
				String table = DatabaseHelper.TABLE_INVENTORY;
				ContentValues values = new ContentValues();
				values.put(DatabaseHelper.I_ITEM_ID, id);
				values.put(DatabaseHelper.I_DATE, date);
				values.put(DatabaseHelper.I_COUNT, count);
				Log.d("xxx", "table: " + table + "values: " + values);
				datasource.insert(this, table, values);
			}
		   else {  
			   Log.d("XXX", "updating stuff");
			   int id = key;
				float count = pair.newcount;
				
			
				Log.d("xxx", "date = " + date + ", id = " + id + ", count = " + count);
				datasource.updateInventory(date, Integer.toString(id), Float.toString(count));
				//datasource.update(this, table, values, whereClause, whereArgs)
		   	}
		   }
		}
		}
	
	private boolean existsInventoryToSave(){
		Log.d("xxx", "existsInventoryToSave: ");
		boolean isSave = false;
		for(int i = 0; i < inventoryarray.size(); i++) {
		   int key = inventoryarray.keyAt(i);
		   inventorypair pair = inventoryarray.get(key);
		   if (pair.isChecked){
			   isSave = true;
			   break;
		   }
		}
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
				int id = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.I_ITEM_ID));
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
		Log.d("xxx", "checkmarkRowWithId count =" + count);
	    for (int i = 0; i < count; i++)
	    {
	        View v = listView.getChildAt(i);
	        if ((Integer)v.getTag() == id)
	        {
	        	float numToOrder = inventoryarray.get(id).newcount;
        		//CheckBox check = (CheckBox) v.findViewById(R.id.check);
	    		TextView tvCount = (TextView) v.findViewById(R.id.num_counted);
	        //	if (isChecked){
		      //  	check.setChecked(true);
		    		tvCount.setText(Float.toString(numToOrder));/*
	        	}
	        	else{
	        		check.setChecked(false);
		    		tvCount.setText("---");
	        	}
	        	*/
	        }
	    }
	}

//--------------------------------------------MySimpleCursorAdapter-----------------------------------------------//	

	private class MySimpleCursorAdapter extends SimpleCursorAdapter {

		public MySimpleCursorAdapter(Context context, int layout, Cursor c,
				String[] from, int[] to, int flags) {
			super(context, layout, c, from, to, flags);
		}
		
		private boolean updateNumToInvWithId(int id)
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
		        	if (inventoryarray.get(id).isChecked){
		        		float newcount = inventoryarray.get(id).newcount;
		        		if (newcount < 1) success = false;
		        		else s = String.valueOf(newcount);
		        	}
		        	TextView text = (TextView) v.findViewById(R.id.order_count);
		            text.setText(s);
		        }
		    }
		    return success;
		}

		public void bindView(View v, Context context, Cursor c) {
			Log.d("xxx", "bindView called");
			int id = c.getInt(c.getColumnIndex(DatabaseHelper.SI_ITEM_ID));
			Log.d("xxx", "Index Received");
			
			Log.d("xxx", "Last Inv Received");
			float sug = 0;
			
			Log.d("xxx", "Values received from cursor");
			
			//if (lastInv < min) sug = max - lastInv;
			TextView textView = (TextView) v.findViewById(R.id.num_counted);
			float lastInv = 0;
			if(datasource.isInventoryAlready(date))  lastInv = c.getFloat(c.getColumnIndex(DatabaseHelper.I_COUNT));
			String instock = "---";
			if(inventoryarray.get(id) == null){
				inventorypair pair = new inventorypair(lastInv, false);
				inventoryarray.put(id, pair);
			}
			if(lastInv > 0){
				textView.setText(Float.toString(inventoryarray.get(id).newcount));
			}
			else textView.setText(instock);
			v.setTag(Integer.valueOf(id));
			/*CheckBox checkBox = (CheckBox) v.findViewById(R.id.check);
			checkBox.setTag(Integer.valueOf(id));
			Log.d("xxx", "checkBox getId: " + checkBox.getId());
			checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton buttonView,
						boolean isChecked) {
					Integer id = (Integer)buttonView.getTag();
					if (isChecked){
						inventoryarray.get(id).isChecked = true;

					} else {
						inventoryarray.get(id).isChecked = false;
					}
					if (!updateNumToOrderWithId(id)){
						buttonView.setChecked(false);
						inventoryarray.get(id).isChecked = !inventoryarray.get(id).isChecked;
					}
				}
			});*/
		
			//inventorypair order = inventoryarray.get(Integer.valueOf(id));
			//if (order != null && order.isChecked) orderCount = String.valueOf(order.newcount);
			
			//if(textView != null){
			//if(lastInv > 0)	instock = Float.toString(lastInv);
			//textView.setText(instock);
			//}
			

		//	String orderSuggested = String.valueOf(sug);
		//	textView = (TextView) v.findViewById(R.id.order_suggested);
		//	if (textView != null) textView.setText(orderSuggested);

			//String lastIntentoryCount = String.valueOf(lastInv);
			//textView = (TextView) v.findViewById(R.id.last_inventory_count);
			//if (textView != null) textView.setText(lastIntentoryCount);
			
			//String itemMin = String.valueOf(min);
			//textView = (TextView) v.findViewById(R.id.order_min);
			//if (textView != null) textView.setText(itemMin);
			
			//String itemMax = String.valueOf(max);
			//textView = (TextView) v.findViewById(R.id.order_max);
			//if (textView != null) textView.setText(itemMax);
			
			//String itemId = c.getString(c.getColumnIndex(DatabaseHelper.SI_ITEM_ID));
			//textView = (TextView) v.findViewById(R.id.item_id);
			//if (textView != null) textView.setText(itemId);
			
			String itemName = c.getString(c.getColumnIndex(DatabaseHelper.SI_NAME));
			textView = (TextView) v.findViewById(R.id.item_name);
			if (textView != null) textView.setText(itemName);
			
			//set the text in the header
			textView = (TextView) v.findViewById(R.id.header);
			if (textView != null) {
				String supplierName = c.getString(c.getColumnIndex(DatabaseHelper.SI_SUPPLIER));
				textView.setText(supplierName);
				
			//	textView = (TextView) v.findViewById(R.id.order_count_col);
			//	if (textView != null) textView.setText(R.string.col_num);
				
			//	textView = (TextView) v.findViewById(R.id.order_suggested_col);
			//	if (textView != null) textView.setText(R.string.col_sug);
				
				textView = (TextView) v.findViewById(R.id.last_inventory_count_col);
				if (textView != null) textView.setText(R.string.col_inv);
				
				//textView = (TextView) v.findViewById(R.id.order_min_col);
				//if (textView != null) textView.setText(R.string.col_min);
				
				//textView = (TextView) v.findViewById(R.id.order_max_col);
				//if (textView != null) textView.setText(R.string.col_max);
				
				//textView = (TextView) v.findViewById(R.id.item_id_col);
				//if (textView != null) textView.setText(R.string.col_id);
				
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
            Log.d("xxx", "Inflater Initialized");
            if (viewType == VIEW_TYPE_GROUP_START) {
                // Inflate a layout to start a new group
                v = inflater.inflate(R.layout.row_new_inventory, parent, false);
                Log.d("xxx", "Inflated");
            } else {
                // Inflate a layout for "regular" items
                v = inflater.inflate(R.layout.row_new_inventory_without_header, parent, false);
                Log.d("xxx", "Inflated");
            }
            return v;
		}

		@Override
		public int getItemViewType(int position) {
			Log.d("xxx", "getItemViewType called");
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
			Log.d("xxx", "isNewGroup called");
            // Get date values for current and previous data items
            String supplier = cursor.getString(cursor
				.getColumnIndex(DatabaseHelper.SI_SUPPLIER));
 
            cursor.moveToPosition(position - 1);
            String previousSupplier = cursor.getString(cursor
    				.getColumnIndex(DatabaseHelper.SI_SUPPLIER)); 
            
            // Restore cursor position
            cursor.moveToPosition(position);
 Log.d("xxx", "position: " + position + "supplier: " + supplier + "previousSupplier" + previousSupplier);
            // return true if its a new supplier
            if (!supplier.equals(previousSupplier)) return true;
            return false;
        }
	}
	
//--------------------------------------------Life Cycle-----------------------------------------------//	

	@Override
	public void onBackPressed() {
		Log.d("xxx", "onBackPressed Called");
		//if (existsInventoryToSave()){
			new AlertDialog.Builder(this)
			.setPositiveButton(R.string.btn_save, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
								Log.d("xxx", "setPositiveButton onClick: ");
								saveInventory();
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
		//}
		//else{
		//	Intent i = new Intent();
		//	setResult(RESULT_OK, i);
		//	finish();
		}
	//}
	
	private void startDeleteDialog(){
		Intent i = new Intent(this, DeleteDialogActivity.class);
    	startActivityForResult(i, ACTIVITY_DELETE_DIALOG);
	}
	
//--------------------------------------------Bottom half-----------------------------------------------//	


//--------------------------------------------Count-----------------------------------------------//	

	public void clearCount(View view) {
		Editable temp = etCount.getText();
		memento = temp.toString();
		etCount.setText(" ");
	}

	public void restorevalue(View v) {
		etCount.setText(memento);
	}

	public void setCount(View view) {
		String content;
		Editable temp = etCount.getText();
		content = temp.toString();
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
		etCount.setText(content);
	}
}