package net.homelinux.inhere.wirelessinfo;

import android.os.AsyncTask;
import android.os.Environment;
import android.net.TrafficStats;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.commons.io.FileUtils;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import android.util.Log;

public class ThrPutTest extends AsyncTask<String, Integer, ThrPutStats> {
	
	long FileLenght = 0;
	private test mActivity;
	private String mFilename;
	private boolean mCompleted = false;
	private boolean mCancelled = false;
	
	private String mHost;
	private int mPort = 21;
	private String mId;
	private String mPasswd;
	
	FTPClient ftpConnection = new FTPClient();
	ThrPutStats tps = null;
	
	String strLine;
	DataInputStream inputStream = null;
	BufferedReader bufferedReader = null;
	FTPClient client = null;
	FTPFile[] ftpFiles = null;
	int reply;
	
	public ThrPutTest (test context, String filename, LoginDetails slogin) {
			mActivity = context;
			mFilename = filename;
			mHost = slogin.getHost();
			mPort = slogin.getPort();
			mId = slogin.getId();
			mPasswd = slogin.getPasswd();

			trace("ThrPutTest: Done set vars in constructor");
	}
	
	@Override 
    protected void onCancelled () {
        mCompleted = true;
        mCancelled = true;
        tps = new ThrPutStats(0, 0.0, 1);
        Integer[] mMovesCompleted;
        mMovesCompleted = new Integer[1];
        mMovesCompleted[0] = 1;
        //if (mActivity != null) mActivity.onFtpTaskCompleted (this, tps, mCancelled);
        disconnect ();
    }
	
	@Override
	protected ThrPutStats doInBackground(String... arg0) {
		trace("doInBackground: start, got var = "+arg0[0]+" form parent");
		tps = new ThrPutStats(555, 555, 5555);
		
		mConnect();
		publishProgress( (int) 333 );
		mDisconnect();
		
		return tps;
	}
	
	protected ThrPutStats backupOf_doInBackground(String... arg0) {

		boolean done = false;
		int i = 0;
		Integer[] doneX;
		doneX = new Integer[3];
		doneX[0] = 0;
		trace("doInBackground: start, var = "+arg0[0]);
		trace("Testing file "+mFilename);
		String ftpFileName = arg0[0];
		int byteDownloadSize = 4096;
		
		//publishProgress( i );
		
		while (!done) {
 	       if (isCancelled ()) {
 	          done = true;
 	       } else {
 	    	  FTPClient con = new FTPClient();
 	 		try
 	 		{
 	 			con.connect(mHost, mPort);
 	 		    if (con.login(mId, mPasswd)) {
 	 		    	con.enterRemotePassiveMode();
 	 		    	con.enterLocalActiveMode(); // important!
 	 		        con.setFileType(FTP.BINARY_FILE_TYPE);
 	 		        trace("doInBackground: Connect to FTP server successful");
 	 		        //FTPFile[] remoteFiles = con.listFiles("test.txt");
 	 		        FTPFile[] remoteFiles = null;
 	 		        try {
 	 		        	remoteFiles = con.listFiles(ftpFileName);
 	 		        } catch (IOException f) {
 	 		        	trace("doInBackground: IOException: "+f.getMessage());
 	 		        	con.logout();
	 			    	con.disconnect();
	 			    	done = true;
 	 	 			}
 	 		        trace("doInBackground: File name = "+remoteFiles[0].getName( ));
 	 		        long length = remoteFiles[0].getSize( );
			        String readableLength = FileUtils.byteCountToDisplaySize( length );
			        trace("doInBackground: File size = "+readableLength);
 	 		        /*
 	 		        String remoteDir = "/";
 	 			     //FTPFile[] remoteFiles = con.listFiles( remoteDir );
 	 			     FTPFile[] remoteFiles = con.listFiles(  );
 	 			     System.out.println( "Files in " + remoteDir );
 	 			     for (int i = 0; i < remoteFiles.length; i++) {
 	 			         String name = remoteFiles[i].getName( );
 	 			         if (name.equals("test.txt")) {
 	 			        	 FileLenght = remoteFiles[i].getSize( );
 	 			        	String readableLength = FileUtils.byteCountToDisplaySize( FileLenght );
 	 			         }
 	 			         //long length = remoteFiles[i].getSize( );
 	 			         //String readableLength = FileUtils.byteCountToDisplaySize( length );
 	 			         //System.out.println( name + ":\t\t" + readableLength );
 	 			     }
 	 			     */
			        
			        
			    	
			    	InputStream myFileStream = null;
			    	try {
			    		myFileStream = con.retrieveFileStream(ftpFileName);
 	 		        } catch (IOException f) {
 	 		        	trace("doInBackground: IOException: "+f.getMessage());
 	 		        	con.logout();
	 			    	con.disconnect();
	 			    	done = true;
 	 	 			}
 	 			     
 	 		      trace("doInBackground: before creating inputStream and bufferReader.");
 			      inputStream = new DataInputStream(myFileStream);
 			      bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
 			      //save file from ftp to local directory
 			      File sdDir = Environment.getExternalStorageDirectory();
 			     File fileToWrite = null;
 			      try {
 			    	  fileToWrite = new File(sdDir, "ftp-test/"+ftpFileName);
 			    	 trace("doInBackground: file sdcard good.");
 			      } catch ( NullPointerException e) {
 			    	 trace("doInBackground: Disconnect ftp, file on sdcard/"+ftpFileName+" fault.");
 			    	con.logout();
 			    	con.disconnect();
 			    	 done = true;
 			      }
 			      
 			      java.io.FileOutputStream fos = new java.io.FileOutputStream(fileToWrite);
 			      java.io.BufferedOutputStream bout = new BufferedOutputStream(fos,byteDownloadSize);
 			      byte data[] = new byte[byteDownloadSize];
 			      
 			    long rxSByteSample = TrafficStats.getTotalRxBytes();
		    	long txSByteSample = TrafficStats.getTotalTxBytes();
		    	long startTime = System.currentTimeMillis();
		    	trace("doInBackground: before reading, set start time = "+startTime);
		    	
		    	int x = 0;
 			      
		    	try {
 			      while((x=myFileStream.read(data,0,byteDownloadSize))>=0){
 			          bout.write(data,0,x);
 			          doneX[0] += x;
 			          double perFromTotal = (((double) doneX[0] / (double) length)*100.00);
 			          publishProgress( (int) perFromTotal );
 			          //trace("MyFtpTask.doInBackground inside while FileStream x="+x+",done="+doneX[0]+", per="+perFromTotal);
 			      }
		    	} catch (IOException e) {
		    		trace("doInBackground: reading fail.");
		    		trace(e.getMessage());
		    		con.logout();
 			    	con.disconnect();
 			    	done = true;
		    	}
 	 			      
 	 			    long rxEByteSample = TrafficStats.getTotalRxBytes();
 		        	long txEByteSample = TrafficStats.getTotalTxBytes();
 		        	long rxDByteSample = rxEByteSample - rxSByteSample;
 		        	long txDByteSample = txEByteSample - txSByteSample;
 		        	long endTime = System.currentTimeMillis();
 		        	double deltaTime = ((double) endTime - (double) startTime)/1000.00;
 		        	double rxStatsKb = (double)(rxDByteSample*8)/1024.00;
 		        	trace("doInBackground: Delta time = "+deltaTime+" s, starttime = "+startTime+", endtime = "+endTime);
 		        	
 		        	doneX[1] = (int) rxDByteSample;
 		        	doneX[2] = (int) deltaTime;
 		        	readableLength = FileUtils.byteCountToDisplaySize( (long) (rxDByteSample/deltaTime) );
 		        	trace("doInBackground: Thrput = "+rxStatsKb/deltaTime+" Kb/s");
 		        	trace("doInBackground: Thrput = "+readableLength+"/s");
 		        	//((TextView) findViewById(R.id.txtWebNetStats)).setText("Delta time = "+deltaTime+" s, Thrput = "+rxStatsMb/updateDelta+" Mb/s, Delta Rx/Tx Bytes = "+(rxDByteSample*8)/1024+" kb/"+txDByteSample+" Bytes");
 		        	
 		        	tps = new ThrPutStats(length, deltaTime, rxDByteSample);
 		        	
 	 			      bout.close();
 	 			      myFileStream.close();
 	 		        
 	 			     if(!con.completePendingCommand()) {
 	 			    	 con.logout();
 	 			    	 con.disconnect();
 	 			    	 done = true;
 	 			          //System.err.println("File transfer failed.");
 	 			          trace("doInBackground: File transfer failed.");
 	 			      }
 	 		    }
 	 		} catch (Exception e) {
 	 			   if (con.isConnected()) {
 	 			    try {
 	 			    	con.logout();  
 	 			    	con.disconnect();  
 	 			    	done = true;
 	 			    } catch (IOException f) {
 	 			     // do nothing
 	 			    }
 	 			   }
 	 		} finally {
 	 			   if (con.isConnected())
 	 			            {
 	 			                try
 	 			                {
 	 			                	con.logout();
 	 			                	con.disconnect();
 	 			                }
 	 			                catch (IOException f)
 	 			                {
 	 			                }
 	 			            }
 	 		}
	    	   		done = true;
 	       }
		}
		trace("doInBackground: finished, var i = "+i);
		return tps;
	}
	
	@Override 
    protected void onPostExecute (ThrPutStats moveCount) {
        mCompleted = true;
        //if (mActivity != null) mActivity.onFtpTaskCompleted (this, moveCount, mCancelled);
        disconnect ();
    }
	
	@Override 
    protected void onProgressUpdate(Integer... info) {
        Integer intval = info[0];
        if (mActivity != null) mActivity.showProgressOnScreen (intval);
    }
	
	public long getFileLenght() {
		trace("getFileLenght: inside");
		return FileLenght;
	}

	public void disconnect () {
        if (mActivity != null) {
           mActivity = null;
           trace("disconnect: task has successfully disconnected from the activity.");
        }
    }
	
	public void mConnect() {
		
		try	{
			ftpConnection.connect(mHost, mPort);
	 		    if (ftpConnection.login(mId, mPasswd)) {
	 		    	ftpConnection.enterRemotePassiveMode();
	 		    	ftpConnection.enterLocalActiveMode(); // important!
	 		    	ftpConnection.setFileType(FTP.BINARY_FILE_TYPE);
	 		        trace("mConnect: Connect to FTP server ("+mHost+") successful");
	 		    }
		} catch (Exception e) {
			   if (ftpConnection.isConnected()) {
	 			    try {
	 			    	ftpConnection.logout();  
	 			    	ftpConnection.disconnect(); 
	 			    	trace("mConnect: Connect to FTP server fail, trying logout");
	 			    } catch (IOException f) {
	 			    	trace("mConnect: Connect to FTP server fail "+f.getMessage());
	 			    }
	 			}
	 	}
		
	}
	
	public void mDisconnect() {
		try {
		    	ftpConnection.logout();  
		    	ftpConnection.disconnect();
		    	trace("mDisconnect: Disconnect from FTP server");
		    } catch (IOException f) {
		    	trace("mDisconnect: Logout fail = "+f.getMessage());
		    }
	}
	
	public void trace(String msg) {
		Log.d("WirelessInfo", ThrPutTest.class.getName()+": "+msg);
	}
}
