package net.homelinux.inhere.wirelessinfo;

import android.os.Parcel;
import android.os.Parcelable;

public class LoginDetails  implements Parcelable {
	private String mHostName;
	private int mPort;
	private String mId;
	private String mPasswd;
	
	@Override
	public int describeContents() {
		return 0;
	}
	
	@Override
	public void writeToParcel(Parcel dest, int flag) {
		dest.writeString(mHostName);
		dest.writeInt(mPort);
		dest.writeString(mId);
		dest.writeString(mPasswd);
	}
	
	public LoginDetails() {
		mPort = 21;
	}
	
	public LoginDetails(String h,int p, String i, String pass) {
		mHostName = h;
		mPort = p;
		mId = i;
		mPasswd = pass;
	}
	
	public LoginDetails(Parcel in) {
		this.mHostName = in.readString();
		this.mPort = in.readInt();
		this.mId = in.readString();
		this.mPasswd = in.readString();
	}
	
	void setHost(String host) {
		mHostName = host;
	}
	void setPort(int port) {
		mPort = port;
	}
	void setId(String id) {
		mId = id;
	}
	void setPasswd(String passwd) {
		mPasswd = passwd.replace("\n", "");
	}
	
	String getHost() {
		return mHostName;
	}
	int getPort() {
		return mPort;
	}
	String getId() {
		return mId;
	}
	String getPasswd() {
		return mPasswd;
	}
	
	public static Parcelable.Creator<LoginDetails> CREATOR = new Parcelable.Creator<LoginDetails>() {
		public LoginDetails createFromParcel(Parcel in) {
			String h = in.readString();
			int p = in.readInt();
			String i = in.readString();
			String pass = in.readString();
			return new LoginDetails(h,p,i,pass);
		}
		
		public LoginDetails[] newArray(int size) {
			return new LoginDetails[size];
		}
	};
}
