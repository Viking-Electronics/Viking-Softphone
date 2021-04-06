package com.vikingelectronics.softphone.activity.detail


import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltNavGraphViewModel
import com.vikingelectronics.softphone.activity.ActivityEntry
import dev.chrisbanes.accompanist.coil.CoilImage

@Composable
fun ActivityDetailScreen(
    entry: ActivityEntry
) {

    val viewModel: ActivityDetailViewModel = hiltNavGraphViewModel()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        
        Text(
            text = entry.timestamp.toDate().toString(),
            style = MaterialTheme.typography.h5
        )
        
        CoilImage(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            data = entry.snapshotUrl,
            contentDescription = "Image from ${entry.description}, at ${entry.timestamp.toDate()}",
            contentScale = ContentScale.Inside,
        )

        Text(
            text = entry.description,
            style = MaterialTheme.typography.body1,
            modifier = Modifier.padding(top = 16.dp)
        )
    }
}