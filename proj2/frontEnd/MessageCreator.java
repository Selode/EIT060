package frontEnd;

import java.io.BufferedReader;
import java.io.Console;
import java.io.IOException;

import common.Data;

public class MessageCreator {

	public MessageCreator() {
	}

	public Data loginRequest(BufferedReader read) throws IOException {
		System.out.println("Please enter username: \n >");
		String username = read.readLine();
		// System.out.println("Please enter password \n >");
		
		Console console = System.console();
	    if (console == null) {
	        System.out.println("Couldn't get Console instance");
	        return new Data();
	    }

	    char passwordArray[] = console.readPassword("Enter your password: ");
		// String password = read.readLine();
	    String password = String.valueOf(passwordArray);
		Data data = new Data();
		data.personalIdentifier = username;
		data.password = password;
		return data;

	}

	public Data newPatientRequest(BufferedReader read) throws IOException {
		System.out.println("Enter patient name: \n >");
		String patientName = read.readLine();
		System.out.println("Enter personal identifier for patient name: \n >");
		String personalIdentifier = read.readLine();
		Data data = new Data();
		data.personalIdentifier = personalIdentifier;
		data.name = patientName;
		return data;
	}

	public Data newEntry(BufferedReader read) throws IOException {
		System.out.println("Enter personal identifier for the patient: \n >");
		String personalIdentifier = read.readLine();
		System.out.println("What would you like the entry to say?: \n >");
		String message = read.readLine();
		System.out.println("Enter the ID of the nurse \n >");
		String nurseId = read.readLine();
		Data data = new Data();
		data.personalIdentifier = personalIdentifier;
		data.message = message;
		data.nurseId = nurseId;
		return data;
	}

	public Data getJournalRequest(BufferedReader read) throws IOException {
		System.out.println("Enter personal identifier for the patient \n >");
		String personalIdentifier = read.readLine();
		Data data = new Data();
		data.personalIdentifier = personalIdentifier;
		return data;
	}

	public Data deleteEntryRequest(BufferedReader read) throws IOException {
		System.out
				.println("Enter the ID for the entry you wish to delete \n >");
		int id = Integer.parseInt(read.readLine());
		Data data = new Data();
		data.id = id;
		return data;
	}

	public Data changeEntryRequest(BufferedReader read) throws IOException {
		System.out
				.println("Enter the ID for the entry you wish to change \n >");
		int id = Integer.parseInt(read.readLine());
		System.out.println("What would you like the message to say? \n >");
		String message = read.readLine();
		Data data = new Data();
		data.id = id;
		data.message = message;

		return data;
	}

	public Data deleteJournalRequest(BufferedReader read) throws IOException {
		System.out.println("Enter personal identifier for the patient \n >");
		String personalIdentifier = read.readLine();
		Data data = new Data();
		data.personalIdentifier=personalIdentifier;
		return data;
	}

	public Data signOut() {
		Data data = new Data();
		return data;
	}
}
