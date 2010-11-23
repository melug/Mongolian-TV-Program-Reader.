package com.shine.tvprogram.defer;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;

public class DeferredTest extends ThreadedActivity {
    /** Called when the activity is first created. */
    final int INFINIT_DIALOG = 1;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*setContentView(R.layout.main);
        showDialog(INFINIT_DIALOG);
        startThread(new BackgroundThread() {
			@Override
			public Object work() {
				for(int i=0; i<10;i++) {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				return null;
			}
			@Override
			public void onFinish(Object result) {
				// TODO Auto-generated method stub
				dismissDialog(INFINIT_DIALOG);
				alert("Finished!");
			}
        });*/
    }
    
    @Override
    protected Dialog onCreateDialog(int id) {
    	// TODO Auto-generated method stub
    	switch(id) {
    		case INFINIT_DIALOG: {
    			ProgressDialog pDialog = new ProgressDialog(DeferredTest.this);
    			pDialog.setMessage("I am working, man.");
    			return pDialog;
    		}
    	}
    	return super.onCreateDialog(id);
    }
}