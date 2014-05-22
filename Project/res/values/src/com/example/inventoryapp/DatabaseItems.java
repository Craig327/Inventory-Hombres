package com.example.inventoryapp;

import java.util.List;
import java.util.Locale;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class DatabaseItems extends ListActivity {
	private ItemsDataSource datasource;
	ListView list;
	List<Item> values;
	LayoutInflater linf;
	Locale current;
	Intent intent;
	AlertDialog.Builder builder;
	AlertDialog options, delete;
	View deletedialog, confirmdelete;
	ArrayAdapter<Item> adapter;
	boolean optionsshown;
	ViewGroup view;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_database_items);
		builder = new AlertDialog.Builder(this);
		current = getResources().getConfiguration().locale;
				datasource = new ItemsDataSource(this);
			    datasource.open();
			    //   path = datasource.getdbpath(); //Path to the db file, USE THIS PATH FOR GOOGLE DRIVE UPLOADING AND DOWNLOADING
			       datasource.createItem("Test Item");//Created a test item for testing, REMOVE BEFORE INTEGRATING DATABASE
			       values = datasource.getAllItems(); 
			       adapter = new ArrayAdapter<Item>(this,
					        R.layout.list_text_layout, values);
					    setListAdapter(adapter);
		list = getListView();
		intent = getIntent();
		linf = LayoutInflater.from(this);
		deletedialog = linf.inflate(R.layout.database_item_delete, view, false);
		confirmdelete = linf.inflate(R.layout.delete_dialog, view, false);
		
		list.setOnItemLongClickListener(new ListView.OnItemLongClickListener(){
			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
					deletemenu();
				return false;
			}});
    
    list.setOnItemClickListener(new ListView.OnItemClickListener(){

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
		launch_existing_dbitem();
		}
    	
    });
    
    
		}

	public void confirmdelete(View v){
	if(delete != null)delete.show();
	else{
	builder.setView(confirmdelete);
	final Toast deletesuccess = Toast.makeText(this, "Delete Successful", Toast.LENGTH_SHORT);
	builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
		
		@Override
		public void onClick(DialogInterface dialog, int which) {
			
			//DELETE THE DATABASE ITEM, Possibly sql statement, something with the list to delete it from that as well??
			//IF DELETE IS SUCCESSFUL
			deletesuccess.show();
			dialog.cancel();
			options.cancel();
		}
	});
	builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int which) {
			dialog.cancel();
		}
	});
	delete = builder.create();
	delete.show();
	}
}
	
	public void deletemenu(){
	if(optionsshown == true)options.show();
	else{
	builder.setView(deletedialog);
	builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
		
		@Override
		public void onClick(DialogInterface dialog, int which) {
			dialog.cancel();		
		}
	});
	
	options = builder.create();
	options.show();
	optionsshown = true;
	}
	}
	
	public void launch_newdbitem(){
		Intent intent = new Intent(this, Database_Item.class);
		intent.putExtra("exists", false);
		startActivity(intent);
	}
	
	public void launch_existing_dbitem(){
		int id, min, max, price;
		String Name, Type, Supplier;
		//INITIALIZE THESE VALUES TO THE VALUES OF THE DB ITEM SELECTED
		id = 0;
		min = 0;
		max = 0;
		price = 0;
		Name = "testname";
		Type = "testtype";
		Supplier = "big booty hoes dance wit it";
		//END INIT
		Intent intent = new Intent(this, Database_Item.class);
		intent.putExtra("exists", true);
		intent.putExtra("id", id );
		intent.putExtra("min", min );
		intent.putExtra("max", max );
		intent.putExtra("price", price );
		intent.putExtra("name", Name );
		intent.putExtra("type", Type );
		intent.putExtra("supplier", Supplier );
		startActivity(intent);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.database_items, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.newdatabaseitem) {
			launch_newdbitem();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	 @Override
	 public void onBackPressed(){
	Intent intent = new Intent(this, MainActivity.class);
	startActivity(intent);
	 }
}
