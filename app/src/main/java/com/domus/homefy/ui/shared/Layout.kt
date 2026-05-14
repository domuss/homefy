package com.domus.homefy.ui.shared

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.CheckBox
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Layout(
    title: String = "Homefy",
    snackbarHost: @Composable (() -> Unit) = {},
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        snackbarHost = snackbarHost,
//        modifier = Modifier.statusBarsPadding(),
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                title = { },
                navigationIcon = {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 12.dp),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(color = Color(0xFF6650A4))
                        ) {
                            IconButton(
                                onClick = {}) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(30.dp)
                                )
                            }
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = {},
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsNone, contentDescription = null
                        )
                    }
                })
        }, bottomBar = {
            BottomAppBar(modifier = Modifier.height(64.dp)) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    BarButton(
                        icon = Icons.Outlined.CheckBox
                    )

                    BarButton(
                        icon = Icons.Outlined.Home
                    )

                    BarButton(
                        icon = Icons.Default.AttachMoney,
                    )
                }
            }
        }, floatingActionButton = {
            FloatingActionButton(
                onClick = {},
                containerColor = Color(0xFF6650A4),
                modifier = Modifier
                    .size(72.dp),
                shape = CircleShape
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
        }) { padding ->
        content(padding)
    }
}

@Composable
fun BarButton(route: String? = null, icon: ImageVector) {
    IconButton(onClick = {}) {
        Icon(
            imageVector = icon, contentDescription = null
        )
    }
}