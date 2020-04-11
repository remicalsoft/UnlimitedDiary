package net.dixq.unlimiteddiary.top

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.AdapterView
import android.widget.ListView
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAuthIOException
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.google.api.services.drive.model.File
import net.dixq.unlimiteddiary.R
import net.dixq.unlimiteddiary.google_api.DriveHelper
import net.dixq.unlimiteddiary.singleton.ApiAccessor
import net.dixq.unlimiteddiary.utils.Lg
import net.dixq.unlimiteddiary.content.*
import net.dixq.unlimiteddiary.content.ContentActivity.RESULT_CREATED
import net.dixq.unlimiteddiary.content.ContentActivity.RESULT_EDITED
import java.io.IOException
import java.util.*


class TopActivity : AppCompatActivity(), SwipeRefreshLayout.OnRefreshListener,
    AdapterView.OnItemClickListener {
    private val _driveHelper = DriveHelper(ApiAccessor.getInstance())
    private val _handler = Handler()
    private val _list:LinkedList<DiaryData> = LinkedList<DiaryData>()

    // ここにアクセスすると、無制限アップロード可能に。photos.google.com/settings

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_top)
        findViewById<SwipeRefreshLayout>(R.id.swipelayout).setOnRefreshListener(this);
        findViewById<ListView>(R.id.list).setOnItemClickListener(this)
        readAndLayout()
        val fab: View = findViewById(R.id.floating_action_button)
        fab.setOnClickListener {
            run {
                val intent = Intent(this, ContentActivity::class.java)
                intent.putExtra(TAG_NEW,"")
                startActivityForResult(intent, REQUEST_CONTENT)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(resultCode){
            RESULT_CREATED-> {
                val title = data!!.getStringExtra(TAG_TITLE)
                val body = data.getStringExtra(TAG_BODY)
                val diaryDat = DiaryData(false, title, body)
                diaryDat.setNowTime()
                insertTopOfList(diaryDat)
                post(diaryDat)
                val adapter = ItemAdapter(this@TopActivity, _list)
                val listView = findViewById<ListView>(R.id.list)
            }
            RESULT_EDITED->{
                val title = data!!.getStringExtra(TAG_TITLE)
                val body  = data.getStringExtra(TAG_BODY)
                val filename = data.getStringExtra(TAG_EDIT_FILENAME)
                val index = getDiaryData(filename)
                _driveHelper.delete(_list[index].file)
                _list[index].proceedRevision()
                _list[index].title = title
                _list[index].body = body
//              _list[index].setNowTime()
                post(_list[index])
                val adapter = ItemAdapter(this@TopActivity, _list)
                val listView = findViewById<ListView>(R.id.list)
            }
        }
    }

    private fun getDiaryData(fileName:String):Int {
        var i=0
        while(i<_list.size){
            if(_list[i].getFileName() == fileName){
                return i
            }
            i++
        }
        return -1
    }

    private fun insertTopOfList(dat:DiaryData){
        if(_list[0].isMonthLine){
            // 先頭が月ラインなら
            if(dat.equalAsMonth(_list[0])){
                // その月と同じデータならその次に挿入
                _list.add(1, dat)
                return
            } else {
                // その月と異なるデータなら月ラインを挿入して次に挿入
                val monthLine = DiaryData(true, dat.year, dat.month)
                _list.add(0, monthLine)
                _list.add(1, dat)
            }
        } else {
            // 先頭が月ラインじゃないってことはありえない。仕様変更があったらここで対応する
        }

    }

    private fun readAndLayout() {
        // 非UIスレッドで呼び出す
        // （AsyncTask等でAPI呼び出し用の基底クラスをつくって認証が必要な場合の処理を実施したほうが良い）
        Thread {
            var fileList: LinkedList<File>? = null
            try {
                fileList = _driveHelper.allFile
            } catch (e: UserRecoverableAuthIOException) {
                // 認証が必要な場合に発生するException。これが発生したら認証のためのIntent発行を行い、認証後、DriveAPIを再呼び出しする
                _handler.post { startActivityForResult(e.intent, REQUEST_AUTHORIZATION) }
            } catch (e: GoogleAuthIOException) {
                Lg.e("Developer ConsoleでClientIDを設定していない場合に発生する")
            } catch (e: IOException) {
                Lg.e("IOException")
            }

            createListData(fileList!!, _list)
            insertMonthLine(fileList!!, _list)

            val adapter = ItemAdapter(this@TopActivity, _list)

            _handler.post {
                findViewById<ProgressBar>(R.id.progress).visibility = View.GONE;
                findViewById<ListView>(R.id.list).adapter = adapter
                findViewById<SwipeRefreshLayout>(R.id.swipelayout).isRefreshing = false;
            }

        }.start()
    }

    private fun createListData(fileList:LinkedList<File>, list:LinkedList<DiaryData>){
        for (file in fileList) {
            try {
                var dat = FileData.convertFileToDiaryData(file)
                dat = FileData.readBody(_driveHelper, file.id, dat)
                if (dat == null) {
                    continue
                }
                _list.add(dat)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    // 日記データの中に月ラインを挿入する
    private fun insertMonthLine(fileList:LinkedList<File>, list:LinkedList<DiaryData>){
        if (list.size == 0) {
            return
        }

        if(!list[0].isMonthLine) {
            // 先頭に月ラインを挿入する
            val monthLine = DiaryData(true, list[0].year, list[0].month)
            list.add(0, monthLine)
        }

        var i = 1 //次の行から計算
        while (i < list.size - 1) {
            if (list[i].isMonthLine || list[i + 1].isMonthLine) {
                i++
                continue
            }
            if (!list[i].equalAsMonth(list[i + 1])){
                val dat = DiaryData(true)
                dat.year = list[i+1].year
                dat.month = list[i+1].month
                list.add(i + 1, dat)
                i++
            }
            i++
        }
    }

    private fun post(diary:DiaryData){
        _driveHelper.post(diary.getFileName(), diary.getConvinedString().toByteArray())
    }

    override fun onRefresh() {
        readAndLayout()
    }

    override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        val intent = Intent(this, ContentActivity::class.java)
        intent.putExtra(TAG_TITLE,_list[position].title)
        intent.putExtra(TAG_BODY,_list[position].body)
        intent.putExtra(TAG_EDIT_FILENAME,_list[position].getFileName())
        startActivityForResult(intent, REQUEST_CONTENT)
    }

    companion object {
        private val REQUEST_AUTHORIZATION = 0
        private val REQUEST_CONTENT = 1
    }

}
