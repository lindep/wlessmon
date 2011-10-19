package net.homelinux.inhere.wirelessinfo.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class WirelessInfoDBHelper extends SQLiteOpenHelper {
	private static final String DATABASE_NAME = "wirelessinfodb";

	private static final int DATABASE_VERSION = 4;

	// Database creation sql statement
	private static final String CREATE_TABLE_SERVERLOGIN = "create table serverLogin (_id integer primary key autoincrement, "
			+ "hostname text not null, port integer not null, loginid text not null, passwd text not null);";
         
  private static final String CREATE_TABLE_CELLINFO = "create table cellInfo (_id integer primary key autoincrement, "
			+ "cellid integer not null, sitename text not null, cellname text not null, lat real not null, lng real not null);";
 

	public WirelessInfoDBHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	// Method is called during creation of the database
	@Override
	public void onCreate(SQLiteDatabase database) {
		database.execSQL(CREATE_TABLE_SERVERLOGIN);
		database.execSQL(CREATE_TABLE_CELLINFO);
	}

	// Method is called during an upgrade of the database, e.g. if you increase
	// the database version
	@Override
	public void onUpgrade(SQLiteDatabase database, int oldVersion,
			int newVersion) {
		trace(WirelessInfoDBHelper.class.getName()+": Upgrading database from version " + oldVersion + " to "
						+ newVersion + ", which will destroy all old data");
		database.execSQL("DROP TABLE IF EXISTS serverLogin");
		database.execSQL("DROP TABLE IF EXISTS cellInfo");
		onCreate(database);
	}
	
	public void trace(String msg) {
		Log.d("WirelessInfo", msg);
	}
}