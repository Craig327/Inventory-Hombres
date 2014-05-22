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
  public boolean editcalled, newcalled;
  /* Crashes when you select another item after already selecting one, I suspect it's something to do
   * with the ListView or Array */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_inventory);
	editcalled = false;
	newcalled = false;
    linf = LayoutInflater.from(this);  
	editorview = linf.inflate(R.layout.edit_dialog_layout, null);
 	inflator = linf.inflate(R.layout.dialog_layout, null);
    date = (EditText) inflator.findViewById(R.id.choosedate);
    current = getResources().getConfiguration().locale;
    list = getListView();
    datasource = new ItemsDataSource(this);
    datasource.open();
    path = datasource.getdbpath(); //Path to the db file, USE THIS PATH FOR GOOGLE DRIVE UPLOADING AND DOWNLOADING
    datasource.createItem("Test Item");//Created a test item for testing, REMOVE BEFORE INTEGRATING DATABASE
    values = datasource.getAllItems(); // Instead of get all items, run a query on the db to get dates representing
    // inventory rows with that specific date
    
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
  }
  
//Set up to change the date of the inventory selected, probably with a sql query.
  // ????Possibly rawQuery("UPDATE inventory SET date = " + selectedDate + " WHERE date= " + the date of the selected object, I can't put my finger on how right now);
 public void deleteitem(){
deleteview = linf.inflate(R.layout.delete_dialog, null);
AlertDialog.Builder deletewarning = new AlertDialog.Builder(this);
deletewarning.setView(deleteview);
deletewarning.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
	
	@Override
	public void onClick(DialogInterface dialog, int which) {
		
		//DELETE THE INVENTORY, Possibly sql statement, something with the list to delete it from that as well??
	}
});
deletewarning.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
	@Override
	public void onClick(DialogInterface dialog, int which) {
		dialog.cancel();
	}
});
}
 
  public void setItemDate(String selectedDate){ 
	  int i = list.getCheckedItemPosition();
	 
	  //adapter.getItem(i).name = selectedDate;
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
  
  public void launchEditDialog(){
	  editcalled = true;
	  newcalled = false;
	  AlertDialog.Builder editor = new AlertDialog.Builder(this);
	  editor.setView(editorview);
	  editor.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
		
		@Override
		public void onClick(DialogInterface dialog, int which) {
			dialog.cancel();		
		}
	});
	  editor.show();
  }
  
  public void launchDateDialog(){
	newcalled = true;
	editcalled = false;
	AlertDialog.Builder start = new AlertDialog.Builder(this);
	start.setView(inflator);
	 
	SimpleDateFormat sdf = new SimpleDateFormat( "MM/dd/yyyy", current ); 
	date.setText( sdf.format( new Date() ));

	start.setPositiveButton("Start", new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int whichButton) {
			String thedate = date.getText().toString();
			launchTypeView(thedate);
			dialog.cancel();
		}
	});
	
	start.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
		
		@Override
		public void onClick(DialogInterface dialog, int which) {
		dialog.cancel();
		}
	});
	start.show();
  }
  
  public void showDatePickerDialog(View v) {
	    DialogFragment newFragment = new DatePickerFragment();
	    newFragment.show(getFragmentManager(), "datePicker");
	}
  
	public void launchTypeView(String date){
		Intent intent = new Intent(this, TableView.class);
		intent.putExtra("inv_date", date);
		startActivity(intent);
	}
	
	@Override
  public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle presses on the action bar items
	    switch (item.getItemId()) {
	        case R.id.newinventory:
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
      inflater.inflate(R.menu.inventory_activity_actions, menu);
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

} 