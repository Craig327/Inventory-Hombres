package com.example.inventoryapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class Database_Item extends Activity {

	LayoutInflater linf;
	AlertDialog.Builder builder;
	AlertDialog save, delete;
	View savedialog, confirmdelete;
	Intent intent;
	Bundle bundle;
	boolean exists;
	EditText id, name, type, supplier, min, max, price; 
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_database__item);
		
		id = (EditText) this.findViewById(R.id.item_id);
		name = (EditText) this.findViewById(R.id.item_name);
		type = (EditText) this.findViewById(R.id.item_type);
		supplier = (EditText) this.findViewById(R.id.item_supplier);
		min = (EditText) this.findViewById(R.id.item_min);
		max = (EditText) this.findViewById(R.id.item_max);
		price = (EditText) this.findViewById(R.id.item_price);
		
		intent = getIntent();
		bundle = intent.getExtras();
		exists = bundle.getBoolean("exists");
		
		if (exists == true){
		id.setText(new Integer(bundle.getInt("id")).toString());
		name.setText(bundle.getString("name"));
		type.setText(bundle.getString("type"));
		supplier.setText(bundle.getString("supplier"));
		min.setText(new Integer(bundle.getInt("min")).toString());
		max.setText(new Integer(bundle.getInt("max")).toString());
		price.setText(new Integer(bundle.getInt("price")).toString());
		}
		
		builder = new AlertDialog.Builder(this);
		linf = LayoutInflater.from(this);
		getActionBar().setTitle("New Item");
		savedialog = linf.inflate(R.layout.save_dialog_layout, null);
		confirmdelete = linf.inflate(R.layout.delete_dialog, null);
	}

	public void showdialog(){
		if(save != null)save.show();
		else{
			builder.setView(savedialog);
			builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.cancel();		
				}
			});
			save = builder.create();
			save.show();
		}
	}
	
	public void saveinventory(View v){
		final Toast savesuccess = Toast.makeText(this, "Save Successful", Toast.LENGTH_SHORT);
		//Save order before quitting. It's called saveinventory so that we don't have to make an identical layout for the dialog box.
		Intent back_to_table = new Intent(this, DatabaseItems.class);
		savesuccess.show();
		startActivity(back_to_table);
	}
	
	public void deletebutton(View v){
		final Intent intent = new Intent(this, DatabaseItems.class);
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
				startActivity(intent);
				dialog.cancel();
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
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		//getMenuInflater().inflate(R.menu.database__item, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	 public void onBackPressed(){
	showdialog();
	 }
}
