package com.leonidex.db;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.leonidex.metalmetal.ImageFetcher;
import com.leonidex.metalmetal.R;

public class MyListAdapter extends CursorAdapter {

	public MyListAdapter(Cursor cursor, Context context) {
		super(context, cursor, true);
	}

	@Override
	public void bindView(View itemView, Context context, Cursor cursor) {
		
		Item item = new Item(cursor);
		
//		if(item.getId() % 2 == 0) {
//			itemView.setBackgroundColor(0xFF003333);
//		} else {
//			itemView.setBackgroundColor(0xFF330033);
//		}
		
		Holder holder = (Holder)itemView.getTag();
		holder.setItem(item);
		
		TextView itemTitleTextView = (TextView) itemView.findViewById(R.id.menu_item_title);
		TextView itemDateTextView = (TextView) itemView.findViewById(R.id.menu_item_date);
		
		String title = item.getTitle();
		title = title.replace("–", "\n");
		itemTitleTextView.setText(title);
		
		String date = item.getDate();
		date = date.replace("/20", "/");
		itemDateTextView.setText(date);
		
		ImageView iconImageView = (ImageView) itemView.findViewById(R.id.menu_item_icon);
		
		ImageFetcher imageFetcher = new ImageFetcher(context, iconImageView);
		imageFetcher.getImageLocation(item.getPosterLink(), true);
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup group) {
		
		Holder holder;
		View itemView = null;
		LayoutInflater inflater = (LayoutInflater) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
		itemView = inflater.inflate(R.layout.list_menu_item_row, group, false);
		holder = new Holder();
		itemView.setTag(holder);
		
		return itemView;
	}
}