package com.domus.homefy.ui.shared

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.House
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.NoAccounts
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.CheckBox
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.domus.homefy.ui.auth.AuthViewModel
import com.domus.homefy.ui.profile.ProfileViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Layout(
    snackbarHost: @Composable (() -> Unit) = {},
    navController: NavController,
    profileViewModel: ProfileViewModel = koinViewModel(),
    authViewModel: AuthViewModel = koinViewModel(),
    content: @Composable (PaddingValues) -> Unit
) {
    var expanded by remember {
        mutableStateOf(false)
    }
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(horizontal = 16.dp)
                        .verticalScroll(
                            rememberScrollState()
                        )
                ) {
                    Text(
                        profileViewModel.currentPublicUser?.name ?: "Usuário não identificado",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.titleLarge
                    )

                    HorizontalDivider()
                    Text("Conta", modifier = Modifier.padding(12.dp), style = MaterialTheme.typography.titleMedium)

                    NavigationDrawerItem(
                        onClick = {
                            navController.navigate("edit-profile")
                            scope.launch {
                                drawerState.close()
                            }
                        },
                        label = { Text("Editar perfil") },
                        selected = false,
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(30.dp)
                            )
                        })
                    NavigationDrawerItem(
                        onClick = {
                            authViewModel.logout()
                        },
                        label = { Text("Sair") },
                        selected = false,
                        icon = {
                            Icon(
                                imageVector = Icons.Default.NoAccounts,
                                contentDescription = null,
                                tint = Color.Red,
                                modifier = Modifier.size(30.dp)
                            )
                        })

                    HorizontalDivider()

                    NavigationDrawerItem(
                        onClick = {
                            scope.launch {
                                drawerState.close()
                            }
                        },
                        label = { Text("Fechar") },
                        selected = false,
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(30.dp)
                            )
                        })


                }
            }
        }
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
                                    onClick = {
                                        scope.launch {
                                            if (drawerState.isOpen) {
                                                drawerState.close()
                                            } else {
                                                drawerState.open()
                                            }
                                        }
                                    }) {
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
                                imageVector = Icons.Default.NotificationsNone,
                                contentDescription = null
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
                            icon = Icons.Outlined.CheckBox,
                            route = "tasks",
                            navController = navController
                        )

                        BarButton(
                            icon = Icons.Outlined.Home,
                            route = "home",
                            navController = navController
                        )

                        BarButton(
                            icon = Icons.Default.AttachMoney,
                            route = "bills",
                            navController = navController
                        )
                    }
                }
            }, floatingActionButton = {
                Box {
                    FloatingActionButton(
                        onClick = { expanded = !expanded },
                        containerColor = Color(0xFF6650A4),
                        modifier = Modifier.size(72.dp),
                        shape = CircleShape
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = !expanded },
                        offset = DpOffset(x = 0.dp, y = (-8).dp)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Criar casa") },
                            leadingIcon = { Icon(Icons.Default.House, contentDescription = null) },
                            onClick = {
                                navController.navigate("create-house")
                                expanded = !expanded
                            })

                        DropdownMenuItem(
                            text = { Text("Criar tarefa") },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.CheckBox,
                                    contentDescription = null
                                )
                            },
                            onClick = {
                                navController.navigate("create-task")
                                expanded = !expanded
                            })

                        DropdownMenuItem(
                            text = { Text("Criar conta") },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.MonetizationOn,
                                    contentDescription = null
                                )
                            },
                            onClick = {
                                navController.navigate("create-bill")
                                expanded = !expanded
                            })
                    }
                }
            }) { padding ->
            content(padding)
        }
    }
}

@Composable
fun BarButton(route: String, icon: ImageVector, navController: NavController) {
    IconButton(onClick = { navController.navigate(route) }) {
        Icon(
            imageVector = icon, contentDescription = null
        )
    }
}