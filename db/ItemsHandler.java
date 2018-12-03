package com.leonidex.db;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

public class ItemsHandler implements ItemsDBConstants {

	private final static String TAG = "ItemsHandler";
	private ItemsDBHelper helper;

	public ItemsHandler(Context context) {
		helper = new ItemsDBHelper(context, DATABASE_NAME, null, 1);
	}

	public List<Item> getAllItems() {
		List<Item> items = new ArrayList<Item>();
		SQLiteDatabase db = helper.getReadableDatabase();
		Cursor cursor = db.query(ITEMS_TABLE_NAME, null, null, null, null,
				null, ITEM_ID + " DESC");

		// Check if the item was found
		if (cursor.moveToFirst()) {
			int idColumnIndex = cursor.getColumnIndex(ITEM_ID);
			int titleColumnIndex = cursor.getColumnIndex(ITEM_TITLE);
			int dateColumnIndex = cursor.getColumnIndex(ITEM_DATE);
			int descriptionColumnIndex = cursor
					.getColumnIndex(ITEM_DESCRIPTION);
			int setlistColumnIndex = cursor.getColumnIndex(ITEM_SETLIST);
			int posterLinkColumnIndex = cursor.getColumnIndex(ITEM_POSTERLINK);
			int icastLinkColumnIndex = cursor.getColumnIndex(ITEM_ICASTLINK);

			do {
				int id = idColumnIndex == -1 ? 0 : cursor.getInt(idColumnIndex);
				String title = cursor.getString(titleColumnIndex);
				String date = cursor.getString(dateColumnIndex);
				String description = cursor.getString(descriptionColumnIndex);
				String setlist = cursor.getString(setlistColumnIndex);
				String posterLink = cursor.getString(posterLinkColumnIndex);
				String icastLink = cursor.getString(icastLinkColumnIndex);

				Item item = new Item(id, title, date, description, setlist,
						posterLink, icastLink);

				items.add(item);
			} while (cursor.moveToNext());

		}

		cursor.close();
		return items;
	}

	public Cursor getAllItemsCursor() {
		SQLiteDatabase db = helper.getReadableDatabase();
		Cursor cursor = db.query(ITEMS_TABLE_NAME, null, null, null, null,
				null, ITEM_ID + " DESC");

		return cursor;
	}

	public int getCount() {
		
		try {
		int count = helper.getReadableDatabase()
				.rawQuery("SELECT * FROM " + ITEMS_TABLE_NAME, null).getCount();
		return count;
		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		}
	}

	// Search for a value
	public Cursor getAllSuggestedValues(String value) {
		SQLiteDatabase db = helper.getReadableDatabase();
		
		// Removing quote marks
		while(value.contains("'")) {
			value = value.replace("'", "");
		}

		String s = "";
		s += '"';
		while(value.indexOf('"') != -1) {
			value = value.replace(s, "");
		}
		
		String selectionArgs = new String("'%" + value + "%'");

		// Searching specific columns
		String selection = ITEM_TITLE + " LIKE " + selectionArgs + " OR "
				+ ITEM_DESCRIPTION + " LIKE " + selectionArgs + " OR "
				+ ITEM_SETLIST + " LIKE " + selectionArgs + " OR " + ITEM_DATE
				+ " LIKE " + selectionArgs;
		Cursor cursor = db.query(ITEMS_TABLE_NAME, null, selection, null, null,
				null, ITEM_ID + " DESC");
		return cursor;
	}

	public String getLastItemTitle() {
		Cursor cursor = getAllItemsCursor();

		int titleColumnIndex = cursor.getColumnIndex(ITEM_TITLE);
		cursor.moveToFirst();	// Order is DESC so the first is actually the last item

		String title = "";

		try {
			title = cursor.getString(titleColumnIndex);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return title;
	}

	public Item addItem(String title, String date, String description,
			String setlist, String posterLink, String icastLink) {

		SQLiteDatabase database = helper.getWritableDatabase();

		ContentValues newItemValues = new ContentValues();

		newItemValues.put(ITEM_TITLE, title);
		newItemValues.put(ITEM_DATE, date);
		newItemValues.put(ITEM_DESCRIPTION, description);
		newItemValues.put(ITEM_SETLIST, setlist);
		newItemValues.put(ITEM_POSTERLINK, posterLink);
		newItemValues.put(ITEM_ICASTLINK, icastLink);

		Item item = null;
		// Inserting the new row, or throwing an exception if an error occurred
		try {
			long id = database.insertOrThrow(ITEMS_TABLE_NAME, null,
					newItemValues);
			item = new Item(id, title, date, description, setlist, posterLink,
					icastLink);
		} catch (SQLiteException ex) {
			Log.e(TAG, ex.getMessage());
			return null;
		} finally {
			database.close();
		}

		return item;
	}

	public Item addItem(Item item) {
		return addItem(item.getTitle(), item.getDate(), item.getDescription(),
				item.getSetlist(), item.getPosterLink(), item.getIcastLink());
	}

	public boolean deleteItem(long id) {
		SQLiteDatabase db = helper.getWritableDatabase();
		try {
			db.delete(ITEMS_TABLE_NAME,

			ITEM_ID + "=?",

			new String[] { String.valueOf(id) });
		} catch (SQLiteException ex) {
			Log.e(TAG, ex.getMessage());
			return false;
		} finally {
			db.close();
		}

		return true;
	}

	public Item updateItem(long id, String title, String date,
			String description, String setlist, String posterLink,
			String icastLink) {

		SQLiteDatabase database = helper.getWritableDatabase();

		ContentValues newItemValues = new ContentValues();

		newItemValues.put(ITEM_TITLE, title);
		newItemValues.put(ITEM_DATE, date);
		newItemValues.put(ITEM_DESCRIPTION, description);
		newItemValues.put(ITEM_SETLIST, setlist);
		newItemValues.put(ITEM_POSTERLINK, posterLink);
		newItemValues.put(ITEM_ICASTLINK, icastLink);

		Item item = null;

		String whereClause = ITEM_ID + "=?";
		String[] whereArgs = new String[] { String.valueOf(id) };

		try {
			int count = database.update(ITEMS_TABLE_NAME, newItemValues,
					whereClause, whereArgs);
			if (count > 0) {
				item = new Item(id, title, date, description, setlist,
						posterLink, icastLink);
				return item;
			} else {
				Log.e(TAG, "Update had no effect");
				return null;
			}
		} catch (SQLiteException e) {
			Log.e(TAG, e.getMessage());
			return null;
		} finally {
			database.close();
		}
	}

	public boolean deleteAll() {
		SQLiteDatabase db = helper.getWritableDatabase();
		try {
			db.delete(ITEMS_TABLE_NAME, "1", null);// execSQL("DELETE FROM " +
													// ITEMS_TABLE_NAME);
		} catch (SQLiteException ex) {
			Log.e(TAG, ex.getMessage());
			return false;
		} finally {
			db.close();
		}

		return true;
	}
}
