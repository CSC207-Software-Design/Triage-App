package triageapp.components;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Calendar;
import android.annotation.SuppressLint;


/** 
 * Class Patient holds the patient's personal information and medical history. 
 * It is used to access and update the Patient's medical record, 
 * and keep track of the patient's current ER visit. */
@SuppressLint("SimpleDateFormat") 
public class Patient implements Serializable {

	/** The date formatted as yyyy-MM-dd.  */
	public static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
	/** The standard for number of characters in a Health Card Number */
	public static final int HEALTH_CARD_NUM_CHARACTERS = 6;
	/** A unique ID for serialization. */
	private static final long serialVersionUID = -4967725129578310921L;
	/** The name of the Patient. */
	private String name;
	/** The date of birth of the Patient. */
	private String dob;
	/** The healthcard number of the Patient. */
	private String healthCardNumber;
	/** The Patient's current ERVisit. */
	private ERVisit currentERVisit;
	/** The urgency points of Patient. */
	private int urgency;
	/** The age of patient. */
	private int age;
	
	
	/**
	 * Constructs a Patient using their name, date of birth, and healthcard number.
	 * Their urgency points is initially set to 0.
	 * and they do not have an ERVisit.
	 * @param name The name of the Patient.
	 * @param dob The date of birth of the patient, expressed as a String.
	 * @param healthCardNumber The health card number of the Patient.
	 * @throws InvalidUserInputException 
	 */
	public Patient(String name, String dob, String healthCardNumber) throws InvalidUserInputException{
		// Checks for unwanted character "~" due to the file 
		// writing procedure.
		if (name.contains("~") | healthCardNumber.length() != HEALTH_CARD_NUM_CHARACTERS)
			throw new InvalidUserInputException();	
		this.name = name;
		this.dob = dob;
		this.healthCardNumber = healthCardNumber;
		this.urgency = 0;
		this.currentERVisit = null;
		
		Date date = new Date();
		try {
			date = dateFormat.parse(dob);
		} catch (ParseException e) {
			// If there is a ParseException, printStackTrace
			e.printStackTrace();
		}
		Calendar dobcalendar = Calendar.getInstance();
		dobcalendar.setTime(date);
		Calendar today = Calendar.getInstance();
		this.age = today.get(Calendar.YEAR) - dobcalendar.get(Calendar.YEAR);
		if (today.get(Calendar.MONTH) < dobcalendar.get(Calendar.MONTH)) {
			this.age--;
		}
		else if (today.get(Calendar.MONTH) == dobcalendar.get(Calendar.MONTH) 
				&& today.get(Calendar.DAY_OF_MONTH) < dobcalendar.get(Calendar.DAY_OF_MONTH)) {
			this.age--;
		}
	}

	/** Adds a new ERVisit to Patient. */
	public void addNewERVisit(){
		this.currentERVisit = new ERVisit();
		setUrgency();
	}

	/** Closes the Patient's current ERVisit. */
	public void closeCurrentERVisit(){
		this.currentERVisit.setClosed();
	}
	
	/** 
	 * Gets name.
	 * @return the name of Patient. 
	 */
	public String getName(){
		return this.name;
	}

	/** 
	 * Gets dob.
	 * @return the date of birth of Patient.
	 */
	public String getDob(){
		return this.dob;
	}
	
	/** 
	 * Gets healthCardNumber.
	 * @return the healthcard number of Patient.
	 */
	public String getHealthCardNumber(){
		return this.healthCardNumber;
	}
	
	/** 
	 * Gets currentERVisit.
	 * @return the current ERVisit of Patient.
	 */
	public ERVisit getCurrentERVisit(){
		return this.currentERVisit;
	}
	
	/** 
	 * Gets urgency.
	 * @return the urgency points of Patient.
	 */
	public int getUrgency(){
		return this.urgency;
	}
	
	@Override
	 /**
	  * Returns a string representation of Patient.
	 * @return a String representation of Patient.
	 */
	public String toString(){
		return this.currentERVisit.toString();
	}	
	
	/**
	 * Sets the currentERVisit.
	 * @param currentERVisit the current ERVisit of Patient.
	 */
	public void setCurrentERVisit(ERVisit currentERVisit){
		this.currentERVisit = currentERVisit;
		if (this.currentERVisit != null)
			this.setUrgency();
	}

	/**
	 * Sets the urgency of the Patient based on points and age.
	 */
	public void setUrgency(){
		if (this.currentERVisit == null){
			this.urgency = 0;
			return;
		}
		if (currentERVisit.getLatestVitalSigns() == null){
			this.urgency = 0;
		} else {
			this.urgency = currentERVisit.getLatestVitalSigns().getPoints();
		}
		if (age < 2){
			this.urgency++;
		}
	}
	
	/**
	 * Sets the urgency of the Patient.
	 * @param urgency The urgency point value.
	 */
	public void setUrgency(int urgency){
		this.urgency = urgency;
	}
}