package net.homelinux.inhere.wirelessinfo;


class CellStatsObject {
	//KEY_IMSI, KEY_TIMEENTER, KEY_CELLID, KEY_RSSI, KEY_LAT, KEY_LNG
	  public String imsi;
	  public String timeEnter;
	  public String cellid;
	  public String rssi;
	  public String lat;
	  public String lng;
	  public transient int value3 = 3;
	  CellStatsObject() {
	    // no-args constructor
	  }
}