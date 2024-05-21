package com.sohnyi.imagecompress

import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import id.zelory.compressor.Compressor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "MainActivity"
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        val imageView = findViewById<ImageView>(R.id.iv)
        val ivCompress = findViewById<ImageView>(R.id.iv_compress)
        val btnPick = findViewById<Button>(R.id.btn_pick)
        val tvSize = findViewById<TextView>(R.id.tv_size)
        val tvSizeCompressed = findViewById<TextView>(R.id.tv_size_compressed)
        val tvPercent = findViewById<TextView>(R.id.tv_percent)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri == null) {
                return@registerForActivityResult
            }
            imageView.setImageURI(uri)
            Log.d(TAG, "onCreate: url path: ${uri.path}")
             val file = uriToFile(uri)
            if (file != null) {
                Log.d(TAG, "onCreate: file path: ${file.absolutePath}")
                val length = file.length()
                tvSize.text = "${length / 1024}KB"

                lifecycleScope.launch(Dispatchers.IO) {
                    val compressedImageFile = Compressor.compress(this@MainActivity, file)
                    withContext(Dispatchers.Main) {
                        if (compressedImageFile != null) {
                            val compressedLength = compressedImageFile.length()
                            tvSizeCompressed.text = "${compressedLength / 1024}KB"

                            tvPercent.text = "${compressedLength.toFloat() / length.toFloat() * 100}%"
                        }
                        val myBitmap = BitmapFactory.decodeFile(compressedImageFile.absolutePath)
                        ivCompress.setImageBitmap(myBitmap)
                    }
                }

            }
        }

        btnPick.setOnClickListener {
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
    }

    private fun uriToFile(uri: Uri): File? {
        var file: File? = null
        try {
            // Get the content resolver instance
            val contentResolver = contentResolver

            // Open the input stream from the URI
            val inputStream = contentResolver.openInputStream(uri)

            // Create a temporary file in the cache directory
            file = File(cacheDir, System.currentTimeMillis().toString() + ".jpg")

            // Write the input stream to the file
            val outputStream = FileOutputStream(file)
            val buffer = ByteArray(1024)
            var bytesRead: Int
            while (inputStream!!.read(buffer).also { bytesRead = it } != -1) {
                outputStream.write(buffer, 0, bytesRead)
            }
            outputStream.close()
            inputStream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return file
    }


}