/**
 * CPSC 559 Winter 2021 Project Component ITERATION 1
 * 
 *  FILE: timpStamp.java
 *  DESCRIPTION: Responsible for time stamping of peer (IPAddress:Port) the moment Peer.java receives a list of peer(s)
 *  PLEASE REFER TO PROJECT REQUIREMENT ITERATION 1 for more information 
 *  
 * @author Jason Huang 
 * @UCID 10149037
 * 
 */
package registry;
public class TimeStamp {
	private String year;
	private String month;
	private String date;
	private String hour;
	private String minute;
	private String second;
	
	public TimeStamp(String year, String month, String date, String hour, String minute, String second) {
		this.year = year;
		this.month = month;
		this.date = date;
		this.hour = hour;
		this.minute = minute;
		this.second = second;
	}
	
	public String getYear() {
		return year;
	}
	public String getMonth() {
		return month;
	}
	public String getDate() {
		return date;
	}
	public String getHour() {
		return hour;
	}
	public String getMinute() {
		return minute;
	}
	public String getSecond() {
		return second;
	}
	public String print() {
		return year + "-" + month + "-" + date + " " + hour + ":" + minute + ":" + second;
	}
}
