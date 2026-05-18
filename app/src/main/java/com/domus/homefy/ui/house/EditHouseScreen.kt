package com.domus.homefy.ui.house

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import org.koin.androidx.compose.koinViewModel
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditHouseScreen(
    navController: NavController,
    houseId: Long,
    currentName: String,
    initialAccessCode: String,
    initialIsCodeActive: Boolean,
    houseViewModel: HouseViewModel = koinViewModel()
) {
    var houseName by remember { mutableStateOf(currentName) }
    var isCodeActive by remember { mutableStateOf(initialIsCodeActive) }

    val uiStatus = houseViewModel.uiStatus
    val members = houseViewModel.houseMembers

    LaunchedEffect(houseId) {
        houseViewModel.loadHouseMembers(houseId)
    }


    Scaffold(
        topBar = {
            MediumTopAppBar(
                title = { Text("Gerenciar Casa", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar"
                        )
                    }
                },
                colors = TopAppBarDefaults.mediumTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

          
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Informações Gerais",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = houseName,
                        onValueChange = { houseName = it },
                        label = { Text("Nome da Casa") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            }


            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Acesso de Convidados",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Código de Convite", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = initialAccessCode,
                                fontSize = 26.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 2.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }


                        SuggestionChip(
                            onClick = { },
                            label = { Text(if (isCodeActive) "Ativo" else "Bloqueado") }
                        )
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.surfaceVariant)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Permitir novos membros", fontWeight = FontWeight.Medium, fontSize = 15.sp)
                            Text("Se desativado, ninguém conseguirá entrar com o código.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = isCodeActive,
                            onCheckedChange = { novoStatus ->
                                isCodeActive = novoStatus
                                houseViewModel.toggleCodeStatus(houseId, novoStatus)
                            }
                        )
                    }
                }
            }


            Text(
                text = "Moradores Vinculados (${members.size})",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 4.dp, top = 8.dp)
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
            ) {
                if (members.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Nenhum morador além de você.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        items(members) { member ->
                            ListItem(
                                headlineContent = { Text(member.name, fontWeight = FontWeight.Medium) },
                                supportingContent = { Text("Morador", fontSize = 12.sp) },
                                leadingContent = {

                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .background(MaterialTheme.colorScheme.primaryContainer, shape = CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Person,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }
                                },
                                trailingContent = {
                                    IconButton(
                                        onClick = { houseViewModel.removeMemberFromHouse(houseId, member.id) }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Remover Membro",
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    }
                                },
                                colors = ListItemDefaults.colors(containerColor = androidx.compose.ui.graphics.Color.Transparent)
                            )
                        }
                    }
                }
            }


            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        houseViewModel.deleteHouse(houseId) {
                            navController.popBackStack()
                        }
                    },
                    enabled = uiStatus != HouseUIStatus.Loading,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Excluir Casa")
                }

                Button(
                    onClick = { houseViewModel.updateHouse(houseId, houseName) },
                    enabled = uiStatus != HouseUIStatus.Loading,
                    modifier = Modifier.weight(1.5f)
                ) {
                    Text("Salvar Alterações")
                }
            }


            if (uiStatus is HouseUIStatus.Error) {
                Text(
                    text = uiStatus.message,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 14.sp,
                    modifier = Modifier.align(Alignment.CenterHorizontally).padding(bottom = 8.dp)
                )
            }


            if (uiStatus is HouseUIStatus.Sucesso) {
                LaunchedEffect(Unit) {
                    navController.popBackStack()
                }
            }
        }
    }
}