package net.dixq.unlimiteddiary.authentication;

import android.accounts.AccountManager;
import android.app.Activity;
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
import net.dixq.unlimiteddiary.top.TopActivity;
import net.dixq.unlimiteddiary.singleton.DriveAccessor;
import net.dixq.unlimiteddiary.write.WriteActivity;

import java.util.Arrays;

public class AuthenticationActivity extends AppCompatActivity {

    private GoogleAccountCredential _credential;
    private static final int REQUEST_ACCOUNT_PICKER = 1;
    private static final int REQUEST_AUTHORIZATION = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupDriveService();
    }

    private void setupDriveService() {
        _credential = GoogleAccountCredential.usingOAuth2(this, Arrays.asList(DriveScopes.DRIVE));
        // 保存済みのアカウント名を取得（xxx@gmail.com）
        String accountName = getSavedAccountName();
        if (accountName.isEmpty()) {
            // アカウント名がなければアカウント選択ダイアログ表示用のIntentを発行
            startActivityForResult(_credential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);
        } else {
            // アカウント名がある場合
            _credential.setSelectedAccountName(accountName);
            // DriveAPI用のインスタンス生成
            DriveAccessor.getInstance().setService(getDriveService());
            startNextActivity();
        }
    }

    private void startNextActivity(){
        Intent intent = new Intent(this, TopActivity.class);
        startActivity(intent);
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
                        DriveAccessor.getInstance().setService(getDriveService());
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

    private Drive getDriveService() {
        return new Drive.Builder(AndroidHttp.newCompatibleTransport(), new GsonFactory(), _credential).build();
    }

    private String getSavedAccountName() {
        SharedPreferences prefs = getSharedPreferences("prefs_name", Context.MODE_PRIVATE);
        return prefs.getString("key_account_name", "");
    }

    private void saveAccountName(final String accountName) {
        SharedPreferences prefs = getSharedPreferences("prefs_name", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("key_account_name", accountName);
        editor.apply();
    }
}
