package net.homelinux.inhere.wirelessinfo.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class IntTmpStoreDBHelper extends SQLiteOpenHelper {
	private static final String DATABASE_NAME = "tmpstoredb";
	private static final String DATABASE_TABLE = "tmpstore";

	private static final int DATABASE_VERSION = 1;

	// Database creation sql statement
	private static final String CREATE_TABLE_USERSTATS = "CREATE TABLE IF NOT EXISTS "+DATABASE_TABLE+" (_id integer primary key autoincrement, timeEnter DATE,"
	+ "imsi text not null, cellid integer not null, rssi integer not null, lat real not null, lng real not null);";

	public IntTmpStoreDBHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	// Method is called during creation of the database
	@Override
	public void onCreate(SQLiteDatabase database) {
		database.execSQL(CREATE_TABLE_USERSTATS);
	}

	// Method is called during an upgrade of the database, e.g. if you increase
	// the database version
	@Override
	public void onUpgrade(SQLiteDatabase database, int oldVersion,
			int newVersion) {
		trace(IntTmpStoreDBHelper.class.getName()+": Upgrading database from version " + oldVersion + " to "
						+ newVersion + ", which will destroy all old data");
		database.execSQL("DROP TABLE IF EXISTS "+DATABASE_TABLE);
		onCreate(database);
	}
	
	public void trace(String msg) {
		Log.d("WirelessInfo", msg);
	}
}