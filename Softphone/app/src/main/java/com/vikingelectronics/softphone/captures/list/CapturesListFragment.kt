package com.vikingelectronics.softphone.captures.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.AlertDialog
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.hilt.navigation.compose.hiltNavGraphViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.vikingelectronics.softphone.R
import com.vikingelectronics.softphone.captures.Capture
import com.vikingelectronics.softphone.captures.RecordCard
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CapturesListFragment: Fragment(R.layout.fragment_generic_compose) {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return super.onCreateView(inflater, container, savedInstanceState)?.apply {
            findViewById<ComposeView>(R.id.composeView).setContent {
                MaterialTheme {
                    CapturesListScreen(findNavController())
                }
            }
        }
    }
}

@Composable
fun CapturesListScreen(
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