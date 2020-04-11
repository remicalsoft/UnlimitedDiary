package net.dixq.unlimiteddiary.top

import com.google.api.services.drive.model.File
import java.util.*

class DiaryData(val isMonthLine: Boolean){

    var year: Int = 0
    var month: Int = 0
    var day: Int = 0
    var hour: Int = 0
    var min: Int = 0
    var count: Int = 0
    var revision: Int = 0
    var title: String = ""
    var body: String = ""
    var file: File? = null

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
    }
    fun getConvinedString():String{
        if(title.isEmpty()){
            return body
        }
        return "<title>$title</title>\n$body"
    }
    fun getFileName(): String {
        return String.format("%04d.%02d.%02d.%02d.%02d.%d.%d.txt", year, month, day, hour, min, count, revision)
    }
    fun proceedRevision(){
        revision++
    }
    fun getFileNamePreRevision(): String {
        return String.format("%04d.%02d.%02d.%02d.%02d.%d.%d.txt", year, month, day, hour, min, count, revision-1)
    }

}
