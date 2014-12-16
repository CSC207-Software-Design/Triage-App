package triageapp.user;

import java.io.Serializable;


import triageapp.components.ERAdmin;
import triageapp.components.Patient;
import triageapp.components.Prescription;
import triageapp.database.TriageDBAdapter;


/** Physician is a User of the system. */
public class Physician extends User implements Serializable {

	/** A unique ID for serialization. */
	private static final long serialVersionUID = 2811241604730812477L;
	
	/** Constructs a Physician with a unique username.
	* @param username A unique username.
	*/
	public Physician(String username) {
		super(username);
	}
	
	/** 
	* Adds a Prescription for a Patient.
	* @param eradmin An instance of ERAdmin.
    * @param patient The Patient whom the Prescription belongs to.
    * @param prescription The Prescription object to be added.
    * @param mDbHelper The database adapter (helper).
    */
	public void addPatientPrescription(ERAdmin eradmin, Patient patient, Prescription prescription, TriageDBAdapter mDbHelper){
		eradmin.addPatientPrescription(patient, prescription, mDbHelper);
	}
}