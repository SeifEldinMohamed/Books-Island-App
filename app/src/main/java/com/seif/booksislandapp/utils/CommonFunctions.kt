package com.seif.booksislandapp.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.view.*
import android.widget.Toast
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import androidx.core.util.PatternsCompat
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.seif.booksislandapp.R
import com.seif.booksislandapp.domain.model.User
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

fun User.isValidUser(): Resource<String, String> {
    val user = this
    return when {
        user.username.length < 5 -> Resource.Error("username is too short min char = 5 !")
        email.isEmpty() -> Resource.Error("email can't be empty !")
        !isValidEmail(email) -> Resource.Error("please enter a valid email !")
        password.isEmpty() -> Resource.Error("password can't be empty !")
        !isValidPasswordFormat(password) -> Resource.Error("Not Valid Password Format !")
        user.gender.isEmpty() -> Resource.Error("please choose your gender !")
        user.governorate.isEmpty() -> Resource.Error("please choose your government !")
        user.district.isEmpty() -> Resource.Error("please choose your district !")
        else -> Resource.Success("valid User")
    }
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
    return PatternsCompat.EMAIL_ADDRESS.matcher(email).matches()
}

fun isValidPasswordFormat(password: String): Boolean {
    val passwordRegex = Pattern.compile(
        "^" +
            "(?=.*[0-9])" + // at least 1 digit
            "(?=.*[a-z])" + // at least 1 lower case letter
            "(?=.*[A-Z])" + // at least 1 upper case letter
            "(?=.*[@#$%^&+=])" + // at least 1 special character
            "(?=\\S+$)" + // no white spaces
            ".{6,}" + // at least 6 characters
            "$"
    )
    return passwordRegex.matcher(password).matches()
}

fun ConnectivityManager.checkInternetConnection(): Boolean {
    val activeNetwork: Network = this.activeNetwork ?: return false
    val capabilities: NetworkCapabilities =
        this.getNetworkCapabilities(activeNetwork) ?: return false
    return when { // return true if there is an internet connection from Wi-Fi, cellular and ethernet
        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
        else -> false
    }
}

@SuppressLint("InflateParams")
fun Context.createAlertDialog(activity: Activity): AlertDialog {
    val builder = AlertDialog.Builder(this)
    builder.setView(activity.layoutInflater.inflate(R.layout.custom_loading_dialog, null))
    builder.setCancelable(true)
    return builder.create()
}
