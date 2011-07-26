package net.homelinux.inhere.wirelessinfo.database;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class WirelessInfoDBAdapter {
	
	// Database fields
	public static final String KEY_ROWID = "_id";
	public static final String KEY_HOSTNAME = "hostname";
	public static final String KEY_PORT = "port";
	public static final String KEY_LOGINID = "loginid";
	public static final String KEY_PASSWD = "passwd";
	private static final String DATABASE_TABLE = "serverLogin";
	private Context context;
	private SQLiteDatabase database;
	private WirelessInfoDBHelper dbHelper;

	public WirelessInfoDBAdapter(Context context) {
		this.context = context;
	}

	public WirelessInfoDBAdapter open() throws SQLException {
		dbHelper = new WirelessInfoDBHelper(context);
		database = dbHelper.getWritableDatabase();
		return this;
	}

	public void close() {
		dbHelper.close();
	}

	/**
	 * Create a new info If the info is successfully created return the new
	 * rowId for that note, otherwise return a -1 to indicate failure.
	 */
	public long createServerInfo(String hostname, int port, String loginid, String passwd) {
		ContentValues initialValues = createContentValues(hostname, port, loginid, passwd);

		return database.insert(DATABASE_TABLE, null, initialValues);
	}

	/**
	 * Update the info
	 */
	public boolean updateServerInfo(long rowId, String hostname, int port,
			String loginid, String passwd) {
		ContentValues updateValues = createContentValues(hostname, port, loginid, passwd);

		return database.update(DATABASE_TABLE, updateValues, KEY_ROWID + "="
				+ rowId, null) > 0;
	}

	/**
	 * Deletes info
	 */
	public boolean deleteServerInfo(long rowId) {
		return database.delete(DATABASE_TABLE, KEY_ROWID + "=" + rowId, null) > 0;
	}
	
	/**
	 * Deletes all info
	 */
	public boolean deleteAllServerInfo() {
		return database.delete(DATABASE_TABLE, null, null) > 0;
	}

	/**
	 * Return a Cursor over the list of all info in the database
	 * 
	 * @return Cursor over all notes
	 */
	public Cursor fetchAllServerInfos() {
		return database.query(DATABASE_TABLE, new String[] { KEY_ROWID,
				KEY_HOSTNAME, KEY_PORT, KEY_LOGINID, KEY_PASSWD }, null, null, null,
				null, null);
	}
	
	public Cursor fetchServerInfoKeyNamePair() {
		Cursor mCursor = database.query(DATABASE_TABLE, new String[] { KEY_ROWID,
				KEY_HOSTNAME }, null, null, null,
				null, null);
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
				KEY_ROWID, KEY_HOSTNAME, KEY_PORT, KEY_LOGINID, KEY_PASSWD },
				KEY_ROWID + "=" + rowId, null, null, null, null, null);
		if (mCursor != null) {
			mCursor.moveToFirst();
		}
		return mCursor;
	}

	private ContentValues createContentValues(String hostname, int port,
			String loginid, String passwd) {
		ContentValues values = new ContentValues();
		values.put(KEY_HOSTNAME, hostname);
		values.put(KEY_PORT, port);
		values.put(KEY_LOGINID, loginid);
		values.put(KEY_PASSWD, passwd);
		return values;
	}
	
	public List<String> selectAll() {
		      List<String> list = new ArrayList<String>();
		      Cursor cursor = this.database.query(DATABASE_TABLE, new String[] { "hostname" },
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


}
