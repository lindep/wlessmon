package net.homelinux.inhere.wirelessinfo.database;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class IntTmpStoreDBAdapter {
	
	// Database fields
	public static final String KEY_ROWID = "_id"; // KEY_ROWID, KEY_IMSI, KEY_TIMEENTER, KEY_CELLID, KEY_RSSI, KEY_LAT, KEY_LNG
	public static final String KEY_IMSI = "imsi";
	public static final String KEY_TIMEENTER = "timeEnter";
	public static final String KEY_CELLID = "cellid";
	public static final String KEY_RSSI = "rssi";
	public static final String KEY_LAT = "lat";
	public static final String KEY_LNG = "lng";
	private static final String DATABASE_TABLE = "tmpstore";
	private Context context;
	private SQLiteDatabase database;
	private IntTmpStoreDBHelper dbHelper;

	public IntTmpStoreDBAdapter(Context context) {
		this.context = context;
	}

	public IntTmpStoreDBAdapter open() throws SQLException {
		dbHelper = new IntTmpStoreDBHelper(context);
		database = dbHelper.getWritableDatabase();
		//start with empty table
		database.delete(DATABASE_TABLE, null, null);
		trace("open: Got writable database");
		return this;
	}

	public void close() {
		dbHelper.close();
	}
	
	/**
     * Select All returns a cursor
     * @return the cursor for the DB selection
     */
    public Cursor cursorSelectAll() {
        Cursor cursor = database.query(
        		DATABASE_TABLE, // Table Name
                new String[] { KEY_ROWID, KEY_IMSI, KEY_TIMEENTER, KEY_CELLID, KEY_RSSI, KEY_LAT, KEY_LNG }, // Columns to return
                null,       // SQL WHERE
                null,       // Selection Args
                null,       // SQL GROUP BY 
                null,       // SQL HAVING
                KEY_ROWID);    // SQL ORDER BY
        return cursor;
    }

	/**
	 * Create a new info If the info is successfully created return the new
	 * String imsi, String timeEnter, int cellid, int rssi, double lat, double lng
	 * rowId for that entry, otherwise return a -1 to indicate failure.
	 */
	public long createCellInfo(String imsi, String timeEnter, int cellid, int rssi, double lat, double lng) {
		trace("createCellInfo: Adding record to internal storage");
		if (timeEnter == null) {
			Calendar c = Calendar.getInstance();
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			timeEnter = df.format(c.getTime());
		}
		ContentValues initialValues = createContentValues(imsi, timeEnter, cellid, rssi, lat, lng);

		return database.insert(DATABASE_TABLE, null, initialValues);
	}
	
	//KEY_ROWID, KEY_IMSI, KEY_TIMEENTER, KEY_CELLID, KEY_RSSI, KEY_LAT, KEY_LNG
	private ContentValues createContentValues(String imsi, String timeEnter, int cellid, int rssi, double lat, double lng) {
		ContentValues values = new ContentValues();
		values.put(KEY_CELLID, cellid);
		values.put(KEY_IMSI, imsi);
		values.put(KEY_TIMEENTER, timeEnter);
		values.put(KEY_RSSI, rssi);
		values.put(KEY_LAT, lat);
		values.put(KEY_LNG, lng);
		return values;
	}
	
	public List<String> selectAll() {
		      List<String> list = new ArrayList<String>();
		      Cursor cursor = this.database.query(DATABASE_TABLE, new String[] { "cellid" },
		        null, null, null, null, "_id ASC");
		      if (cursor.moveToFirst()) {
		         do {
		            list.add(cursor.getString(0));
		         } while (cursor.moveToNext());
		      }
		      if (cursor != null && !cursor.isClosed()) {
		         cursor.close();
		      }
		      return list;
	}
	
    /**
     * Select All that returns an ArrayList
     * @return the ArrayList for the DB selection
     * public class Friend {
    	public String id;
    	public String name;
    	public byte[] picture;
    	public Bitmap pictureBitmap;;
	}
     */
	/*
    public ArrayList<Friend> listSelectAll() {
        ArrayList<Friend> list = new ArrayList<Friend>();
        Cursor cursor = this.db.query(TABLE_NAME, 
        		new String[] { "fid", "name" }, 
        		null, null, null, null, "name");
        if (cursor.moveToFirst()) {
            do {
                Friend f = new Friend();
                f.id = cursor.getString(0);
                f.name = cursor.getString(1);
                list.add(f);
            } while (cursor.moveToNext());
        }
        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }
        return list;
    }
    */
	
	public int getDbInfo() {
		final String SQL_STATEMENT = "SELECT count(*) as _id FROM "+DATABASE_TABLE;	
		Cursor mCursor = database.rawQuery(SQL_STATEMENT, new String [] {});
		trace("getDbInfo: After SQL Query");    
		
		if (mCursor != null) {
			mCursor.moveToFirst();
		}
		return mCursor.getInt(mCursor.getColumnIndex("_id"));
	}
	
	public void trace(String msg) {
		Log.d("WirelessInfo", IntTmpStoreDBAdapter.class.getName()+": "+msg);
	}


}
