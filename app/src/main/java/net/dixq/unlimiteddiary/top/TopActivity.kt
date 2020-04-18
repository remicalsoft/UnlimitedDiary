package net.dixq.unlimiteddiary.top

import android.content.ContentValues
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
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
import net.dixq.unlimiteddiary.common.DbHelper
import net.dixq.unlimiteddiary.common.JsonUtils
import net.dixq.unlimiteddiary.common.Lg
import net.dixq.unlimiteddiary.common.google_api.DriveHelper
import net.dixq.unlimiteddiary.common.singleton.ApiAccessor
import net.dixq.unlimiteddiary.content.ContentActivity
import net.dixq.unlimiteddiary.content.TAG_DIARY_DELETE
import net.dixq.unlimiteddiary.content.TAG_JSON_DIARY
import net.dixq.unlimiteddiary.content.TAG_NEW
import java.io.IOException
import java.util.*


class TopActivity : AppCompatActivity(), SwipeRefreshLayout.OnRefreshListener,
    AdapterView.OnItemClickListener {

    private val _driveHelper = DriveHelper(ApiAccessor.getInstance())
    private val _handler = Handler()
    private var _list:LinkedList<DiaryData> = LinkedList<DiaryData>()
    private var _db:DbHelper? = null
    private var _listAdapter:ItemAdapter? = null

    // ここにアクセスすると、無制限アップロード可能に。photos.google.com/settings

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_top)
        _db = DbHelper(this.applicationContext)
        findViewById<SwipeRefreshLayout>(R.id.swipelayout).setOnRefreshListener(this);
        findViewById<ListView>(R.id.list).onItemClickListener = this
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
            RESULT_OK-> {
                val diaryData = DiaryData(false)
                diaryData.setFromJson(data!!.getStringExtra(TAG_JSON_DIARY))
                val index = getDiaryDataIgnoreRevisioin(diaryData)
                if (index == -1) {
                    //見つからなかったら新規投稿
                    insertTopOfList(diaryData)
                    saveDiary(diaryData)
                } else {
                    if(data!!.getStringExtra(TAG_DIARY_DELETE)!=null) {
                        //削除なら
                        deleteDiary(_list[index])
                        _list.removeAt(index)
                    } else {
                        //編集なら
                        deleteDiary(_list[index])
                        _list[index] = diaryData
                        saveDiary(diaryData)
                    }
                }
                val adapter = ItemAdapter(this@TopActivity, _list)
                val listView = findViewById<ListView>(R.id.list)
                listView.adapter = adapter
            }
        }
    }

    private fun getDiaryDataIgnoreRevisioin(data:DiaryData):Int {
        var i=0
        while(i<_list.size){
            if(data.equalsIgnoreRevision(_list[i].getFileName())){
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
                Lg.e("IOException:"+e.message)
            }

            val updateList = LinkedList<File>()
            val dbFileList = readFromDb()
            for(file in fileList!!){
                overrideFileId(file, dbFileList)
                if(!existsInList(file.name, dbFileList)){
                    // データベースと一致しない物は更新リストに入れて更新する
                    updateList.add(file)
                }
            }
            for(data in dbFileList){
                if(!existsInList(data, fileList)){
                    // データベースにあってサーバーにない物はデータベースから消す
                    deleteDiary(data)
                    dbFileList.remove(data)
                }
            }

            _list = dbFileList
            sortList()
            _list = insertMonthLine(_list)

            _listAdapter = ItemAdapter(this@TopActivity, _list)
            _handler.post {
                findViewById<ProgressBar>(R.id.progress).visibility = View.GONE;
                findViewById<ListView>(R.id.list).adapter = _listAdapter
                findViewById<SwipeRefreshLayout>(R.id.swipelayout).isRefreshing = false;
            }
            for(file in updateList){
                val content = _driveHelper.getContent(file.id)
                val diary = DiaryData(false)
                diary.setFromJson(content)
                diary.fileId = file.id
                diary.isJustNowFound = true
                removeIgnoreRevision(diary, _list) // Revision違いの同じ物があれば消す
                deleteDiary(diary)
                saveDiary(diary)
                _list.add(diary)
                _list = insertMonthLine(_list)
                sortList()
                _listAdapter!!.setList(_list)
                _handler.post{ _listAdapter!!.notifyDataSetChanged() }
            }

        }.start()
    }

    private fun overrideFileId(file:File, list: LinkedList<DiaryData>){
        for(data in list){
            if(data.getFileName() == file.name){
                data.fileId = file.id
                return
            }
        }
    }

    private fun sortList(){
        _list.sortWith(Comparator {
                a,b ->  b.getFileName().compareTo(a.getFileName())
        })
    }

    private fun removeIgnoreRevision(data:DiaryData, list:LinkedList<DiaryData>){
        for(d in list){
            if(d.equalsIgnoreRevision(data.getFileName())){
                list.remove(d)
                return
            }
        }
    }

    private fun existsInList(filename:String, list:LinkedList<DiaryData>):Boolean {
        for(data in list){
            if(data.equals(filename)){
                return true
            }
        }
        return false
    }

    private fun existsInList(data:DiaryData, list:LinkedList<File>):Boolean {
        for(file in list){
            if(data.equalsIgnoreRevision(file.name)){
                return true
            }
        }
        return false
    }

    // 日記データの中に月ラインを挿入する
    private fun insertMonthLine(list:LinkedList<DiaryData>):LinkedList<DiaryData>{
        if (list.size == 0) {
            return list
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
        return list
    }

    override fun onRefresh() {
        readAndLayout()
    }

    override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        val intent = Intent(this, ContentActivity::class.java)
        intent.putExtra(TAG_JSON_DIARY, JsonUtils.encode(_list[position]))
        startActivityForResult(intent, REQUEST_CONTENT)
    }

    private fun saveDiary(data:DiaryData){
        val db = _db!!.writableDatabase
        val cvalues = ContentValues()
        cvalues.put(DbHelper.COLUMN_NAME_TITLE, data.getFileName())
        cvalues.put(DbHelper.COLUMN_NAME_SUBTITLE, JsonUtils.encode(data))
        db.insert(DbHelper.TABLE_NAME, null, cvalues)
    }

    private fun deleteDiary(data:DiaryData){
        val db: SQLiteDatabase = _db!!.readableDatabase
        val name: String = data.getFileName()
        val values = ContentValues()
        values.put(DbHelper.COLUMN_NAME_TITLE, name)
        db.delete(DbHelper.TABLE_NAME, DbHelper.COLUMN_NAME_TITLE+"=?", arrayOf(name))
    }

    private fun readFromDb():LinkedList<DiaryData> {
        val list = LinkedList<DiaryData>()
        val db: SQLiteDatabase = _db!!.readableDatabase
        val cursor = db.query(
            DbHelper.TABLE_NAME,
            arrayOf(DbHelper.COLUMN_NAME_TITLE, DbHelper.COLUMN_NAME_SUBTITLE),
            null,
            null,
            null,
            null,
            null
        )
        cursor.moveToFirst()
        for (i in 0 until cursor.count) {
            Lg.d("FileName: " + cursor.getString(0))
            list.add(DiaryData(false, cursor.getString(1)))
            cursor.moveToNext()
        }
        cursor.close()
        return list
    }

    companion object {
        private val REQUEST_AUTHORIZATION = 0
        private val REQUEST_CONTENT = 1
    }

}
