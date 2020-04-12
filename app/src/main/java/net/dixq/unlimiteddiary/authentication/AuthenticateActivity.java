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

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;

import net.dixq.unlimiteddiary.google_api.DriveHelper;
import net.dixq.unlimiteddiary.singleton.ApiAccessor;
import net.dixq.unlimiteddiary.top.TopActivity;
import java.util.Arrays;

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
            proceesNextStep();
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
                    proceesNextStep();
                } else {
                    finish();
                }

                break;
            case REQUEST_CODE_TOP:
                finish();
                break;
        }
    }

    private void proceesNextStep(){
        ApiAccessor.getInstance().setDriveService(getDriveService());
        new Thread(()->{
            DriveHelper driveHelper = new DriveHelper(ApiAccessor.getInstance());
            ApiAccessor.getInstance().setFolderId(driveHelper.getFolderId());
            _handler.post(this::startNextActivity);
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

}

