package com.seif.booksislandapp.utils

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.Patterns
import android.view.*
import android.widget.Toast
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern

/**
 * start the Activity [T], in a more concise way,
 * while still allowing to configure the  [Intent] in
 * the optional [block] lambda
 * **/
inline fun <reified T : Activity> Context.start(block: Intent.() -> Unit = {}) {
    val intent = Intent(this, T::class.java)
    block(intent)
    startActivity(intent)
}

fun Context.loadColor(@ColorRes colorRes: Int): Int {
    return ContextCompat.getColor(this, colorRes)
}

fun Context.showToast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_LONG).show()
}

fun View.showSnackBar(message: String) {
    Snackbar.make(this, message, Snackbar.LENGTH_SHORT).show()
}

fun Fragment.toast(msg: String?) {
    Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show()
}

fun View.show() {
    this.visibility = View.VISIBLE
}

fun View.hide() {
    this.visibility = View.GONE
}

fun View.invisible() {
    this.visibility = View.INVISIBLE
}

fun View.disable() {
    isEnabled = false
}

fun View.enabled() {
    isEnabled = true
}

fun Date.formatDate(): String {
    val formatter = SimpleDateFormat("dd/MM/yyyy - hh:mm a", Locale.getDefault())
    return formatter.format(this)
}

fun Context.createDialog(layout: Int, cancelable: Boolean): Dialog {
    val dialog = Dialog(this, android.R.style.Theme_Dialog)
    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
    dialog.setContentView(layout)
    dialog.window?.setGravity(Gravity.CENTER)
    dialog.window?.setLayout(
        WindowManager.LayoutParams.MATCH_PARENT,
        WindowManager.LayoutParams.WRAP_CONTENT
    )
    dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    dialog.setCancelable(cancelable)
    return dialog
}

fun isValidEmailAndPassword(email: String, password: String): Resource<String, String> {
    return if (email.isEmpty()) {
        Resource.Error("email can't be empty !")
    } else if (!isValidEmail(email)) {
        Resource.Error("please enter a valid email !")
    } else if (password.isEmpty()) {
        Resource.Error("password can't be empty !")
    } else if (!isValidPasswordFormat(password)) {
        Resource.Error("please enter a valid password !")
    } else {
        Resource.Success("valid User")
    }
}

fun isValidEmailInput(email: String): Resource<String, String> {
    return if (email.isEmpty()) {
        Resource.Error("email can't be empty !")
    } else if (!isValidEmail(email)) {
        Resource.Error("please enter a valid email !")
    } else {
        Resource.Success("valid Email")
    }
}

fun isValidEmail(email: String): Boolean {
    return Patterns.EMAIL_ADDRESS.matcher(email).matches()
}

fun isValidPasswordFormat(password: String): Boolean {
    val passwordRegex = Pattern.compile(
        "^" +
            "(?=.*[0-9])" + // at least 1 digit
            // "(?=.*[a-z])" + // at least 1 lower case letter
            //  "(?=.*[A-Z])" + // at least 1 upper case letter
            //  "(?=.*[a-zA-Z])" +      // any letter
            //  "(?=.*[@#$%^&+=])" +    // at least 1 special character
            "(?=\\S+$)" + // no white spaces
            ".{6,}" + // at least 8 characters
            "$"
    )
    return passwordRegex.matcher(password).matches()
}
