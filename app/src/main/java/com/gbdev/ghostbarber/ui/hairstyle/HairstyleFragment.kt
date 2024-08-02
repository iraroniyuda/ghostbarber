package com.gbdev.ghostbarber.ui.hairstyle

import android.Manifest
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.gbdev.ghostbarber.BuildConfig
import com.gbdev.ghostbarber.R
import com.gbdev.ghostbarber.databinding.FragmentHairstyleBinding
import kotlinx.coroutines.*
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt
import kotlin.math.max
import kotlin.math.min
import android.media.ExifInterface
import android.graphics.Matrix
import android.os.Environment
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import android.view.animation.LinearInterpolator
import android.animation.ObjectAnimator

class HairstyleFragment : Fragment() {

    private var _binding: FragmentHairstyleBinding? = null
    private val binding get() = _binding!!

    private val apiKey = BuildConfig.API_KEY
    private var currentPhotoPath: String? = null
    private var processedImageUrl: String? = null

    private val pickImageResultLauncher = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri: Uri? ->
        uri?.let {
            Log.d("HairstyleFragment", "Image selected: $it")
            currentPhotoPath = handleImageUriWithGlide(it)
            showImagePickedUI()
        }
    }

    private val takePictureResultLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success: Boolean ->
        if (success) {
            currentPhotoPath?.let { handleCameraPhotoWithGlide(it) }
            showImagePickedUI()
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        handlePermissionResult(permissions)
    }

    private val createDocumentLauncher = registerForActivityResult(ActivityResultContracts.CreateDocument("image/jpeg")) { uri: Uri? ->
        uri?.let { saveImageToUri(it) }
    }

    private lateinit var selectedHairstyle: Hairstyle
    private var selectedColor: String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHairstyleBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Setup RecyclerView and Spinner
        setupRecyclerView()
        setupColorSpinner()

        // Setup button listeners
        setupButtonListeners()

        // Initial UI state
        resetPageToInitialState()

        // Initialize new buttons
        view.findViewById<ImageButton>(R.id.homeButton).setOnClickListener {
            findNavController().navigate(R.id.navigation_home)
        }
        view.findViewById<ImageButton>(R.id.profileButton).setOnClickListener {
            findNavController().navigate(R.id.navigation_profile)
        }
        view.findViewById<ImageButton>(R.id.reelsButton).setOnClickListener {
            findNavController().navigate(R.id.navigation_reels)
        }
        view.findViewById<ImageButton>(R.id.aiHairstyleButton).setOnClickListener {
            // Do nothing when clicked
        }
        view.findViewById<ImageButton>(R.id.ic_booking).setOnClickListener {
            findNavController().navigate(R.id.bookFragment)
        }
        view.findViewById<ImageButton>(R.id.moreButton).setOnClickListener {
            findNavController().navigate(R.id.moreFragment)
        }
    }

    private fun setupRecyclerView() {
        val hairstyles = listOf(
            Hairstyle("BuzzCut", R.drawable.buzz_cut),
            Hairstyle("UnderCut", R.drawable.under_cut),
            Hairstyle("Pompadour", R.drawable.pompadour),
            Hairstyle("SlickBack", R.drawable.slickback),
            Hairstyle("CurlyShag", R.drawable.curlyshag),
            Hairstyle("WavyShag", R.drawable.wavy_shag),
            Hairstyle("FauxHawk", R.drawable.fauxhawk),
            Hairstyle("Spiky", R.drawable.spiky),
            Hairstyle("CombOver", R.drawable.combover),
            Hairstyle("HighTightFade", R.drawable.high_tight_fade),
            Hairstyle("ManBun", R.drawable.manbun),
            Hairstyle("Afro", R.drawable.afro),
            Hairstyle("LowFade", R.drawable.lowfade),
            Hairstyle("UndercutLongHair", R.drawable.undercut_long_hair),
            Hairstyle("TwoBlockHaircut", R.drawable.twoblock_hair_cut),
            Hairstyle("TexturedFringe", R.drawable.textured_fringe),
            Hairstyle("BluntBowlCut", R.drawable.bluntbowlcut),
            Hairstyle("LongWavyCurtainBangs", R.drawable.longwavycurtainbangs),
            Hairstyle("MessyTousled", R.drawable.messytousled),
            Hairstyle("MediumLengthWavy", R.drawable.mediumlengthwavy),
            Hairstyle("CornrowBraids", R.drawable.cornrow_braids),
            Hairstyle("LongHairTiedUp", R.drawable.longhairtiedup),
            Hairstyle("Middle-parted", R.drawable.middleparted),
        )

        val adapter = HairstyleAdapter(hairstyles) { selectedHairstyle ->
            this.selectedHairstyle = selectedHairstyle
            binding.selectedHairstyleName.text = selectedHairstyle.name
            validateSelections()
        }

        binding.recyclerViewHairstyles.layoutManager = GridLayoutManager(requireContext(), 2) // 2 columns
        binding.recyclerViewHairstyles.adapter = adapter
    }

    private fun setupColorSpinner() {
        val colorsArray = resources.getStringArray(R.array.colors_array).toMutableList()
        colorsArray.add(0, getString(R.string.pick_hair_color)) // Add the hint at the first position

        val adapter = object : ArrayAdapter<String>(requireContext(), android.R.layout.simple_spinner_item, colorsArray) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val textView = super.getView(position, convertView, parent) as TextView
                textView.setTextColor(resources.getColor(R.color.white)) // Set text color to white
                textView.setBackgroundColor(resources.getColor(android.R.color.black)) // Set background color to black
                return textView
            }

            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                val textView = super.getDropDownView(position, convertView, parent) as TextView
                textView.setTextColor(resources.getColor(R.color.white)) // Set text color to white
                textView.setBackgroundColor(resources.getColor(android.R.color.black)) // Set background color to black
                return textView
            }
        }

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.colorSpinner.adapter = adapter

        // Set the Spinner background to transparent
        binding.colorSpinner.setBackgroundColor(resources.getColor(android.R.color.transparent))

        binding.colorSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                selectedColor = if (position == 0) null else parent.getItemAtPosition(position) as String
                validateSelections()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                selectedColor = null
                validateSelections()
            }
        }
    }

    private fun setupButtonListeners() {
        binding.selectImageButton.setOnClickListener {
            if (isAtLeastAndroidTiramisu()) {
                launchPhotoPicker()
            } else {
                checkAndRequestPermissions(
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
                ) {
                    launchImagePicker()
                }
            }
        }

        binding.captureImageButton.setOnClickListener {
            checkAndRequestPermissions(
                arrayOf(Manifest.permission.CAMERA)
            ) {
                launchCamera()
            }
        }

        binding.changeHairstyleButton.setOnClickListener {
            currentPhotoPath?.let {
                showProcessingIndicator()
                showLoadingBarAnimation()
                changeHairstyle(it, selectedHairstyle.name, selectedColor ?: "")
                binding.changeHairstyleButton.visibility = View.GONE
                binding.selectImageButton.visibility = View.GONE
                binding.captureImageButton.visibility = View.GONE
                binding.recyclerViewHairstyles.visibility = View.GONE
                binding.colorSpinner.visibility = View.GONE
            }
        }

        binding.downloadImageButton.setOnClickListener {
            showDownloadDialog()
        }

        binding.removeImageButton.setOnClickListener {
            showRemoveConfirmationDialog()
        }

        binding.backToHomeButton.setOnClickListener {
            findNavController().navigate(R.id.action_hairstyleFragment_to_navigation_home)
        }
    }

    private fun showLoadingBarAnimation() {
        binding.progressBar.visibility = View.VISIBLE
        val animator = ObjectAnimator.ofInt(binding.progressBar, "progress", 0, 100)
        animator.duration = 10000 // 10 seconds
        animator.interpolator = LinearInterpolator()
        animator.start()
    }

    private fun launchPhotoPicker() {
        val request = PickVisualMediaRequest.Builder()
            .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly)
            .build()
        pickImageResultLauncher.launch(request)
    }

    private fun showDownloadDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Download Image")
        val input = EditText(requireContext())
        input.hint = "Enter file name"
        builder.setView(input)
        builder.setPositiveButton("Download") { _, _ ->
            val fileName = input.text.toString()
            if (fileName.isNotEmpty()) {
                createDocumentLauncher.launch("$fileName.jpg")
            } else {
                Toast.makeText(requireContext(), "File name cannot be empty", Toast.LENGTH_SHORT).show()
            }
        }
        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.cancel()
        }
        builder.show()
    }

    private fun saveImageToUri(uri: Uri) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                processedImageUrl?.let { url ->
                    val bitmap = Glide.with(this@HairstyleFragment)
                        .asBitmap()
                        .load(url)
                        .submit()
                        .get()

                    requireContext().contentResolver.openOutputStream(uri)?.use { out ->
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
                    }

                    withContext(Dispatchers.Main) {
                        Toast.makeText(requireContext(), "Image downloaded to: $uri", Toast.LENGTH_LONG).show()
                        Log.d("HairstyleFragment", "Image downloaded to: $uri")
                        resetPageToInitialState() // Reset the page after download
                    }
                }
            } catch (e: IOException) {
                Log.e("HairstyleFragment", "Download failed: ${e.message}")
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Download failed", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showRemoveConfirmationDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Remove Image")
        builder.setMessage("Do you want to remove the result without downloading it?")
        builder.setPositiveButton("Yes") { _, _ ->
            removeImageAndResetPage()
        }
        builder.setNegativeButton("No") { dialog, _ ->
            dialog.cancel()
        }
        builder.show()
    }

    private fun removeImageAndResetPage() {
        currentPhotoPath?.let {
            val file = File(it)
            if (file.exists()) file.delete()
        }
        resetPageToInitialState() // Reset the page after removing the image
    }

    private fun validateSelections() {
        if (this::selectedHairstyle.isInitialized && selectedColor != null) {
            if (selectedHairstyle.name != "Choose Hairstyle" && selectedColor != "Choose Color") {
                showChangeHairstyleButton()
            } else {
                hideChangeHairstyleButton()
            }
        }
    }

    private fun showImagePickedUI() {
        binding.recyclerViewHairstyles.visibility = View.VISIBLE
        binding.colorSpinner.visibility = View.VISIBLE
        Toast.makeText(requireContext(), "Please select hairstyle and color", Toast.LENGTH_LONG).show()
    }

    private fun hideChangeHairstyleButton() {
        binding.changeHairstyleButton.visibility = View.GONE
    }

    private fun showChangeHairstyleButton() {
        binding.changeHairstyleButton.visibility = View.VISIBLE
    }

    private fun hideDownloadRemoveButtons() {
        binding.buttonDownloadRemoveContainer.visibility = View.GONE
    }

    private fun showDownloadRemoveButtons() {
        binding.buttonDownloadRemoveContainer.visibility = View.VISIBLE
    }

    private fun hideSpinners() {
        binding.recyclerViewHairstyles.visibility = View.GONE
        binding.colorSpinner.visibility = View.GONE
    }

    private fun launchImagePicker() {
        Log.d("HairstyleFragment", "Launching image picker")
        val request = PickVisualMediaRequest.Builder()
            .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly)
            .build()
        pickImageResultLauncher.launch(request)
    }

    private fun launchCamera() {
        Log.d("HairstyleFragment", "Launching camera")
        val photoFile: File? = createImageFile()
        photoFile?.let {
            val photoURI: Uri = FileProvider.getUriForFile(requireContext(), "com.gbdev.ghostbarber.fileprovider", it)
            takePictureResultLauncher.launch(photoURI)
        }
    }

    private fun checkAndRequestPermissions(permissions: Array<String>, onAllPermissionsGranted: () -> Unit) {
        val permissionsNeeded = permissions.filter {
            ContextCompat.checkSelfPermission(requireContext(), it) != PackageManager.PERMISSION_GRANTED
        }
        Log.d("HairstyleFragment", "Permissions needed: $permissionsNeeded")
        if (permissionsNeeded.isNotEmpty()) {
            requestPermissionLauncher.launch(permissionsNeeded.toTypedArray())
        } else {
            onAllPermissionsGranted()
        }
    }

    private fun handlePermissionResult(permissions: Map<String, Boolean>) {
        permissions.forEach { (permission, granted) ->
            Log.d("HairstyleFragment", "Permission: $permission, Granted: $granted")
        }
        if (permissions.entries.all { it.value }) {
            Toast.makeText(requireContext(), "All permissions granted", Toast.LENGTH_SHORT).show()
            // Proceed with the action that requires permissions
        } else {
            Toast.makeText(requireContext(), "Permissions denied. Please grant the required permissions.", Toast.LENGTH_LONG).show()
            Log.e("HairstyleFragment", "Required permissions denied")
        }
    }

    private fun createImageFile(): File? {
        return try {
            val storageDir: File? = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir).also {
                currentPhotoPath = it.absolutePath
                Log.d("HairstyleFragment", "File created at: ${it.absolutePath}")
            }
        } catch (ex: IOException) {
            Log.e("HairstyleFragment", "Error creating file: ${ex.message}")
            null
        }
    }

    private fun handleCameraPhotoWithGlide(filePath: String): String {
        val resizedBitmap = resizeImage(filePath, 2000, 2000)
        resizedBitmap?.let {
            val file = File(filePath)
            FileOutputStream(file).use { out ->
                it.compress(Bitmap.CompressFormat.JPEG, 85, out)
            }
            currentPhotoPath = file.absolutePath
            Glide.with(this)
                .load(Uri.fromFile(file))
                .apply(RequestOptions().skipMemoryCache(true).diskCacheStrategy(com.bumptech.glide.load.engine.DiskCacheStrategy.NONE))
                .into(binding.hairstyleImageView)
        }
        return filePath
    }

    private fun handleImageUriWithGlide(uri: Uri): String {
        Log.d("HairstyleFragment", "Handling image URI: $uri")
        val file = File(requireContext().cacheDir, "picked_image.jpg")
        if (file.exists()) file.delete() // Ensure any existing file is deleted

        val inputStream = requireContext().contentResolver.openInputStream(uri)
        if (inputStream != null) {
            try {
                val outputStream = FileOutputStream(file)
                inputStream.copyTo(outputStream)
                outputStream.close()
                inputStream.close()
                Log.d("HairstyleFragment", "New file created for picked image at: ${file.absolutePath}")

                // Resize the image
                val resizedBitmap = resizeImage(file.absolutePath, 2000, 2000)
                resizedBitmap?.let {
                    FileOutputStream(file).use { out ->
                        it.compress(Bitmap.CompressFormat.JPEG, 85, out)
                    }
                }

                // Set the current photo path to the new file path
                currentPhotoPath = file.absolutePath

                // Load the image into the ImageView using Glide
                Glide.with(this)
                    .load(Uri.fromFile(file))
                    .apply(RequestOptions().skipMemoryCache(true).diskCacheStrategy(com.bumptech.glide.load.engine.DiskCacheStrategy.NONE))
                    .into(binding.hairstyleImageView)

            } catch (e: IOException) {
                Log.e("HairstyleFragment", "Failed to write picked image to cache", e)
            }
        } else {
            Log.e("HairstyleFragment", "Input stream is null for URI: $uri")
        }

        return file.absolutePath
    }

    private fun changeHairstyle(imagePath: String, hairstyle: String, color: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val file = File(imagePath)
            Log.d("HairstyleFragment", "Attempting to access file at: $imagePath. Exists: ${file.exists()} Can Read: ${file.canRead()}")
            if (!file.exists() or !file.canRead()) {
                Log.e("HairstyleFragment", "File does not exist or can't be read: $imagePath")
                handleAPIFailure("File does not exist or can't be read")
                return@launch
            }

            // Resize image if necessary
            val resizedBitmap = resizeImage(imagePath, 2000, 2000)
            resizedBitmap?.let {
                FileOutputStream(file).use { out ->
                    it.compress(Bitmap.CompressFormat.JPEG, 85, out)
                }
                // Log the dimensions of the resized image
                Log.d("HairstyleFragment", "Resized image dimensions: ${resizedBitmap.width}x${resizedBitmap.height}")
            }

            val requestBody = MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("task_type", "async")
                .addFormDataPart("auto", "1")
                .addFormDataPart("image_size", "1")
                .addFormDataPart("image", file.name, file.asRequestBody("image/jpeg".toMediaTypeOrNull()))
                .addFormDataPart("hair_style", hairstyle)
                .addFormDataPart("color", color)
                .build()

            val request = Request.Builder()
                .url("https://www.ailabapi.com/api/portrait/effects/hairstyle-editor-pro")
                .post(requestBody)
                .addHeader("ailabapi-api-key", apiKey)
                .build()

            OkHttpClient().newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Log.e("HairstyleFragment", "API call failed: ${e.message}")
                    handleAPIFailure(e.message ?: "Unknown error")
                }

                override fun onResponse(call: Call, response: Response) {
                    if (!response.isSuccessful) {
                        Log.e("HairstyleFragment", "API call unsuccessful: ${response.code}")
                        handleAPIFailure(parseErrorMessage(response.body?.string()))
                        return
                    }
                    val responseBody = response.body!!.string()
                    Log.d("HairstyleFragment", "API Response: $responseBody")  // Log the full response
                    val result = JSONObject(responseBody)
                    val taskId = result.optString("task_id")
                    if (taskId.isNotEmpty()) {
                        // Query the result of the asynchronous task
                        queryTaskResult(taskId)
                    } else {
                        Log.e("HairstyleFragment", "No task ID found in response")
                        handleAPIFailure("No task ID found in response")
                    }
                }
            })
        }
    }

    private fun parseErrorMessage(responseBody: String?): String {
        return try {
            val result = JSONObject(responseBody)
            val errorDetail = result.optJSONObject("error_detail")
            errorDetail?.optString("message") ?: "Unknown error"
        } catch (e: Exception) {
            "Unknown error"
        }
    }

    private fun queryTaskResult(taskId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            var continuePolling = true
            val url = "https://www.ailabapi.com/api/common/query-async-task-result?task_id=$taskId"
            val request = Request.Builder()
                .url(url)
                .get()
                .addHeader("ailabapi-api-key", apiKey)
                .build()

            while (continuePolling) {
                delay(5000) // Wait for 5 seconds before polling again

                OkHttpClient().newCall(request).execute().use { response ->
                    val responseBody = response.body!!.string()
                    val result = JSONObject(responseBody)
                    if (result.has("task_status")) {
                        val taskStatus = result.getInt("task_status")

                        withContext(Dispatchers.Main) {
                            when (taskStatus) {
                                2 -> { // Processing was successful
                                    val data = result.optJSONObject("data")
                                    val imageUrls = data?.optJSONArray("images")
                                    if (imageUrls != null && imageUrls.length() > 0) {
                                        val imageUrl = imageUrls.getString(0)
                                        Log.d("HairstyleFragment", "Image URL found: $imageUrl")
                                        processedImageUrl = imageUrl
                                        updateImageView(imageUrl)
                                        continuePolling = false // Stop the loop
                                        showDownloadRemoveButtons()
                                        hideChangeHairstyleButton()
                                    } else {
                                        Log.e("HairstyleFragment", "No image URL found in task result. Full result: $result")
                                        continuePolling = false // Stop the loop
                                    }
                                }
                                0 -> Log.d("HairstyleFragment", "Task is queued")
                                1 -> Log.d("HairstyleFragment", "Task still processing")
                                else -> {
                                    Log.e("HairstyleFragment", "Error in task processing. Full result: $result")
                                    handleAPIFailure(parseErrorMessage(responseBody))
                                    continuePolling = false // Stop the loop on error
                                }
                            }
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            Log.e("HairstyleFragment", "No task_status in response. Full result: $result")
                            handleAPIFailure(parseErrorMessage(responseBody))
                            continuePolling = false // Stop the loop
                        }
                    }
                }
            }
        }
    }

    private fun resizeImage(filePath: String, maxWidth: Int, maxHeight: Int): Bitmap? {
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        BitmapFactory.decodeFile(filePath, options)

        val (originalHeight: Int, originalWidth: Int) = options.run { outHeight to outWidth }
        var inSampleSize = 1

        if (originalHeight > maxHeight || originalWidth > maxWidth) {
            val heightRatio = (originalHeight.toFloat() / maxHeight.toFloat()).roundToInt()
            val widthRatio = (originalWidth.toFloat() / maxWidth.toFloat()).roundToInt()
            inSampleSize = maxOf(heightRatio, widthRatio)
        }

        options.inSampleSize = inSampleSize
        options.inJustDecodeBounds = false

        var resizedBitmap = BitmapFactory.decodeFile(filePath, options)

        // Further scale the bitmap if necessary to fit exactly within maxWidth and maxHeight
        resizedBitmap = resizedBitmap?.let {
            Bitmap.createScaledBitmap(it, minOf(it.width, maxWidth), minOf(it.height, maxHeight), true)
        }

        // Correct the orientation based on EXIF data
        resizedBitmap = resizedBitmap?.let {
            correctBitmapOrientation(filePath, it)
        }

        resizedBitmap?.let {
            Log.d("HairstyleFragment", "Resized image dimensions: ${it.width}x${it.height}")
        }

        return resizedBitmap
    }

    private fun correctBitmapOrientation(filePath: String, bitmap: Bitmap): Bitmap {
        val exif = ExifInterface(filePath)
        val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
        val matrix = Matrix()

        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.postScale(-1f, 1f)
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.postScale(1f, -1f)
            // Add other cases if needed
        }

        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    private fun handleAPIFailure(message: String) {
        CoroutineScope(Dispatchers.Main).launch {
            hideProcessingIndicator()
            hideLoadingBar()
            showToast(message)
            resetPageToInitialState()
        }
    }

    private fun updateImageView(imageUrl: String) {
        Log.d("HairstyleFragment", "Updating ImageView with URL: $imageUrl")
        Glide.with(this)
            .load(imageUrl)
            .apply(RequestOptions().error(R.drawable.error_placeholder)) // Use an error placeholder
            .into(binding.hairstyleImageView)

        binding.selectImageButton.visibility = View.GONE
        binding.captureImageButton.visibility = View.GONE
        binding.downloadImageButton.visibility = View.VISIBLE
        binding.removeImageButton.visibility = View.VISIBLE
        hideProcessingIndicator()
        hideLoadingBar()
    }

    private fun resetPageToInitialState() {
        binding.hairstyleImageView.setImageDrawable(null)
        currentPhotoPath = null
        processedImageUrl = null
        hideChangeHairstyleButton()
        hideDownloadRemoveButtons()
        hideSpinners()
        binding.colorSpinner.setSelection(0)
        binding.selectedHairstyleName.text = ""
        binding.selectImageButton.visibility = View.VISIBLE
        binding.captureImageButton.visibility = View.VISIBLE
        binding.recyclerViewHairstyles.visibility = View.GONE
        binding.colorSpinner.visibility = View.GONE
        clearGlideCache()
    }

    private fun showProcessingIndicator() {
        binding.processingIndicator.visibility = View.VISIBLE
    }

    private fun hideProcessingIndicator() {
        binding.processingIndicator.visibility = View.GONE
    }

    private fun hideLoadingBar() {
        binding.progressBar.visibility = View.GONE
    }

    private fun showToast(message: String) {
        val toast = Toast.makeText(requireContext(), message, Toast.LENGTH_LONG)
        toast.show()
    }

    private fun clearGlideCache() {
        CoroutineScope(Dispatchers.IO).launch {
            Glide.get(requireContext()).clearDiskCache()
        }
        Glide.get(requireContext()).clearMemory()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        currentPhotoPath?.let {
            val file = File(it)
            if (file.exists()) file.delete()
        }
    }

    companion object {
        private const val PICK_IMAGES_REQUEST_CODE = 1001

        fun isAtLeastAndroidTiramisu(): Boolean {
            return android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU
        }
    }
}
