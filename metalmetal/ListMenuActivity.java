package com.leonidex.metalmetal;

import android.animation.LayoutTransition;
import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.FilterQueryProvider;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;

import com.leonidex.db.Holder;
import com.leonidex.db.Item;
import com.leonidex.db.ItemsHandler;
import com.leonidex.db.MyListAdapter;

public class ListMenuActivity extends Activity implements Constants {

	FragmentManager fragManager;
	FragmentTransaction fragTransaction;

	ItemsHandler handler;
	MyListAdapter listAdapter;
	Cursor itemsCursor;
	ListView menuItemListView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_list_menu);

		handler = new ItemsHandler(this);

		// handler.deleteAll();

		// Must get all items cursor again after releasing it with the previous
		// delete function
		if (listAdapter == null) {
			itemsCursor = handler.getAllItemsCursor();
			listAdapter = new MyListAdapter(itemsCursor, this);

			// Items list view
			menuItemListView = (ListView) findViewById(R.id.menu_item_listview);
			menuItemListView.setAdapter(listAdapter);

			// Clicking on an item opens an ItemFragment for it
			menuItemListView.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> parent, View view,
						int position, long id) {
					Holder holder = (Holder) view.getTag();
					Item item = holder.getItem();

					Intent intent = new Intent(getApplicationContext(),
							ItemActivity.class);

					intent.putExtra("title", item.getTitle());
					intent.putExtra("date", item.getDate());
					intent.putExtra("description", item.getDescription());
					intent.putExtra("setlist", item.getSetlist());
					intent.putExtra("poster_link", item.getPosterLink());
					intent.putExtra("icast_link", item.getIcastLink());
					intent.putExtra("position", position);

					startActivityForResult(intent, REQUESTCODE_ITEM_ACTIVITY);
				}
			});

			listAdapter.setFilterQueryProvider(new FilterQueryProvider() {

				@Override
				public Cursor runQuery(CharSequence constraint) {
					String partialValue = constraint.toString();
					return handler.getAllSuggestedValues(partialValue);
				}
			});
		}

		// Getting last position
		Bundle bundle = getIntent().getExtras();
		if (bundle != null) {
			int position = bundle.getInt("position");
			menuItemListView.setSelection(position);
		}
	}
	
	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.list_menu, menu);

		MenuItem searchItem = menu.findItem(R.id.list_action_search);
		SearchView searchView = (SearchView) searchItem.getActionView();

		SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
		searchView.setSearchableInfo(searchManager
				.getSearchableInfo(getComponentName()));

		searchView.setOnQueryTextListener(new OnQueryTextListener() {

			@Override
			public boolean onQueryTextSubmit(String query) {
				return true;
			}

			@Override
			public boolean onQueryTextChange(String newText) {
				// listAdapter.getCursor().moveToFirst();
				listAdapter.swapCursor(handler.getAllSuggestedValues(newText));
				listAdapter.notifyDataSetChanged();
				return true;
			}
		});

		// Animating the searchview
		// Get the ID for the search bar LinearLayout
		int searchBarId = searchView.getContext().getResources()
				.getIdentifier("android:id/search_bar", null, null);

		// Get the search bar Linearlayout
		LinearLayout searchBar = (LinearLayout) searchView
				.findViewById(searchBarId);

		// Give the Linearlayout a transition animation.
		LayoutTransition transition = new LayoutTransition();
		transition.setDuration(100);
		searchBar.setLayoutTransition(transition);

		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		switch (id) {
			case R.id.action_main:
				finish();
				return true;
			
			case R.id.action_about:
				AboutDialog dialog = new AboutDialog();
				dialog.showAboutDialog(this);
				return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	    super.onActivityResult(requestCode, resultCode, data);
	    if (requestCode == REQUESTCODE_ITEM_ACTIVITY && resultCode == RESULT_OK) {
	    	if(data.getBooleanExtra("go_to_main", false)) {
	    		finish();
	    	}
	    }
	}
}
