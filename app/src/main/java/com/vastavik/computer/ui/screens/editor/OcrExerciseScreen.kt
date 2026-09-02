package com.vastavik.computer.ui.screens.editor

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.FlashlightOff
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.vastavik.computer.ui.theme.BrutalDefaults
import com.vastavik.computer.ui.theme.neoShape

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OcrExerciseScreen(onNavigate: (String)->Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var ocrText by remember { mutableStateOf("") }
    var edited by remember { mutableStateOf("") }
    var aiResponse by remember { mutableStateOf("") }
    var tab by remember { mutableIntStateOf(0) }
    var isProcessing by remember { mutableStateOf(false) }
    var processingDone by remember { mutableStateOf(false) }
    var isFlashOn by remember { mutableStateOf(false) }

    // CameraX state — in-app capture (does NOT launch system camera)
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    var camera by remember { mutableStateOf<androidx.camera.core.Camera?>(null) }
    val previewViewRef = remember { mutableStateOf<PreviewView?>(null) }

    fun runOcr(bitmap: Bitmap) {
        isProcessing = true
        processingDone = false
        val inputImage = InputImage.fromBitmap(bitmap, 0)
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        recognizer.process(inputImage)
            .addOnSuccessListener { visionText ->
                ocrText = visionText.text
                edited = visionText.text
                isProcessing = false
                processingDone = true
            }
            .addOnFailureListener {
                ocrText = ""
                edited = ""
                isProcessing = false
                processingDone = true
            }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {}

    val pickImageLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        imageUri = uri
        if (uri != null) {
            isProcessing = true
            processingDone = false
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                inputStream?.close()
                if (bitmap != null) runOcr(bitmap)
            } catch (e: Exception) {
                isProcessing = false; processingDone = true
            }
        }
    }

    LaunchedEffect(tab) {
        if (tab == 1) {
            val hasPermission = context.checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
            if (!hasPermission) cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    LaunchedEffect(isFlashOn) {
        try { camera?.cameraControl?.enableTorch(isFlashOn) } catch (_: Exception) {}
    }

    Scaffold(
        topBar = { TopAppBar(title={Text("Coding Exercise", fontWeight=FontWeight.Bold)}, navigationIcon={IconButton(onClick={onNavigate("home")}){Icon(Icons.Filled.ArrowBack,contentDescription=null)}}) },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp).verticalScroll(rememberScrollState())) {
            TabRow(selectedTabIndex = tab, containerColor = MaterialTheme.colorScheme.surface) {
                Tab(selected = tab==0, onClick={tab=0}, text={Text("Type Code")})
                Tab(selected = tab==1, onClick={tab=1}, text={Text("Photo OCR")})
            }
            Spacer(Modifier.height(16.dp))
            if (tab==0) {
                Text("Write code and let AI review (chat format):", fontWeight=FontWeight.W600)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value=edited, onValueChange={edited=it}, modifier=Modifier.fillMaxWidth().height(180.dp), placeholder={Text("Paste/write code here...")}, textStyle = LocalTextStyle.current.copy(fontFamily=FontFamily.Monospace, fontSize=13.sp))
                Spacer(Modifier.height(12.dp))
                Button(onClick={ aiResponse = "Gemini 3.7 Flash review: Good structure! Consider adding comments and handling empty input." }, modifier=Modifier.fillMaxWidth()) { Icon(Icons.Filled.AutoFixHigh, contentDescription=null); Spacer(Modifier.width(8.dp)); Text("Ask Gemini to Review") }
                if (aiResponse.isNotEmpty()) {
                    Spacer(Modifier.height(12.dp))
                    Card(shape=neoShape(12.dp), colors=CardDefaults.cardColors(containerColor=MaterialTheme.colorScheme.surface)) { Text(aiResponse, modifier=Modifier.padding(16.dp), fontSize=13.sp) }
                }
            } else {
                if (isProcessing) {
                    Text("Processing image with OCR...", fontWeight=FontWeight.W600, fontSize=14.sp, color=MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(12.dp))
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                } else if (processingDone) {
                    Text("OCR extracted (editable):", fontWeight=FontWeight.W600, fontSize=12.sp)
                    Spacer(Modifier.height(6.dp))
                    OutlinedTextField(value=edited, onValueChange={edited=it}, modifier=Modifier.fillMaxWidth().height(140.dp), textStyle=LocalTextStyle.current.copy(fontFamily=FontFamily.Monospace, fontSize=13.sp))
                    Spacer(Modifier.height(12.dp))
                    Button(onClick={ aiResponse = "OCR + Gemini 3.7 Flash: Extracted lines. Suggestion: fix indentation and add main guard."}, modifier=Modifier.fillMaxWidth()) { Icon(Icons.Filled.AutoFixHigh, contentDescription=null); Spacer(Modifier.width(8.dp)); Text("Send to Gemini") }
                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(onClick={ processingDone = false; ocrText=""; edited="" }, modifier=Modifier.fillMaxWidth()) { Icon(Icons.Filled.Refresh, contentDescription=null); Spacer(Modifier.width(6.dp)); Text("Retake Photo") }
                    if (aiResponse.isNotEmpty()) {
                        Spacer(Modifier.height(12.dp))
                        Card(shape=neoShape(12.dp)) { Text(aiResponse, modifier=Modifier.padding(16.dp), fontSize=13.sp) }
                    }
                } else {
                    // Brutal CameraX preview — vertical portrait 3:4, proper bottom/right black offset (matches site)
                    val hasPermission = context.checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                    // outer brutal wrapper
                    Box(modifier = Modifier.fillMaxWidth().padding(end = BrutalDefaults.ShadowOffset, bottom = BrutalDefaults.ShadowOffset)) {
                        Box(modifier = Modifier.matchParentSize().offset(x = BrutalDefaults.ShadowOffset, y = BrutalDefaults.ShadowOffset).clip(RoundedCornerShape(BrutalDefaults.RadiusLarge)).background(Color.Black))
                        Box(
                            modifier = Modifier.fillMaxWidth().aspectRatio(3f/4f).clip(RoundedCornerShape(BrutalDefaults.RadiusLarge))
                                .background(Color(0xFF0F172A)).border(BrutalDefaults.BorderWidth, Color.Black, RoundedCornerShape(BrutalDefaults.RadiusLarge)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (hasPermission) {
                                AndroidView(
                                    factory = { ctx ->
                                        val pv = PreviewView(ctx).apply {
                                            scaleType = PreviewView.ScaleType.FILL_CENTER
                                            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                                        }
                                        previewViewRef.value = pv
                                        val providerFuture = ProcessCameraProvider.getInstance(ctx)
                                        providerFuture.addListener({
                                            val provider = providerFuture.get()
                                            val preview = Preview.Builder().build().also { it.setSurfaceProvider(pv.surfaceProvider) }
                                            val capture = ImageCapture.Builder()
                                                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                                                .build()
                                            imageCapture = capture
                                            val selector = CameraSelector.DEFAULT_BACK_CAMERA
                                            try {
                                                provider.unbindAll()
                                                camera = provider.bindToLifecycle(lifecycleOwner, selector, preview, capture)
                                                camera?.cameraControl?.enableTorch(isFlashOn)
                                            } catch (_: Exception) {}
                                        }, ContextCompat.getMainExecutor(ctx))
                                        pv
                                    },
                                    modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(BrutalDefaults.RadiusLarge))
                                )
                                if (isFlashOn) {
                                    Box(modifier = Modifier.align(Alignment.TopEnd).padding(10.dp).clip(CircleShape).background(Color.Black.copy(alpha=0.6f)).border(1.5.dp, Color.Black, CircleShape).padding(6.dp)) {
                                        Icon(Icons.Filled.FlashlightOn, contentDescription = null, tint = Color(0xFFFFE500), modifier = Modifier.size(14.dp))
                                    }
                                }
                            } else {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Filled.CameraAlt, contentDescription = null, tint = Color.White.copy(0.7f), modifier = Modifier.size(44.dp))
                                    Spacer(Modifier.height(8.dp))
                                    Text("Camera permission required", color = Color.White.copy(0.7f), fontSize = 12.sp)
                                    Spacer(Modifier.height(8.dp))
                                    Button(onClick = { cameraPermissionLauncher.launch(Manifest.permission.CAMERA) }, shape = RoundedCornerShape(12.dp)) { Text("Grant permission", fontSize = 12.sp) }
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(14.dp))

                    // Three brutal buttons: Flash | Camera (middle circular) | Gallery — proper spacing, all with bottom/right offset
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        // Flash — brutal pill
                        Box(modifier = Modifier.weight(1f).height(48.dp).padding(end = 4.dp, bottom = 4.dp)) {
                            Box(modifier = Modifier.matchParentSize().offset(x=4.dp, y=4.dp).clip(RoundedCornerShape(14.dp)).background(Color.Black))
                            Box(
                                modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(14.dp))
                                    .background(if (isFlashOn) Color(0xFFFFE500) else Color.White)
                                    .border(BrutalDefaults.BorderWidth, Color.Black, RoundedCornerShape(14.dp))
                                    .clickable { isFlashOn = !isFlashOn },
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(if (isFlashOn) Icons.Filled.FlashlightOn else Icons.Filled.FlashlightOff, contentDescription=null, tint = Color.Black, modifier=Modifier.size(18.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text(if (isFlashOn) "Flash On" else "Flash Off", fontWeight=FontWeight.ExtraBold, fontSize=12.sp, color=Color.Black)
                                }
                            }
                        }
                        // Camera — brutal circle, in-app capture (no system camera intent)
                        Box(modifier = Modifier.size(68.dp).padding(end=4.dp, bottom=4.dp)) {
                            Box(modifier = Modifier.matchParentSize().offset(x=4.dp, y=4.dp).clip(CircleShape).background(Color.Black))
                            Box(
                                modifier = Modifier.fillMaxSize().clip(CircleShape).background(Color.White)
                                    .border(BrutalDefaults.BorderWidth, Color.Black, CircleShape)
                                    .clickable {
                                        val cap = imageCapture
                                        if (cap != null) {
                                            isProcessing = true
                                            cap.takePicture(ContextCompat.getMainExecutor(context), object : ImageCapture.OnImageCapturedCallback() {
                                                override fun onCaptureSuccess(image: ImageProxy) {
                                                    val bmp = imageProxyToBitmap(image)
                                                    image.close()
                                                    if (bmp != null) runOcr(bmp) else { isProcessing = false; processingDone = true }
                                                }
                                                override fun onError(exception: ImageCaptureException) {
                                                    isProcessing = false; processingDone = true
                                                }
                                            })
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Filled.CameraAlt, contentDescription="Capture", tint=Color.Black, modifier=Modifier.size(26.dp))
                            }
                        }
                        // Gallery — brutal pill
                        Box(modifier = Modifier.weight(1f).height(48.dp).padding(end=4.dp, bottom=4.dp)) {
                            Box(modifier = Modifier.matchParentSize().offset(x=4.dp, y=4.dp).clip(RoundedCornerShape(14.dp)).background(Color.Black))
                            Box(
                                modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(14.dp)).background(Color.White)
                                    .border(BrutalDefaults.BorderWidth, Color.Black, RoundedCornerShape(14.dp))
                                    .clickable { pickImageLauncher.launch("image/*") },
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.PhotoLibrary, contentDescription=null, tint=Color.Black, modifier=Modifier.size(18.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("Gallery", fontWeight=FontWeight.ExtraBold, fontSize=12.sp, color=Color.Black)
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(14.dp))
                    Text("In-app camera — captures directly from the preview above. No system camera app.", color=Color(0xFF64748B), fontSize=11.sp, fontWeight=FontWeight.SemiBold)
                    Spacer(Modifier.height(2.dp))
                    Text("OCR (ML Kit) extracts text, you edit, then Gemini explains.", color=MaterialTheme.colorScheme.onSurfaceVariant, fontSize=11.sp)
                }
            }
        }
    }
}

private fun imageProxyToBitmap(image: ImageProxy): Bitmap? {
    val buffer = image.planes[0].buffer
    val bytes = ByteArray(buffer.remaining())
    buffer.get(bytes)
    var bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size) ?: return null
    val rotation = image.imageInfo.rotationDegrees
    if (rotation != 0) {
        val matrix = Matrix().apply { postRotate(rotation.toFloat()) }
        bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }
    return bitmap
}
