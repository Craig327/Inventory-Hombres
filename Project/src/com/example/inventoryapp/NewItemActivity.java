package com.example.inventoryapp;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

public class NewItemActivity extends Activity {
	
	public void saveItem(View button){
		
		//Get data from the text fields and spinner
		final EditText idField = (EditText) findViewById(R.id.EditTextID);
		String id = idField.getText().toString();
		
		final Spinner typeSpinner = (Spinner) findViewById(R.id.SpinnerItemType);
		String itemType = typeSpinner.getSelectedItem().toString();
				
		final EditText nameField = (EditText) findViewById(R.id.EditTextName);
		String name = nameField.getText().toString();

		final Spinner supplierSpinner = (Spinner) findViewById(R.id.SpinnerSupplier);
		String supplier = supplierSpinner.getSelectedItem().toString();
		//final EditText supplierField = (EditText) findViewById(R.id.EditTextSupplier);
		//String supplier = supplierField.getText().toString();
		
		final EditText minField = (EditText) findViewById(R.id.EditTextMin);
		String min = minField.getText().toString();
		
		final EditText maxField = (EditText) findViewById(R.id.EditTextMax);
		String max = maxField.getText().toString();
		
		if(id.isEmpty()){
			flagError("ID");
		}
		else if(itemType.isEmpty()){
			flagError("Item Type");
		}
		else if(name.isEmpty()){
			flagError("Name");
		}
		else if(supplier.isEmpty()){
			flagError("supplier");
		}
		else if(min.isEmpty()){
			flagError("Min Value");
		}
		else if(max.isEmpty()){
			flagError("Max Value");
		}
		else{
			//Package the data into a bundle
			Bundle bundle = new Bundle();
			bundle.putString("id", id);
			bundle.putString("name", name);
			bundle.putString("itemType", itemType);
			bundle.putString("supplier", supplier);
			bundle.putString("min", min);
			bundle.putString("max", max);
		
			//Create an intent, place the bundle into it, and return as the result of the calling method
			//**NOTE** intent.putExtra requires two fields, the name and what is being added
			// Name must include the package name as the prefix
			Intent intent=new Intent();
			intent.putExtra("com.example.inventoryApp.newItem", bundle);
			setResult(RESULT_OK, intent);
			finish();
		}
	}
	
	public void flagError(String field){
		final Toast saveFailed = Toast.makeText(this, "Missing value for " + field,
				Toast.LENGTH_SHORT);
		saveFailed.show();
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_new_item);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.new_item, menu);
		return true;
	}
	
	@Override
	public void onBackPressed() {
		Intent intent = new Intent();
		//set return type to all if new item is cancelled
		intent.putExtra("returnType", "All");
		setResult(RESULT_CANCELED, intent);
		finish();
		return;
	}

}
