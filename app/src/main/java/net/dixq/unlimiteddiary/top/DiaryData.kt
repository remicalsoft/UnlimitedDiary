package net.dixq.unlimiteddiary.top

import android.graphics.Color
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.api.services.drive.model.File
import net.dixq.unlimiteddiary.utils.Lg
import java.util.*

data class DiaryData(val isMonthLine: Boolean) {

    var year: Int = 0
    var month: Int = 0
    var day: Int = 0
    var hour: Int = 0
    var min: Int = 0
    var sec: Int = 0
    var mill: Int = 0
    var revision: Int = 0
    var title: String = ""
    var body: String = ""
    var author: String = ""
    var color: String = Color.WHITE.toString()
    var fileId: String = ""

    constructor(isMonthLine: Boolean, year:Int, month:Int) : this(isMonthLine) {
        this.year = year
        this.month = month
    }
    constructor(isMonthLine: Boolean, title:String, body:String) : this(isMonthLine){
        this.title = title
        this.body = body
    }
    fun equalAsMonth(dat: DiaryData): Boolean {
        return dat.year == year && dat.month == month
    }
    fun setNowTime(){
        val cl: Calendar = Calendar.getInstance()
        year  = cl.get(Calendar.YEAR)
        month = cl.get(Calendar.MONTH)+1
        day = cl.get(Calendar.DATE)
        hour = cl.get(Calendar.HOUR_OF_DAY)
        min = cl.get(Calendar.MINUTE)
        sec = cl.get(Calendar.SECOND)
        mill = (System.currentTimeMillis() % 1000).toInt()
    }
    fun getFileName(): String {
        return String.format("%04d.%02d.%02d.%02d.%02d.%d.%d.txt", year, month, day, hour, min, sec, mill)
    }
    fun proceedRevision(){
        revision++
    }
    fun setFromJson(json:String){
        val mapper = ObjectMapper()
        try {
            val node = mapper.readTree(json);

            year = node.get("year").asInt()
            month = node.get("month").asInt()
            day = node.get("day").asInt()
            hour = node.get("hour").asInt()
            min = node.get("min").asInt()
            sec = node.get("sec").asInt()
            mill = node.get("mill").asInt()
            revision = node.get("revision").asInt()
            title = node.get("title").asText()
            body = node.get("body").asText()
            author = node.get("author").asText()
            color = node.get("color").asText()
            fileId = node.get("fileId").asText()

        } catch (e:Exception) {
            Lg.e("json exception : "+e.message)
        }
    }
    fun isNewPostData():Boolean {
        return year==0
    }
}
