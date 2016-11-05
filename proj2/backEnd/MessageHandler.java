package backEnd;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

import common.Data;
import common.Doctor;
import common.Entry;
import common.EntryJsonStruct;
import common.Message;
import common.Nurse;
import common.Patient;
import common.Person;
import common.ReturnMessage;
import common.SecurityLevel;

public class MessageHandler {

	Message message;
	Person executer;
	Database db;
	
	Logger logger;

	public MessageHandler(Message m, Database db, Logger logger) {
		this.message = m;
		this.db = db;
		this.logger = logger;
		if(this.logger == null){
			System.err.println("mh: Logger was null...");
		}
		
		if(this.message.token != null && !this.message.command.equals("signIn")){
			System.out.println("Will try to find person with that token");
			// Based on token, get person executing
			for (Person p : db.persons) {
				if (p.getToken() != null && m.token.equals(p.getToken())
						&& !m.token.isEmpty()) {
					this.executer = p;
					System.out.println("Found person with token");
					break;
				}
			}
			if (this.executer == null) {
				// Found no person with that token. Abort!
				System.err.println("Found no person with that token");
				return;
			}
		}
	}
	
	private void logData(String message){
		String caller = "unknown";
		if(this.executer != null)
			caller = this.executer.personalIdentifier;
		this.logger.info(caller+":\t"+message);
	}

	public ReturnMessage execute() {
		ReturnMessage back = null;
		this.logData("Got message "+message.command);
		switch (message.command) {
		case "addPatient":
			back = addPatient();
			break;
		case "addEntry":
			back = addEntry();
			break;
		case "changeEntry":
			back = changeEntry();
			break;
		case "deleteEntry":
			back = deleteEntry();
			break;
		case "deleteJournal":
			back = deleteJournal();
			break;
		case "getJournal":
			back = getJournal();
			break;
		case "signIn":
			back = signIn();
			break;
		case "signOut":
			back = signOut();
			break;
		default:
			break;
		}
		return back;
	}
	
	private ReturnMessage signOut(){
		this.executer.setToken(null);
		ReturnMessage back = new ReturnMessage();
		Data data = new Data();
		data.message = "Signed out";
		logger.info(this.executer.name+" signed out");
		back.data = data;
		return back;
	}
	
	private ReturnMessage signIn(){
		String password = message.data.password;
		String username = message.data.personalIdentifier;
		this.executer = null;
		
		for(Person p : db.persons){
			try {
				if(p.personalIdentifier.equals(username) && p.password.equals(Person.HashPassword(password, p.salt))){
					// Found person
					this.executer = p;
					break;
				}
			} catch (Exception e) {
				// TODO: handle exception
				continue;
			}
		}
		
		ReturnMessage back = new ReturnMessage();
		Data data = new Data();
		
		if(this.executer == null){
			// No user, wrong password/username
			data.message = "Wrong username or password";
			this.logData("Failed login attempt");
			back.data = data;
			return back;
		}
		
		data.message = "";
		// Generate hash
		if(executer.getToken() != null){
			// Return message person was already signed in
			data.message += "User was already signed in. Signing in you instead\n";
		}
		
		String token = Person.GenerateNewToken();
		this.executer.setToken(token);
		data.token = token;
		String type = "unknown";
		switch(this.executer.level){
		case Doctor:
			type = "doctor";
			break;
		case Patient:
			type = "patient";
			break;
		case Nurse:
			type = "nurse";
			break;
		case Government:
			type = "government";
			break;
		}
		data.message += "User "+this.executer.name+" signed in succesfully as "+type+"!";
		this.logData("User "+this.executer.name+" signed in succesfully as "+type+"!");

		back.data = data;
		return back;
	}
	
	private Patient getPatient(String id){
		Patient person = null;
		// Find person
		for (Person p : db.persons) {
			if (p.personalIdentifier.equals(id) && p.level == SecurityLevel.Patient) {
				person = (Patient) p;
				break;
			}
		}
		return person;
	}

	private List<Entry> getJournal(Person person) {
		List<Entry> entries = new ArrayList<Entry>();
		for (Entry e : db.entrys) {
			if (e.patient.personalIdentifier.equals(person.personalIdentifier))
				entries.add(e);
		}
		return entries;
	}

	private Data getDataFromEntries(List<Entry> entries) {
		List<EntryJsonStruct> list = new ArrayList<EntryJsonStruct>();
		Data data = new Data();
		for (Entry e : entries) {
			EntryJsonStruct tmp = new EntryJsonStruct();
			tmp.doctor = e.doctor.personalIdentifier;
			tmp.nurse = e.nurse.personalIdentifier;
			tmp.patient = e.patient.personalIdentifier;
			tmp.message = e.message;
			tmp.id = e.id;
			list.add(tmp);
		}
		
		EntryJsonStruct[] jsonEntries = new EntryJsonStruct[list.size()];
		for(int i = 0; i < list.size(); i++)
			jsonEntries[i] = list.get(i);
		
		data.entries = jsonEntries;
		return data;
	}

	public ReturnMessage getJournal() {
		ReturnMessage back = new ReturnMessage();
		Data data = new Data();

		if (executer.level == SecurityLevel.Patient) {
			Patient person = getPatient(message.data.personalIdentifier);
			if (person == null) {
				data.message = "Not a valid personal identifier";
				this.logData("Tried to get journal of non existing patient "+message.data.personalIdentifier);
				back.data = data;
				return back;
			} else if (person.personalIdentifier
					.equals(executer.personalIdentifier)) {
				// Patients can only acces their own journal
				// Return journal
				List<Entry> entries = getJournal(person);
				data = getDataFromEntries(entries);
				data.message = "Got your own journal for you!";
				this.logData("Got journal for "+person.name);
			} else {
				data.message = "Error - Patients can only acces their own journal";
				this.logData("Tried to get journal of another patient");
			}
		} else if (executer.level == SecurityLevel.Nurse
				|| executer.level == SecurityLevel.Doctor) {
			Patient person = getPatient(message.data.personalIdentifier);
			if (person == null) {
				data.message = "Not a valid id";
				this.logData("Tried to get journal for a non valid id "+message.data.personalIdentifier);
				back.data = data;
				return back;
			} else {
				List<Entry> fullEntryList = getJournal(person);
				List<Entry> nurseAllowedList = new ArrayList<Entry>();

				Person.HospitalDivistion division = null;
				if (executer.level == SecurityLevel.Nurse) {
					Nurse nurse = (Nurse) executer;
					division = nurse.hospitalDivision;
				} else {
					Doctor doctor = (Doctor) executer;
					division = doctor.hospitalDivision;
				}

				for (Entry e : fullEntryList) {
					if (e.nurse.personalIdentifier
							.equals(executer.personalIdentifier)
							|| e.hospitalDivision == division) {
						nurseAllowedList.add(e);
					}
				}
				data = getDataFromEntries(nurseAllowedList);
				data.message = "Got the entries for you";
				String listOfEntriesNames = "";
				for(Entry e : nurseAllowedList)
					listOfEntriesNames += e.patient.name+ " ";
				this.logData("Returned entries for "+listOfEntriesNames);
			}
		} else if (executer.level == SecurityLevel.Government) {
			Patient person = getPatient(message.data.personalIdentifier);
			List<Entry> entries = getJournal(person);
			data = getDataFromEntries(entries);
			data.message = "Got the journals for you mr government.";
			String listOfEntriesNames = "";
			for(Entry e : entries)
				listOfEntriesNames += e.patient.name+ " ";
			this.logData("Returned entries for "+listOfEntriesNames);
		}
		back.data = data;
		return back;
	}

	public ReturnMessage deleteJournal() {
		ReturnMessage back = new ReturnMessage();
		Data data = new Data();

		List<Entry> toRemove = new ArrayList<Entry>();
		if (executer.level == SecurityLevel.Government) {
			for (Entry e : db.entrys) {
				if (e.patient.personalIdentifier.equals(message.data.personalIdentifier))
					toRemove.add(e);
			}
			for (Entry e : toRemove) {
				db.entrys.remove(e);
			}
			data.message = "Removed journal";
			this.logData("Removed journal for "+message.data.personalIdentifier);
		} else {
			data.message = "You are not allowed to remove the journal";
			this.logData("Unauthorized attempt to remove journal");
		}

		back.data = data;
		return back;
	}

	public ReturnMessage deleteEntry() {
		ReturnMessage back = new ReturnMessage();
		Data data = new Data();

		if (executer.level == SecurityLevel.Government) {
			deleteEntry(message.data.id);
			data.message = "Succesfully removed entry";
			this.logData("Succesfully removed entry "+message.data.id);
		} else {
			data.message = "You are not allowed to delete entry";
			this.logData("Unauthorized attempt to remove entry "+message.data.id);
		}
		back.data = data;
		return back;
	}

	public void deleteEntry(int id) {
		Entry entry = null;
		for (Entry e : db.entrys) {
			if (e.id == message.data.id) {
				entry = e;
				break;
			}
		}
		db.entrys.remove(entry);
	}

	public ReturnMessage changeEntry() {
		ReturnMessage back = new ReturnMessage();
		Data data = new Data();

		Entry entry = null;
		// Find entry
		for (Entry e : db.entrys) {
			if (e.id == message.data.id) {
				entry = e;
				break;
			}
		}

		if (entry == null) {
			data.message = "No such entry";
			back.data = data;
			return back;
		}

		if ((this.executer.level == SecurityLevel.Doctor && this.executer.personalIdentifier == entry.doctor.personalIdentifier)
				|| (this.executer.level == SecurityLevel.Nurse && this.executer.personalIdentifier == entry.nurse.personalIdentifier)) {
			// Change entry
			entry.message = message.data.message;
			data.message = "Succesfully changed the entry";
			this.logData("Succesfully changed entry "+entry.id);
		} else {
			// Error, not allowed
			data.message = "Error, you are not allowed to change that entry";
			this.logData("Unauthorized attempt to change entry "+entry.id);
		}

		back.data = data;
		return back;
	}

	public ReturnMessage addEntry() {

		ReturnMessage back = new ReturnMessage();
		Data data = new Data();

		if (executer.level == SecurityLevel.Doctor) {
			// Find Patient
			Patient patient = null;
			for (Person p : db.persons) {
				if (p.personalIdentifier
						.equals(message.data.personalIdentifier)) {
					patient = (Patient) p;
				}
			}

			// Find nurse
			Nurse nurse = null;
			for (Person p : db.persons) {
				if (p.personalIdentifier.equals(message.data.nurseId)) {
					nurse = (Nurse) p;
				}
			}

			if (patient != null && nurse != null
					&& patient.level == SecurityLevel.Patient
					&& nurse.level == SecurityLevel.Nurse) {
				try {
					Entry e = new Entry(patient, nurse, (Doctor) executer,
							message.data.message, this.db.getNextId());
					db.entrys.add(e);
					// Return successful message
					data.message = "Successfuly added entry. New entry got id "+e.id;
					data.id = e.id;
					this.logData("Succesfully added entry "+data.id);
				} catch (Exception e) {
					// Error creating entry
					data.message = "Error creating entry. " + e.toString();
					this.logData("Error creating entry "+e.toString());
				}
			} else {
				// Non valid nurse/doctor
				data.message = "Nurse was not a nurse or doctor was not a doctor.";
				this.logData("Error creating entry, nurse was not a nurse, or doctor was not a doctor");
			}
		}else{
			data.message = "You are not allowed to add an entry.";
			this.logData("Unauthorized attempt to add entry for "+message.data.personalIdentifier);
		}
		back.data = data;
		return back;
	}
	
	public String randomPass(){
		// Generates a random password
		Random rand = new Random();
		StringBuilder builder = new StringBuilder();
		for(int i = 0; i < 8; i++){
			char tmp = (char) (rand.nextInt(89)+33);
			builder.append(tmp);
		}
		return builder.toString();
	}

	public ReturnMessage addPatient() {
		// Add a person to the database.
		// Only doctor can do this!

		// Check so no other patient has the same id
		boolean same = false;
		for(Person p : db.persons) {
			if(p.personalIdentifier.equals(message.data.personalIdentifier)){
				same = true;
				break;
			}
		}
		
		System.out.println("Add patient, same: "+same);
		ReturnMessage back = new ReturnMessage();
		Data data = new Data();

		if (executer.level == SecurityLevel.Doctor && !same) {
			
			// Random generate a password and send via mail to patient (mail not implemented)
			String password = randomPass();
					
			Patient p = new Patient(message.data.personalIdentifier,
					message.data.name, SecurityLevel.Patient, password);
			this.db.persons.add(p);
			data.message = "Patient created succesfully! The patients automatically generated password will be emaild to him/her.";
			this.logData("Created new patient "+p.name);
		} else {
			// Not allowed to to that or patient already exists!
			data.message = "You are not allowed to create a patient, or the patient already exists.";
			this.logData("Unauthorized attempt to create patient");
		}
		back.data = data;
		return back;
	}
}
