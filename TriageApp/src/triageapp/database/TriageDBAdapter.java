package triageapp.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;


/**
 * Triage database access helper class. Extends DBAdapter to inherit
 * the ability to open/create/close the database. Defines the basic CRUD operations
 * for the Triage+ app. Gives the ability to list all information for a specific 
 * Patient or ERVisit, create new rows in the tables corresponding to a new
 * ERVisit, VitalSigns, Prescription, or Patient. Also allows retrieval and 
 * modification of a specific ERVisit. Uses Cursors.
 */
public class TriageDBAdapter extends DBAdapter{
	
	//Columns names for each table in the DB.
	
	/** Column name for the primary key row ID universal to all Tables. */
	public static final String KEY_ROWID = "_ID";
	
	// Column names for the Patient table.
	/** Patient table name. */
	public static final String DATABASE_TABLE_PATIENT = "Patient";
	/** Column containing Patient health card numbers stored as TEXT. */
	public static final String KEY_HEALTHCARD =  "healthCardNumber"; 
	/** Column containing Patient names. */
	public static final String KEY_NAME = "name";
	/** Column containing Patient birth dates stored as TEXT. */
	public static final String KEY_DOB = "dob";
    
	// Column names for the Role table.
	/** Role table name. */
    public static final String DATABASE_TABLE_ROLE = "Role";
    /** Column containing possible user roles stored as TEXT strings. */
    public static final String KEY_ROLENAME =  "roleName"; 
    
    // Column names for the ERVisit table.
    /** ERVisit table name. */
    public static final String DATABASE_TABLE_ERVISIT = "ERVisit";
    /** Column containing foreign key Patient Row IDs corresponding to the 
     * patient in the Patient Table that the ERVisit references. */
	public static final String KEY_PATIENTID = "PatientID";
	/** Column containing ER arrival times stored as INTEGER (long). */
	public static final String KEY_ARRIVALTIME = "arrivalTime";
	/** Column containing time seen by the doctor stored as INTEGER (long). 
	 * Can be null, representing the fact that a Patient has not been seen by
	 * Doctor during this ERVisit. */
    public static final String KEY_TIMESEENBYDOCTOR = "timeSeenByDoctor";
    /** Column containing INTEGER flag (0 or 1) indicating whether the Visit is closed
     * (whether the Patient has been dismissed from the ER). For all ERVisits referencing
     * the same Patient, only one may be open (isClosed = 0, or False). */
    public static final String KEY_CLOSED = "isClosed";
    /** Column containing the urgency value of this visit. */
    public static final String KEY_URGENCY = "urgency";
    
    // Column names for the VitalSigns table
    /** VitalSigns table name. */
    public static final String DATABASE_TABLE_VITALS = "VitalSigns";
    /** Column containing systolic blood pressure values stored as INTEGER. */
	public static final String KEY_SYSTOLIC = "systolic";
	/** Column containing diastolic blood pressure values stored as INTEGER. */
    public static final String KEY_DIASTOLIC = "diastolic";
    /** Column containing temperature values stored as REAL (double/float). */
    public static final String KEY_TEMPERATURE = "temperature";
    /** Column containing heart rate values stored as REAL (double/float). */
    public static final String KEY_HEARTRATE = "heartRate";
    /** Column containing timestamp values of the set of VitalSigns stored as INTEGER (long). */
    public static final String KEY_TIMESTAMP = "timestamp";
    
    /**
     * Foreign key pointing to the rowID of the ERVisit.
     * Column name equivalent in both VitalSigns and Prescription tables.
     */
    public static final String KEY_ERVISITID = "ERVisitID";
    
    // Column names for the Prescription table.
    /** Prescription table name. */
    public static final String DATABASE_TABLE_PRESCRIPTION = "Prescription";
    /** Column containing Prescription medication names stored as TEXT. */
    public static final String KEY_MEDICATION = "medication";
    /** Column containing Prescription instructions stored as TEXT. */
	public static final String KEY_INSTRUCTIONS = "instructions";
    
	// Column names for the User table.
	/** User table name. */
	public static final String DATABASE_TABLE_USER = "User";
	/** Column containing Usernames stored as TEXT. */
    public static final String KEY_USERNAME = "username";
    /** Column containing Passwords stored as TEXT. */
	public static final String KEY_PASSWORD = "password";
	/** Column containing foreign keys pointing to the row ID of the User's role
	 * name in the Role table. */
    public static final String KEY_ROLEID = "RoleID";
    
    /** 
     * SQL query to fetch a Patient's current ERVisit. The query has one parameter,
     * the health card number string of the Patient.
     **/
    private static final String FETCH_CURRENT_ERVISIT_FOR_THIS_PATIENT = 
	"SELECT ERVisit._ID, arrivalTime, timeSeenByDoctor, urgency " +
	"FROM ERVisit " +
	"JOIN Patient ON Patient._ID=ERVisit.PatientID " +
	"WHERE Patient.healthCardNumber= ? AND  ERVisit.isClosed=0";

    /**
     * SQL query to fetch all Patients currently in the ER who 
     * HAVE NOT yet been seen by the doctor in order of 
     * urgency.
     */
    private static final String FETCH_PATIENTS_BY_URGENCY = 
	"SELECT healthCardNumber " +
	"FROM ERVisit " +
	"JOIN Patient ON Patient._ID=ERVisit.PatientID " +
	"WHERE isClosed=0 AND timeSeenByDoctor IS NULL " + 
	"ORDER BY urgency DESC, arrivalTime ASC";
    
    /**
     * SQL query to fetch all Patients currently in the ER who 
     * ARE being seen by the doctor.
     */
    private static final String FETCH_PATIENTS_SENT_TO_DOCTOR =
    "SELECT healthCardNumber " +
    "FROM ERVisit " +
    "JOIN Patient ON Patient._ID=ERVisit.PatientID " +
	"WHERE isClosed=0 AND timeSeenByDoctor IS NOT NULL";

    /**
     * SQL query to fetch a User's role name if the user
     * exists. The query has two parameters: the User's
     * username and password.
     */
    private static final String FETCH_USER = 
    "SELECT Role.roleName " +
    "FROM User " + 
    "JOIN Role ON RoleID= Role._ID " +
    "WHERE User.username = ? AND User.password= ?";
    
    /**
     * SQL query to fetch a formatted string containing all of the
     * Patient's medical history. The query has one parameter, 
     * the patient's health card number.
     */
    public static final String FETCH_MEDICAL_RECORD = 
    		"SELECT group_concat(Medicalrecord ,' ') AS PatientMedicalRecord FROM ( " +
    				 "select " +   
    				  "'+++++++++++++++++++++ \n Record \n' || '+++++++++++++++++++++ \n' || " +
    					"'Arrival Date: ' || strftime('%m/%d/%Y ', datetime(arrivalTime/1000, 'unixepoch', 'localtime')) || '\n' || " +
    					"'Arrival Time: ' || strftime('%H:%M', datetime(arrivalTime/1000, 'unixepoch', 'localtime')) || '\n' || " +
    				        "(CASE  WHEN timeSeenByDoctor IS NULL THEN '' " +
    				               "ELSE  'Seen by Doctor Date: ' ||  strftime('%m/%d/%Y ', datetime(timeSeenByDoctor/1000, 'unixepoch', 'localtime')) || '\n' || " +
    						"'Seen by Doctor Time: ' || strftime('%H:%M', datetime(timeSeenByDoctor/1000, 'unixepoch', 'localtime')) || '\n' " +
    				         "END)   || CASE  WHEN AllVisitVitals IS NULL THEN '' ELSE AllVisitVitals END " +
    				          "|| CASE  WHEN AllPrescriptions IS NULL THEN '' ELSE AllPrescriptions END " +
    				          "as Medicalrecord " +
    				 "FROM ERVisit " + 
    				 "JOIN Patient ON ERVisit.PatientID=Patient._ID " +
    				 "LEFT JOIN (SELECT ERVisitID, group_concat(VisitVitalSign ,'') AS AllVisitVitals FROM ( " +
    				          "SELECT ERVisitID, " +
    				          "'------------------'  ||  '\n'  || " + 
    					 "'Vital Signs:'  ||  '\n'  ||  " +
    					 "'Date: ' || strftime('%m/%d/%Y ', datetime(timestamp/1000, 'unixepoch', 'localtime')) || '\n' || " +
    					 "'Time: ' || strftime('%H:%M', datetime(timestamp/1000, 'unixepoch', 'localtime')) || '\n' || " +
    					 "'Systolic: ' || " +
    					 "CAST(systolic AS TEXT)  || '\n' || " +
    					 "'Diastolic: ' || " +
    					 "CAST(diastolic AS TEXT)  || '\n' || " +
    					 "'Temperature: ' || " +
    					 "CAST(temperature AS TEXT)  || '\n' || " +
    					 "'Heart Rate: ' || " +
    					 "CAST(heartRate AS TEXT) || '\n' " +  
    				          "AS VisitVitalSign " +
    				         "FROM  VitalSigns ORDER BY timestamp DESC " + 
    				        ") GROUP BY ERVisitID) V  ON V. ERVisitID =ERVisit._ID " +
    				  "LEFT JOIN ( " +
    				  "SELECT ERVisitID, group_concat(Prescriptions ,' ') as AllPrescriptions FROM ( " +  
    				        "SELECT ERVisitID,  '------------------' ||  '\n' || " + 
    					"'Prescription:' ||  '\n' || " + 
    					"'Name: ' || " + 
    					"medication ||  '\n' || " + 
    					"'Instructions: ' || " +
    					"instructions ||  '\n' " +
    				        "AS Prescriptions " +
    				         "FROM  Prescription " +
    				        ") GROUP BY ERVisitID " +
    				  ") P  ON P. ERVisitID =ERVisit._ID " +
    				  "WHERE IsClosed =1 AND Patient.healthCardNumber= ? ORDER BY arrivalTime DESC)";
    
	/**
     * Constructs a TriageDBAdapter object and takes the context 
     * to allow the database to be opened/created.
     * @param ctx The Context within which to work.
     */
	public TriageDBAdapter(Context ctx) {
		super(ctx);
	}
	
	/**
     * Return a Cursor over the list of all Patients in the database
     * @return Cursor over all Patients.
     */
    public Cursor fetchAllPatients() {
        return mDb.query(DATABASE_TABLE_PATIENT, new String[] {KEY_HEALTHCARD,
        		KEY_NAME, KEY_DOB}, null, null, null, null, null);

    }
    
	/**
     * Return a Cursor over the ERVisit for this Patient that is NOT
     * Closed (current) if existing.
     * @return Cursor over current ERVisit
     */
    public Cursor fetchCurrentERVisitForThisPatient(String patientHealthCard) {
    	//KEY_ROWID, KEY_ARRIVALTIME, KEY_TIMESEENBYDOCTOR, KEY_URGENCY
    	String[] args = {patientHealthCard};
		Cursor mCursor = mDb.rawQuery(FETCH_CURRENT_ERVISIT_FOR_THIS_PATIENT, args);
		if (mCursor != null) {
			mCursor.moveToFirst();
		}
		return mCursor;
    }
    
	/**
     * Return a Cursor over the list of all the Prescriptions for the ERVisit
     * with this row id.
     * @return Cursor over all the Prescriptions for the ERVisit.
     */
    public Cursor fetchPrescriptionsForThisVisit(long ervisitID){
        //KEY_MEDICATION, KEY_INSTRUCTIONS
    	Cursor mCursor =
    			//searches for the Prescriptions in the database
    			//the first parameter true indicates that we are interested 
    			//in one distinct result.
    			mDb.query(true, DATABASE_TABLE_PRESCRIPTION, 
    					new String[] {KEY_MEDICATION, KEY_INSTRUCTIONS}, 
    					KEY_ERVISITID + "=" + ervisitID, 
    					null, null, null, KEY_ROWID + " ASC", null);
    	
    	if (mCursor != null) {
    		mCursor.moveToFirst();
    	}
    	// Return a Cursor positioned at the first Prescription that
    	// matches the given ervisit rowId.
    	return mCursor; 
    }
    
	/**
     * Return a Cursor over the list of all the Vitals for the ERVisit
     * with this row id.
     * @return Cursor over all the VitalSigns for the ERVisit.
     */
    public Cursor fetchVitalSignsForThisVisit(long ervisitID){
    	Cursor mCursor =
    			//searches for the Prescriptions in the database
    			//the first parameter true indicates that we are interested 
    			//in one distinct result.
    			mDb.query(true, DATABASE_TABLE_VITALS, 
    					new String[] {KEY_SYSTOLIC, KEY_DIASTOLIC, KEY_TEMPERATURE, KEY_HEARTRATE, KEY_TIMESTAMP}, 
    					KEY_ERVISITID + "=" + ervisitID, 
    					null, null, null, KEY_TIMESTAMP + " ASC", null);
    	
    	if (mCursor != null) {
    		mCursor.moveToFirst();
    	}
    	// Return a Cursor positioned at the first VitalSigns that 
    	// matches the given ervisit rowId.
    	return mCursor; 
    }
    
    /**
     * Return a Cursor positioned at the String containing the 
     * condensed formatted String representation of the Patient's
     * Medical Record to be used for display if the Patient
     * has Medical History at this ER.
     * @param patientHealthCard The health card number identifying the
     * patient whose medical record information is to be retrieved.
     * @return Cursor positioned to the MedicalRecord String representation,
     * if the Medical Record exists, otherwise return an empty Cursor.
     */
    public Cursor fetchMedicalRecordForThisPatient(String patientHealthCard) {
    	//will return Medical Record TEXT already formatted. No need to do 
    	//anything else.
    	String[] args = {patientHealthCard};
    	Cursor mCursor = mDb.rawQuery(FETCH_MEDICAL_RECORD, args);
		if (mCursor != null) {
			mCursor.moveToFirst();
		}
		return mCursor;
    }
    
    /**
     * Return a Cursor over the list of health card numbers of patients
     * in the database who are currently visiting the ER and HAVEN'T been
     * seen by a doctor. The list is ordered by the patient's visit
     * urgency value.
     * @return Cursor over health card numbers of current patients ordered
     * by urgency.
     */
    public Cursor fetchPatientsByUrgency() {
    	//KEY_HEALTHCARD 
    	//multiple lines
    	//Patients that are currently in the ER, aka have one ERVisit.isClosed = false
    	//(HCNs are ordered by urgency, and by arrival time.)
		return mDb.rawQuery(FETCH_PATIENTS_BY_URGENCY, null);
    }
    
    /**
     * Return a Cursor over the list of health card numbers of patients
     * in the database who are currently visiting the ER and ARE being
     * seen by a doctor.
     * @return Cursor over health card numbers of current patients 
     * seen by the doctor.
     */
    public Cursor fetchPatientsSentToDoctor(){
    	return mDb.rawQuery(FETCH_PATIENTS_SENT_TO_DOCTOR, null);
    }

    /**
     * Return a Cursor positioned at the user row that matches the given 
     * combination of username and password.
     * @param username username of user to retrieve.
     * @param password password of user to retrieve.
     * @return Cursor positioned to matching user, if found.
     */
    public Cursor fetchUser(String username, String password){
    	//KEY_ROLENAME
    	//validation of username, password happens here.
    	String[] args = {username, password};
    	Cursor mCursor = mDb.rawQuery(FETCH_USER, args);
		if (mCursor != null) {
			mCursor.moveToFirst();
		}
		return mCursor;
    }
    
    /**
     * Create a new User login using the username and password provided. If the
     * User row is successfully created return true.
     * @param username the username for the User login.
     * @return true if insertion of the new user row is successful.
     */
    public boolean createUser(String username, String password, String role){
    	//Retrieve the RoleID for the role String in the Role table
    	//to be used as the foreign key for the user row.
    	Cursor mCursor = 
    			mDb.query(DATABASE_TABLE_ROLE, 
				new String[] {KEY_ROWID}, 
				KEY_ROLENAME + "='" + role + "'", 
				null, null, null, null, null);
    	if (!mCursor.moveToFirst()) {
			return false;
		}
    	long roleID = mCursor.getLong(0);
    	
    	ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_USERNAME, username); // key-value pairs
        initialValues.put(KEY_PASSWORD, password);
        initialValues.put(KEY_ROLEID, roleID);
    	return mDb.insert(DATABASE_TABLE_USER, null, initialValues) != -1;
    }
    
    /**
     * Insert a new Patient row into the Patient table.
     * If the Patient row is successfully created return true.
     * @param healthCard The patient's health card number.
     * @param dob The patient's birth date.
     * @return true if insertion is successful.
     */
    public boolean createPatient(String healthCard, String name, String dob) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_HEALTHCARD, healthCard); // key-value pairs
        initialValues.put(KEY_NAME, name);
        initialValues.put(KEY_DOB, dob);
        //returns true if creation successful
        return mDb.insert(DATABASE_TABLE_PATIENT, null, initialValues) != -1;
    }
    
    /**
     * Create a new ERVisit row in the ERVisit table.
     * If successfully created return the new rowId for that ERVisit, 
     * otherwise return -1 to indicate failure.
     * @param arrivalTime The arrival time at the ER.
     * @param patientHealthCard The patient who is visiting the ER.
     * @param urgency The urgency value of the visit.
     * @return rowId or -1 if failed.
     */
    public long createERVisit(long arrivalTime, String patientHealthCard, int urgency)  {
        //returns the row _id value for the newly created row.
    	//returns ervisitID in reality
    	Cursor mCursor = 
    			mDb.query(DATABASE_TABLE_PATIENT, 
				new String[] {KEY_ROWID}, 
				KEY_HEALTHCARD + "='" + patientHealthCard + "'", 
				null, null, null, null, null);
    	if (!mCursor.moveToFirst()) {
			return 0L;
		}
    	long patientID = mCursor.getLong(0);
    	
    	 ContentValues initialValues = new ContentValues();
         initialValues.put(KEY_ARRIVALTIME, arrivalTime); // key-value pairs
         initialValues.put(KEY_PATIENTID, patientID); // key-value pairs
         initialValues.put(KEY_URGENCY, urgency); // key-value pairs
         //"insert" returns the row _id value for the newly created row, 
         //or -1 if an error occurred.
         return mDb.insert(DATABASE_TABLE_ERVISIT, null, initialValues);
    }
    
    /**
     * Create a new VitalSigns row in the VitalSigns table,
     * and update the urgency value of the ERVisit during which this set
     * of vital signs was recorded.
     * If successfully created return true.
     * @param ervisitID The row id of the visit in the ERVisit table.
     * that the vital signs are for.
     * @param sys Systolic blood pressure value.
     * @param dia Diastolic blood pressure value.
     * @param HR Heart Rate value.
     * @param timeStamp The time these vital signs were taken.
     * @param urgency The updated urgency value of the visit based on the 
     * new vital sign values.
     * @return true if insertion and update are successful.
     */
    public boolean createVitalSigns(long ervisitID, 
    		int sys, int dia, float temp, float HR, long timeStamp, int urgency) { 	
    	//Note: urgency parameter - this is the urgency taken from patient.
    	//Two transactions with one method. Two birds with one stone.
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_SYSTOLIC, sys); // key-value pairs.
        initialValues.put(KEY_DIASTOLIC, dia);
        initialValues.put(KEY_TEMPERATURE, temp);
        initialValues.put(KEY_HEARTRATE, HR);
        initialValues.put(KEY_TIMESTAMP, timeStamp);
        initialValues.put(KEY_ERVISITID, ervisitID);
        ContentValues args = new ContentValues();
        args.put(KEY_URGENCY, urgency); // key-value pairs.
        //return true if creation and update were successful.
        return  mDb.insert(DATABASE_TABLE_VITALS, null, initialValues) != -1 &&
        		mDb.update(DATABASE_TABLE_ERVISIT, args, KEY_ROWID + "=" + ervisitID, null) > 0;
    }
    
    /**
     * Create a new Prescription row in the Prescription table.
     * If successfully created return true.
     * @param ervisitID The row id of the visit in the ERVisit table 
     * for which this prescription was written.
     * @param name The name of the medication.
     * @param instructions The medication instructions.
     * @return true if insertion is successful.
     */
    public boolean createPrescription(long ervisitID, String name, String instructions) {
    	//returns true if creation successful.
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_MEDICATION, name); // key-value pairs.
        initialValues.put(KEY_INSTRUCTIONS, instructions);
        initialValues.put(KEY_ERVISITID, ervisitID);
        return mDb.insert(DATABASE_TABLE_PRESCRIPTION, null, initialValues) != -1;
    }
    
    /**
     * Overloading method.
     * Update the ERVisit using the details provided. The ERVisit to be updated is
     * specified using the rowId, and it is altered to use the time seen by doctor
     * value passed in.
     * @param ervisitID id of the ER visit to update.
     * @param timeSeenByDoctor Value to set the ERVisit's TIMESEENBYDOCTOR column to.
     * @return true if the ER Visit was successfully updated, false otherwise.
     */
    public boolean updateERVisit(long ervisitID, long timeSeenByDoctor) {
    	ContentValues args = new ContentValues();
        args.put(KEY_TIMESEENBYDOCTOR, timeSeenByDoctor); // key-value pairs
        //return true if update was successful.
        return mDb.update(DATABASE_TABLE_ERVISIT, args, KEY_ROWID + "=" + ervisitID, null) > 0;
    }
    
    /**
     * Overloading method.
     * Update the ERVisit using the details provided. The ERVisit to be updated is
     * specified using the rowId, and it is altered to use the closed
     * value passed in.
     * @param ervisitID id of the ER visit to update.
     * @param closed Value to set the ERVisit's CLOSED column to.
     * @return true if the ER Visit was successfully updated, false otherwise.
     */
    public boolean updateERVisit(long ervisitID, boolean closed) {
    	//turn boolean into either 0 for False, or 1 for true...
    	//I am expecting here "1" for true, b/c the only time you use this method
    	//is when you close the ERVisit.
    	
        ContentValues args = new ContentValues();
        args.put(KEY_CLOSED, closed); // key-value pairs.
        //return true if update was successful.
        return mDb.update(DATABASE_TABLE_ERVISIT, args, KEY_ROWID + "=" + ervisitID, null) > 0;
    }

}
