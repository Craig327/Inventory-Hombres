package com.example.inventoryhombres;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
//This activity should be accessed from the Inventory button from the Main Activity, it will pull the tables from the database
//and dynamically create buttons representing each table
public class TableSelect extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_table_select);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.table_select, menu);
		return true;
	}

}
