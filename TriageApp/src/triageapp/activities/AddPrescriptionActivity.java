package triageapp.activities;

import triageapp.components.ERAdmin;
import triageapp.components.InvalidUserInputException;
import triageapp.components.Patient;
import triageapp.components.Prescription;
import triageapp.database.TriageDBAdapter;
import triageapp.user.Physician;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;


/** An Activity to add a prescription for a specific Patient. */
public class AddPrescriptionActivity extends Activity {
	
	/** This AddPrescriptionActivity's ERAdmin. */
	private ERAdmin erAdmin;

	/** This AddPrescriptionActivity's Patient. */
	private Patient patient;
	
	/** This AddPrescriptionActivity's Physician. */
	private Physician physician;
	
	/** The DB Adapter used to save Prescription data to DB. */
	private TriageDBAdapter mDbHelper;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Removes title bar.
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.add_prescription_activity);

		// Sets screen orientation lock.
		setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

		// Gets the ERadmin and Patient from the previous activity via the intent.
		Intent intentPatientActivity = getIntent();
		erAdmin = (ERAdmin) intentPatientActivity.getSerializableExtra("eradmin");
		// As this activity is always used by a Physician, we can assume that 
		// the User is a Physician.
		physician = (Physician) intentPatientActivity.getSerializableExtra("user");
		patient = erAdmin.lookUpPatient(intentPatientActivity.getStringExtra("healthcardnumber"));
		// Dynamically changes the text of the button to include patient's name.
		TextView newPrescription = (TextView) findViewById(R.id.enter_button);
		newPrescription.setText("Add " + patient.getName() + "'s Prescription");
		
		mDbHelper = new TriageDBAdapter(this);
	    mDbHelper.open();
	}

	/**
	 * Called when the "Add Prescription" button is clicked.
	 * Reads data from the UI, and if user input is correct,
	 * creates a Prescription object and has the Physician add 
	 * the new prescription.
	 * @param v A user interface component.
	 */
	public void addNewPrescription(View v) {

		// Gets the Prescription name from the user.
		EditTextErrorFixed nameText = (EditTextErrorFixed) findViewById(R.id.editText1); 
		String name = nameText.getText().toString(); 

		// Gets the Prescription instructions from the user.
		EditTextErrorFixed instructionText = (EditTextErrorFixed) findViewById(R.id.editText2); 
		String instructions = instructionText.getText().toString(); 
		// The Toast for when the user inputs incorrect information.
		Toast toast = Toast.makeText(this,  getString(R.string.invalid_input), Toast.LENGTH_SHORT);
		View view = toast.getView();
		//sets the background colour to red to indicate that that toast is an error message,
		//not a general notification.
		view.setBackgroundColor(getResources().getColor(R.color.red04));
		
		if(!name.matches("") && !instructions.matches("")) { 
			// Adds the Prescriptions for a patient if the Prescription 
			// doesn't contain illegal characters, such as new line or
			// "~" (for parsing reasons).
			try {
				Prescription prescription = new Prescription(name, instructions); 
				physician.addPatientPrescription(erAdmin, patient, prescription, mDbHelper);
				
				// Passes on objects to PatientActivity.java.
				getIntent().putExtra("erAdmin", erAdmin);
				setResult(RESULT_OK, getIntent());
				//Finishes the AddPrescriptionActivity.
				finish();
			} catch (InvalidUserInputException e) {
				setErrorMessage(name, instructions);
				toast.show();
			} 
		} else {
			setErrorMessage(name, instructions);
			toast.show();
		}

	}
	
	/**
	 * Sets error messages that will be displayed in a popup next to the 
	 * text fields that contain invalid input.
	 * @param name The name of the Prescription medication.
	 * @param instructions The instructions for the Prescription medication.
	 */
	public void setErrorMessage(String name, String instructions){
		// Checks for invalid user input, and sets the error of the EditText 
		// with the incriminating input.
		if (name.matches(""))
			((EditTextErrorFixed) findViewById(R.id.editText1)).setError("Empty field");
		else if (name.contains("~"))
			((EditTextErrorFixed) findViewById(R.id.editText1)).setError("Contains ~");
		
		if (instructions.matches(""))
			((EditTextErrorFixed) findViewById(R.id.editText2)).setError("Empty field");
		else if (instructions.contains("~") & instructions.contains("\n"))
			((EditTextErrorFixed) findViewById(R.id.editText2)).setError("Contains ~ and new line.");
		else if (instructions.contains("~"))
			((EditTextErrorFixed) findViewById(R.id.editText2)).setError("Contains ~");
		else if (instructions.contains("\n"))
			((EditTextErrorFixed) findViewById(R.id.editText2)).setError("Contains new line");
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu.
		// reuses vitals_activity menu since neither of these activities have an action bar.
		getMenuInflater().inflate(R.menu.vitals_activity, menu);
		return true;
	}
	
    /**
     * Catches the event when the user 
     * leaves AddPrescriptionActivity. 
     * Closes the DB connection.
     */
    @Override
    protected void onPause() {
    	mDbHelper.close();
    	super.onPause();
    }
}
