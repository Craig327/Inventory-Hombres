package com.example.inventoryapp;

import java.util.List;

import android.support.v4.widget.SimpleCursorAdapter;
import android.text.Editable;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

public class Inventory_Count extends ListActivity {
	Intent intent;
	Bundle iteminfo;
	String itemname,date,memento;
	Cursor cursor;
	SimpleCursorAdapter adapter;
	boolean exist, type,saveshown,deleteshown;
	LinearLayout countbox;
	AlertDialog.Builder builder;
	AlertDialog editdialog, deletedialog, savedialog;
	View editorview, deleteview,save;
	LayoutInflater linf;
	ListView list;
	ArrayAdapter<Item> list_adapter;
	List<Item> values;
	EditText count;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_inventory__count);
		list = getListView();
		builder = new AlertDialog.Builder(this);
		linf = LayoutInflater.from(this);
		count =	(EditText) findViewById(R.id.countbox);
		countbox = (LinearLayout)this.findViewById(R.id.countlayout);
		deleteview = linf.inflate(R.layout.delete_dialog, null);
		editorview = linf.inflate(R.layout.count_edit, null);
		intent = getIntent();
		iteminfo = intent.getExtras();
		itemname = iteminfo.getString("category"); //name of the category (table) selected
		date = iteminfo.getString("inv_date");
		exist = iteminfo.getBoolean("exists");
		type = iteminfo.getBoolean("inv");
		save = linf.inflate(R.layout.save_dialog_layout,null);
		if(exist == false && type == true)getActionBar().setTitle(R.string.title_activity_table_view + itemname); // if new inventory
		else if(exist == true && type == true){ // else if existing inventory
			getActionBar().setTitle(date + "Inventory");
			getActionBar().setSubtitle(itemname);
			list.setOnItemLongClickListener(new ListView.OnItemLongClickListener(){
				@Override
				public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
						int arg2, long arg3) {
					launcheditdialog();
					adapter.notifyDataSetChanged();
					return false;
				}});
			}
		if(exist == false && type == false)getActionBar().setTitle("New Order"); // if new order
			else if(exist == true && type == false){
			getActionBar().setTitle(date + "Order");
			countbox.setVisibility(LinearLayout.INVISIBLE);
			
			list.setOnItemLongClickListener(new ListView.OnItemLongClickListener(){
				@Override
				public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
						int arg2, long arg3) {
				
					launcheditdialog();
					adapter.notifyDataSetChanged();
					return false;
				}});
		}
	//	cursor = this.getContentResolver().query(TABLE NAME HERE, WHICH COLUMN TO GRAB HERE, null, null, null);
	//	adapter = new SimpleCursorAdapter(this, ,cursor,null,null,null)
		countbox = (LinearLayout)this.findViewById(R.id.countlayout);
		
	}

	public void deletebutton(View v){
		final Intent backtoinv = new Intent(this, InventoryActivity.class);
		backtoinv.putExtra("query_type",false);
		if(deletedialog != null)deletedialog.show();
		else{
		builder.setView(deleteview);
		final Toast deletesuccess = Toast.makeText(this, "Delete Successful", Toast.LENGTH_SHORT);
		builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				
				//DELETE THE INVENTORY, Possibly sql statement, something with the list to delete it from that as well??
				//IF DELETE IS SUCCESSFUL
				deletesuccess.show();
				startActivity(backtoinv);
				
			}
		});
		builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		});
		deleteshown = true;
		deletedialog = builder.create();
		deletedialog.show();
		}
	}
	
	public void editbutton(){
		countbox.setVisibility(LinearLayout.VISIBLE);
	}
	
	public void launcheditdialog(){
		if(editdialog != null)editdialog.show();
		else{
		builder.setView(editorview);
		builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();		
			}
		});
		editdialog = builder.create();
		editdialog.show();
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		//getMenuInflater().inflate(R.menu.inventory__count, menu);
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
	
	public void clearCount(View view){
		Editable temp = count.getText();
		memento = temp.toString();
	count.setText(" ");
	}
	
	public void restorevalue(View v){
		count.setText(memento);
	}
	
	public void setCount(View view){
		String content;
		Editable temp = count.getText();
		content = temp.toString();
		switch (view.getId()){
				case R.id.decimal: content += '.';
				break;
				case R.id.one: content += '1';
				break;
				case R.id.two: content += '2';
				break;
				case R.id.three: content += '3';
				break;
				case R.id.four: content += '4';
				break;
				case R.id.five: content += '5';
				break;
				case R.id.six: content += '6';
				break;
				case R.id.seven: content += '7';
				break;
				case R.id.eight: content += '8';
				break;
				case R.id.nine: content += '9';
				break;
				case R.id.zero: content += '0';
				break;
		}
		count.setText(content);
	}
	
	public void saveinventory(View v){
		final Toast savesuccess = Toast.makeText(this, "Save Successful", Toast.LENGTH_SHORT);
		//Save order before quitting. It's called saveinventory so that we don't have to make an identical layout for the dialog box.
		Intent back_to_table = new Intent(this, InventoryActivity.class);
		back_to_table.putExtra("query_type",false);
		startActivity(back_to_table);
		savesuccess.show();
	}
	
	 @Override
	 public void onBackPressed(){
		 if(deleteshown == true)deletedialog.cancel();
		 if(saveshown == true)savedialog.cancel();
		 if (savedialog!= null)savedialog.show();
		 else {
		 builder.setView(save);
	 builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		});
	 savedialog = builder.create();
	 savedialog.show();
	 saveshown = true;
		 }
	 }
}
