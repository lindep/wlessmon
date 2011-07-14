package net.homelinux.inhere.wirelessinfo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

public class test extends Activity {
	
	String PUBLIC_STATIC_STRING_IDENTIFIER = null;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.test);
		
		Bundle b = this.getIntent().getExtras();
        String s = b.getString("IDENT");
        trace("From parent intent = "+s);
		
		
		/*Intent resultIntent = new Intent();
		String tabIndexValue = "inside";
		resultIntent.putExtra(PUBLIC_STATIC_STRING_IDENTIFIER, tabIndexValue);
		setResult(Activity.RESULT_OK, resultIntent);
		finish();*/
		
		try{
			Intent resultIntent = new Intent(getApplicationContext(),test.class);
			resultIntent.putExtra(PUBLIC_STATIC_STRING_IDENTIFIER, "data_to_activity");
			setResult(Activity.RESULT_OK, resultIntent);				
			finish();
		}catch (Exception e){
			trace("test intent eror ."+e.getMessage());
		}	
		trace("test intent close.");
	}
	
	public void trace(String msg) {
		Log.d("WirelessInfo", msg);
	}

}
