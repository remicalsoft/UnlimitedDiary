package net.dixq.unlimiteddiary.content

import android.content.Context
import android.content.DialogInterface
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.view.*
import android.view.View.OnLayoutChangeListener
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import net.dixq.unlimiteddiary.R
import net.dixq.unlimiteddiary.common.OkCancelDialog
import net.dixq.unlimiteddiary.top.DiaryData


class DetailFragment : Fragment() {

    private var _jsonDiary:String? = null
    private var _diaryData:DiaryData? = null
    private var _rootView:View? = null
    private var _activity:ContentActivity?=null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        _activity = context as ContentActivity
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_detail, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _rootView = view
        val toolbar = view.findViewById<Toolbar>(R.id.toolbar);
        _activity!!.setSupportActionBar(toolbar)
        _activity!!.supportActionBar!!.setDisplayHomeAsUpEnabled(true)        // Backボタンを有効にする
        _activity!!.supportActionBar!!.setHomeButtonEnabled(true)
        _activity!!.supportActionBar!!.title = ""
        _diaryData = DiaryData(false)
        _jsonDiary = _activity!!.intent.getStringExtra(TAG_JSON_DIARY)
        _diaryData!!.setFromJson(_jsonDiary!!)
        view.findViewById<TextView>(R.id.txt_title).text = _diaryData!!.title
        view.findViewById<TextView>(R.id.txt_body).text = _diaryData!!.body
        _rootView!!.addOnLayoutChangeListener(OnLayoutChangeListener { v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom ->
            if (left == 0 && top == 0 && right == 0 && bottom == 0) {
                return@OnLayoutChangeListener
            }
            val idarray = arrayListOf<Int>(R.id.img00,R.id.img01,R.id.img02,R.id.img03)
            for(i in 0..3){
                val filename = _diaryData!!.getJpegFileName(this.context!!, i)
                val bmp = BitmapFactory.decodeFile(filename) ?: break
                val imgView = view.findViewById<ImageButton>(idarray[i])
                val size = PostFragment.getFitSize(_activity!!, _rootView!!, bmp)
                imgView.background = BitmapDrawable(this.resources, bmp)
                imgView.visibility = View.VISIBLE
                imgView.layoutParams.width = size.width
                imgView.layoutParams.height = size.height
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.detail, menu)
    }

    override fun onOptionsItemSelected(item:MenuItem): Boolean{
        when (item.itemId) {
            R.id.menu_edit -> {
                val bundle = Bundle()
                bundle.putString(TAG_JSON_DIARY, _jsonDiary)
                val fragment = PostFragment()
                fragment.arguments = bundle
                _activity!!.changeFragment(fragment)
            }
            R.id.menu_delete -> {
                OkCancelDialog(_activity!!, "この日記を削除しますか？", DialogInterface.OnClickListener { a, b ->
                    val bundle = Bundle()
                    bundle.putString(TAG_JSON_DIARY, _jsonDiary)
                    bundle.putString(TAG_DIARY_DELETE, "")
                    val fragment = PostingFragment()
                    fragment.arguments = bundle
                    _activity!!.changeFragment(fragment)
                }, null).show()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {
        public val REQUEST_CODE_POST = 0
    }

}