package triageapp.activities;

import triageapp.components.ERAdmin;
import triageapp.components.Patient;
import triageapp.user.User;
import triageapp.user.Nurse;
import triageapp.user.Physician;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.FragmentTransaction;
import android.app.ActionBar.Tab;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;


/** The Main Activity to display lists of current Patients, and navigate inside
 *  the Application (search for patient, view patient info, etc.).
 *  Displays only a waiting list of current Patients being seen by the doctor
 *  if the user viewing this screen is a Physician.
 *  If the user viewing this screen is a Nurse, then the Activity also displays 
 *  a list of current Patients who have not yet been seen by a doctor by urgency.
 */
public class MainActivity extends FragmentActivity {

	/**
	 * The PagerAdapter that will provide fragments representing
	 * each list of Patients (by Urgency, or Sent to Doctor). 
	 * It extends FragmentPagerAdapter, which will destroy and 
	 * re-create fragments as needed, saving and restoring their
	 * state in the process.
	 */
	PatientCollectionPagerAdapter mDemoCollectionPagerAdapter;

	/** The names of the tabs in this Activity. */
	public static String[] displayWhich = {"urgency", "senttodoctor"};
	
	/** The names of possible dialogs, identifying the intent of the user on invoking
	 * the dialog: to search for a patient, or search for a patient's medical record. */
	public static final String[] searchWhich = {"patient", "medicalrecord"};

	/** This MainActivity's ERAdmin. */
	private ERAdmin erAdmin;
	
	/** This MainActivity's User. */
	private User user;
	
	/** A value representing the type of this MainActivity's user. */
	private boolean userType;
	
	/** The request code for viewing a patient's info. */
	protected static final int VIEW_PATIENT = 0;
	
	/** The request code for adding a new patient. */
	protected static final int ADD_PATIENT = 1;

	/**
	 * The CustomViewPager that will display the object collection.
	 * It enables the user to flip left and right through each tab fragment (page).
	 * When the user is a Nurse, it allows them to go between the pages displaying the
	 * Urgency list of Patients and the Patients being seen by the doctor. 
	 * When the user is a Physician, there is only one tab/page displayed: Patients "Sent to Doctor". 
	 * Thus for the Physician, the swipe functionality of this ViewPager is disabled.
	 */
	CustomViewPager mViewPager;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        if (savedInstanceState != null){
        	// Restore value of members from saved state.
        	erAdmin = (ERAdmin) savedInstanceState.getSerializable("eradmin");
        	userType = savedInstanceState.getBoolean("usertype");
        	//Checks and sets the User to its respective type.
        	if(userType == User.NURSE)
    			user = (Nurse) savedInstanceState.getSerializable("user");
        	else
        		user = (Physician) savedInstanceState.getSerializable("user");   
        	
        } else {
        	//Get's the ERAdmin and User from the previous activity.
    		Intent intent = getIntent();
    		erAdmin = (ERAdmin) intent.getSerializableExtra("eradmin");
        	// Checks and sets the User to its respective type, and changes the 
    		// userType accordingly.
    		if(intent.getSerializableExtra("user") instanceof 
    				triageapp.user.Physician){
    			userType = User.PHYSICIAN;
    			user = (Physician) intent.getSerializableExtra("user");
    		} else {
    			userType = User.NURSE;
    			user = (Nurse) intent.getSerializableExtra("user");
    		}
        }
        
        if (userType == User.NURSE){
        	this.setTitle(getString(R.string.nurse));
        	getActionBar().setIcon(R.drawable.icon_nurse);
        } else {
        	this.setTitle(getString(R.string.doctor));
        	getActionBar().setIcon(R.drawable.triage);
        }

        // Create an adapter that when requested, will return a fragment representing a Patient list.
        // ViewPager and its adapters use support library fragments, so we must use
        // getSupportFragmentManager.
        mDemoCollectionPagerAdapter = new PatientCollectionPagerAdapter(getSupportFragmentManager());
        
        // Set up action bar.
        final ActionBar actionBar = getActionBar();

        // Specify that tabs should be displayed in the action bar.
	    actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // Set up the ViewPager, attaching the adapter.
        mViewPager = (CustomViewPager) findViewById(R.id.custom_pager);
        mViewPager.setAdapter(mDemoCollectionPagerAdapter);
        mViewPager.setOnPageChangeListener(
                new ViewPager.SimpleOnPageChangeListener() {
                    @Override
                    public void onPageSelected(int position) {
                        // When swiping between pages, select the
                        // corresponding tab.
                        getActionBar().setSelectedNavigationItem(position);
                    }
                });
        
	    // Create a tab listener that is called when the user changes tabs.
	    ActionBar.TabListener tabListener = new ActionBar.TabListener() {
			@Override
			public void onTabReselected(Tab tab, FragmentTransaction arg1) {
				
			}

			@Override
			public void onTabSelected(Tab tab, FragmentTransaction ft) {
				mViewPager.setCurrentItem(tab.getPosition());
			}

			@Override
			public void onTabUnselected(Tab tab, FragmentTransaction arg1) {
			}
	    };
	    
	    if (userType == User.NURSE){
	    	// Add 2 tabs, specifying the tab's text and TabListener.
		    actionBar.addTab(
		    		actionBar.newTab()
		    		.setText("Urgency")
		    		.setTabListener(tabListener));
		    actionBar.addTab(
		    		actionBar.newTab()
		    		.setText("Sent To Doctor")
		    		.setTabListener(tabListener));
	    }
	    
	    if (userType == User.PHYSICIAN){
	    	// Add a single tab, specifying the tab's text and TabListener
		    actionBar.addTab(
		    		actionBar.newTab()
		    		.setText("Sent To Doctor")
		    		.setTabListener(tabListener));
		    mViewPager.setPagingEnabled(false);
	    }    
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_activity, menu);
        userOptions(menu);
        return true;
    }
    
    /**
     * Called on creation of this screen's (MainActivity) menu.
     * Changes the screen navigability to suit User role.
     * If the user is a Nurse, sets the Add Patient button to visible. 
     * Otherwise if the user is a Physician, hides the Add Patient button. 
     * @param menu The menu for this MainActivity.
     */
    public void userOptions(Menu menu){
    	menu.findItem(R.id.menu_add_patient).setVisible((userType == User.NURSE));
    }
    
    /** Used to handle events generated from the menu (e.g., when the user
     * selects the "Search Patient" item).
     **/
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch (item.getItemId()) {
    	case R.id.menu_add_patient: // add a new patient
    		Intent i = new Intent(this, AddPatientActivity.class);
    		i.putExtra("eradmin", erAdmin);
    		i.putExtra("user", user);
    		startActivityForResult(i, ADD_PATIENT); //result: patient is signed into the ER.
    		return true;
    	case R.id.menu_search: // look up a patient
    		SearchDialogFragment fragment = new SearchDialogFragment(this, searchWhich[0]);
    		return true;
    	case R.id.menu_medical_record: // case: look up a Patient's entire medical history
    		SearchDialogFragment med_fragment = new SearchDialogFragment(this, searchWhich[1]);
    		return true;
		}
        return super.onOptionsItemSelected(item);
    }
    
    /**
     * Gets the updated ERAdmin object from the previous Activity
     * and reloads the PatientListFragments in case a Patient
     * was added to or removed from current patients lists in ERAdmin.
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode,
    		Intent intent) {
    	super.onActivityResult(requestCode, resultCode, intent);
    	//Update this MainActivity's erAdmin object.
    	if (requestCode == VIEW_PATIENT && resultCode == RESULT_OK) {
    		erAdmin = (ERAdmin) intent.getSerializableExtra("eradmin");
        } else if (requestCode == ADD_PATIENT && resultCode == RESULT_OK){
        	erAdmin = (ERAdmin) intent.getSerializableExtra("eradmin");
        	/* POSSIBLE ADD-IN CODE:
        	Intent patientIntent = new Intent(MainActivity.this, PatientActivity.class);
    		patientIntent.putExtra("eradmin", erAdmin);
    		patientIntent.putExtra("user", user);
    		patientIntent.putExtra("healthcardnumber", intent.getIntExtra("healthcardnumber", 0));
			startActivityForResult(i, VIEW_PATIENT);*/
        }
    	mDemoCollectionPagerAdapter.notifyDataSetChanged();	
    	
    }
    
    /**
     * Called before Activity is recreated (on an orientation change).
     * Saves the state (saves ERAdmin object) of the Activity 
     * so that the state can be restored in onCreate.
     */
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
    	//super.onSaveInstanceState(savedInstanceState);
        //Save the PatientActivity's current state.
        savedInstanceState.putSerializable("eradmin", erAdmin);
        savedInstanceState.putBoolean("usertype", userType);
        savedInstanceState.putSerializable("user", user);
    }
   
    /**
     * Catches the event when the user presses the 
     * back button to go back the previous Activity,
     * and sends the (possibly) updated ERAdmin object
     * as the result.
     */
    @Override
    public void onBackPressed() {
    	Intent i = new Intent();
    	i.putExtra("eradmin", erAdmin);
    	setResult(RESULT_OK, i);
    	super.onBackPressed();
    }
    
 
    /**
     * A internal Dialog box class for looking up a Patient. 
     * Prompts a user to look up a Patient by entering the health card number.
     * Displayed for two functions: to look up a Patient's info,
     * or to view a Patient's medical record.
     */
    public class SearchDialogFragment extends Dialog {
		
    	/**
    	 * The EditText for entering the Patient's health card number.
    	 */
		private EditText filterText = null;
		
		/**
		 * This Activity (MainActivity.class).
		 */
		Context c;
		
		/**
		 * View representing the layout of the Dialog.
		 */
		private View v;
		
		/** The type of dialog being displayed (look up patient vs. look up medical record) */
		private final String displaytype;
		
		/**
		 * Creates a Dialog window that uses the default dialog frame style
		 * and displays the title and performs actions corresponding the searchType.
		 * @param context The Context the Dialog is to run in.
		 * @param searchType The key identifying the type of dialog based on the user's intent.
		 */
		public SearchDialogFragment(Context context, String searchType){
			super(context);
			this.c = context;
			this.displaytype = searchType;
            // Use the Builder class for convenient dialog construction.
    		LayoutInflater inflater = getLayoutInflater();
    		
    		v = inflater.inflate(R.layout.patient_search_dialog, null);
    		filterText = (EditText) v.findViewById(R.id.searchBox);
    		// Uses the Builder class for convenient dialog construction.
    		AlertDialog.Builder builder = new AlertDialog.Builder(c);
    		//Sets the title to inform the user they have either chosen
    		//to look up a Patient's information OR a Patient's Medical Record.
    		if (displaytype.equals(searchWhich[0]))
    			builder.setTitle(R.string.patient_search_dialog);
    		else
    			builder.setTitle(R.string.medical_record_search_dialog);
    		builder.setView(v) // Set the layout for the dialog.
    		.setPositiveButton(R.string.menu_ok,  new DialogInterface.OnClickListener(){
    			
    			@Override
    			public void onClick(DialogInterface dialog, int which) {
    				String healthCardText = filterText.getText().toString();
    				try { 
    					Integer.parseInt(healthCardText); 
    				} catch(NumberFormatException e) { 
    					Context context = getApplicationContext();
    					CharSequence message = "Invalid health card number ID";
    					Toast toast = Toast.makeText(context,  message, Toast.LENGTH_SHORT);
    					toast.show();
    					return;
    				}

    				Patient patient = erAdmin.lookUpPatient(healthCardText);

    				if (patient != null){ //when correct health card entered:
    					if (displaytype.equals(searchWhich[0])){ //Look up a Patient's info.
    						Intent i = new Intent(MainActivity.this, PatientActivity.class);
    						i.putExtra("eradmin", erAdmin);
    						i.putExtra("user", user);
    						i.putExtra("healthcardnumber", healthCardText);
    						startActivityForResult(i, VIEW_PATIENT);
    					} else { //Look up a Patient's Medical Record
    						Intent i = new Intent(MainActivity.this, ShowMedicalRecord.class);
    			    		i.putExtra("eradmin", erAdmin);
    			    		i.putExtra("healthcardnumber", healthCardText);
    			    		i.putExtra("usertype", userType);
    			    		startActivity(i);
    					}
    				} else{ //when the patient info is not entered correctly.
    					Context context = getApplicationContext();
    					CharSequence message = "Patient with health card number " + healthCardText + " not found.";
    					Toast toast = Toast.makeText(context,  message, Toast.LENGTH_SHORT);
    					toast.show();
    				}
    			}   	   
    		}).setNegativeButton(R.string.menu_cancel, new DialogInterface.OnClickListener(){
    			@Override
    			public void onClick(DialogInterface dialog, int which) {
    				// User cancelled the dialog.
    			}
    		});

    		Dialog alert = builder.create();
    		alert.show();
		}
    }

    /**
     * A FragmentPagerAdapter that returns a ListFragment
     * representing a list of Patients (either by urgency 
     * or Patients who are seeing the Doctor).
     */
    public class PatientCollectionPagerAdapter extends FragmentStatePagerAdapter {
    	
    	/** The number of fragments in this adapter. */
    	public static final int NUM_FRAGMENTS = 2; 
    	/** The Array of PatientListTabFragments within this Activity */
    	public PatientListTabFragment[] mFragments = new PatientListTabFragment[2];

    	public PatientCollectionPagerAdapter(FragmentManager fm) {
    		super(fm);
    	}

    	/**
    	 * Creates a new instance of PatientListTabFragment at this tab position.
    	 * @param position The Position within this adapter
    	 * @return the PatientListTabFragment associated with a specified position.
    	 */
    	@Override
    	public Fragment getItem(int position) {
    		mFragments[position] = PatientListTabFragment.newInstance(position, erAdmin, user, userType);
    		return mFragments[position];
    	}

    	@Override
    	public int getCount() {
    		return NUM_FRAGMENTS;
    	}
    	
    	@Override
    	public int getItemPosition(Object object) {     
    	    return POSITION_NONE;
    	}

    }


}
