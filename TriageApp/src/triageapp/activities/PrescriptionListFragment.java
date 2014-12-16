package triageapp.activities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import triageapp.components.ERAdmin;
import triageapp.components.InvalidUserInputException;
import triageapp.components.Patient;
import triageapp.components.Prescription;
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleAdapter;


/**
 * A ListFragment representing a section of the app,
 * displaying a Patient's list of Prescriptions.
 */
public class PrescriptionListFragment extends ListFragment {
    
    /** This PrescriptionListFragment's ERAdmin. */
	private ERAdmin erAdmin;
	
	/** This PrescriptionListFragment's Patient. */
	private Patient patient;
    
	/**
	 * Creates a new instance of PrescriptionListFragment.
     * @param erAdmin PrescriptionListFragment's ERAdmin.
     * @param healthCardNumber The health card number identifying the Patient whose 
     * prescriptions are displayed in this PrescriptionListFragment.
	 * @return PrescriptionListFragment an instance of PrescriptionListFragment.
	 */
	static PrescriptionListFragment newInstance(ERAdmin erAdmin, String healthCardNumber) {
		PrescriptionListFragment f = new PrescriptionListFragment();
		Bundle args = new Bundle();
		args.putSerializable("eradmin", erAdmin);
		args.putString("healthcardnumber",  healthCardNumber);
		f.setArguments(args);

		return f;
	}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /**
     * The Fragment's UI is a ListView with each row containing
     * info from each set of the Patient's Prescriptions written
     * by the doctor during their ER Visit. 
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
    	return inflater.inflate(R.layout.prescription_list, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        fillData(); // fills the list with the Patient's prescriptions
        
	}
    
    /** Fills each row of the ListFragment with Prescription information using a SimpleAdapter.
     * @throws InvalidUserInputException */
    @SuppressLint("SimpleDateFormat") 
    public void fillData() {
		erAdmin = (ERAdmin) getArguments().getSerializable("eradmin");
		patient = erAdmin.lookUpPatient(getArguments().getString("healthcardnumber"));
		if (patient.getCurrentERVisit() == null || !patient.getCurrentERVisit().isSeenByDoctor()){// || patient.getCurrentERVisit().getVitalsSignRecords().isEmpty()){
			return;
		}
		
		
		List<Map<String, String>> displayList = new ArrayList<Map<String, String>>();
		
		List<Prescription> prescriptionList = patient.getCurrentERVisit().getPrescriptionRecords();
		// Generate an iterator. Start just after the last element.
		ListIterator<Prescription> iterator = prescriptionList.listIterator(prescriptionList.size());
		// Iterate in reverse, since we want to retrieve Prescriptions in
		// descending order of time they were written.
		while(iterator.hasPrevious()) {
			Prescription medication = iterator.previous();
			Map<String, String> hashMap = new HashMap<String, String>();
            hashMap.put("name",  medication.getMedicationName());
            hashMap.put("instructions",  medication.getInstructions());
            displayList.add(hashMap);
        }
		
		String[] from = new String[]{"name", "instructions"};
        int[] to = new int[]{R.id.medication_name, R.id.instructions};
        setListAdapter(new SimpleAdapter(getActivity(), displayList, R.layout.prescription_row, from, to));
    }
    
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);	
	
	}

}
