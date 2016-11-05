package frontEnd;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.security.KeyStore;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import javax.security.cert.X509Certificate;
import com.google.gson.Gson;
import common.Entry;
import common.EntryJsonStruct;
import common.Message;
import common.ReturnMessage;

/*
 * This example shows how to set up a key manager to perform client
 * authentication.
 *
 * This program assumes that the client is not inside a firewall.
 * The application can be modified to connect to a server outside
 * the firewall by following SSLSocketClientWithTunneling.java.
 */
public class client {
	public static void main(String[] args) throws Exception {

		String host = null;
		int port = -1;

		try {
			host = args[0];
			port = Integer.parseInt(args[1]);
		} catch (IllegalArgumentException e) {
			System.out.println("USAGE: java client host port");
			System.exit(-1);
		}

		try { /* set up a key manager for client authentication */
			SSLSocketFactory factory = null;
			try {
				char[] password = "password".toCharArray();
				KeyStore ks = KeyStore.getInstance("JKS");
				KeyStore ts = KeyStore.getInstance("JKS");
				KeyManagerFactory kmf = KeyManagerFactory
						.getInstance("SunX509");
				TrustManagerFactory tmf = TrustManagerFactory
						.getInstance("SunX509");
				SSLContext ctx = SSLContext.getInstance("TLS");
				ks.load(new FileInputStream("clientkeystore"), password); // keystore
																			// password
																			// (storepass)
				ts.load(new FileInputStream("clienttruststore"), password); // truststore
																			// password
																			// (storepass);
				kmf.init(ks, password); // user password (keypass)
				tmf.init(ts); // keystore can be used as truststore here
				ctx.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
				factory = ctx.getSocketFactory();
			} catch (Exception e) {
				throw new IOException(e.getMessage());
			}
			SSLSocket socket = (SSLSocket) factory.createSocket(host, port);
			System.out.println("\nsocket before handshake:\n" + socket + "\n");

			/*
			 * send http request
			 * 
			 * See SSLSocketClient.java for more information about why there is
			 * a forced handshake here when using PrintWriters.
			 */
			socket.startHandshake();

			SSLSession session = socket.getSession();
			X509Certificate cert = (X509Certificate) session
					.getPeerCertificateChain()[0];
			String subject = cert.getSubjectDN().getName();
			System.out
					.println("certificate name (subject DN field) on certificate received from server:\n"
							+ subject + "\n");
			System.out.println("Issuer name: " + cert.getIssuerDN().getName());
			System.out.println("Serial number: " + cert.getSerialNumber());
			System.out.println("socket after handshake:\n" + socket + "\n");
			System.out.println("secure connection established\n\n");

			BufferedReader read = new BufferedReader(new InputStreamReader(
					System.in));
			PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
			BufferedReader in = new BufferedReader(new InputStreamReader(
					socket.getInputStream()));

			String msg;
			MessageCreator mc = new MessageCreator();
			String token = null;
			String commandList = "List of valid commands \n > login \n >"
					+ " add patient \n > add entry \n > get journal "
					+ "\n > delete entry \n > change entry \n > delete journal \n > log out \n >";
			for (;;) {
				System.out.print(">");
				msg = read.readLine();
				if (msg.equalsIgnoreCase("quit")) {
					break;
				}
				Message message = new Message();
				int command = 0;
				switch (msg.toLowerCase()) {
				case "login":
					message.data = mc.loginRequest(read);
					message.command = "signIn";
					command = 1;
					break;
				case "sign in":
					message.data = mc.loginRequest(read);
					message.command = "signIn";
					command = 1;
					break;
				case "add patient":
					message.data = mc.newPatientRequest(read);
					message.command = "addPatient";
					command = 2;
					break;
				case "add entry":
					message.data = mc.newEntry(read);
					message.command = "addEntry";
					command = 3;
					break;
				case "get journal":
					message.command = "getJournal";
					message.data = mc.getJournalRequest(read);
					command = 4;
					break;
				case "delete entry":
					message.command = "deleteEntry";
					message.data = mc.deleteEntryRequest(read);
					command = 5;
					break;
				case "change entry":
					message.command = "changeEntry";
					message.data = mc.changeEntryRequest(read);
					command = 6;
					break;
				case "delete journal":
					message.command = "deleteJournal";
					message.data = mc.deleteJournalRequest(read);
					command = 7;
					break;
				case "log out":
					message.command = "signOut";
					message.data = mc.signOut();
					command = 8;
					break;
				case "sign out":
					message.command = "signOut";
					message.data = mc.signOut();
					command = 8;
					break;
				case "help":
					System.out.println(commandList);
					continue;
				default:
					System.out.println("Not a valid command, type 'help' to see a list of commands");
					continue;
				}

				Gson gson = new Gson();
				message.token = token;
				String gsonMessage = gson.toJson(message);

				out.println(gsonMessage);
				out.flush();

				/**
				 * 
				 * Här kommer svar från servern i form av ett ReturnMessage
				 * 
				 */
				String inputData = in.readLine();
				ReturnMessage returnMsg = gson.fromJson(inputData,
						ReturnMessage.class);

				switch (command) {
				case 1:
					token = returnMsg.data.token;
					System.out.println(returnMsg.data.message);
					break;
				case 2:
					System.out.println(returnMsg.data.message);
					break;
				case 3:
					System.out.println(returnMsg.data.message);
					break;
				case 4:
					EntryJsonStruct[] entries = returnMsg.data.entries;
					System.out.println(returnMsg.data.message + "\n");
					for (EntryJsonStruct e : entries) {
						System.out.print("ID: " + e.id + "\t");
						System.out.println(e.message + "\n");
					}
					break;
				case 5:
					System.out.println(returnMsg.data.message);
					break;
				case 6:
					System.out.println(returnMsg.data.message);
					break;
				case 7:
					System.out.println(returnMsg.data.message);
					break;
				case 8:
					System.out.println(returnMsg.data.message);
					break;
				default:
					break;
				}

			}
			in.close();
			out.close();
			read.close();
			socket.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
