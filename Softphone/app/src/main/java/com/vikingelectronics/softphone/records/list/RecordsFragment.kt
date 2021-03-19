package com.vikingelectronics.softphone.records.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.vikingelectronics.softphone.R
import com.vikingelectronics.softphone.records.RecordCard
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RecordsFragment: Fragment(R.layout.fragment_generic_compose) {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return super.onCreateView(inflater, container, savedInstanceState)?.apply {
            findViewById<ComposeView>(R.id.composeView).setContent {
                MaterialTheme {
                    RecordsScreen(findNavController())
                }
            }
        }
    }
}

@Composable
fun RecordsScreen(
    navController: NavController
) {

    val viewModel: RecordsViewModel = viewModel()

    LazyColumn (
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {

        items(viewModel.externalRecords) {
            RecordCard(record = it, navController)
        }

    }
}

