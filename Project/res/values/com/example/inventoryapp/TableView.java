package com.example.inventoryapp;

import android.os.Bundle;
import android.app.ListActivity;
import android.content.Intent;
import android.view.View;

public class TableView extends ListActivity {
	String date;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_table_view);
		
		Intent info = getIntent();//get the intent 
		Bundle getdate = info.getExtras();//get stuff that was packed into intent
		date = (String) getdate.get("inv_date"); //The date chosen from the previous activity put into variable	
	}

	
	public void onClick(View view) {
	
	  }
}
