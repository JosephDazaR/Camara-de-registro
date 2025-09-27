package com.example.camare

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.TextView
import android.widget.ImageButton
import android.widget.Toast
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.example.camare.model.FotoEntity
import com.example.camare.viewmodel.FotoViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {

    private val REQUEST_PERMISSIONS = 100
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var photoNumber = 0
    private lateinit var cameraExecutor: ExecutorService

    // 👇 estas deben ir aquí arriba
    private lateinit var fotoViewModel: FotoViewModel
    private var cachedFotos: List<FotoEntity> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Crear el executor para CameraX
        cameraExecutor = Executors.newSingleThreadExecutor()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Inicializar ViewModel
        fotoViewModel = ViewModelProvider(this).get(FotoViewModel::class.java)
        fotoViewModel.todasLasFotos.observe(this) { fotos ->
            cachedFotos = fotos ?: emptyList()
            photoNumber = cachedFotos.size
        }

        findViewById<ImageButton>(R.id.botonC).setOnClickListener {
            if (checkPermissions()) {
                startCamera()
            } else {
                requestPermissions()
            }
        }

        findViewById<ImageButton>(R.id.registro).setOnClickListener {
            val intent = Intent(this, RegistrosActivity::class.java)
            startActivity(intent)
        }

    }

    private fun checkPermissions(): Boolean {
        val cameraPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
        val locationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
        return cameraPermission == PackageManager.PERMISSION_GRANTED &&
                locationPermission == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.CAMERA, Manifest.permission.ACCESS_FINE_LOCATION),
            REQUEST_PERMISSIONS
        )
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSIONS) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                startCamera()
            } else {
                Toast.makeText(this, "Permisos requeridos para usar cámara y ubicación", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Iniciar la cámara con CameraX
    @SuppressLint("MissingPermission")
    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build()
            val previewView = findViewById<androidx.camera.view.PreviewView>(R.id.previewView)
            preview.surfaceProvider = previewView.surfaceProvider

            val imageCapture = ImageCapture.Builder().build()
            val cameraSelector = CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build()

            try {
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)
                findViewById<ImageButton>(R.id.botonC).setOnClickListener {
                    takePicture(imageCapture)
                }
            } catch (e: Exception) {
                Toast.makeText(this, "Error al iniciar la cámara", Toast.LENGTH_SHORT).show()
            }

        }, ContextCompat.getMainExecutor(this))
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    private fun takePicture(imageCapture: ImageCapture) {
        val photoFile = File(externalMediaDirs.first(), "${UUID.randomUUID()}.jpg")
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(outputOptions, cameraExecutor, object : ImageCapture.OnImageSavedCallback {
            @SuppressLint("MissingPermission")
            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                val currentTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
                photoNumber++

                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    val lat = location?.latitude ?: 0.0
                    val lon = location?.longitude ?: 0.0

                    // Guardar en Room
                    val foto = FotoEntity(
                        numero = photoNumber,
                        fechaHora = currentTime,
                        latitud = lat,
                        longitud = lon,
                        uri = photoFile.absolutePath
                    )
                    fotoViewModel.insertarFoto(foto)

                    // Mostrar Toast
                    val toast = Toast(this@MainActivity)
                    val inflater = layoutInflater
                    val layout = inflater.inflate(R.layout.custom_toast_layout, null)
                    val toastText = layout.findViewById<TextView>(R.id.toast_text)
                    toastText.text = "Foto $photoNumber guardada con ubicación ($lat, $lon) a las $currentTime"
                    toast.view = layout
                    toast.duration = Toast.LENGTH_LONG
                    toast.show()
                }
            }

            override fun onError(exception: ImageCaptureException) {
                Toast.makeText(this@MainActivity, "Error al capturar la foto", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}
