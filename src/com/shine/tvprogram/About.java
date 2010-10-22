package com.shine.tvprogram;

import android.app.Activity;
import android.os.Bundle;

public class About extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		try {
			super.onCreate(savedInstanceState);
			setContentView(R.layout.about);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}
