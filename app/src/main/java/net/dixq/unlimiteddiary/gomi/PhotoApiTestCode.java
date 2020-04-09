package net.dixq.unlimiteddiary.gomi;

import android.accounts.Account;

import com.google.android.gms.auth.GoogleAuthUtil;

import net.dixq.unlimiteddiary.google_api.PhotosApiHelper;
import net.dixq.unlimiteddiary.singleton.ApiAccessor;
import net.dixq.unlimiteddiary.utils.Lg;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class PhotoApiTestCode {

    private void test(String accessToken) throws IOException, JSONException {
        Lg.e("folderid = "+ ApiAccessor.getInstance().getFolderId());
        String PAGE_SIZE = "99";
        HttpUrl url = new HttpUrl.Builder()
                .scheme("https")
                .host("photoslibrary.googleapis.com")
                .addPathSegment("v1")
                .addPathSegment("mediaItems:search")
                .addQueryParameter("pageSize", PAGE_SIZE)
                .addQueryParameter("albumId", ApiAccessor.getInstance().getFolderId())
                .build();

        OkHttpClient client = new OkHttpClient();
        MediaType MIMEType= MediaType.parse("application/json; charset=utf-8");
        RequestBody requestBody = RequestBody.create (MIMEType,
                "{\"filters\": { " +
                        "\"mediaTypeFilter\": { " +
//                     "\"mediaTypes\": [\"VIDEO\"]" +
                        "\"mediaTypes\": [\"PHOTO\"]" +
                        "}" +
                        "}}"
        );

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + accessToken )
                .post(requestBody)
                .build();

        Response response = client.newCall(request).execute();

        Lg.e("new body:"+response.body().string());
        JSONObject json = new JSONObject(response.body().string());
        JSONArray mediaItems = json.getJSONArray("mediaItems");
        for(int i=0; i<mediaItems.length(); i++){
            JSONObject jsonObject = mediaItems.getJSONObject(i);
            Lg.e("json:"+jsonObject.toString());
        }

    }

    void onActivityResult(){
        new Thread(()-> {
            try {
//                String accountName = getSavedAccountName();
//                String accountType = getSavedAccountType();
//                String accessToken = GoogleAuthUtil.getToken(this, new Account(accountName, accountType), "oauth2:https://www.googleapis.com/auth/photoslibrary");
//                ApiAccessor.getInstance().setPhotosApiAccessToken(accessToken);
//                ApiAccessor.getInstance().setFolderId(new PhotosApiHelper().getFolderId());
//                _handler.post(()->startNextActivity());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

    }

}
