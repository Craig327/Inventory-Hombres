package com.example.inventoryapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;

public class TableView extends ListActivity {
	String date, itemname;
	View save,deleteview;
	LayoutInflater linf;
	AlertDialog.Builder builder,deletewarning;
	AlertDialog savedialog,deletedialog;
	boolean deleteshown, saveshown, exist;
	Intent backtoinventory, tocount;
	ListView tablelist;
	int selecteditem;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_table_view);
		
		Intent info = getIntent();//get the intent 
		Bundle getdate = info.getExtras();//get stuff that was packed into intent
		date = (String) getdate.get("inv_date"); //The date chosen from the previous activity put into variable	
		exist = getdate.getBoolean("existing");
		if(exist == false)getActionBar().setTitle(R.string.action_bar_new_inventory);
		else getActionBar().setTitle(date);
		
		linf = LayoutInflater.from(this); 
		 save = linf.inflate(R.layout.save_dialog_layout,null);
		 builder = new AlertDialog.Builder(this);
		 deletewarning = new AlertDialog.Builder(this);
		 deleteshown = false;
		 saveshown = false;
		 backtoinventory = new Intent(this, InventoryActivity.class);
		 backtoinventory.putExtra("query_type", true);
		 tablelist = getListView();
		 tablelist.setOnItemSelectedListener(new ListView.OnItemSelectedListener(){
			 @Override
				public void onItemSelected(AdapterView<?> arg0, View arg1,
						int arg2, long arg3) {
					selecteditem = tablelist.getSelectedItemPosition();
					itemname = (String)tablelist.getItemAtPosition(selecteditem);
					
				}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				selecteditem=-1;
				
			}});
	}
	
	public void launchcountactivity(){
		tocount = new Intent(this, Inventory_Count.class);
		tocount.putExtra("category", itemname);
		tocount.putExtra("exists", exist);
		tocount.putExtra("thedate", date);
		tocount.putExtra("inv", true);
		startActivity(tocount);
	}
	/*
	 public boolean onCreateOptionsMenu(Menu menu) {
	      // Inflate the menu items for use in the action bar
	      MenuInflater inflater = getMenuInflater();
	      inflater.inflate(R.menu.main, menu);
	      return super.onCreateOptionsMenu(menu);
	  }
	 */
	 public void deletebutton(View v){ //Dialog called when Don't Save is pressed after back button
		 if (deletedialog != null)deletedialog.show();
		 else{
		 deleteview = linf.inflate(R.layout.delete_dialog, null);
		 deletewarning.setView(deleteview);                      //This function doesn't work
		 deletewarning.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
		 	
		 	@Override
		 	public void onClick(DialogInterface dialog, int which) {
		 		
		 		startActivity(backtoinventory);
		 		//DELETE THE INVENTORY, Possibly sql statement, something with the list to delete it from that as well??
		 	}
		 });
		 deletewarning.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
		 	@Override
		 	public void onClick(DialogInterface dialog, int which) {
		 		dialog.cancel();
		 	}
		 });
		 deletedialog = deletewarning.create();
		 deletedialog.show();
		 deleteshown = true;
		 }
	}
	 
	 
	 public void saveinventory(View v){ //Would insert new inventory into the database
		 //***TYPE CODE HERE TO SAVE NEW INVENTORY TO DB***
		 
		 //IF SAVE TO DATABASE IS SUCCESSFUL
		 Toast successtoast = Toast.makeText(this, "Save Successful", Toast.LENGTH_SHORT);
		 successtoast.show();
		 
		 savedialog.dismiss();
		 startActivity(backtoinventory);
		 /*
		 //IF ERROR IN SAVING TO DATABASE
		 Toast errortoast = Toast.makeText(this, "There was an error", Toast.LENGTH_SHORT);
		 errortoast.show();
		 */
	 }
	 
	 @Override
	 public void onBackPressed(){
		 if(deleteshown == true)deletedialog.cancel();
		 if(saveshown == true)savedialog.cancel();
		 
		 builder.setView(save);
	 builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		});
	 if (savedialog != null);
	 else savedialog = builder.create();
	 
	 savedialog.show();
	 saveshown = true;
	 }
}
