package com.vikingelectronics.softphone.captures.list


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltNavGraphViewModel
import androidx.navigation.NavController
import com.vikingelectronics.softphone.captures.RecordCard

@Composable
fun CapturesList(
    navController: NavController
) {

    val viewModel: CapturesListViewModel = hiltNavGraphViewModel()

    LazyColumn (
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {

        items(viewModel.capturesList) {
            RecordCard(capture = it, navController)
        }
    }
}