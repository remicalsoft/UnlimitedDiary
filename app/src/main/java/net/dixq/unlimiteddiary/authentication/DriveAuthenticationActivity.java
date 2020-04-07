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
import net.dixq.unlimiteddiary.singleton.ApiAccessor;

import java.util.Arrays;


public class DriveAuthenticationActivity extends AbstractAuthenticationActivity {

    private static final String TAG_SAVE_DRIVE_ACCOUNT = "TAG_SAVE_DRIVE_ACCOUNT";
    private static final String KEY_SAVE_DRIVE_ACCOUNT = "KEY_SAVE_DRIVE_ACCOUNT";

    @Override
    protected GoogleAccountCredential getCredential() {
        return GoogleAccountCredential.usingOAuth2(this, Arrays.asList(DriveScopes.DRIVE));
    }

    @Override
    protected void setApi() {
        // DriveAPI用のインスタンス生成
        ApiAccessor.getInstance().setDriveService(getDriveService());
    }

    @Override
    protected String getTagForSave() {
        return TAG_SAVE_DRIVE_ACCOUNT;
    }

    @Override
    protected String getKeyForSave() {
        return KEY_SAVE_DRIVE_ACCOUNT;
    }

    @Override
    protected Intent getNextIntent() {
        return new Intent(this, PhotosAuthenticationActivity.class);
    }

    private Drive getDriveService() {
        return new Drive.Builder(AndroidHttp.newCompatibleTransport(), new GsonFactory(), _credential).build();
    }

}
