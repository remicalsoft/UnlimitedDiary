package net.dixq.unlimiteddiary.content

import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.gms.tasks.Tasks
import com.google.api.client.http.ByteArrayContent
import com.google.api.services.drive.model.File
import net.dixq.unlimiteddiary.R
import net.dixq.unlimiteddiary.common.Define
import net.dixq.unlimiteddiary.common.JsonUtils
import net.dixq.unlimiteddiary.common.singleton.ApiAccessor
import net.dixq.unlimiteddiary.top.DiaryData
import net.dixq.unlimiteddiary.common.Lg
import java.io.IOException
import java.util.concurrent.Callable
import java.util.concurrent.Executors

class PostingFragment : Fragment() {

    var _activity: ContentActivity? = null
    var _diaryData: DiaryData? = null
    var _diaryJson: String? = null
    val _accessor = ApiAccessor.getInstance()
    var _isDeleted = false

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
            postNewFile(_diaryData!!.getFileName())
        } else if(arguments!!.getString(TAG_DIARY_DELETE)!=null) {
            delete(_diaryData!!.fileId)
            val intent = Intent();
            intent.putExtra(TAG_JSON_DIARY, JsonUtils.encode(_diaryData))
            intent.putExtra(TAG_DIARY_DELETE, "")
            _activity!!.setResult(RESULT_OK, intent)
            _activity!!.finish()
        } else {
            // 編集
            val deleteFileId = _diaryData!!.fileId
            _diaryData!!.proceedRevision()
            val newFileName = _diaryData!!.getFileName()
            postNewFileAndDelete(newFileName, deleteFileId)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_posting, container, false)
    }

    private fun postNewFile(fileName:String) {
        Tasks.call(Executors.newSingleThreadExecutor(), Callable<String>{
            val metadata = File()
                .setParents(listOf(_accessor.folderId))
                .setMimeType(Define.mimeTypeText)
                .setName(fileName)

            val file = _accessor.driveService.files().create(metadata).execute()
                ?: throw IOException("Null result when requesting file creation.")
            _diaryData!!.fileId = file.id
            finish()
            file.id
        })
            .addOnSuccessListener {
                _diaryData!!.fileId = it
                postNewFileCore(fileName, it)
            }
            .addOnFailureListener {
                Lg.e("新規ファイル作成に失敗 : "+it.message)
            }
    }

    private fun postNewFileCore(fileName:String, fileId:String){
        // it: String としてファイルIDを取得できる。
        Tasks.call(Executors.newSingleThreadExecutor(), Callable<Void>{
            val metadata = File().setName(fileName)
            val contentStream = ByteArrayContent(Define.mimeTypeText, JsonUtils.encode(_diaryData!!).toByteArray())
            _accessor.driveService.files().update( fileId , metadata, contentStream).execute()
            null
        })
            .addOnSuccessListener {
                Lg.d("投稿成功")
            }
            .addOnFailureListener {
                Lg.e("投稿失敗 : "+it.message)
            }
    }

    private fun postNewFileAndDelete(fileName:String, deleteFileId:String){
        Tasks.call(Executors.newSingleThreadExecutor(), Callable<String>{
            val metadata = File()
                .setParents(listOf(_accessor.folderId))
                .setMimeType(Define.mimeTypeText)
                .setName(fileName)

            val file = _accessor.driveService.files().create(metadata).execute()
                ?: throw IOException("Null result when requesting file creation.")
            _diaryData!!.fileId = file.id
            finish()
            file.id
        })
            .addOnSuccessListener {
                _diaryData!!.fileId = it
                postNewFileAndDeleteCore(fileName, it, deleteFileId)
            }
            .addOnFailureListener {
                Lg.e("新規ファイル作成に失敗")
            }

    }

    private fun postNewFileAndDeleteCore(fileName:String, fileId:String, deleteFileId:String){
        // it: String としてファイルIDを取得できる。
        Tasks.call(Executors.newSingleThreadExecutor(), Callable<Void>{
            val metadata = File().setName(fileName)
            val contentStream = ByteArrayContent(Define.mimeTypeText, JsonUtils.encode(_diaryData!!).toByteArray())
            _accessor.driveService.files().update( fileId , metadata, contentStream).execute()
            null
        })
            .addOnSuccessListener {
                Lg.d("投稿成功")
                delete(deleteFileId)
            }
            .addOnFailureListener {
                Lg.e("投稿失敗 : "+it.message)
            }
    }

    private fun delete(deleteFileId:String){
        Tasks.call(Executors.newSingleThreadExecutor(), Callable<Void>{
            _accessor.driveService.files().delete(deleteFileId).execute()
            null
        })
            .addOnSuccessListener {
                Lg.d("削除成功")
            }
            .addOnFailureListener {
                Lg.e("削除失敗["+deleteFileId+"] : "+it.message)
                delete(deleteFileId)
            }
    }

    private fun finish(){
        val intent = Intent();
        intent.putExtra(TAG_JSON_DIARY, JsonUtils.encode(_diaryData))
        _activity!!.setResult(RESULT_OK, intent)
        _activity!!.finish()
    }

}