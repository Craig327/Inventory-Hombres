package com.example.inventoryhombres;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
//This activity will populate rows in a list view for updating/taking inventory etc.
public class TablePopulate extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_table_populate);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.table_populate, menu);
		return true;
	}

}
