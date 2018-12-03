package com.leonidex.db;

import android.provider.BaseColumns;

public interface ItemsDBConstants {

	public static final String DATABASE_NAME = "items.db";

	public static final int DATABASE_VERSION = 1;
	public static final String ITEMS_TABLE_NAME = "items";
	
	public static final String ITEM_ID = BaseColumns._ID;
	public static final String ITEM_TITLE = "title";
	public static final String ITEM_DATE = "date";
	public static final String ITEM_DESCRIPTION = "description";
	public static final String ITEM_SETLIST = "setlist";
	public static final String ITEM_POSTERLINK = "posterlink";	// link to image
	public static final String ITEM_ICASTLINK = "icastlink";

	public static final String PREFS_NAME = "metalmetalprefs";
	
	// Number of items in the shipped database in assets
	public static final int ASSET_DB_COUNT = 301;
}
