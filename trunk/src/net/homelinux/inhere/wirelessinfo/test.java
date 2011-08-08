package net.homelinux.inhere.wirelessinfo;

import java.util.List;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import net.homelinux.inhere.wirelessinfo.database.WirelessInfoDBAdapter;

public class test extends Activity  {
	//implements AdapterView.OnItemSelectedListener
	String PUBLIC_STATIC_STRING_IDENTIFIER = null;
	private EditText InputFileName;
	private Button connectFtpButton;
	private ToggleButton ftpActionButton;
	LoginDetails serverLogin = null;
	LoginDetails[] serverLoginObj = new LoginDetails[2];
	private ThrPutTest mCurrentThrPutTask = null;
	
	private WirelessInfoDBAdapter dbAdapter;
	
	private Spinner spinServerName;
	private Cursor cursor;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.test);
		
		InputFileName = (EditText) findViewById(R.id.inputFileName);
		TextView tvTrace = (TextView) findViewById(R.id.trace);
		connectFtpButton = (Button) findViewById(R.id.connectFtp);
		
		spinServerName = (Spinner) findViewById(R.id.ftpHostSpinner);
		
		Spinner spinner = (Spinner) findViewById(R.id.fileSizeSpinner);
	    ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
	            this, R.array.fileSize_array, android.R.layout.simple_spinner_item);
	    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	    spinner.setAdapter(adapter);
	    
	    spinner.setOnItemSelectedListener(new MyOnItemSelectedListener());
	    
	    
	    Intent intent = getIntent();
	    Parcelable p = intent.getParcelableExtra("serverLogin");
	    LoginDetails serverLoginObjTest = intent.getParcelableExtra("serverLogin");
	    //serverLoginObj = (LoginDetails[]) intent.getParcelableArrayExtra("arrayServerlogin");
	    
	    //LoginDetails serverLoginObj = (LoginDetails)this.getIntent().getParcelableExtra("serverLogin");
	    
	    dbAdapter = new WirelessInfoDBAdapter(this);
	    try {
	    	dbAdapter.open();
	    	trace("Opened DB");
	    	fillSpinner();
	    	spinServerName.setOnItemSelectedListener(new MyServerNameItemSelectedListener());	    	
	    	dbAdapter.close();
	    } catch (SQLException e) {
	    	trace("onCreate: Fail to open db "+e.getMessage());
	    	dbAdapter.close();
	    }
	    
	    
	    Bundle b = this.getIntent().getExtras();
		if (b.getBoolean("hostStatus")) {
			serverLogin = new LoginDetails();
	        serverLogin.setHost(b.getString("host"));
	        serverLogin.setPort(b.getInt("port"));
	        serverLogin.setId(b.getString("id"));
	        serverLogin.setPasswd(b.getString("passwd"));
        
	        trace("onCreate: End. Got server to test = "+serverLogin.getHost());
		} else {
			Toast.makeText(this, "Please set Login Details first!", Toast.LENGTH_SHORT).show();
		}
	}
	
	private void fillSpinner() {
		//cursor = dbAdapter.fetchAllServerInfos();
		cursor = dbAdapter.fetchServerInfoKeyNamePair();
    	startManagingCursor(cursor);
    	
    	String[] from = new String[] { WirelessInfoDBAdapter.KEY_HOSTNAME, WirelessInfoDBAdapter.KEY_ROWID };
		int[] to = new int[] { android.R.id.text1 };
		SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, android.R.layout.simple_spinner_item, cursor, from, to);
		//SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, android.R.layout.two_line_list_item, cursor, from, to);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		//Spinner s = (Spinner) findViewById(R.id.ftpHostSpinner);
		spinServerName.setAdapter(adapter);
    	
	}
/*	
	public void onItemSelected(AdapterView<?> parent,
		View v, int position, long id) {
		setListAdapter(listAdapters[position]);
	}
	
	public void onNothingSelected(AdapterView<?> parent) {
	// ignore
	}
*/	
	public void onClickBack(View v) {
		trace("onClickBack: Start.");
		
		Bundle b = this.getIntent().getExtras();
        String s = b.getString("IDENT");
        String s1 = b.getString("IDENT1");
        trace("onClickBack: From parent intent = "+s+", second string = "+s1);
        
        String FileName = InputFileName.getText().toString();
		
		try{
			Intent resultIntent = new Intent(getApplicationContext(),test.class);
			resultIntent.putExtra(PUBLIC_STATIC_STRING_IDENTIFIER, "data_to_activity");
			resultIntent.putExtra("FILE_NAME", FileName);
			setResult(Activity.RESULT_OK, resultIntent);
			trace("onClickBack: before running finish(), sending back to whoever launched this.");
			finish();
		}catch (Exception e){
			trace("onClickBack: intent eror ."+e.getMessage());
		}
		trace("onClickBack: activity closed.");
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
	
	public class MyServerNameItemSelectedListener implements OnItemSelectedListener {
	    public void onItemSelected(AdapterView<?> parent,
	        View view, int pos, long id) {
	    	Cursor cursor = (Cursor) parent.getItemAtPosition(pos);
	    	
	    	int key_rowid = cursor.getInt(cursor.getColumnIndex(WirelessInfoDBAdapter.KEY_ROWID));
	    	String host = cursor.getString(cursor.getColumnIndex(WirelessInfoDBAdapter.KEY_HOSTNAME));
	      trace("Spinner, pos = "+pos+", server = " +host+", id = "+id+", row_id = "+key_rowid);
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
        
	        trace("onClickThrputTest: End. Got server to test = "+serverLogin.getHost());
		} else {
			Toast.makeText(this, "Please set Login Details first!", Toast.LENGTH_SHORT).show();
		}
		
	}
	
	public void onClickDBTest(View v) {
		
		dbAdapter = new WirelessInfoDBAdapter(this);
	    try {
	    	dbAdapter.open();
	    	trace("Opened DB");
	    	List<String> names = this.dbAdapter.selectAll();
	    	for (String name : names) {
	    		trace("onClickDBTest: Found hostname = "+name);
	    	}
	    	dbAdapter.close();
	    } catch (SQLException e) {
	    	trace("onClickDBTest: Fail to open db "+e.getMessage());
	    	dbAdapter.close();
	    }
	}
	
	public void onClickftpAction(View v) {
		ftpActionButton = (ToggleButton) findViewById(R.id.ftpAction);

		ThrPutTest t = mCurrentThrPutTask;
		if (t != null) {
			trace("onClickConnect: Still connected, disconnect first");
		} else {
			ThrPutTest ft = new ThrPutTest(this, "filename", serverLogin);
			mCurrentThrPutTask = ft;
		}
		
		// Connect to FTP server 
		if (ftpActionButton.isChecked()) {
			trace("onClickftpAction: ToggelButton isChecked, will connect.");
			mCurrentThrPutTask.mConnect();
		} else if (mCurrentThrPutTask != null ){
			trace("onClickftpAction: Will disconnect.");
			mCurrentThrPutTask.mDisconnect();
		}
	}
	
	public void trace(String msg) {
		Log.d("WirelessInfo", test.class.getName()+": "+msg);
	}

}
