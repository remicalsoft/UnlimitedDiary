package net.dixq.unlimiteddiary.top;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import net.dixq.unlimiteddiary.R;
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
        if(_list.get(position).isMonthLine()) {
            convertView = _inflater.inflate(R.layout.row_month_line, parent, false);
            ((TextView)convertView.findViewById(R.id.txt_month)).setText(_list.get(position).getYear() +"/"+ _list.get(position).getMonth());
        } else {
            DiaryData dat = _list.get(position);
            String dayOfWeek = "("+ CalendarUtils.getDatOfWeek(dat.getYear(), dat.getMonth() -1, dat.getDay()) +")";
            convertView = _inflater.inflate(R.layout.row_diary, parent, false);
            TextView textView = (TextView)convertView.findViewById(R.id.txt_day);
            textView.setText(String.valueOf(dat.getDay()));
            ((TextView)convertView.findViewById(R.id.txt_dayofweek)).setText(dayOfWeek);
            ((TextView)convertView.findViewById(R.id.txt_title)).setText(dat.getTitle());
            ((TextView)convertView.findViewById(R.id.txt_body)).setText(dat.getBody());
            if(dat.getTitle().isEmpty()){
                ((TextView)convertView.findViewById(R.id.txt_title)).setVisibility(View.GONE);
            } else {
                ((TextView)convertView.findViewById(R.id.txt_title)).setVisibility(View.VISIBLE);
            }
        }
        return convertView;
    }
}
