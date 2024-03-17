package com.example.annotations.docscanner

import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.annotations.docscanner.ui.theme.DocScannerTheme
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions.RESULT_FORMAT_JPEG
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions.RESULT_FORMAT_PDF
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions.SCANNER_MODE_FULL
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult
import java.io.File
import java.io.FileOutputStream

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val options = GmsDocumentScannerOptions.Builder()
            .setGalleryImportAllowed(true)
            .setPageLimit(5)
            .setResultFormats(RESULT_FORMAT_JPEG, RESULT_FORMAT_PDF)
            .setScannerMode(SCANNER_MODE_FULL)
            .build()

        val scanner = GmsDocumentScanning.getClient(options)
        setContent {
            DocScannerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var imageUris by remember {
                        mutableStateOf<List<Uri>>(emptyList())
                    }
                    // launch document scanning activity
                    val scannerLauncher =
                        rememberLauncherForActivityResult(contract = ActivityResultContracts.StartIntentSenderForResult()) { result ->
                            if (result.resultCode == RESULT_OK) {
                                val res =
                                    GmsDocumentScanningResult.fromActivityResultIntent(result.data)
                                imageUris = res?.pages?.map { it.imageUri } ?: emptyList()

                                res?.pdf?.let { pdf ->
                                    val fos = FileOutputStream(File(filesDir, "scan.pdf"))
                                    contentResolver.openInputStream(pdf.uri)?.use { it.copyTo(fos) }
                                }
                            }

                        }

                    // UI
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        imageUris.forEach { uri ->
                            AsyncImage(
                                model = uri,
                                contentDescription = "",
                                contentScale = ContentScale.FillWidth,
                                modifier = Modifier.fillMaxWidth(1f)
                            )
                        }
                        Button(modifier = Modifier
                            .wrapContentSize(), onClick = {
                            scanner.getStartScanIntent(this@MainActivity)
                                .addOnSuccessListener {
                                    scannerLauncher.launch(IntentSenderRequest.Builder(it).build())
                                }
                                .addOnFailureListener {
                                    Toast.makeText(this@MainActivity, it.message, Toast.LENGTH_LONG).show()
                                }
                        }) {
                            Text(text = "Scan PDF")
                        }
                    }
                }
            }
        }
    }
}
