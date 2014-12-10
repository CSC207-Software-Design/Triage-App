package triageapp.activities;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LevelListDrawable;
import android.os.Bundle;
import android.text.Html;
import android.text.Html.ImageGetter;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

/** Activity that displays information about the App. */
public class AboutActivity extends Activity implements ImageGetter{

	
	/** This AboutActivity's main layout */
	private LinearLayout mainLayout;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// This sets up the screen to have the About text displayed.
		ScrollView scrollView = new ScrollView(this); // scrollable page screen.
		mainLayout = new LinearLayout(this);
		mainLayout.setOrientation(LinearLayout.VERTICAL);  
		setContentView(scrollView);
		LinearLayout layout = new LinearLayout(this);
		//text view takes up the parent screen space.
		TextView text = new TextView(this);
		//Sets the text of the About page.
		//Uses html tags to style the text, and Html.fromHtml to 
		//process the HTML string into displayable styled text.
		text.setText(Html.fromHtml(getString(R.string.about), this, null));
		layout.addView(text);
		mainLayout.addView(layout);
		scrollView.addView(mainLayout);
		setContentView(scrollView);
	}
	
	/**
	 * Replaces image tags in the HTML string with
	 * images in the drawable folder.
	 */
    @Override
    public Drawable getDrawable(String arg0) {
        // TODO Auto-generated method stub.
        int id = 0;

        if(arg0.equals("ic_action_search_light.png")){
            id = R.drawable.ic_action_search_light;
        } else if (arg0.equals("ic_action_add_prescription_light.png")){
        	id = R.drawable.ic_action_add_prescription_light;
        } else if (arg0.equals("ic_action_content_add_light.png")){
        	id = R.drawable.ic_action_content_add_light;
        } else if (arg0.equals("ic_action_content_new_light.png")){
        	id = R.drawable.ic_action_content_new_light;
        } else if (arg0.equals("ic_action_file_folder_shared_light.png")){
        	id = R.drawable.ic_action_file_folder_shared_light;
        } else if (arg0.equals("ic_action_send_to_doctor_light.png")){
        	id = R.drawable.ic_action_send_to_doctor_light;
        } else if (arg0.equals("ic_action_social_person_add_light.png")){
        	id = R.drawable.ic_action_social_person_add_light;
        } else if (arg0.equals("ic_action_content_discard_light.png")){
        	id = R.drawable.ic_action_content_discard_light;
        }
        LevelListDrawable d = new LevelListDrawable();
        Drawable empty = (Drawable) getResources().getDrawable(id);
        d.addLevel(0, 0, empty);
        d.setBounds(0, 0, 60, 60);
        return d;
    }
	
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.about_activity, menu);
        return true;
    }
    
	/** Used to handle events generated from the menu (e.g., when the user
     * selects the "Close" item).
     **/
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch (item.getItemId()) {
    	case R.id.menu_back: // close this screen to go back to the Login Page.
    		finish();
    		return true;
		}
        return super.onOptionsItemSelected(item);
    }
}
