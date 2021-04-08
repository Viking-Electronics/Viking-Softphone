package com.vikingelectronics.softphone.activity.list

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltNavGraphViewModel
import androidx.navigation.NavController
import com.vikingelectronics.softphone.R
import com.vikingelectronics.softphone.activity.ActivityEntry
import com.vikingelectronics.softphone.activity.ActivityEntryCard

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ActivityList(
    navController: NavController,
    shouldShowToolbarActions: MutableState<Boolean>,
): @Composable (RowScope.() -> Unit) {

    val selectedEntries by remember { mutableStateOf(mutableListOf<ActivityEntry>()) }

    val viewModel: ActivityListViewModel = hiltNavGraphViewModel()
    val toolbarActions: @Composable RowScope.() -> Unit = {
        Button(onClick = {
//            selectedEntries.forEach { viewModel }
        }) {
            Icon(
                imageVector = Icons.Default.Favorite,
                contentDescription = "Favorite Icon"
            )
        }

    }


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
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }

            items(activity) { item: ActivityEntry ->
                val selectedState = mutableStateOf(false)

                ActivityEntryCard(entry = item, navController, selectedState) {
                    if (selectedEntries.contains(item)) {
                        selectedEntries.remove(item)
                        selectedState.value = false
                    } else {
                        selectedEntries.add(item)
                        selectedState.value = true
                    }
                    shouldShowToolbarActions.value = selectedEntries.isNotEmpty()
                }
            }
        }
    }

    return toolbarActions
}