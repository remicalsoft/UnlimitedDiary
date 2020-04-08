package net.dixq.unlimiteddiary.authentication;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.FileUtils;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.api.gax.rpc.ApiException;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.common.collect.ImmutableList;
import com.google.photos.library.v1.PhotosLibraryClient;
import com.google.photos.library.v1.PhotosLibrarySettings;
import com.google.photos.library.v1.internal.InternalPhotosLibraryClient;
import com.google.photos.library.v1.proto.Album;
import com.google.photos.library.v1.proto.MediaItem;

import com.google.photos.library.v1.PhotosLibraryClient;
import com.google.photos.library.v1.PhotosLibrarySettings;

import net.dixq.unlimiteddiary.R;
import net.dixq.unlimiteddiary.google_api.PhotosApiHelper;
import net.dixq.unlimiteddiary.singleton.ApiAccessor;
import net.dixq.unlimiteddiary.top.TopActivity;
import net.dixq.unlimiteddiary.utils.Lg;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class AuthenticateActivity extends AppCompatActivity {

    public static final int REQUEST_CODE_TOP = 0;
    public static final int REQUEST_ACCOUNT_PICKER = 1;
    private GoogleAccountCredential _credential;
    private Handler _handler = new Handler();

    @Override
    protected void onCreate(Bundle b){
        super.onCreate(b);
        setupAuthentication();
    }

    private void setupAuthentication() {
        _credential = GoogleAccountCredential.usingOAuth2(this, Arrays.asList(DriveScopes.DRIVE,"https://www.googleapis.com/auth/photoslibrary"));
        String accountName = getSavedAccountName();
        if (accountName.isEmpty()) {
            startActivityForResult(_credential.newChooseAccountIntent(), AuthenticateActivity.REQUEST_ACCOUNT_PICKER);
        } else {
            _credential.setSelectedAccountName(accountName);
            setupApi();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case AuthenticateActivity.REQUEST_ACCOUNT_PICKER:
                if (resultCode == RESULT_OK && data != null) {
                    String accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    String accountType = data.getStringExtra(AccountManager.KEY_ACCOUNT_TYPE);
                    saveAccountName(accountName);
                    saveAccountType(accountType);
                    _credential.setSelectedAccountName(accountName);

                    setupApi();
                } else {
                    finish();
                }

                break;
            case REQUEST_CODE_TOP:
                finish();
                break;
        }
    }

    private void setupApi(){
        ApiAccessor.getInstance().setDriveService(getDriveService());

        new Thread(()-> {
            try {
                String accountName = getSavedAccountName();
                String accountType = getSavedAccountType();
                String accessToken = GoogleAuthUtil.getToken(this, new Account(accountName, accountType), "oauth2:https://www.googleapis.com/auth/photoslibrary");
                ApiAccessor.getInstance().setPhotosApiAccessToken(accessToken);
                ApiAccessor.getInstance().setFolderId(new PhotosApiHelper().getFolderId());
//                test();
                _handler.post(()->startNextActivity());
            } catch (IOException | GoogleAuthException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void startNextActivity(){
        Intent intent = new Intent(this, TopActivity.class);
        startActivityForResult(intent, REQUEST_CODE_TOP);
    }

    private Drive getDriveService() {
        return new Drive.Builder(AndroidHttp.newCompatibleTransport(), new GsonFactory(), _credential).build();
    }

    private static final String TAG_AUTH_ACOUNT = "TAG_AUTH_ACOUNT";
    private static final String KEY_AUTH_ACOUNT_NAME = "KEY_AUTH_ACOUNT_NAME";
    private static final String KEY_AUTH_ACOUNT_TYPE = "KEY_AUTH_ACOUNT_TYPE";
    private String getSavedAccountName() {
        return getSharedPreferences(TAG_AUTH_ACOUNT, Context.MODE_PRIVATE)
                .getString(KEY_AUTH_ACOUNT_NAME, "");
    }
    private void saveAccountName(final String accountName) {
        getSharedPreferences(TAG_AUTH_ACOUNT, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_AUTH_ACOUNT_NAME, accountName)
                .apply();
    }
    private String getSavedAccountType() {
        return getSharedPreferences(TAG_AUTH_ACOUNT, Context.MODE_PRIVATE)
                .getString(KEY_AUTH_ACOUNT_TYPE, "");
    }
    private void saveAccountType(final String accountType) {
        getSharedPreferences(TAG_AUTH_ACOUNT, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_AUTH_ACOUNT_TYPE, accountType)
                .apply();
    }

   private void test() throws IOException {
//       Resources res = this.getResources();
//       InputStream is = res.openRawResource(R.raw.cre);
       String json = "{\"installed\":{\"client_id\":\"874276609166-1ca6o010mrmp3l38mblokvinlvr4fcaa.apps.googleusercontent.com\",\"project_id\":\"unlimiteddiary\",\"auth_uri\":\"https://accounts.google.com/o/oauth2/auth\",\"token_uri\":\"https://oauth2.googleapis.com/token\",\"auth_provider_x509_cert_url\":\"https://www.googleapis.com/oauth2/v1/certs\",\"redirect_uris\":[\"urn:ietf:wg:oauth:2.0:oob\",\"http://localhost\"]}}";
       InputStream is = new ByteArrayInputStream(json.getBytes("utf-8"));

//       AssetManager as = getResources().getAssets();
//       InputStream is = as.open("cre.json");

       GoogleCredentials myCredentials = GoogleCredentials.fromStream(is).createScoped(Arrays.asList("https://www.googleapis.com/auth/photoslibrary"));;

// Set up the Photos Library Client that interacts with the API
       PhotosLibrarySettings settings =
               PhotosLibrarySettings.newBuilder()
                       .setCredentialsProvider(
                               FixedCredentialsProvider.create(myCredentials))
                       .build();

       try (PhotosLibraryClient photosLibraryClient =
                    PhotosLibraryClient.initialize(settings)) {

           try {
               // Make a request to list all media items in the user's library
               // Iterate over all the retrieved media items
               // Pagination is handled automatically
               InternalPhotosLibraryClient.ListMediaItemsPagedResponse response = photosLibraryClient.listMediaItems();
               for (MediaItem item : response.iterateAll()) {
                   // Get some properties of a media item
                   String id = item.getId();
                   String description = item.getDescription();
                   String mimeType = item.getMimeType();
                   String productUrl = item.getProductUrl();
                   String filename = item.getFilename();
                   Lg.e("filename:"+filename);
               }
           } catch (ApiException e) {
               // Handle error
           }

           // Create a new Album  with at title
//           Album createdAlbum = photosLibraryClient.createAlbum("My Album");

           // Get some properties from the album, such as its ID and product URL
//           String id = album.getId();
//           String url = album.getProductUrl();

       } catch (ApiException e) {
           // Error during album creation
       }

    }

}

