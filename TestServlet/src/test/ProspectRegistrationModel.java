package test;

public class ProspectRegistrationModel {
	private String addressLine1;
	private String addressLine2;
	private String addressLine3;
	private String addressLine4;
	private String countryCode;
	private String customerName;
	private char dualAdminInd;
	private char dualAuthInd;
	private Integer eaccountCount;
	private Integer iaccountCount;
	private String postCode;
	private char submitInd;
	private Integer trackingId;
	private Integer userDetailCount;
	private IncludedAccountModel[] includedAccounts;
	private ExcludedAccountModel[] excludedAccounts;
	private UserDetailModel[] userDetails;
	
	

	public IncludedAccountModel[] getIncludedAccounts() {
		return includedAccounts;
	}

	public void setIncludedAccounts(IncludedAccountModel[] includedAccounts) {
		this.includedAccounts = includedAccounts;
	}

	public ExcludedAccountModel[] getExcludedAccounts() {
		return excludedAccounts;
	}

	public void setExcludedAccounts(ExcludedAccountModel[] excludedAccounts) {
		this.excludedAccounts = excludedAccounts;
	}

	public void setAddressLine1(String addressLine1) {
		this.addressLine1 = addressLine1;
	}

	public String getAddressLine1(){
		return addressLine1;
	}
	
	public void setAddressLine2(String addressLine2){
		this.addressLine2=addressLine2;
	}
	
	public String getAddressLine2(){
		return addressLine2;
	}
	
	public void setAddressLine3(String addressLine3){
		this.addressLine3=addressLine3;
	}
	
	public String getAddressLine3(){
		return addressLine3;
	}
	
	public void setAddressLine4(String addressLine4){
		this.addressLine4=addressLine4;
	}
	
	public String getAddressLine4(){
		return addressLine4;
	}
	
	public void setCountryCode(String ucountryCode){
		this.countryCode=ucountryCode;
	}
	
	public String getCountryCode(){
		return countryCode;
	}
	
	public String getCustomerName(){
		return customerName;
	}
	
	public void setCustomerName(String customerName){
		this.customerName=customerName;
	}
	
	public char getDualAdminInd(){
		return dualAdminInd;
	}
	
	public void setDualAdminInd(char dualAdminInd){
		this.dualAdminInd=dualAdminInd;
	}
	
	public char getDualAuthInd(){
		return dualAuthInd;
	}
	
	public void setDualAuthInd(char dualAuthInd){
		this.dualAuthInd=dualAuthInd;
	}
	
	public void setEaccountCount(Integer eAccountCount){
		this.eaccountCount=eAccountCount;
	}
	
	public Integer getEaccountCount(){
		return eaccountCount;
	}
	
	public void setIaccountCount(Integer iaccountCount){
		this.iaccountCount=iaccountCount;
	}
	
	public Integer getIaccountCount(){
		return iaccountCount;
	}
	
	public void setPostCode(String postCode){
		this.postCode=postCode;
	}
	
	public String getPostCode(){
		return postCode;
	}
	
	public char getSubmitInd(){
		return submitInd;
	}
	
	public void setSubmitInd(char submitInd){
		this.submitInd=submitInd;
	}
	
	public Integer getTrackingId(){
		return trackingId;
	}
	
	public void setTrackingId(Integer trackingId){
		this.trackingId=trackingId;
	}
	
	
	public void setUserDetailCount(Integer userDetailCount){
		this.userDetailCount=userDetailCount;
	}
	
	public Integer getUserDetailCount(){
		return userDetailCount;
	}

	public UserDetailModel[] getUserDetails() {
		return userDetails;
	}

	public void setUserDetails(UserDetailModel[] userDetails) {
		this.userDetails = userDetails;
	}
}