package net.dixq.unlimiteddiary.top

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class DiaryData(val isDayCell:Boolean) : Parcelable {
    var year: Int = 0
    var month: Int = 0
    var day: Int = 0
    var hour: Int = 0
    var min: Int = 0
    var count: Int = 0
    var revision: Int = 0
    var title: String = ""
    var body: String = ""

    fun equalAsMonth(dat: DiaryData): Boolean {
        return dat.year == year && dat.month == month
    }

}