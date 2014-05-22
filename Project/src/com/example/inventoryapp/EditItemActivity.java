package com.example.inventoryapp;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;

public class EditItemActivity extends Activity {
	//keep track of the old name so that we can search by it and replace it in the database
	String oldName = "";
	//keep track of old type in global scope to set the view on back pressed
	String returnType = "";
	
	public void editItem(View button){
		//Get data from the text fields and spinner
		final EditText idField = (EditText) findViewById(R.id.EditTextID);
		String id = idField.getText().toString();
				
		final Spinner typeSpinner = (Spinner) findViewById(R.id.SpinnerItemType);
		String itemType = typeSpinner.getSelectedItem().toString();
						
		final EditText nameField = (EditText) findViewById(R.id.EditTextName);
		String name = nameField.getText().toString();

		final Spinner supplierSpinner = (Spinner) findViewById(R.id.SpinnerSupplier);
		String supplier = supplierSpinner.getSelectedItem().toString();
				
		final EditText minField = (EditText) findViewById(R.id.EditTextMin);
		String min = minField.getText().toString();
				
		final EditText maxField = (EditText) findViewById(R.id.EditTextMax);
		String max = maxField.getText().toString();
				
		//Package the data into a bundle
		Bundle bundle = new Bundle();
		bundle.putString("id", id);
		bundle.putString("name", name);
		bundle.putString("itemType", itemType);
		bundle.putString("supplier", supplier);
		bundle.putString("min", min);
		bundle.putString("max", max);
		bundle.putString("oldName", oldName);
				
		//Create an intent, place the bundle into it, and return as the result of the calling method
		//**NOTE** intent.putExtra requires two fields, the name and what is being added
		// Name must include the package name as the prefix
		Intent intent=new Intent();
		intent.putExtra("com.example.inventoryApp.editItem", bundle);
		setResult(RESULT_OK, intent);
		finish();
	}
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_edit_item);
		
		Bundle item = this.getIntent().getExtras();
		oldName= item.getString("name");
		returnType = item.getString("itemType");
		
		final EditText idField = (EditText) findViewById(R.id.EditTextID);
		idField.setText(item.getString("id"));
		
		final Spinner typeField = (Spinner) findViewById(R.id.SpinnerItemType);
		int index = getIndex(typeField, item.getString("itemType"));
		typeField.setSelection(index);
		
		final EditText nameField = (EditText) findViewById(R.id.EditTextName);
		nameField.setText(item.getString("name"));
		
		final Spinner supplierField = (Spinner) findViewById(R.id.SpinnerSupplier);
		int index2 = getIndex(supplierField, item.getString("supplier"));
		supplierField.setSelection(index2);
		
		final EditText minField = (EditText) findViewById(R.id.EditTextMin);
		minField.setText(item.getString("min"));
		
		final EditText maxField = (EditText) findViewById(R.id.EditTextMax);
		maxField.setText(item.getString("max"));
	}
	
	private int getIndex(Spinner spinner, String myString){

        int index = 0;

        for (int i=0;i<spinner.getCount();i++){
            if (spinner.getItemAtPosition(i).equals(myString)){
                index = i;
            }
        }
        return index;
}
	    
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.edit_item, menu);
		return true;
	}
	@Override
	public void onBackPressed() {
		Intent intent = new Intent();
		intent.putExtra("returnType", returnType);
		setResult(RESULT_CANCELED, intent);
		finish();
		return;
	}

}
