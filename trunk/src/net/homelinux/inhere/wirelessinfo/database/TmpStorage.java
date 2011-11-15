package net.homelinux.inhere.wirelessinfo.database;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import net.homelinux.inhere.wirelessinfo.verification.WirelessInfoException;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.widget.Toast;

public class TmpStorage {
	private SQLiteDatabase myDB = null;
	private static final String DATABASE_FILE = "tmpstor.db";
	private static final String TABLE_TABLE = "rssi";
	private String stPathToDB;
	private boolean dbFileReady = false;
	
	public static final String KEY_ROWID = "_id"; // KEY_ROWID, KEY_IMSI, KEY_TIMEENTER, KEY_CELLID, KEY_RSSI, KEY_LAT, KEY_LNG
	public static final String KEY_IMSI = "imsi";
	public static final String KEY_TIMEENTER = "timeEnter";
	public static final String KEY_CELLID = "cellid";
	public static final String KEY_RSSI = "rssi";
	public static final String KEY_LAT = "lat";
	public static final String KEY_LNG = "lng";
	
	private static final String CREATE_TABLE_CELLSTATS = "CREATE TABLE IF NOT EXISTS "+TABLE_TABLE+" (_id integer primary key autoincrement, timeEnter DATE,"
		+ "imsi text not null, cellid integer not null, rssi integer not null, lat real not null, lng real not null);";

	/**
	* Create temporary storage (true = new or false = update)
	*/
	public TmpStorage(boolean createNew) {
		
		stPathToDB = android.os.Environment.getExternalStorageDirectory().toString()+"/wirelessinfo/"+DATABASE_FILE;
		if(createDirIfNotExists(stPathToDB)) {
			dbFileReady = true;
			
			if (createNew) {
				openTmpStorForWriting();
			} else {
				openTmpStorForUpdate();
			}
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
		Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String formattedDate = df.format(c.getTime());
        
		try {
		   myDB.execSQL("INSERT INTO "+ TABLE_TABLE + " (timeEnter, imsi, cellid, rssi, lat, lng)" + " VALUES ('"+formattedDate+"', '"+imsi+"', "+Integer.parseInt(cellid)+", "+rssi+", "+lat+", "+lng+");");
		   trace("insertData: INSERT INTO "+ TABLE_TABLE + " (timeEnter, cellid, rssi, lat, lng)" + " VALUES ('"+formattedDate+"','"+imsi+"', "+cellid+", "+rssi+", "+lat+", "+lng+");");
		} catch (SQLException e) {
			trace ("insertData: "+e.getMessage());
		}
	}
	
	public void openTmpStorForUpdate() {
		File dbfile = new File( stPathToDB );
		try {
			myDB = SQLiteDatabase.openOrCreateDatabase(dbfile, null);
			trace("openTmpStorForUpdate: DB open");
			
			/* Create a Table in the Database. */
			myDB.execSQL(CREATE_TABLE_CELLSTATS);
		} catch (Exception e) {
			   trace("Error "+e);
			   //Toast.makeText( this, "Can not create DB for recording "+e, Toast.LENGTH_SHORT ).show();
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
	
	/**
     * Select All returns a cursor
     * @return the cursor for the DB selection
     * return KEY_IMSI, KEY_TIMEENTER, KEY_CELLID, KEY_RSSI, KEY_LAT, KEY_LNG
     */
    public Cursor SelectAll() throws WirelessInfoException {
    	Cursor cursor;
    	try {
	        cursor = myDB.query(
	        		TABLE_TABLE, // Table Name
	                new String[] { KEY_IMSI, KEY_TIMEENTER, KEY_CELLID, KEY_RSSI, KEY_LAT, KEY_LNG }, // Columns to return
	                null,       // SQL WHERE
	                null,       // Selection Args
	                null,       // SQL GROUP BY 
	                null,       // SQL HAVING
	                KEY_ROWID);    // SQL ORDER BY
	        if (cursor != null) {
	        	cursor.moveToFirst();
			}
	       
    	} catch (SQLException e) {
    		throw new WirelessInfoException(e.getMessage()); 
    	}
    	return cursor;
    }

    // Destructor
    public void finalize() {
    	if (myDB.isOpen()) {
            myDB.close();
    	}
    }
    
    public void close() {
    	trace("close: Closing DB");
    	myDB.close();
	}
    
    public void trace(String msg) {
		Log.d("WirelessInfo", TmpStorage.class.getName()+": "+msg);
	}
}
