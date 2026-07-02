package com.drivertest.app.ui.add

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import com.drivertest.app.ui.components.ErrorDialog
import java.io.File

@Composable
fun AddScreen(
    viewModel: AddViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    // Camera launcher for OCR photo capture — uses TakePicture with URI
    // (TakePicturePreview is unreliable: many camera apps don't return thumbnail data)
    var cameraImageUri by remember { mutableStateOf<Uri?>(null) }
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            cameraImageUri?.let { uri ->
                try {
                    val inputStream = context.contentResolver.openInputStream(uri)
                    val bitmap = BitmapFactory.decodeStream(inputStream)
                    inputStream?.close()
                    if (bitmap != null) {
                        viewModel.onPhotoTaken(bitmap)
                    }
                } catch (_: Exception) { }
            }
        }
    }

    // Gallery launcher for OCR photo pick
    val ocrGalleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            try {
                val inputStream = context.contentResolver.openInputStream(it)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                inputStream?.close()
                if (bitmap != null) {
                    viewModel.onPhotoTaken(bitmap)
                }
            } catch (e: Exception) {
                // Image load failed, ignore
            }
        }
    }

    // Image picker for image import tab
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            try {
                val inputStream = context.contentResolver.openInputStream(it)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                inputStream?.close()
                if (bitmap != null) {
                    viewModel.onImageSelected(bitmap)
                }
            } catch (e: Exception) {
                // Image load failed, ignore
            }
        }
    }

    // Show snackbar on save success
    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            snackbarHostState.showSnackbar("保存成功!")
            viewModel.clearMessages()
        }
    }

    // Show error dialog for save errors
    if (uiState.saveError != null) {
        ErrorDialog(
            message = uiState.saveError!!,
            onDismiss = { viewModel.clearMessages() }
        )
    }

    val tabs = listOf(
        Triple("文本输入", Icons.Default.Edit, 0),
        Triple("拍照识别", Icons.Default.Camera, 1),
        Triple("AI搜索", Icons.Default.SmartToy, 2),
        Triple("图片导入", Icons.Default.AddPhotoAlternate, 3)
    )

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Tab Row
            TabRow(
                selectedTabIndex = uiState.selectedTab,
                modifier = Modifier.fillMaxWidth()
            ) {
                tabs.forEach { (label, icon, index) ->
                    Tab(
                        selected = uiState.selectedTab == index,
                        onClick = { viewModel.selectTab(index) },
                        text = { Text(label, style = MaterialTheme.typography.labelSmall) },
                        icon = { Icon(icon, contentDescription = label) }
                    )
                }
            }

            // Tab Content
            when (uiState.selectedTab) {
                0 -> TextInputSection(
                    title = uiState.textTitle,
                    content = uiState.textContent,
                    isSaving = uiState.isSaving,
                    onTitleChange = { viewModel.updateTitle(it) },
                    onContentChange = { viewModel.updateContent(it) },
                    onSave = { viewModel.saveTextCard() },
                    modifier = Modifier.fillMaxSize()
                )
                1 -> PhotoOcrSection(
                    bitmap = uiState.capturedBitmap,
                    ocrTitle = uiState.ocrTitle,
                    ocrContent = uiState.ocrContent,
                    isProcessing = uiState.isOcrProcessing,
                    isSaving = uiState.isSaving,
                    error = uiState.ocrError,
                    onTakePhoto = {
                        val dir = File(context.cacheDir, "camera_photos")
                        if (!dir.exists()) dir.mkdirs()
                        val file = File(dir, "ocr_${System.currentTimeMillis()}.jpg")
                        val uri = FileProvider.getUriForFile(
                            context,
                            "${context.packageName}.fileprovider",
                            file
                        )
                        cameraImageUri = uri
                        cameraLauncher.launch(uri)
                    },
                    onPickFromGallery = { ocrGalleryLauncher.launch("image/*") },
                    onTitleChange = { viewModel.updateOcrTitle(it) },
                    onContentChange = { viewModel.updateOcrContent(it) },
                    onRetake = { viewModel.retakePhoto() },
                    onSave = { viewModel.saveOcrCard() },
                    modifier = Modifier.fillMaxSize()
                )
                2 -> AiSearchSection(
                    searchQuery = uiState.searchQuery,
                    isSearching = uiState.isAiSearching,
                    generatedCards = uiState.generatedCards,
                    selectedIndices = uiState.selectedCardIndices,
                    isSaving = uiState.isSaving,
                    error = uiState.aiError,
                    onQueryChange = { viewModel.updateSearchQuery(it) },
                    onSearch = { viewModel.performAiSearch() },
                    onToggleCard = { viewModel.toggleCardSelection(it) },
                    onSaveSelected = { viewModel.saveSelectedCards() },
                    modifier = Modifier.fillMaxSize()
                )
                3 -> ImageImportSection(
                    bitmap = uiState.imageBitmap,
                    isSaving = uiState.isSaving,
                    onPickImage = { imagePickerLauncher.launch("image/*") },
                    onClear = { viewModel.clearImage() },
                    onSave = { viewModel.saveImageCard() },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
