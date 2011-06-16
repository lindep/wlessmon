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
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.telephony.NeighboringCellInfo;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.telephony.SignalStrength;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class WirelessInfo extends Activity {

 private final static String API_KEY = "KJ7DB6kbP6UE4b5O3AskOMttTppKFGHDYuJ81J8T";

 private TelephonyManager tm;
 MyPhoneStateListener MyListener;
 private GsmCellLocation location;
 private int cid, lac, mcc, mnc, cellPadding;
 private String networkType;

 /** Called when the activity is first created. */
 @Override
 public void onCreate(Bundle savedInstanceState) {
  super.onCreate(savedInstanceState);
  setContentView(R.layout.main);

  MyListener   = new MyPhoneStateListener();
  tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
  tm.listen(MyListener ,PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
  
  /*
   * Setup a listener for the UpdateCellButton. Pressing this button will fetch
   * the current cell info from the phone.
   */
  final Button UpdateCellButton = (Button) findViewById(R.id.UpdateCellButton);
  UpdateCellButton.setOnClickListener(new View.OnClickListener() {
   public void onClick(View v) {
    location = (GsmCellLocation) tm.getCellLocation();
    cid = location.getCid();
    lac = location.getLac();

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
     /*
     cid = cid - 65536;
      */
    } else {
     cellPadding = 4;
     networkType = "GSM";
    }

    ((TextView) findViewById(R.id.other_txt)).setText("Network Type: "
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
      + getPaddedInt(mcc, 3));
    ((TextView) findViewById(R.id.TextView04)).setText("MNC: "
      + getPaddedInt(mnc, 2));
   }
  });

  /*
   * Setup a listener for the GetPositionButton. When pressing this button the
   * cell info is sent to the server and hopefully we will get a longitude and
   * latitude back.
   */
  final Button GetPositionButton = (Button) findViewById(R.id.GetPositionButton);
  GetPositionButton.setOnClickListener(new View.OnClickListener() {
   public void onClick(View v) {

    String strResult;
    
    /**
     * Make sure data services up
     */
    ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
    NetworkInfo niw = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
    boolean isWifiAvail = niw.isAvailable();
    boolean isWifiConn = niw.isConnected();
    
    NetworkInfo ni = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
    boolean isMobileAvail = ni.isAvailable();
    boolean isMobileConn = ni.isConnected();
    strResult = "No network available!!";
    
    if ((isWifiAvail && isWifiConn) || (isMobileAvail && isMobileConn)) {
    /**
     * Seems that cid and lac shall be in hex. Cid should be padded with zero's
     * to 8 numbers if UMTS (3G) cell, otherwise to 4 numbers. Mcc padded to 3
     * numbers. Mnc padded to 2 numbers.
     */
    	try {
    		// Update the current location
    		updateLocation(getPaddedHex(cid, cellPadding), getPaddedHex(lac, 4),
    				getPaddedInt(mnc, 2), getPaddedInt(mcc, 3));
    		strResult = "Position updated!";
    	} catch (IOException e) {
    		strResult = "Error!\n" + e.getMessage();
    	}
    }

    // Show an info Toast with the results of the updateLocation
    // call.
    Toast t = Toast.makeText(getApplicationContext(), strResult,
      Toast.LENGTH_LONG);
    t.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
    t.show();
   }
  });
 }


    /* Called when the application is minimized */
   @Override
   protected void onPause() {
      super.onPause();
      tm.listen(MyListener, PhoneStateListener.LISTEN_NONE);
   }

    /* Called when the application resumes */
   @Override
   protected void onResume() {
      super.onResume();
      tm.listen(MyListener,PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
   }


    /* —————————– */
    /* Start the PhoneState listener */
    /* —————————– */
    private class MyPhoneStateListener extends PhoneStateListener {
      /* Get the Signal strength from the provider, each time there is an update */
      private int ss, ssdbm, cdmadbm;

      @Override
      public void onSignalStrengthsChanged(SignalStrength signalStrength) {
         super.onSignalStrengthsChanged(signalStrength);
         cdmadbm = signalStrength.getEvdoDbm();
         int cdmaecio = signalStrength.getEvdoEcio();
         ss = signalStrength.getGsmSignalStrength();
         
         ssdbm = getRSSI(ss);
         ((TextView) findViewById(R.id.other_txt1)).setText("RSSI: -"+ ssdbm +"dBm");
         Toast.makeText(getApplicationContext(), "CINR = "+ String.valueOf(signalStrength.getGsmSignalStrength()) +", -"+ ssdbm +"dBm, "+ cdmadbm +", "+ cdmaecio, Toast.LENGTH_SHORT).show();
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

 private void updateLocation(String cid, String lac, String mnc, String mcc)
   throws IOException {
  InputStream is = null;
  ByteArrayOutputStream bos = null;
  byte[] data = null;
  try {

   // Build the url
   StringBuilder uri = new StringBuilder("http://cellid.labs.ericsson.net/");
   // Set this param to xml to get the server response in XML instead
   // of json
   uri.append("json");
   uri.append("/lookup?cellid=").append(cid);
   uri.append("&mnc=").append(mnc);
   uri.append("&mcc=").append(mcc);
   uri.append("&lac=").append(lac);
   uri.append("&key=").append(API_KEY);

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
   if (data != null) {
    try {
     // Parse the Json data
     JSONObject position = new JSONObject(new String(data))
       .getJSONObject("position");

     // update the GUI items with the received position info
     ((TextView) findViewById(R.id.position_longitude)).setText("Longitude: "
       + position.getDouble("longitude"));
     ((TextView) findViewById(R.id.position_latitude)).setText("Latitude: "
       + position.getDouble("latitude"));
     ((TextView) findViewById(R.id.position_name)).setText("Name: "
       + position.getString("name"));
     ((TextView) findViewById(R.id.position_accuracy)).setText("Accuracy: "
       + position.getDouble("accuracy"));
    } catch (JSONException e) {
     e.printStackTrace();
    } catch (Exception e) {
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
}

