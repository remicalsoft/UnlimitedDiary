package net.dixq.unlimiteddiary.authentication

import android.app.Activity
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import androidx.appcompat.view.ContextThemeWrapper
import net.dixq.unlimiteddiary.R
import net.dixq.unlimiteddiary.utils.convertDpToPx

class Buttons : View.OnClickListener {

    private var _activity: Activity? = null
    private var _selectedId = -1
    private val _buttonArray = arrayListOf<Button>()

    fun layout(activity:Activity){
        _activity = activity
        val root = _activity!!.findViewById<LinearLayout>(R.id.layout_button_root)
        val rlpm = root.layoutParams as LinearLayout.LayoutParams
        rlpm.gravity = Gravity.CENTER
        root.layoutParams = rlpm

        createLayoutAndButtons(root, RAW_NUM*0);
        createLayoutAndButtons(root, RAW_NUM*1);
        createLayoutAndButtons(root, RAW_NUM*2);
    }

    fun getColor():Int {
        if(_selectedId==-1){
            return -1
        }
        return _activity!!.getColor(colors[_selectedId])
    }

    override fun onClick(v: View?) {
        for(i in 0 until _buttonArray.size){
            if(_buttonArray[i] == v){
                _buttonArray[i].text="✓"
                _selectedId = i
            } else {
                _buttonArray[i].text = ""
            }
        }
    }

    private fun createLayoutAndButtons(root: LinearLayout, index:Int){
        val layout = LinearLayout(_activity)
        layout.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        for(i in 0 until RAW_NUM){
            val button = createButton(index+i)
            button.setOnClickListener(this)
            layout.addView(button)
            _buttonArray.add(button)
        }
        root.addView(layout)
    }

    private fun createButton(id:Int): Button {
        val c = ContextThemeWrapper(_activity, R.style.ThemeColorButton)
        val button = Button(c)
        val dp54 = convertDpToPx(_activity!!, 54)
        val dp60 = convertDpToPx(_activity!!, 60)
        button.layoutParams = LinearLayout.LayoutParams(dp54, dp60)
        button.backgroundTintList = ColorStateList.valueOf(_activity!!.getColor(colors[id]))
        return button
    }

    companion object {
        private val RAW_NUM = 6
        private val colors = intArrayOf(
            R.color.md_red_800,
            R.color.md_pink_800,
            R.color.md_purple_800,
            R.color.md_deep_purple_800,
            R.color.md_indigo_800,
            R.color.md_blue_800,
            R.color.md_light_blue_800,
            R.color.md_cyan_800,
            R.color.md_teal_800,
            R.color.md_green_800,
            R.color.md_light_green_800,
            R.color.md_lime_800,
            R.color.md_yellow_800,
            R.color.md_amber_800,
            R.color.md_orange_800,
            R.color.md_deep_orange_800,
            R.color.md_brown_800,
            R.color.md_blue_grey_800
        )
    }

}