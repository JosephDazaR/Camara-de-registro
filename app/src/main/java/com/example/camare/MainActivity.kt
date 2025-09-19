package com.example.camare

import android.Manifest
import android.annotation.SuppressLint
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
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {

    private val REQUEST_IMAGE_CAPTURE = 1
    private val REQUEST_PERMISSIONS = 100
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var photoNumber = 0
    private lateinit var cameraExecutor: ExecutorService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Crear el executor para CameraX
        cameraExecutor = Executors.newSingleThreadExecutor()

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        findViewById<ImageButton>(R.id.botonC).setOnClickListener {
            // Verificar permisos antes de iniciar la cámara
            if (checkPermissions()) {
                startCamera()  // Iniciar la cámara si los permisos están correctos
            } else {
                requestPermissions()  // Pedir permisos si no están concedidos
            }
        }
        findViewById<ImageButton>(R.id.registro).setOnClickListener {
            val registros = readPhotoMetadata()

            val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Registros de Fotos")
                .setMessage(registros)
                .setPositiveButton("Cerrar", null)
                .create()

            dialog.show()
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

            // Configurar la vista previa de la cámara
            val preview = Preview.Builder().build()

            // Obtener la vista previa de la cámara y vincularla al PreviewView
            val previewView = findViewById<androidx.camera.view.PreviewView>(R.id.previewView)
            preview.surfaceProvider = previewView.surfaceProvider

            // Configurar ImageCapture para capturar imágenes
            val imageCapture = ImageCapture.Builder().build()

            // Seleccionar la cámara trasera
            val cameraSelector = CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build()

            try {
                // Vincular la cámara con la vista previa y captura de imágenes
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture
                )

                // Botón para capturar foto
                findViewById<ImageButton>(R.id.botonC).setOnClickListener {
                    takePicture(imageCapture)
                }

            } catch (e: Exception) {
                Toast.makeText(this, "Error al iniciar la cámara", Toast.LENGTH_SHORT).show()
            }

        }, ContextCompat.getMainExecutor(this))
    }

    // Tomar la foto con CameraX
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

                    // Aquí guardar la foto y metadata (bitmap, lat, lon, fecha, número)
                    val fileName = photoFile.name
                    savePhotoMetadata(photoNumber, currentTime, lat, lon, fileName)

                    val toast = Toast(this@MainActivity)
                    val inflater = layoutInflater
                    val layout = inflater.inflate(R.layout.custom_toast_layout, null)

                    // Obtén el TextView del layout inflado
                    val toastText = layout.findViewById<TextView>(R.id.toast_text)

                    // Configura el texto del Toast personalizado
                    toastText.text = "Foto $photoNumber guardada con ubicación ($lat, $lon) a las $currentTime"

                    // Establece el layout del Toast
                    toast.view = layout
                    toast.duration = Toast.LENGTH_LONG // Puedes usar LENGTH_SHORT si prefieres menos tiempo
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
        cameraExecutor.shutdown()  // Limpiar el executor al destruir la actividad
    }
    private fun savePhotoMetadata(
        number: Int,
        dateTime: String,
        lat: Double,
        lon: Double,
        fileName: String
    ) {
        try {
            val logFile = File(filesDir, "photos_log.csv")
            val isNewFile = !logFile.exists()

            // Abrir en modo append
            logFile.appendText(
                if (isNewFile) {
                    // Cabecera si es la primera vez
                    "Numero,FechaHora,Latitud,Longitud,Archivo\n"
                } else {
                    ""
                }
            )

            // Escribir registro
            logFile.appendText("$number,$dateTime,$lat,$lon,$fileName\n")

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error guardando metadata", Toast.LENGTH_SHORT).show()
        }
    }
    private fun readPhotoMetadata(): String {
        val logFile = File(filesDir, "photos_log.csv")
        return if (logFile.exists()) {
            logFile.readText()
        } else {
            "No hay registros aún."
        }
    }


}
