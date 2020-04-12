package net.dixq.unlimiteddiary.content

import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import com.google.android.gms.tasks.Tasks
import com.google.android.material.textfield.TextInputEditText
import com.google.api.client.http.ByteArrayContent
import com.google.api.services.drive.model.File
import net.dixq.unlimiteddiary.R
import net.dixq.unlimiteddiary.common.Define
import net.dixq.unlimiteddiary.common.JsonParser
import net.dixq.unlimiteddiary.singleton.ApiAccessor
import net.dixq.unlimiteddiary.top.DiaryData
import net.dixq.unlimiteddiary.utils.Lg
import java.io.IOException
import java.util.concurrent.Callable
import java.util.concurrent.Executors

class PostingFragment : Fragment() {

    var _activity: ContentActivity? = null
    var _diaryData: DiaryData? = null
    var _diaryJson: String? = null
    val _accessor = ApiAccessor.getInstance()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        _activity = context as ContentActivity
    }

    override fun onCreate(bundle: Bundle?) {
        super.onCreate(bundle)
        _diaryData = DiaryData(false)
        _diaryJson = arguments!!.getString(TAG_JSON_DIARY)
        _diaryData!!.setFromJson(_diaryJson!!)
        if(_diaryData!!.isNewPostData()) {
            // 新規策を作成して投稿
            _diaryData!!.setNowTime()
            postNewFile(_diaryData!!.getFileName(), JsonParser.encodeJson(_diaryData!!).toByteArray())
        } else {
            // 編集
            post(_diaryData!!.getFileName(), _diaryData!!.fileId, JsonParser.encodeJson(_diaryData!!).toByteArray())
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_posting, container, false)
    }

    private fun postNewFile(fileName:String, content:ByteArray) {
        Tasks.call(Executors.newSingleThreadExecutor(), Callable<String>{
            val metadata = File()
                .setParents(listOf(_accessor.folderId))
                .setMimeType(Define.mimeTypeText)
                .setName(fileName)

            val file = _accessor.driveService.files().create(metadata).execute()
                ?: throw IOException("Null result when requesting file creation.")
            _diaryData!!.fileId = file.id
            file.id
        })
            .addOnSuccessListener {
                post(fileName, it, content)
            }
            .addOnFailureListener {
                Lg.e("新規ファイル作成に失敗")
            }
    }

    private fun post(fileName:String, fileId:String, content:ByteArray){
        // it: String としてファイルIDを取得できる。
        Tasks.call(Executors.newSingleThreadExecutor(), Callable<Void>{
            val metadata = File().setName(fileName)
            val contentStream = ByteArrayContent(Define.mimeTypeText, content)
            _accessor.driveService.files().update( fileId , metadata, contentStream).execute()

            null
        })
            .addOnSuccessListener {
                Lg.d("投稿成功")
                val intent = Intent();
                intent.putExtra(TAG_JSON_DIARY, JsonParser.encodeJson(_diaryData))
                _activity!!.setResult(RESULT_OK, intent)
                _activity!!.finish()
            }
            .addOnFailureListener {
                Lg.d("投稿失敗 : "+it.message)
            }
    }

}