package net.homelinux.inhere.wirelessinfo.database;

import java.io.File;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.widget.Toast;

public class TmpStorage {
	private SQLiteDatabase myDB = null;
	private static final String DATABASE_FILE = "tmpstor.db";
	private static final String DATABASE_TABLE = "tmpstor";
	private static final String TABLE_TABLE = "rssi";
	private String stPathToDB;
	private boolean dbFileReady = false;
	
	public static final String KEY_ROWID = "_id";
	public static final String KEY_RECORDID = "recordid";
	public static final String KEY_RECORDTIME = "recordtime";
	public static final String KEY_CELLID = "cellid";
	public static final String KEY_LAT = "lat";
	public static final String KEY_LNG = "lng";
	
	private static final String CREATE_TABLE_CELLSTATS = "CREATE TABLE IF NOT EXISTS "+TABLE_TABLE+" (_id integer primary key autoincrement, "
		+ "imsi text not null, cellid integer not null, rssi integer not null, lat real not null, lng real not null);";

	public TmpStorage() {
		
		stPathToDB = android.os.Environment.getExternalStorageDirectory().toString()+"/wirelessinfo/"+DATABASE_FILE;
		if(createDirIfNotExists(stPathToDB)) {
			dbFileReady = true;
			
			openTmpStorForWriting();
		}
		
	}
	
	private boolean createDirIfNotExists(String path) { 
		boolean ret = true;

		trace("createDirIfNotExists: start file create");
	    File file = new File(android.os.Environment.getExternalStorageDirectory()+"/wirelessinfo");
	    if (file.exists()) {
	    	trace("createDirIfNotExists: Path exists already = "+file.toString());
	    	return true;
	    }
	    if (!file.mkdirs()) {
            trace("createDirIfNotExists: Problem creating file "+path);
            ret = false;
        }
	    return ret;
	}
	
	/**
	* Insert into tmpstorage
	*/
	public void insertData(String imsi, String cellid, int rssi, double lat, double lng) {
		   /* Insert data to a Table*/
		try {
		   myDB.execSQL("INSERT INTO "+ TABLE_TABLE + " (imsi, cellid, rssi, lat, lng)" + " VALUES ('"+imsi+"', "+Integer.parseInt(cellid)+", "+rssi+", "+lat+", "+lng+");");
		   trace("insertData: INSERT INTO "+ TABLE_TABLE + " (cellid, rssi, lat, lng)" + " VALUES ('"+imsi+"', "+cellid+", "+rssi+", "+lat+", "+lng+");");
		} catch (SQLException e) {
			trace ("insertData: "+e.getMessage());
		}
	}
	
	public void openTmpStorForWriting() {
		File dbfile = new File( stPathToDB );
		try {
			myDB = SQLiteDatabase.openOrCreateDatabase(dbfile, null);
			trace("openTmpStorForWriting: DB open");
			
			/* Create a Table in the Database. */
			myDB.execSQL("DROP TABLE IF EXISTS "+TABLE_TABLE);
			myDB.execSQL(CREATE_TABLE_CELLSTATS);
		} catch (Exception e) {
			   trace("Error "+e);
			   //Toast.makeText( this, "Can not create DB for recording "+e, Toast.LENGTH_SHORT ).show();
		}
	}  

    // Destructor
    public void finalize() {
    	if (myDB.isOpen()) {
            myDB.close();
    	}
    }
    
    public void close() {
    	myDB.close();
	}
    
    public void trace(String msg) {
		Log.d("WirelessInfo", TmpStorage.class.getName()+": "+msg);
	}
}