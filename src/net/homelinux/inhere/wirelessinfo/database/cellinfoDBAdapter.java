package net.homelinux.inhere.wirelessinfo.database;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class cellinfoDBAdapter {
	
	// Database fields
	public static final String KEY_ROWID = "_id";
	public static final String KEY_CELLID = "cellid";
	public static final String KEY_SITENAME = "sitename";
	public static final String KEY_CELLNAME = "cellname";
	public static final String KEY_LAT = "lat";
	public static final String KEY_LNG = "lng";
	private static final String DATABASE_TABLE = "cellInfo";
	private Context context;
	private SQLiteDatabase database;
	private WirelessInfoDBHelper dbHelper;

	public cellinfoDBAdapter(Context context) {
		this.context = context;
	}

	public cellinfoDBAdapter open() throws SQLException {
		dbHelper = new WirelessInfoDBHelper(context);
		database = dbHelper.getWritableDatabase();
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
                new String[] { KEY_ROWID, KEY_CELLID, KEY_SITENAME }, // Columns to return
                null,       // SQL WHERE
                null,       // Selection Args
                null,       // SQL GROUP BY 
                null,       // SQL HAVING
                KEY_CELLID);    // SQL ORDER BY
        return cursor;
    }

	/**
	 * Create a new info If the info is successfully created return the new
	 * rowId for that entry, otherwise return a -1 to indicate failure.
	 */
	public long createCellInfo(int cellid, String sitename, String cellname, double lat, double lng) {
		ContentValues initialValues = createContentValues(cellid, sitename, cellname, lat, lng);

		return database.insert(DATABASE_TABLE, null, initialValues);
	}

	/**
	 * Update the info
	 */
	public boolean updateCellInfo(long rowId, int cellid, String sitename, String cellname, double lat, double lng) {
		ContentValues updateValues = createContentValues(cellid, sitename, cellname, lat, lng);

		return database.update(DATABASE_TABLE, updateValues, KEY_ROWID + "="
				+ rowId, null) > 0;
	}

	/**
	 * Deletes info
	 */
	public boolean deleteCellInfo(long rowId) {
		return database.delete(DATABASE_TABLE, KEY_ROWID + "=" + rowId, null) > 0;
	}
	
	/**
	 * Deletes all info
	 */
	public boolean deleteAllCellInfo() {
		return database.delete(DATABASE_TABLE, null, null) > 0;
	}

	/**
	 * Return a Cursor over the list of all info in the database
	 * 
	 * @return Cursor over all notes
	 */
	public Cursor fetchAllCellInfos() {
		return database.query(DATABASE_TABLE, new String[] { KEY_ROWID,
				KEY_CELLID, KEY_SITENAME, KEY_CELLNAME, KEY_LAT, KEY_LNG }, null, null, null,
				null, null);
	}
	
	public Cursor fetchServerInfoKeyNamePair() {
		Cursor mCursor = database.query(DATABASE_TABLE, new String[] { KEY_ROWID,
				KEY_CELLID }, null, null, null, null, null);
		if (mCursor != null) {
			mCursor.moveToFirst();
		}
		return mCursor;
	}

	/**
	 * Return a Cursor positioned at the defined info
	 */
	public Cursor fetchServerInfo(long rowId) throws SQLException {
		Cursor mCursor = database.query(true, DATABASE_TABLE, new String[] {
				KEY_ROWID, KEY_CELLID, KEY_SITENAME, KEY_CELLNAME, KEY_LAT, KEY_LNG },
				KEY_ROWID + "=" + rowId, null, null, null, null, null);
		if (mCursor != null) {
			mCursor.moveToFirst();
		}
		return mCursor;
	}
	
	/**
	 * query cell info by cell name
	 * 
	 * @return Cursor for all records for this cell name
	 */
	public Cursor getInfoByCellName(String cellname) throws SQLException {
		final String SQL_STATEMENT = "SELECT "+KEY_ROWID+", "+KEY_CELLID+", "+KEY_SITENAME+", "+KEY_CELLNAME+", "+KEY_LAT+", "+KEY_LNG+" FROM "+DATABASE_TABLE+" WHERE "+KEY_CELLNAME+"=?";	
		Cursor mCursor = database.rawQuery(SQL_STATEMENT, new String[] { cellname });
		trace("getInfoByCellName: After SQL Query");    
		
		if (mCursor != null) {
			mCursor.moveToFirst();
		}
		return mCursor;
	}
	
	/**
	 * query cell info by cell ID
	 * 
	 * @return Cursor for all records for this cell ID
	 */
	public Cursor getInfoByCellId(String cellid) throws SQLException {
		final String SQL_STATEMENT = "SELECT "+KEY_ROWID+", "+KEY_CELLID+", "+KEY_SITENAME+", "+KEY_CELLNAME+", "+KEY_LAT+", "+KEY_LNG+" FROM "+DATABASE_TABLE+" WHERE "+KEY_CELLID+"=?";	
		Cursor mCursor = database.rawQuery(SQL_STATEMENT, new String[] { cellid });
		trace("getInfoByCellName: After SQL Query");    
		
		if (mCursor != null) {
			mCursor.moveToFirst();
		}
		return mCursor;
	}

	private ContentValues createContentValues(int cellid, String sitename, String cellname, double lat, double lng) {
		ContentValues values = new ContentValues();
		values.put(KEY_CELLID, cellid);
		values.put(KEY_SITENAME, sitename);
		values.put(KEY_CELLNAME, cellname);
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
		Log.d("WirelessInfo", cellinfoDBAdapter.class.getName()+": "+msg);
	}


}
