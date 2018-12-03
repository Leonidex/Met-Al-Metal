package com.leonidex.db;

import android.database.Cursor;

public class Item implements ItemsDBConstants {

	private final static String TAG = "Item";

	private long id;
	private String title;
	private String date;
	private String description;
	private String setlist;
	private String posterLink;
	private String icastLink;

	public Item() {

	}

	public Item(Cursor cursor) {
		int idColumnIndex = cursor.getColumnIndex(ITEM_ID);
		int titleColumnIndex = cursor.getColumnIndex(ITEM_TITLE);
		int dateColumnIndex = cursor.getColumnIndex(ITEM_DATE);
		int descriptionColumnIndex = cursor.getColumnIndex(ITEM_DESCRIPTION);
		int setlistColumnIndex = cursor.getColumnIndex(ITEM_SETLIST);
		int posterLinkColumnIndex = cursor.getColumnIndex(ITEM_POSTERLINK);
		int icastLinkColumnIndex = cursor.getColumnIndex(ITEM_ICASTLINK);

		id = idColumnIndex == -1 ? 0 : cursor.getInt(idColumnIndex);
		title = cursor.getString(titleColumnIndex);
		date = cursor.getString(dateColumnIndex);
		description = cursor.getString(descriptionColumnIndex);
		setlist = cursor.getString(setlistColumnIndex);
		posterLink = cursor.getString(posterLinkColumnIndex);
		icastLink = cursor.getString(icastLinkColumnIndex);
	}

	public Item(long id, String title, String date, String description,
			String setlist, String posterLink, String icastLink) {
		this.id = id;
		this.title = title;
		this.date = date;
		this.description = description;
		this.setlist = setlist;
		this.posterLink = posterLink;
		this.icastLink = icastLink;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getDescription() {
		String quoteMark = "";
		quoteMark += '"';
		
		while(description.contains("&quot;")) {
			description = description.replace("&quot;", quoteMark);
		}
		
		while(description.contains("&#8230;")) {
			description = description.replace("&#8230;", "... ");
		}
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getSetlist() {
		return setlist;
	}

	public void setSetlist(String setlist) {
		this.setlist = setlist;
	}

	public String getPosterLink() {
		return posterLink;
	}

	public void setPosterLink(String posterLink) {
		this.posterLink = posterLink;
	}

	public String getIcastLink() {
		return icastLink;
	}

	public void setIcastLink(String icastLink) {
		this.icastLink = icastLink;
	}
}
