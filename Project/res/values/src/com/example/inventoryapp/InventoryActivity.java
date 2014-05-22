package com.example.inventoryapp;


import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.example.inventoryapp.R;

public class InventoryActivity extends ListActivity {
  private ItemsDataSource datasource;
  LayoutInflater linf;
  View inflator,editorview,deleteview;
  EditText date;
  Locale current;
  String path;
  ListView list;
  ArrayAdapter<Item> adapter;
  List<Item> values;
  public boolean editcalled, newcalled,displaytype;
  AlertDialog.Builder deletebuilder,editbuilder,startbuilder;
  AlertDialog editor, deletewarning,start;
  Intent intent;
  Bundle type;
  /* Crashes when you select another item after already selecting one, I suspect it's something to do
   * with the ListView or Array */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_inventory);
    intent = getIntent();
    type = intent.getExtras();
    displaytype = type.getBoolean("query_type");
    
	editcalled = false;
	newcalled = false;
    linf = LayoutInflater.from(this);  
	editorview = linf.inflate(R.layout.edit_dialog_layout, null);
 	

    current = getResources().getConfiguration().locale;
    list = getListView();
   datasource = new ItemsDataSource(this);
    datasource.open();
 //   path = datasource.getdbpath(); //Path to the db file, USE THIS PATH FOR GOOGLE DRIVE UPLOADING AND DOWNLOADING
    datasource.createItem("Test Item");//Created a test item for testing, REMOVE BEFORE INTEGRATING DATABASE
    values = datasource.getAllItems(); // Instead of get all items, run a query on the db to get dates representing
    // inventory rows with that specific date
    deletebuilder = new AlertDialog.Builder(this);
    editbuilder = new AlertDialog.Builder(this);
    startbuilder = new AlertDialog.Builder(this);
    // use the SimpleCursorAdapter to show the
    // elements in a ListView
   adapter = new ArrayAdapter<Item>(this,
        R.layout.list_text_layout, values);
    setListAdapter(adapter);
  
    list.setOnItemLongClickListener(new ListView.OnItemLongClickListener(){
			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				launchEditDialog();
				adapter.notifyDataSetChanged();
				return false;
			}});
    
    list.setOnItemClickListener(new ListView.OnItemClickListener(){

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			//date.setText(/*The Selected db items date, like list.getSelectedItem().....*/)
			if(displaytype == true)launchtableedit(date.toString()); //if inventory....
			else existingorder(date.toString()); // else its an existing order....
			
			adapter.notifyDataSetChanged();
		}
    	
    });
    if(displaytype == true){
    getActionBar().setTitle(R.string.action_bar_inventory);
    inflator = linf.inflate(R.layout.dialog_layout, null);
    }
    else {
    	getActionBar().setTitle(R.string.action_bar_order);
    	inflator = linf.inflate(R.layout.order_date_dialog, null);
    } 
    date = (EditText) inflator.findViewById(R.id.choosedate);
}
  
  public void existingorder(String string){
	  Intent intent;
	  intent = new Intent(this, OrderEditActivity.class);
	  intent.putExtra("inv_date", string);
	  startActivity(intent);
  }
  
  public void launchtableedit(String string){
	Intent intent;
		
		if(displaytype == true){
			intent = new Intent(this, TableView.class);
			intent.putExtra("inv", true); // if displaytype is true, user is doing inventory
		}
		else{
			intent = new Intent(this, Inventory_Count.class);
			intent.putExtra("inv", false); // if displaytype is false, user is doing orders
		}
		intent.putExtra("inv_date", string);
		intent.putExtra("existing", true);
		startActivity(intent);
  }
  
  public void launchEditDialog(){
	  editcalled = true;
	  newcalled = false;
	  if(editor != null)editor.show();
	  else {
	  editbuilder.setView(editorview);
	  editbuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
		
		@Override
		public void onClick(DialogInterface dialog, int which) {
			dialog.cancel();		
		}
	});
	  editor = editbuilder.create();
	  editor.show();
	  }
  }
  
//Set up to change the date of the inventory selected, probably with a sql query.
  // ????Possibly rawQuery("UPDATE inventory SET date = " + selectedDate + " WHERE date= " + the date of the selected object, I can't put my finger on how right now);
 public void deleteitem(View v){
if(deletewarning != null)deletewarning.show();
else {
deleteview = linf.inflate(R.layout.delete_dialog, null);
deletebuilder.setView(deleteview);                      //This function doesn't work
final Toast deletesuccess = Toast.makeText(this, "Delete Successful", Toast.LENGTH_SHORT);
deletebuilder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
	
	@Override
	public void onClick(DialogInterface dialog, int which) {
		
		//DELETE THE INVENTORY, Possibly sql statement, something with the list to delete it from that as well??
		//IF DELETE IS SUCCESSFUL
		deletesuccess.show();
		
	}
});
deletebuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
	@Override
	public void onClick(DialogInterface dialog, int which) {
		dialog.cancel();
	}
});
deletewarning = deletebuilder.create();
deletewarning.show();
	}
}
 
  public void setItemDate(String selectedDate){ 
	  int i = list.getCheckedItemPosition();
	 // i = -1 all the time. That is causing it to crash each time when we try to do stuff to it
	  adapter.getItem(i).name = selectedDate;
	  System.out.println(i); 
	  adapter.notifyDataSetChanged();
	  //THIS ONE DOESN'T WORK
	  //Item temp = (Item) list.getItemAtPosition(i);
	 // System.out.println(temp.name);
	 // System.out.println(selectedDate);
	 // temp.name = selectedDate;
  }
  
  public void setDate(String Dte){ //Sets date of EditText object to send to TableView, DOES NOT SET DATE FOR INVENTORY ITEM
	  date.setText(Dte);
	}
  
 
  public void launchDateDialog(){
	newcalled = true;
	editcalled = false;
	if(start != null)start.show();
	else{
	startbuilder.setView(inflator);
	 
	SimpleDateFormat sdf = new SimpleDateFormat( "MM/dd/yyyy", current ); 
	date.setText( sdf.format( new Date() ));

	startbuilder.setPositiveButton("Start", new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int whichButton) {
			String thedate = date.getText().toString();
			launchTypeView(thedate);
			dialog.cancel();
		}
	});
	
	startbuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
		
		@Override
		public void onClick(DialogInterface dialog, int which) {
		dialog.cancel();
		}
	});
	start = startbuilder.create();
	start.show();
	}
  }
  
  public void showDatePickerDialog(View v) {
	    DialogFragment newFragment = new DatePickerFragment();
	    newFragment.show(getFragmentManager(), "datePicker");
	}
  
	public void launchTypeView(String date){
		Intent intent;
		if(displaytype == true){
			 intent = new Intent(this, TableView.class);
			intent.putExtra("inv", true); //if displaytype is true, the user is doing inventory
		}
		else {
			intent = new Intent(this, Inventory_Count.class);
			intent.putExtra("inv", false); //if displaytype is false, the user is doing orders
		}
		intent.putExtra("inv_date", date);
		intent.putExtra("exists", false);
		
		startActivity(intent);
	}
	
	@Override
  public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle presses on the action bar items
	    switch (item.getItemId()) {
	        case R.id.newinventory:
	          launchDateDialog();
	            return true;
	        case R.id.neworder:
	        	launchDateDialog();
	        	return true;
	       
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}
  
  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
      // Inflate the menu items for use in the action bar
      MenuInflater inflater = getMenuInflater();
      if(displaytype == true)inflater.inflate(R.menu.inventory_activity_actions, menu);
      else inflater.inflate(R.menu.order_activity_actions, menu);
      return super.onCreateOptionsMenu(menu);
  }

  @Override
  protected void onResume() {
    datasource.open();
    super.onResume();
  }

  @Override
  protected void onPause() {
    datasource.close();
    super.onPause();
  }
  
  @Override
	 public void onBackPressed(){
	Intent intent = new Intent(this, MainActivity.class);
	startActivity(intent);
	 }

} 