package net.dixq.unlimiteddiary;

import android.accounts.AccountManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.ListView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAuthIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import net.dixq.unlimiteddiary.utils.Lg;
import net.dixq.unlimiteddiary.utils.StopWatch;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity3 extends AppCompatActivity {

    private static final int REQUEST_ACCOUNT_PICKER = 1;
    private static final int REQUEST_AUTHORIZATION = 2;
    private GoogleAccountCredential _credential;
    private Drive _driveService;
    private List<File> _fileList = new ArrayList<File>();
    private Handler _handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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
            _driveService = getDriveService();
            // Drive API呼び出し
            callDriveApi();
        }
    }

    private void callDriveApi() {
        // 非UIスレッドで呼び出す
        // （AsyncTask等でAPI呼び出し用の基底クラスをつくって認証が必要な場合の処理を実施したほうが良い）
        (new Thread(new Runnable() {
            @Override
            public void run() {
                StopWatch sw = new StopWatch();
                String folderID = "";
                _fileList.clear();
                try {
                    // ファイル一覧を取得したい場合
                    Drive.Files.List request = _driveService.files().list();
                    // クエリ文字列をセット（以下はMIMETYPEを指定）
                    request.setQ("mimeType = 'application/vnd.google-apps.folder' and name = 'UnlimitedDiary'");

                    do {
                        // API呼び出し
                        FileList files = request.execute();
                        // 取得したFileリストを保持用のメンバーにセット
                        _fileList.addAll(files.getFiles());
                        // 全アイテムを取得するために繰り返し
                        request.setPageToken(files.getNextPageToken());
                    } while (request.getPageToken() != null
                            && request.getPageToken().length() > 0);

                    for (File f : _fileList) {
//                      Lg.d(f.getName());
                        if(f.getName().equals("UnlimitedDiary")){
                            Lg.e("mimeType:"+f.getMimeType());
                            folderID = f.getId();
                        }
                    }

                } catch (UserRecoverableAuthIOException e) {
                    // 認証が必要な場合に発生するException
                    // これが発生したら認証のためのIntent発行を行い、認証後、DriveAPIを再呼び出しする
                    final Intent authIntent = e.getIntent();
                    _handler.post(new Runnable() {
                        @Override
                        public void run() {
                            startActivityForResult(authIntent, REQUEST_AUTHORIZATION);
                        }
                    });
                } catch (GoogleAuthIOException e) {
                    // Developer ConsoleでClientIDを設定していない場合に発生する
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                _fileList.clear();
                {
                    try {
                        Drive.Files.List request = _driveService.files().list().setQ("'" + folderID + "' in parents and mimeType != 'application/vnd.google-apps.folder' and trashed = false");
                        do {
                            // API呼び出し
                            FileList files = request.execute();
                            // 取得したFileリストを保持用のメンバーにセット
                            _fileList.addAll(files.getFiles());
                            // 全アイテムを取得するために繰り返し
                            request.setPageToken(files.getNextPageToken());
                        } while (request.getPageToken() != null
                                && request.getPageToken().length() > 0);

                        Lg.e("ここから");
                        for (File f : _fileList) {
                            Lg.w("name:"+f.getName());
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Lg.e("time:"+sw.getDiff());

                    ArrayList<DiaryData> list = new ArrayList<>();
                    DiaryData dat = new DiaryData(true, 2020, 4);
                    list.add(dat);
                    for(File file : _fileList){
                        try {
                            InputStream im = _driveService.files().get(file.getId()).executeMediaAsInputStream();
                            Reader reader = new InputStreamReader(im, "utf-8");
                            BufferedReader bufferedReader = new BufferedReader(reader);
                            String[] split = file.getName().split("\\.");
                            dat = new DiaryData(false, Integer.parseInt(split[2]), bufferedReader.readLine());
                            dat._year = Integer.parseInt(split[0]);
                            dat._month = Integer.parseInt(split[1]);
                            dat._day = Integer.parseInt(split[2]);
                            dat._hour = Integer.parseInt(split[3]);
                            dat._min = Integer.parseInt(split[4]);
                            list.add(dat);
                        } catch (IOException e) { }
                    }
                    ItemAdapter adapter = new ItemAdapter(MainActivity3.this, list);
                    ListView listView = findViewById(R.id.list);
                    _handler.post(()-> listView.setAdapter(adapter));
                }
            }
        })).start();
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
                        _driveService = getDriveService();
                        // Drive API呼び出し
                        callDriveApi();
                        // 必要ならアカウント名を保存しておく
                        saveAccountName(accountName);
                    }
                } else {
                    // エラー処理
                }
                break;

            case REQUEST_AUTHORIZATION:
                if (resultCode == RESULT_OK) {
                    // 認証に成功したのでDrive APIを再呼び出し
                    callDriveApi();
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
        editor.commit();
    }
}
