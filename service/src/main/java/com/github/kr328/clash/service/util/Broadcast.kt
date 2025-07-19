package com.github.kr328.clash.service.util

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Environment
import android.util.Log
import com.github.kr328.clash.common.constants.Intents
import com.github.kr328.clash.common.constants.Permissions
import java.io.File
import java.util.*


fun Context.sendBroadcastSelf(intent: Intent) {
    sendBroadcast(
        intent.setPackage(this.packageName),
        Permissions.RECEIVE_SELF_BROADCASTS
    )
}

fun Context.sendProfileChanged(uuid: UUID) {
    val intent = Intent(Intents.ACTION_PROFILE_CHANGED)
        .putExtra(Intents.EXTRA_UUID, uuid.toString())

    sendBroadcastSelf(intent)
}

fun Context.sendProfileLoaded(uuid: UUID) {
    val intent = Intent(Intents.ACTION_PROFILE_LOADED)
        .putExtra(Intents.EXTRA_UUID, uuid.toString())

    sendBroadcastSelf(intent)
}

fun Context.sendProfileUpdateCompleted(uuid: UUID) {
    val intent = Intent(Intents.ACTION_PROFILE_UPDATE_COMPLETED)
        .putExtra(Intents.EXTRA_UUID, uuid.toString())

    sendBroadcastSelf(intent)
}

fun Context.sendProfileUpdateFailed(uuid: UUID, reason: String) {
    val intent = Intent(Intents.ACTION_PROFILE_UPDATE_FAILED)
        .putExtra(Intents.EXTRA_UUID, uuid.toString())
        .putExtra(Intents.EXTRA_FAIL_REASON, reason)

    sendBroadcastSelf(intent)
}

fun Context.sendOverrideChanged() {
    val intent = Intent(Intents.ACTION_OVERRIDE_CHANGED)

    sendBroadcastSelf(intent)
}

fun Context.sendServiceRecreated() {
    notifyLauncher(false)
    sendBroadcastSelf(Intent(Intents.ACTION_SERVICE_RECREATED))
}

fun Context.sendClashStarted() {
    notifyLauncher(true)
    sendBroadcastSelf(Intent(Intents.ACTION_CLASH_STARTED))
}

fun Context.sendClashStopped(reason: String?) {
    notifyLauncher(false)
    sendBroadcastSelf(
        Intent(Intents.ACTION_CLASH_STOPPED).putExtra(
            Intents.EXTRA_STOP_REASON,
            reason
        )
    )
}

//private val sync = Any()
fun Context.notifyLauncher(isOpen: Boolean) {
    Log.d("LAUNCHER", "notifyLauncher: $isOpen")
    val intent = Intent()
    intent.setAction("com.udi.daisy.VPN_STATUS")
    intent.putExtra("is_open", isOpen)
    intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES)
    intent.setComponent(
        ComponentName("com.udi.daisy", "com.udi.module_statusbar.VpnStatusReceiver")
    )
    sendBroadcast(intent)

//    synchronized(sync) {
//        try {
//            val statusFile = File(Environment.getExternalStoragePublicDirectory(""), "clash_status")
//            if (!statusFile.exists()) {
//                statusFile.createNewFile()
//            }
//            statusFile.writeText(if (isOpen) "1" else "0")
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
//
//    }

}
