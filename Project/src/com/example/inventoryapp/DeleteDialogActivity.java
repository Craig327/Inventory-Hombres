package com.example.inventoryapp;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;

public class DeleteDialogActivity extends Activity implements
android.view.View.OnClickListener{
	private Button btnCancel, btnDelete;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.delete_dialog);

		btnCancel = (Button) findViewById(R.id.btn_cancel);
		btnDelete = (Button) findViewById(R.id.btn_delete);
		btnCancel.setOnClickListener(this);
		btnDelete.setOnClickListener(this);
		
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			boolean isNoSave = extras.getBoolean("isNoSave");
			if (isNoSave) btnDelete.setText(R.string.are_you_sure_lose_save);
		};
	}

	@Override
	public void onClick(View v) {
		Log.d("xxx", "onClick 1");
		Intent i = new Intent();
		setResult(RESULT_OK, i);
		switch (v.getId()) {
		case R.id.btn_cancel:
			Log.d("xxx", "onClick 1");
			finish();

			break;
		case R.id.btn_delete:
			Log.d("xxx", "onClick 2");
			i.putExtra("isDelete", true);
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
