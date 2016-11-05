package common;

public class Nurse extends Person {
	
	public HospitalDivistion hospitalDivision;
	
	public Nurse(String personalIdentifier, String name, SecurityLevel level, HospitalDivistion division, String password) {
		super(personalIdentifier, name, level, password);
		this.hospitalDivision = division;
	}

}
