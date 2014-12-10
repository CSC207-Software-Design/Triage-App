package triageapp.activities;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import triageapp.components.ERAdmin;
import triageapp.components.Patient;
import triageapp.components.VitalSigns;
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleAdapter;


/**
 * A ListFragment representing a section of the app,
 * displaying a Patient's list of VitalSigns.
 */
public class VitalsListFragment extends ListFragment {
    
    /** This VitalsListFragment's ERAdmin. */
	private ERAdmin erAdmin;
	
	/** This VitalsListFragment's Patient. */
	private Patient patient;
    
	/**
	 * Creates a new instance of VitalsListFragment.
     * @param erAdmin VitalsListFragment's ERAdmin.
     * @param healthCardNumber The health card number identifying the Patient whose 
     * vitals information is displayed in this VitalsListFragment.
	 * @return VitalsListFragment an instance of VitalsListFragment.
	 */
	static VitalsListFragment newInstance(ERAdmin erAdmin, String healthCardNumber) {
		VitalsListFragment f = new VitalsListFragment();
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
     * info from each set of the Patient's VitalSigns taken 
     * during their ER Visit. 
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
    	return inflater.inflate(R.layout.vitals_list, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        fillData(); // fills the list with the Patient's vital signs.
        
	}
    
    /** Fills each row of the ListFragment with VitalSigns Data using a SimpleAdapter */
    @SuppressLint("SimpleDateFormat") 
    public void fillData() {
		erAdmin = (ERAdmin) getArguments().getSerializable("eradmin");
		patient = erAdmin.lookUpPatient(getArguments().getString("healthcardnumber"));
		if (patient.getCurrentERVisit() == null){// || patient.getCurrentERVisit().getVitalsSignRecords().isEmpty()){
			return;
		}
		List<Map<String, String>> displayList = new ArrayList<Map<String, String>>();
		
		List<VitalSigns> vitalsList = patient.getCurrentERVisit().getVitalsSignRecords();
		// Generate an iterator. Start just after the last element.
		ListIterator<VitalSigns> iterator = vitalsList.listIterator(vitalsList.size());
		// Iterate in reverse, since we want to retrieve Vital signs
		// descending order of arrival time.
		while(iterator.hasPrevious()) {
			VitalSigns vitalsSet = iterator.previous();
			Map<String, String> hashMap = new HashMap<String, String>();
            hashMap.put("systolic",  "Systolic:       " + vitalsSet.getSystolic() + "");
            hashMap.put("diastolic", "Diastolic:      " + vitalsSet.getDiastolic() + "");
            hashMap.put("temp",      "Temperature:    " + vitalsSet.getTemperature() + " \u00b0" + "C" + "");
            hashMap.put("heartrate", "Heart rate:     " + vitalsSet.getHeartRate() + " BPM" + "");
            Date time = new Date (vitalsSet.getTimestamp());
            hashMap.put("date", "Date: " + (new SimpleDateFormat("MM-dd-yyyy")).format(time));
            hashMap.put("time", "Time: " + (new SimpleDateFormat("HH:mm")).format(time));
            displayList.add(hashMap);
        }
		
		//systolic~diastolic~temperature~heart rate~points~timestamp
		String[] from = new String[]{"systolic", "diastolic", "temp", "heartrate", "date", "time"};
        int[] to = new int[]{R.id.list_leftrow1, R.id.list_leftrow2,R.id.list_leftrow3,
        		R.id.list_leftrow4, R.id.list_rightrow1, R.id.list_rightrow2};
        setListAdapter(new SimpleAdapter(getActivity(), displayList, R.layout.vitals_row, from, to));
    }
    
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);	
	
	}
}

