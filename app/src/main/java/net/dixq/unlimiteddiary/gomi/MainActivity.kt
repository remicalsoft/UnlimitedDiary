package net.dixq.unlimiteddiary.gomi

import android.app.ProgressDialog.show
import android.content.Intent
import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity;
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.api.Scope
import com.google.android.gms.tasks.Tasks
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.ByteArrayContent
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.api.services.drive.model.FileList

import java.io.IOException
import java.util.*
import java.util.concurrent.Callable
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {
//
//    var googleSignInClient: GoogleSignInClient? = null
//    var driveService: Drive? = null
//    val REQUEST_CODE_SIGNIN = 1
//    val REQUEST_CODE_AVAILABILITY = 2
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setSupportActionBar(toolbar)
//
//        fab.setOnClickListener { view ->
//            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                .setAction("Action", null).show()
//        }
//        if(isGoogleApiAvailable()) {
//            signIn()
//        }
//    }
//
//    private fun isGoogleApiAvailable() :Boolean{
//        val availability = GoogleApiAvailability.getInstance()
//        val result = availability.isGooglePlayServicesAvailable(this)
//        return when(result){
//            ConnectionResult.SUCCESS ->
//                true
//            ConnectionResult.SERVICE_MISSING,
//            ConnectionResult.SERVICE_DISABLED -> {
//                val dialog = availability.getErrorDialog(
//                    this, result, REQUEST_CODE_AVAILABILITY, null)
//                dialog.show()
//                false
//            }
//            else -> {
//                OkDialog(this,"Please connect to internet", null)
//                false
//            }
//        }
//    }
//
//    private fun signIn() {
//        val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
//            .requestScopes(Scope(DriveScopes.DRIVE_FILE))
//            .build()
//        googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions)
//        googleSignInClient?.apply {
//            startActivityForResult(this.signInIntent, REQUEST_CODE_SIGNIN)
//        }
//    }
//
//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//
//        if (requestCode == REQUEST_CODE_SIGNIN) {
//            if (resultCode == RESULT_OK && data != null) {
//                handleSignInResult(data)
//            } else {
//                OkDialog(this, "使えません", null).show()
//            }
//        }
//    }
//
//    private fun handleSignInResult(intent: Intent) {
//        GoogleSignIn.getSignedInAccountFromIntent(intent)
//            .addOnSuccessListener {
//                Toast.makeText(this, "ログイン成功", Toast.LENGTH_LONG).show()
//                val credential = GoogleAccountCredential.usingOAuth2(
//                    this, Collections.singleton(DriveScopes.DRIVE_FILE)
//                )
//                credential.selectedAccount = it.account
//                driveService = Drive.Builder(
//                    AndroidHttp.newCompatibleTransport(),
//                    GsonFactory(),
//                    credential
//                ).setApplicationName("Drive API").build()
//                startDriveProcess()
//            }
//            .addOnFailureListener {
//                OkDialog(this, "ログイン失敗", null).show()
//            }
//    }
//
//    private fun startDriveProcess() {
//
//        var fileID = ""
//        Tasks.call(Executors.newSingleThreadExecutor(), Callable<String>{
//            val metadata = com.google.api.services.drive.model.File()
//                .setParents(listOf("root"))
//                .setMimeType("text/plain")
//                .setName("2020.04.05.0.txt")
//
//            val googleFile = driveService!!.files().create(metadata).execute()
//                ?: throw IOException("Null result when requesting file creation.")
//
//            googleFile.id
//        })
//            .addOnSuccessListener {
//                // it: String としてファイルIDを取得できる。
//                fileID = it
//                Toast.makeText(this, "ファイルID取得成功！", Toast.LENGTH_LONG).show()
//            }
//            .addOnFailureListener {
//                // 失敗
//                Toast.makeText(this, "ファイルID取得失敗:"+it.message, Toast.LENGTH_LONG).show()
//            }
//
//        Tasks.call(Executors.newSingleThreadExecutor(), Callable<Void>{
//            val data = "<title>test</title>".toByteArray()
//            val metadata = com.google.api.services.drive.model.File().setName("2020.04.05.0.txt")
//            val contentStream = ByteArrayContent( "text/plain", data)
//            driveService!!.files().update( fileID , metadata, contentStream).execute()
//
//            null
//        })
//            .addOnSuccessListener {
//                // 成功
//                Toast.makeText(this, "アップロード成功", Toast.LENGTH_LONG).show()
//            }
//            .addOnFailureListener {
//                // 失敗
//                Toast.makeText(this, "アップロード失敗", Toast.LENGTH_LONG).show()
//            }
////
////        Tasks.call<FileList>(
////            Executors.newSingleThreadExecutor(),
////            Callable<FileList> {
////                driveService!!.files().list()
////                    .setSpaces("drive")
////                    .setFields("nextPageToken, files(id, name, modifiedTime)")
////                    .execute()
////            })
////            .addOnSuccessListener {
////                // アクセス成功
////                // it.files :List<File> にファイル情報が来る
////                for(file in it.files) {
////                    Toast.makeText(this, "name:"+file.name, Toast.LENGTH_LONG).show()
////                }
////            }
////            .addOnFailureListener {
////                Toast.makeText(this, "アクセス失敗", Toast.LENGTH_LONG).show()
////            }
//
//    }
//
//    override fun onCreateOptionsMenu(menu: Menu): Boolean {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        menuInflater.inflate(R.menu.menu_main, menu)
//        return true
//    }
//
//    fun logout(){
//        googleSignInClient?.apply {
//            this.signOut()
//                .addOnSuccessListener {
//                    // ログアウト成功
//                }
//                .addOnFailureListener {
//                    // ログアウト失敗
//                }
//        }
//    }
//
//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        return when (item.itemId) {
//            R.id.action_settings -> true
//            else -> super.onOptionsItemSelected(item)
//        }
//    }
}
