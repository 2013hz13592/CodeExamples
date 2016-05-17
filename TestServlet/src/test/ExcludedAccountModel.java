package test;

public class ExcludedAccountModel {
	String accountKey;
	char domesticInternationalInd;
	char thirdPartyInd;
	
	public String getAccountKey(){
		return accountKey;
	}
	
	public void setAccountKey(String accountKey){
		this.accountKey=accountKey;
	}
	
	public char geDomesticInternationalInd(){
		return domesticInternationalInd;
	}
	
	public void setDomesticInternationalInd(char domesticInternationalInd){
		this.domesticInternationalInd=domesticInternationalInd;
	}
	
	public char getThirdPartyInd(){
		return thirdPartyInd;
	}
	
	public void setThirdPartyInd(char thirdPartyInd){
		this.thirdPartyInd=thirdPartyInd;
	}
}

