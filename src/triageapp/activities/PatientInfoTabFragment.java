package triageapp.activities;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import triageapp.components.ERAdmin;
import triageapp.components.Patient;
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.support.v4.app.Fragment;


/**
 * A Fragment representing a section of the app screen,
 * displaying a Patients's info.
 */
@SuppressLint("SimpleDateFormat") 
public class PatientInfoTabFragment extends Fragment{

	/** This PatientInfoTabFragment's ERAdmin. */
	private ERAdmin erAdmin;
	
	/** This PatientInfoTabFragment's Patient. */
	private Patient patient;
	
	/** The View for this PatientInfoTabFragment. */
	private View view;
	
	/**
	 * Creates a new instance of PatientInfoTabFragment.
	 * @param erAdmin This PatientInfoTabFragment's ERAdmin.
     * @param healthCardNumber The health card number identifying the Patient whose 
     * information is displayed in this PatientInfoTabFragment.
	 * @return PatientInfoTabFragment an instance of PatientInfoTabFragment.
	 */
	public static PatientInfoTabFragment newInstance(ERAdmin erAdmin, String healthCardNumber) {
		PatientInfoTabFragment f = new PatientInfoTabFragment();
		Bundle args = new Bundle();
		args.putSerializable("eradmin", erAdmin);
		args.putString("healthcardnumber", healthCardNumber);
		f.setArguments(args);
		return f;
	}
	
    /**
     * The Fragment's UI is a TableLayout with each table row containing
     * a field of the Patient's information.
     */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		view = inflater.inflate(R.layout.patient_info, container, false);
		fillData(); // fills the View with the Patient's info.
		return view;
	}
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);	
	}
	
	/** Fills each TextView of this Fragment with the corresponding Patient data 
	 * Does not display arrival time and urgency if Patient is not currently signed in,
	 * and if the Patient has not seen the doctor, does not display the time seen by doctor.
	 */
	private void fillData(){
		TextView nameTextView = (TextView) view.findViewById(R.id.patient_name);
		erAdmin = (ERAdmin) getArguments().getSerializable("eradmin");
		patient = erAdmin.lookUpPatient(getArguments().getString("healthcardnumber"));
		nameTextView.setText(patient.getName());
		TextView healthNumberTextView = (TextView) view.findViewById(R.id.patient_health_number);
		healthNumberTextView.setText("Health Card Number: " + patient.getHealthCardNumber());
		TextView dobTextView = (TextView) view.findViewById(R.id.patient_dob);
		Date date = new Date(0);
		try {
			date = Patient.dateFormat.parse(patient.getDob());
		} catch (ParseException e) {
			
		}
		dobTextView.setText("Date of Birth: " + (new SimpleDateFormat("MM-dd-yyyy")).format(date));
		if (patient.getCurrentERVisit() != null){
			fillExtraData();
		} 
	}
	
	/**
	 * Displays Patient's arrival time and urgency.
	 * If the patient has been seen by the doctor, also displays the time they were seen.
	 * Called only when Patient is a current visitor of the ER. 
	 */
	private void fillExtraData(){
		TextView arrivalTimeTextView = (TextView) view.findViewById(R.id.patient_arrivalTime);
		Date time = new Date (patient.getCurrentERVisit().getArrivalTime());
		arrivalTimeTextView.setText("Arrival time: " + (new SimpleDateFormat("MM-dd-yyyy")).format(time) + " at " + (new SimpleDateFormat("HH:mm")).format(time));
		TextView urgencyTextView = (TextView) view.findViewById(R.id.patient_urgency);
		urgencyTextView.setText("Urgency: " + patient.getUrgency());
		if (patient.getCurrentERVisit().isSeenByDoctor()){
			//fill seen by doc
			TextView timeSeenTextView = (TextView) view.findViewById(R.id.patient_time_seen_by_doctor);
			Date time_seen = new Date (patient.getCurrentERVisit().getTimeSeenByDoctor());
			timeSeenTextView.setText("Time seen by doctor: " + (new SimpleDateFormat("MM-dd-yyyy")).format(time_seen) + " at " + (new SimpleDateFormat("HH:mm")).format(time_seen));
		}
	}
	
}


