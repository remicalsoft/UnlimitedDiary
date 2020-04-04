package net.dixq.unlimiteddiary;

public class DiaryData {
    public int _year, _month, _day, _hour, _min;
    public String _title;
    public String _body;
    boolean _isShowDay;
    public DiaryData(boolean isShowDay, int day, String body){
        _isShowDay = isShowDay;
        _day = day;
        _body = body;
    }
    public DiaryData(boolean isShowDay, int year, int month){
        _isShowDay = isShowDay;
        _year = year;
        _month = month;
    }
}
