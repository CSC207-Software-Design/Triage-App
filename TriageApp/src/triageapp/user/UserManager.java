package triageapp.user;

import java.io.Serializable;
import java.util.Scanner;

import triageapp.database.TriageDBAdapter;


import android.database.Cursor;


/** Class that is responsible for verifying and granting access to a User. */
public class UserManager implements Serializable{
	
	private static final long serialVersionUID = 550588670334600153L;
	
	/** The file path to passwords.txt. */
	protected static final String PASSWORDS_PATH = "/files/passwords.txt. ";

	/** Constructs a UserManager object. */
	public UserManager(){
	}
	
	/**
	 * Gets either a Nurse instance or a Physician instance depending on the role of the User. 
	 * Fetches the data from the database.
	 * Returns null if the user does not exist in passwords.txt.
	 * @param username The User Name.
	 * @param password The password given by the user.
	 * @param dbAdapter The database adapter (helper).
	 * @return A User object that corresponds to the user's role.
	 */
	public User getUser(String username, String password, TriageDBAdapter dbAdapter){
		Cursor userCursor = dbAdapter.fetchUser(username, password);
		
		//Returns a User only if there is a username and password that corresponds to a user.
		if (userCursor.getCount() > 0){
			String role = userCursor.getString(0);
			if (role.equals("nurse")){
				return new Nurse(username);
			}
			if (role.equals("physician")){
				return new Physician(username);
			}
		} 
		return null;
	}
	
	/**
	 * Loads the username, the password and role attached to that user into the database from passwords.txt.
	 * This method is only used for the initial load.
	 * @param dbAdapter The database adapter (helper).
	 */
	public void loadUserInformation(TriageDBAdapter dbAdapter){
		try{
			Scanner scanner = new Scanner(getClass().getResourceAsStream(PASSWORDS_PATH));
            String nxt_line;
            String[] tokens;
            while (scanner.hasNextLine()){
                nxt_line = scanner.nextLine();
                tokens = nxt_line.split(",");
                //Replaces the \n and \r from the last character (certain JREs fail to skip over one or the other.
                dbAdapter.createUser(tokens[0], tokens[1], tokens[2].replace("\r","").replace("\n",""));
            }
            //closes the Cursor.
            scanner.close();
        }catch(NullPointerException e){
        }
	}
}