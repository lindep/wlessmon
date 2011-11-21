package net.homelinux.inhere.wirelessinfo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import com.google.gson.Gson;
import net.homelinux.inhere.wirelessinfo.database.TmpStorage;
import net.homelinux.inhere.wirelessinfo.verification.WirelessInfoException;
import android.database.Cursor;
import android.util.Log;

public class UploadData {
	private TmpStorage tmpStorage;
	private CellStatsObject[] statsObj;
	
	public UploadData() {
		
		tmpStorage = new TmpStorage(false);
		Cursor cursor;
		try {
			cursor = tmpStorage.SelectAll();	
			int numRecords = cursor.getCount();
			if (numRecords > 0) {
	    		statsObj = new CellStatsObject[numRecords];
	    		trace("UploadData: Found "+numRecords+" cell stats data");
	    		if (cursor.moveToFirst()) {
	    			int i = 0;
	    			trace("UploadData: Adding index = "+i+" for "+cursor.getString(4)+" to object");
		         do {	        	 
		        	 statsObj[i] = new CellStatsObject();
		        	 statsObj[i].imsi =  cursor.getString(0);
		        	 statsObj[i].timeEnter =  cursor.getString(1);
		        	 statsObj[i].cellid =  cursor.getString(2);
		        	 statsObj[i].rssi =  cursor.getString(3);
		        	 double lat = cursor.getDouble(4);
		        	 double lng = cursor.getDouble(5);
		        	 statsObj[i].lat =  String.valueOf(lat);
		        	 statsObj[i].lng =  String.valueOf(lng);
					i = i + 1;
		         } while (cursor.moveToNext());
			      }
			      if (cursor != null && !cursor.isClosed()) {
			         cursor.close();
			      }
			      trace("UploadData: statsObj populated, "+statsObj.length+" records");
	    	}

		} catch (WirelessInfoException e) {
			tmpStorage.close();
			trace("UploadData: Temp Storage error "+e.getMessage());
		}
    	tmpStorage.close();
	}
	
	public int size() {
		if (statsObj != null) {
			return statsObj.length;
		}
		return 0;
	}
	
	public void uploadDriveTestViaWeb() throws WirelessInfoException {
		
		if (statsObj != null) {
			Gson gsona = new Gson();
			String json = gsona.toJson(statsObj);
			
			HttpClient client = new DefaultHttpClient();
			try {
				HttpPost post = new HttpPost("http://inhere.homelinux.net/test/uploaddata.php");
				try {
					List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
					nameValuePairs.add(new BasicNameValuePair("json", json));
					nameValuePairs.add(new BasicNameValuePair("id",	"53024"));
					post.setEntity(new UrlEncodedFormEntity(nameValuePairs));
		 
					HttpResponse response = client.execute(post);
					StatusLine status = response.getStatusLine();
				    if (status.getStatusCode() != 200) {
				    	throw new WirelessInfoException("Invalid response from server: " + status.toString());
				    }
					/*
					BufferedReader rd = new BufferedReader(new InputStreamReader(
							response.getEntity().getContent()));
					String line = "";
					while ((line = rd.readLine()) != null) {
						line = line+" "+line;
					}
					*/
					trace("uploadDriveTestViaWeb: Done, got return code = "+status.toString());

				} catch (IOException e) {
					throw new WirelessInfoException(e.getMessage()); 
				}
			} catch (IllegalArgumentException e) {
				throw new WirelessInfoException("Invalid uri request: "+e.getMessage());
			}
			
			

		/*
			final AsyncHttpClient client = new AsyncHttpClient();
			
			RequestParams params = new RequestParams();
			params.put("json", json);

			 client.post("http://inhere.homelinux.net/test/t.php", params, new AsyncHttpResponseHandler() {
					@Override
					public void onSuccess(String response) {
						trace("uploadCellInfoViaWeb: "+response);
					}
				});
		*/
		}
		else {
			throw new WirelessInfoException("Object does not exists"); 
		}
	}
	
	public void trace(String msg) {
		Log.d("WirelessInfo", WirelessInfo.class.getName()+": "+msg);
	}
}
