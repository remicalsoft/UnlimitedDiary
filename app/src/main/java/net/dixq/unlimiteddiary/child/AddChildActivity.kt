package net.dixq.unlimiteddiary.child

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.android.material.textfield.TextInputEditText
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog
import net.dixq.unlimiteddiary.R
import net.dixq.unlimiteddiary.common.Lg
import net.dixq.unlimiteddiary.common.OkDialog
import net.dixq.unlimiteddiary.common.PrefUtils
import java.util.*


class AddChildActivity : AppCompatActivity(), DatePickerDialog.OnDateSetListener, View.OnClickListener {

    var _year:Int = 0
    var _month:Int = 0
    var _dayOfMonth:Int = 0

    override fun onCreate(b:Bundle?) {
        super.onCreate(b)
        setContentView(R.layout.main_add_child)
        val toolbar = findViewById<Toolbar>(R.id.toolbar);
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)        // Backボタンを有効にする
        supportActionBar!!.setHomeButtonEnabled(true)
        supportActionBar!!.title = ""
        findViewById<Button>(R.id.btn_select_date).setOnClickListener(this)
        findViewById<Button>(R.id.btn_ok).setOnClickListener(this)

        val mapper = ObjectMapper()
//
//        val childList2 = LinkedList<ChildData>()
//        childList2.add(ChildData("hoge1", _year, _month, _dayOfMonth))
//        childList2.add(ChildData("hoge2", _year, _month, _dayOfMonth))
//        childList2.add(ChildData("hoge3", _year, _month, _dayOfMonth))
//        val json = mapper.writeValueAsString(childList2)
//        PrefUtils.write(this, TAG_CHILD_DATA, json)
//
        val ret = PrefUtils.read(this, TAG_CHILD_DATA)
        if(ret.isEmpty())return
        val childList = convertList(mapper.readValue(ret, Array<ChildData>::class.java))
        for(child in childList){
            Lg.e("child : "+child.toString())
        }
    }

    fun showDatePickerDialog() {
        val now: Calendar = Calendar.getInstance()
        val dpd: DatePickerDialog = DatePickerDialog.newInstance(
            this,
            now.get(Calendar.YEAR),
            now.get(Calendar.MONTH),
            now.get(Calendar.DAY_OF_MONTH)
        )
        dpd.show(fragmentManager, "Timepickerdialog")
    }

    override fun onDateSet(view: DatePickerDialog?, year: Int, monthOfYear: Int, dayOfMonth: Int) {
        _year = year
        _month = monthOfYear+1
        _dayOfMonth = dayOfMonth
        findViewById<TextView>(R.id.txt_date).text = String.format("%04d年%02d月%02d日", _year, _month, _dayOfMonth)
    }

    override fun onClick(v: View?) {
        when(v!!.id){
            R.id.btn_select_date->{
                showDatePickerDialog()
            }
            R.id.btn_ok->{
                val name = findViewById<TextInputEditText>(R.id.edt_name).text.toString()
                if(name.isEmpty() || _year==0){
                    OkDialog(this, "名前を入力し、生年月日を選択してください。", null).show()
                    return
                }
                val ret = PrefUtils.read(this, TAG_CHILD_DATA)
                val mapper = ObjectMapper()
                var childList = LinkedList<ChildData>()
                if(ret.isNotEmpty()) {
                    childList = convertList(mapper.readValue(ret, Array<ChildData>::class.java))
                }
                childList.add(ChildData(name, _year, _month, _dayOfMonth))
                val json = mapper.writeValueAsString(childList)
                PrefUtils.write(this, TAG_CHILD_DATA, json)
                finish()
            }
        }

    }

    fun convertList(array:Array<ChildData>):LinkedList<ChildData> {
        val list = LinkedList<ChildData>()
        for(c in array){
            list.add(c)
        }
        return list
    }

    companion object {
        public val TAG_CHILD_DATA = "tag_child_data"
    }
}

data class ChildData(val name:String, val year:Int, val month:Int, val dayOfMonth:Int) {
    // 空のコンストラクタがないとJsonのデコードができない
    constructor() : this("", 0, 0, 0){
    }
}
