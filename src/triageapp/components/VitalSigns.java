package triageapp.components;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import android.annotation.SuppressLint;


/**
 * VitalSigns stores the vital signs (eg. blood pressure, temperature, heart rate) of a patient 
 * at the time he/she was checked.
 */
@SuppressLint("SimpleDateFormat") 
public class VitalSigns implements Serializable{
	
	/** A unique ID for serialization. */
	private static final long serialVersionUID = -7126051044157567299L;
	
	/**	The patient's blood pressure (systolic) in mm Hg. */
	private int systolic;	
	/**	The patient's blood pressure (diastolic) in mm Hg. */
	private int diastolic;	
	/**	The patient's temperature in Celsius. */
	private float temperature;	
	/**	The patients heart rate in bpm. */
	private float heartRate;	
	/**	The time in milliseconds when vital signs were recorded. */
	private long timestamp;	
	/**	The patient's urgency points. */
	private int points;
	
	/** The normal range of body temperature for a Patient in Celsius. */
	public static final float TEMPERATURE_RANGE = 39.0f;
	/** The normal range of heart rate for a Patient in bpm. */
	public static final float[] HEART_RATE_RANGE = {100.0f, 50.0f};
	/** The normal range of blood pressure for a Patient in mm Hg. */
	public static final int[] BLOOD_PRESSURE_RANGE = {140, 90};
	
	
	/**
	 * Constructs VitalSigns with inputs of systolic, diastolic,
	 * temperature, and heart rate of the patient.
	 * @param sys The systolic blood pressure of the Patient. 
	 * @param dia The diastolic blood pressure of the Patient.
	 * @param temp The body temperature of the Patient.
	 * @param HR The heart rate of the Patient.
	 * @throws InvalidUserInputException 
	 */
	public VitalSigns(int sys,int dia,float temp,float HR) throws InvalidUserInputException {
		if((sys > 500) ||
			(dia > 500) ||
			(temp > 100) ||
			(HR > 350))
			throw new InvalidUserInputException();
		systolic = sys;
		diastolic = dia;
		temperature =  temp;
		heartRate = HR;
		timestamp = Calendar.getInstance().getTimeInMillis();	//get the current time.
		calculateUrgencyPoints();
	}
	
	/**
	 * Constructs a VitalSigns for a loaded ERVisit (used when creating record from file).
	 * @param sys The systolic blood pressure of the Patient. 
	 * @param dia The diastolic blood pressure of the Patient.
	 * @param temp The body temperature of the Patient.
	 * @param HR The heart rate of the Patient.
	 * @param timeStamp the timestamp of the VitalSigns.
	 */
	public VitalSigns(int sys,int dia,float temp,float HR, long timeStamp){
		//SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		//passes values as Strings to save space on calling method.
		systolic = sys;
		diastolic = dia;
		temperature = temp;
		heartRate = HR;
		this.timestamp = timeStamp;
		calculateUrgencyPoints();
	}
	
	/**
	 * Calculates urgency level based on vital sign data.
	 */
	private void calculateUrgencyPoints(){
		int pts=0;
		if (temperature>=TEMPERATURE_RANGE){
			pts++;
		}
		if (systolic>=BLOOD_PRESSURE_RANGE[0] | diastolic>=BLOOD_PRESSURE_RANGE[1]){
			pts++;
		}
		if (heartRate>=HEART_RATE_RANGE[0] | heartRate<=HEART_RATE_RANGE[1]){
			pts++;
		}
		this.points = pts;
	}

	/**
	 * Gets the patient's blood pressure (systolic) number.
	 * @return Patient's blood pressure (systolic) number.
	 */
	public int getSystolic() {
		return systolic;
	}

	/**
	 * Gets the patient's blood pressure (diastolic) number.
	 * @return Patient's blood pressure (diastloic) number.
	 */
	public int getDiastolic() {
		return diastolic;
	}

	/**
	 * Gets the patient's temperature.
	 * @return The patient's temperature. 
	 */
	public float getTemperature() {
		return temperature;
	}

	/**
	 * Gets the patient's heart rate.
	 * @return The patient's heart rate.
	 */
	public float getHeartRate() {
		return heartRate;
	}

	/**
	 * Gets the time stamp (milliseconds) when vital signs was recorded.
	 * @return The time stamp (milliseconds) when vital signs was recorded.
	 */
	public long getTimestamp() {
		return timestamp;
	}

	/**
	 * Gets the patient's urgency points based.
	 * @return The patient's urgency points.
	 */
	public int getPoints() {
		return points;
	}
	
	/**
	 * Returns a String representation of the patient's vital signs.
	 * @return A String representation of the patient's vital signs.
	 */
	public String toString() {
		String dateString = ERVisit.dateTimeFormat.format(new Date(timestamp));
		return "VitalSigns~"+systolic+"~"+diastolic+"~"+temperature+"~"+heartRate+"~"+ dateString + "\n";
	}
}
