package net.dixq.unlimiteddiary.google_api;

import net.dixq.unlimiteddiary.authentication.JsonParser;
import net.dixq.unlimiteddiary.singleton.ApiAccessor;
import net.dixq.unlimiteddiary.utils.Lg;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class PhotosApiHelper {
    public String getFolderId(){
        String accessToken = ApiAccessor.getInstance().getPhotosApiAccessToken();
        Request request = new Request.Builder()
                .url("https://photoslibrary.googleapis.com/v1/albums/")
                .addHeader("Authorization", "Bearer " + accessToken)
                .build();
        OkHttpClient client = new OkHttpClient();
        Response response = null;
        try {
            response = client.newCall(request).execute();
            String body = response.body().string();
            Lg.d("body:" + body);
            return new JsonParser().getFolderId(body);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void showInFolderList(String albumId){


    }

}
