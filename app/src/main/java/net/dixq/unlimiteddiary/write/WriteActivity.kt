package net.dixq.unlimiteddiary.write

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import net.dixq.unlimiteddiary.R
import net.dixq.unlimiteddiary.top.DiaryData
import net.dixq.unlimiteddiary.uiparts.OkDialog
import java.util.*


class WriteActivity : AppCompatActivity(), View.OnClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_write)
        findViewById<EditText>(R.id.edt_body).requestFocus()
        findViewById<ImageButton>(R.id.button_clear).setOnClickListener(this)
        findViewById<Button>(R.id.button_post).setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.button_clear->{
                setResult(Activity.RESULT_CANCELED)
                finish()
            }
            R.id.button_post->{
                onClickPostButton()
            }
        }
    }

    private fun onClickPostButton(){
        var textCount = 0;
        textCount += findViewById<TextInputEditText>(R.id.edt_title).text.toString().length
        textCount += findViewById<TextInputEditText>(R.id.edt_body).text.toString().length
        if(textCount==0){
            OkDialog(this, "何も入力されていません。", null).show()
            return
        }
        val intent = Intent()
        intent.putExtra(TAG_TITLE, findViewById<TextInputEditText>(R.id.edt_title).text.toString())
        intent.putExtra(TAG_BODY,  findViewById<TextInputEditText>(R.id.edt_body) .text.toString())
        setResult(RESULT_OK, intent)
        finish()
    }
    companion object {
        public val TAG_TITLE = "tag_title"
        public val TAG_BODY = "tag_body";
    }
}