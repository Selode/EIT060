package common;

import com.google.gson.Gson;

/*
 * Class with static methods that serves as the intelligence between the client 
 * and the server. 
 */
public class ClientServerHandeler {

	public ClientServerHandeler() {

	}

	public static void main(String[] args) {

		/**
		 * Back End programming below
		 */

		/**
		 * Front End Programming below
		 */

		// This is only a test
		Patient patient = new Patient("9200001337", "Simon",
				SecurityLevel.Patient, "lol");
		Gson gson = new Gson();
		String s = gson.toJson(patient);
		System.out.println(s);
		//Test over
	}
}