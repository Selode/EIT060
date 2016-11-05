package common;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;
import java.util.Random;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public abstract class Person {
	public String personalIdentifier, name;
	private String token;
	public String password; // Hashed password
	public byte[] salt;
	public SecurityLevel level;
	
	public enum HospitalDivistion{
		FIRST,
		SECOND,
		THIRD,
		FOURTH
	};
	
	public Person(String personalIdentifier, String name, SecurityLevel level, String password) {
		this.personalIdentifier=personalIdentifier;
		this.name=name;
		this.level=level;
		this.salt = new byte[16];
		Random random =  new Random(); 
		random.nextBytes(this.salt);
		try {
			this.password = Person.HashPassword(password, this.salt);
		} catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
			System.err.println("Could not hash password :(");
		}
	}
	
	public void setToken(String token){
		this.token = token; 
	}
	
	public String getToken(){
		return this.token;
	}
	
	public static String GenerateNewToken(){
		// Generates a 256 characters long random sequence. 
		Random rand = new Random();
		StringBuilder builder = new StringBuilder();
		for(int i = 0; i < 256; i++){
			char tmp = (char) (rand.nextInt(89)+33);
			builder.append(tmp);
		}
		return builder.toString();
	}

	public static String HashPassword(String password, byte[] salt) throws NoSuchAlgorithmException, InvalidKeySpecException{
		KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 65536, 128);
		SecretKeyFactory f = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
		byte[] hash = f.generateSecret(spec).getEncoded();
		Base64.Encoder enc = Base64.getEncoder();
		return enc.encodeToString(hash);
	}

}