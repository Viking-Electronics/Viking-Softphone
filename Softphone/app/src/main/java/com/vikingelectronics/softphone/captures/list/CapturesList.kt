package com.vikingelectronics.softphone.captures.list


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltNavGraphViewModel
import androidx.navigation.NavController
import com.vikingelectronics.softphone.captures.Capture
import com.vikingelectronics.softphone.captures.RecordCard

@Composable
fun CapturesList(
    navController: NavController,
    shouldShowToolbarActions: MutableState<Boolean>
): @Composable RowScope.() -> Unit {

    val viewModel: CapturesListViewModel = hiltNavGraphViewModel()

    val selectedCaptures by remember { mutableStateOf(mutableListOf<Capture>()) }
    val toolbarActions: @Composable RowScope.() -> Unit = {
        Button(onClick = {
            selectedCaptures.forEach { viewModel }
        }) {
            Icon(imageVector = Icons.Default.Download, contentDescription = "Download Icon")
        }
        Button(onClick = { /*TODO*/ }) {
            Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete Icon")
        }
    }

    LazyColumn (
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {

        items(viewModel.capturesList) { capture ->
            val selectedState = mutableStateOf(false)

            RecordCard(capture = capture, navController, selectedState) {
                if (selectedCaptures.contains(capture)) {
                    selectedCaptures.remove(capture)
                    selectedState.value = false
                } else {
                    selectedCaptures.add(capture)
                    selectedState.value = true
                }
                shouldShowToolbarActions.value = selectedCaptures.isNotEmpty()
            }
        }
    }

    return toolbarActions
}