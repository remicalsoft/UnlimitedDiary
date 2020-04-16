package net.dixq.unlimiteddiary.content

import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import com.google.android.material.textfield.TextInputEditText
import net.dixq.unlimiteddiary.R
import net.dixq.unlimiteddiary.authentication.AuthenticateActivity
import net.dixq.unlimiteddiary.common.JsonUtils
import net.dixq.unlimiteddiary.common.PrefUtils
import net.dixq.unlimiteddiary.top.DiaryData
import net.dixq.unlimiteddiary.common.OkDialog
import net.dixq.unlimiteddiary.utils.convertDpToPx
import java.io.IOException


class PostFragment : Fragment(), View.OnClickListener {

    var _isNew:Boolean = false
    var _activity:ContentActivity?=null
    var _rootView:View? = null
    var _diaryData:DiaryData? = null
    val _listImage = mutableListOf<ImageButton>()
    var _imageAddNum = 0

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
        _rootView = view
        _activity!!.setSupportActionBar(view.findViewById<Toolbar>(R.id.toolbar))
        _activity!!.supportActionBar!!.setDisplayHomeAsUpEnabled(true)        // Backボタンを有効にする
        _activity!!.supportActionBar!!.setHomeButtonEnabled(true)
        _activity!!.supportActionBar!!.title = ""
        _isNew = _activity!!.intent.getStringExtra(TAG_NEW)!=null
        view.findViewById<Button>(R.id.button_post).setOnClickListener(this)
        view.findViewById<Button>(R.id.btn_add).setOnClickListener(this)
        if(!_isNew) {
            view.findViewById<TextInputEditText>(R.id.edt_title).setText(_diaryData!!.title)
            view.findViewById<TextInputEditText>(R.id.edt_body).setText(_diaryData!!.body)
        }
        view.findViewById<TextInputEditText>(R.id.edt_body).requestFocus()
        _listImage.add(view.findViewById<ImageButton>(R.id.img00))
        _listImage.add(view.findViewById<ImageButton>(R.id.img01))
        _listImage.add(view.findViewById<ImageButton>(R.id.img02))
        _listImage.add(view.findViewById<ImageButton>(R.id.img03))
    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.button_post-> onClickPostButton()
            R.id.btn_add -> {
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "image/*"
                }
                startActivityForResult(intent, REQUEST_CODE_SAF)
            }
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        if (requestCode == REQUEST_CODE_SAF && resultCode == RESULT_OK) {
            var uri: Uri? = null
            if (resultData != null) {
                uri = resultData.data
                try {
                    val bitmap = MediaStore.Images.Media.getBitmap(context?.contentResolver, uri)
                    val imgView = _listImage[_imageAddNum]
                    val scaledBitmap = createScaledBitmap(bitmap)
                    imgView.setImageBitmap(scaledBitmap)
                    imgView.visibility = View.VISIBLE
                    imgView.layoutParams.width = scaledBitmap.width
                    imgView.layoutParams.height = scaledBitmap.height
                    _imageAddNum++
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun createScaledBitmap(preBitmap:Bitmap):Bitmap {
        val dp16 = convertDpToPx(context!!, 16).toFloat()
        val resizeScale: Float =
        if (preBitmap.width >= preBitmap.height) {
            (_rootView!!.width  - dp16) / preBitmap.width
        } else {
            (_rootView!!.height - dp16) / preBitmap.height
        }
        return Bitmap.createScaledBitmap(
            preBitmap,
            (preBitmap.width * resizeScale).toInt(),
            (preBitmap.height * resizeScale).toInt(),
            true
        )
    }

    private fun onClickPostButton(){
        var textCount = 0;
        textCount += view!!.findViewById<TextInputEditText>(R.id.edt_title).text.toString().length
        textCount += view!!.findViewById<TextInputEditText>(R.id.edt_body).text.toString().length
        if(textCount==0){
            OkDialog(
                _activity as Context,
                "何も入力されていません。",
                null
            ).show()
            return
        }
        if(_diaryData==null){
            _diaryData = DiaryData(false)
        }
        _diaryData!!.title = view!!.findViewById<TextInputEditText>(R.id.edt_title).text.toString()
        _diaryData!!.body = view!!.findViewById<TextInputEditText>(R.id.edt_body).text.toString()
        _diaryData!!.author = PrefUtils.read(context, AuthenticateActivity.KEY_HANDLE_NAME)
        _diaryData!!.color = PrefUtils.read(context, AuthenticateActivity.KEY_HANDLE_NAME_COLOR)
        val json = JsonUtils.encode(_diaryData)
        val bundle = Bundle()
        bundle.putString(TAG_JSON_DIARY, json)
        val fragment = PostingFragment();
        fragment.arguments = bundle
        _activity!!.changeFragment(fragment)
    }

    companion object {
        private val REQUEST_CODE_SAF = 0
    }

}