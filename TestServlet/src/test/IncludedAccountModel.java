package test;

public class IncludedAccountModel {
	String accountInd;
	char domesticInternationalInd;
	char thirdPartyInd;
	char mainAccountInd;
	char billAccountInd;
	
	public String getAccountInd(){
		return accountInd;
	}
	
	public void setAccountInd(String accountInd){
		this.accountInd=accountInd;
	}
	
	public char getIdomesticInternationalInd(){
		return domesticInternationalInd;
	}
	
	public void setDomesticInternationalInd(char domesticInternationalInd){
		this.domesticInternationalInd=domesticInternationalInd;
	}
	
	public char getThirdPartyInd() {
		return thirdPartyInd;
	}

	public void setThirdPartyInd(char thirdPartyInd) {
		this.thirdPartyInd = thirdPartyInd;
	}

	public char getMainAccountInd(){
		return mainAccountInd;
	}
	
	public void setMainAccountInd(char mainAccountInd){
		this.mainAccountInd=mainAccountInd;
	}
	
	public char getIbillAccountInd(){
		return billAccountInd;
	}
	
	public void setBillAccountInd(char billAccountInd){
		this.billAccountInd=billAccountInd;
	}
}
