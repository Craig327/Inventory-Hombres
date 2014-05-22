package com.example.inventoryapp;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.os.Bundle;
import android.app.Activity;
import android.app.DialogFragment;
import android.content.Intent;
import android.graphics.Rect;
import android.util.Log;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class DateDialogActivity extends Activity implements 
android.view.View.OnClickListener{
	String defaultDate;
	private boolean isEditNotNew;
	private Button btnStartOrSave;
	private EditText etDate;
	private String title;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			defaultDate = extras.getString("defaultDate");
			isEditNotNew = extras.getBoolean("isEditNotNew");
			title = extras.getString("title");
		} else{
			isEditNotNew = false;
			defaultDate = null;
		}
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.order_date_dialog);
		btnStartOrSave = (Button) findViewById(R.id.btn_start_save);
		if (isEditNotNew) btnStartOrSave.setText("Save");
		btnStartOrSave.setOnClickListener(this);
		TextView itemtitle = (TextView) findViewById(R.id.itemtitle);
		if (title != null) itemtitle.setText(title);
		etDate = (EditText) findViewById(R.id.choosedate);

		//insert date
		if (defaultDate != null) {
			etDate.setText(ItemsDataSource.dateSqlToStd(defaultDate));
		}
		else {
			Locale current;
			current = getResources().getConfiguration().locale;
			SimpleDateFormat sdf = new SimpleDateFormat("M/d/yyyy", current);
			etDate.setText(sdf.format(new Date()));
		}
	}

	public void showDatePickerDialog(View v) {
		DialogFragment newFragment = new DatePickerFragment();
		newFragment.show(getFragmentManager(), "datePicker");
	}
	
	public void setDate(String dte){
		etDate.setText(dte);
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.date_dialog, menu);
		return true;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {

		case R.id.btn_start_save:
			Intent i = new Intent();
			i.putExtra("defaultDate", defaultDate);
			i.putExtra("dateChosen", ItemsDataSource.dateStdToSql(etDate.getText().toString()));
			i.putExtra("isEditNotNew", isEditNotNew);
			setResult(RESULT_OK, i);
			finish();
			break;
		default:
			break;
		}
		finish();		
	}
	
	@Override
	public boolean dispatchTouchEvent(MotionEvent event) {
	    Rect dialogBounds = new Rect();
	    getWindow().getDecorView().getHitRect(dialogBounds);
	    if (!dialogBounds.contains((int) event.getX(), (int) event.getY()) && event.getAction() == MotionEvent.ACTION_DOWN){
	        // Tapped outside so we finish the activity, needed to set result so doesn't crash
			Intent i = new Intent();
			setResult(RESULT_OK, i);
			Log.d("xxx", "tapped outside the box");
	        this.finish();
	    }
	    return super.dispatchTouchEvent(event);
	}
	
	@Override
	public void onBackPressed() {
		Log.d("xxx", "onBackPressed Called");
		Intent i = new Intent();
		setResult(RESULT_OK, i);
		super.onBackPressed();
	}
}
