package com.example.inventoryapp;

import android.database.Cursor;
import android.support.v4.widget.SimpleCursorAdapter.ViewBinder;
import android.view.View;

public class CustomViewBinder implements ViewBinder {
		@Override
		public boolean setViewValue(View view, Cursor cursor, int columnIndex){
			int onhand;
			// cursor.getColumnIndex(/*Get number in stock and put here*/);
			onhand = cursor.getInt(columnIndex);
			return false;
		}
}
