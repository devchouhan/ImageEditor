package com.example.imageeditor

import android.Manifest
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Matrix
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.MediaStore.Images
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.exifinterface.media.ExifInterface
import com.lyrebirdstudio.croppylib.Croppy
import com.lyrebirdstudio.croppylib.main.CropRequest
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


class MainActivity : AppCompatActivity() {
    private val CAMERA_REQUEST = 100
    private val STORAGE_REQUEST = 200
    var cameraPermission: Array<String>?=null
    var storagePermission: Array<String>?=null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // allowing permissions of gallery and camera
        // allowing permissions of gallery and camera
        cameraPermission = arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        storagePermission = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)

        selectImageTV.setOnClickListener {
            showImagePicDialog()
        }

        cropImageTV.setOnClickListener {
            photoUri?.let {
                val cropRequest = CropRequest.Auto(sourceUri = it, requestCode = 101)
                Croppy.start(this, cropRequest)
            }
        }

        rotateImageTV.setOnClickListener {
            rotateImage()
        }

        imageView.setOnClickListener {

            val pathToImage = photoUri!!.path
            val exif = ExifInterface(pathToImage!!)
            val tagsToCheck = arrayOf(
                ExifInterface.TAG_DATETIME,
                ExifInterface.TAG_GPS_LATITUDE,
                ExifInterface.TAG_GPS_LONGITUDE,
                ExifInterface.TAG_EXPOSURE_TIME
            )
            val hashMap = HashMap<String, String>()
            for (tag in tagsToCheck)
                exif.getAttribute(tag)?.let { hashMap[tag] = it }

           /* runBlocking {
                launch {
                    imageView.invalidate()
                    val drawable = imageView.drawable
                    val bitmap = drawable.toBitmap()
                    saveBitmap(bitmap, System.currentTimeMillis().toString())

                }
            }*/

        }
    }


    private fun showImagePicDialog() {
        val options = arrayOf("Camera", "Gallery")
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setTitle("Pick Image From")
        builder.setItems(options, DialogInterface.OnClickListener {dialog, which ->
            if (which == 0) {
                if (!checkCameraPermission()!!) {
                    requestCameraPermission()
                } else {
                    takePhoto()
                }
            } else if (which == 1) {
                if (!checkStoragePermission()!!) {
                    requestStoragePermission()
                } else {
                    pickFromGallery()
                }
            }
        })
        builder.create().show()
    }

    // checking storage permissions
    private fun checkStoragePermission(): Boolean? {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
    }

    // Requesting  gallery permission
    private fun requestStoragePermission() {
        requestPermissions(storagePermission!!, STORAGE_REQUEST)
    }

    // checking camera permissions
    private fun checkCameraPermission(): Boolean? {
        val result = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        val result1 = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        return result && result1
    }

    // Requesting camera permission
    private fun requestCameraPermission() {
        requestPermissions(cameraPermission!!, CAMERA_REQUEST)
    }


    var photoUri :Uri ? =null
    private fun pickFromGallery() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), STORAGE_REQUEST)
    }

    private fun takePhoto(){
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(Intent.createChooser(cameraIntent, "Select Picture"), CAMERA_REQUEST)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d("CHECKCODELOG", "onActivityResult: $resultCode")

        data?.let {
            if (requestCode == STORAGE_REQUEST) {
                val selectedImage: Uri = it.getData()!!
                photoUri = selectedImage
                imageView.setImageURI(it.data)
            }else if (requestCode == CAMERA_REQUEST) {
                val photo: Bitmap = it.extras?.get("data") as Bitmap
                imageView.setImageBitmap(photo)

                val tempUri = getImageUri(applicationContext, photo)
                photoUri = tempUri
                Log.d("CHECKCODELOG", "onActivityResult: $tempUri")
            }else if(requestCode == 101){
                imageView.setImageURI(data.data)
            }else{

            }

        }

    }

    fun rotateImage(){
         runBlocking {
             launch {
                 imageView.invalidate()
                 val drawable = imageView.drawable
                 val bitmap = drawable.toBitmap()

                 val matrix = Matrix()
                 matrix.postRotate(imageView.rotation + 90f);
                 var btmp = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);

                 imageView.setImageBitmap(btmp)
             }
         }




    }

    fun getImageUri(inContext: Context, inImage: Bitmap): Uri? {
        val bytes = ByteArrayOutputStream()
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path = Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null)
        return Uri.parse(path)
    }

    private fun saveBitmap(bitmap: Bitmap, fileName: String) {
        val file = File(Environment.getExternalStorageDirectory(), fileName)
        var fileOS: FileOutputStream? = null
        try {
            fileOS = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOS)
            fileOS.flush()
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            if (fileOS != null) {
                try {
                    fileOS.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }

}