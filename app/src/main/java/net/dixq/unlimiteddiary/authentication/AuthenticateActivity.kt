package net.dixq.unlimiteddiary.authentication

import android.accounts.AccountManager
import android.app.Activity
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.os.Handler
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.view.marginLeft
import com.google.android.material.textfield.TextInputEditText
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import net.dixq.unlimiteddiary.R
import net.dixq.unlimiteddiary.common.PrefUtils
import net.dixq.unlimiteddiary.google_api.DriveHelper
import net.dixq.unlimiteddiary.singleton.ApiAccessor
import net.dixq.unlimiteddiary.top.TopActivity
import net.dixq.unlimiteddiary.uiparts.OkDialog
import net.dixq.unlimiteddiary.utils.convertDpToPx
import java.util.*


class AuthenticateActivity : AppCompatActivity(), View.OnClickListener {
    private var _credential: GoogleAccountCredential? = null
    private val _handler = Handler()
    private var _isAuthenticationReady = false
    private var _isNameReady = false
    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        if (PrefUtils.read(this, KEY_HANDLE_NAME).isEmpty()) {
            setContentView(R.layout.main_authentication)
            findViewById<View>(R.id.button_submit).setOnClickListener(this)
            layoutButtons()
        } else {
            _isNameReady = true
        }
        setupAuthentication()
    }

    private fun layoutButtons(){
        if(_isNameReady){
            return
        }
        val root = findViewById<LinearLayout>(R.id.layout_button_root)
        val rlpm = root.layoutParams as LinearLayout.LayoutParams
        rlpm.gravity = Gravity.CENTER
        root.layoutParams = rlpm

        createLayoutAndButtons(root, 6*0);
        createLayoutAndButtons(root, 6*1);
        createLayoutAndButtons(root, 6*2);
    }

    private fun createLayoutAndButtons(root:LinearLayout, index:Int){
        val layout = LinearLayout(this)
        layout.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        for(i in 0..5){
            layout.addView(createButton(index+i))
        }
        root.addView(layout)
    }

    private fun createButton(id:Int):Button {
        val c = ContextThemeWrapper(this, R.style.ThemeColorButton)
        val button = Button(c)
        val dp54 = convertDpToPx(this, 54)
        val dp60 = convertDpToPx(this, 60)
        button.layoutParams = LinearLayout.LayoutParams(dp54, dp60)
        button.backgroundTintList = ColorStateList.valueOf(getColor(colors[id]))
        return button
    }

    private fun setupAuthentication() {
        val credential = GoogleAccountCredential.usingOAuth2(
            this,
            Arrays.asList(
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
            prepareDrive()
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
                prepareDrive()
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
            OkDialog(this, "ニックネームを入力してください", null)
            return
        }
        PrefUtils.save(
            this,
            KEY_HANDLE_NAME,
            edt.text.toString()
        )
        _isNameReady = true
        proceesNextStep()
    }

    private fun prepareDrive() {
        ApiAccessor.getInstance().driveService = driveService
        Thread(Runnable {
            val driveHelper = DriveHelper(ApiAccessor.getInstance())
            ApiAccessor.getInstance().folderId = driveHelper.getFolderId()
        }).start()
    }

    private fun proceesNextStep() {
        if (_isAuthenticationReady == false || _isNameReady == false) {
            return
        }
        _handler.post { startNextActivity() }
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
        private const val KEY_HANDLE_NAME = "KEY_HANDLE_NAME"

        private val colors = intArrayOf(
            R.color.md_red_800,
            R.color.md_pink_800,
            R.color.md_purple_800,
            R.color.md_deep_purple_800,
            R.color.md_indigo_800,
            R.color.md_blue_800,
            R.color.md_light_blue_800,
            R.color.md_cyan_800,
            R.color.md_teal_800,
            R.color.md_green_800,
            R.color.md_light_green_800,
            R.color.md_lime_800,
            R.color.md_yellow_800,
            R.color.md_amber_800,
            R.color.md_orange_800,
            R.color.md_deep_orange_800,
            R.color.md_brown_800,
            R.color.md_blue_grey_800
        )
    }
}