package com.example.inventoryapp;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		//MySQLiteHelper sqlhelper = new MySQLiteHelper(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
//		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	public void launchInventory(View view){
		Intent intent = new Intent(this, InventoryActivity.class);
		//Send false if you're looking for Orders, true if looking for inventory
		intent.putExtra("query_type",true);
		startActivity(intent);
	}
	
	public void launchOrders(View view){
		Intent intent = new Intent(this, InventoryActivity.class);
		//Send false if you're looking for Orders, true if looking for inventory
		intent.putExtra("query_type",false);
		startActivity(intent);
	}
	
	public void launchDatabase(View view){	
	Intent intent = new Intent(this, DatabaseItems.class);
	startActivity(intent);
	}

}
