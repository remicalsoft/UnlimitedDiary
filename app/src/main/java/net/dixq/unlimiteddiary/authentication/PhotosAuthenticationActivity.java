package net.dixq.unlimiteddiary.authentication;
import android.accounts.Account;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Scope;

import net.dixq.unlimiteddiary.R;
import net.dixq.unlimiteddiary.gomi.MainActivity;
import net.dixq.unlimiteddiary.utils.Lg;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class PhotosAuthenticationActivity extends AppCompatActivity {

    private GoogleSignInAccount mGoogleSignInAccount = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume(){
        super.onResume();
        choseAccount();
    }

    public void choseAccount() {
        try {
            Scope mScope = new Scope("https://www.googleapis.com/auth/photoslibrary");
            GoogleSignInOptions gso = new GoogleSignInOptions
                    .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestScopes(mScope)
                    .requestEmail()
                    .build();
            GoogleApiClient mGoogleApiClient = new GoogleApiClient
                    .Builder(this)
                    .enableAutoManage(this, null)
                    .addScope(mScope)
                    .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                    .build();
            Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
            startActivityForResult(signInIntent, 1);
        } catch (Exception e) {
            Log.d("TAG", e.getMessage());
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                mGoogleSignInAccount = result.getSignInAccount();
            }
            if (mGoogleSignInAccount != null) {
                AsyncTask<Void, Void, String> task = new AsyncTask<Void, Void, String>() {
                    @Override
                    protected String doInBackground(Void... params) {
                        String accessToken = null;
                        try {
                            accessToken = GoogleAuthUtil.getToken(
                                    PhotosAuthenticationActivity.this,
                                    new Account(mGoogleSignInAccount.getEmail(), GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE),
                                    "oauth2:https://www.googleapis.com/auth/photoslibrary");
                            Request request = new Request.Builder()
                                    .url("https://photoslibrary.googleapis.com/v1/albums/")
                                    .addHeader("Authorization", "Bearer " + accessToken)
                                    .build();
                            OkHttpClient client = new OkHttpClient();
                            Response response = null;
                            try {
                                response = client.newCall(request).execute();
                                String r = response.body().string();
                                getAlbumId(r);
//                                Lg.w("json:\n"+r);
//                                Album album = new JsonParser().parse(r);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        } catch (IOException transientEx) {
                            Log.e("TAG", transientEx.toString());
                        } catch (UserRecoverableAuthException e) {
                            Log.e("TAG", e.toString());
                        } catch (GoogleAuthException authEx) {
                            Log.e("TAG", authEx.toString());
                        }
                        return accessToken;
                    }

                    @Override
                    protected void onPostExecute(String token) {
                        Log.i("TAG", "Access token retrieved:" + token);
                    }
                };
                task.execute();
            }
        }
    }

    private String getAlbumId(String json){
        String[]split = json.split(",");
        for(int i=0; i<split.length; i++){
            if(split[i].contains("\"title\": \"UnlimitedDiary\"")){
                Lg.e("あった！:["+split[i-1]+"]");
            }
        }
        return "";
    }

}