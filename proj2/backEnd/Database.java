package backEnd;

import java.util.ArrayList;
import java.util.List;

import common.Doctor;
import common.Entry;
import common.Government;
import common.Nurse;
import common.Patient;
import common.Person;
import common.SecurityLevel;

public class Database {
	
	List<Person> persons;
	List<Entry> entrys;
	
	int entryId;
	
	public Database(){
		this.persons = new ArrayList<Person>();
		this.entrys = new ArrayList<Entry>();
		this.entryId = 0;
	}
	
	public int getNextId(){
		int tmp = this.entryId;
		this.entryId ++;
		return tmp;
	}
	
	public void populate(){
		Patient a = new Patient("1234567890", "Kalle Anka", SecurityLevel.Patient, "pass1");
		Patient b = new Patient("6789012345", "Hunke Felding", SecurityLevel.Patient, "pass2");
		Patient c = new Patient("9012345678", "Joakim von Anka", SecurityLevel.Patient, "pass3");
		
		Nurse n1 = new Nurse("0000000001", "Goofy", SecurityLevel.Nurse, Person.HospitalDivistion.FIRST, "pass4");
		Nurse n2 = new Nurse("0000000002", "Mickey Mouse", SecurityLevel.Nurse, Person.HospitalDivistion.SECOND, "pass5");
		
		Doctor d1 = new Doctor("1337000001", "Huey", SecurityLevel.Doctor, Person.HospitalDivistion.FIRST, "pass4");
		Doctor d2 = new Doctor("1337000002", "Dewey", SecurityLevel.Doctor, Person.HospitalDivistion.SECOND, "pass5");
		
		Government g1 = new Government("0000000000", "Mr Obama", SecurityLevel.Government, "obama");;
		
		this.persons.add(a);
		this.persons.add(b);
		this.persons.add(c);
		this.persons.add(n1);
		this.persons.add(n2);
		this.persons.add(d1);
		this.persons.add(d2);
		this.persons.add(g1);
		
		try{
			Entry e1 = new Entry(a, n1, d1, "Did a brain transplant", this.getNextId());
			Entry e2 = new Entry(a, n1, d1, "Did another brain transplant", this.getNextId());
			Entry e3 = new Entry(b, n1, d1, "Did a head transplant.", this.getNextId());
			Entry e4 = new Entry(c, n2, d2, "Removed legs and arms from patient.", this.getNextId());
			
			entrys.add(e1);
			entrys.add(e2);
			entrys.add(e3);
			entrys.add(e3);
			
		}catch(Exception e){
			System.err.println("Got an exception when createing entries: "+e.toString());
		}
	}
}
