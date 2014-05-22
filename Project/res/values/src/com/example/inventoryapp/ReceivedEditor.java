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
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ReceivedEditor extends ListActivity {
	Intent intent;
	Bundle iteminfo;
	String itemname,date;
	Cursor cursor;
	SimpleCursorAdapter adapter;
	LinearLayout countbox;
	AlertDialog.Builder builder;
	AlertDialog editdialog, deletedialog, receiveddialog, savedialog;
	View editorview, deleteview, receiveview, save;
	LayoutInflater linf;
	ListView list;
	ArrayAdapter<Item> list_adapter;
	List<Item> values;
	EditText count;
	TextView counted;
	String memento;
	boolean saveshown, deleteshown;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_inventory__count);
		memento = "";
		count =	(EditText) findViewById(R.id.countbox);
		countbox = (LinearLayout)this.findViewById(R.id.countlayout);
		intent = getIntent();
		iteminfo = intent.getExtras();
		date = iteminfo.getString("inv_date");
		counted = (TextView) this.findViewById(R.id.countedlabel);
		receiveview = linf.inflate(R.layout.received_dialog, null);
			getActionBar().setTitle(date + "Order");
			countbox.setVisibility(LinearLayout.INVISIBLE);
			editorview = linf.inflate(R.layout.count_edit, null);
			list.setOnItemLongClickListener(new ListView.OnItemLongClickListener(){
				@Override
				public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
						int arg2, long arg3) {
					deleteview = linf.inflate(R.layout.delete_dialog, null);
					editorview = linf.inflate(R.layout.received_change_dialog, null);
					launcheditdialog();
					adapter.notifyDataSetChanged();
					return false;
				}});
		
	//	cursor = this.getContentResolver().query(TABLE NAME HERE, WHICH COLUMN TO GRAB HERE, null, null, null);
	//	adapter = new SimpleCursorAdapter(this, ,cursor,null,null,null)
		countbox = (LinearLayout)this.findViewById(R.id.countlayout);
		 list = getListView();
	}

	public void restorevalue(){
		count.setText(memento);
	}
	
	public void edit_ordered(){
	counted.setText("Edit Ordered");	
	countbox.setVisibility(LinearLayout.VISIBLE);
	// Direct the "countbox" to the "Ordered" Value
	}
	
	public void deletebutton(){
		builder.setView(deleteview);
		final Toast deletesuccess = Toast.makeText(this, "Delete Successful", Toast.LENGTH_SHORT);
		builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				//DELETE THE INVENTORY, Possibly sql statement, something with the list to delete it from that as well??
				//IF DELETE IS SUCCESSFUL
				deletesuccess.show();	
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
	
	public void launcheditdialog(){
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

	public void clearCount(View view){
		Editable temp = count.getText();
		memento = temp.toString();
	count.setText(" ");
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
	public void saveinventory(){
		//Save order before quitting. It's called saveinventory so that we don't have to make an identical layout for the dialog box.
		Intent back_to_count = new Intent(this, OrderEditActivity.class);
		back_to_count.putExtra("query_type",false);
		startActivity(back_to_count);
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
	 savedialog = builder.create();
	 savedialog.show();
	 saveshown = true;
	 }
}

