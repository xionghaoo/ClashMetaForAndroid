package com.github.kr328.clash

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.Environment
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.github.kr328.clash.common.constants.Intents
import com.github.kr328.clash.common.util.intent
import com.github.kr328.clash.common.util.setUUID
import com.github.kr328.clash.design.MainDesign
import com.github.kr328.clash.design.ui.ToastDuration
import com.github.kr328.clash.remote.Remote
import com.github.kr328.clash.remote.StatusClient
import com.github.kr328.clash.service.model.Profile
import com.github.kr328.clash.util.startClashService
import com.github.kr328.clash.util.stopClashService
import com.github.kr328.clash.util.withProfile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.util.*
import com.github.kr328.clash.design.R

class ExternalControlActivity : Activity(), CoroutineScope by MainScope() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("ExternalControlActivity", "action: ${intent.action}")
        when(intent.action) {
            Intent.ACTION_VIEW -> {
                val uri = intent.data ?: return finish()
                val url = uri.getQueryParameter("url") ?: return finish()
                launch {
                    val uuid = withProfile {
                        val type = when (uri.getQueryParameter("type")?.lowercase(Locale.getDefault())) {
                            "url" -> Profile.Type.Url
                            "file" -> Profile.Type.File
                            else -> Profile.Type.Url
                        }
                        val name = uri.getQueryParameter("name") ?: getString(R.string.new_profile)

                        create(type, name).also {
                            patch(it, name, url, 0)
                        }
                    }
                    startActivity(PropertiesActivity::class.intent.setUUID(uuid))
                    finish()
                }
            }

            Intents.ACTION_TOGGLE_CLASH -> if(Remote.broadcasts.clashRunning) {
                stopClash()
            }
            else {
                startClash()
            }

            Intents.ACTION_START_CLASH -> if(!Remote.broadcasts.clashRunning) {
                startClash()
            }
            else {
                Toast.makeText(this, R.string.external_control_started, Toast.LENGTH_LONG).show()
            }

            Intents.ACTION_STOP_CLASH -> if(Remote.broadcasts.clashRunning) {
                stopClash()
            }
            else {
                Toast.makeText(this, R.string.external_control_stopped, Toast.LENGTH_LONG).show()
            }

            Intents.ACTION_STOP_CLASH_BACKGROUND -> {
                val isRunning = StatusClient(this@ExternalControlActivity).isRunning()
                if (isRunning) {
                    stopClash()
                } else {
                    Toast.makeText(this, R.string.external_control_stopped, Toast.LENGTH_LONG).show()
                }
            }
            Intents.ACTION_STATUS -> {
                val isRunning = StatusClient(this@ExternalControlActivity).isRunning()
                setResult(1, Intent().apply {
                    putExtra("clashRunning", isRunning)
                })
                Log.d("ExternalControlActivity", "status query: $isRunning")
            }
        }
        Log.d("ExternalControlActivity", "activity finish")
        return finish()
    }

    override fun onDestroy() {
        Log.d("ExternalControlActivity", "activity onDestroy")
        super.onDestroy()
    }

    private fun startClash() {
//        if (currentProfile == null) {
//            Toast.makeText(this, R.string.no_profile_selected, Toast.LENGTH_LONG).show()
//            return
//        }
        val vpnRequest = startClashService()
        if (vpnRequest != null) {
            Toast.makeText(this, R.string.unable_to_start_vpn, Toast.LENGTH_LONG).show()
            return
        }
        Toast.makeText(this, R.string.external_control_started, Toast.LENGTH_LONG).show()
    }

    private fun stopClash() {
        stopClashService()
        Toast.makeText(this, R.string.external_control_stopped, Toast.LENGTH_LONG).show()
    }
}