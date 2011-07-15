package net.homelinux.inhere.wirelessinfo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

public class test extends Activity {
	
	String PUBLIC_STATIC_STRING_IDENTIFIER = null;
	private EditText InputFileName;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.test);
		
		InputFileName = (EditText) findViewById(R.id.inputFileName);
	}
	
	public void onClickTest(View v) {
		trace("test: onClickStartTest: Start.");
		
		Bundle b = this.getIntent().getExtras();
        String s = b.getString("IDENT");
        String s1 = b.getString("IDENT1");
        trace("From parent intent = "+s+", second string = "+s1);
        
        String FileName = InputFileName.getText().toString();
		
		try{
			Intent resultIntent = new Intent(getApplicationContext(),test.class);
			resultIntent.putExtra(PUBLIC_STATIC_STRING_IDENTIFIER, "data_to_activity");
			resultIntent.putExtra("FILE_NAME", FileName);
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
