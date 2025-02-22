package com.christian.christian_picker_image

import android.app.Activity
import android.content.Context
import android.content.Intent
import java.util.ArrayList
import android.os.Environment
//import com.christian.christian_picker_image.camera.CameraActivity

import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.PluginRegistry
import io.flutter.plugin.common.PluginRegistry.Registrar
import io.flutter.view.FlutterView

import com.imagepicker.features.ImagePicker
import com.imagepicker.model.Image

import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding

/**
 * ChristianPickerImagePlugin
 */
class ChristianPickerImagePlugin : FlutterPlugin, ActivityAware, MethodCallHandler, PluginRegistry.ActivityResultListener, PluginRegistry.RequestPermissionsResultListener {

    private val view: FlutterView? = null
    private var pendingResult: Result? = null
    private val methodCall: MethodCall? = null

    private var context: Context? = null
    // private var activity: Activity? = null
    // private var channel: MethodChannel? = null
    private var messenger: BinaryMessenger? = null

    private val REQUEST_CODE_CHOOSE = 1001
    private val REQUEST_CODE_GRANT_PERMISSIONS = 2001

    private val NumberOfImagesToSelect = 5

    private lateinit var channel : MethodChannel
    private var activity: Activity? = null

    // private constructor(_activity: Activity, _context: Context, _channel: MethodChannel, _messenger: BinaryMessenger) {
    //     this.activity = _activity
    //     this.context = _context
    //     this.channel = _channel
    //     this.messenger = _messenger
    // }

    override fun onAttachedToEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        channel = MethodChannel(binding.binaryMessenger, "christian_picker_image")
        channel.setMethodCallHandler(this)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray): Boolean {
        return false
    }

    private fun presentPicker(maxImages: Int) {
        ImagePicker.create(this.activity)
                .limit(maxImages)
                .showCamera(true)// Activity or Fragment
                .start();
    }

    override fun onMethodCall(call: MethodCall, result: Result) {

        this.pendingResult = result;

        when (call.method) {
            PICK_IMAGES -> {
                val maxImages = call.argument<Int>(MAX_IMAGES)!!

                if (maxImages <= 0) {
                    return
                }

                presentPicker(maxImages)
            }
            "getPlatformVersion" -> result.success("Android " + android.os.Build.VERSION.RELEASE)
            else -> result.notImplemented()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent): Boolean {

        if (ImagePicker.shouldHandle(requestCode, resultCode, data)) {
            // Get a list of picked images
            val images: List<Image> = ImagePicker.getImages (data)
            val list: ArrayList<Map<String,String>> = ArrayList<Map<String,String>>(0)

            for (image in images) {
                val containerMap = HashMap<String, String>()
                containerMap.put("path", image.path)
                list.add(containerMap)
            }

            this.pendingResult?.success(list)
        }

        return false
    }

    companion object {

        private val PICK_IMAGES = "pickImages"
        private val REFRESH_IMAGE = "refreshImage"
        private val MAX_IMAGES = "maxImages"
        private val ANDROID_OPTIONS = "androidOptions"

        private val SELECTED_ASSETS = "selectedAssets"
        private val ENABLE_CAMERA = "enableCamera"
        private val REQUEST_CODE_CHOOSE = 1001
        private val REQUEST_CODE_GRANT_PERMISSIONS = 2001

        /**
         * Plugin registration.
         */
        @JvmStatic
        fun registerWith(registrar: Registrar) {
            if (registrar.activity() == null) {
                // If a background flutter view tries to register the plugin, there will be no activity from the registrar,
                // we stop the registering process immediately because the ImagePicker requires an activity.
                return
            }
            val channel = MethodChannel(registrar.messenger(), "christian_picker_image")
            val instance = ChristianPickerImagePlugin().also {
                it.activity = registrar.activity()
            }
            channel.setMethodCallHandler(instance)
            instance.context = registrar.context()
            registrar.addRequestPermissionsResultListener(instance)
        }
    }

    override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        channel?.setMethodCallHandler(null)
    }

    override fun onDetachedFromActivity() {
        activity = null
    }

    override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
        activity = binding.activity
    }

    override fun onAttachedToActivity(binding: ActivityPluginBinding) {
        activity = binding.activity
        binding.addRequestPermissionsResultListener(this)
    }

    override fun onDetachedFromActivityForConfigChanges() {
        activity = null
    }

}