package common;

public class Entry {
	
	public Patient patient;
	public Nurse nurse;
	public Doctor doctor;
	public String message;
	public Person.HospitalDivistion hospitalDivision;
	public int id;
	
	public Entry(Patient patient, Nurse nurse, Doctor doctor, String message, int id) throws Exception{
		this.patient = patient;
		this.nurse = nurse;
		this.doctor = doctor;
		this.message = message;
		this.id = id;
		
		if(doctor.hospitalDivision != nurse.hospitalDivision){
			throw new Exception("Nurse and doctor is not in the same division");
		}
		this.hospitalDivision = doctor.hospitalDivision;
	}
	
	@Override
	public boolean equals(Object o){
		Entry e = (Entry) o;
		return e.id == this.id;
	}
}