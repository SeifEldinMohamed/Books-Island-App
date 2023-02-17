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
import android.widget.ScrollView
import android.widget.Toast
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import androidx.core.util.PatternsCompat
import androidx.fragment.app.Fragment
import com.musfickjamil.snackify.Snackify
import com.seif.booksislandapp.R
import com.seif.booksislandapp.domain.model.book.Book
import com.seif.booksislandapp.domain.model.User
import com.seif.booksislandapp.domain.model.adv.auction.AuctionAdvertisement
import com.seif.booksislandapp.domain.model.adv.donation.DonateAdvertisement
import com.seif.booksislandapp.domain.model.adv.sell.SellAdvertisement
import org.imaginativeworld.oopsnointernet.callbacks.ConnectionCallback
import org.imaginativeworld.oopsnointernet.dialogs.pendulum.NoInternetDialogPendulum
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

fun View.showSuccessSnackBar(message: String) {
    Snackify.success(this, message, Snackify.LENGTH_SHORT).show()
}

fun View.showErrorSnackBar(message: String) {
    Snackify.error(this, message, Snackify.LENGTH_SHORT).show()
}

fun View.showInfoSnackBar(message: String) {
    Snackify.info(this, message, Snackify.LENGTH_SHORT).show()
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
    val formatter = SimpleDateFormat("dd/MM", Locale.getDefault())
    return formatMonth(formatter.format(this))
}

fun Date.formatDateInDetails(): String {
    val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    return formatter.format(this)
}

fun formatMonth(date: String): String {
    val day = date.substring(0, 2)
    var month = date.substring(3)
    month = when (month) {
        "01" -> " JAN"
        "02" -> " FEB"
        "03" -> " MAR"
        "04" -> " APR"
        "05" -> " MAY"
        "06" -> " JUN"
        "07" -> " JUL"
        "08" -> " AUG"
        "09" -> " SEP"
        "10" -> " OCT"
        "11" -> " NOV"
        "12" -> " DEC"
        else -> month
    }

    return (day + month)
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
fun Context.createLoadingAlertDialog(activity: Activity): AlertDialog {
    val builder = AlertDialog.Builder(this)
    builder.setView(activity.layoutInflater.inflate(R.layout.custom_loading_dialog, null))
    builder.setCancelable(false)
    return builder.create()
}

fun ScrollView.scrollToBottom() {
    val lastChild = getChildAt(childCount - 1)
    val bottom = lastChild.bottom + paddingBottom
    val delta = bottom - (scrollY + height)
    smoothScrollBy(0, delta)
}

fun SellAdvertisement.checkSellAdvertisementUpload(): Resource<String, String> {
    return if (this.ownerId.isEmpty()) {
        Resource.Error("User is not LoggedIn !")
    } else if (this.publishTime.toString().isEmpty()) {
        Resource.Error("problem in Phone Time !")
    } else if (this.location.isEmpty()) {
        Resource.Error("Location Can't be Empty")
    } else if (this.status.toString().isEmpty()) {
        Resource.Error("Status Can't be Empty")
    } else if (this.price.isEmpty()) {
        Resource.Error("Price Can't be Empty")
    } else {
        return when (val result = this.book.validateBookData()) {
            is Resource.Error -> Resource.Error(result.message)
            is Resource.Success -> Resource.Success(result.data)
        }
    }
}

fun DonateAdvertisement.checkDonateAdvertisementUpload(): Resource<String, String> {
    return if (this.ownerId.isEmpty()) {
        Resource.Error("User is not LoggedIn !")
    } else if (this.publishTime.toString().isEmpty()) {
        Resource.Error("problem in Phone Time !")
    } else if (this.location.isEmpty()) {
        Resource.Error("Location Can't be Empty")
    } else if (this.status.toString().isEmpty()) {
        Resource.Error("Status Can't be Empty")
    } else {
        return when (val result = this.book.validateBookData()) {
            is Resource.Error -> Resource.Error(result.message)
            is Resource.Success -> Resource.Success(result.data)
        }
    }
}

fun AuctionAdvertisement.checkAuctionAdvertisementUpload(): Resource<String, String> {
    return if (this.ownerId.isEmpty()) {
        Resource.Error("User is not LoggedIn !")
    } else if (this.publishTime.toString().isEmpty()) {
        Resource.Error("problem in Phone Time !")
    } else if (this.location.isEmpty()) {
        Resource.Error("Location Can't be Empty!")
    } else if (this.status.toString().isEmpty()) {
        Resource.Error("Status Can't be Empty!")
    } else if (this.startPrice == null) {
        Resource.Error("Start Price Can't be Empty!")
    } else if (this.postDuration.isEmpty()) {
        Resource.Error("Post Duration Can't be Empty!")
    } else {
        return when (val result = this.book.validateBookData()) {
            is Resource.Error -> Resource.Error(result.message)
            is Resource.Success -> Resource.Success(result.data)
        }
    }
}

private fun Book.validateBookData(): Resource<String, String> {
    return if (this.author.isEmpty()) {
        Resource.Error("Please add the author of the book!")
    } else if (this.title.isEmpty()) {
        Resource.Error("Please add the title of the book!")
    } else if (this.category.isEmpty()) {
        Resource.Error("Please choose the category of the book!")
    } else if (this.isUsed == null) {
        Resource.Error("Please choose the condition of the book!")
    } else if (this.description.isEmpty()) {
        Resource.Error("Please add the description of the book!")
    } else if (this.images.isEmpty()) {
        Resource.Error("Please add at least one image for the book!")
    } else if (this.edition.isEmpty()) {
        Resource.Error("Please choose the edition of the book!")
    } else {
        Resource.Success("All Book Data is Valid")
    }
}

fun Fragment.handleNoInternetConnectionState(view: View) {
    NoInternetDialogPendulum.Builder(
        requireActivity(),
        lifecycle
    ).apply {
        dialogProperties.apply {
            connectionCallback = object : ConnectionCallback { // Optional
                override fun hasActiveConnection(hasActiveConnection: Boolean) {
                    when (hasActiveConnection) {
                        true -> view.showInfoSnackBar("Internet connection is back")
                        false -> Unit
                    }
                }
            }

            cancelable = true // Optional
            noInternetConnectionTitle = "No Internet" // Optional
            noInternetConnectionMessage =
                "Check your Internet connection and try again." // Optional
            showInternetOnButtons = true // Optional
            pleaseTurnOnText = "Please turn on" // Optional
            wifiOnButtonText = "Wifi" // Optional
            mobileDataOnButtonText = "Mobile data" // Optional
            onAirplaneModeTitle = "No Internet" // Optional
            onAirplaneModeMessage = "You have turned on the airplane mode." // Optional
            pleaseTurnOffText = "Please turn off" // Optional
            airplaneModeOffButtonText = "Airplane mode" // Optional
            showAirplaneModeOffButtons = true // Optional
        }
    }.build()
}
