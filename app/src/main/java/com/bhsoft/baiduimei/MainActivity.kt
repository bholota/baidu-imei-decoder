package com.bhsoft.baiduimei

import android.Manifest
import android.os.Bundle
import android.os.Environment
import android.util.Base64
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions
import java.io.File
import java.io.FileNotFoundException
import java.nio.charset.Charset
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec


@ExperimentalStdlibApi
class MainActivity(val scope: CoroutineScope = MainScope()) : AppCompatActivity(),
    CoroutineScope by scope, EasyPermissions.PermissionCallbacks {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        imeiText.text = "IMEI Loading..."

        checkBaiduFileContent()
    }

    @AfterPermissionGranted(reqStoragePermission)
    fun checkBaiduFileContent() {

        val perms = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        if (EasyPermissions.hasPermissions(this, *perms)) {
            launch {
                try {
                    val result = getBaiduFileContent()
                    imeiText.text = "Found baidu data:\n\n$result"
                } catch (e: FileNotFoundException) {
                    imeiText.text = "Rejoice, no baidu hidden file was found"
                } catch (e: IllegalArgumentException) {
                    imeiText.text = "Baidu file exists but it's empty"
                }
            }
        } else {
            EasyPermissions.requestPermissions(
                this,
                "Storage read permission is required to access baidu generated file",
                reqStoragePermission,
                *perms
            )
        }
    }

    suspend fun getBaiduFileContent(): String = withContext(Dispatchers.IO) {
        val file = File(Environment.getExternalStorageDirectory().absolutePath + filePath)
        if (!file.exists()) {
            throw FileNotFoundException()
        }
        val fileContents = file.readText()
        if (fileContents.isEmpty()) {
            throw IllegalArgumentException("Expected file was empty")
        }

        val decodedBytes = decodeBase64(fileContents)
        val result = String(decipher(decodedBytes), Charset.defaultCharset())

        result
    }

    fun decodeBase64(input: String): ByteArray {
        return Base64.decode(input, Base64.DEFAULT)
    }

    fun decipher(data: ByteArray): ByteArray {
        val skeySpec = SecretKeySpec(key, "AES")
        val cipher = Cipher.getInstance("AES/CBC/PKCS7Padding")
        cipher.init(Cipher.DECRYPT_MODE, skeySpec, IvParameterSpec(iv))
        return cipher.doFinal(data)
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            AppSettingsDialog.Builder(this).build().show()
        }
        imeiText.text = "Storage read permission required"
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
        // not used
    }

    @ExperimentalStdlibApi
    companion object {
        const val reqStoragePermission = 5986
        const val filePath = "/backups/.SystemConfig/.cuid2"
        var iv = "30212102dicudiab".encodeToByteArray() //.toHexString()
        var key = "30212102dicudiab".encodeToByteArray() //.toHexString()
    }
}
