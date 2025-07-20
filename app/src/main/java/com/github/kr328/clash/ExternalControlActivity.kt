package com.github.kr328.clash

import android.app.Activity
import android.app.ComponentCaller
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.github.kr328.clash.common.constants.Intents
import com.github.kr328.clash.common.util.intent
import com.github.kr328.clash.common.util.setUUID
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
import com.github.kr328.clash.design.store.UiStore
import com.github.kr328.clash.service.util.notifyLauncher

class ExternalControlActivity : Activity(), CoroutineScope by MainScope() {

//    private var isShowVpnAuthDialog = 0
    private var startSuccess = false

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
                val result = if (isRunning) {
                    stopClash()
                    true
                } else {
                    Toast.makeText(this, R.string.external_control_stopped, Toast.LENGTH_LONG).show()
                    false
                }
                setResult(3, Intent().apply {
                    putExtra("success", result)
                })
            }
            Intents.ACTION_STATUS -> {
                val isRunning = StatusClient(this@ExternalControlActivity).isRunning()
                notifyLauncher(isRunning)
                setResult(1, Intent().apply {
                    putExtra("clashRunning", isRunning)
                })
                Log.d("ExternalControlActivity", "status query: $isRunning")
            }
        }
        Log.d("ExternalControlActivity", "activity finish")
        if (intent.action == Intents.ACTION_START_CLASH_BACKGROUND) {
            val isRunning = StatusClient(this@ExternalControlActivity).isRunning()
            if (!isRunning) {
//                isShowVpnAuthDialog = 1
                startClashBackground()
            } else {
                Toast.makeText(this, R.string.external_control_started, Toast.LENGTH_LONG).show()
                setStartResult(false)
            }
        } else {
            return finish()
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d("ExternalControlActivity", "onResume: ")
//        if (isShowVpnAuthDialog == 2 && !startSuccess) {
//            finish()
//        }
    }

    override fun onPause() {
        super.onPause()
        Log.d("ExternalControlActivity", "onPause: ")
//        if (isShowVpnAuthDialog == 1) {
//            isShowVpnAuthDialog = 2
//            // 显示授权弹窗
//        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d("ExternalControlActivity", "onActivityResult: $requestCode, ${resultCode}")
        if (requestCode == 90011) {
            val success = resultCode == RESULT_OK
            if (success) {
                startSuccess = true
                UiStore(this).enableVpn = true
                startClashService()
            }
            setStartResult(success)
        } else {
            setStartResult(false)
        }
    }

    private fun startClashBackground() {
        val vpnRequest = startClashService()

        try {
            if (vpnRequest != null) {
                startSuccess = false
                startActivityForResult(vpnRequest, 90011)
            } else {
                setStartResult(true)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            setStartResult(false)
        }
    }

    private fun setStartResult(success: Boolean) {
        setResult(2, Intent().apply {
            putExtra("success", success)
        })
        finish()
    }

    private fun startClash(): Boolean {
//        if (currentProfile == null) {
//            Toast.makeText(this, R.string.no_profile_selected, Toast.LENGTH_LONG).show()
//            return
//        }
        val vpnRequest = startClashService()
        if (vpnRequest != null) {
            Toast.makeText(this, R.string.unable_to_start_vpn, Toast.LENGTH_LONG).show()
            return false
        }
        Toast.makeText(this, R.string.external_control_started, Toast.LENGTH_LONG).show()
        return true
    }

    private fun stopClash() {
        stopClashService()
        Toast.makeText(this, R.string.external_control_stopped, Toast.LENGTH_LONG).show()
    }
}