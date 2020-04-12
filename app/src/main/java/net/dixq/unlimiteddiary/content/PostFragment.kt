package net.dixq.unlimiteddiary.content

import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.Button
import android.widget.LinearLayout
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import com.google.android.material.textfield.TextInputEditText
import net.dixq.unlimiteddiary.R
import net.dixq.unlimiteddiary.common.JsonParser
import net.dixq.unlimiteddiary.content.ContentActivity.RESULT_CREATED
import net.dixq.unlimiteddiary.content.ContentActivity.RESULT_EDITED
import net.dixq.unlimiteddiary.top.DiaryData
import net.dixq.unlimiteddiary.uiparts.OkDialog


class PostFragment : Fragment(), View.OnClickListener {

    var _isNew:Boolean = false
    var _activity:ContentActivity?=null
    var _diaryData:DiaryData? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        _activity = context as ContentActivity
    }

    override fun onCreate(bundle:Bundle?){
        super.onCreate(bundle)
        if(arguments!=null) {
            _diaryData = DiaryData(false)
            _diaryData!!.setFromJson(arguments!!.getString(TAG_JSON_DIARY)!!)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_post, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _activity!!.setSupportActionBar(view.findViewById<Toolbar>(R.id.toolbar))
        _activity!!.supportActionBar!!.setDisplayHomeAsUpEnabled(true)        // Backボタンを有効にする
        _activity!!.supportActionBar!!.setHomeButtonEnabled(true)
        _activity!!.supportActionBar!!.title = ""
        _isNew = _activity!!.intent.getStringExtra(TAG_NEW)!=null
        view.findViewById<Button>(R.id.button_post).setOnClickListener(this)
        if(!_isNew) {
            view.findViewById<TextInputEditText>(R.id.edt_title).setText(_diaryData!!.title)
            view.findViewById<TextInputEditText>(R.id.edt_body).setText(_diaryData!!.body)
        }
        view.findViewById<TextInputEditText>(R.id.edt_body).requestFocus()
    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.button_post-> onClickPostButton()
        }
    }

    private fun onClickPostButton(){
        var textCount = 0;
        textCount += view!!.findViewById<TextInputEditText>(R.id.edt_title).text.toString().length
        textCount += view!!.findViewById<TextInputEditText>(R.id.edt_body).text.toString().length
        if(textCount==0){
            OkDialog(_activity as Context, "何も入力されていません。", null).show()
            return
        }
        if(_diaryData==null){
            _diaryData = DiaryData(false)
        }
        _diaryData!!.title = view!!.findViewById<TextInputEditText>(R.id.edt_title).text.toString()
        _diaryData!!.body = view!!.findViewById<TextInputEditText>(R.id.edt_body).text.toString()
        val json = JsonParser.encodeJson(_diaryData)
        val bundle = Bundle()
        bundle.putString(TAG_JSON_DIARY, json)
        val fragment = PostingFragment();
        fragment.arguments = bundle
        _activity!!.changeFragment(fragment)
    }

}