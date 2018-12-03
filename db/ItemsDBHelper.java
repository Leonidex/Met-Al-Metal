package com.leonidex.db;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

public class ItemsDBHelper extends SQLiteOpenHelper implements ItemsDBConstants {

	private static String DB_PATH = "/data/data/com.leonidex.metalmetal/databases/";
	Context myContext;
	private SQLiteDatabase myDataBase;

	public ItemsDBHelper(Context context, String name, CursorFactory factory,
			int version) {
		super(context, name, factory, version);

		myContext = context;
		createDB();
		
		try {
			openMyDataBase();
			close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private final static String TAG = "ItemsDBHelper";

	@Override
	public void onCreate(SQLiteDatabase database) {
		Log.d(TAG, "Creating all the tables");

	}

	@Override
	public void onUpgrade(SQLiteDatabase arg0, int versionOld, int versionNew) {
		Log.e(TAG, "onUpgrade");
	}

	/**
	 * Creates a empty database on the system and rewrites it with your own
	 * database.
	 * */
	private void createDB() {

		boolean dbExist = checkDataBase();

		if (!dbExist) {

			// By calling this method and empty database will be created into
			// the default system path
			// of your application so we are going to be able to overwrite that
			// database with our database.
			this.getReadableDatabase();
			try {
				Log.d("createDB", "copyDataBase()");
				copyDataBase();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Check if the database already exist to avoid re-copying the file each
	 * time you open the application.
	 * 
	 * @return true if it exists, false if it doesn't
	 */
	private boolean checkDataBase() {

		SQLiteDatabase checkDB = null;

		try {
			String myPath = DB_PATH + DATABASE_NAME;
			checkDB = SQLiteDatabase.openDatabase(myPath, null,
					SQLiteDatabase.OPEN_READONLY);

		} catch (SQLiteException e) {
			Log.e("checkDataBase", "Database doesn't exist");
			// database does't exist yet.

		}

		if (checkDB != null) {

			checkDB.close();

		}

		return checkDB != null ? true : false;
	}

	/**
	 * Copies your database from your local assets-folder to the just created
	 * empty database in the system folder, from where it can be accessed and
	 * handled. This is done by transfering bytestream.
	 * */
	private void copyDataBase() throws IOException {

		// Open your local db as the input stream
		InputStream myInput = myContext.getAssets().open(DATABASE_NAME);

		// Path to the just created empty db
		String outFileName = DB_PATH + DATABASE_NAME;

		// Open the empty db as the output stream
		OutputStream myOutput = new FileOutputStream(outFileName);

		// transfer bytes from the inputfile to the outputfile
		byte[] buffer = new byte[1024];
		int length;
		while ((length = myInput.read(buffer)) > 0) {
			myOutput.write(buffer, 0, length);
		}

		// Close the streams
		myOutput.flush();
		myOutput.close();
		myInput.close();
	}

	public void openMyDataBase() throws SQLException {

		// Open the database
		String myPath = DB_PATH + DATABASE_NAME;
		myDataBase = SQLiteDatabase.openDatabase(myPath, null,
				SQLiteDatabase.OPEN_READONLY);

	}

	@Override
	public synchronized void close() {

		if (myDataBase != null)
			myDataBase.close();

		super.close();

	}
}
