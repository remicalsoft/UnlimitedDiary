package net.dixq.unlimiteddiary.authentication

import android.accounts.AccountManager
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import net.dixq.unlimiteddiary.R
import net.dixq.unlimiteddiary.common.PrefUtils
import net.dixq.unlimiteddiary.common.google_api.DriveHelper
import net.dixq.unlimiteddiary.common.singleton.ApiAccessor
import net.dixq.unlimiteddiary.top.TopActivity
import net.dixq.unlimiteddiary.common.OkDialog


class AuthenticateActivity : AppCompatActivity(), View.OnClickListener {

    private var _credential: GoogleAccountCredential? = null
    private val _handler = Handler()
    private var _isAuthenticationReady = false
    private var _isNameReady = false
    private var _buttons = Buttons()

    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        if (PrefUtils.read(this, KEY_HANDLE_NAME).isEmpty()) {
            setContentView(R.layout.main_authentication)
            findViewById<View>(R.id.button_submit).setOnClickListener(this)
            _buttons.layout(this)
        } else {
            _isNameReady = true
        }
        setupAuthentication()
    }

    private fun setupAuthentication() {
        val credential = GoogleAccountCredential.usingOAuth2(
            this,
            listOf(
                DriveScopes.DRIVE,
                "https://www.googleapis.com/auth/photoslibrary"
            )
        )
        _credential = credential
        val accountName =
            PrefUtils.read(this, KEY_AUTH_ACOUNT_NAME)
        if (accountName.isEmpty()) {
            startActivityForResult(
                credential.newChooseAccountIntent(),
                REQUEST_ACCOUNT_PICKER
            )
        } else {
            credential.setSelectedAccountName(accountName)
            _isAuthenticationReady = true
            proceesNextStep()
        }
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_ACCOUNT_PICKER -> if (resultCode == Activity.RESULT_OK && data != null) {
                val accountName =
                    data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME)
                val accountType =
                    data.getStringExtra(AccountManager.KEY_ACCOUNT_TYPE)
                PrefUtils.save(
                    this,
                    KEY_AUTH_ACOUNT_NAME,
                    accountName
                )
                PrefUtils.save(
                    this,
                    KEY_AUTH_ACOUNT_TYPE,
                    accountType
                )
                _credential!!.selectedAccountName = accountName
                _isAuthenticationReady = true
                proceesNextStep()
            } else {
                finish()
            }
            REQUEST_CODE_TOP -> finish()
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
        private const val KEY_AUTH_ACOUNT_NAME = "KEY_AUTH_ACOUNT_NAME"
        private const val KEY_AUTH_ACOUNT_TYPE = "KEY_AUTH_ACOUNT_TYPE"
        public const val KEY_HANDLE_NAME = "KEY_HANDLE_NAME"
        public const val KEY_HANDLE_NAME_COLOR = "KEY_HANDLE_NAME_COLOR"
    }
}