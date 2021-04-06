package com.vikingelectronics.softphone.activity.list

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltNavGraphViewModel
import androidx.navigation.NavController
import com.vikingelectronics.softphone.R
import com.vikingelectronics.softphone.activity.ActivityEntry
import com.vikingelectronics.softphone.activity.ActivityEntryCard
import com.vikingelectronics.softphone.extensions.setParcelableAndNavigate
import com.vikingelectronics.softphone.navigation.Screen

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ActivityList(navController: NavController) {

    val viewModel: ActivityListViewModel = hiltNavGraphViewModel()

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
                    modifier = Modifier.clickable {
                        navController.setParcelableAndNavigate(Screen.Secondary.ActivityDetail, item)
                    }
                )
            }
        }
    }
}