package net.homelinux.inhere.wirelessinfo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.ToggleButton;

public class test extends Activity {
	
	String PUBLIC_STATIC_STRING_IDENTIFIER = null;
	private EditText InputFileName;
	private Button connectFtpButton;
	private ToggleButton ftpActionButton;
	LoginDetails serverLogin = null;
	private ThrPutTest mCurrentThrPutTask = null;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.test);
		
		InputFileName = (EditText) findViewById(R.id.inputFileName);
		
		connectFtpButton = (Button) findViewById(R.id.connectFtp);
		
		Spinner spinner = (Spinner) findViewById(R.id.fileSizeSpinner);
	    ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
	            this, R.array.fileSize_array, android.R.layout.simple_spinner_item);
	    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	    spinner.setAdapter(adapter);
	    
	    spinner.setOnItemSelectedListener(new MyOnItemSelectedListener());
	    
	    Bundle b = this.getIntent().getExtras();
		if (b.getBoolean("hostStatus")) {
			serverLogin = new LoginDetails();
	        serverLogin.setHost(b.getString("host"));
	        serverLogin.setPort(b.getInt("port"));
	        serverLogin.setId(b.getString("id"));
	        serverLogin.setPasswd(b.getString("passwd"));
        
	        trace("WirelessInfo: onClickThrputTest: End. Got server to test = "+serverLogin.getHost());
		} else {
			Toast.makeText(this, "Please set Login Details first!", Toast.LENGTH_SHORT).show();
		}
	}
	
	public void onClickBack(View v) {
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
	
	public class MyOnItemSelectedListener implements OnItemSelectedListener {
	    public void onItemSelected(AdapterView<?> parent,
	        View view, int pos, long id) {
	      trace("The file size = " +parent.getItemAtPosition(pos).toString());
	    }
	    public void onNothingSelected(AdapterView parent) {
	      // Do nothing.
	    }
	}
	
	public void onClickThrputTest(View v) {
		trace("WirelessInfo: onClickThrputTest: Start.");
		
		Bundle b = this.getIntent().getExtras();
		if (b.getBoolean("hostStatus")) {
			LoginDetails serverLogin = new LoginDetails();
	        serverLogin.setHost(b.getString("host"));
	        serverLogin.setPort(b.getInt("port"));
	        serverLogin.setId(b.getString("id"));
	        serverLogin.setPasswd(b.getString("passwd"));
        
	        trace("WirelessInfo: onClickThrputTest: End. Got server to test = "+serverLogin.getHost());
		} else {
			Toast.makeText(this, "Please set Login Details first!", Toast.LENGTH_SHORT).show();
		}
		
	}
	
	public void onClickConnect(View v) {
		String buttonType = (String) connectFtpButton.getText();
		ThrPutTest t = mCurrentThrPutTask;
		if (t != null) {
			connectFtpButton.setText("Disconnect");
			trace("test: onClickConnect: Still connected, disconnect first");
		} else {
			ThrPutTest ft = new ThrPutTest(this, "filename", serverLogin);
			mCurrentThrPutTask = ft;
			connectFtpButton.setText("Disconnect");
		}
	}
	
	public void onClickftpAction(View v) {
		ftpActionButton = (ToggleButton) findViewById(R.id.ftpAction);

		ThrPutTest t = mCurrentThrPutTask;
		if (t != null) {
			trace("test: onClickConnect: Still connected, disconnect first");
		} else {
			ThrPutTest ft = new ThrPutTest(this, "filename", serverLogin);
			mCurrentThrPutTask = ft;
		}
		
		// Connect to FTP server 
		if (ftpActionButton.isChecked()) {
			trace("test: onClickftpAction: ToggelButton isChecked, will connect.");
			mCurrentThrPutTask.mConnect();
		} else if (mCurrentThrPutTask != null ){
			trace("test: onClickftpAction: Will disconnect.");
			mCurrentThrPutTask.mDisconnect();
		}
	}
	
	public void trace(String msg) {
		Log.d("WirelessInfo", msg);
	}

}
