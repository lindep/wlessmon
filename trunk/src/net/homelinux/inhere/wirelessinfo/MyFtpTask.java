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

public class MyFtpTask extends AsyncTask<String, Integer, Integer[]> {
	
	long FileLenght = 0;
	private WirelessInfo mActivity;
	private String mFilename;
	private boolean mCompleted = false;
	private boolean mCancelled = false;
	
	String strLine;
	DataInputStream inputStream = null;
	BufferedReader bufferedReader = null;
	FTPClient client = null;
	FTPFile[] ftpFiles = null;
	int reply;
	
	public MyFtpTask (WirelessInfo context, String filename) {
			mActivity = context;
			mFilename = filename;
			Log.d("Demo", "MyFtpTask: Done set vars in constructor");
	}
	
	@Override 
    protected void onCancelled () {
        mCompleted = true;
        mCancelled = true;
        Integer[] mMovesCompleted;
        mMovesCompleted = new Integer[1];
        mMovesCompleted[0] = 1;
        if (mActivity != null) mActivity.onFtpTaskCompleted (this, mMovesCompleted, mCancelled);
        disconnect ();
    }
	
	@Override
	protected Integer[] doInBackground(String... arg0) {
		boolean done = false;
		int i = 0;
		Integer[] doneX;
		doneX = new Integer[3];
		doneX[0] = 0;
		Log.d("Demo", "MyFtpTask.doInBackground start, var = "+arg0[0]);
		String ftpFileName = arg0[0];
		String hostName, id, passwd;
		int port = 21;
		
		if (ftpFileName.equals("test.txt")) {
			hostName = "inhere.homelinux.net";
			port = 1998;
			id = "test-ftp";
			passwd = "TestFtp123";
		}
		else {
			hostName = "118.148.1.20";
			id = "drivetest";
			passwd = "THRuu4ut";
		}
			
		//publishProgress( i );
		
		while (!done) {
 	       if (isCancelled ()) {
 	          done = true;
 	       } else {
 	    	  FTPClient con = new FTPClient();
 	 		try
 	 		{
 	 		    //con.connect("inhere.homelinux.net", 1998);
 	 		    //if (con.login("test-ftp", "TestFtp123"))
 	 			con.connect(hostName, port);
 	 		    if (con.login(id, passwd))
 	 		    {
 	 		    	con.enterRemotePassiveMode();
 	 		    	con.enterLocalActiveMode(); // important!
 	 		        con.setFileType(FTP.BINARY_FILE_TYPE);
 	 		        Log.d ("Demo", "MyFtpTask.doInBackground Connect to FTP server successful");
 	 		        //FTPFile[] remoteFiles = con.listFiles("test.txt");
 	 		        FTPFile[] remoteFiles = null;
 	 		        try {
 	 		        	remoteFiles = con.listFiles(ftpFileName);
 	 		        } catch (IOException f) {
 	 		        	Log.d ("Demo", "MyFtpTask.doInBackground IOException: "+f.getMessage());
 	 		        	con.logout();
	 			    	con.disconnect();
	 			    	done = true;
 	 	 			}
 	 		        Log.d ("Demo", "MyFtpTask.doInBackground File name = "+remoteFiles[0].getName( ));
 	 		        long length = remoteFiles[0].getSize( );
			        String readableLength = FileUtils.byteCountToDisplaySize( length );
			        Log.d ("Demo", "MyFtpTask.doInBackground File size = "+readableLength);
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
			        
			        long rxSByteSample = TrafficStats.getTotalRxBytes();
			    	long txSByteSample = TrafficStats.getTotalTxBytes();
			    	long start=System.currentTimeMillis();
			    	
			    	InputStream myFileStream = null;
			    	try {
			    		myFileStream = con.retrieveFileStream(ftpFileName);
 	 		        } catch (IOException f) {
 	 		        	Log.d ("Demo", "MyFtpTask.doInBackground IOException: "+f.getMessage());
 	 		        	con.logout();
	 			    	con.disconnect();
	 			    	done = true;
 	 	 			}
 	 			     
 	 			      inputStream = new DataInputStream(myFileStream);
 	 			      bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
 	 			      //save file from ftp to local directory
 	 			      File sdDir = Environment.getExternalStorageDirectory();
 	 			      
 	 			      File fileToWrite = new File(sdDir, "ftp-test/"+ftpFileName);
 	 			      Log.d ("Demo", "MyFtpTask.doInBackground saving file");
 	 			      java.io.FileOutputStream fos = new java.io.FileOutputStream(fileToWrite);
 	 			      java.io.BufferedOutputStream bout = new BufferedOutputStream(fos,1024);
 	 			      byte data[] = new byte[1024];
 	 			      int x = 0;
 	 			      
 	 			      while((x=myFileStream.read(data,0,1024))>=0){
 	 			          bout.write(data,0,x);
 	 			          doneX[0] += x;
 	 			          double perFromTotal = (((double) doneX[0] / (double) length)*100.00);
 	 			          publishProgress( (int) perFromTotal );
 	 			          //Log.d ("Demo", "MyFtpTask.doInBackground inside while FileStream x="+x+",done="+doneX[0]+", per="+perFromTotal);
 	 			      }
 	 			      
 	 			    long rxEByteSample = TrafficStats.getTotalRxBytes();
 		        	long txEByteSample = TrafficStats.getTotalTxBytes();
 		        	long rxDByteSample = rxEByteSample - rxSByteSample;
 		        	long txDByteSample = txEByteSample - txSByteSample;
 		        	double updateDelta = (double) (System.currentTimeMillis()- start)/1000.00;
 		        	double rxStatsKb = (double)(rxDByteSample*8)/1024.00;
 		        	
 		        	doneX[1] = (int) rxDByteSample;
 		        	doneX[2] = (int) updateDelta;
 		        	readableLength = FileUtils.byteCountToDisplaySize( (long) (rxDByteSample/updateDelta) );
 		        	Log.d ("Demo", "MyFtpTask.doInBackground Thrput = "+rxStatsKb/updateDelta+" Kb/s");
 		        	Log.d ("Demo", "MyFtpTask.doInBackground Thrput = "+readableLength+"/s");
 		        	//((TextView) findViewById(R.id.txtWebNetStats)).setText("Delta time = "+updateDelta+" s, Thrput = "+rxStatsMb/updateDelta+" Mb/s, Delta Rx/Tx Bytes = "+(rxDByteSample*8)/1024+" kb/"+txDByteSample+" Bytes");
 		        	
 	 			      bout.close();
 	 			      myFileStream.close();
 	 		        
 	 			     if(!con.completePendingCommand()) {
 	 			    	 con.logout();
 	 			    	 con.disconnect();
 	 			          //System.err.println("File transfer failed.");
 	 			          Log.d("Demo", "MyFtp: File transfer failed.");
 	 			      }
 	 		    }
 	 		} catch (Exception e) {
 	 			   if (con.isConnected()) {
 	 			    try {
 	 			    	con.logout();  
 	 			    	con.disconnect();  
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
		Log.d ("Demo", "MyFtpTask.doInBackground finished, var i = "+i);
		return doneX;
	}
	
	@Override 
    protected void onPostExecute (Integer moveCount[]) {
        mCompleted = true;
        if (mActivity != null) mActivity.onFtpTaskCompleted (this, moveCount, mCancelled);
        disconnect ();
    }
	
	@Override 
    protected void onProgressUpdate(Integer... info) {
        Integer intval = info[0];
        if (mActivity != null) mActivity.showFtpProgressOnScreen (intval);
    }
	
	public long getFileLenght() {
		Log.d("Demo", "MyFtp: inside");
		return FileLenght;
	}

	public void disconnect () {
        if (mActivity != null) {
           mActivity = null;
           Log.d ("Demo", "MyFtpTask has successfully disconnected from the activity.");
        }
    }
}
