package net.dixq.unlimiteddiary.google_api

import com.google.android.gms.tasks.Tasks
import com.google.api.client.http.ByteArrayContent
import com.google.api.services.drive.model.File
import net.dixq.unlimiteddiary.common.Define
import net.dixq.unlimiteddiary.exception.FatalErrorException
import net.dixq.unlimiteddiary.singleton.ApiAccessor
import net.dixq.unlimiteddiary.utils.Lg
import net.dixq.unlimiteddiary.utils.StreamUtils
import java.io.IOException
import java.util.*
import java.util.concurrent.Callable
import java.util.concurrent.Executors

class DriveHelper(private val _accessor: ApiAccessor) {
    private var _folderId: String? = null

    @get:Throws(IOException::class)
    val folderId: String
        get() = _folderId ?: folderIdCore

    @get:Throws(IOException::class)
    private val folderIdCore: String
        private get() {
            val list =
                ArrayList<File>()
            val request =
                ApiAccessor.getInstance().driveService.files().list()
            request.q =
                "mimeType = 'application/vnd.google-apps.folder' and name = '" + Define.FOLDER_NAME + "'"
            do {
                val files = request.execute()
                list.addAll(files.files)
                request.pageToken = files.nextPageToken
            } while (request.pageToken != null && request.pageToken.length > 0)
            if (list.size != 1) {
                throw FatalErrorException("UnlimitedDiaryのフォルダが見つからないか、2つ以上あります")
            }
            return list[0].id
        }

    @get:Throws(IOException::class)
    val allFile: LinkedList<File>
        get() {
            _folderId = folderId
            return getAllFile(_folderId)
        }

    @Throws(IOException::class)
    fun getAllFile(folderId: String?): LinkedList<File> {
        val list =
            LinkedList<File>()
        val request =
            ApiAccessor.getInstance().driveService.files()
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
        return list
    }

    @Throws(IOException::class)
    fun getContent(fileId: String?): String {
        val im =
            ApiAccessor.getInstance().driveService.files()[fileId].executeMediaAsInputStream()
        return StreamUtils.getText(im)
    }

    fun post(fileName:String, content:ByteArray){
        Tasks.call(Executors.newSingleThreadExecutor(), Callable<String>{
            val metadata = File()
                .setParents(listOf(_folderId))
                .setMimeType(Define.mimeTypeText)
                .setName(fileName)

            val googleFile = ApiAccessor.getInstance().driveService.files().create(metadata).execute()
                ?: throw IOException("Null result when requesting file creation.")

            googleFile.id
        })
            .addOnSuccessListener {
                // it: String としてファイルIDを取得できる。
                Tasks.call(Executors.newSingleThreadExecutor(), Callable<Void>{
                    val metadata = File().setName(fileName)
                    val contentStream = ByteArrayContent(Define.mimeTypeText, content)
                    ApiAccessor.getInstance().driveService.files().update( it , metadata, contentStream).execute()

                    null
                })
                    .addOnSuccessListener {
                        Lg.e("投稿成功")
                    }
                    .addOnFailureListener {
                        Lg.e("投稿失敗")
                    }
            }
            .addOnFailureListener {
                Lg.e("新規ファイル作成に失敗")
            }
    }
}