package com.shine.tvprogram;

import android.app.ListActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class ProgramList extends ListActivity {
	 /** Called when the activity is first created. */
	static final String[] TVPrograms = new String[] {
		"08.30   \"Өглөө\" хөтөлбөр",
		"09.30   Амьдралын үргэлжлэл",
		"10.00   \"Алим\"хүүхдийн хөтөлбөр",
		"10.30   \"Ярилцъя\" /шууд /",
		"11.30   Баянхонгор аймаг -Өнөөдөр",
		"12.00   Хонгор нутгийн багачуудын аялгуу",
		"12.30   Эргэх дөрвөн цаг",
		"13.00   Мэдээ",
		"13.10   Баянхонгор аймгийн ХЖТ-ын концерт",
		"14.00   Орон нутаг дахь боловсрол",
		"14.20   Үзэгч, сонсогч, редакцийн уулзалт",
		"15.00   Мэдээ",
		"15.10   \"Соёл-Эрдэнэ\" теле сэтгүүл",
		"16.00   \"Ардын дуу\"цэнгээнт нэвтрүүлэг",
		"17.00    Парламентын хэлэлцүүлэг",
		"18.00   Мэдээ",
		"18.10   Хөгжлийн тухай яриа",
		"18.40   Аяллын цаг",
		"19.20   \"Неу Неу\"цэнгээнт нэвтрүүлэг",
		"20.00   \"Цагийн хүрд\"мэдээллийн хөтөлбөр",
		"21.00   Эзэнгүйдэл: Алт тойрсон асуудал...",
		"21.40   \"Залуус\" хөтөлбөр",
		"22.30   \"ИЛҮҮ САРТАЙ ЗУН\" У.С.К",
		"00.00   Үдшийн мэдээ",
		"00.10   \"Гэрэлтэй цонх\" хөтөлбөр",
	  };
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
	  super.onCreate(savedInstanceState);

	  setListAdapter(new ArrayAdapter<String>(this, R.layout.tv_item, TVPrograms));

	  ListView lv = getListView();
	  lv.setTextFilterEnabled(true);

	  lv.setOnItemClickListener(new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {

		      // When clicked, show a toast with the TextView text
		      Toast.makeText(getApplicationContext(), ((TextView) arg1).getText(),
		          Toast.LENGTH_SHORT).show();
		}
	  });
	}
}
