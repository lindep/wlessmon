package net.homelinux.inhere.wirelessinfo.verification;

import android.content.Context;
import android.database.SQLException;
import android.util.Log;
import net.homelinux.inhere.wirelessinfo.test;
import net.homelinux.inhere.wirelessinfo.database.WirelessInfoDBAdapter;

public class VerifyService {
	private Context context;
	
	VerifyService(Context context) {
		this.context = context;
	}
	
	private boolean serverInfo() throws WirelessInfoException {
		WirelessInfoDBAdapter dbAdapter = new WirelessInfoDBAdapter(context);
		try {
	    	dbAdapter.open();
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
