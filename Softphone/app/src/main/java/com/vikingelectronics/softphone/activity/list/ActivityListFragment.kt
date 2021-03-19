package com.vikingelectronics.softphone.activity.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.vikingelectronics.softphone.R
import com.vikingelectronics.softphone.activity.ActivityEntry
import com.vikingelectronics.softphone.activity.ActivityEntryCard
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ActivityListFragment: Fragment(R.layout.fragment_generic_compose) {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return super.onCreateView(inflater, container, savedInstanceState)?.apply {
            findViewById<ComposeView>(R.id.composeView).setContent {
                MaterialTheme {
                    ActivityListScreen(findNavController())
                }
            }
        }
    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ActivityListScreen(navController: NavController) {

    val viewModel: ActivityListViewModel = viewModel()

    LazyColumn {
//        item {
//            Button(onClick = viewModel::generateActivityEntries) {
//                Text(text = "Generate")
//            }
//        }

        viewModel.groupedEntries.forEach { (deviceName, activity) ->

            stickyHeader{
                Row(
                    modifier = Modifier.fillMaxWidth().background(colorResource(id = R.color.light_grey_color))
                ) {
                    Text(
                        text = deviceName,
                        modifier = Modifier
                            .padding(start = 8.dp)
                    )
                }
            }

            items(activity) { item: ActivityEntry ->
                ActivityEntryCard(
                    entry = item,
                    modifier = Modifier.clickable { viewModel.navigateToDetail(navController, item) }
                )
            }
        }
    }
}