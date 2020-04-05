package net.dixq.unlimiteddiary.top

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAuthIOException
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.google.api.services.drive.model.File
import kotlinx.android.synthetic.main.main_top.*
import net.dixq.unlimiteddiary.R
import net.dixq.unlimiteddiary.drive.DriveProcessor
import net.dixq.unlimiteddiary.singleton.DriveAccessor
import net.dixq.unlimiteddiary.utils.Lg
import net.dixq.unlimiteddiary.write.WriteActivity

import java.io.*
import java.util.LinkedList

class TopActivity : AppCompatActivity() {
    private val _driveProcesser = DriveProcessor(DriveAccessor.getInstance())
    private var _fileList = LinkedList<File>()
    private val _handler = Handler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_top)
        readAndLayout()
        val fab: View = findViewById(R.id.floating_action_button)
        fab.setOnClickListener { view -> {
                val intent = Intent(this, WriteActivity::class.java)
                startActivityForResult(intent, REQUEST_WRITE)
            }
        }
    }

     private fun readAndLayout() {
        // 非UIスレッドで呼び出す
        // （AsyncTask等でAPI呼び出し用の基底クラスをつくって認証が必要な場合の処理を実施したほうが良い）
        Thread {
            try {
                _fileList = _driveProcesser.allFile
            } catch (e: UserRecoverableAuthIOException) {
                // 認証が必要な場合に発生するException。これが発生したら認証のためのIntent発行を行い、認証後、DriveAPIを再呼び出しする
                _handler.post { startActivityForResult(e.intent, REQUEST_AUTHORIZATION) }
            } catch (e: GoogleAuthIOException) {
                Lg.e("Developer ConsoleでClientIDを設定していない場合に発生する")
            } catch (e: IOException) {
                Lg.e("IOException")
            }

            val list = LinkedList<DiaryData>()
            for (file in _fileList) {
                try {
                    var dat = FileData.convertFileToDiaryData(file)
                    dat = FileData.readBody(_driveProcesser, file.id, dat)
                    if (dat == null) {
                        continue
                    }
                    list.add(dat)
                } catch (e: IOException) {
                    e.printStackTrace()
                }

            }
            if (list.size != 0) {
                run {
                    val dat = DiaryData(true)
                    dat.year = list[0].year
                    dat.month = list[0].month
                    list.add(0, dat)
                }
                var i = 1
                while (i < list.size - 1) {
                    if (list[i].isDayCell || list[i + 1].isDayCell) {
                        i++
                        continue
                    }
                    if (!list[i].equalAsMonth(list[i + 1])) {
                        val dat = DiaryData(true)
                        dat.year = list[i + 1].year
                        dat.month = list[i + 1].month
                        list.add(i + 1, dat)
                        i++
                    }
                    i++
                }
                val adapter = ItemAdapter(this@TopActivity, list)
                val listView = findViewById<ListView>(R.id.list)
                _handler.post { listView.adapter = adapter }
            }
        }.start()
    }

    companion object {
        private val REQUEST_AUTHORIZATION = 0
        private val REQUEST_WRITE = 1;
    }
}
