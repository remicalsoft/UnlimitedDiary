package net.dixq.unlimiteddiary.top

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.ListView
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.Tasks
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAuthIOException
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.google.api.client.http.ByteArrayContent
import com.google.api.services.drive.model.File
import net.dixq.unlimiteddiary.R
import net.dixq.unlimiteddiary.common.Define
import net.dixq.unlimiteddiary.drive.DriveHelper
import net.dixq.unlimiteddiary.singleton.DriveAccessor
import net.dixq.unlimiteddiary.utils.Lg
import net.dixq.unlimiteddiary.write.WriteActivity
import java.io.IOException
import java.util.*
import java.util.concurrent.Callable
import java.util.concurrent.Executors


class TopActivity : AppCompatActivity() {
    private val _driveProcesser =
        DriveHelper(DriveAccessor.getInstance())
    private val _handler = Handler()
    private val _list:LinkedList<DiaryData> = LinkedList<DiaryData>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_top)
        readAndLayout()
        val fab: View = findViewById(R.id.floating_action_button)
        fab.setOnClickListener {
            run {
                val intent = Intent(this, WriteActivity::class.java)
                startActivityForResult(intent, REQUEST_WRITE)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode){
            REQUEST_WRITE->{
                if(resultCode == Activity.RESULT_OK) {
                    val title = data!!.getStringExtra(WriteActivity.TAG_TITLE)
                    val body  = data.getStringExtra(WriteActivity.TAG_BODY)
                    val diaryDat = DiaryData(false, title, body)
                    diaryDat.setNowTime()
                    insertTopOfList(diaryDat)
                    post(diaryDat)
                    val adapter = ItemAdapter(this@TopActivity, _list)
                    val listView = findViewById<ListView>(R.id.list)
                }
            }
        }
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
                fileList = _driveProcesser.allFile
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
            }

        }.start()
    }

    private fun createListData(fileList:LinkedList<File>, list:LinkedList<DiaryData>){
        for (file in fileList) {
            try {
                var dat = FileData.convertFileToDiaryData(file)
                dat = FileData.readBody(_driveProcesser, file.id, dat)
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

        // 先頭に月ラインを挿入する
        val monthLine = DiaryData(true, list[0].year, list[0].month)
        list.add(0, monthLine)

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
        _driveProcesser.post(diary.getFileName(), diary.getConvinedString().toByteArray())
    }

    companion object {
        private val REQUEST_AUTHORIZATION = 0
        private val REQUEST_WRITE = 1;
    }
}
