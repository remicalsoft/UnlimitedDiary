package net.dixq.unlimiteddiary.content

import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.Button
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import com.google.android.material.textfield.TextInputEditText
import net.dixq.unlimiteddiary.R
import net.dixq.unlimiteddiary.content.ContentActivity.RESULT_CREATED
import net.dixq.unlimiteddiary.content.ContentActivity.RESULT_EDITED
import net.dixq.unlimiteddiary.uiparts.OkDialog


class PostFragment : Fragment(), View.OnClickListener {

    var _isNew:Boolean = false
    var _activity:ContentActivity?=null
    var _title:String? = null
    var _body:String? = null
    var _filename:String? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        _activity = context as ContentActivity
    }

    override fun onCreate(bundle:Bundle?){
        super.onCreate(bundle)
        if(arguments!=null) {
            _title = arguments!!.getString(TAG_TITLE)
            _body = arguments!!.getString(TAG_BODY)
            _filename = arguments!!.getString(TAG_EDIT_FILENAME)
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
            view.findViewById<TextInputEditText>(R.id.edt_title).setText(_title)
            view.findViewById<TextInputEditText>(R.id.edt_body).setText(_body)
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
        val intent = Intent()
        intent.putExtra(TAG_TITLE, view!!.findViewById<TextInputEditText>(R.id.edt_title).text.toString())
        intent.putExtra(TAG_BODY,  view!!.findViewById<TextInputEditText>(R.id.edt_body) .text.toString())
        intent.putExtra(TAG_EDIT_FILENAME, _filename)
        if(_isNew) {
            _activity!!.setResult(RESULT_CREATED, intent)
        } else {
            _activity!!.setResult(RESULT_EDITED, intent)
        }
        _activity!!.finish()
    }

}