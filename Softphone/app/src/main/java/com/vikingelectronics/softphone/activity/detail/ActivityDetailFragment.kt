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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.fragment.navArgs
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.vikingelectronics.softphone.R
import com.vikingelectronics.softphone.activity.ActivityEntry
import dagger.hilt.android.AndroidEntryPoint
import dev.chrisbanes.accompanist.coil.CoilImage
import dev.chrisbanes.accompanist.coil.CoilImageDefaults

@AndroidEntryPoint
class ActivityDetailFragment: Fragment(R.layout.fragment_generic_compose) {

    private val args: ActivityDetailFragmentArgs by navArgs()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return super.onCreateView(inflater, container, savedInstanceState)?.apply {
            findViewById<ComposeView>(R.id.composeView).setContent {
                MaterialTheme {
                    ActivityDetailScreen(entry = args.activityEntry)
                }
            }
        }
    }
}

@Composable
fun ActivityDetailScreen(
    entry: ActivityEntry
) {

    val viewModel: ActivityDetailViewModel = viewModel()
    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {

        item {
            CoilImage(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp),
                data = entry.snapshotUrl,
                contentDescription = "Image from ${entry.description}, at ${entry.timestamp.toDate()}",
                contentScale = ContentScale.Inside,
            )
        }

        items(viewModel.storageFileDLRefs) {
            Text(text = it)
        }


//        LazyColumn(
//            modifier = Modifier.fillMaxHeight()
//        ) {
//
//            viewModel.storageFileDLRefs.forEach {
//                item {
//                    Text(text = it)
//                }
//            }
////            items(viewModel.storageFileDLRefs) { ref ->
////                Text(text = ref)
////            }
//        }
    }
}