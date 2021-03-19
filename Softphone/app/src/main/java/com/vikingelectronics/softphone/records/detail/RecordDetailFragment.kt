package com.vikingelectronics.softphone.records.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import com.vikingelectronics.softphone.R
import com.vikingelectronics.softphone.records.Record
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RecordDetailFragment: Fragment(R.layout.fragment_generic_compose) {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return super.onCreateView(inflater, container, savedInstanceState)?.apply {
            findViewById<ComposeView>(R.id.composeView).setContent {

            }
        }
    }
}

@Composable
fun RecordDetailScreen(
    record: Record
) {

}