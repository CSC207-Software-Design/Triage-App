package triageapp.activities;

import java.io.File;

import triageapp.components.ERAdmin;
import triageapp.components.InvalidUserInputException;
import triageapp.database.TriageDBAdapter;
import triageapp.user.User;
import triageapp.user.UserManager;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;


/** The Activity to login into the Application. */
public class LoginActivity extends Activity {
	
	/** This LoginActivity's ERAdmin. */
	private ERAdmin erAdmin;

	/** This LoginActivity's UserManager. */
	private UserManager userManager;
	
	/** This LoginActivity's DB Adapter used to save data to the DB*/
	private TriageDBAdapter mDbHelper;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login_activity);
		//Sets the screen orientation to portrait.
		setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		mDbHelper = new TriageDBAdapter(this);
		erAdmin = new ERAdmin();
		//Creates the User Manager.
		userManager = new UserManager(); 
		//Creates the ERAdmin, and loads all current patient data.
		File database = getApplicationContext().getDatabasePath(TriageDBAdapter.DATABASE_NAME);
		if (!database.exists()) {
			mDbHelper.open(); //Creates the database.
			try {
				erAdmin.initialLoadPatients(mDbHelper);
				userManager.loadUserInformation(mDbHelper);
			} catch (InvalidUserInputException e) {
				e.printStackTrace();
			} 
		} else {
			mDbHelper.open(); //Opens the database.
			try {
				erAdmin.loadPatients(mDbHelper);
				erAdmin.loadUrgencyList(mDbHelper);
				erAdmin.loadSentToDoctorList(mDbHelper);
			} catch (InvalidUserInputException e) {
				e.printStackTrace();
			}
			
		}


		
		
	}
	
	/**
	 * Verifies the username and password of this LoginActivity's User and  
	 * passes this LoginActivity's User and ERAdmin to MainActivity.class 
	 * via the intent.
	 * @param view A component of the User Interface.
	 */
	public void loginUser(View view) {
		Intent intent = new Intent(this, MainActivity.class);

		//Gets the username entered by the user. 
		EditText usernameText = (EditText) findViewById(R.id.username_field);
		String username = usernameText.getText().toString();

		//Gets the password entered by the user.
		EditText passwordText = (EditText) findViewById(R.id.password_field);
		String password = passwordText.getText().toString();

		//Verifies whether the username and password are correct.
		//If an actual User object is returned, the username and password
		//combination is valid and corresponds to a User listed in the 
		//passwords file.
		User user = userManager.getUser(username, password, mDbHelper);
		if (!username.matches("") && !password.matches("") && user != null) {
			intent.putExtra("user", user);
			intent.putExtra("eradmin", erAdmin);
			startActivityForResult(intent, 0);

		} else {
			// Displays incorrect username/password message.
			Toast toast = Toast.makeText(this,  getString(R.string.incorrect_login), Toast.LENGTH_SHORT);
			toast.setGravity(Gravity.BOTTOM, 0, 0);
			toast.show();
			//TextView message = (TextView) findViewById(R.id.error_message);
			//message.setText(getString(R.string.incorrect_login));
		}
	}
		
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.login_activity, menu);
		return true;
	}
	
	/** Used to handle events generated from the menu (e.g., when the user
	 * selects the "About" item).
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_about: // go to About page of this App.
			Intent i = new Intent(this, AboutActivity.class);
			startActivity(i); 
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

    /**
     * Gets the updated ERAdmin object from the previous Activity.
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode,
    		Intent intent) {
    	super.onActivityResult(requestCode, resultCode, intent);
    	erAdmin = (ERAdmin) intent.getSerializableExtra("eradmin");
    }
	
    /**
     * Catches the event when the LoginActivity
     * resumes after the user returns to this
     * Activity from another activity. 
     * Opens a DB connection if one is not
     * already in existence.
     */
    @Override
    protected void onResume() {
    	if (!mDbHelper.isOpen())
    		mDbHelper.open();
    	super.onResume();
    }

    /**
     * Catches the event when the user leaves the
     * LoginActivity to go to another activity
     * or exits the app. Closes the DB 
     * connection.
     */
    @Override
    protected void onPause() {
    	mDbHelper.close();
    	super.onPause();
    }
    
}
