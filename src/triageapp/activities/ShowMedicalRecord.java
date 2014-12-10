package triageapp.activities;

import triageapp.components.*;
import triageapp.database.TriageDBAdapter;
import triageapp.user.User;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;


/** An Activity to display Patient's medical record.*/
public class ShowMedicalRecord extends Activity {

	/** This ShowMedicalRecord Activity's main layout. */
	private LinearLayout mainLayout;
	
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent previousIntent = getIntent();
		ERAdmin erAdmin = (ERAdmin) previousIntent.getSerializableExtra("eradmin");
		Patient patient = erAdmin.lookUpPatient(previousIntent.getStringExtra("healthcardnumber"));
		this.setTitle(patient.getName() + "'s " + this.getTitle());
		boolean userType = previousIntent.getBooleanExtra("usertype", true);
		if (userType == User.NURSE)
			getActionBar().setIcon(R.drawable.icon_nurse);
		else
			getActionBar().setIcon(R.drawable.triage);
		// This sets up the screen to have the Patient's ERVisits and vitals to be
		// displayed.
		ScrollView scrollView= new ScrollView(this); // scrollable page screen.
		mainLayout = new LinearLayout(this);
		mainLayout.setOrientation(LinearLayout.VERTICAL);  
		setContentView(scrollView);
		LinearLayout layout = new LinearLayout(this);
		TextView text = new TextView(this);
		//restricts the width of the textView, so that extra long prescription instructions
		//fit in a view with a width of 350 pixels.
		text.setLayoutParams(new LayoutParams(350, LayoutParams.WRAP_CONTENT));
		TriageDBAdapter mDbHelper = new TriageDBAdapter(this);
		mDbHelper.open();
		String medicalRec = erAdmin.getPatientMedicalRecord(patient, mDbHelper);
		if (medicalRec != null){
			text.setText(medicalRec);
		} else{
			text.setText("No records");
		}
		mDbHelper.close();
		layout.addView(text);
		layout.setGravity(Gravity.CENTER);
		mainLayout.addView(layout);
		scrollView.addView(mainLayout);
		setContentView(scrollView);
	}
}
