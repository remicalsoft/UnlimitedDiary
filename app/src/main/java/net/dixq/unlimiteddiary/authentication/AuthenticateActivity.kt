package net.dixq.unlimiteddiary.authentication

import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
import com.google.android.material.textfield.TextInputEditText
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import net.dixq.unlimiteddiary.R
import net.dixq.unlimiteddiary.common.Lg
import net.dixq.unlimiteddiary.common.OkDialog
import net.dixq.unlimiteddiary.common.PrefUtils
import net.dixq.unlimiteddiary.common.google_api.DriveHelper
import net.dixq.unlimiteddiary.common.singleton.ApiAccessor
import net.dixq.unlimiteddiary.top.TopActivity
import java.util.*


class AuthenticateActivity : AppCompatActivity(), View.OnClickListener {

    private var _credential: GoogleAccountCredential? = null
    private val _handler = Handler()
    private var _isAuthenticationReady = false
    private var _isNameReady = false
    private var _buttons = Buttons()

    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        if(!isGoogleApiAvailable()){
            OkDialog(this, "GooglePlay開発者サービスをインストールするか有効にしてください。",  DialogInterface.OnClickListener { _, _ ->
                startGooglePlay()
                finish()
            }).show()
            return
        }
        if (PrefUtils.read(this, KEY_HANDLE_NAME).isEmpty()) {
            setContentView(R.layout.main_authentication)
            findViewById<View>(R.id.button_submit).setOnClickListener(this)
            _buttons.layout(this)
        } else {
            _isNameReady = true
        }
        setupAuthentication()
    }

    private fun isGoogleApiAvailable() :Boolean{
        val availability = GoogleApiAvailability.getInstance()
        return when(availability.isGooglePlayServicesAvailable(this)){
            ConnectionResult.SUCCESS -> true
            ConnectionResult.SERVICE_DISABLED,
            ConnectionResult.SERVICE_MISSING,
            ConnectionResult.SERVICE_UPDATING,
            ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED,
            ConnectionResult.SERVICE_INVALID,
            ConnectionResult.SERVICE_DISABLED -> false
            else -> false
        }
    }

    private fun startGooglePlay() {
        val googlePlayIntent = Intent(Intent.ACTION_VIEW);
        googlePlayIntent.data = Uri.parse("market://details?id=com.google.android.gms")
        startActivityForResult(googlePlayIntent, REQUEST_GOOGLE_PLAY);
    }

    private fun setupAuthentication() {
        val account: GoogleSignInAccount? = GoogleSignIn.getLastSignedInAccount(this)
        if(account==null) {
            val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestScopes(Scope(DriveScopes.DRIVE))
                .build()
            var googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions)
            startActivityForResult(googleSignInClient.signInIntent, REQUEST_ACCOUNT_PICKER)
        } else {
            // ログインしていれば、認証スキップ
            _credential = GoogleAccountCredential.usingOAuth2(
                this, Collections.singleton(DriveScopes.DRIVE)
            )
            _credential!!.selectedAccount = account.account
            _isAuthenticationReady = true
            proceesNextStep()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_ACCOUNT_PICKER-> {
                val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                try {
                    val account: GoogleSignInAccount? = task.getResult(ApiException::class.java)
                    Lg.e("成功")
                    _credential = GoogleAccountCredential.usingOAuth2(
                        this, Collections.singleton(DriveScopes.DRIVE)
                    )
                    _credential!!.selectedAccount = account!!.account
                    _isAuthenticationReady = true
                    proceesNextStep()
                } catch (e: Exception) {
                    Lg.e("失敗")
                }
            }
            else -> {
                Lg.e("その他！");
            }
        }
    }

    override fun onClick(v: View) {
        val edt = findViewById<TextInputEditText>(R.id.edt_body)
        if (edt.text.toString().isEmpty()) {
            OkDialog(
                this,
                "ニックネームを入力してください。",
                null
            ).show()
            return
        }
        val color = _buttons.getColor()
        if(color == -1){
            OkDialog(this, "カラーを選択してください。", null).show()
            return
        }
        PrefUtils.save(this, KEY_HANDLE_NAME, edt.text.toString())
        PrefUtils.save(this, KEY_HANDLE_NAME_COLOR, color.toString())
        _isNameReady = true
        proceesNextStep()
    }

    private fun proceesNextStep() {
        if (!_isAuthenticationReady || !_isNameReady) {
            return
        }
        ApiAccessor.getInstance().driveService = driveService
        Thread(Runnable {
            val driveHelper = DriveHelper(ApiAccessor.getInstance())
            ApiAccessor.getInstance().folderId = driveHelper.getFolderId()
            _handler.post { startNextActivity() }
        }).start()
    }

    private fun startNextActivity() {
        val intent = Intent(this, TopActivity::class.java)
        startActivityForResult(intent, REQUEST_CODE_TOP)
    }

    private val driveService: Drive
        private get() = Drive.Builder(
            AndroidHttp.newCompatibleTransport(),
            GsonFactory(),
            _credential
        ).build()

    companion object {
        const val REQUEST_CODE_TOP = 0
        const val REQUEST_ACCOUNT_PICKER = 1
        const val REQUEST_GOOGLE_PLAY = 2
        public const val KEY_HANDLE_NAME = "KEY_HANDLE_NAME"
        public const val KEY_HANDLE_NAME_COLOR = "KEY_HANDLE_NAME_COLOR"
    }
}