/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.homelinux.inhere.wirelessinfo;

/*
 * Pieter Linde
 */

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.util.List;

import net.homelinux.inhere.wirelessinfo.database.IntTmpStoreDBAdapter;
import net.homelinux.inhere.wirelessinfo.database.TmpStorage;
import net.homelinux.inhere.wirelessinfo.database.WirelessInfoDBAdapter;
import net.homelinux.inhere.wirelessinfo.database.cellinfoDBAdapter;
import net.homelinux.inhere.wirelessinfo.verification.VerifyService;
import net.homelinux.inhere.wirelessinfo.verification.WirelessInfoException;

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
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.telephony.NeighboringCellInfo;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.telephony.SignalStrength;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.net.TrafficStats;

import com.google.gson.Gson;
import com.loopj.android.http.*;

public class WirelessInfo extends Activity {

	// private final static String API_KEY =
	// "KJ7DB6kbP6UE4b5O3AskOMttTppKFGHDYuJ81J8T";

	private boolean upLoadCellInfo = false;
	private boolean cellInfoRecording = false;
	private int ssdbm;
	private ProgressDialog progressDialog;
	
	private double lat, lng;

	private TelephonyManager tm;
	MyPhoneStateListener MyListener;
	int listenEvents;
	private GsmCellLocation mobileLocation;
	private int cid, lac, mcc, mnc, cellPadding;
	private String networkType, SignalHeading, imsi;

	private TextView webPageText;

	private String locationType = "network";
	private TextView latituteField;
	private TextView longitudeField;
	private LocationManager locationManager;
	
	private String provider;
	
	private LocationManager mlocManager;
	private LocationListener mlocListener;
	
	private TmpStorage tmpStorage;

	private String ftpFileName;
	private MyFtpTask mCurrentFtpTask = null;

	ThrPutStats tps = null;

	String testvar;
	LoginDetails[] serverLogin = new LoginDetails[2];
	
	VerifyService verify;
	
	private WirelessInfoDBAdapter dbAdapter;
	private cellinfoDBAdapter cellInfoDbA;
	private IntTmpStoreDBAdapter intTmpStoreDbA;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		latituteField = (TextView) findViewById(R.id.unknownLat);
		longitudeField = (TextView) findViewById(R.id.unknownLong);

		// Get the location manager
		//locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		// Define the criteria how to select the location provider -> use
		// default
		//provider = locationManager.getBestProvider(criteria, false);
		//Location location = locationManager.getLastKnownLocation(provider);
		
		Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
    ////////criteria.setAccuracy(Criteria.ACCURACY_COARSE);
        criteria.setAltitudeRequired(false);
        criteria.setBearingRequired(false);
        criteria.setCostAllowed(true);
        criteria.setPowerRequirement(Criteria.POWER_HIGH); //POWER_LOW
        //String provider = locationManager.getBestProvider(criteria, true);

        //Gson gson = new Gson();
		
		mlocManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		
		provider = mlocManager.getBestProvider(criteria, true);
		mlocListener = new MyLocationListener();
		// Start when activating recording and stop when recording end.
		//mlocManager.requestLocationUpdates( LocationManager.GPS_PROVIDER, 0, 0, mlocListener);
		Location location = mlocManager.getLastKnownLocation(provider);
		
		verify = new VerifyService(this);

		// Initialize the location fields
		if (location != null) {
			trace("onCreate: Provider " + provider + " has been selected.");
			lat = (location.getLatitude());
			lng = (location.getLongitude());
			latituteField.setText(String.valueOf(lat));
			longitudeField.setText(String.valueOf(lng));
		} else {
			latituteField.setText("Provider not available");
			longitudeField.setText("Provider not available");
		}

		MyListener = new MyPhoneStateListener();
		tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		
		int events = PhoneStateListener.LISTEN_SIGNAL_STRENGTH | 
		 PhoneStateListener.LISTEN_DATA_ACTIVITY | 
		 PhoneStateListener.LISTEN_CELL_LOCATION |
		 PhoneStateListener.LISTEN_CALL_STATE |
		 PhoneStateListener.LISTEN_CALL_FORWARDING_INDICATOR |
		 PhoneStateListener.LISTEN_DATA_CONNECTION_STATE |
		 PhoneStateListener.LISTEN_MESSAGE_WAITING_INDICATOR |
		 PhoneStateListener.LISTEN_SERVICE_STATE;
		
		listenEvents = PhoneStateListener.LISTEN_SIGNAL_STRENGTHS;
		
		tm.listen(MyListener, listenEvents);

		imsi = tm.getSubscriberId();

		((TextView) findViewById(R.id.TextViewImsi)).setText("IMSI: " + imsi);
		webPageText = (TextView) findViewById(R.id.webPageText);

		final AsyncHttpClient client = new AsyncHttpClient();
		
		

		/*
		 * Setup a listener for the UpdateCellButton. Pressing this button will
		 * fetch the current cell info from the phone.
		 */

		final Button UpdateCellButton = (Button) findViewById(R.id.UpdateCellButton);

		UpdateCellButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				mobileLocation = (GsmCellLocation) tm.getCellLocation();
				cid = mobileLocation.getCid();
				lac = mobileLocation.getLac();

				/*
				 * Mcc and mnc is concatenated in the networkOperatorString. The
				 * first 3 chars is the mcc and the last 2 is the mnc.
				 */
				String networkOperator = tm.getNetworkOperator();
				if (networkOperator != null && networkOperator.length() > 0) {
					try {
						mcc = Integer.parseInt(networkOperator.substring(0, 3));
						mnc = Integer.parseInt(networkOperator.substring(3));
					} catch (NumberFormatException e) {
					}
				}

				/*
				 * TelephonyManager test = (TelephonyManager)
				 * getSystemService(TELEPHONY_SERVICE);
				 */
				TextView Neighboring = (TextView) findViewById(R.id.neighboring);
				List<NeighboringCellInfo> NeighboringList = tm
						.getNeighboringCellInfo();

				String stringNeighboring = "Neighboring List- Lac : Cid : RSSI\n";
				for (int i = 0; i < NeighboringList.size(); i++) {
					String dBm;
					int rssi = NeighboringList.get(i).getRssi();
					if (rssi == NeighboringCellInfo.UNKNOWN_RSSI) {
						dBm = "Unknown RSSI";
					} else {
						dBm = String.valueOf(-113 + 2 * rssi) + " dBm";
					}
					stringNeighboring = stringNeighboring
							+ String.valueOf(NeighboringList.get(i).getLac())
							+ " : "
							+ String.valueOf(NeighboringList.get(i).getCid())
							+ " : " + dBm + "\n";
				}

				Neighboring.setText(stringNeighboring);

				/*
				 * Check if the current cell is a UMTS (3G) cell. If a 3G cell
				 * the cell id padding will be 8 numbers, if not 4 numbers.
				 */
				if (tm.getNetworkType() == TelephonyManager.NETWORK_TYPE_UMTS
						|| tm.getNetworkType() == TelephonyManager.NETWORK_TYPE_HSDPA) {
					cellPadding = 8;
					networkType = "UMTS";
					SignalHeading = "RSSI";
					/*
					 * cid = cid - 65536;
					 */
				} else {
					cellPadding = 4;
					networkType = "GSM";
					SignalHeading = "RSCP";
				}
				
				String activityTypeDisplay = "Activity Type";
				if (upLoadCellInfo == true) {
					activityTypeDisplay = "(U) Activity Type";
					
				}
				((TextView) findViewById(R.id.dataType))
						.setText(activityTypeDisplay+": "+ tm.getDataActivity());

				/*
				 * Update the GUI with the current cell's info
				 */
				String cellid = getCellId(cid, networkType);
				String cellinfo;
				try {
					cellinfo = getCellInfoById(Integer.parseInt(cellid));
				} catch (WirelessInfoException e) {
					cellinfo = "";
				}
				
				((TextView) findViewById(R.id.networktype))
						.setText("Network Type: " + networkType + ": "
								+ tm.getNetworkType());
				((TextView) findViewById(R.id.TextView01)).setText(": CellID: "
						+ cellid + ": " + cid);
				((TextView) findViewById(R.id.CellInfo)).setText(cellinfo);
				((TextView) findViewById(R.id.TextView02)).setText("LAC: "
						+ getPaddedInt(lac, 4) + ": " + getPaddedHex(lac, 4));
				((TextView) findViewById(R.id.TextView03)).setText("MCC: "
						+ getPaddedInt(mcc, 3) + ", MNC: "
						+ getPaddedInt(mnc, 2));
				// ((TextView) findViewById(R.id.TextView04)).setText("MNC: "+
				// getPaddedInt(mnc, 2));

				if (TrafficStats.getMobileRxBytes() == TrafficStats.UNSUPPORTED) {
					((TextView) findViewById(R.id.txtNetStats))
							.setText("Stats: RxBytes - UNSUPPORTED");

				} else {
					long TotalTxBytes = TrafficStats.getTotalTxBytes();
					((TextView) findViewById(R.id.txtNetStats))
							.setText("Rx/Tx Bytes = "
									+ String.valueOf(TrafficStats
											.getTotalRxBytes()) + "/"
									+ TotalTxBytes);
				}
				
				if (cellInfoRecording) {
					intTmpStoreDbA.createCellInfo(imsi, null, Integer.parseInt(cellid), ssdbm, lat, lng);
				}
				
				if (upLoadCellInfo == true) {
					uploadCellInfoViaWeb(Integer.parseInt(cellid));
					//tmpStorage.close();
				}
			}
		});

		/*
		 * Setup a listener for the GetWebPageButton. When pressing this button
		 * the cell info is sent to the server and hopefully we will get a web
		 * page.
		 */
		final Button GetWebPageAction = (Button) findViewById(R.id.GetWebPageButton);
		GetWebPageAction.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {

				ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
				webPageText.setText("Info:");

				/**
				 * Check the active network connection.
				 */
				NetworkInfo ani = cm.getActiveNetworkInfo();

				if (ani != null && ani.isConnected()) {
					final long rxSByteSample = TrafficStats.getTotalRxBytes();
					final long txSByteSample = TrafficStats.getTotalTxBytes();
					final long start = System.currentTimeMillis();

					// client.get("http://www.google.com", new
					// AsyncHttpResponseHandler() {
					client.get(
							"http://inhere.homelinux.net/android/xml_post.php?id=3&name=testing&score=555",
							new AsyncHttpResponseHandler() {
								@Override
								public void onSuccess(String response) {
									// System.out.println(response);

									long rxEByteSample = TrafficStats
											.getTotalRxBytes();
									long txEByteSample = TrafficStats
											.getTotalTxBytes();
									long rxDByteSample = rxEByteSample
											- rxSByteSample;
									long txDByteSample = txEByteSample
											- txSByteSample;
									double updateDelta = (double) (System
											.currentTimeMillis() - start) / 1000.00;
									double rxStatsMb = (double) (rxDByteSample * 8) / 1024.00 / 1024.00;

									((TextView) findViewById(R.id.txtWebNetStats))
											.setText("Delta time = "
													+ updateDelta
													+ " s, Thrput = "
													+ rxStatsMb
													/ updateDelta
													+ " Mb/s, Delta Rx/Tx Bytes = "
													+ (rxDByteSample * 8)
													/ 1024 + " kb/"
													+ txDByteSample + " Bytes");

									webPageText.setText(response);
									/*
									 * Toast t =
									 * Toast.makeText(getApplicationContext(),
									 * "Web Page downloaded successful",
									 * Toast.LENGTH_SHORT);
									 * t.setGravity(Gravity.CENTER_VERTICAL, 0,
									 * 0); t.show();
									 */
								}
							});
				} else {
					String strResult = "No network available!!";
					Toast t = Toast.makeText(getApplicationContext(),
							strResult, Toast.LENGTH_LONG);
					t.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
					t.show();
				}
			}
		});
	}
	
	/*
	@Override
	public void onLocationChanged(Location location) {
		lat = (location.getLatitude());
		lng = (location.getLongitude());
		latituteField.setText(String.valueOf(lat));
		longitudeField.setText(String.valueOf(lng));
	}
*/
	
	
	/* Class My Location Listener */

	public class MyLocationListener implements LocationListener	{
		@Override
		public void onLocationChanged(Location loc)	{
			trace("MyLocationListener: onLocationChanged event");
			if (loc != null) {
				lat = (loc.getLatitude());
				lng = (loc.getLongitude());
				latituteField.setText(String.valueOf(lat));
				longitudeField.setText(String.valueOf(lng));
				cellInfoRecording = true;
				//loc.getLatitude();
				//loc.getLongitude();
				//String Text = "My current location is: Latitud = " + loc.getLatitude() + "Longitud = " + loc.getLongitude();
			
				//Toast.makeText( getApplicationContext(),Text,Toast.LENGTH_SHORT).show();
			}
		}

		@Override
		public void onProviderDisabled(String provider)	{
			Toast.makeText( getApplicationContext(), provider+" Disabled", Toast.LENGTH_SHORT ).show();
		}
	
		@Override
		public void onProviderEnabled(String provider) {
			Toast.makeText( getApplicationContext(),provider+" Enabled",Toast.LENGTH_SHORT).show();
		}
	
		@Override
		public void onStatusChanged(String provider, int status, Bundle extras)	{
		}
	}
	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent i = new Intent(WirelessInfo.this, test.class);
		switch (item.getItemId()) {
		
		case R.id.settings:
			trace("onOptionsItemSelected: Open Settings.");
			
			try {
				verify.serverInfo();
				trace("onOptionsItemSelected: Found server Info in DB");
			} catch (WirelessInfoException e) {
				trace("onOptionsItemSelected: "+e.getMessage());
			}
			
			Bundle b = new Bundle();
            b.putString("IDENT", "data_to_subactivity");
            b.putString("IDENT1", "more data_to_subactivity");
            i.putExtras(b);
            /*
            if (serverLogin[0] != null) {
            	b.putBoolean("hostStatus",true);
	            b.putString("host",serverLogin[0].getHost());
	            b.putInt("port",serverLogin[0].getPort());
	            b.putString("id",serverLogin[0].getId());
	            b.putString("passwd",serverLogin[0].getPasswd());
	            
	            i.putExtras(b);
	            Parcelable lt = new LoginDetails(serverLogin[0].getHost(), serverLogin[0].getPort(), serverLogin[0].getId(), serverLogin[0].getPasswd());
	            //LoginDetails ld = new LoginDetails(serverLogin[0].getHost(), serverLogin[0].getPort(), serverLogin[0].getId(), serverLogin[0].getPasswd());
	            //LoginDetails[] p = new LoginDetails[2];
	            //p = serverLogin;
	            i.putExtra("serverLogin", lt);
	            //i.putExtra("arrayServerlogin", p);
	            
	            
            } else {
            	b.putBoolean("hostStatus",false);
            	Toast.makeText(this, "Please set Login Details first!", Toast.LENGTH_SHORT).show();
            }
            */
            startActivityForResult(i, SET_SETTINGS);
	
			break;
		case R.id.mclearlogin:
			try {
				clearLoginDetails();
				status("Login details cleared.");
				trace("onOptionsItemSelected: click Clear login on menu");
			} catch (WirelessInfoException e) {
				trace("onOptionsItemSelected: "+e.getMessage());
			}
			break;
		case R.id.icontext:
			trace("onOptionsItemSelected: menu");
			String activityTypeDisplay = "Activity Type";
			if (upLoadCellInfo == true) {
				upLoadCellInfo = false;
				activityTypeDisplay = "Activity Type";
			} else {
				upLoadCellInfo = true;
				activityTypeDisplay = "(U) Activity Type";
			}
			((TextView) findViewById(R.id.dataType)).setText(activityTypeDisplay+":");
			
			break;
		case R.id.sRecord:
			trace("onOptionsItemSelected: Start recording");
			
			intTmpStoreDbA = new IntTmpStoreDBAdapter(this);
			try {
				intTmpStoreDbA.open();
		    	trace("menuStartRecording: Opened DB");
		    	/* Moved recording start to location update event */
		    	//cellInfoRecording = true;
				((TextView) findViewById(R.id.recordStatus)).setText("(R) ");
				//Start GPS
				if (mlocManager != null) {
					mlocManager.requestLocationUpdates( LocationManager.GPS_PROVIDER, 400, 1, mlocListener);
				}
		    } catch (SQLException e) {
		    	trace("menuStartRecording: Fail to open db "+e.getMessage());
		    	intTmpStoreDbA.close();
		    }
		    /*
			try {
				verify.wirelessInfoTmpStor();
				tmpStorage = new TmpStorage(true);
				
				cellInfoRecording = true;
				((TextView) findViewById(R.id.recordStatus)).setText("(R) ");
				
			} catch (WirelessInfoException e) {
				trace("Temp Storage error "+e.getMessage());
				Toast.makeText( getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT ).show();
			}
			*/
			
			
			
			
			break;
		case R.id.eRecord:
			trace("onOptionsItemSelected: Stop recording");
			if (mlocManager != null && cellInfoRecording) {
				mlocManager.removeUpdates(mlocListener);
			}
			boolean prevRecordingStatus = cellInfoRecording;
			cellInfoRecording = false;
			//End recording, copy all records from internal to external
			if (intTmpStoreDbA != null && prevRecordingStatus) {
				// Copy all records in internal storage to external storage.
				//CellStatsObject[] statsObj;
				Cursor cursor = intTmpStoreDbA.cursorSelectAll();
				int numRecords = cursor.getCount();
				int numRecord = 0;
				if (numRecords > 0) {
					tmpStorage = new TmpStorage(false);
		    		//statsObj = new CellStatsObject[numRecords];
		    		trace("UploadData: Found "+numRecords+" cell stats data");
		    		if (cursor.moveToFirst()) {			
		    			trace("UploadData: Adding index = "+numRecord+" for "+cursor.getString(4)+" to object");
			         do {
			        	 double lat = cursor.getDouble(4);
			        	 double lng = cursor.getDouble(5);
			        	 try {
							tmpStorage.insertData(cursor.getString(0), cursor.getString(1), cursor.getString(2), cursor.getInt(3), lat, lng);
						} catch (WirelessInfoException e) {
							trace("Recording Stop: "+e.getMessage());
							cursor.moveToLast();
						}
			        	 /*
			        	 statsObj[numRecord] = new CellStatsObject();
			        	 statsObj[numRecord].imsi =  cursor.getString(0);
			        	 statsObj[numRecord].timeEnter =  cursor.getString(1);
			        	 statsObj[numRecord].cellid =  cursor.getString(2);
			        	 statsObj[numRecord].rssi =  cursor.getString(3);
			        	 double lat = cursor.getDouble(4);
			        	 double lng = cursor.getDouble(5);
			        	 statsObj[numRecord].lat =  String.valueOf(lat);
			        	 statsObj[numRecord].lng =  String.valueOf(lng);
			        	 */
			        	 numRecord = numRecord + 1;
			         } while (cursor.moveToNext());
				      }
				      if (cursor != null && !cursor.isClosed()) {
				         cursor.close();
				      }
				      trace("UploadData: Copied "+numRecord+" records to external storage");
				      tmpStorage.close();
		    	}
				
				//tmpStorage = new TmpStorage(false);
				//tmpStorage.insertData(activityTypeDisplay, cellid, rssi, lat, lng);
				intTmpStoreDbA.deleteAll();
				intTmpStoreDbA.close();
				intTmpStoreDbA = null;
				
			}
			/*
			if (tmpStorage != null) {
				tmpStorage.close();
			}
			*/
			
			((TextView) findViewById(R.id.recordStatus)).setText("");
			break;
		}
		return true;
	}
	
	static final int SET_SETTINGS = 0;
	private static final String PUBLIC_STATIC_STRING_IDENTIFIER = null;
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data); 
		  switch(requestCode) { 
		    case (SET_SETTINGS) : { 
		      if (resultCode == Activity.RESULT_OK) { 
	        	  String returnData = data.getStringExtra(PUBLIC_STATIC_STRING_IDENTIFIER);
	        	  ftpFileName = data.getStringExtra("FILE_NAME");

	        	  //Toast.makeText(getApplicationContext(), "onActivityResult: " + returnData, Toast.LENGTH_LONG).show();
	        	  trace("onActivityResult: from child intent = " + returnData);
	        	} else if (resultCode == RESULT_CANCELED) {
	        		trace("onActivityResult: ERROR");	
	        	}
		    }
		    default: 
		    	break; 
		  }
    }

	/* Called when the application is minimized */
	@Override
	protected void onPause() {
		super.onPause();
		tm.listen(MyListener, PhoneStateListener.LISTEN_NONE);
		//locationManager.removeUpdates(this);
		//tmpStorage.close();
		if (mlocManager != null) {
			mlocManager.removeUpdates(mlocListener);
			//mlocManager = null;
		}
	}

	/* Called when the application resumes */
	@Override
	protected void onResume() {
		super.onResume();
		tm.listen(MyListener, listenEvents);
		//tmpStorage = new TmpStorage(false);
		//mlocManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		if (mlocManager != null && cellInfoRecording) {
			mlocManager.requestLocationUpdates( LocationManager.GPS_PROVIDER, 400, 1, mlocListener);
			//locationManager.requestLocationUpdates(provider, 400, 1, this);
		} else {
			trace("onResume: Location manager = null, can not request locaion updates");
		}
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		trace("onDestroy: Upload db var = "+upLoadCellInfo);
		if (tmpStorage != null) {
			tmpStorage.close();
		}
		if (mlocManager != null) {
			mlocManager.removeUpdates(mlocListener);
			mlocManager = null;
			trace("onDestroy: NULL mlocManager");
		}
	}

	/* —————————– */
	/* Start the PhoneState listener */
	/* —————————– */
	private class MyPhoneStateListener extends PhoneStateListener {
		/*
		 * Get the Signal strength from the provider, each time there is an
		 * update
		 */
		private int ss, ber, cdmadbm;

		@Override
		public void onSignalStrengthsChanged(SignalStrength signalStrength) {
			super.onSignalStrengthsChanged(signalStrength);
			cdmadbm = signalStrength.getEvdoDbm();
			// int cdmaecio = signalStrength.getEvdoEcio();
			ss = signalStrength.getGsmSignalStrength();
			ber = signalStrength.getGsmBitErrorRate();

			ssdbm = getRSSI(ss);
			
			mobileLocation = (GsmCellLocation) tm.getCellLocation();
			cid = mobileLocation.getCid();
			if (tm.getNetworkType() == TelephonyManager.NETWORK_TYPE_UMTS
					|| tm.getNetworkType() == TelephonyManager.NETWORK_TYPE_HSDPA) {
				cellPadding = 8;
				networkType = "UMTS";
				SignalHeading = "RSSI";
				/*
				 * cid = cid - 65536;
				 */
			} else {
				cellPadding = 4;
				networkType = "GSM";
				SignalHeading = "RSCP";
			}
			String cellid = getCellId(cid, networkType);
			
			if (cellInfoRecording) {
				//tmpStorage.insertData(imsi, null, cellid, ssdbm, lat, lng);
				intTmpStoreDbA.createCellInfo(imsi, null, Integer.parseInt(cellid), ssdbm, lat, lng);
				((TextView) findViewById(R.id.recordStatus)).setText("(R-"+intTmpStoreDbA.getDbInfo()+")");
			}

			((TextView) findViewById(R.id.other_txt1)).setText(cellid+": "+SignalHeading
					+ ": -" + ssdbm + "dBm");
		}
		
		@Override
		public void onDataActivity(int direction) {
			String directionString = "none";
			
			switch(direction)
			{
				case TelephonyManager.DATA_ACTIVITY_IN: 	directionString = "IN"; break;
				case TelephonyManager.DATA_ACTIVITY_OUT: 	directionString = "OUT"; break;
				case TelephonyManager.DATA_ACTIVITY_INOUT: 	directionString = "INOUT"; break;
				case TelephonyManager.DATA_ACTIVITY_NONE: 	directionString = "NONE"; break;
				default:									directionString = "UNKNOWN: " + direction; break;
			}
			
			//setDataDirection(info_ids[INFO_DATA_DIRECTION_INDEX],direction);
			
			((TextView) findViewById(R.id.dataType)).setText("Activity Type: "+ directionString);
			
			trace("onDataActivity " + directionString);
			
			super.onDataActivity(direction);
		}
		
		@Override
		public void onDataConnectionStateChanged(int state)
		{
			String connectionState = "Unknown";
			
			switch(state)
			{
				case TelephonyManager.DATA_CONNECTED: 		connectionState = "Connected"; break;
				case TelephonyManager.DATA_CONNECTING: 		connectionState = "Connecting"; break;
				case TelephonyManager.DATA_DISCONNECTED: 	connectionState = "Disconnected"; break;
				case TelephonyManager.DATA_SUSPENDED: 		connectionState = "Suspended"; break;
				default: 									connectionState = "Unknown: " + state; break;
			}
			
			((TextView) findViewById(R.id.dataType)).setText("Activity Type: "+ connectionState);
			
			trace("onDataState " + connectionState);
			
			super.onDataConnectionStateChanged(state);
		}
	};/* End of private Class */

	public int getRSSI(int ss) {
		int ssdbm;
		if (ss == 0) {
			ssdbm = 113;
		} else if (ss == 1) {
			ssdbm = 111;
		} else if (ss > 1 && ss < 31) {
			ssdbm = 109 - (ss * 2);
		} else {
			ssdbm = 51;
		}
		return ssdbm;
	}

	/**
	 * Convert an int to an hex String and pad with 0's up to minLen.
	 */
	String getPaddedHex(int nr, int minLen) {
		String str = Integer.toHexString(nr);
		if (str != null) {
			while (str.length() < minLen) {
				str = "0" + str;
			}
		}
		return str;
	}

	public String getCellId(int cellid, String netType) {
		String netTypeCheck = "GSM";
		if (netType.equals(netTypeCheck)) {
			return String.valueOf(cellid);
		}

		String str = Integer.toHexString(cellid);
		if (str != null) {
			str = str.substring(str.length() - 4);
		}
		str = String.valueOf(Integer.parseInt(str, 16));
		return str;
	}

	/**
	 * Convert an int to String and pad with 0's up to minLen.
	 */
	String getPaddedInt(int nr, int minLen) {
		String str = Integer.toString(nr);
		if (str != null) {
			while (str.length() < minLen) {
				str = "0" + str;
			}
		}
		return str;
	}
	
	private boolean checkAPNSettings() throws WirelessInfoException {
		status("checkAPNSettings");
		trace("checkAPNSettings: Start.");
		
		Cursor mCursor = getContentResolver().query(
				Uri.parse("content://telephony/carriers"),
				new String[] { "name", "apn", "current" }, "current=1",
				null, null);	
		if (mCursor != null && mCursor.getCount() > 0) {
			try {
				if (mCursor.moveToFirst()) {
					int rowCount = mCursor.getCount();
					int i = 0;
					String apn;
					boolean foundAPN = false;
					while (i < rowCount) {
						apn = mCursor.getString(1);
						if (apn.equals("internet-test")) {
							foundAPN = true;
							status("Please make sure APN (internet-test) is the active APN.");
						}
						/*name = name + ", " + mCursor.getString(0) + ", "
								+ mCursor.getString(1) + ", "
								+ mCursor.getString(2);
						status("APN Name = " + name + ", rows = "
								+ mCursor.getCount() + ", apn = "
								+ mCursor.getString(2));*/
						i++;
						mCursor.moveToNext();
					}
					if (!foundAPN) {
						throw new WirelessInfoException("Please create APN (internet-test) to be able to connect to internal FTP server.");
					}
				} 
			} finally {	
				mCursor.close();
			}
		}
		else {
			throw new WirelessInfoException("Please create APN (internet-test) to be able to connect to internal FTP server.");
		}
		return true;
	}
	
	private boolean clearLoginDetails() throws WirelessInfoException {
		if (serverLogin.length > 0 && serverLogin[0] != null) {
			serverLogin[0] = null;
			serverLogin[1] = null;
			
			dbAdapter = new WirelessInfoDBAdapter(this);
			try {
		    	dbAdapter.open();
		    	trace("clearLoginDetails: Opened DB");
		    	if (dbAdapter.deleteAllServerInfo()) {
		    		trace("clearLoginDetails: clear Info in DB");
		    	} else {
		    		trace("clearLoginDetails: clear Info in DB failed");
		    		dbAdapter.close();
		    	}
		    } catch (SQLException e) {
		    	trace("clearLoginDetails: Fail to open db "+e.getMessage());
		    	dbAdapter.close();
		    }
		    
			return true;
		}
		else {
			throw new WirelessInfoException("No Login details to clear.");
		}
	}

	private boolean setLoginDetails() throws WirelessInfoException {

		File ftpdir = new File(Environment.getExternalStorageDirectory()
				+ "/ftp-test");
		if (!(ftpdir.exists() || ftpdir.isDirectory())) {
			status("Please create Dir on sdcard. (/sdcard/ftp-test)");
			Toast t = Toast.makeText(getApplicationContext(),
					"Please create dir on SDCARD (/sdcard/ftp-test)",
					Toast.LENGTH_SHORT);
			t.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
			t.show();

			throw new WirelessInfoException("Dir does not exist. (/sdcard/ftp-test), please create.");

		}
		
		
		try {
			verify.serverInfo();
			// If data exsits in DB set local vars.
			Cursor cursor;
			dbAdapter = new WirelessInfoDBAdapter(this);
		    try {
		    	dbAdapter.open();
		    	trace("setLoginDetails: Opened DB");
		    	cursor = dbAdapter.fetchAllServerInfos();
		    	startManagingCursor(cursor);
		    	if (cursor.getCount() > 0) {
		    		trace("setLoginDetails: Found Login details in DB");
		    		status("Login info good in DB, no need to get.");
		    		if (cursor.moveToFirst()) {
		    			int i = 0;
			         do {
			            serverLogin[i] = new LoginDetails();
						serverLogin[i].setHost(cursor.getString(1));
						serverLogin[i].setPort(cursor.getInt(2));
						serverLogin[i].setId(cursor.getString(3));
						serverLogin[i].setPasswd(cursor.getString(4));
						i = i + 1;
			         } while (cursor.moveToNext() && i < 2);
				      }
				      if (cursor != null && !cursor.isClosed()) {
				         cursor.close();
				      }
		    		dbAdapter.close();
			    	return true;
		    	}
		    	dbAdapter.close();
		    } catch (SQLException e) {
		    	trace("setLoginDetails: Fail to open db "+e.getMessage());
		    	dbAdapter.close();
		    }
	    
		} catch (WirelessInfoException e) {
			trace("setLoginDetails: "+e.getMessage());
		}

		// Check Local variables.
		if (serverLogin.length > 0 && serverLogin[0] != null) {
			status("setLoginDetails: Login info good, no need to get.");
			return true;
		}

		progressDialog = ProgressDialog.show(this, "Please wait....",
				"Retrieving Login Details");
		/*
		 * try { getTestLoginDetails(); progressDialog.dismiss(); } catch
		 * (IOException e) { status
		 * ("setLoginDetails: Failure. "+e.getMessage()); }
		 */

		new Thread(new Runnable() {
			public void run() {
				try {
					getTestLoginDetails();
					progressDialog.dismiss();
				} catch (IOException e) {
					status("setLoginDetails: Failure. " + e.getMessage());
				}
			}
		}).start();
		return true;
	}
	
	public void onClickStartGetLogin(View v) {
		status("onClickStartGetLogin");
		trace("onClickStartGetLogin: Start.");
		
		try {
			checkAPNSettings();
			
			try {
				setLoginDetails();
			} catch (WirelessInfoException e) {
				status(e.getMessage());
			}
		} catch (WirelessInfoException e) {
			status(e.getMessage());
			Toast t = Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_SHORT);
			t.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
			t.show();
		}
	}

	public void onClickStartInternalFTP(View v) {
		status("onClickStartInternalFTP");
		trace("onClickStartInternalFTP: Start.");
		
		try {
			checkAPNSettings();
			
			try {
				setLoginDetails();
				MyFtpTask t = mCurrentFtpTask;
				if (t != null) {
					trace("onClickStartInternalFTP: Please wait for the previous task to finish.");
					status("Please wait for the previous task MyFtpTask to finish.");
				} else if (serverLogin[1] == null) {
					trace("onClickStartInternalFTP: No Login details, please try again");
					status("No Login details, please try again.");
				} else {
					// will test ftp file. No used in download. ftpFileName
					MyFtpTask ft = new MyFtpTask(this, "2MEG", serverLogin[1]);
					mCurrentFtpTask = ft;

					// Start the task.
					trace("onClickStartInternalFTP: Running MyFtpTask.execute start.");
					ft.execute("2MEG");
					trace("onClickStartInternalFTP: Running MyFtpTask.execute done.");
				}
			} catch (WirelessInfoException e) {
				status(e.getMessage());
			}
			
		} catch (WirelessInfoException e) {
			status(e.getMessage());
			Toast t = Toast.makeText(getApplicationContext(),
					e.getMessage(),
					Toast.LENGTH_SHORT);
			t.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
			t.show();
		}

		
	}

	public void onClickStartFTP(View v) {
		status("onClickStartFTP");
		trace("onClickStartFTP: Start.");

		try {
			setLoginDetails();
			MyFtpTask t = mCurrentFtpTask;
			if (t != null) {
				trace("onClickStartFTP: Please wait for the previous task to finish.");
				status("Please wait for the previous task MyFtpTask to finish.");
			} else if (serverLogin[0] == null) {
				trace("onClickStartFTP: No Login details, please try again");
				status("No Login details, please try again.");
			} else {
				MyFtpTask ft = new MyFtpTask(this, ftpFileName, serverLogin[0]);
				mCurrentFtpTask = ft;

				// Start the task.
				trace("onClickStart: Running MyFtpTask.execute.");
				ft.execute("test.txt");
			}
		} catch (WirelessInfoException e) {
			status(e.getMessage());
		}
	}

	public void onClickStopFTP(View v) {

		trace("onClickStopFTP: Stop Button.");

		MyFtpTask t = mCurrentFtpTask;
		if (t == null) {
			trace("onClickStopFTP: There is no task to disconnect.");
			status("There is no task to disconnect.");
		} else {
			endFtpBackgroundTasks(true);
			trace("onClickStopFTP: disconnect executed. Check the Logs.");
			status("No more updates here. Check the Log.");
		}

	}

	public void onFtpTaskCompleted(MyFtpTask task, ThrPutStats moveCount,
			boolean cancelled) {
		status("Task completed");
		if (cancelled) {
			status("Task FTP cancelled after " + moveCount.getFileSizeBytes()
					+ " Bytes, Delta RX Bytes = " + moveCount.getRxBytes());
			trace("onFtpTaskCompleted: Task FTP cancelled after "
					+ moveCount.getFileSizeBytes()
					+ " Bytes, Delta RX Bytes = " + moveCount.getRxBytes());
		} else {
			double rxStatsKb = (double) (moveCount.getRxBytes() * 8) / 1024.00;
			double thput = rxStatsKb / moveCount.getTime();

			status("Task FTP ended after " + moveCount.getFileSizeBytes()
					+ " Bytes, Delta RX Bytes = " + moveCount.getRxBytes()
					+ ", Delta Time = " + moveCount.getTime()
					+ ", Throughput = " + thput + " Kb/s");
			trace("onFtpTaskCompleted: Task FTP ended after "
					+ moveCount.getFileSizeBytes()
					+ " Bytes, Delta RX Bytes = " + moveCount.getRxBytes()
					+ ", Delta Time = " + moveCount.getTime()
					+ ", Throughput = " + thput + " Kb/s");
		}
		mCurrentFtpTask.disconnect();
		mCurrentFtpTask = null;
	}

	private void endFtpBackgroundTasks(boolean cleanup) {
		if (mCurrentFtpTask != null) {

			// If we are cleaning up, it means that the UI is no longer
			// available or will soon be unavailable.
			if (cleanup) {
				mCurrentFtpTask.disconnect();
			}

			// Make sure the task is cancelled.
			mCurrentFtpTask.cancel(true);

			// Finish cleanup by removing the reference to the task
			if (cleanup) {
				mCurrentFtpTask = null;
				trace("endFtpBackgroundTasks: cleanup. Check the Logs.");
				status("endFtpBackgroundTasks: Interrupted and ended task.");
			} else {
				trace("endFtpBackgroundTasks: Interrupted. not null.");
				status("endFtpBackgroundTasks: Interrupted task.");
			}
		}
	}

	public void showFtpProgressOnScreen(int val) {
		// trace
		// ("WirelessInfo: showFtpProgressOnScreen: Got progress report. "+val+"%");
		status("Got progress report from FTP task. "+ val + "%");
	}
	
	private void uploadCellInfoViaWeb(int cellID) {
		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

		NetworkInfo ani = cm.getActiveNetworkInfo();

		if (ani != null && ani.isConnected()) {
			/*
			JSONObject json = new JSONObject();
			try {
				json.put("cellid", cellID);
				json.put("rssi", ssdbm);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				trace("uploadCellInfoViaWeb: "+e.getMessage());
			}
			*/
            
			final AsyncHttpClient client = new AsyncHttpClient();
			
			RequestParams params = new RequestParams();
			params.put("cellid", String.valueOf(cellID));
			params.put("rssi", String.valueOf(ssdbm));
			params.put("imsi", imsi);
			params.put("lat", String.valueOf(lat));
			params.put("lng", String.valueOf(lng));

			 client.post("http://inhere.homelinux.net/test/cellinfo_post.php", params, new AsyncHttpResponseHandler() {
					@Override
					public void onSuccess(String response) {
						trace("uploadCellInfoViaWeb: "+response);
					}
				});
			/* 
			client.get(
					"http://inhere.homelinux.net/test/cellinfo_post.php?",
					new AsyncHttpResponseHandler() {
						@Override
						public void onSuccess(String response) {
							trace("uploadCellInfoViaWeb: HTTP post success.");
						}
					});
			*/
		} else {
			String strResult = "No network available!!";
			Toast t = Toast.makeText(getApplicationContext(),
					strResult, Toast.LENGTH_LONG);
			t.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
			t.show();
		}
	}

	private void getTestLoginDetails() throws IOException {
		trace("getTestLoginDetails: Start.");
		//if (serverLogin == null) {
			//LoginDetails[] serverLogin = new LoginDetails[2];
		//}
		InputStream is = null;
		ByteArrayOutputStream bos = null;
		byte[] data = null;
		try {

			// Build the url
			StringBuilder uri = new StringBuilder(
					"http://inhere.homelinux.net/test/getinfoxml.php");
			/*
			 * StringBuilder uri = new StringBuilder(
			 * "http://cellid.labs.ericsson.net/lookup?cellid=3CB5&mnc=24&mcc=530&lac=0002&key=KJ7DB6kbP6UE4b5O3AskOMttTppKFGHDYuJ81J8T"
			 * );
			 */
			// Set this param to xml to get the server response in XML instead
			// of json

			uri.append("?alt=json");
			/*
			 * uri.append("/lookup?cellid=").append(cid);
			 * uri.append("&mnc=").append(mnc); uri.append("&mcc=").append(mcc);
			 * uri.append("&lac=").append(lac);
			 * uri.append("&key=").append(API_KEY);
			 */
			// Create an HttpGet request
			HttpGet request = new HttpGet(uri.toString());

			// Send the HttpGet request
			HttpClient httpClient = new DefaultHttpClient();
			HttpResponse response = httpClient.execute(request);

			// Check the response status
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
					throw new IOException("You have reached the limit"
							+ "for the number of requests per day. The "
							+ "maximum number of requests per day is "
							+ "currently 500.");
				case HttpURLConnection.HTTP_NOT_FOUND:
					throw new IOException("The cell could not be found"
							+ "in the database");
				default:
					throw new IOException("HTTP response code: " + status);
				}
			}
			trace("getTestLoginDetails: After http connection, getting login details from web.");

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

			trace("getTestLoginDetails: Finished adding byteStream to local var.");
			boolean dbGood = false;
			if (data != null) {
				dbAdapter = new WirelessInfoDBAdapter(this);
				try {
			    	dbAdapter.open();
			    	dbGood = true;
			    	trace("getTestLoginDetails: Opened DB");
			    } catch (SQLException e) {
			    	trace("getTestLoginDetails: Fail to open db "+e.getMessage());
			    	dbAdapter.close();
			    }
				try {
					// Parse the Json data
					JSONArray info = new JSONObject(new String(data)).getJSONArray("info");
					// trace
					// ("WirelessInfo.getTestLoginDetails: After Json. "+position.get("accuracy"));*/
					int numVals = info.length();
					for (int i = 0; i < numVals; i++) {
						JSONObject jobj = info.getJSONObject(i);

						serverLogin[i] = new LoginDetails();
						serverLogin[i].setHost(jobj.getString("server"));
						serverLogin[i].setPort(jobj.getInt("port"));
						serverLogin[i].setId(jobj.getString("id"));
						serverLogin[i].setPasswd(jobj.getString("passwd"));
						
						trace("getTestLoginDetails: Adding new server ("+jobj.getString("server")+") info to DB and local var (ServerInfo)");
						dbAdapter.createServerInfo(jobj.getString("server"), jobj.getInt("port"), jobj.getString("id"), jobj.getString("passwd"));
					}
					
					if (dbGood) {
						dbAdapter.close();
						trace("getTestLoginDetails: Closed DB");
					}

					/*
					 * // update the GUI items with the received position info
					 * ((TextView)
					 * findViewById(R.id.position_longitude)).setText
					 * ("Longitude: " + position.getDouble("longitude"));
					 * ((TextView)
					 * findViewById(R.id.position_latitude)).setText(
					 * "Latitude: " + position.getDouble("latitude"));
					 * ((TextView)
					 * findViewById(R.id.position_name)).setText("Name: " +
					 * position.getString("name")); ((TextView)
					 * findViewById(R.id
					 * .position_accuracy)).setText("Accuracy: " +
					 * position.getDouble("accuracy"));
					 */
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
	
	private String getNetworkTypeString(int type){
		String typeString = "Unknown";
		
		switch(type)
		{
			case TelephonyManager.NETWORK_TYPE_EDGE:	typeString = "EDGE"; break;
			case TelephonyManager.NETWORK_TYPE_GPRS:	typeString = "GPRS"; break;
			case TelephonyManager.NETWORK_TYPE_UMTS:	typeString = "UMTS"; break;
			default:									typeString = "UNKNOWN"; break;
		}
		
		return typeString;
	}
	
	/**
	 * Return a Cell Name as sting
	 * 
	 * @return String
	 */
	
	public String getCellInfoById(int cellid) throws WirelessInfoException {
		String cellname = "", sitename = "";
		trace("getCellInfoById: start.");
		cellInfoDbA = new cellinfoDBAdapter(this);
		trace("getCellInfoById: got value from edittext = "+cellid);
		if (! (cellid > 0 && cellid < 65535)) {
			throw new WirelessInfoException("Invalid cell ID");
		}
			
		try {
			cellInfoDbA.open();
			trace("onClickCellLookup: DB Open");
			try {
				Cursor cellInfoRecord = (Cursor) this.cellInfoDbA.getInfoByCellId(String.valueOf(cellid));
				trace("onClickCellLookup: Cursor = "+cellInfoRecord);
				
				if (cellInfoRecord.getCount() > 0) {
					trace("onClickCellLookup: Found "+cellInfoRecord.getCount()+" records");
					sitename = cellInfoRecord.getString(cellInfoRecord.getColumnIndex(cellinfoDBAdapter.KEY_SITENAME));
					cellname = cellInfoRecord.getString(cellInfoRecord.getColumnIndex(cellinfoDBAdapter.KEY_CELLNAME));
					
					trace("onClickCellLookup: "+cellid+", cellname = "+cellname+", sitename = "+sitename);
					status(cellid+", Cell name = "+cellname+", Site name = "+sitename);
				} else {
					trace("onClickCellLookup: No records");
					status("No records for cell ID "+cellid);
				}
				cellInfoDbA.close();
				
			} catch (SQLException e) {
		    	trace("onClickCellLookup: "+e.getMessage());
		    	cellInfoDbA.close();
		    }
		} catch (SQLException e) {
			trace("onClickCellLookup: "+e.getMessage());
		}
		return cellname+", "+sitename;
	}
	/*
	private boolean startRecording() {
		new Thread(new Runnable() {
			public void run() {
				try {
					tmpStorage.insertData(imsi, cellid, ssdbm, lat, lng);
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
	*/
	
	public void status(String message) {
		TextView tv = (TextView) findViewById(R.id.textStatus);
		tv.setText(message);
	}

	public void trace(String msg) {
		Log.d("WirelessInfo", WirelessInfo.class.getName()+": "+msg);
	}
/*
	@Override
	public void onLocationChanged(Location location) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub
		
	}
*/
}
