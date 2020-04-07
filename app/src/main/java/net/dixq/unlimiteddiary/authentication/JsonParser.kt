package net.dixq.unlimiteddiary.authentication

import com.squareup.moshi.Json
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import net.dixq.unlimiteddiary.utils.Lg

class JsonParser {
    fun parse(content:String):Album{
        val adapter = Moshi.Builder().build().adapter(Album::class.java)
        val album = adapter.fromJson(content)
        Lg.e("album id : "+album!!.id)
        return album
    }
    fun parseToList(content:String){
        val moshi = Moshi.Builder().build()
        val type = Types.newParameterizedType(List::class.java, Album::class.java)
        val listAdapter: JsonAdapter<List<Album>> = moshi.adapter(type)
        val listObject = listAdapter.fromJson(content)
        if (listObject == null) {
            Lg.e("null!")
            return
        }
        for(str in listObject){
            Lg.e(str.toString())
        }
    }
}

data class Album(val id:String, val title:String, val productUrl:String, val mediaItemCount:String, val coverPhotoBaseUrl:String, val coverPhotoMediaItemId:String){

    @field:Json(name = "last_update")   // @Json(name = "last_update")
    var lastUpdate: Long = 0                // ↑これだと、変換されなかった

    override fun toString():String =
        "id:$id title:$title productUrl:$productUrl mediaItemCount:$mediaItemCount coverPhotoBaseUrl:$coverPhotoBaseUrl coverPhotoMediaItemId:$coverPhotoMediaItemId"
}