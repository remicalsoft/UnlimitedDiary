package net.dixq.unlimiteddiary.top

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import net.dixq.unlimiteddiary.R
import net.dixq.unlimiteddiary.common.CalendarUtils

class ItemAdapter(private val _context: Context, list: List<DiaryData>) : BaseAdapter() {

    private val _inflater: LayoutInflater = _context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    private var _list: List<DiaryData>

    fun setList(list: List<DiaryData>) {
        _list = list
    }

    override fun getCount(): Int {
        return _list.size
    }

    override fun getItem(position: Int): Any {
        return _list[position]
    }

    override fun getItemId(position: Int): Long {
        return 0
    }

    override fun getView(
        position: Int,
        convertView: View?,
        parent: ViewGroup
    ): View {
        var convertView:View? = convertView
        if (_list[position].isMonthLine) {
            convertView = _inflater.inflate(R.layout.row_month_line, parent, false)
            convertView.findViewById<TextView>(R.id.txt_month).text = _list[position].year.toString() + "/" + _list[position].month
        } else {
            val dat = _list[position]
            val dayOfWeek ="(" + CalendarUtils.getDatOfWeek(dat.year,dat.month - 1, dat.day) + ")"
            convertView = _inflater.inflate(R.layout.row_diary, parent, false)
            val textView = convertView.findViewById<TextView>(R.id.txt_day)
            textView.text = dat.day.toString()
            convertView.findViewById<TextView>(R.id.txt_dayofweek).text = dayOfWeek
            convertView.findViewById<TextView>(R.id.txt_title).text = dat.title
            convertView.findViewById<TextView>(R.id.txt_body).text = dat.body
            convertView.findViewById<TextView>(R.id.txt_time).text = String.format("%02d:%02d", dat.hour, dat.min)
            val author = convertView.findViewById<TextView>(R.id.txt_author)
            author.text = dat.author
            if (dat.color.isNotEmpty()) {
                author.setBackgroundColor(dat.color.toInt())
            }
            if (dat.title.isEmpty()) {
                (convertView.findViewById<View>(R.id.txt_title) as TextView).visibility = View.GONE
            } else {
                (convertView.findViewById<View>(R.id.txt_title) as TextView).visibility =
                    View.VISIBLE
            }
            if (_list[position].isJustNowFound) {
                _list[position].isJustNowFound = false
                val colorFrom = _context.getColor(R.color.md_deep_orange_600)
                val colorTo = Color.TRANSPARENT
                val colorAnimation = ValueAnimator.ofObject(ArgbEvaluator(), colorFrom, colorTo)
                colorAnimation.duration = 1000
                val finalConvertView = convertView
                colorAnimation.addUpdateListener { animator: ValueAnimator ->
                    finalConvertView.setBackgroundColor(animator.animatedValue as Int)
                }
                colorAnimation.start()
            }
        }
        return convertView
    }

    init {
        _list = list
    }
}