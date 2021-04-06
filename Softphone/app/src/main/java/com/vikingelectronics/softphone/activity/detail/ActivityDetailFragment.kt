package com.vikingelectronics.softphone.activity.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.hilt.navigation.compose.hiltNavGraphViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.vikingelectronics.softphone.R
import com.vikingelectronics.softphone.activity.ActivityEntry
import dagger.hilt.android.AndroidEntryPoint
import dev.chrisbanes.accompanist.coil.CoilImage
import dev.chrisbanes.accompanist.coil.CoilImageDefaults

@AndroidEntryPoint
class ActivityDetailFragment: Fragment(R.layout.fragment_generic_compose) {

//    private val args: ActivityDetailFragmentArgs by navArgs()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return super.onCreateView(inflater, container, savedInstanceState)?.apply {
            findViewById<ComposeView>(R.id.composeView).setContent {
                MaterialTheme {
//                    ActivityDetailScreen(entry = args.activityEntry)
                }
            }
        }
    }
}

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