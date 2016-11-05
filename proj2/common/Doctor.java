package common;

public class Doctor extends Person {
	
	public HospitalDivistion hospitalDivision;
	
	public Doctor(String personalIdentifier, String name, SecurityLevel level, HospitalDivistion division, String password) {
		super(personalIdentifier, name, level, password);
		this.hospitalDivision = division;
	}

}
