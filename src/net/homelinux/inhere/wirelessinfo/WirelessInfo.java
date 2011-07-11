/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.homelinux.inhere.wirelessinfo;

/*
 * Pieter Linde
 */

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.database.Cursor;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.NeighboringCellInfo;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.telephony.SignalStrength;
import android.telephony.gsm.GsmCellLocation;
import android.text.TextUtils;
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

import com.loopj.android.http.*;

public class WirelessInfo extends Activity implements LocationListener {

 //private final static String API_KEY = "KJ7DB6kbP6UE4b5O3AskOMttTppKFGHDYuJ81J8T";
	
	private ProgressDialog progressDialog;

 private TelephonyManager tm;
 MyPhoneStateListener MyListener;
 private GsmCellLocation mobileLocation;
 private int cid, lac, mcc, mnc, cellPadding;
 private String networkType, SignalHeading, imsi;
 
 private TextView webPageText;
 
 private TextView latituteField;
 private TextView longitudeField;
 private LocationManager locationManager;
 private String provider;
 
 private MyFtpTask mCurrentFtpTask = null;
 
 ThrPutStats tps = null;
 
 String testvar;
 LoginDetails[] serverLogin = new LoginDetails[2];

 @Override
 public void onCreate(Bundle savedInstanceState) {
  super.onCreate(savedInstanceState);
  setContentView(R.layout.main);
  
  latituteField = (TextView) findViewById(R.id.unknownLat);
  longitudeField = (TextView) findViewById(R.id.unknownLong);
  
//Get the location manager
	locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
	// Define the criteria how to select the location provider -> use
	// default
	Criteria criteria = new Criteria();
	provider = locationManager.getBestProvider(criteria, false);
	Location location = locationManager.getLastKnownLocation(provider);

	// Initialize the location fields
	if (location != null) {
		System.out.println("Provider " + provider + " has been selected.");
		double lat = (location.getLatitude());
		double lng = (location.getLongitude());
		latituteField.setText(String.valueOf(lat));
		longitudeField.setText(String.valueOf(lng));
	} else {
		latituteField.setText("Provider not available");
		longitudeField.setText("Provider not available");
	}

  MyListener   = new MyPhoneStateListener();
  tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
  tm.listen(MyListener ,PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
  imsi = tm.getSubscriberId();
  
  ((TextView) findViewById(R.id.TextViewImsi)).setText("IMSI: "+imsi);
  webPageText = (TextView) findViewById(R.id.webPageText);
  
  final AsyncHttpClient client = new AsyncHttpClient();

  /*
   * Setup a listener for the UpdateCellButton. Pressing this button will fetch
   * the current cell info from the phone.
   */
  
  final Button UpdateCellButton = (Button) findViewById(R.id.UpdateCellButton);
  
  UpdateCellButton.setOnClickListener(new View.OnClickListener() {
   public void onClick(View v) {
	   mobileLocation = (GsmCellLocation) tm.getCellLocation();
    cid = mobileLocation.getCid();
    lac = mobileLocation.getLac();
    
    /*
     * Mcc and mnc is concatenated in the networkOperatorString. The first 3
     * chars is the mcc and the last 2 is the mnc.
     */
    String networkOperator = tm.getNetworkOperator();
    if (networkOperator != null && networkOperator.length() > 0) {
     try {
      mcc = Integer.parseInt(networkOperator.substring(0, 3));
      mnc = Integer.parseInt(networkOperator.substring(3));
     } catch (NumberFormatException e) {
     }
    }
    
    /*TelephonyManager test = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);*/ 
    TextView Neighboring = (TextView)findViewById(R.id.neighboring);
    List<NeighboringCellInfo> NeighboringList = tm.getNeighboringCellInfo();
    
    String stringNeighboring = "Neighboring List- Lac : Cid : RSSI\n";
    for(int i=0; i < NeighboringList.size(); i++){
    	String dBm;
    	int rssi = NeighboringList.get(i).getRssi();
    	if(rssi == NeighboringCellInfo.UNKNOWN_RSSI){
    		dBm = "Unknown RSSI";
    		}else{
    			dBm = String.valueOf(-113 + 2 * rssi) + " dBm";
    			}
    	stringNeighboring = stringNeighboring
    	+ String.valueOf(NeighboringList.get(i).getLac()) +" : "
    	+ String.valueOf(NeighboringList.get(i).getCid()) +" : "
    	+ dBm +"\n";
    	}
    
    Neighboring.setText(stringNeighboring);
       
    /*
     * Check if the current cell is a UMTS (3G) cell. If a 3G cell the cell id
     * padding will be 8 numbers, if not 4 numbers.
     */
    if (tm.getNetworkType() == TelephonyManager.NETWORK_TYPE_UMTS || tm.getNetworkType() == TelephonyManager.NETWORK_TYPE_HSDPA) {
     cellPadding = 8;
     networkType = "UMTS";
     SignalHeading = "RSSI";
     /*
     cid = cid - 65536;
      */
    } else {
     cellPadding = 4;
     networkType = "GSM";
     SignalHeading = "RSCP";
    }

    ((TextView) findViewById(R.id.dataType)).setText("Network Type: "
      + TelephonyManager.NETWORK_TYPE_UMTS + ":" + tm.getDataActivity());

    /*
     * Update the GUI with the current cell's info
     */
    ((TextView) findViewById(R.id.networktype)).setText("Network Type: "
      + networkType + ": " + tm.getNetworkType());
    ((TextView) findViewById(R.id.TextView01)).setText("CellID: "
      + getCellId(cid, networkType)  + ": " + cid);
    ((TextView) findViewById(R.id.TextView02)).setText("LAC: "
      + getPaddedHex(lac, 4) + ": " + getPaddedInt(lac, 4));
    ((TextView) findViewById(R.id.TextView03)).setText("MCC: "
      + getPaddedInt(mcc, 3)+", MNC: "+ getPaddedInt(mnc, 2));
    //((TextView) findViewById(R.id.TextView04)).setText("MNC: "+ getPaddedInt(mnc, 2));
    
    if (TrafficStats.getMobileRxBytes() == TrafficStats.UNSUPPORTED) {
		((TextView) findViewById(R.id.txtNetStats)).setText("Stats: RxBytes - UNSUPPORTED");
		
	}
	else {
		long TotalTxBytes = TrafficStats.getTotalTxBytes();
		((TextView) findViewById(R.id.txtNetStats)).setText("Rx/Tx Bytes = "+String.valueOf(TrafficStats.getTotalRxBytes())+"/"+TotalTxBytes);
	}
   }
  });

  /*
   * Setup a listener for the GetWebPageButton. When pressing this button the
   * cell info is sent to the server and hopefully we will get a web page.
   */
  final Button GetWebPageAction = (Button) findViewById(R.id.GetWebPageButton);
  GetWebPageAction.setOnClickListener(new View.OnClickListener() {
   public void onClick(View v) {
	   
	
	
    ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
    webPageText.setText("Info:");
    
    /** 
    * Check the active network connection.
    */
    NetworkInfo ani = cm.getActiveNetworkInfo();
    
    if (ani != null && ani.isConnected()) {
    	final long rxSByteSample = TrafficStats.getTotalRxBytes();
    	final long txSByteSample = TrafficStats.getTotalTxBytes();
    	final long start=System.currentTimeMillis();
    	
    	//client.get("http://www.google.com", new AsyncHttpResponseHandler() {
    	client.get("http://inhere.homelinux.net/test/xml_post.php?id=3&name=testing&score=555", new AsyncHttpResponseHandler() {
	        @Override
	        public void onSuccess(String response) {
	            //System.out.println(response);

	        	long rxEByteSample = TrafficStats.getTotalRxBytes();
	        	long txEByteSample = TrafficStats.getTotalTxBytes();
	        	long rxDByteSample = rxEByteSample - rxSByteSample;
	        	long txDByteSample = txEByteSample - txSByteSample;
	        	double updateDelta = (double) (System.currentTimeMillis()- start)/1000.00;
	        	double rxStatsMb = (double)(rxDByteSample*8)/1024.00/1024.00;

	        	((TextView) findViewById(R.id.txtWebNetStats)).setText("Delta time = "+updateDelta+" s, Thrput = "+rxStatsMb/updateDelta+" Mb/s, Delta Rx/Tx Bytes = "+(rxDByteSample*8)/1024+" kb/"+txDByteSample+" Bytes");
	        	
	        	webPageText.setText(response);
	        	/*
	        	Toast t = Toast.makeText(getApplicationContext(), "Web Page downloaded successful",
	          	      Toast.LENGTH_SHORT);
	          	t.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
	          	t.show();
	          	*/
	        }
	    });
    }
    else {
    	String strResult = "No network available!!";
    	Toast t = Toast.makeText(getApplicationContext(), strResult,
    	      Toast.LENGTH_LONG);
    	t.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
    	t.show();
    }
   }
   });
 }
 @Override
 public boolean onCreateOptionsMenu(Menu menu){
	 MenuInflater inflater = getMenuInflater();
	 inflater.inflate(R.menu.menu, menu);
	 return true;
 }
 
 @Override
 public boolean onOptionsItemSelected(MenuItem item) {
     switch (item.getItemId()) {
         case R.id.icon:     Toast.makeText(this, "You pressed the icon!", Toast.LENGTH_LONG).show();
                             break;
         case R.id.text:     Toast.makeText(this, "You pressed the text!", Toast.LENGTH_LONG).show();
                             break;
         case R.id.icontext: Toast.makeText(this, "You pressed the icon and text!", Toast.LENGTH_LONG).show();
                             break;
     }
     return true;
 }


    /* Called when the application is minimized */
   @Override
   protected void onPause() {
      super.onPause();
      tm.listen(MyListener, PhoneStateListener.LISTEN_NONE);
      locationManager.removeUpdates(this);
   }

    /* Called when the application resumes */
   @Override
   protected void onResume() {
      super.onResume();
      tm.listen(MyListener,PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
      locationManager.requestLocationUpdates(provider, 400, 1, this);
   }


    /* —————————– */
    /* Start the PhoneState listener */
    /* —————————– */
    private class MyPhoneStateListener extends PhoneStateListener {
      /* Get the Signal strength from the provider, each time there is an update */
      private int ss, ber, ssdbm, cdmadbm;

      @Override
      public void onSignalStrengthsChanged(SignalStrength signalStrength) {
         super.onSignalStrengthsChanged(signalStrength);
         cdmadbm = signalStrength.getEvdoDbm();
         //int cdmaecio = signalStrength.getEvdoEcio();
         ss = signalStrength.getGsmSignalStrength();
         ber = signalStrength.getGsmBitErrorRate();
         
         
         ssdbm = getRSSI(ss);

         ((TextView) findViewById(R.id.other_txt1)).setText(SignalHeading+": -"+ ssdbm +"dBm");
         //Toast.makeText(getApplicationContext(), "CINR = "+ String.valueOf(signalStrength.getGsmSignalStrength()) +", -"+ ssdbm +"dBm, "+ cdmadbm +", "+ cdmaecio+", BER ="+ber, Toast.LENGTH_SHORT).show();
      }
    };/* End of private Class */

   public int getRSSI(int ss) {
       int ssdbm;
        if (ss == 0) {
             ssdbm = 113;
         }
         else if (ss == 1) {
             ssdbm = 111;
         }
         else if (ss > 1 && ss < 31) {
             ssdbm = 109 - (ss * 2);
         }
         else {
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
         str = str.substring(str.length()-4);
     }
     str = String.valueOf(Integer.parseInt(str,16));
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
 
 @Override
	public void onLocationChanged(Location location) {
		double lat = (location.getLatitude());
		double lng = (location.getLongitude());
		latituteField.setText(String.valueOf(lat));
		longitudeField.setText(String.valueOf(lng));
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onProviderEnabled(String provider) {
		Toast.makeText(this, "Enabled new provider " + provider,
				Toast.LENGTH_SHORT).show();

	}

	@Override
	public void onProviderDisabled(String provider) {
		Toast.makeText(this, "Disenabled provider " + provider,
				Toast.LENGTH_SHORT).show();
	}
	
	public void onClickStartGetLogin (View v) {
    	status ("onClickStartGetLogin");
    	trace ("TestAH: onClickStartGetLogin: Start.");
    	
    	setLoginDetails();
    	
    	Cursor mCursor = getContentResolver().query(Uri.parse("content://telephony/carriers"), new String[] {"name", "apn", "current"}, "current=1", null, null); 
                        if (mCursor!=null) { 
                                try { 
                                if (mCursor.moveToFirst()) { 
                                	int i = 0;
                                	int t = mCursor.getCount();
                                	String name = "";
                                	while (i < t) {
                                		name = name+", "+mCursor.getString(0)+", "+mCursor.getString(1)+", "+mCursor.getString(2); 
                                        status ("APN Name = "+name+", rows = "+mCursor.getCount()+", apn = "+mCursor.getString(2));
                                        i++;
                                        mCursor.moveToNext();
                                	}
                                } 
                                } finally { 
                                        mCursor.close(); 
                                } 
                        }
    }
	
	private boolean setLoginDetails() {
		
		if (serverLogin.length > 0 && serverLogin[0] != null) {
			status ("setLoginDetails: Login info good, no need to get.");
			return true;
		}
		
		progressDialog = ProgressDialog.show(this, "Please wait....", "Retrieving Login Details");
		/*
		try {
			getTestLoginDetails();
			progressDialog.dismiss();
		} catch (IOException e) {
			status ("setLoginDetails: Failure. "+e.getMessage());
		}
		*/
		
		
    	new Thread(new Runnable(){
    		public void run(){
    			try {
    				getTestLoginDetails();
    				progressDialog.dismiss();
    			} catch (IOException e) {
    				status ("setLoginDetails: Failure. "+e.getMessage());
    			}
    		}
    	}).start();
    	return true;
	}
	
	public void onClickStartInternalFTP (View v) {
    	status ("onClickStartInternalFTP");
    	trace ("TestAH: onClickStartInternalFTP: Start.");

    	setLoginDetails();
    	
    	if (setLoginDetails()) {
    		status ("Start FTP session to server = "+serverLogin[1].getHost()+", port = "+serverLogin[1].getPort());
	    	MyFtpTask t = mCurrentFtpTask;
	    	if (t != null) {
	        	trace ("TestAH: onClickStartFTP: Please wait for the previous task to finish.");
	           status ("Please wait for the previous task MyFtpTask to finish.");
	        } else if (serverLogin[0] == null) {
	        	trace ("TestAH: onClickStartFTP: No Login details, please try again");
		           status ("No Login details, please try again.");
	        } else {
	        	MyFtpTask ft = new MyFtpTask (this, "1MEG", serverLogin[1]);
	        	mCurrentFtpTask = ft;
	
	          // Start the task.
	          trace ("TestAH: onClickStart: Running MyFtpTask.execute.");
	          ft.execute ("1MEG");  
	        }
    	}
    	else {
    		status ("Failed Getting new login details!!!");
    	}
    }
 
	public void onClickStartFTP (View v) {
    	status ("onClickStartFTP");
    	trace ("TestAH: onClickStartFTP: Start.");

    	if (setLoginDetails()) {
	    	MyFtpTask t = mCurrentFtpTask;
	    	if (t != null) {
	        	trace ("TestAH: onClickStartFTP: Please wait for the previous task to finish.");
	           status ("Please wait for the previous task MyFtpTask to finish.");
	        } else if (serverLogin[0] == null) {
	        	trace ("TestAH: onClickStartFTP: No Login details, please try again");
		           status ("No Login details, please try again.");
	        } else {
	        	MyFtpTask ft = new MyFtpTask (this, "test.txt", serverLogin[0] );
	        	mCurrentFtpTask = ft;
	
	          // Start the task.
	          trace ("TestAH: onClickStart: Running MyFtpTask.execute.");
	          ft.execute ("test.txt");  
	        }
    	}
    	else {
    		status ("Failed Getting new login details!!!");
    	}
    }
    
    public void onClickStopFTP (View v) {
    
    	trace ("TestAH.onClickStopFTP: Stop Button.");
		
		MyFtpTask t = mCurrentFtpTask;
	    if (t == null) {
	    	trace ("TestAH.onClickStopFTP: There is no task to disconnect.");
	       status ("There is no task to disconnect.");
	    } else {
	    	endFtpBackgroundTasks(true);
	      trace ("TestAH.onClickStopFTP: disconnect executed. Check the Logs.");
	      status ("No more updates here. Check the Log.");
	    }

    }
    public void onFtpTaskCompleted (MyFtpTask task, ThrPutStats moveCount, boolean cancelled)
    {
        status ("Task completed");
        if (cancelled) {
           status ("Task FTP cancelled after "+moveCount.getFileSizeBytes()+" Bytes, Delta RX Bytes = "+moveCount.getRxBytes());
           trace ("TestAH.onFtpTaskCompleted: Task FTP cancelled after "+moveCount.getFileSizeBytes()+" Bytes, Delta RX Bytes = "+moveCount.getRxBytes());
        } else {
        	double rxStatsKb = (double)(moveCount.getRxBytes()*8)/1024.00;
        	double thput = rxStatsKb / moveCount.getTime();
        	
           status ("Task FTP ended after "+moveCount.getFileSizeBytes()+" Bytes, Delta RX Bytes = "+moveCount.getRxBytes()+", Delta Time = "+moveCount.getTime()+", Throughput = "+thput+" Kb/s");
           trace ("TestAH.onFtpTaskCompleted: Task FTP ended after "+moveCount.getFileSizeBytes()+" Bytes, Delta RX Bytes = "+moveCount.getRxBytes()+", Delta Time = "+moveCount.getTime()+", Throughput = "+thput+" Kb/s");
       }
        mCurrentFtpTask.disconnect ();
        mCurrentFtpTask = null;
    }
    
    private void endFtpBackgroundTasks (boolean cleanup) {
        if (mCurrentFtpTask != null) {

           // If we are cleaning up, it means that the UI is no longer available or will soon be unavailable.
           if (cleanup) {
        	   mCurrentFtpTask.disconnect ();
           }

           // Make sure the task is cancelled.
           mCurrentFtpTask.cancel (true);

           // Finish cleanup by removing the reference to the task
           if (cleanup) {
        	   mCurrentFtpTask = null;
        	   trace ("TestAH.endFtpBackgroundTasks: cleanup. Check the Logs.");
              status ("endFtpBackgroundTasks: Interrupted and ended task.");
           } else {
        	   trace ("TestAH.endFtpBackgroundTasks: Interrupted. not null.");
        	   status ("endFtpBackgroundTasks: Interrupted task.");
           }
        }
    }
    
    public void showFtpProgressOnScreen (int val) {
    	//trace ("TestAH: showFtpProgressOnScreen: Got progress report. "+val+"%");
    	status ("TestAH.showFtpProgressOnScreen: Got progress report from FTP task. "+val+"%");
    }
    
    public void status (String message) {
        TextView tv = (TextView) findViewById (R.id.textStatus);
        tv.setText (message);
    }
    
    public void trace (String msg) 
    {
        Log.d("Demo", msg);
    }
    
    private void getTestLoginDetails() throws IOException {
    	trace ("WirelessInfo.getTestLoginDetails: Start.");
   InputStream is = null;
   ByteArrayOutputStream bos = null;
   byte[] data = null;
   try {

    // Build the url
    StringBuilder uri = new StringBuilder("http://inhere.homelinux.net/test/getinfo.php"); 
    /*StringBuilder uri = new StringBuilder("http://cellid.labs.ericsson.net/lookup?cellid=3CB5&mnc=24&mcc=530&lac=0002&key=KJ7DB6kbP6UE4b5O3AskOMttTppKFGHDYuJ81J8T");*/
    // Set this param to xml to get the server response in XML instead
    // of json
    /*
    uri.append("json");
    uri.append("/lookup?cellid=").append(cid);
    uri.append("&mnc=").append(mnc);
    uri.append("&mcc=").append(mcc);
    uri.append("&lac=").append(lac);
    uri.append("&key=").append(API_KEY);
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
      throw new IOException("The cell could not be " + "found in the database");
     case HttpURLConnection.HTTP_BAD_REQUEST:
      throw new IOException("Check if some parameter "
        + "is missing or misspelled");
     case HttpURLConnection.HTTP_UNAUTHORIZED:
      throw new IOException("Make sure the API key is " + "present and valid");
     case HttpURLConnection.HTTP_FORBIDDEN:
      throw new IOException("You have reached the limit"
        + "for the number of requests per day. The "
        + "maximum number of requests per day is " + "currently 500.");
     case HttpURLConnection.HTTP_NOT_FOUND:
      throw new IOException("The cell could not be found" + "in the database");
     default:
      throw new IOException("HTTP response code: " + status);
     }
    }
    trace ("WirelessInfo.getTestLoginDetails: After http connection.");

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
    
    trace ("WirelessInfo.getTestLoginDetails: Before adding data to string.");
    if (data != null) {
    	String strResponse = new String(data);
    	int servers = 0;
  	  	StringTokenizer st = new StringTokenizer(strResponse, ";"); 
  	  	while(st.hasMoreTokens()) {
  		  String s1 = st.nextToken();
  		  serverLogin[servers] = new LoginDetails();
  		  
  		  StringTokenizer st1 = new StringTokenizer(s1, "#");
      	  while(st1.hasMoreTokens()) {
      		  serverLogin[servers].setHost(st1.nextToken());
      		  serverLogin[servers].setPort(Integer.parseInt(st1.nextToken()));
      		  serverLogin[servers].setId(st1.nextToken());
      		  serverLogin[servers].setPasswd(st1.nextToken());
      	  }
      	  servers++;
  	  }
    	//status(new String(data));
     //try {
      // Parse the Json data
      /*JSONObject position = new JSONObject(new String(data)).getJSONObject("position");
      trace ("WirelessInfo.getTestLoginDetails: After Jason. "+position.get("accuracy"));*/
      /*serverLogin[0] = new LoginDetails();
      serverLogin[0].setHost(info.getString("server"));
      serverLogin[0].setPort(info.getInt("port"));*/
      
/*
      // update the GUI items with the received position info
      ((TextView) findViewById(R.id.position_longitude)).setText("Longitude: "
        + position.getDouble("longitude"));
      ((TextView) findViewById(R.id.position_latitude)).setText("Latitude: "
        + position.getDouble("latitude"));
      ((TextView) findViewById(R.id.position_name)).setText("Name: "
        + position.getString("name"));
      ((TextView) findViewById(R.id.position_accuracy)).setText("Accuracy: "
        + position.getDouble("accuracy"));
      */
    /* } catch (JSONException e) {
    	 trace (e.getMessage());
      e.printStackTrace();
     } catch (Exception e) {
    	 trace (e.getMessage());
      e.printStackTrace();
     }*/
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

}

