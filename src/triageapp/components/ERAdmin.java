package triageapp.components;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import triageapp.database.TriageDBAdapter;

import android.database.Cursor;


/**
 * The class that handles all high level operations in TriageApp. 
 * Handles: 
 * 		adding Patient
 * 		adding Prescription
 * 		adding Vital Signs
 * 		sending Patient to doctor
 * 		looking up a Patient
 *		signing in Patient
 *		closing Visit Record
 *		loading lists     
 */
public class ERAdmin implements Serializable{

	/** A unique ID for serialization. */
	private static final long serialVersionUID = -8228083728561572751L;
	
	/** A list of all Patients currently signed in, ordered by urgency. */
	protected List<Patient> urgencyList = new ArrayList<Patient>();
	/** A HashMap that maps health card numbers to Patients. All Patients (past, present) are included. */
	protected Map<String, Patient> PatientList = new HashMap<String, Patient>();
	/** A list of Patients already sent to be evaluated by a physician. */
	protected List<Patient> patientsSentToDoctor = new ArrayList<Patient>();
	
	/** File path for the file that contains all Patients' basic information. */
	protected static final String PATIENT_RECORDS_PATH = "/files/patient_records.txt";
		
	/**
	 * Adds a new VitalSigns to the patients's current ERVisit and update the urgencyList and the database accordingly.
	 * @param patient The Patient whose VitalSigns is being added.
	 * @param vitals The VitalSigns being added to the Patient's current ERVisit.
	 * @param dbAdapter The database adapter (helper).
	 */
    public void addPatientVitals(Patient patient, VitalSigns vitals, TriageDBAdapter dbAdapter){
    	patient.getCurrentERVisit().addVitalsSignRecord(vitals);
        patient.setUrgency(); 
        
        //Update the database with the new VitalSigns.
    	dbAdapter.createVitalSigns(patient.getCurrentERVisit().getID(), vitals.getSystolic(), vitals.getDiastolic(), 
    			vitals.getTemperature(), vitals.getHeartRate(), vitals.getTimestamp(), patient.getUrgency());
    	
    	//Reload the urgencyList from the database.
    	this.loadUrgencyList(dbAdapter);
    }
    
    /**
     * Adds a Prescription to a Patient's current ERVisit.
     * @param patient The Patient whose Prescription is being added.
     * @param prescription The Prescription being added to the Patient's current ERVisit.
     * @param dbAdapter The database adapter (helper).
     */
    public void addPatientPrescription(Patient patient, Prescription prescription, TriageDBAdapter dbAdapter){
    	patient.getCurrentERVisit().addPrescriptionRecord(prescription);
    	
    	//Update the database with the new Prescription.
    	dbAdapter.createPrescription(patient.getCurrentERVisit().getID(), prescription.getMedicationName(), prescription.getInstructions());
    }
    
    /**
     * Send a Patient to the doctor. Sets the seenByDoctor variable in the Patient's current ERVisit to true.
     * @param patient The Patient being sent to a Physician.
     * @param dbAdapter The database adapter (helper).
     */
    public void sendToDoctor(Patient patient, TriageDBAdapter dbAdapter){
    	patient.getCurrentERVisit().setSeenByDoctor();
    	this.patientsSentToDoctor.add(patient);
    	this.urgencyList.remove(patient);
    	
    	//Update the Patient's current ERVisit in the database with the time seen by doctor.
        dbAdapter.updateERVisit(patient.getCurrentERVisit().getID(), patient.getCurrentERVisit().getTimeSeenByDoctor());
    }
    
    /**
     * Closes a Patient's current ERVisit.
	 * Removes references of the patient in urgencyList and patientsSentToDoctor.
	 * Updates the ERVisit entry in the database. 
     * @param patient The Patient whose ERVisit is being closed.
     * @param dbAdapter The database adapter (helper).
     */
	public void closeCase(Patient patient, TriageDBAdapter dbAdapter){
		//Sets the closed parameter for the Patient's current ERVisit to 1. 
		dbAdapter.updateERVisit(patient.getCurrentERVisit().getID(), true);
		
		patient.getCurrentERVisit().setClosed();
		patient.setCurrentERVisit(null);
		
		//Sets the urgency level to 0.
		patient.setUrgency();
		this.urgencyList.remove(patient);
		this.patientsSentToDoctor.remove(patient);
	}
	
	/**
	 * Signs in an existing Patient. Creates a new ERVisit for the Patient and add a corresponding entry in the database.
	 * @param healthNumber The Patient's health card number.
	 * @param dbAdapter The database adapter (helper).
	 * @return The Patient being signed in.
	 */
	public Patient signInPatient(String healthNumber, TriageDBAdapter dbAdapter){
		Patient patient = this.lookUpPatient(healthNumber);
		patient.addNewERVisit();
		this.urgencyList.add(patient);
		
		//Create an entry for a new ERVisit int the database.
		long ERVisitID = dbAdapter.createERVisit(patient.getCurrentERVisit().getArrivalTime(), healthNumber, patient.getUrgency());
		//Sets the ERVisitID in for the ERVisit. Used to navigate in the database.
		patient.getCurrentERVisit().setID(ERVisitID);
		return patient;
	}
	
	/**
	 * Adds a new Patient to the database and PatientList. Also signs the Patient in.
	 * @param name The Patient's name.
	 * @param dob The Patient's date of birth.
	 * @param healthNumber The Patient's health card number.
	 * @param dbAdapter The database adapter (helper).
	 * @throws InvalidUserInputException 
	 */
	public void addPatient(String name, String dob, String healthNumber, TriageDBAdapter dbAdapter) throws InvalidUserInputException{
		//Checks if the health card number is unique
		if (lookUpPatient(healthNumber) != null)
			throw new InvalidUserInputException();
		Patient patient = new Patient(name, dob, healthNumber);
		this.PatientList.put(healthNumber, patient);	
		//Create an entry in the database for the new Patient.
		dbAdapter.createPatient(healthNumber, name, dob);
		
		//Sign in the Patient.
		this.signInPatient(healthNumber, dbAdapter);
	}
	
	/**
	 * Returns the Patient with the given health card number.
	 * @param healthNumber The health card number that corresponds to a patient in PatientList.
	 * @return The patient with the given health card number.
	 */
	public Patient lookUpPatient(String healthNumber){
		return this.PatientList.get(healthNumber);
	}
	
	/**
	 * Gets the formatted String to display the Medical Record for the Patient.
	 * @param patient The Patient whose Medical Record is being displayed.
	 * @param dbAdapter The database adapter (helper).
	 * @return The formatted String representing the Patient's Medical Record.
	 */
	public String getPatientMedicalRecord(Patient patient, TriageDBAdapter dbAdapter){
		//Gets a formatted String representing the Patient's medical record.
		Cursor medicalRecordCursor = dbAdapter.fetchMedicalRecordForThisPatient(patient.getHealthCardNumber());
		//The String is at row 0, column 0.
		String medRecText = medicalRecordCursor.getString(0);
		//Closes the Cursor.
		medicalRecordCursor.close();
		return medRecText;
	}
	
	/**
	 * Gets urgencyList.
	 * @return The urgencyList.
	 */
	public List<Patient> getUrgencyList(){
		return this.urgencyList;
	}
	
	/**
	 * Gets patientsSentToDoctor.
	 * @return The list of Patients already sent to a Physician.
	 */
	public List<Patient> getPatientsSentToDoctorList(){
		return this.patientsSentToDoctor;
	}
	
	/**
	 * Initial load of patient_records.txt. Populates PatientList and writes the information to the database.
	 * @param dbAdapter The database adapter (helper).
	 * @throws InvalidUserInputException
	 */
    public void initialLoadPatients(TriageDBAdapter dbAdapter) throws InvalidUserInputException{
    	try{
    		Scanner scanner = new Scanner(getClass().getResourceAsStream(PATIENT_RECORDS_PATH));
    		String nxt_line;
    		String[] tokens;
    		while (scanner.hasNextLine()){
    			nxt_line = scanner.nextLine();
    			tokens = nxt_line.split(",");
    			//Replaces the \n and \r from the last character (certain JREs fail to skip over one or the other.
    			this.PatientList.put(tokens[0], new Patient(tokens[1], tokens[2].replace("\r","").replace("\n",""), tokens[0]));
    			//Populates the database with the patient information.
    			dbAdapter.createPatient(tokens[0], tokens[1], tokens[2].replace("\r","").replace("\n",""));
    		}
    		//Closes the Cursor.
    		scanner.close();
    	}catch(NullPointerException e){
    		e.printStackTrace();
    	}
    }
	
    /**
     * Loads Patient information from the database and populates PatientList.
     * @param dbAdapter The database adapter (helper).
     * @throws InvalidUserInputException
     */
	public void loadPatients(TriageDBAdapter dbAdapter) throws InvalidUserInputException{
		//Gets all the Patient information in a table from the database.
		Cursor patientsCursor = dbAdapter.fetchAllPatients();
		for (boolean hasItem = patientsCursor.moveToFirst(); hasItem; hasItem = patientsCursor.moveToNext()) {
			Patient patient = new Patient(patientsCursor.getString(1), patientsCursor.getString(2), patientsCursor.getString(0));
			//Repopulates PatientList.
    		this.PatientList.put(patientsCursor.getString(0), patient);
    	} 
		//Closes the Cursor.
		patientsCursor.close();
		//Load the current ERVisit (and the VitalSigns and Prescription for the ERVisit) for each Patient.
		this.loadCurrentERVisit(dbAdapter);
	}
	
	/**
	 * Loads all Patients' current ERVisit from the database.
	 * @param dbAdapter The database adapter (helper).
	 * @throws InvalidUserInputException
	 */
	public void loadCurrentERVisit(TriageDBAdapter dbAdapter) throws InvalidUserInputException{
		for (Patient patient: this.PatientList.values()){
			//Gets the current ERVisit for each Patient.
			Cursor ERVisitCursor = dbAdapter.fetchCurrentERVisitForThisPatient(patient.getHealthCardNumber());
			//If the Patient has no current ERVisit, do nothing.
			if (ERVisitCursor.getCount() > 0){
				//The ERVisit is not closed.
				ERVisit currentERVisit = new ERVisit(ERVisitCursor.getLong(0), ERVisitCursor.getLong(1), false, ERVisitCursor.getLong(2) == 0 ? null : ERVisitCursor.getLong(2));
				
				//Gets the Prescriptions for this ERVisit.
				Cursor prescriptionCursor = dbAdapter.fetchPrescriptionsForThisVisit(currentERVisit.getID());
				for (boolean hasItem = prescriptionCursor.moveToFirst(); hasItem; hasItem = prescriptionCursor.moveToNext()) {
					//Adds the Prescription to the ERVisit.
					currentERVisit.addPrescriptionRecord(new Prescription(prescriptionCursor.getString(0), prescriptionCursor.getString(1)));
		    	}
				//Closes the Cursor.
				prescriptionCursor.close();
				
				//Gets the VitalSigns for this ERVisit.
				Cursor vitalSignsCursor = dbAdapter.fetchVitalSignsForThisVisit(currentERVisit.getID());
				for (boolean hasItem = vitalSignsCursor.moveToFirst(); hasItem; hasItem = vitalSignsCursor.moveToNext()) {
					//Adds the VitalSigns to the ERVisit.
					currentERVisit.addVitalsSignRecord(new VitalSigns(vitalSignsCursor.getInt(0), vitalSignsCursor.getInt(1), 
							vitalSignsCursor.getFloat(2), vitalSignsCursor.getFloat(3), vitalSignsCursor.getLong(4)));
		    	}
				//Closes the Cursor.
				vitalSignsCursor.close();
				
				//Sets the Patient's urgency level.
				patient.setUrgency(ERVisitCursor.getInt(3));
				patient.setCurrentERVisit(currentERVisit);
			}
			//Closes the Cursor.
			ERVisitCursor.close();
		}
	}
	
	/**
	 * Loads the list of Patients ordered by urgency from the database and populates urgencyList.
	 * @param dbAdapter The database adapter (helper).
	 */
	public void loadUrgencyList(TriageDBAdapter dbAdapter){
		//Gets a list of Patients sorted by urgency.
		Cursor urgencyListCursor = dbAdapter.fetchPatientsByUrgency();
		//Removes all entries from urgencyList.
    	this.urgencyList.clear();
    	
    	for (boolean hasItem = urgencyListCursor.moveToFirst(); hasItem; hasItem = urgencyListCursor.moveToNext()) {
    		//Repopulates urgencyList.
    		this.urgencyList.add(this.lookUpPatient(urgencyListCursor.getString(0)));  
    	}
    	//Closes the Cursor.
    	urgencyListCursor.close();
	}
	
	/**
	 * Loads the list of Patients sent to the doctor from the database and populates patientsSentToDoctor.
	 * @param dbAdapter The database adapter (helper).
	 */
	public void loadSentToDoctorList(TriageDBAdapter dbAdapter){
		//Gets a list of Patients sent to the doctor.
		Cursor sentToDoctorCursor = dbAdapter.fetchPatientsSentToDoctor();
		//Removes all entries from patientsSentToDoctor.
		this.patientsSentToDoctor.clear();
		for (boolean hasItem = sentToDoctorCursor.moveToFirst(); hasItem; hasItem = sentToDoctorCursor.moveToNext()) {
			//Repopulates patientsSentToDoctor.
    		this.patientsSentToDoctor.add(this.lookUpPatient(sentToDoctorCursor.getString(0)));  
    	}
		//Closes the Cursor.
		sentToDoctorCursor.close();
	}
}