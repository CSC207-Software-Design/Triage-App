package triageapp.user;

import java.io.Serializable;


import triageapp.components.ERAdmin;
import triageapp.components.InvalidUserInputException;
import triageapp.components.Patient;
import triageapp.components.VitalSigns;
import triageapp.database.TriageDBAdapter;


/** Nurse is a User of the system. */
public class Nurse extends User implements Serializable {

	/** A unique ID for serialization. */
	private static final long serialVersionUID = -4042998941428479991L;

	/** Constructs a Nurse with a unique username. 
	 * @param username A unique username.
	 * */
	public Nurse(String username) {
		super(username);
	}
	
	/** 
	 * Signs in a Patient with their healthcard number.
	 * @param eradmin An instance of ERAdmin.
	 * @param healthNumber The health card number of the Patient to be signed in.
	 * @param mDbHelper The database adapter (helper).
	 */
	public void signInPatient(ERAdmin eradmin, String healthNumber, TriageDBAdapter mDbHelper){
		eradmin.signInPatient(healthNumber, mDbHelper);
	}
	
	/** 
	 * Create a new Patient using a name, date of birth, and healthcard number.
	 * @param eradmin An instance of ERAdmin.
	 * @param name The name of the new Patient.
	 * @param dob The date of birth of the new Patient.
	 * @param healthNumber The health card number of the new Patient.
	 * @param mDbHelper The database adapter (helper).
	 * @throws InvalidUserInputException 
	 */
	public void addPatient(ERAdmin eradmin, String name, String dob, String healthNumber, TriageDBAdapter mDbHelper) throws InvalidUserInputException{
		eradmin.addPatient(name, dob, healthNumber, mDbHelper);
	}
	
	/** 
	* Sends a Patient to a Doctor.
	* @param eradmin An instance of ERAdmin.
	* @param patient The Patient who has already been seen by a Physician.
	* @param mDbHelper The database adapter (helper).
	*/
	public void sendToDoctor(ERAdmin eradmin, Patient patient, TriageDBAdapter mDbHelper){
		eradmin.sendToDoctor(patient, mDbHelper);
	}
	
	/** 
	 * Adds a VitalSigns to a Patient
	 * @param eradmin An instance of ERAdmin.
	 * @param patient The Patient whom the VitalSign record belongs to. 
     * @param vitals The VitalSign record that is going to be added to the Patient's current ERVisit.
     * @param mDbHelper The database adapter (helper).
     */
	public void addPatientVitals(ERAdmin eradmin, Patient patient, VitalSigns vitals, TriageDBAdapter mDbHelper){
		eradmin.addPatientVitals(patient, vitals, mDbHelper);
	}
}
