package net.dixq.unlimiteddiary.common.google_api

import com.google.android.gms.tasks.Tasks
import com.google.api.services.drive.model.File
import net.dixq.unlimiteddiary.common.Define
import net.dixq.unlimiteddiary.common.Lg
import net.dixq.unlimiteddiary.common.StopWatch
import net.dixq.unlimiteddiary.common.StreamUtils
import net.dixq.unlimiteddiary.common.exception.FatalErrorException
import net.dixq.unlimiteddiary.common.singleton.ApiAccessor
import java.io.IOException
import java.util.*
import java.util.concurrent.Callable
import java.util.concurrent.Executors

class DriveHelper(private val _accessor: ApiAccessor) {

    fun getFolderId():String {
        val list = ArrayList<File>()
        val request = _accessor.driveService.files().list()
        request.q = "mimeType = 'application/vnd.google-apps.folder' and name = '" + Define.FOLDER_NAME + "'"
        do {
            val files = request.execute()
            list.addAll(files.files)
            request.pageToken = files.nextPageToken
        } while (request.pageToken != null && request.pageToken.isNotEmpty())
        if (list.size != 1) {
            throw FatalErrorException("UnlimitedDiaryのフォルダが見つからないか、2つ以上あります")
        }
        return list[0].id
    }

    fun getJpegFolderId():String {
        val list = ArrayList<File>()
        val request = _accessor.driveService.files().list()
        request.q = "mimeType = 'application/vnd.google-apps.folder' and name = '" + Define.FOLDER_JPEG_NAME + "'"
        do {
            val files = request.execute()
            list.addAll(files.files)
            request.pageToken = files.nextPageToken
        } while (request.pageToken != null && request.pageToken.isNotEmpty())
        if (list.size != 1) {
            throw FatalErrorException("UnlimitedDiaryのフォルダが見つからないか、2つ以上あります")
        }
        return list[0].id
    }

    @get:Throws(IOException::class)
    val allFile: LinkedList<File>
        get() {
            return getAllFile(_accessor.folderId)
        }

    @Throws(IOException::class)
    fun getAllFile(folderId: String?): LinkedList<File> {
        val sw = StopWatch();
        val list =
            LinkedList<File>()
        val request =
            _accessor.driveService.files()
                .list() //対象のフォルダ以下　かつ　フォルダは除外　かつ　ゴミ箱行きは除外
                .setQ("'$folderId' in parents and mimeType != 'application/vnd.google-apps.folder' and trashed = false")
        do {
            val files = request.execute()
            list.addAll(files.files)
            // 全アイテムを取得するために繰り返し
            request.pageToken = files.nextPageToken
        } while (request.pageToken != null && request.pageToken.isNotEmpty()
        )
        list.sortWith(Comparator {
                a,b ->  b.name.compareTo(a.name)
        })
        Lg.e("sw:"+sw.getDiff());
        return list
    }

    @Throws(IOException::class)
    fun getContent(fileId: String?): String {
        val im = _accessor.driveService.files()[fileId].executeMediaAsInputStream()
        return StreamUtils.getText(im)
    }

    @Throws(IOException::class)
    fun getJpegFile(fileId: String?): ByteArray {
        val im = _accessor.driveService.files()[fileId].executeMediaAsInputStream()
        return StreamUtils.readAll(im)
    }

    fun delete(deleteFileId:String){
        Tasks.call(Executors.newSingleThreadExecutor(), Callable<Void>{
            _accessor.driveService.files().delete(deleteFileId).execute()
            null
        })
            .addOnSuccessListener {
                Lg.d("削除成功")
            }
            .addOnFailureListener {
                Lg.d("削除失敗")
            }
    }
}