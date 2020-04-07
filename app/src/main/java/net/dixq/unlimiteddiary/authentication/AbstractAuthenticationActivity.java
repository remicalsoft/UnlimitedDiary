package net.dixq.unlimiteddiary.authentication;

import android.accounts.AccountManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;

import net.dixq.unlimiteddiary.singleton.ApiAccessor;
import net.dixq.unlimiteddiary.top.TopActivity;

import java.util.Arrays;

public abstract class AbstractAuthenticationActivity extends AppCompatActivity {

    protected GoogleAccountCredential _credential;
    protected static final int REQUEST_ACCOUNT_PICKER = 1;
    protected static final int REQUEST_AUTHORIZATION = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupDriveService();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_ACCOUNT_PICKER:
                if ((resultCode == RESULT_OK) && (data != null)) {
                    // 選択されたアカウント名を取得
                    String accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        _credential.setSelectedAccountName(accountName);
                        setApi();
                        // 必要ならアカウント名を保存しておく
                        saveAccountName(accountName);
                        // Drive API呼び出し
                        startNextActivity();
                    }
                } else {
                    // エラー処理
                }
                break;

            case REQUEST_AUTHORIZATION:
                if (resultCode == RESULT_OK) {
                    // 認証に成功したのでDrive APIを再呼び出し
                    startNextActivity();
                } else {
                    // エラー処理
                }
                break;
        }
    }

    protected abstract GoogleAccountCredential getCredential();
    protected abstract void setApi();
    protected abstract String getTagForSave();
    protected abstract String getKeyForSave();
    protected abstract Intent getNextIntent();

    private void setupDriveService() {
        _credential = getCredential();
        // 保存済みのアカウント名を取得（xxx@gmail.com）
        String accountName = getSavedAccountName();
        if (accountName.isEmpty()) {
            // アカウント名がなければアカウント選択ダイアログ表示用のIntentを発行
            startActivityForResult(_credential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);
        } else {
            // アカウント名がある場合
            _credential.setSelectedAccountName(accountName);
            setApi();
            startNextActivity();
        }
    }

    private void startNextActivity(){
        startActivity(getNextIntent());
    }

    private String getSavedAccountName() {
        SharedPreferences prefs = getSharedPreferences(getTagForSave(), Context.MODE_PRIVATE);
        return prefs.getString(getKeyForSave(), "");
    }

    private void saveAccountName(final String accountName) {
        SharedPreferences prefs = getSharedPreferences(getTagForSave(), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(getKeyForSave(), accountName);
        editor.apply();
    }

}
