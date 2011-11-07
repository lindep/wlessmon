package net.homelinux.inhere.wirelessinfo;

public class LoginDetails {
	private String mHostName;
	private int mPort;
	private String mId;
	private String mPasswd;
	
	LoginDetails() {
		mPort = 21;
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
		mPasswd = passwd;
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
}
