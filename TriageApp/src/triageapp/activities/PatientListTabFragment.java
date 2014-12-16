package triageapp.activities;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import triageapp.components.ERAdmin;
import triageapp.components.Patient;
import triageapp.user.Nurse;
import triageapp.user.Physician;
import triageapp.user.User;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.AdapterView.OnItemLongClickListener;


/**
 * A ListFragment representing a section of the app,
 * displaying a list of Patients with clickable 
 * Patient items.
 */
@SuppressLint("SimpleDateFormat") 
public class PatientListTabFragment extends ListFragment{
	
	/** The type of list this Fragment is displaying (urgency vs. sent to doctor). */
	private String displaytype;
	
	/** The Argument key identifying the type of data displayed in this Fragment. */
	public static final String ARG_OBJECT = "displaytype";
	
	/** This PatientListTabFragment's ERAdmin. */
	private ERAdmin erAdmin;
	
	/** This PatientListTabFragment's User. */
	private User user;
	
	/** A value representing the type of this PatientListTabFragment's user.*/
	private boolean userType;
	
    /**
     * Create a new instance of PatientListTabFragment.
     * @param num The Position within the adapter (tab position)
     * @param erAdmin This PatientListTabFragment's ERAdmin.
     * @param user This PatientListTabFragment's User.
     * @param userType Boolean identifier of the type of User passed as a 
     * parameter (Nurse or Physician).
     * @return PatientListTabFragment an instance of PatientListTabFragment, 
     * with the correct display type as argument.
     */
    static PatientListTabFragment newInstance(int num, ERAdmin erAdmin, User user, boolean userType) { 	
    	
    	PatientListTabFragment f = new PatientListTabFragment();
        // Supply num input as an argument.
        Bundle args = new Bundle();
        args.putString(PatientListTabFragment.ARG_OBJECT, MainActivity.displayWhich[num]);
        args.putInt("num", num);
        args.putSerializable("eradmin", erAdmin);
        args.putSerializable("user", user);
        args.putBoolean("usertype", userType);
        f.setArguments(args);
        
        return f;
    }
    
    /**
     * Creates this fragment and retrieve this instance's number from its arguments.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        displaytype = getArguments().getString(ARG_OBJECT, "urgency");
    }

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.patient_list, container, false);
	}

	@Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        fillData(); //fills the list
	}

	/** Fills each row of the ListFragment with Patient Data using a SimpleAdapter.
	 * Checks the userType and displayType arguments to determine what kind of list
	 * of patients to display (patients by urgency, or patients being seen by the doctor).
	 * */
	public void fillData() {
		displaytype = (String) getArguments().get(ARG_OBJECT);
		erAdmin = (ERAdmin) getArguments().getSerializable("eradmin");
		userType = getArguments().getBoolean("usertype");
		
		if (userType == User.PHYSICIAN && displaytype.equals(MainActivity.displayWhich[1]))
			return;
		//dummy code, REMOVE THIS!!
		//if (userType == MainActivity.NURSE & displaytype.equals(MainActivity.displayWhich[1]))
		//	return;
		
		List<Patient> patientList;
		String[] from;
		List<Map<String, String>> displayList = new ArrayList<Map<String, String>>();
		
		if (userType == User.PHYSICIAN){ //tab is currently on Waiting List.
			 user = (Physician) getArguments().getSerializable("user");
			 from = new String[]{"name", "textview1", "textview2"};
			 patientList = erAdmin.getPatientsSentToDoctorList();
			 
		}
		else{ //userType is Nurse
			user = (Nurse) getArguments().getSerializable("user");
			if (displaytype.equals(MainActivity.displayWhich[0])){ //tab is currently on Urgency.
				from = new String[]{"name", "textview1", "textview2"};
				patientList = erAdmin.getUrgencyList();	
				
			} else{ //tab is currently on Sent To Doctor.
				from = new String[]{"name", "textview2", "textview1"};
				patientList = erAdmin.getPatientsSentToDoctorList();
			}	
		}

		for (Patient patient : patientList) { 
            Map<String, String> hashMap = new HashMap<String, String>();
            hashMap.put("name", patient.getName());
            String time;
            //tab is currently on "Urgency" (for the Nurse) or 
            //"Waiting List" (for the Doctor). Arrival time of 
            //the patient in the ER should be displayed.
            if (displaytype.equals(MainActivity.displayWhich[0])) 
            	time = "Arrival: " + (new SimpleDateFormat("MM-dd-yyyy HH:mm")).format(
            			new Date(patient.getCurrentERVisit().getArrivalTime()));
            //tab is currently on "Sent To Doctor" for the Nurse
            //the time seen by the doctor should be displayed in 
            //this view.
            else 
            	time = (new SimpleDateFormat("MM-dd-yyyy HH:mm")).format(
            			new Date(patient.getCurrentERVisit().getTimeSeenByDoctor()));
            hashMap.put("textview2", time);
            hashMap.put("textview1", "Urgency: " + patient.getUrgency());
            displayList.add(hashMap);
        }
		
        int[] to = new int[]{R.id.list_row1, R.id.list_row2, R.id.list_row3};
        setListAdapter(new SimpleAdapter(getActivity(), displayList, R.layout.patient_row, from, to));
	}
    
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		getListView().setOnItemLongClickListener(myListItemLongClickListener);	
		
		
	}

	/** Navigates to PatientActivity.class which displays info on the Patient
	 * who is representing this list item that was clicked.
	 * @param l The ListView of this Fragment where the click happened.
	 * @param v The view (row) that was clicked within the ListView.
	 * @param position The position of the view in the list, also corresponding to
	 * the index of the Patient within the Urgency/Sent To Doctor list.
	 * @param id The row id of the item that was clicked
	 */
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		l.setItemChecked(position, false);
		List<Patient> patientList;
		//sets the list to "patients seen by doctor" list.
		if (userType == User.PHYSICIAN | 
				(userType == User.NURSE & displaytype.equals(MainActivity.displayWhich[1]))){
			patientList = erAdmin.getPatientsSentToDoctorList();
		}
		else { //sets the list to the Patients "by Urgency", who have not yet seen the doctor list.
			patientList = erAdmin.getUrgencyList();
		}	
		Patient patient = patientList.get(position); //position is equivalent to index of the arrayList.
		Intent i = new Intent(getActivity(), PatientActivity.class);
		i.putExtra("eradmin", erAdmin);
		i.putExtra("user", user);
		i.putExtra("healthcardnumber", patient.getHealthCardNumber());
		//go to Patient's info Page...
		getActivity().startActivityForResult(i, MainActivity.VIEW_PATIENT);
		//startActivityForResult(i, MainActivity.VIEW_PATIENT);
	}
	
	public void clearListChoice(){
		getListView().requestLayout();
		getListView().clearChoices();
		
	}


	OnItemLongClickListener myListItemLongClickListener = new OnItemLongClickListener(){
		@Override
		public boolean onItemLongClick(AdapterView<?> view, View row,
				int position, long id) {
			row.setSelected(true);
			return true;
		}
	};
	

}



