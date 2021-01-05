/*
 * Created by Ascendik on 10/8/19 12:25 PM
 * Copyright (c) 2019. All rights reserved.
 */

package com.ascendik.diary.util

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.*
import android.graphics.*
import android.media.ExifInterface
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.util.Base64
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.content.PermissionChecker
import com.example.demoappforfirebase.MainActivity
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

object ImageUtil {
    const val REQUEST_TAKE_PHOTO = 1
    const val REQUEST_GALLERY_PHOTO = 2
    private lateinit var currentPhotoPath: String
    var pathForImage = ""

    fun onLaunchCamera(activity: MainActivity) {
        val options = arrayOf("Take Photo", "Choose from Gallery", "Cancel")

        val builder = AlertDialog.Builder(activity)
        builder.setTitle("Choose picture")

        builder.setItems(options) { dialog, which ->
            when {
                options[which] == "Take Photo" -> {
                    dispatchTakePictureIntent(activity)
                }
                options[which] == "Choose from Gallery" -> {
                    val getIntent = Intent(Intent.ACTION_GET_CONTENT)
                    getIntent.type = "image/*"
                    val pickIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    pickIntent.type = "image/*"
                    val chooserIntent = Intent.createChooser(getIntent, "Select Image")
                    chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf(pickIntent))
                    activity.startActivityForResult(chooserIntent, REQUEST_GALLERY_PHOTO)
                }
                options[which] == "Cancel" -> {
                    dialog!!.dismiss()
                }
            }
        }
        builder.show()
    }

    @Throws(IOException::class)
    fun decodeFromFirebaseBase64(image: String?): Bitmap? {
        val decodedByteArray = Base64.decode(image, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(decodedByteArray, 0, decodedByteArray.size)
    }

    @SuppressLint("QueryPermissionsNeeded")
    fun dispatchTakePictureIntent(activity: MainActivity) {
        val hasCameraPermission =
            ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA)
        if (hasCameraPermission == PermissionChecker.PERMISSION_GRANTED) {
            Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
                // Ensure that there's a camera activity to handle the intent
                takePictureIntent.resolveActivity(activity.packageManager)?.also {
                    // Create the File where the photo should go
                    val photoFile: File? = try {
                        createImageFile(activity)
                    } catch (ex: IOException) {
                        // Error occurred while creating the File
                        null
                    }
                    // Continue only if the File was successfully created
                    photoFile?.also {
                        val photoURI: Uri = FileProvider.getUriForFile(
                            activity,
                            "com.example.demoappforfirebase.fileprovider",
                            it
                        )
                        pathForImage = it.absolutePath
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                        activity.startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO)
                    }
                }
            }
        } else {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(Manifest.permission.CAMERA),
                REQUEST_TAKE_PHOTO
            )
        }
    }

    fun addImageFromCamera(context: Context): String {
        if (::currentPhotoPath.isInitialized) {
            val file = File(currentPhotoPath)
            try {
                val bitmap: Bitmap =
                    decodeSampledBitmapFromFile(context, file.absolutePath, 1920f, 1080f)!!
                storeImage(file, bitmap)
                return file.absolutePath.substring(file.absolutePath.lastIndexOf("/") + 1)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return ""
    }

    @Throws(IOException::class)
    private fun createImageFile(context: Context): File {
        // Create an image file name
        val timeStamp: String =
            SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File? = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            currentPhotoPath = absolutePath
        }
    }

    private fun storeImage(file: File, bmp: Bitmap) {
        try {
            val out = FileOutputStream(file)
            bmp.compress(Bitmap.CompressFormat.JPEG, 80, out)
            out.flush()
            out.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun decodeSampledBitmapFromFile(
        context: Context,
        imageUri: String,
        height: Float,
        width: Float
    ): Bitmap? {
        val filePath = getRealPathFromURI(context, imageUri)
        var scaledBitmap: Bitmap? = null

        val options = BitmapFactory.Options()

        // by setting this field as true, the actual bitmap pixels are not loaded in the memory. Just the bounds are loaded. If
        // you try the use the bitmap here, you will get null.
        options.inJustDecodeBounds = true
        var bmp = BitmapFactory.decodeFile(filePath, options)

        var actualHeight = options.outHeight
        var actualWidth = options.outWidth

        // max Height and width values of the compressed image is taken as 816x612
        var imgRatio = (actualWidth / actualHeight).toFloat()
        val maxRatio = width / height

        // width and height values are set maintaining the aspect ratio of the image
        if (actualHeight > height || actualWidth > width) {
            when {
                imgRatio < maxRatio -> {
                    imgRatio = height / actualHeight
                    actualWidth = (imgRatio * actualWidth).toInt()
                    actualHeight = height.toInt()
                }
                imgRatio > maxRatio -> {
                    imgRatio = width / actualWidth
                    actualHeight = (imgRatio * actualHeight).toInt()
                    actualWidth = width.toInt()
                }
                else -> {
                    actualHeight = height.toInt()
                    actualWidth = width.toInt()

                }
            }
        }
        //  setting inSampleSize value allows to load a scaled down version of the original image
        options.inSampleSize = calculateInSampleSize(options, actualWidth, actualHeight)

        //  inJustDecodeBounds set to false to load the actual bitmap
        options.inJustDecodeBounds = false

        // this options allow android to claim the bitmap memory if it runs low on memory
        options.inPurgeable = true
        options.inInputShareable = true
        options.inTempStorage = ByteArray(16 * 1024)

        try {
            //  load the bitmap from its path
            bmp = BitmapFactory.decodeFile(filePath, options)
        } catch (exception: OutOfMemoryError) {
            exception.printStackTrace()
        }

        try {
            scaledBitmap = Bitmap.createBitmap(actualWidth, actualHeight, Bitmap.Config.ARGB_8888)
        } catch (exception: OutOfMemoryError) {
            exception.printStackTrace()
        }

        val ratioX = actualWidth / options.outWidth.toFloat()
        val ratioY = actualHeight / options.outHeight.toFloat()
        val middleX = actualWidth / 2.0f
        val middleY = actualHeight / 2.0f

        val scaleMatrix = Matrix()
        scaleMatrix.setScale(ratioX, ratioY, middleX, middleY)

        val canvas = Canvas(scaledBitmap!!)
        canvas.setMatrix(scaleMatrix)
        canvas.drawBitmap(
            bmp,
            middleX - bmp.width / 2,
            middleY - bmp.height / 2,
            Paint(Paint.FILTER_BITMAP_FLAG)
        )

        // check the rotation of the image and display it properly
        val exif: ExifInterface
        try {
            exif = ExifInterface(filePath!!)
            val orientation = exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION, 0
            )
            val matrix = Matrix()
            when (orientation) {
                6 -> matrix.postRotate(90f)
                3 -> matrix.postRotate(180f)
                8 -> matrix.postRotate(270f)
            }
            scaledBitmap = Bitmap.createBitmap(
                scaledBitmap, 0, 0, scaledBitmap.width, scaledBitmap.height, matrix,
                true
            )
            return scaledBitmap
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }

    private fun calculateInSampleSize(
        options: BitmapFactory.Options,
        reqWidth: Int,
        reqHeight: Int
    ): Int {
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val heightRatio = (height.toFloat() / reqHeight.toFloat()).roundToInt()
            val widthRatio = (width.toFloat() / reqWidth.toFloat()).roundToInt()
            inSampleSize = if (heightRatio < widthRatio) heightRatio else widthRatio
        }
        val totalPixels = (width * height).toFloat()
        val totalReqPixelsCap = (reqWidth * reqHeight * 2).toFloat()
        while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
            inSampleSize++
        }

        return inSampleSize
    }


    private fun getRealPathFromURI(context: Context, contentURI: String?): String? {
        val contentUri = Uri.parse(contentURI)
        val cursor = context.contentResolver.query(contentUri, null, null, null, null)
        return if (cursor == null) {
            contentUri.path
        } else {
            cursor.moveToFirst()
            val index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA)
            val path = cursor.getString(index)
            cursor.close()
            path
        }
    }


    fun addingPictureCanceled(context: Context) {
        if (::currentPhotoPath.isInitialized) {
            val file = File(getRealPathFromURI(context, currentPhotoPath)!!)
            if (file.exists()) {
                file.delete()
            }
        }
    }
}