package net.dixq.unlimiteddiary;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import net.dixq.unlimiteddiary.utils.CalendarUtils;

import java.util.List;


public class ItemAdapter extends BaseAdapter {
    private LayoutInflater _inflater;
    private List<DiaryData> _list;

    public ItemAdapter(Context context, List<DiaryData> list){
        _inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        _list = list;
    }

    @Override
    public int getCount() {
        return _list.size();
    }

    @Override
    public Object getItem(int position) {
        return _list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(_list.get(position)._isShowDay) {
            convertView = _inflater.inflate(R.layout.row_month, parent, false);
            ((TextView)convertView.findViewById(R.id.txt_month)).setText(_list.get(position)._year+"/"+_list.get(position)._month);
        } else {
            DiaryData dat = _list.get(position);
            String dayOfWeek = "("+ CalendarUtils.getDatOfWeek(dat._year, dat._month-1, dat._day) +")";
            convertView = _inflater.inflate(R.layout.row_diary, parent, false);
            TextView textView = (TextView)convertView.findViewById(R.id.txt_day);
            textView.setText(String.valueOf(dat._day));
            ((TextView)convertView.findViewById(R.id.txt_dayofweek)).setText(dayOfWeek);
            ((TextView)convertView.findViewById(R.id.txt_body)).setText(dat._body);
        }
        return convertView;
    }
}
