package com.example.translateocrapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.translateocrapp.databinding.ActivityMainBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

typealias LumaListener = (luma: Double) -> Unit

class MainActivity : AppCompatActivity() {
    // This is the main activity of the app
    private lateinit var bottomSheetActions:BottomSheetBehavior<ConstraintLayout>
    private lateinit var viewBinding: ActivityMainBinding
    // Create a progress dialog
    private lateinit var progressBar: ProgressBar

    // AlertDialog to show download progress
    private var progressDialog: AlertDialog? = null

    private var imageCapture: ImageCapture? = null

    private lateinit var cameraExecutor: ExecutorService
    private lateinit var myRecyclerView: RecyclerView
    private lateinit var database: OcrDatabase
    private lateinit var ocrResultAdapter: OcrResultAdapter

    private val activityResultLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        )
        { permissions ->
            // Handle Permission granted/rejected
            var permissionGranted = true
            permissions.entries.forEach {
                if (it.key in REQUIRED_PERMISSIONS && it.value == false)
                    permissionGranted = false
            }
            if (!permissionGranted) {
                Toast.makeText(
                    baseContext,
                    "Permission request denied",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                startCamera()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        // Initialize database
        database = OcrDatabase.getDatabase(this)

        val bottomSheet = findViewById<ConstraintLayout>(R.id.bottom_sheet)
        myRecyclerView = findViewById<RecyclerView>(R.id.rcBookmARKED)
        myRecyclerView.layoutManager = LinearLayoutManager(this)
        ocrResultAdapter = OcrResultAdapter(emptyList()) // Start with empty list
        myRecyclerView.adapter = ocrResultAdapter


        // Initialize BottomSheetBehavior
        bottomSheetActions = BottomSheetBehavior.from(bottomSheet)

        // Optional: Set initial state
        bottomSheetActions.state = BottomSheetBehavior.STATE_COLLAPSED

        // Request camera permissions
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            requestPermissions()
        }
        progressBar = ProgressBar(this).apply {
            isIndeterminate = true
        }

        // Set up the listeners for take photo and video capture buttons
        viewBinding.imageCaptureButton.setOnClickListener { takePhoto() }

        cameraExecutor = Executors.newSingleThreadExecutor()

        bottomSheet.setOnClickListener{
            if (bottomSheetActions.state == BottomSheetBehavior.STATE_EXPANDED)
                bottomSheetActions.setState(
                BottomSheetBehavior.STATE_COLLAPSED
            )
            else bottomSheetActions.setState(BottomSheetBehavior.STATE_EXPANDED)
        }

        bottomSheetActions.addBottomSheetCallback(object:BottomSheetBehavior.BottomSheetCallback(){
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                    // Fetch data when bottom sheet is expanded
                    fetchOcrResults()
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                myRecyclerView.alpha = slideOffset
                myRecyclerView.visibility = if (slideOffset > 0) View.VISIBLE else View.INVISIBLE
            }

        })
    }


    private fun fetchOcrResults() {
        CoroutineScope(Dispatchers.Main).launch {
            val results = withContext(Dispatchers.IO) {
                database.ocrResultDao().getAllOcrResults()
            }
            ocrResultAdapter.updateData(results)
          
            if (results.isEmpty()) {
                myRecyclerView.visibility = View.INVISIBLE // Hide if no data
            }
        }
    }
    private fun takePhoto() {
        Handler(Looper.getMainLooper()).post {
            progressDialog = AlertDialog.Builder(this)
                .setTitle("Processing ...")
                .setCancelable(false)
                .setView(ProgressBar(this).apply { isIndeterminate = true }) // No reuse
                .show()
        }

        Log.d(TAG, "takePhoto: called")

        // Get a stable reference of the modifiable image capture use case
        val imageCapture = imageCapture ?: return

        // Create time stamped name and MediaStore entry.
        val fileName = "translateocrapp.jpg"

        // Get the output directory for saving the image
        val outputDirectory = getOutputDirectory()

        // Create the file for the image capture
        val photoFile = File(outputDirectory, fileName)

        // Create output options object which contains the file to save the image
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()


        Log.d(TAG, "takePhoto: contentValues created")

        Log.d(TAG, "takePhoto: outputOptions created")
        // Set up image capture listener, which is triggered after photo has
        // been taken
        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
                    Handler(Looper.getMainLooper()).post {
                        progressDialog?.dismiss()
                        progressDialog = null
                    }
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val msg = "Photo capture succeeded: ${output.savedUri}"

                                      // Start the PreviewActivity and pass the image path
                    val intent = Intent(this@MainActivity, PreviewActivity::class.java)
                    intent.putExtra(PreviewActivity.EXTRA_IMAGE_PATH, photoFile.absolutePath)
                    Handler(Looper.getMainLooper()).post {
                        progressDialog?.dismiss()
                        progressDialog = null
                    }
                    startActivity(intent)
                }
            }
        )

    }


    // Function to get the app's private directory for saving images
    private fun getOutputDirectory(): File {
        val mediaDir = externalMediaDirs.firstOrNull()?.let {
            File(it, resources.getString(R.string.app_name)).apply { mkdirs() }
        }
        return if (mediaDir != null && mediaDir.exists())
            mediaDir else filesDir
    }

    private fun startCamera() {

        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(viewBinding.viewFinder.surfaceProvider)
                }

            imageCapture = ImageCapture.Builder()
                .build()

            // Select back camera as a default
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture
                )

            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(this))

    }

    private fun requestPermissions() {
        activityResultLauncher.launch(REQUIRED_PERMISSIONS)
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext, it
        ) == PackageManager.PERMISSION_GRANTED
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }


    companion object {
        private const val TAG = "CameraXApp"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private val REQUIRED_PERMISSIONS =
            mutableListOf(
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO
            ).apply {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                    add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }.toTypedArray()
    }


}
