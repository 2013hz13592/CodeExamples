package test;

public class Status {
	private Integer trackingId;
	String emailAddress;
	
	public String getEmailAddress(){
		return emailAddress;
	}
	
	public Integer getTrackingId(){
		return trackingId;
	}
	
	public void setTrackingId(Integer trackingId){
		this.trackingId=trackingId;
	}
	
	public void setEmailAddress(String emailAddress){
		this.emailAddress=emailAddress;
	}
}
