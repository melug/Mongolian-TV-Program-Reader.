package com.shine.tvprogram.defer;

import java.util.HashMap;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

public class ThreadedActivity extends Activity {

    private BackgroundHandler mainHandler = new BackgroundHandler();
 
    public void startThread(BackgroundThread backgroundProcess) {
    	backgroundProcess.setHandler(mainHandler);
    	backgroundProcess.start();
    }

	public void alert(String alertText) {
		Toast.makeText(this, alertText, Toast.LENGTH_SHORT).show();
	}
}

class BackgroundHandler extends Handler {
   	HashMap<Integer, BackgroundThread> threads = new HashMap<Integer, BackgroundThread>();
   	
	public void addThread(BackgroundThread bThread) {
		threads.put(bThread.hashCode(), bThread);
	}
	
	public BackgroundThread getThread(int threadId) {
		return threads.get(threadId);
	}
	
	@Override
	public void handleMessage(Message msg) {
		Bundle b = msg.getData();
		Integer threadId = b.getInt("threadId");
		
		BackgroundThread bThread = getThread(threadId);
		bThread.onFinish(bThread.workResult);
		threads.remove(threadId);
	}
}

abstract class BackgroundThread extends Thread {
	private BackgroundHandler bHandler;
	public Object workResult;
	
	//override me.
	public abstract Object work();
	//I want to run in main thread, after work() has finished.
	public abstract void onFinish(Object result);
	
	public void run() {
		workResult = work();
		//I finished my work, tell handler to run my onFinish method.
		Message msg = bHandler.obtainMessage();
		Bundle bundle = new Bundle();
		bundle.putInt("threadId", this.hashCode());
		msg.setData(bundle);
		bHandler.addThread(this);
		bHandler.sendMessage(msg);
	}
	
	public void setHandler(BackgroundHandler mHandler) {
		this.bHandler = mHandler;
	}
}