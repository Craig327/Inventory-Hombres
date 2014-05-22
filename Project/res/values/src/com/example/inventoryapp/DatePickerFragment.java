package com.example.inventoryapp;

import java.util.Calendar;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.widget.DatePicker;

public class DatePickerFragment extends DialogFragment
implements DatePickerDialog.OnDateSetListener {

@Override
public Dialog onCreateDialog(Bundle savedInstanceState) {
// Use the current date as the default date in the picker
final Calendar c = Calendar.getInstance();
int year = c.get(Calendar.YEAR);
int month = c.get(Calendar.MONTH);
int day = c.get(Calendar.DAY_OF_MONTH);

// Create a new instance of DatePickerDialog and return it
return new DatePickerDialog(getActivity(), this, year, month, day);
}

public void onDateSet(DatePicker view, int year, int month, int day) {
	Activity activity = getActivity();
	if  (activity instanceof InventoryActivity){
	if(((InventoryActivity)getActivity()).editcalled == true){
		((InventoryActivity)getActivity()).setItemDate((month+1) + "/" + day + "/" + year);
		((InventoryActivity)getActivity()).editcalled = false;
	}
	else if(((InventoryActivity)getActivity()).newcalled == true){
		((InventoryActivity)getActivity()).setDate((month+1) + "/" + day + "/" + year);
		((InventoryActivity)getActivity()).newcalled = false;
	}
	if(activity instanceof OrderEditActivity){
	if(((OrderEditActivity)getActivity()).datecalled == true){
		((OrderEditActivity)getActivity()).setDate((month+1) + "/" + day + "/" + year);
		((OrderEditActivity)getActivity()).datecalled = false;
	}
	}
	}
}}