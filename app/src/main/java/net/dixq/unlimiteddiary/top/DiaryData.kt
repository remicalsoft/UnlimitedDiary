package net.dixq.unlimiteddiary.top

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.util.*

@Parcelize
data class DiaryData(val isMonthLine: Boolean) : Parcelable {
    var year: Int = 0
    var month: Int = 0
    var day: Int = 0
    var hour: Int = 0
    var min: Int = 0
    var count: Int = 0
    var revision: Int = 0
    var title: String = ""
    var body: String = ""

    constructor(isMonthLine: Boolean, year:Int, month:Int) : this(isMonthLine) {
        this.year = year
        this.month = month
    }

    fun equalAsMonth(dat: DiaryData): Boolean {
        return dat.year == year && dat.month == month
    }

    fun setNowTime(){
        val cl: Calendar = Calendar.getInstance()
        year  = cl.get(Calendar.YEAR)
        month = cl.get(Calendar.MONTH)+1
        day = cl.get(Calendar.DATE)
        hour = cl.get(Calendar.HOUR)
        min = cl.get(Calendar.MINUTE)
    }

    companion object {
        public val TAG = "DiaryDate"
    }

}