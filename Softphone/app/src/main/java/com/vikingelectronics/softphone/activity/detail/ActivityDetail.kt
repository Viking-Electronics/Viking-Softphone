package com.vikingelectronics.softphone.activity.detail


import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.coil.rememberCoilPainter
import com.vikingelectronics.softphone.activity.ActivityEntry

@Composable
fun ActivityDetail(
    entry: ActivityEntry
) {

    val viewModel: ActivityDetailViewModel = hiltViewModel()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        
        Text(
            text = entry.timestamp.toDate().toString(),
            style = MaterialTheme.typography.h5
        )
        
        Image(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            painter = rememberCoilPainter(request = entry.snapshotUrl),
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