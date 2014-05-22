package com.example.inventoryapp;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.app.ActionBar;
import android.app.ActionBar.OnNavigationListener;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class DatabaseActivity extends ListActivity implements OnNavigationListener {
	
	//get an instance of the ItemsDataSource class to handle SQL
	private ItemsDataSource dataSource;
	private ImportDatabase importer;
	
	//create the list view
	ListView list;
	View editorView;
	ArrayAdapter<Item> adapter;
	List<Item> values;
	
	//variable to keep track of which item actions are being performed on
	private int position;
	
	//-----------------------------------Create the Activity-----------------------------------------------//
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d("xxx", "onCreate DatabaseActivity");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_database);

		list = getListView();
		registerForContextMenu(list);  
		
		//this is in place of the dataSource.open() method, it instead grabs the DB stored in assets
		//by using the sqliteassethelper package extended by the ImportDatabase class
		importer = new ImportDatabase(this);
		dataSource = new ItemsDataSource(this);
		
		//Populate the listView with the database
		show("All");
		createActionMenu();
		
		//handle click of a single item
		list.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View v, int position, long id){                    
				editItem(position);
			}                       
		});
	}

	//-----------------------------------Populate the list with selected item type----------------------------------//
	public void show(String type){
		dataSource.database = importer.getWritableDatabase();

		if(type == "All")
			values = dataSource.getAllItems();
		else{
			values = dataSource.getItemsOfType(type);
		}
		adapter = new ArrayAdapter<Item>(this, R.layout.list_text_layout,
				values);
		setListAdapter(adapter);
	}
	
	//---------------------------------------------Options Menu-------------------------------------------------//
	
	//Action Menu is a drop down menu to sort the items in the database by their item type.
	public void createActionMenu(){
		final ActionBar actionBar = getActionBar();
		actionBar.setDisplayShowTitleEnabled(false);
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		
		ArrayList<String> itemList = new ArrayList<String>();
		itemList.add("All");
		itemList.add("Draft Beer");
		itemList.add("Bottle Beer");
		itemList.add("Tequila");
		itemList.add("Liquor (Non-Tequila)");
		itemList.add("Other");
		ArrayAdapter<String> typeAdapter = new ArrayAdapter<String>(this, 
				android.R.layout.simple_list_item_1, android.R.id.text1, itemList);
		actionBar.setListNavigationCallbacks(typeAdapter, this);
	}
	
	//I hate this section of code so much but it works! It's so ugly, I don't want a switch statement
	//but function over form in this case I suppose
	@Override
	public boolean onNavigationItemSelected(int typePosition, long id) {
		switch(typePosition) {
	    case 0:
	        show("All");
	        break;
	    case 1:
	        show("Draft Beer");
	        break;
		case 2:
			show("Bottle Beer");
			break;
		case 3:
			show("Tequila");
			break;
		case 4:
			show("Liquor (Non-Tequila)");
			break;
		case 5:
			show("Other");
			break;
		}
		return true;
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.database_activity_actions, menu);
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle presses on the action bar items
		switch (item.getItemId()) {
		//when the "New Item" button is pressed
		//pass the intent and the request code to the NewItemActivity
		//startActivityForResult indicates that we expect a response from the method
		case R.id.newitem:
			newItem();
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	
//-------------------------------------------Context Menu-----------------------------------------------//
	
	//the following two methods are for the Context Menu, which allows the user to select
	//an action upon a long click of a button. I didn't use onLongClickListener here because
	//a context menu is a much easier interface from what I saw.
	
	@Override  
		public void onCreateContextMenu(ContextMenu menu, View v, 
				ContextMenuInfo menuInfo) {
	    	super.onCreateContextMenu(menu, v, menuInfo);  
	        MenuInflater m = getMenuInflater();  
	        m.inflate(R.menu.database_context_menu, menu);  
	}  
	@Override  
	  public boolean onContextItemSelected(MenuItem item) {
		//get the info for the list item that was long clicked, namely the position
		//of the item in the list
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo(); 
		position = (int) info.id;
		switch(item.getItemId()){  
	    	case R.id.delete_item:  
	    		//create an alert dialog for a delete confirmation
	    		new AlertDialog.Builder(this)
	    	    .setTitle("Delete entry")
	    	    .setMessage("Are you sure you want to delete this entry?")
	    	    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
	    	        public void onClick(DialogInterface dialog, int which) { 
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
	    }  
	    return super.onContextItemSelected(item);  
	}
	

	//---------------------------------Update Database---------------------------------------------------//
	
	//remove the item from the database. It takes an integer, the position of the list item to 
	//be removed as a parameter
	public void deleteItem(int pos){
		dataSource.database = importer.getWritableDatabase();
    	dataSource.deleteItem(values.get(position));
    	
    	String name = values.get(position).getName();
    	
        values.remove(pos);  
        values = dataSource.getAllItems();
        this.adapter.notifyDataSetChanged(); 
		adapter = new ArrayAdapter<Item>(this, R.layout.list_text_layout,
				values);
		setListAdapter(adapter);
		
		final Toast deleteSuccess = Toast.makeText(this, name + " Deleted",
				Toast.LENGTH_SHORT);
		deleteSuccess.show();
	}
	//edit the item, same story as above
	public void editItem(int pos){
		dataSource.database = importer.getWritableDatabase();
		
		Item temp = values.get(pos);
		
		String id = String.valueOf(temp.getId());
		String name = temp.getName();
		Item item = dataSource.getItemByName(name);
		String itemType = item.getType();
		String supplier = item.getSupplier();
		String min = Double.toString(item.getMin());
		String max = Double.toString(item.getMax());
		
		Bundle bundle = new Bundle();				
		bundle.putString("id", id);
		bundle.putString("name", name);										
		bundle.putString("itemType", itemType);
		bundle.putString("supplier", supplier);				
		bundle.putString("min", min);
		bundle.putString("max", max);
		
		Intent intent = new Intent(this, EditItemActivity.class);
		intent.putExtras(bundle);
		startActivityForResult(intent, 2);
	}
	public void newItem(){
		Intent intent = new Intent(this, NewItemActivity.class);
		startActivityForResult(intent, 1);
	}
	
//------------------------------------Handle Activity Results--------------------------------------------//
		
		
	@Override
	public void onActivityResult(int requestCode,int resultCode,Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);
		//When the NewItemActivity method calls finish(), check the result and request codes
		//If both are valid, create a new bundle, and copy the bundle passed from the intent into it
		//Then, pull the strings out of the bundle and use it to execute an SQL query 
		//Creating a new item in the database
		if(resultCode == RESULT_OK && requestCode == 1){
			Bundle bundle = data.getBundleExtra("com.example.inventoryApp.newItem");
			String id, name, type, supplier, min, max, current="0";
			id = bundle.getString("id");
			name = bundle.getString("name");
			type = bundle.getString("itemType");
			supplier = bundle.getString("supplier");
			min = bundle.getString("min");
			max = bundle.getString("max");
				
			dataSource.database = importer.getWritableDatabase();
			if(dataSource.itemExists(id)){
				final Toast addFailed = Toast.makeText(this, "Error: Item Already Exists",
						Toast.LENGTH_SHORT);
				addFailed.show();
			}
			else{
				dataSource.newItem(id, type, name, supplier, min, max, current);
				
				final Toast addSuccess = Toast.makeText(this, name + " Added",
						Toast.LENGTH_SHORT);
				addSuccess.show();
			}
			show(type);
		}
		else if(resultCode == RESULT_OK && requestCode == 2){
			Bundle bundle = data.getBundleExtra("com.example.inventoryApp.editItem");
			String id, name, type, supplier, min, max, current="0", oldName;
			id = bundle.getString("id");
			name = bundle.getString("name");
			type = bundle.getString("itemType");
			supplier = bundle.getString("supplier");
			min = bundle.getString("min");
			max = bundle.getString("max");
			oldName=bundle.getString("oldName");
				
			dataSource.database = importer.getWritableDatabase();
			dataSource.updateItemByName(id, type, name, supplier, min, max, current, oldName);
			
			final Toast editSuccess = Toast.makeText(this, name + " Updated",
					Toast.LENGTH_SHORT);
			editSuccess.show();
			show(type);
		}
		else if(resultCode == RESULT_CANCELED){
			String type = data.getStringExtra("returnType");
			Log.d("xxx", "TYPE = " + type);
			show(type);
		}
	}
	//---------------------------------Activity Life Cycle------------------------------------------//
	@Override
	protected void onResume() {
		dataSource.database = importer.getWritableDatabase();
		super.onResume();
	}

	@Override
	protected void onPause() {
		dataSource.close();
		super.onPause();
	}

}
