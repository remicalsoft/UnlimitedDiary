package net.dixq.unlimiteddiary.top

import android.content.Context
import android.graphics.Color
import com.fasterxml.jackson.databind.ObjectMapper
import net.dixq.unlimiteddiary.common.Lg
import java.util.*

data class DiaryData(val isMonthLine: Boolean) {

    val INIT_VALUE = 9999
    var year: Int = INIT_VALUE
    var month: Int = INIT_VALUE
    var day: Int = INIT_VALUE
    var hour: Int = INIT_VALUE
    var min: Int = INIT_VALUE
    var sec: Int = INIT_VALUE
    var mill: Int = INIT_VALUE
    var revision: Int = 0
    var title: String = ""
    var body: String = ""
    var author: String = ""
    var color: String = Color.WHITE.toString()
    var fileId: String = "-1"
    var isJustNowFound = false
    var jpegFile:Long = 0 // ファイルの存在がbitフラグになっている。1つ目のファイルがあれば0b0001,それに加えて2つ目のファイルがあれば0b0011、1つ目のファイルが消えたら0b0010

    constructor(isMonthLine: Boolean, year:Int, month:Int) : this(isMonthLine) {
        this.year = year
        this.month = month
    }
    constructor(isMonthLine: Boolean, title:String, body:String) : this(isMonthLine){
        this.title = title
        this.body = body
    }
    constructor(isMonthLine: Boolean, json:String) : this(isMonthLine){
        setFromJson(json)
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
        return String.format("%04d.%02d.%02d.%02d.%02d.%d.%d.%d.%d.txt", year, month, day, hour, min, sec, mill, revision, jpegFile)
    }
    fun getJpegFileName(num:Int):String {
        return String.format("%04d.%02d.%02d.%02d.%02d.%d.%d", year, month, day, hour, min, sec, mill)+ "."+num+".jpg"
    }
    fun getJpegFilePath(context:Context, num:Int):String {
        return context.getExternalFilesDir(null)!!.path + "/" + getJpegFileName(num)
    }
    fun getJpegFileNames():LinkedList<String> {
        val list = LinkedList<String>()
        for(i in 0..63){
            if(jpegFile and (1.toLong() shl i) != 0.toLong()){
                list.add(getJpegFileName(i))
            }
        }
        return list
    }
    fun getJpegFilePaths(context:Context):LinkedList<String> {
        val list = LinkedList<String>()
        for(i in 0..63){
            if(jpegFile and (1.toLong() shl i) != 0.toLong()){
                list.add(getJpegFilePath(context, i))
            }
        }
        return list
    }
    fun getMinJpegFileName():String? {
        val min = getMinJpegFileIndex()
        if(min==-1){
            return null
        }
        return  getJpegFileName(min)
    }
    fun getMinJpegFileIndex():Int {
        for(i in 0..63){
            if(jpegFile and (1.toLong() shl i) != 0.toLong()){
                return i
            }
        }
        return -1
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
            jpegFile = node.get("jpegFile").asLong()

        } catch (e:Exception) {
            Lg.e("json exception : "+e.message)
        }
    }
    fun isNewPostData():Boolean {
        return year==INIT_VALUE
    }
    fun equals(fileName:String):Boolean {
        val strs = fileName.split(".")
        val year = strs[0].toInt()
        val month = strs[1].toInt()
        val day = strs[2].toInt()
        val hour = strs[3].toInt()
        val min = strs[4].toInt()
        val sec = strs[5].toInt()
        val mill = strs[6].toInt()
        val rev = strs[7].toInt()
        val jpg = strs[8].toLong()
        if(this.year == year && this.month == month && this.day == day && this.hour == hour && this.min == min && this.sec == sec && this.mill == mill && this.revision == rev && this.jpegFile == jpg){
            return true
        }
        return false
    }
    fun equalsOnlyDate(fileName:String):Boolean {
        val strs = fileName.split(".")
        val year = strs[0].toInt()
        val month = strs[1].toInt()
        val day = strs[2].toInt()
        val hour = strs[3].toInt()
        val min = strs[4].toInt()
        val sec = strs[5].toInt()
        val mill = strs[6].toInt()
        if(this.year == year && this.month == month && this.day == day && this.hour == hour && this.min == min && this.sec == sec && this.mill == mill){
            return true
        }
        return false
    }
    fun addJpegFile(){
        var max = 0
        for(i in 0..63){
            if(jpegFile and (1.toLong() shl i) != 0.toLong()){
                max = i
            }
        }
        jpegFile = jpegFile or (1.toLong() shl max)
    }

    companion object {
        fun existsJpegFile(fileName: String): Boolean {
            val strs = fileName.split(".")
            if (strs[8].toLong() != 0.toLong()) {
                return true
            }
            return false
        }
    }
}
