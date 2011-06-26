/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.homelinux.inhere.wirelessinfo;

/*
 * Pieter Linde
 */

import java.util.List;

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
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.*;

public class WirelessInfo extends Activity {

 //private final static String API_KEY = "KJ7DB6kbP6UE4b5O3AskOMttTppKFGHDYuJ81J8T";

 private TelephonyManager tm;
 MyPhoneStateListener MyListener;
 private GsmCellLocation location;
 private int cid, lac, mcc, mnc, cellPadding;
 private String networkType, SignalHeading, imsi;
 
 private TextView webPageText;


 /** Called when the activity is first created. */
 @Override
 public void onCreate(Bundle savedInstanceState) {
  super.onCreate(savedInstanceState);
  setContentView(R.layout.main);

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
     SignalHeading = "RSSI";
     /*
     cid = cid - 65536;
      */
    } else {
     cellPadding = 4;
     networkType = "GSM";
     SignalHeading = "RSCP";
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
	    //client.get("http://www.google.com", new AsyncHttpResponseHandler() {
    	client.get("http://inhere.homelinux.net/test/xml_post.php?id=3&name=testing&score=555", new AsyncHttpResponseHandler() {
	        @Override
	        public void onSuccess(String response) {
	            //System.out.println(response);

	        	webPageText.setText(response);
	        	Toast t = Toast.makeText(getApplicationContext(), "Web Page downloaded successful",
	          	      Toast.LENGTH_LONG);
	          	t.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
	          	t.show();
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
}

