package triageapp.components;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.annotation.SuppressLint;


/** An ER Visit Record class. */
@SuppressLint("SimpleDateFormat") 
public class ERVisit implements Serializable{
	
	/** A unique ID for serialization. */
	private static final long serialVersionUID = 3158772401410039603L;

	/** The date-time format  */
	public static final SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
	
	/** This ERVisit's id in the DB */
	public long id;
	/** The Patient's arrival time at the ER. */
	private long arrivalTime;
	/** The time that the patient was seen by a doctor. */
	private long timeSeenByDoctor;
	/** Collection of Vital Sign records ordered by arrival time in ascending order. */
	private List<VitalSigns> vitalSignsRecords;
	/** Collection of Prescription records. */
	private List<Prescription> prescriptionRecords;
	/**	This visit's current status. */
	private boolean isClosed;
	/** A boolean value representing whether or not the Patient 
	 * has been seen by a Physician during This ERVisit*/
	private boolean seenByDoctor;

	/**
	 * Constructs an ERVisit Record with closed status initially false.
	 * and arrival time set to the instant the ER Visit is created.
	 */
	public ERVisit(){
		isClosed = false;
		seenByDoctor = false;
		vitalSignsRecords = new ArrayList<VitalSigns>();
		prescriptionRecords = new ArrayList<Prescription>();
		arrivalTime = Calendar.getInstance().getTimeInMillis();
	}
	
	/**
	 * Constructs an existing ERVisit (used when creating records from file).
	 * @param arrivalTime The arrival time of the Patient.
	 * @param closed Whether the ERVisit has been closed.
	 * @param timeSeenByDoctor The time seen by a doctor.
	 * @throws ParseException 
	 */
	public ERVisit(long id, long arrivalTime, boolean closed, Long timeSeenByDoctor){
		this.id = id;
		this.isClosed = closed;
		//this.healthCardNumber = healthCardNumber;
		this.vitalSignsRecords = new ArrayList<VitalSigns>();
		this.prescriptionRecords = new ArrayList<Prescription>();
		this.arrivalTime = arrivalTime;
		if (timeSeenByDoctor != null){
			this.timeSeenByDoctor = timeSeenByDoctor;
			this.seenByDoctor = true;
		} else {
			this.seenByDoctor = false;
		}
			
	}
	
	/**
	 * Sets the id field of this ERVisit corresponding to its row.
	 * in the ERVisit DB table.
	 * @param id The id field of this ERVisit corresponding to its row.
	 */
	public void setID(long id){
		this.id = id;
	}
	
	/**
	 * Gets the id field of this ERVisit corresponding to its row.
	 * in the ERVisit database table. Used for updating the ERVisit columns
	 * in the database.
	 * @return The id field of this ERVisit corresponding to its row.
	 */
	public long getID(){
		return this.id;
	}
	
	/**
	 * Sets this ERVisit's status to closed.
	 */
	public void setClosed(){
		isClosed = true;
	}
	
	/**
	 * Gets this ERVisit's current status.
	 * @return The ERVisit's status.
	 */
	public boolean getClosed(){
		return isClosed;
	}
	
	/**
	 * Returns true iff this Patient has been seen by a Physician.
	 * @return true iff this Patient has been seen by a Physician.
	 */
	public boolean isSeenByDoctor() {
		return this.seenByDoctor;
	}
	
	/**
	 * Sets this ERVisit's seenByDoctor to true, and sets the time stamp of 
	 * this Patient's timeSeenByDoctor.
	 */
	public void setSeenByDoctor(){
		timeSeenByDoctor = Calendar.getInstance().getTimeInMillis();
		seenByDoctor = true;
	}

	/**
	 * Returns the time this patient has been seen by a Physician, 
	 * and 0 if the patient has not been seen.
	 * @return The time this patient has been seen by a Physician, 
	 * and 0 if the patient has not been seen.
	 */
	public long getTimeSeenByDoctor(){
		return timeSeenByDoctor;
	}
	
	/**
	 * Returns the Patient's arrival time at the ER.
	 * @return The ER Arrival time.
	 */
	public long getArrivalTime(){
		return arrivalTime;
	}
	
	/**
	 * Returns the collection of VitalSigns taken during this visit.
	 * @return An ArrayList of VitalSign records.
	 */
	public List<VitalSigns> getVitalsSignRecords(){
		return vitalSignsRecords;
	}
	
	/**
	 * Adds new VitalSigns to this ER Visit Record.
	 * @param vitalsign The VitalSigns of a Patient that this ER Visit Record belongs to.
	 */
	public void addVitalsSignRecord(VitalSigns vitalsign){
		vitalSignsRecords.add(vitalsign);
	}
	
	/**
	 * Returns the collection of Prescriptions recorded by the Physician during this visit.
	 * @return An ArrayList of Prescription records.
	 */
	public List<Prescription> getPrescriptionRecords(){
		return prescriptionRecords;
	}
	
	/**
	 * Adds a new Prescription to this ER Visit Record.
	 * @param prescription The Medication for the Patient that this ER Visit Record belongs to.
	 */
	public void addPrescriptionRecord(Prescription prescription){
		prescriptionRecords.add(prescription);
	}
	
	/**
	 * Gets most recent record of VitalSigns.
	 * @return The latest record of VitalSigns.
	 */
	public VitalSigns getLatestVitalSigns(){
		if (vitalSignsRecords.isEmpty())
			return null;
		else
			return vitalSignsRecords.get(vitalSignsRecords.size() - 1);
	}
	
	@Override
	/** @return A String representation of this ERVisit. */
	public String toString(){
		String arrivaltime = dateTimeFormat.format(new Date(arrivalTime));
				//arrivalTime
		//String erVisitString = "ERVisit~" + healthCardNumber + "~" + arrivaltime;
		String erVisitString = "ERVisit~" + arrivaltime;
		if (isSeenByDoctor())
			erVisitString += "~" + dateTimeFormat.format(new Date(timeSeenByDoctor));
		erVisitString += "\n";
		for (VitalSigns vitals : vitalSignsRecords){
			erVisitString += vitals.toString();
		}
		for (Prescription pres : prescriptionRecords){
			erVisitString += pres.toString();
		}
		return erVisitString;
	}

	/**
	 * Returns a string representation for displaying this ERVisit record.
	 * @return A string representation to display this ERVisit record.
	 */
	public String getDisplay() {
		SimpleDateFormat calendarFormat = new SimpleDateFormat("MM/dd/yyyy");
		SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
		//Formats the arrival time by splitting it up into a Calendar date,
		//and a time.
		String calendarDate = calendarFormat.format(new Date(arrivalTime));
		String time = timeFormat.format(new Date(arrivalTime));
		String displayHeader = "+++++++++++++++++++++ \n Record \n" + "+++++++++++++++++++++ \n" +
				"Arrival Date: " + calendarDate + "\n" +
				"Arrival Time: " + time + "\n";
		
		if (this.seenByDoctor){
			calendarDate = calendarFormat.format(new Date(timeSeenByDoctor));
			time = timeFormat.format(new Date(timeSeenByDoctor));
			displayHeader += "Seen by Doctor Date: " + calendarDate + "\n" +
								"Seen by Doctor Time: " + time + "\n";
							
		}
		
		// Constructs the display of every VitalSign.
		String vitalsString = "";
		for(VitalSigns vitals : vitalSignsRecords) {
			//Formats the date-time of each VitalSign's timestamp for display.
			calendarDate = calendarFormat.format(new Date(vitals.getTimestamp()));
			time = timeFormat.format(new Date(vitals.getTimestamp()));
			vitalsString = vitalsString + 
					"------------------" + "\n" +
					"Vital Signs:" + "\n" +
					"Date: " + calendarDate + "\n" +
					"Time: " + time + "\n" +
					"Systolic: " +
					vitals.getSystolic() + "\n" +
					"Diastolic: " +
					vitals.getDiastolic() + "\n" +
					"Temperature: " +
					vitals.getTemperature() + "\n" +
					"Heart Rate: " +
					vitals.getHeartRate() + "\n"; 
		}
		// Constructs the display of every Prescription.
		String prescriptionString = "";
		for(Prescription prescription : prescriptionRecords) {
			prescriptionString = prescriptionString + 
					"------------------" + "\n" +
					"Prescription:" + "\n" +
					"Name: " +
					prescription.getMedicationName() + "\n" +
					"Instructions: " +
					prescription.getInstructions() + "\n";	
		}
		
		return displayHeader + vitalsString + prescriptionString;
	}

	

}