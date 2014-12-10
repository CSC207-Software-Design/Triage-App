package triageapp.user;

import java.io.Serializable;


/** An abstract User of the system. */
public abstract class User implements Serializable {
	
	/** The boolean value representing the user type Nurse. */
	public static final boolean NURSE = true;
	
	/** The boolean value representing the user type Physician. */
	public static final boolean PHYSICIAN = false;

	/** A unique ID for serialization. */
	private static final long serialVersionUID = 8581398291113834419L;
	private String username;
	
	/** Constructs a User with a unique username.
	 * @param username A unique username.
	 */
	public User(String username){
		this.username = username;
	}
	
	/** 
	 * Gets username.
	 * @return the username of a User. 
	 */
	public String getUsername(){
		return username;
	}	
	
}
