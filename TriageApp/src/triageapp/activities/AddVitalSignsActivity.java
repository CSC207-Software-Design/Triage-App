package triageapp.activities;

import triageapp.components.ERAdmin;
import triageapp.components.InvalidUserInputException;
import triageapp.components.Patient;
import triageapp.components.VitalSigns;
import triageapp.database.TriageDBAdapter;
import triageapp.user.Nurse;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;


/** An Activity to add vitals for a specific Patient. */
public class AddVitalSignsActivity extends Activity {

	/** This AddVitalSignsActivity's ERAdmin. */
	private ERAdmin erAdmin;

	/** This AddVitalsActivity's Patient. */
	private Patient patient;
	
	/** This AddVitalSignsActivity's Nurse. */
	private Nurse nurse;
	
	/** The DB Adapter used to save Vital Signs data to DB*/
	private TriageDBAdapter mDbHelper;
	
	/** An identifier for the primitive data type int */
	private final int INT = 0;
	
	/** An identifier for the primitive data type float */
	private final int FLOAT = 1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Removes title bar.
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.add_vitals_activity);

		// Sets screen orientation lock.
		setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

		// Gets the ERadmin and Patient from the previous activity via the intent.
		Intent intentPatientActivity = getIntent();
		erAdmin = (ERAdmin) intentPatientActivity.getSerializableExtra("eradmin");
		patient = erAdmin.lookUpPatient(intentPatientActivity.getStringExtra("healthcardnumber"));
		// As this activity is always used by a Nurse, we can assume that 
		// the User is a Nurse.
		nurse = (Nurse) intentPatientActivity.getSerializableExtra("user");
		// Dynamically changes the text of the button to include patient's name.
		TextView newPatient = (TextView) findViewById(R.id.enter_button);
		newPatient.setText("Add " + patient.getName() + "'s Vital Signs");
		
		mDbHelper = new TriageDBAdapter(this);
	    mDbHelper.open();
	}

	/**
	 * Called when the "Add Vital Signs" button is clicked.
	 * Reads data from the UI, and if user input is correct
	 * creates a VitalSigns object and has the Nurse add 
	 * the new vitals.
	 * @param v A user interface component.
	 */
	public void addNewRecord(View v) {

		// Gets the user input for systolic from the EditText field.
		EditTextErrorFixed systolic = (EditTextErrorFixed) findViewById(R.id.editText1);
		String systolicInput = systolic.getText().toString();

		// Gets the user input for diastolic from the EditText field.
		EditTextErrorFixed diastolic = (EditTextErrorFixed) findViewById(R.id.editText2);
		String diastolicInput = diastolic.getText().toString();

		// Gets the user input for temperature from the EditText field.
		EditTextErrorFixed temperature = (EditTextErrorFixed) findViewById(R.id.editText3);
		String temperatureInput = temperature.getText().toString();

		// Gets the user input for heart rate from the EditText field.
		EditTextErrorFixed heartRate = (EditTextErrorFixed) findViewById(R.id.editText4);
		String heartRateInput = heartRate.getText().toString();

		// The Toast for when the user inputs incorrect information.
		Toast toast = Toast.makeText(this,  getString(R.string.invalid_input), Toast.LENGTH_SHORT);
		View view = toast.getView();
		view.setBackgroundColor(getResources().getColor(R.color.red04));

		int systolicValue = 0;
		float heartRateValue = 0;
		float temperatureValue = 0;
		int diastolicValue = 0;
		try {
			// Changes heartRate, temperature, diastolic and systolic each 
			// to a float (or int) from a String.
			//Try/catch blocks are here to make sure ALL String inputs 
			//are parsed, without a empty String exception thrown early 
			//cutting off the processing for one of them.
			//The boolean flag later generates the appropriate exception
			//if needed to set the error messages for the user's benefit.
			boolean doesParsingFail = false;
			try {systolicValue = Integer.parseInt(systolicInput);}
			catch(NumberFormatException e){doesParsingFail = true;}
			try {diastolicValue = Integer.parseInt(diastolicInput);}
			catch(NumberFormatException e){doesParsingFail = true;}
			try {temperatureValue = Float.parseFloat(temperatureInput);}
			catch(NumberFormatException e){doesParsingFail = true;}
			try {heartRateValue = Float.parseFloat(heartRateInput);}
			catch(NumberFormatException e){doesParsingFail = true;}
			
			if (doesParsingFail) throw new NumberFormatException();
			
			// Creates a new VitalSigns object based on the user input, 
			// Time is automatically set to NOW in VitalSigns`s constructor.
			VitalSigns vitals = new VitalSigns(systolicValue, diastolicValue, 
					temperatureValue, heartRateValue);
			//ERAdmin adds the vitals signs for the Patient.
			
			nurse.addPatientVitals(erAdmin, patient, vitals, mDbHelper);
			getIntent().putExtra("erAdmin", erAdmin);
			setResult(RESULT_OK, getIntent());
			//Finishes the AddVitalSignsActivity.
			finish();
		} catch (InvalidUserInputException exception){ //Caught a text field with unreasonable input
			setErrorMessage(systolicValue, diastolicValue, temperatureValue, heartRateValue);
			//in case of empty user input
			setErrorMessage(systolicInput, diastolicInput, temperatureInput, heartRateInput);
			toast.show();
		} catch (NumberFormatException exception){ // User input is empty somewhere, or invalid type.
			//in case of unreasonable user input
			setErrorMessage(systolicValue, diastolicValue, temperatureValue, heartRateValue);
			setErrorMessage(systolicInput, diastolicInput, temperatureInput, heartRateInput);
			toast.show();

		}

	}
	
	/**
	 * Checks if parsing the String to the given primitive type throws an exception. 
	 * 2 types are available for parsing: int and float.
	 * @param input A String containing the parsableType representation to be parsed
	 * @param parsableType The primitive type the String is to parsed to.
	 * @return true if the string contains the correct parsable primitive type.
	 */
	public boolean isParsable(String input, int parsableType){
	    boolean parsable = true;
	    try {
	    	if (parsableType == this.INT)
	    		Integer.parseInt(input);
	    	else if (parsableType == this.FLOAT)
	    		Float.parseFloat(input);
	    } catch(NumberFormatException e){
	        parsable = false;
	    }
	    return parsable;
	}
	
	/**
	 * Checks the integer values of the fields for unreasonable input,
	 * and sets appropriate error messages that will be displayed in 
	 * a popup next to the text fields where invalid input was detected. 
	 * @param systolic The systolic blood pressure of the Patient.
	 * @param diastolic The diastolic blood pressure of the Patient.
	 * @param temperature The temperature of the Patient.
	 * @param heartRate The heart rate of the Patient.
	 */
	public void setErrorMessage(int systolic, int diastolic, float temperature, 
			float heartRate){
		// Checks for unreasonable user input, and sets the error of the EditText 
		// with the incriminating input.
		if(systolic > 500) 
			((EditTextErrorFixed) findViewById(R.id.editText1)).setError("Number > 500");
		if (diastolic > 500)	
			((EditTextErrorFixed) findViewById(R.id.editText2)).setError("Number > 500");
		if (temperature > 100)	
			((EditTextErrorFixed) findViewById(R.id.editText3)).setError("Number > 100");
		if (heartRate > 350)	
			((EditTextErrorFixed) findViewById(R.id.editText4)).setError("Number > 350");
	}
	
	/**
	 * Checks the string values of the fields for empty or invalid input.
	 * Sets error messages that will be displayed in a popup next to the 
	 * text fields that contain the invalid input. 
	 * @param systolic The String representation of the Patient's systolic blood pressure.
	 * @param diastolic The String representation of the Patient's diastolic blood pressure.
	 * @param temperature The String representation of the Patient's temperature.
	 * @param heartRate The String representation of the Patient's heart rate.
	 */
	public void setErrorMessage(String systolic, String diastolic, String temperature, 
			String heartRate){
		// Checks for empty user input, and sets the error of the EditText 
		// with the empty input.
		if (systolic.matches("")) 
			((EditTextErrorFixed) findViewById(R.id.editText1)).setError("Empty field");
		else if (!isParsable(systolic, this.INT))
			((EditTextErrorFixed) findViewById(R.id.editText1)).setError("Integer required");
		if (diastolic.matches("")) 
			((EditTextErrorFixed) findViewById(R.id.editText2)).setError("Empty field");
		else if (!isParsable(diastolic, this.INT))
			((EditTextErrorFixed) findViewById(R.id.editText2)).setError("Integer required");
		
		if (temperature.matches("")) 
			((EditTextErrorFixed) findViewById(R.id.editText3)).setError("Empty field");
		else if (!isParsable(temperature, this.FLOAT))
			((EditTextErrorFixed) findViewById(R.id.editText3)).setError("Decimal required");
		if (heartRate.matches("")) 
			((EditTextErrorFixed) findViewById(R.id.editText4)).setError("Empty field");
		else if (!isParsable(heartRate, this.FLOAT))
			((EditTextErrorFixed) findViewById(R.id.editText4)).setError("Decimal required");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu.
		getMenuInflater().inflate(R.menu.vitals_activity, menu);
		return true;
	}
	
    /**
     * Catches the event when the user 
     * leaves AddVitalsActivity. 
     * Closes the DB connection.
     */
    @Override
    protected void onPause() {
    	mDbHelper.close();
    	super.onPause();
    }
	
}
