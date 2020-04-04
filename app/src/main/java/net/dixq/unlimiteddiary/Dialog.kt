package net.dixq.unlimiteddiary

import android.content.Context
import android.content.DialogInterface
import androidx.appcompat.app.AlertDialog

class OkDialog (
    private val _context: Context,
    private val _message:String,
    private val _listener: DialogInterface.OnClickListener?
){

    private var _dialog:AlertDialog? = null

    fun show(){
        _dialog = AlertDialog.Builder(_context)
            .setMessage(_message)
            .setPositiveButton("OK", _listener)
            .create()
        _dialog?.show()
    }

    fun dismiss(){
        _dialog?.dismiss()
    }

}

class OkCancelDialog(
    private val _context: Context,
    private val _message:String,
    private val _okListener:DialogInterface.OnClickListener?,
    private val _cancelListener:DialogInterface.OnClickListener?){

    private var _dialog: AlertDialog? = null

    fun show(){
        _dialog = AlertDialog.Builder(_context)
            .setMessage(_message)
            .setPositiveButton("OK", _okListener)
            .setNegativeButton("Cancel", _cancelListener)
            .create()
        _dialog?.show()
    }

    fun dismiss(){
        _dialog?.dismiss()
    }

}