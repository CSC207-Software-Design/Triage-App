package triageapp.activities;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import triageapp.components.ERAdmin;
import triageapp.components.InvalidUserInputException;
import triageapp.components.Patient;
import triageapp.database.TriageDBAdapter;
import triageapp.user.Nurse;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Toast;


@SuppressLint("SimpleDateFormat") 
/** The Activity to add a new Patient and sign them into the ER. */
public class AddPatientActivity extends Activity{
	
	/** The date format used to display the Patient's date of birth on the 
	 * AddPatientActivity screen. */
	private static final SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy");
	
	/** This AddPatientActivity's Button to select the patient's date of birth. 
	 * Opens a DatePickerDialog on onClick. */
	private Button birthDate; 
	
	/** This AddPatientActivity's PatientManager. */
	private ERAdmin erAdmin;

	/** This AddPatientActivity's Nurse. */
	private Nurse nurse;
	
	/** The DB Adapter used to save patient data to DB*/
	private TriageDBAdapter mDbHelper;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.add_patient_activity);
		// Sets the screen orientation to portrait.
		setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		getActionBar().setIcon(R.drawable.icon_nurse);
		birthDate = (Button) findViewById(R.id.startDateButton);
		Calendar c = Calendar.getInstance();
		String date = formatDateNumbers(c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH), c.get(Calendar.YEAR));
		birthDate.setText(date);
		birthDate.setOnClickListener(new View.OnClickListener() {
        	@Override
        	public void onClick(View v) {
        		//opens a DatePickerDialog on clicking the birthDate button.
        		int [] date = parseDateString(birthDate.getText().toString());
        		DatePickerDialog dP = new DatePickerDialog(AddPatientActivity.this, 
        				mBirthDateSetListener, date[0], date[1], date[2]);
        		dP.show();
        	}
        });
		
		// Gets the previous AddPatientActivity's ERAdmin and Nurse via 
		// the intent.
		Intent intentPrevious = getIntent();
		erAdmin = (ERAdmin) intentPrevious.getSerializableExtra("eradmin");
		// As this activity is always used by a Nurse, we can assume that 
		// the User is a Nurse.
		nurse = (Nurse) intentPrevious.getSerializableExtra("user");
		mDbHelper = new TriageDBAdapter(this);
	    mDbHelper.open();
		
	}
	
	/**
	 * Called when the "Sign in Patient" button is clicked.
	 * Reads data from the UI and if the user input is correct,
	 * has the Nurse add a new patient and sign them in.
	 * @param v A user interface component.
	 */
	public void addPatient(View v) {
		
		// Reads the user input for the name of the patient.
		EditTextErrorFixed nameText = (EditTextErrorFixed) findViewById(R.id.patientNameEditText);
		String name = nameText.getText().toString();
		String birth_date = birthDate.getText().toString();
		
		Date date = new Date(0);
		try {
			date = AddPatientActivity.dateFormat.parse(birth_date);
		} catch (ParseException e1) {
			e1.printStackTrace();
		}
		String dob = Patient.dateFormat.format(date);
		
		// Reads the user input for the health card number of the patient.
		EditTextErrorFixed hcnText = (EditTextErrorFixed) findViewById(R.id.healthCardEditText);
		String hcn = hcnText.getText().toString();
		
		// The Toast for when the user inputs incorrect information.
		Toast toast = Toast.makeText(this,  getString(R.string.invalid_input), Toast.LENGTH_SHORT);
		View view = toast.getView();
		//sets the background colour to red to indicate that that toast is an error message,
		//not a general notification.
		view.setBackgroundColor(getResources().getColor(R.color.red04));
		if (!name.matches("") && !hcn.matches("")){
			try{
				// Changes health card number to an int from a String.
				//Should be unique.
				Integer.parseInt(hcn);
				//Nurse adds the new Patient.
				nurse.addPatient(erAdmin, name, dob, hcn, mDbHelper);
				getIntent().putExtra("erAdmin", erAdmin);
				getIntent().putExtra("healthcardnumber", hcn);
				setResult(RESULT_OK, getIntent());
				//Finishes the AddPatientActivity.
				finish();
			} 
			catch (InvalidUserInputException exception){
			    setErrorMessage(name, hcn);
				toast.show();
			} 
			catch (NumberFormatException exception){
				setErrorMessage(name, hcn);
				toast.show();
			}
		} else {
			setErrorMessage(name, hcn);
			toast.show();	
		}
	}
	
	/**
	 * Sets error messages that will be displayed in a popup next to the 
	 * text fields that contain invalid input.
	 * @param name The patient name.
	 * @param healthCard The health card number.
	 */
	public void setErrorMessage(String name, String healthCard){
		if (name.matches(""))
			((EditTextErrorFixed) findViewById(R.id.patientNameEditText)).setError("Empty field");
		else if (name.contains("~"))
			((EditTextErrorFixed) findViewById(R.id.patientNameEditText)).setError("Contains ~");
		
		if (healthCard.matches(""))
			((EditTextErrorFixed) findViewById(R.id.healthCardEditText)).setError("Empty field");
		else if(!isParsableToInt(healthCard))
			((EditTextErrorFixed) findViewById(R.id.healthCardEditText)).setError("Integer required");
		else if (healthCard.length() != Patient.HEALTH_CARD_NUM_CHARACTERS)
			((EditTextErrorFixed) findViewById(R.id.healthCardEditText)).setError("6 characters required");
		else if (erAdmin.lookUpPatient(healthCard) != null)
			((EditTextErrorFixed) findViewById(R.id.healthCardEditText)).setError("HCN exists");
			
			
	}
	
	/**
	 * Checks if parsing the String to an integer throws an exception.
	 * @param input A String containing the int representation to be parsed.
	 * @return true if the string contains a parsable integer.
	 */
	public boolean isParsableToInt(String input){
		boolean parsable = true;
		try{
			Integer.parseInt(input);
		}catch(NumberFormatException e){
			parsable = false;
		}
		return parsable;
	}
	

	/**
	 * Called when the user is done filling in the date from the DatePickerDialog.
	 * Formats the date based on the year, month, and day data, and sets it as 
	 * the text of the birthDate selection button to make the user's choice
	 * visible.
	 */
    private DatePickerDialog.OnDateSetListener mBirthDateSetListener = new OnDateSetListener() {
    	
    	/**
    	 * Catches the event when the Date in the DatePickerDialog is set.
    	 * Formats the date, and sets the birthDate button text to this formatted
    	 * date String.
    	 * @param view The view associated with this listener.
		 * @param year The year that was set.
		 * @param monthOfYear The month that was set (0-11) for compatibility with Calendar.
         * @param dayOfMonth The day of the month that was set.
    	 */
    	@Override
    	public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
    		String date = formatDateNumbers(monthOfYear, dayOfMonth, year);
    		birthDate.setText(date);
    	}
    };
    
    /**
     * Parses a formatted date String and returns the year, month, and 
     * day as individual tokens that can be used to set the initial 
     * date in the DatePickerDialog the next time it is shown.
     * @param s The formatted Date String.
     * @return An int array containing integers corresponding to the year,
     * month and day as represented in the Date String s.
     * */
    private int[] parseDateString (String s){
    	Date date = new Date();
    	try {
			date = dateFormat.parse(s);
		} catch (ParseException e) {
			e.printStackTrace();
		}
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);
        int[] tokens = {year, month, day};
    	return tokens;
    }

    /**
     * Formats date tokens (year, month, day) into a readable string
     * that can be displayed back to the user as text on the birthDate 
     * selection button or used as a parameter for the patient's date 
     * of birth when creating a new Patient.
     * @param monthOfYear The month that was set by the user.
     * @param dayOfMonth The day that was set.
     * @param year The year that was set.
     * @return A formatted Date string.
     */
    public String formatDateNumbers (int monthOfYear,
            int dayOfMonth, int year){
    	String formattedMonth = "" + (monthOfYear + 1);
	    String formattedDayOfMonth = "" + dayOfMonth;
	    if(monthOfYear < 10)
	        formattedMonth = "0" + formattedMonth;
	    if(dayOfMonth < 10){
	        formattedDayOfMonth = "0" + formattedDayOfMonth;
	    }
	    String date = formattedMonth + "-" + formattedDayOfMonth + "-" + year;
	    return date;
    }
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu.
		getMenuInflater().inflate(R.menu.add_patient_activity, menu);
		return true;
	}

    /**
     * Catches the event when the user 
     * leaves AddPatientActivity. 
     * Closes the DB connection.
     */
    @Override
    protected void onPause() {
    	mDbHelper.close();
    	super.onPause();
    }
	

}
