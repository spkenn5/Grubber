package skripsi.com.grubber.timeline;

import android.app.Application;

import com.parse.Parse;
import com.parse.ParseACL;
import com.parse.ParseUser;

public class ParseApplication extends Application {
	
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		
		// Add your initialization code here
		Parse.initialize(this, "SSEgk1NJvwGKT0TOtULPmBQLlcMhNrr4XJ46JiVd", "n0fAB1BJRh2rPssFibGrYKASrGCjmNCZpjdzGQBz");
		
		ParseUser.enableAutomaticUser();
        ParseACL defaultACL = new ParseACL();
 
        // If you would like all objects to be private by default, remove this line.
        defaultACL.setPublicReadAccess(true);
 
        ParseACL.setDefaultACL(defaultACL, true);
	}
}
