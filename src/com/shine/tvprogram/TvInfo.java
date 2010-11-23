package com.shine.tvprogram;

import java.io.ByteArrayInputStream;

import com.shine.tvprogram.db.ProgramDatabase;
import com.shine.tvprogram.threads.ContentUpdater;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.database.Cursor;
import android.database.SQLException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

public class TvInfo extends Activity {
	SimpleCursorAdapter programListCursorAdapter, channelListCursorAdapter;
	Cursor channelsCursor, programsCursor;
	ListView programListView;
	Gallery channelListView;
	View homeView;
	
	private ContentUpdater contentUpdater;
	private ProgramDatabase db;
	private final String tag = "TvInfo";
	private int currentDay;
	private Integer selectedDay = currentDay = DateHelper.getTodayOfWeek();
	private Long currentChannelId;
	private Long selectedProgramId;
	/*
	 * isDebug их хэрэгтэй хувьсагч шүү. хэрэглэсэн газруудыг нь хараарай.
	 * Жижигхэн өөрчлөлт эд нарыг дебаг ашиглаж шалгах нь үнэхээр ядаргаатай.
	 * Цаг үрсэн ажил тиймээс энэ хувьсагчийн утгыг шалгаад Toast message явуулах
	 * хэрэгтэй. Яг түгээх үедээ isDebug = false; болгоорой.
	 */
	static final boolean isDebug = true;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tv_info);
		try {
			db = new ProgramDatabase(TvInfo.this);
			db.openAsRead();
			programListView = (ListView) findViewById(R.id.program_list_view);
			channelListView = (Gallery) findViewById(R.id.channel_list_view);
			this.
			homeView = findViewById(R.id.home_view);
			programListView.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> arg0, View view, int position,
						long id) {
					selectedProgramId = id;
					showDialog(ALARM_DIALOG);
				}
			});
			channelListView.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> arg0, View view, int position,
						long id) {
					if(homeView.getVisibility() != View.GONE) {
						homeView.setVisibility(View.GONE);
					}
					currentChannelId = id;
					fillPrograms(id);
				}
			});
			fillChannels();
		} catch (SQLException e) {
			if(TvInfo.isDebug) {
				alert(e.toString());
			} else {
				alert("Яачив? :)");
			}
		}
	}
	
	void fillPrograms() {
		fillPrograms(currentChannelId);
	}
	
	void fillPrograms(Long channelId) {
		if(channelId == null) {
			return;
		}
		if(programsCursor != null) {
			programsCursor.close();
		}
		programsCursor = db.getPrograms(channelId, selectedDay);
		programListCursorAdapter = new SimpleCursorAdapter(this, R.layout.programs_entry, programsCursor, 
				new String[] { "time_to_air", "program_name" }, 
				new int[] { R.id.time, R.id.program_name });
		
		programListView.setAdapter(programListCursorAdapter);
	}
	
	void fillChannels() {
		if(channelsCursor != null) {
			channelsCursor.close();
		}
		channelsCursor = db.getAllChannels();
		channelListCursorAdapter = new SimpleCursorAdapter(this, R.layout.channels_entry, channelsCursor, 
				new String[] { "channel_name", "img_data" }, 
				new int[] { R.id.channel_name, R.id.channel_icon }) {
			public void bindView (View view, Context context, Cursor cursor) {
				//Android's adapter class is not extensible enough.
				//I wanted to set ViewBinder, but before ViewBinder is used 
				//it tried to convert Blob data to String that throws some weird 
				//exception. So that I overrided bindView.
				try {
					Long channelId = cursor.getLong(cursor.getColumnIndex("_id"));
					TextView channelNameView = (TextView)view.findViewById(R.id.channel_name);
					ImageView iconView = (ImageView)view.findViewById(R.id.channel_icon);
					TextView currentProgram = (TextView)view.findViewById(R.id.program_now);
					String channelName = cursor.getString(cursor.getColumnIndex("channel_name"));
					byte[] imageBytes = cursor.getBlob(cursor.getColumnIndex("img_data"));
					channelNameView.setText(channelName);
					if(imageBytes != null) {
						ByteArrayInputStream imageStream = new ByteArrayInputStream(imageBytes);
						Bitmap theImage = BitmapFactory.decodeStream(imageStream);
						iconView.setImageBitmap(theImage);
						channelNameView.setVisibility(View.GONE);
					}
					Cursor currentProgramCursor = db.getCurrentProgram(channelId);
					if(currentProgramCursor.getCount() == 1) {
						String programName = currentProgramCursor.getString(currentProgramCursor.getColumnIndex("program_name"));
						currentProgram.setText(programName);
					} else {
						currentProgram.setText("нэвтрүүлэг алга.");
					}
				} catch (Exception e) {
					if(TvInfo.isDebug) {
						alert(e.toString());
					} else {
						alert("Алдаа гарчихлаа даа.");
					}
				}
			}
		};
		channelListView.setAdapter(channelListCursorAdapter);
	}

	/**
	 * res/menu/programs_menu.xml-ийг үз.
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.programs_menu, menu);
	    return true;
	}
	
	final int DOWNLOAD_PROGRESS_DIALOG = 0;
	final int ALARM_DIALOG = 1;
	
	ProgressDialog downloadProgress;
	/*
	 * Хоёрхон төрлийн диалог.
	 * 	1. Татах явцыг үзүүлэх
	 *  2. Сэрүүлэг тавих үед
	 */
	@Override
	protected Dialog onCreateDialog(int id) {
		switch(id) {
			case DOWNLOAD_PROGRESS_DIALOG: {
				downloadProgress = new ProgressDialog(TvInfo.this);
				downloadProgress.setMessage("Татаж байна.");
				downloadProgress.setCancelable(false);
				return downloadProgress;
			}
			case ALARM_DIALOG: {
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setMessage("Би энэ нэвтрүүлэгт дуртай")
			       .setCancelable(false)
			       .setPositiveButton("Тийм", new DialogInterface.OnClickListener() {
			           public void onClick(DialogInterface dialog, int id) {
			        	   try {
			        		   setReminder();
			        	   } catch(Exception e) {
			        		   alert(e.toString());
			        	   }
			           }
			       })
			       .setNegativeButton("Үгүй", new DialogInterface.OnClickListener() {
			           public void onClick(DialogInterface dialog, int id) {
			                dialog.cancel();
			           }
			       });
				AlertDialog alert = builder.create();
				return alert;
			}
			
		}
		return super.onCreateDialog(id);
	}
	
	public void setReminder() {
		setReminder(selectedProgramId);
	}
	
	public void setReminder(Long programId) {
		   /* PendingIntent бүр өөрийн гэсэн дугаартай байна. Манай тохиолдолд ТВ мэдээг
		    * баазад хадгалж байгаа учраас нэвтрүүлэг бүр өөрийн харгалзах ID-тай гэвч
		    * тэр нь Long төрлийнх нь харин PendingIntent-ийн ID дугаар нь Integer төрөлтэй
		    * учраас Long-ийг аль болох давхцуулахгүйгээр Integer төрөл рүү хөрвүүлэх хэрэгтэй
		    * болно. Иймд PendingIntent-ийн ID дугаарыг гаргаж авахдаа нэвтрүүлгийн ID дугаарыг
		    * хамгийн их бүхэл тоонд үлдэгдэлтэй хувааж гаргаж авсан.
		    * PendingIntent.id = TvProgram.id % Integer.MAX_VALUE
		    */
		   db.setReminderOn(programId);
		   Intent favIntent = new Intent(TvInfo.this, AlarmReceiver.class);
		   favIntent.putExtra("programId", programId);

		   PendingIntent alarm = PendingIntent.getBroadcast(TvInfo.this, (int)(programId % Integer.MAX_VALUE), 
				   favIntent, PendingIntent.FLAG_ONE_SHOT);
		   
		   AlarmManager alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
		   
		   Cursor favProgramCursor = db.getProgram(programId);
		   long rightNow = System.currentTimeMillis();
		   long programTime = DateHelper.getTimeOfProgram(
				   favProgramCursor.getInt(favProgramCursor.getColumnIndex("day")),
				   favProgramCursor.getString(favProgramCursor.getColumnIndex("time_to_air")));
		   
		   long fromNow = (programTime - rightNow) / (1000 * 60);
		   if (fromNow > 0) {
			   alarmManager.set(AlarmManager.RTC_WAKEUP, programTime /*rightNow + 1 * 1000*/, 
					   alarm);
		   } else {
			   alert("Уучлаарай, гараад өнгөрсөн нэвтрүүлэг байна :(");
		   }
		   favProgramCursor.close();
		   
		   if(TvInfo.isDebug) {
			   alert("from now(in minute): " + fromNow);
		   }
	}
	/**
	 * Доорх кодонд өдрүүдийг интервалаар шалгаж болох байсан л даа. Гэхдээ
	 * өдрүүд нь анхнаасаа тодорхой утга заагдсан тогтмолууд байсан
	 * болохоор интервалаар шалгаагүй. Мөн зарим нэг календар
	 * дээр 1-ийг Ням гариг гээд, эсвэл 1-ийг Даваа гариг гээд 
	 * толгой эргүүлээд байсан болохоор тэрийг бодолгүй шууд тогтмол
	 * утгатай нь харгалзуулаад хийсэн.
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int day = -1;
		switch(item.getItemId()) {
			case R.id.monday: {
				alert("Даваа");
				day = 1;
				break;
			}
			case R.id.tuesday: {
				day = 2;
				alert("Мягмар");
				break;
			}
			case R.id.wednesday: {
				day = 3;
				alert("Лхагва");
				break;
			}
			case R.id.thursday: {
				day = 4;
				alert("Пүрэв");
				break;
			}
			case R.id.friday: {
				day = 5;
				alert("Баасан");
				break;
			}
			case R.id.saturday: {
				day = 6;
				alert("Бямба");
				break;
			}
			case R.id.sunday: {
				day = 7;
				alert("Ням");
				break;
			}
			case R.id.download_program: {
				showDialog(DOWNLOAD_PROGRESS_DIALOG);
				contentUpdater = new ContentUpdater(handler, db);
				contentUpdater.start();
				break;
			}
			case R.id.search_program: {
				onSearchRequested();
				break;
			}
			case R.id.favourite_programs: {
     		    Intent favIntent = new Intent(TvInfo.this, FavouriteList.class);
     		    startActivity(favIntent);
     		    break;
			}
		}
		if(day != -1) {
			selectedDay = day;
			fillPrograms();
		}
		return true;
	}
	/**
	 * Бусад арын процессууд ажиллах боломжтой. Тэдгээр нь
	 * өөрсдөө статусаа үндсэн процесс руу мэдээллэх шаардлагатай.
	 * Тэрнээс биш байн байн үндсэн процесс нь бусдыгаа шалгах нь
	 * төвөгтэй байв.
	 * 
	 * Арын процесс болох татах ажиллагаа нь үндсэндээ 2 хэсгээс бүрднэ.
	 * 	1. Үндсэн мэдээллийг татах. http://tvmongolier.appspot.com/tvfeed/json
	 *  Үндсэн мэдээлэл нь телевизүүдийн нэвтрүүлгийн болон, сувгуудийн
	 *  мэдээллийг агуулсан текст хэлбэртэй. HTML, JSON, XML биш! Эдгээр
	 *  форматууд нь ажиллагааг маш удаан болгож байсан. Хамгийн хурдан
	 *  гэж бодсон JSON дээр хүртэл 500 гаруй килобайт текстэн мэдээлэл
	 *  татуулж байв.
	 *  2. Хадгална. Хадгалах явцад сувгуудийн мета мэдээлэл дундаас
	 *  зургийн URL-ийг ялгаж аваад мөн татаж хадгална.
	 */
	final Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			Bundle b = msg.getData();
			String status = b.getString("status");
			if(ContentUpdater.DOWNLOAD_FINISHED.equals(status)) {
				downloadProgress.setMessage("Татаж дууслаа, одоо хадгалж байна.");
			} else if(ContentUpdater.DOWNLOAD_FAILED.equals(status)) {
				dismissDialog(DOWNLOAD_PROGRESS_DIALOG);
				if(TvInfo.isDebug) {
					alert(b.getString("reason"));
				} else {
					alert("Амжилтгүй боллоо.");
				}
			} else if(ContentUpdater.DOWNLOAD_PROGRESS.equals(status)) {
				int progress = b.getInt(ContentUpdater.PROGRESS);
				downloadProgress.setMessage("Татаж байна. " + progress + "кб");
			} else if(ContentUpdater.SAVING.equals(status)) {
				String channel = b.getString("channel");
				downloadProgress.setMessage(channel + "-ийг хадгалж байна");
			} else if(ContentUpdater.SAVED.equals(status)) {
				dismissDialog(DOWNLOAD_PROGRESS_DIALOG);
				fillChannels();
			}
		};
	};
	/**
	 * Жижигхэн туслах функц.
	 * @param alertText
	 */
	public void alert(String alertText) {
		Toast.makeText(TvInfo.this, alertText, Toast.LENGTH_LONG).show();
	}
}
