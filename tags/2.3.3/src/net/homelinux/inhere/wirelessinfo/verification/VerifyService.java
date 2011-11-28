package net.homelinux.inhere.wirelessinfo.verification;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.os.Environment;
import android.util.Log;
import net.homelinux.inhere.wirelessinfo.database.WirelessInfoDBAdapter;

public class VerifyService extends Activity {
	private Context context;
	
	public VerifyService(Context context) {
		this.context = context;
	}
	
	public boolean serverInfo() throws WirelessInfoException {
		WirelessInfoDBAdapter dbAdapter = new WirelessInfoDBAdapter(context);
		Cursor cursor;
		try {
	    	dbAdapter.open();
	    	cursor = dbAdapter.fetchServerInfoKeyNamePair();
	    	startManagingCursor(cursor);
	    	if (cursor.getCount() > 0) {
	    		return true;
	    	}
	    	trace("serverInfo: Opened DB");
	    	dbAdapter.close();
	    	return true;
	    } catch (SQLException e) {
	    	trace("serverInfo: Fail to open db "+e.getMessage());
	    	dbAdapter.close();
	    }

		throw new WirelessInfoException("Test Server info do not exist");
	}
	
	public boolean wirelessInfoTmpStor() throws WirelessInfoException {
		
		boolean mExternalStorageAvailable = false;
		boolean mExternalStorageWriteable = false;
		String state = Environment.getExternalStorageState();

		if (Environment.MEDIA_MOUNTED.equals(state)) {
		    // We can read and write the media
		    mExternalStorageAvailable = mExternalStorageWriteable = true;
		} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
		    // We can only read the media
		    mExternalStorageAvailable = true;
		    mExternalStorageWriteable = false;
		} else {
		    // Something else is wrong. It may be one of many other states, but all we need
		    //  to know is we can neither read nor write
		    mExternalStorageAvailable = mExternalStorageWriteable = false;
		}
		
		if (mExternalStorageAvailable && mExternalStorageWriteable) {
			return true;
		} else {
			throw new WirelessInfoException("External storage not available for writing");
		}
	}
	
	public void trace(String msg) {
		Log.d("WirelessInfo", VerifyService.class.getName()+": "+msg);
	}

}
