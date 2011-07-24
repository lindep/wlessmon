package net.homelinux.inhere.wirelessinfo.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class WirelessInfoDBHelper extends SQLiteOpenHelper {
	private static final String DATABASE_NAME = "wirelessinfodb";

	private static final int DATABASE_VERSION = 2;

	// Database creation sql statement
	private static final String DATABASE_CREATE = "create table serverLogin (_id integer primary key autoincrement, "
			+ "hostname text not null, port integer not null, loginid text not null, passwd text not null);";

	public WirelessInfoDBHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	// Method is called during creation of the database
	@Override
	public void onCreate(SQLiteDatabase database) {
		database.execSQL(DATABASE_CREATE);
	}

	// Method is called during an upgrade of the database, e.g. if you increase
	// the database version
	@Override
	public void onUpgrade(SQLiteDatabase database, int oldVersion,
			int newVersion) {
		trace(WirelessInfoDBHelper.class.getName()+": Upgrading database from version " + oldVersion + " to "
						+ newVersion + ", which will destroy all old data");
		database.execSQL("DROP TABLE IF EXISTS serverLogin");
		onCreate(database);
	}
	
	public void trace(String msg) {
		Log.d("WirelessInfo", msg);
	}
}