package net.dixq.unlimiteddiary.content

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import net.dixq.unlimiteddiary.R


class DetailFragment : Fragment() {

    private var _title:String? = null
    private var _body:String? = null
    private var _filename:String? = null

    var _activity:ContentActivity?=null

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
        val toolbar = view.findViewById<Toolbar>(R.id.toolbar);
        _activity!!.setSupportActionBar(toolbar)
        _activity!!.supportActionBar!!.setDisplayHomeAsUpEnabled(true)        // Backボタンを有効にする
        _activity!!.supportActionBar!!.setHomeButtonEnabled(true)
        _activity!!.supportActionBar!!.title = ""
        _title = _activity!!.intent.getStringExtra(TAG_TITLE)
        _body = _activity!!.intent.getStringExtra(TAG_BODY)
        _filename = _activity!!.intent.getStringExtra(TAG_EDIT_FILENAME)
        view.findViewById<TextView>(R.id.txt_title).text = _title
        view.findViewById<TextView>(R.id.txt_body).text = _body
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.detail, menu)
    }

    override fun onOptionsItemSelected(item:MenuItem): Boolean{
        when (item.itemId) {
            R.id.menu_edit -> {
                val bundle = Bundle()
                bundle.putString(TAG_TITLE, _title)
                bundle.putString(TAG_BODY, _body)
                bundle.putString(TAG_EDIT_FILENAME, _filename)
                val fragment = PostFragment()
                fragment.arguments = bundle
                _activity!!.changeFragment(fragment)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {
        public val REQUEST_CODE_POST = 0
    }

}