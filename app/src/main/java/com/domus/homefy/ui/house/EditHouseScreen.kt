package com.domus.homefy.ui.house

import android.content.ClipData
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.domus.homefy.data.HouseMemberFull
import com.domus.homefy.data.Role
import com.domus.homefy.ui.auth.AdminState
import com.domus.homefy.ui.auth.AuthViewModel
import com.domus.homefy.ui.shared.LoadingScreen
import org.koin.androidx.compose.koinViewModel

@Composable
fun EditHouseScreen(
    navController: NavController,
    houseId: Long,
    currentName: String,
    initialAccessCode: String,
    initialIsCodeActive: Boolean,
    padding: PaddingValues,
    authViewModel: AuthViewModel = koinViewModel(),
    houseViewModel: HouseViewModel = koinViewModel()
) {
    val adminState by authViewModel.isAdmin.collectAsState()

    LaunchedEffect(houseId) {
        authViewModel.checkAdmin(houseId)
        houseViewModel.loadHouseMembers(houseId)
    }

    val loading = adminState is AdminState.Loading

    if (loading) {
        LoadingScreen()
    } else {
        EditHouseScreenContent(
            navController = navController,
            houseId = houseId,
            currentName = currentName,
            initialAccessCode = initialAccessCode,
            initialIsCodeActive = initialIsCodeActive,
            isAdmin = (adminState as AdminState.IsAdmin).admin,
            padding = padding
        )
    }

}

@Composable
fun EditHouseScreenContent(
    navController: NavController,
    houseId: Long,
    currentName: String,
    initialAccessCode: String,
    initialIsCodeActive: Boolean,
    isAdmin: Boolean,
    padding: PaddingValues,
    houseViewModel: HouseViewModel = koinViewModel()
) {
    var isCodeActive by remember { mutableStateOf(initialIsCodeActive) }
    var houseName by remember(currentName) { mutableStateOf(currentName) }
    var displayedHouseName by remember(currentName) { mutableStateOf(currentName) }
    var pendingHouseName by remember { mutableStateOf<String?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    val houseMembers by houseViewModel.houseMembersState.collectAsState()
    val uiStatus = houseViewModel.uiStatus
    val actionsEnabled = isAdmin && uiStatus != HouseUIStatus.Loading

    LaunchedEffect(uiStatus) {
        if (uiStatus is HouseUIStatus.Sucesso) {
            pendingHouseName?.let { savedName ->
                displayedHouseName = savedName
                houseName = savedName
                pendingHouseName = null
            }
        } else if (uiStatus is HouseUIStatus.Error) {
            pendingHouseName = null
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFAFAFA))
            .padding(padding)
            .verticalScroll(rememberScrollState())
            .padding(start = 18.dp, top = 14.dp, end = 18.dp, bottom = 112.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Seja bem-vindo a ${displayedHouseName.ifBlank { "Casa" }}!",
            fontSize = 26.sp,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(18.dp))

        HouseNameCard(
            houseName = houseName,
            isAdmin = isAdmin,
            actionsEnabled = actionsEnabled,
            onNameChange = { houseName = it },
            onSave = {
                val trimmedName = houseName.trim()
                pendingHouseName = trimmedName
                houseViewModel.updateHouse(houseId, trimmedName)
            },
            onDelete = { showDeleteDialog = true }
        )

        Spacer(Modifier.height(18.dp))

        HouseMembersCard(
            members = houseMembers,
            canManageMembers = isAdmin,
            onManageMembers = {
                navController.navigate("house-members/$houseId") {
                    launchSingleTop = true
                }
            }
        )

        Spacer(Modifier.height(18.dp))

        InviteCodeCard(
            accessCode = initialAccessCode,
            isCodeActive = isCodeActive,
            isAdmin = isAdmin,
            onCodeStatusChange = { newStatus ->
                isCodeActive = newStatus
                houseViewModel.toggleCodeStatus(houseId, newStatus)
            }
        )

        Spacer(Modifier.height(18.dp))

        if (!isAdmin) {
            Text(
                "Somente administradores podem editar a casa.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }

        if (uiStatus is HouseUIStatus.Error) {
            Spacer(Modifier.height(12.dp))
            Text(
                uiStatus.message,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Excluir casa") },
            text = { Text("Tem certeza que deseja excluir ${displayedHouseName.ifBlank { "esta casa" }}?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        houseViewModel.deleteHouse(houseId) {
                            navController.popBackStack()
                        }
                    },
                    enabled = actionsEnabled
                ) {
                    Text("Excluir", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun HouseNameCard(
    houseName: String,
    isAdmin: Boolean,
    actionsEnabled: Boolean,
    onNameChange: (String) -> Unit,
    onSave: () -> Unit,
    onDelete: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = Color(0xFFD3D3D3),
                shape = RoundedCornerShape(8.dp),
            )
            .background(Color.White, RoundedCornerShape(8.dp))
            .padding(14.dp)
    ) {
        Text(
            "Nome da casa",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(10.dp))

        OutlinedTextField(
            value = houseName,
            onValueChange = onNameChange,
            enabled = isAdmin,
            singleLine = true,
            label = { Text("Nome") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = onSave,
                enabled = actionsEnabled && houseName.isNotBlank(),
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(
                    Icons.Outlined.Save,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text("Salvar")
            }

            OutlinedButton(
                onClick = onDelete,
                enabled = actionsEnabled,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Icon(
                    Icons.Outlined.Delete,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text("Excluir")
            }
        }
    }
}

@Composable
private fun HouseMembersCard(
    members: List<HouseMemberFull>,
    canManageMembers: Boolean,
    onManageMembers: () -> Unit
) {
    val visibleMembers = members.take(3)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = Color(0xFFD3D3D3),
                shape = RoundedCornerShape(8.dp),
            )
            .background(Color.White, RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "Membros da casa",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            AssistChip(
                onClick = {},
                enabled = false,
                label = { Text(members.size.toString()) },
                leadingIcon = {
                    Icon(
                        Icons.Outlined.Group,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            )
        }

        Spacer(Modifier.height(8.dp))

        if (members.isEmpty()) {
            Text(
                "Não há membros",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 12.dp)
            )
        } else {
            visibleMembers.forEach { member ->
                MemberRow(member)
            }

            if (members.size > visibleMembers.size) {
                Text(
                    "...",
                    modifier = Modifier.padding(start = 8.dp, top = 2.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (canManageMembers) {
            Spacer(Modifier.height(8.dp))

            OutlinedButton(
                onClick = onManageMembers,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .height(36.dp),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp)
            ) {
                Text("Gerenciar membros")
            }
        }
    }
}

@Composable
private fun InviteCodeCard(
    accessCode: String,
    isCodeActive: Boolean,
    isAdmin: Boolean,
    onCodeStatusChange: (Boolean) -> Unit
) {
    val context = LocalContext.current
    var copied by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 136.dp)
            .border(
                width = 1.dp,
                color = Color(0xFFD3D3D3),
                shape = RoundedCornerShape(8.dp),
            )
            .background(Color.White, RoundedCornerShape(8.dp))
            .padding(14.dp)
    ) {
        Text(
            "Código de convite",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                accessCode.ifBlank { "------" },
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 2.sp
            )

            IconButton(
                onClick = {
                    val clipboardManager = context.getSystemService(
                        Context.CLIPBOARD_SERVICE
                    ) as android.content.ClipboardManager
                    clipboardManager.setPrimaryClip(
                        ClipData.newPlainText("Código de convite", accessCode)
                    )
                    copied = true
                },
                enabled = accessCode.isNotBlank()
            ) {
                Icon(
                    Icons.Outlined.ContentCopy,
                    contentDescription = "Copiar código",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (copied) {
            Text(
                "Código copiado",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 2.dp)
            )
        }

        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(if (isCodeActive) "Convites ativos" else "Convites bloqueados")
            Switch(
                checked = isCodeActive,
                enabled = isAdmin,
                onCheckedChange = onCodeStatusChange
            )
        }
    }
}

@Composable
private fun MemberRow(member: HouseMemberFull) {
    val name = member.user.name ?: member.user.username ?: "Usuário"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .border(1.dp, Color(0xFFD3D3D3), RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Outlined.AccountCircle,
            contentDescription = null,
            tint = Color(0xFF777777),
            modifier = Modifier.size(30.dp)
        )

        Spacer(Modifier.width(10.dp))

        Text(
            name,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f)
        )

        RoleChip(member.role)
    }
}

@Composable
private fun RoleChip(role: Role) {
    val label = when (role) {
        Role.HOUSE_ADMIN -> "Admin"
        Role.RESIDENT -> "Morador"
    }

    Surface(
        color = Color.White,
        contentColor = Color(0xFF444444),
        border = BorderStroke(1.dp, Color(0xFFD3D3D3)),
        shape = RoundedCornerShape(50)
    ) {
        Text(
            label,
            fontSize = 12.sp,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
        )
    }
}

//@Composable
//fun OldScreen() {
//
//    Column(
//        horizontalAlignment = Alignment.CenterHorizontally,
//        verticalArrangement = Arrangement.Center,
//        modifier = Modifier
//            .fillMaxSize()
//            .padding(16.dp)
//    ) {
//        Text("Editar Casa", fontSize = 28.sp)
//
//        Spacer(modifier = Modifier.height(24.dp))
//
//        OutlinedTextField(
//            value = houseName,
//            onValueChange = { houseName = it },
//            label = { Text("Nome da Casa") },
//            modifier = Modifier.fillMaxWidth()
//        )
//
//        Spacer(modifier = Modifier.height(16.dp))
//
//        Card(
//            modifier = Modifier.fillMaxWidth(),
//            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
//        ) {
//            Column(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(16.dp)
//            ) {
//                Text(
//                    "Código de Convite",
//                    fontSize = 14.sp,
//                    color = MaterialTheme.colorScheme.onSurfaceVariant
//                )
//
//
//                Text(
//                    initialAccessCode,
//                    fontSize = 24.sp,
//                    fontWeight = FontWeight.Bold,
//                    letterSpacing = 2.sp
//                )
//
//                Spacer(modifier = Modifier.height(8.dp))
//
//                Row(
//                    modifier = Modifier.fillMaxWidth(),
//                    verticalAlignment = Alignment.CenterVertically,
//                    horizontalArrangement = Arrangement.SpaceBetween
//                ) {
//                    Text(
//                        if (isCodeActive) "Convites Ativos" else "Convites Bloqueados",
//                        fontSize = 16.sp
//                    )
//                    Switch(
//                        checked = isCodeActive,
//                        onCheckedChange = { novoStatus ->
//                            isCodeActive = novoStatus
//                            houseViewModel.toggleCodeStatus(houseId, novoStatus)
//                        }
//                    )
//                }
//            }
//        }
//
//        Spacer(modifier = Modifier.height(24.dp))
//
//
//        Button(
//            onClick = {
//                houseViewModel.updateHouse(houseId, houseName)
//            },
//            enabled = uiStatus != HouseUIStatus.Loading,
//            modifier = Modifier.fillMaxWidth()
//        ) {
//            Text("Salvar")
//        }
//
//        Spacer(modifier = Modifier.height(8.dp))
//
//        OutlinedButton(
//            onClick = {
//
//                houseViewModel.deleteHouse(houseId) {
//                    navController.popBackStack()
//                }
//            },
//            enabled = uiStatus != HouseUIStatus.Loading,
//            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
//            modifier = Modifier.fillMaxWidth()
//        ) {
//            Text("Excluir Casa")
//        }
//
//        when (uiStatus) {
//            is HouseUIStatus.Sucesso -> {
//                LaunchedEffect(Unit) {
//                    navController.popBackStack()
//                }
//            }
//
//            is HouseUIStatus.Error -> {
//                Text(uiStatus.message)
//            }
//
//            else -> {}
//        }
//    }
//}
