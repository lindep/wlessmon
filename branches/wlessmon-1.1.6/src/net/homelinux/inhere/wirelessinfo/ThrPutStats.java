package net.homelinux.inhere.wirelessinfo;

public class ThrPutStats {
	private long mDeltaRxBytes;
	private double mDeltaTime;
	private long mFileSizeBytes;
	
	public ThrPutStats(long fileSizeBytes, double deltatime, long rxBytes) {
		mFileSizeBytes = fileSizeBytes;
		mDeltaRxBytes = rxBytes;
		mDeltaTime = deltatime;
	}
	
	long getFileSizeBytes() {
		return mFileSizeBytes;
	}
	
	double getTime() {
		return mDeltaTime;
	}
	long getRxBytes() {
		return mDeltaRxBytes;
	}
	
}