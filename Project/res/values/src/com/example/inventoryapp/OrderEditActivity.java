package com.example.inventoryapp;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.support.v4.widget.SimpleCursorAdapter;
import android.text.Editable;
import android.app.AlertDialog;
import android.app.DialogFragment;
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

public class OrderEditActivity extends ListActivity {
	Intent intent;
	Bundle iteminfo;
	String itemname,date;
	Cursor cursor;
	SimpleCursorAdapter adapter;
	LinearLayout countbox;
	AlertDialog.Builder builder;
	AlertDialog editdialog, deletedialog, receiveddialog;
	View editorview, deleteview, receiveview;
	LayoutInflater linf;
	ListView list;
	ArrayAdapter<Item> list_adapter;
	List<Item> values;
	EditText count,datedialog;
	TextView counted;
	String memento;
	Locale current;
	boolean datecalled;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_order_edit);
		datecalled = false;
		current = getResources().getConfiguration().locale;
		builder = new AlertDialog.Builder(this);
		linf = LayoutInflater.from(this);
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
			 list = getListView();
			list.setOnItemLongClickListener(new ListView.OnItemLongClickListener(){
				@Override
				public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
						int arg2, long arg3) {
					deleteview = linf.inflate(R.layout.delete_dialog, null);
					editorview = linf.inflate(R.layout.count_edit, null);
					launcheditdialog();
					adapter.notifyDataSetChanged();
					return false;
				}});
		
	//	cursor = this.getContentResolver().query(TABLE NAME HERE, WHICH COLUMN TO GRAB HERE, null, null, null);
	//	adapter = new SimpleCursorAdapter(this, ,cursor,null,null,null)
		countbox = (LinearLayout)this.findViewById(R.id.countlayout);
		datedialog = (EditText) receiveview.findViewById(R.id.choosedate);
		SimpleDateFormat sdf = new SimpleDateFormat( "MM/dd/yyyy", current ); 
		datedialog.setText( sdf.format( new Date() ));
	}

	public void restorevalue(){
		count.setText(memento);
	}
	
	public void gotoreceivedorder(){
		Editable temp = datedialog.getText();
		date = temp.toString();
		Intent to_new_order = new Intent(this, Inventory_Count.class);
		to_new_order.putExtra("exists", true);
		to_new_order.putExtra("thedate", date);
		to_new_order.putExtra("inv", false);
		startActivity(to_new_order);
		receiveddialog.cancel(); 
		// Send over database info that will populate the list with relevant "received" information to edit
	}
	
	public void receivedbutton(View v){
		datecalled = true;
		builder.setView(receiveview);
		
		builder.setPositiveButton("Start", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				gotoreceivedorder();
			}
		});
		builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();		
			}
		});
		receiveddialog =builder.create();
		receiveddialog.show();
	}
	
	public void edit_ordered(){
	counted.setText("Edit Ordered");	
	countbox.setVisibility(LinearLayout.VISIBLE);
	// Direct the "countbox" to the "Ordered" Value
	}
	
	public void edit_received(){
		counted.setText("Edit Received");
		countbox.setVisibility(LinearLayout.VISIBLE);
		// Direct the "countbox" to the "Received" Value
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
		deletedialog = builder.create();
		deletedialog.show();
	}
	
	public void editbutton(){
		countbox.setVisibility(LinearLayout.VISIBLE);
		counted.setText("Edit Ordered");
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
	
	public void showDatePickerDialog(View v){
		DialogFragment newFragment = new DatePickerFragment();
	    newFragment.show(getFragmentManager(), "datePicker");
	}
	
	 public void setDate(String date){ //Sets date of EditText object to send to TableView, DOES NOT SET DATE FOR INVENTORY ITEM
		  datedialog.setText(date);
		}
}

