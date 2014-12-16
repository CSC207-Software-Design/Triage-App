package triageapp.activities;

import triageapp.components.ERAdmin;
import triageapp.database.TriageDBAdapter;
import triageapp.user.Nurse;
import triageapp.user.Physician;
import triageapp.user.User;
import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.app.ActionBar.Tab;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;


/** The Activity to display a Patient's information, Vital Signs (if the patient is
 * currently signed in the ER), and Prescriptions (if the User viewing the Activity
 * screen is a Physician, and the patient is signed in and seen by the doctor).
 * */
public class PatientActivity extends FragmentActivity {
	
	/**
	 * The PagerAdapter that will provide fragments representing the Patient's
	 * info, the list of the Patient's vital signs, and (possibly) the list
	 * of the Patient's prescriptions (For Physicians). 
	 * It extends FragmentStatePagerAdapter, which will destroy and re-create 
	 * fragments as needed, saving and restoring their state in the process.
	 */
	PatientInformationPagerAdapter mDemoCollectionPagerAdapter;
	 
	 /** This PatientActivity's ERAdmin. */
	 private ERAdmin erAdmin;
	 
	 /** This PatientActivity's User. */
	 private User user;
	 
	 /** This PatientActivity's DB Adapter used to save data to the DB. */
	 private TriageDBAdapter mDbHelper;
	 
	 /** A value representing the type of this PatientActivity's user. */
	 private boolean userType;
	 
	 /** The Patient's health card number for this PatientActivity. */
	 private String healthCardNumber;
	 
	 /** The Tab displaying this Patient's ERVisit vitals info. */
	 private Tab vitalsTab;
	 
	 /** The Tab displaying this Patient's ERVisit prescription info (for Physicians). */
	 private Tab prescriptionTab;
	 
	 /** The request code for adding new vital signs. */
	 private static final int ADD_VITAL_SIGNS = 0;  // The request code
	 
	 /** The request code for adding a new prescription. */
	 private static final int ADD_PRESCRIPTION = 1;  // The request code
	 
	 /** The request code for showing the Patient's Medical Record. */
	 private static final int SHOW_MEDICAL_RECORD = 2;  // The request code
	 
	 /** Identifier for the tab that is currently selected in the ActionBar */
	 private int currentTabPosition;
	 
	 /** The number of tabs in this screen depending on user role. */
	 private int numberOfTabs;

    /**
     * The CustomViewPager that will display the object collection.
     * It enables the user to flip left and right through each tab fragment (page)
     * to go between Patient info, vitals, and prescriptions (only for Physicians) 
     * when the patient is signed in.
     * If the patient is signed out, the swipe functionality of this ViewPager
     * is disabled.
     */
	 CustomViewPager mViewPager;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.patient_activity);
        mDbHelper = new TriageDBAdapter(this);
		mDbHelper.open(); // opens or creates the database.
        // Check whether we're recreating a previously destroyed instance.
        if (savedInstanceState != null){
        	// Restore value of members from saved state.
        	erAdmin = (ERAdmin) savedInstanceState.getSerializable("eradmin");
        	userType = savedInstanceState.getBoolean("usertype");
        	//Checks and sets the User to its respective type.
        	if (userType == User.NURSE) user = (Nurse) savedInstanceState.getSerializable("user");
        	else user = (Physician) savedInstanceState.getSerializable("user");  
        	
        	healthCardNumber = savedInstanceState.getString("healthcardnumber");
        	currentTabPosition = savedInstanceState.getInt("currenttabposition");
        } else {
            // Initialize members with default values for a new instance.
        	Intent previousIntent = getIntent();
        	erAdmin = (ERAdmin) previousIntent.getSerializableExtra("eradmin");
        	if(previousIntent.getSerializableExtra("user") instanceof 
    				triageapp.user.Physician){
    			userType = User.PHYSICIAN;
    			user = (Physician) previousIntent.getSerializableExtra("user");
    		} else {
    			userType = User.NURSE;
    			user = (Nurse) previousIntent.getSerializableExtra("user");
    		}
            healthCardNumber = previousIntent.getStringExtra("healthcardnumber");
        }
        Log.w("track", "currentTabPosition111: " + currentTabPosition);
        
        if (userType == User.NURSE){
        	numberOfTabs = 2;
        	getActionBar().setIcon(R.drawable.icon_nurse);
        } else {
        	numberOfTabs = 3; //displays an additional tab of a list of prescriptions for Physicians.
        	getActionBar().setIcon(R.drawable.triage);
        }
        
        // Create an adapter that when requested, will return a fragment representing an object in
        // the collection.
        // ViewPager and its adapters use support library fragments, so we must use
        // getSupportFragmentManager.
        mDemoCollectionPagerAdapter = new PatientInformationPagerAdapter(getSupportFragmentManager());
        
        // Set up action bar.
        ActionBar actionBar = getActionBar();

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
                        currentTabPosition = position;
                        Log.w("track", "ack1");
                    }
                });
        
        
	    // Create a tab listener that is called when the user changes tabs.
	    ActionBar.TabListener tabListener = new ActionBar.TabListener() {
			@Override
			public void onTabReselected(Tab tab, FragmentTransaction arg1) {
			}

			@Override
			public void onTabSelected(Tab tab, FragmentTransaction ft) {
				//currentTabPosition = tab.getPosition();
				mViewPager.setCurrentItem(tab.getPosition());
				Log.w("track", "ack2");
			}

			@Override
			public void onTabUnselected(Tab tab, FragmentTransaction arg1) {
			}
	    };
	    
	    // Add 2 tabs, specifying the tab's text and TabListener
	    actionBar.addTab(
	    		actionBar.newTab()
	    		.setText("Info")
	    		.setTabListener(tabListener));
	    // Tab displaying vitalSigns may or may not be displayed, depending on whether patient is 
	    // currently signed into the ER.
	    if (isPatientSignedIn() && !(userType == User.PHYSICIAN && !isPatientSeenByDoctor())){
	    	vitalsTab = actionBar.newTab()
	    			.setText("Vitals")
	    			.setTabListener(tabListener);
	    	actionBar.addTab(vitalsTab);
	    	if (userType == User.PHYSICIAN){ //create a prescriptions tab only for Physicians.
	    		prescriptionTab = actionBar.newTab()
	    				.setText("Prescriptions")
	    				.setTabListener(tabListener);
	    		actionBar.addTab(prescriptionTab);
	    	}
	    }
	    Log.w("track", "currentTabPosition: " + currentTabPosition);
	    getActionBar().setSelectedNavigationItem(currentTabPosition);
	    mViewPager.setCurrentItem(currentTabPosition);
    }

    /**
     * Catches the event when the user presses the 
     * back button to go back the previous Activity,
     * and sends the (possibly) updated ERAdmin object
     * as the result.
     */
    @Override
    public void onBackPressed() {
    	Intent i = getIntent();
    	i.putExtra("eradmin", erAdmin);
    	setResult(RESULT_OK, i);
    	super.onBackPressed();
    	
    }
    
    /**
     * Return true if patient is currently visiting the ER.
     * @return whether this patient is currently signed in the ER.
     */
    private boolean isPatientSignedIn(){
    	return (erAdmin.lookUpPatient(healthCardNumber)).getCurrentERVisit() != null;
    }
    
    /**
     * Return true if patient is being seen by the doctor.
     * @return whether this patient is being seen by the doctor.
     */
    private boolean isPatientSeenByDoctor(){
    	if (!isPatientSignedIn())
    		return false;
    	return (erAdmin.lookUpPatient(healthCardNumber)).getCurrentERVisit().isSeenByDoctor();
    }
    
    /**
     * Called on creation of this screen and whenever patient is signed in or signed out.
     * Changes the screen navigability to suit the Patient's status change (from currently 
     * signed in or signed out from the ER).
     * The form of the option menu change depends on the user role.
     * For Nurses:
     * If patient is signed in to the ER, enables the ViewPager and hides the Sign Out button and
     * sets the Add New Vital Signs button to visible. If not, disables the ViewPager, makes the 
     * Sign In button visible. If a current patient has not yet been seen by the doctor, 
     * the Send to Doctor button is visible. The Add Prescription button is always invisible.
     * For Physicians:
     * If a patient is signed in AND currently being seen by a doctor, the Add Prescription
     * button is set to Visible, and the Physician has the ability to sign out a patient 
     * via the visible Sign Out menu button. The Add Vitals button is always invisible.
     * @param patientMenu The menu for this PatientActivity.
     */
    private void patientSignedIn(Menu patientMenu){
    	if (isPatientSignedIn()){
    		if (userType == User.PHYSICIAN && !isPatientSeenByDoctor())
    			mViewPager.setPagingEnabled(false);
    		else
    			mViewPager.setPagingEnabled(true);
    	}
    	else{	
    		mViewPager.setPagingEnabled(false);
    	}
    	
    	if (userType == User.NURSE){
    		patientMenu.findItem(R.id.menu_add_prescription).setVisible(false);
    		patientMenu.findItem(R.id.menu_send_to_doctor).setVisible(isPatientSignedIn() && !isPatientSeenByDoctor());
    		patientMenu.findItem(R.id.menu_add_vitals).setVisible(isPatientSignedIn());	
    		patientMenu.findItem(R.id.menu_sign_in).setVisible(!isPatientSignedIn());
    		patientMenu.findItem(R.id.menu_sign_out).setVisible(isPatientSignedIn());
    	} else {
    		patientMenu.findItem(R.id.menu_sign_in).setVisible(false);
    		patientMenu.findItem(R.id.menu_send_to_doctor).setVisible(false);
    		patientMenu.findItem(R.id.menu_add_vitals).setVisible(false);
    		patientMenu.findItem(R.id.menu_add_prescription).setVisible(isPatientSeenByDoctor());
    		patientMenu.findItem(R.id.menu_sign_out).setVisible(isPatientSeenByDoctor());
    	}
    }
    

    /**
     * Called before the Activity is recreated (when Patient is signed in/out, 
     * or on a screen orientation change).
     * Saves the state (saves the ERAdmin object, the health card number identifying
     * the Patient being featured in this PatientActivity, and the tab position) 
     * of the Activity so that the state can be restored in onCreate.
     */
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Save the PatientActivity's current state.
        savedInstanceState.putSerializable("eradmin", erAdmin);
        savedInstanceState.putSerializable("user", user);
        savedInstanceState.putBoolean("usertype", userType);
        savedInstanceState.putString("healthcardnumber", healthCardNumber);
        savedInstanceState.putInt("currenttabposition", currentTabPosition);
        //super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.patient_activity, menu);
        patientSignedIn(menu); //sets visibility of sign in/out action bar button.
        return true;
    }
    
    /** Used to handle events generated from the menu (e.g., when the user
     * selects the "View Medical Record" item).
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch (item.getItemId()) {
    	case R.id.menu_sign_in:
    		Nurse nurse = (Nurse) user;
			nurse.signInPatient(erAdmin, this.healthCardNumber, mDbHelper);
    		recreate(); //recreates the activity to show an extra tab for patient vitals (for Nurses)
    		return true;
    	case R.id.menu_sign_out:
    		//Saves this Patient's current visit data to their 
    	    //Medical History on DB using this PatientActivity's ERAdmin.
    	    //Used in the case when the Patient is dismissed from the ER.
    		erAdmin.closeCase(erAdmin.lookUpPatient(healthCardNumber), mDbHelper);
    		recreate(); //recreates the activity to remove the extra tab for patient 
    					//vitals and (possibly) prescriptions (for Physicians).
    		getActionBar().removeTab(vitalsTab); //removes tab
    		if (userType == User.PHYSICIAN)
    			getActionBar().removeTab(prescriptionTab);
    		currentTabPosition = 0;
    		return true;
    	case R.id.menu_send_to_doctor:
    		Nurse nurs = (Nurse) user;
    		nurs.sendToDoctor(erAdmin, erAdmin.lookUpPatient(healthCardNumber), mDbHelper);
    		//change visibility of this button.
    		item.setVisible(false);
    		//Message informing the Nurse that the patient was successfully transferred to
    		//a Doctor's care.
    		mDemoCollectionPagerAdapter.notifyDataSetChanged();
    		CharSequence message_sent = "Patient has been sent to the doctor.";
			Toast toast_sent = Toast.makeText(this,  message_sent, Toast.LENGTH_SHORT);
			toast_sent.show();
    		return true;
    	case R.id.menu_medical_record: // case: show the Patient's entire medical history.
    		Intent i = new Intent(this, ShowMedicalRecord.class);
    		i.putExtra("eradmin", erAdmin);
    		i.putExtra("healthcardnumber", healthCardNumber);
    		i.putExtra("usertype", userType);
    		startActivity(i);
    		return true;
    	case R.id.menu_add_vitals: // case: add a new set of VitalSigns (ONLY for Nurses).
    		Intent a = new Intent(this, AddVitalSignsActivity.class);
    		a.putExtra("eradmin", erAdmin);
    		a.putExtra("healthcardnumber", healthCardNumber);
    		a.putExtra("user", user);
    		startActivityForResult(a, ADD_VITAL_SIGNS); //result: patient vitals are updated.
    		return true;
    	case R.id.menu_add_prescription: // case: add a new prescription (ONLY for Physicians)
    		Intent b = new Intent(this, AddPrescriptionActivity.class);
    		b.putExtra("eradmin", erAdmin);
    		b.putExtra("user", user);
    		b.putExtra("healthcardnumber", healthCardNumber);
    		startActivityForResult(b, ADD_PRESCRIPTION); //result: patient prescriptions are updated.
    		return true;	
		}
        return super.onOptionsItemSelected(item);
    }
    
    /**
     * On return from the AddVitalSignsActivity, gets the updated ERAdmin object 
     * and reloads the screen in case a set of vital signs was added, and 
     * consequently the Patient's urgency level was changed. 
     * On return from the AddPrescriptionActivity, gets the updated ERAdmin object
     * and reloads the screen in case a prescription was added for the Patient.
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode,
    		Intent intent) {
    	super.onActivityResult(requestCode, resultCode, intent);
    	if (requestCode == ADD_VITAL_SIGNS && resultCode == RESULT_OK) {
    		//Update this PatientActivity's erAdmin object.
    		erAdmin = (ERAdmin) intent.getSerializableExtra("eradmin");
        } else if (requestCode == ADD_PRESCRIPTION && resultCode == RESULT_OK){
        	//Update this PatientActivity's erAdmin object.
        	erAdmin = (ERAdmin) intent.getSerializableExtra("eradmin");
        }
    	mDemoCollectionPagerAdapter.notifyDataSetChanged();
    }
    
    /**
     * Catches the event when the PatientActivity
     * resumes after the user returns from 
     * another activity. Opens a DB connection 
     * if one is not already in existence.
     */
    @Override
    protected void onResume() {
    	if (!mDbHelper.isOpen())
    		mDbHelper.open();
    	super.onResume();
    }

    /**
     * Catches the event when the user leaves
     * PatientActivity or exits the app. 
     * Closes the DB connection.
     */
    @Override
    protected void onPause() {
    	mDbHelper.close();
    	super.onPause();
    }

    /**
     * A FragmentStatePagerAdapter that returns a ListFragment
     * representing the list of the Patient's vital signs
     * or a simple Fragment representing Patient's info.
     */
    public class PatientInformationPagerAdapter extends FragmentStatePagerAdapter {

    	/** The number of fragments in this adapter. 
    	 * Changes based on the type of User viewing the
    	 * PatientActivity screen composed of these fragments. 
    	 * "numberOfTabs" is set on creation of the Activity.*/
    	public final int NUM_FRAGMENTS = numberOfTabs;
    	
    	/** The PatientInfoTabFragment within this Activity */
    	PatientInfoTabFragment infoFragment;
    	
    	/** The VitalsListFragment within this Activity */
    	VitalsListFragment vitalsFragment;
    	
    	/** The PrescriptionListFragment within this Activity */
    	PrescriptionListFragment prescriptionsFragment;

    	public PatientInformationPagerAdapter(FragmentManager fm) {
    		super(fm);
    	}

    	/**
    	 * Creates a new instance of PatientInfoTabFragment, VitalsListFragment 
    	 * or PrescriptionListFragment (for Physicians) at this tab position.
    	 * @param position The Position within this adapter
    	 * @return the appropriate Fragment associated with a specified position.
    	 */
    	@Override
    	public Fragment getItem(int position) {
    		//Return the Fragment associated with a specified position.
    		if (position == 0){
    			infoFragment = PatientInfoTabFragment.newInstance(erAdmin, healthCardNumber);
    			return infoFragment;
    		} else if (position == 1){
    			vitalsFragment = VitalsListFragment.newInstance(erAdmin, healthCardNumber);
    			return vitalsFragment;
    		} else { //position == 2
    			prescriptionsFragment = PrescriptionListFragment.newInstance(erAdmin, healthCardNumber);
    			return prescriptionsFragment;
    		}
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
