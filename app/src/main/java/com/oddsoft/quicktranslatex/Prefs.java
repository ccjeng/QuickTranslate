package com.oddsoft.quicktranslatex;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.view.MenuItem;

public class Prefs extends PreferenceActivity {
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.settings);

		getActionBar().setDisplayHomeAsUpEnabled(true);

	      // Get the custom preference
	  /*     Preference aboutPref = (Preference) findPreference("about");
	      aboutPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
	                       public boolean onPreferenceClick(Preference preference) {
	                    	   startActivity(new Intent(Prefs.this, AboutActivity.class));
	                                 return true;
	                              }
	                      });
	      // Get the custom preference
	     Preference upgradePref = (Preference) findPreference("upgrade");
	      upgradePref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
	                       public boolean onPreferenceClick(Preference preference) {
	                    	   		linkMarket();
	                                 return true;
	                             }
	                      });
	                      */
	}
	
	private void linkMarket() {
		Uri uri = Uri.parse("market://details?id=com.oddsoft.quicktranslatepro");
		startActivity( new Intent( Intent.ACTION_VIEW, uri ) );
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
			case android.R.id.home:
				finish();
				return true;
		}

		return super.onOptionsItemSelected(item);
	}

}
