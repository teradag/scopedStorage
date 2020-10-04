package com.qohuck.scopedstorage

import android.Manifest
import android.annotation.TargetApi
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker.PERMISSION_GRANTED
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream


class MainActivity : AppCompatActivity() {
    companion object {
        private val TAG = MainActivity::class.qualifiedName

        private const val REQUEST_CODE = 1
        private const val TEST_FILE_DIRECTORY = "sdcard/test/"
        private const val READ_TEST_FILE_PATH = TEST_FILE_DIRECTORY + "icon2.png"
        private const val WRITE_TEST_FILE_PATH = TEST_FILE_DIRECTORY + "icon3.png"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (haveStoragePermission().not()) {
            // 正常動作を確認するサンプルアプリなので，許可されなかったときを考慮しない。
            requestStoragePermission()
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (haveAllFilesAccessPermission().not()) {
                // 正常動作を確認するサンプルアプリなので，許可されなかったときを考慮しない。
                requestAllFilesAccessPermission()
            }
        }

        imageView.setOnClickListener {
            imageView.setImageResource(R.drawable.icon1)
            showToast("画像を初期状態に戻しました。")
        }

        read_button.setOnClickListener {
            imageView.setImageBitmap(BitmapFactory.decodeFile(READ_TEST_FILE_PATH))
            showToast("SDカードに保存されている画像を表示しました。")
        }

        write_button.setOnClickListener {
            saveImageToSdcard()
        }
    }

    /**
     * ストレージ読み書きのパーミッションが付与されているかを確認する。
     *
     * @return ストレージ読み書きのパーミッションが付与済みか
     */
    private fun haveStoragePermission(): Boolean {
        // WRITE権限だけ確認しておけば両方権限が振られているはずだが，
        // READ権限だけ振られている可能性があるため両方確認する。
        // 一般的には，両権限振られているので，WRITE権限を先に確認することで確認の回数が減る。
        return checkPermissionGranted(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                && checkPermissionGranted(Manifest.permission.READ_EXTERNAL_STORAGE)
    }

    /**
     * パーミッションが付与されているかを確認する。
     * https://developer.android.com/training/permissions/requesting?hl=ja#already-granted
     *
     * @param permission 確認したいパーミッション
     */
    private fun checkPermissionGranted(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(this, permission) == PERMISSION_GRANTED
    }

    /**
     * ストレージ読み書きのパーミッション付与を要求する。
     * https://developer.android.com/training/permissions/requesting?hl=ja#already-granted
     */
    private fun requestStoragePermission() {
        requestPermissions(
            arrayOf(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ),
            REQUEST_CODE
        )
    }

    /**
     * 「すべてのファイルへのアクセス」が許可されているか確認する。
     * https://developer.android.com/preview/privacy/storage?hl=ja#all-files-access
     *
     * @return 「すべてのファイルへのアクセス」が許可されているか
     */
    @TargetApi(30)
    private fun haveAllFilesAccessPermission(): Boolean {
        return Environment.isExternalStorageManager()
    }

    /**
     * 「すべてのファイルへのアクセス」を要求する。
     * https://developer.android.com/preview/privacy/storage?hl=ja#all-files-access
     */
    @TargetApi(30)
    private fun requestAllFilesAccessPermission() {
        val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
        intent.data = Uri.parse("package:$packageName")
        try {
            intent.resolveActivity(packageManager).className
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Log.w(TAG, "Request all files access not supported.", e)
        }
    }

    /**
     * SDCARD の指定場所に drawable から読み込んだ画像を保存する。
     */
    private fun saveImageToSdcard() {
        if (haveAllFilesAccessPermission().not()) {
            showToast("「すべてのファイルへのアクセス」が許可されていません。")
            return
        }

        val bitmap = BitmapFactory.decodeResource(resources, R.drawable.icon3)
        val file = File(WRITE_TEST_FILE_PATH)
        try {
            FileOutputStream(file).use {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
            }
            showToast("SDカードへのファイル書き込みに成功しました。")
        } catch (e: FileNotFoundException) {
            Log.w(TAG, "Failed save image to sdcard.", e)
            showToast("SDカードへのファイル書き込みに失敗しました。")
        }
    }

    /**
     * Toast を表示する。
     */
    private fun showToast(text: String) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
    }
}