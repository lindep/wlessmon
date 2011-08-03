package net.homelinux.inhere.wirelessinfo.verification;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
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
	
	public void trace(String msg) {
		Log.d("WirelessInfo", VerifyService.class.getName()+": "+msg);
	}

}
