package net.dixq.unlimiteddiary.content

import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
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
import com.google.android.gms.common.images.Size
import com.google.android.material.textfield.TextInputEditText
import net.dixq.unlimiteddiary.R
import net.dixq.unlimiteddiary.authentication.AuthenticateActivity
import net.dixq.unlimiteddiary.common.BitmapUtils
import net.dixq.unlimiteddiary.common.JsonUtils
import net.dixq.unlimiteddiary.common.OkDialog
import net.dixq.unlimiteddiary.common.PrefUtils
import net.dixq.unlimiteddiary.top.DiaryData
import net.dixq.unlimiteddiary.utils.convertDpToPx
import java.io.IOException
import java.util.*


class PostFragment : Fragment(), View.OnClickListener {

    var _isNew:Boolean = false
    var _activity:ContentActivity?=null
    var _rootView:View? = null
    var _diaryData:DiaryData? = null
    val _imageViewList = mutableListOf<ImageButton>()
    var _imageAddNum = 0
    val _bitmapList: LinkedList<Bitmap> = LinkedList<Bitmap>()

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
        _imageViewList.add(view.findViewById<ImageButton>(R.id.img00))
        _imageViewList.add(view.findViewById<ImageButton>(R.id.img01))
        _imageViewList.add(view.findViewById<ImageButton>(R.id.img02))
        _imageViewList.add(view.findViewById<ImageButton>(R.id.img03))

        _rootView!!.addOnLayoutChangeListener(View.OnLayoutChangeListener { v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom ->
            if (left == 0 && top == 0 && right == 0 && bottom == 0) {
                return@OnLayoutChangeListener
            }
            if(_bitmapList.size!=0){
                // 既にロードしている場合はもうしない
                return@OnLayoutChangeListener
            }
            if(_diaryData==null){
                // 新規投稿
                return@OnLayoutChangeListener
            }
            val idarray = arrayListOf<Int>(R.id.img00, R.id.img01, R.id.img02, R.id.img03)
            for (i in 0..3) {
                val filename = _diaryData!!.getJpegFilePath(this.context!!, i)
                val bmp = BitmapFactory.decodeFile(filename) ?: break
                _bitmapList.add(bmp)
                val imgView = view.findViewById<ImageButton>(idarray[i])
                val size = getFitSize(_activity!!, _rootView!!, bmp)
                imgView.background = BitmapDrawable(this.resources, bmp)
                imgView.visibility = View.VISIBLE
                imgView.layoutParams.width = size.width
                imgView.layoutParams.height = size.height
                _imageAddNum++
            }
            if(_bitmapList.size==4){
                view.findViewById<Button>(R.id.btn_add).visibility = View.GONE
            }
        })
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
                    val loadedBmp = MediaStore.Images.Media.getBitmap(context?.contentResolver, uri)
                    val rotatedBmp = BitmapUtils.rotateImageIfRequired(loadedBmp, this.context, uri)
                    val resizedBmp = BitmapUtils.resize(rotatedBmp,1080)
                    _bitmapList.add(resizedBmp)
                    updateImageView(resizedBmp)
                    if(_bitmapList.size==4){
                        _rootView!!.findViewById<Button>(R.id.btn_add).visibility = View.GONE
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun updateImageView(bmp:Bitmap){
        val imgView = _imageViewList[_imageAddNum]
        val size = getFitSize(this.context!!, _rootView!!, bmp)
        imgView.background = BitmapDrawable(this.resources, bmp)
        imgView.visibility = View.VISIBLE
        imgView.layoutParams.width = size.width
        imgView.layoutParams.height = size.height
        _imageAddNum++
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
        for(i in 0 until _bitmapList.size){
            bundle.putByteArray(TAG_POST_IMAGE+i.toString(), BitmapUtils.compress(_bitmapList[i], 70))
        }
        val fragment = PostingFragment();
        fragment.arguments = bundle
        _activity!!.changeFragment(fragment)
    }

    companion object {
        private val REQUEST_CODE_SAF = 0
        public  fun getFitSize(context:Context, rootView:View, bitmap:Bitmap):Size {
            val dp16 = convertDpToPx(context!!, 16).toFloat()
            val w = bitmap.width
            val h = bitmap.height
            val resizeScale: Float = (rootView!!.width  - dp16) / w
            return Size((w * resizeScale).toInt(), (h * resizeScale).toInt());
        }
    }

}