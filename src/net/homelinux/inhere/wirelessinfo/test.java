package net.homelinux.inhere.wirelessinfo;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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
import net.homelinux.inhere.wirelessinfo.database.cellinfoDBAdapter;
import net.homelinux.inhere.wirelessinfo.verification.WirelessInfoException;

public class test extends Activity  {
	//implements AdapterView.OnItemSelectedListener
	private ProgressDialog progressDialog;
	
	String PUBLIC_STATIC_STRING_IDENTIFIER = null;
	private EditText cellLookupInfo;
	@SuppressWarnings("unused")
	private Button connectFtpButton;
	private ToggleButton ftpActionButton;
	
	LoginDetails serverLogin = null;
	LoginDetails[] serverLoginObj; // = new LoginDetails[];
	
	private ThrPutTest mCurrentThrPutTask = null;
	
	private WirelessInfoDBAdapter dbAdapter;
	private cellinfoDBAdapter cellInfoDbA;
	
	private Spinner spinServerName;
	private Cursor cursor;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.test);
		
		cellLookupInfo = (EditText) findViewById(R.id.cellLookupName);
		findViewById(R.id.trace);
		connectFtpButton = (Button) findViewById(R.id.connectFtp);
		
		spinServerName = (Spinner) findViewById(R.id.ftpHostSpinner);
		
		Spinner spinner = (Spinner) findViewById(R.id.fileSizeSpinner);
	    ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
	            this, R.array.fileSize_array, android.R.layout.simple_spinner_item);
	    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	    spinner.setAdapter(adapter);
	    
	    spinner.setOnItemSelectedListener(new MyOnItemSelectedListener());

	    Intent intent = getIntent();
	    intent.getParcelableExtra("serverLogin");
	    //intent.getParcelableExtra("serverLogin");
	    
	    //LoginDetails serverLoginObj = (LoginDetails)this.getIntent().getParcelableExtra("serverLogin");
	    
	    dbAdapter = new WirelessInfoDBAdapter(this);
	    cellInfoDbA = new cellinfoDBAdapter(this);
	    try {
	    	dbAdapter.open();
	    	trace("Opened DB");
	    	fillSpinner();
	    	spinServerName.setOnItemSelectedListener(new MyServerNameItemSelectedListener());
	    	populateServerLoginDetails(dbAdapter);
	    	setLoginDetailsBasedOnSpinner();
	        
	    	dbAdapter.close();
	    } catch (SQLException e) {
	    	trace("onCreate: Fail to open db "+e.getMessage());
	    	dbAdapter.close();
	    }
	    
	    /*
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
		*/
	}
	
	private void populateServerLoginDetails(WirelessInfoDBAdapter db) {
		db.fetchAllServerInfos();
		Cursor cursor = db.fetchAllServerInfos();
    	startManagingCursor(cursor);
    	if (cursor.getCount() > 0) {
    		trace("populateServerLoginDetails: Found "+cursor.getCount()+" Login details in DB");
    		serverLoginObj = new LoginDetails[cursor.getCount()];   		
    		if (cursor.moveToFirst()) {
    			int i = 0;
	         do {
	        	 trace("populateServerLoginDetails: Adding host ("+cursor.getString(1)+") to serverLogin Object.");
	        	 serverLoginObj[i] = new LoginDetails();
	        	 serverLoginObj[i].setHost(cursor.getString(1));
	        	 serverLoginObj[i].setPort(cursor.getInt(2));
	        	 serverLoginObj[i].setId(cursor.getString(3));
	        	 serverLoginObj[i].setPasswd(cursor.getString(4));
				i = i + 1;
	         } while (cursor.moveToNext() && i < 20);
		      }
		      if (cursor != null && !cursor.isClosed()) {
		         cursor.close();
		      }
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
        
        String FileName = "FileName";
		
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
	    @SuppressWarnings("rawtypes")
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
	    	setLoginDetailsBasedOnSpinner();
	    }
	    @SuppressWarnings("rawtypes")
		public void onNothingSelected(AdapterView parent) {
	      // Do nothing.
	    }
	}
	
	public void onClickCellLookup(View v) {
		trace("onClickCellLookup: start.");
		String cellInfo = cellLookupInfo.getText().toString();
		trace("onClickCellLookup: got value from edittext = "+cellInfo);
		if (cellInfo.length() > 0) {
			
			try {
				cellInfoDbA.open();
				trace("onClickCellLookup: DB Open");
				try {
					Cursor cellInfoRecord = (Cursor) this.cellInfoDbA.getInfoByCellId(cellInfo);
					trace("onClickCellLookup: Cursor = "+cellInfoRecord);
					
					if (cellInfoRecord.getCount() > 0) {
						trace("onClickCellLookup: Found "+cellInfoRecord.getCount()+" records");
						String sitename = cellInfoRecord.getString(cellInfoRecord.getColumnIndex(cellinfoDBAdapter.KEY_SITENAME));
						String cellname = cellInfoRecord.getString(cellInfoRecord.getColumnIndex(cellinfoDBAdapter.KEY_CELLNAME));
						
						trace("onClickCellLookup: "+cellInfo+", cellname = "+cellname+", sitename = "+sitename);
						status(cellInfo+", Cell name = "+cellname+", Site name = "+sitename);
					}
					else {
						trace("onClickCellLookup: No records");
						status("No records for cell ID "+cellInfo);
					}
					
					
				} catch (SQLException e) {
			    	trace("onClickCellLookup: "+e.getMessage());
			    	cellInfoDbA.close();
			    }
			} catch (SQLException e) {
				trace("onClickCellLookup: "+e.getMessage());
			}
		}
		else {
			trace("onClickCellLookup: No name to lookup!");
		}
		
	}
	
	private boolean setLoginDetailsBasedOnSpinner() {
		Cursor cursor = (Cursor) spinServerName.getSelectedItem();
		//int key_rowid = cursor.getInt(cursor.getColumnIndex(WirelessInfoDBAdapter.KEY_ROWID));
		String sName = cursor.getString(cursor.getColumnIndex(WirelessInfoDBAdapter.KEY_HOSTNAME));
		
		for(int i = 0; i < serverLoginObj.length; i++) {
			if (sName.equalsIgnoreCase(serverLoginObj[i].getHost())) {
				serverLogin = serverLoginObj[i];
				return true;
			}
		}
		
		return false;
	}
	
	public void onClickThrputTest(View v) {
		trace("onClickThrputTest: Start.");
		
		if (serverLogin != null) {
			trace("onClickThrputTest: Doing Throughput test to "+serverLogin.getHost());
			ThrPutTest ft = new ThrPutTest(this, "filename", serverLogin);
	    	ft.execute("filename");
		}
		
		/*
		//spinServerName.getSelectedItemPosition();
		Cursor cursor = (Cursor) spinServerName.getSelectedItem();
		int key_rowid = cursor.getInt(cursor.getColumnIndex(WirelessInfoDBAdapter.KEY_ROWID));
		String sName = cursor.getString(cursor.getColumnIndex(WirelessInfoDBAdapter.KEY_HOSTNAME));
		
		trace("onClickThrputTest: from Spinner. Server name = "+sName);
		
		dbAdapter = new WirelessInfoDBAdapter(this);
	    try {
	    	dbAdapter.open();
	    	trace("Opened DB");
	    	Cursor dbRecord = (Cursor) this.dbAdapter.fetchServerInfo(key_rowid);
	    	trace("onClickThrputTest: from Spinner. Start DB Query with row_id = "+key_rowid);
	    	
	    	int port = dbRecord.getInt(dbRecord.getColumnIndex(WirelessInfoDBAdapter.KEY_PORT));
	    	String loginid = dbRecord.getString(dbRecord.getColumnIndex(WirelessInfoDBAdapter.KEY_LOGINID));
	    	String passwd = dbRecord.getString(dbRecord.getColumnIndex(WirelessInfoDBAdapter.KEY_PASSWD));
	    	dbAdapter.close();
	    	
	    	trace("onClickThrputTest: from Spinner. Server = "+sName+", port = "+port+", Login ID = "+loginid+", Passwd = ***");
	    	trace("onClickThrputTest: from Spinner. End DB Query with row_id = "+key_rowid);
	    	
	    	LoginDetails selectedServer = new LoginDetails();
	    	selectedServer.setHost(sName);
	    	selectedServer.setPort(port);
	    	selectedServer.setId(loginid);
	    	selectedServer.setPasswd(passwd);
	    	
	    	ThrPutTest ft = new ThrPutTest(this, "filename", selectedServer);
	    	ft.execute("filename");
	        
	    } catch (SQLException e) {
	    	trace("onClickDBTest: Fail to open db "+e.getMessage());
	    	dbAdapter.close();
	    }
		*/
	    /*
		Bundle b = this.getIntent().getExtras();
		if (b.getBoolean("hostStatus")) {
			LoginDetails serverLoginFromIntent = new LoginDetails();
			serverLoginFromIntent.setHost(b.getString("host"));
			serverLoginFromIntent.setPort(b.getInt("port"));
			serverLoginFromIntent.setId(b.getString("id"));
			serverLoginFromIntent.setPasswd(b.getString("passwd"));
        
	        trace("onClickThrputTest: from Intent extras. Server name = "+serverLoginFromIntent.getHost());
		} else {
			Toast.makeText(this, "Please set Login Details first!", Toast.LENGTH_SHORT).show();
		}
		*/
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
	    
	    cellInfoDbA = new cellinfoDBAdapter(this);
		try {
			cellInfoDbA.open();
			int records = cellInfoDbA.getDbInfo();
	    	trace("onClickDBTest: "+records+" in cell info DB");
	    	status("Found "+records+" cell info records");
	    	cellInfoDbA.close();
	    } catch (SQLException e) {
	    	trace("purgeCellInfo: Fail to open db "+e.getMessage());
	    	cellInfoDbA.close();
	    }
	}
	
	public void onClickftpAction(View v) {
		ftpActionButton = (ToggleButton) findViewById(R.id.ftpAction);

		ThrPutTest t = mCurrentThrPutTask;
		if (t != null) {
			trace("onClickftpAction: Still connected, disconnect first");
		} else {
			trace("onClickftpAction: Object ft for server ("+serverLogin.getHost()+")");
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
			mCurrentThrPutTask = null;
		}
	}
	
	public void showProgressOnScreen(int val) {
		// trace
		// ("WirelessInfo: showFtpProgressOnScreen: Got progress report. "+val+"%");
		status("Got progress report from FTP task. "+ val + "%");
	}
	
	private void purgeCellInfo() throws WirelessInfoException {
		
		cellInfoDbA = new cellinfoDBAdapter(this);
		try {
			cellInfoDbA.open();
			cellInfoDbA.deleteAllCellInfo();
	    	trace("purgeCellInfo: Opened DB");
	    	status("Cell Info purged");
	    	cellInfoDbA.close();
	    } catch (SQLException e) {
	    	trace("purgeCellInfo: Fail to open db "+e.getMessage());
	    	cellInfoDbA.close();
	    	throw new WirelessInfoException("Failed to access cell Info DB!");
	    }
	}
	
	private boolean loadCellInfoLocal() throws WirelessInfoException {
		
		progressDialog = ProgressDialog.show(this, "Please wait....",
		"Retrieving Cell Info Details");
		
		new Thread(new Runnable() {
			public void run() {
				try {
					getCellInfoFromNetwork();
					progressDialog.dismiss();
				} catch (IOException e) {
					status("setLoginDetails: Failure. " + e.getMessage());
					try {
						throw new WirelessInfoException("Failed to access cell Info DB!");
					} catch (WirelessInfoException e1) {
						e1.printStackTrace();
						trace("loadCellInfoLocal: "+e1.getMessage());
					}
				}
			}
		}).start();
		return true;
	}
	
	private void getCellInfoFromNetwork() throws IOException {
		trace("getCellInfoFromNetwork: Start.");
		
		InputStream is = null;
		ByteArrayOutputStream bos = null;
		byte[] data = null;
		try {
			StringBuilder uri = new StringBuilder(
					"http://inhere.homelinux.net/test/getcellinfo.php");
			uri.append("?alt=json&key=53024");
			HttpGet request = new HttpGet(uri.toString());
			HttpClient httpClient = new DefaultHttpClient();
			HttpResponse response = httpClient.execute(request);
			int status = response.getStatusLine().getStatusCode();
			if (status != HttpURLConnection.HTTP_OK) {
				switch (status) {
				case HttpURLConnection.HTTP_NO_CONTENT:
					throw new IOException("The cell could not be "
							+ "found in the database");
				case HttpURLConnection.HTTP_BAD_REQUEST:
					throw new IOException("Check if some parameter "
							+ "is missing or misspelled");
				case HttpURLConnection.HTTP_UNAUTHORIZED:
					throw new IOException("Make sure the API key is "
							+ "present and valid");
				case HttpURLConnection.HTTP_FORBIDDEN:
					throw new IOException("You have reached the limit "
							+ "for the number of requests per day. The "
							+ "maximum number of requests per day is "
							+ "currently 500.");
				case HttpURLConnection.HTTP_NOT_FOUND:
					throw new IOException("The cell could not be found "
							+ "in the database " + status);
				default:
					throw new IOException("HTTP response code: " + status);
				}
			}
			trace("getCellInfoFromNetwork: After http connection, getting login details from web.");

			// The response was ok (HTTP_OK) so lets read the data
			HttpEntity entity = response.getEntity();
			is = entity.getContent();
			bos = new ByteArrayOutputStream();
			byte buf[] = new byte[256];
			while (true) {
				int rd = is.read(buf, 0, 256);
				if (rd == -1)
					break;
				bos.write(buf, 0, rd);
			}
			bos.flush();
			data = bos.toByteArray();

			trace("getCellInfoFromNetwork: Finished adding byteStream to local var.");
			boolean dbGood = false;
			if (data != null) {
				cellInfoDbA = new cellinfoDBAdapter(this);
				try {
					cellInfoDbA.open();
			    	dbGood = true;
			    	trace("getCellInfoFromNetwork: Opened DB");
			    } catch (SQLException e) {
			    	trace("getCellInfoFromNetwork: Fail to open db "+e.getMessage());
			    	cellInfoDbA.close();
			    }
				try {
					JSONArray info = new JSONObject(new String(data)).getJSONArray("info");
					int numVals = info.length();
					for (int i = 0; i < numVals; i++) {
						JSONObject jobj = info.getJSONObject(i);
						
						trace("getCellInfoFromNetwork: Adding new server ("+jobj.getString("cellid")+") info to DB");
						cellInfoDbA.createCellInfo(
								jobj.getInt("cellid"), 
								jobj.getString("sitename"), 
								jobj.getString("cellname"), 
								jobj.getDouble("lat"),
								jobj.getDouble("lng"));
					}
					
					if (dbGood) {
						cellInfoDbA.close();
						trace("getCellInfoFromNetwork: Closed DB");
					}
				} catch (JSONException e) {
					trace(e.getMessage());
					e.printStackTrace();
				} catch (Exception e) {
					trace(e.getMessage());
					e.printStackTrace();
				}
			}
		} catch (MalformedURLException e) {
			Log.e("ERROR", e.getMessage());
		} catch (IllegalArgumentException e) {
			throw new IOException(
					"URL was incorrect. Did you forget to set the API_KEY?");
		} finally {
			// make sure we clean up after us
			try {
				if (bos != null)
					bos.close();
			} catch (Exception e) {
			}
			try {
				if (is != null)
					is.close();
			} catch (Exception e) {
			}
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.testmenu, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		
		case R.id.mLoad:
			trace("onOptionsItemSelected: Get new cell info data and load into load DB.");
			
			try {
				loadCellInfoLocal();
				trace("onOptionsItemSelected: Found server Info in DB");
			} catch (WirelessInfoException e) {
				trace("onOptionsItemSelected: "+e.getMessage());
			}
	
			break;
		case R.id.mClear:
			try {
				purgeCellInfo();
				trace("onOptionsItemSelected: click Clear cell info on menu");
			} catch (WirelessInfoException e) {
				trace("onOptionsItemSelected: "+e.getMessage());
			}
			break;
		case R.id.icontext:
			trace("onOptionsItemSelected: menu");
			break;
		}
		return true;
	}
	
	public void status(String message) {
		TextView tv = (TextView) findViewById(R.id.trace);
		tv.setText(message);
	}
	
	public void trace(String msg) {
		Log.d("WirelessInfo", test.class.getName()+": "+msg);
	}

}
