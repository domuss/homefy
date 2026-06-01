package com.domus.homefy.ui.house

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.AdminPanelSettings
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
fun ManageHouseMembersScreen(
    navController: NavController,
    houseId: Long,
    padding: PaddingValues,
    authViewModel: AuthViewModel = koinViewModel(),
    houseViewModel: HouseViewModel = koinViewModel()
) {
    val adminState by authViewModel.isAdmin.collectAsState()
    val members by houseViewModel.houseMembersState.collectAsState()
    val uiStatus = houseViewModel.uiStatus
    var memberToPromote by remember { mutableStateOf<HouseMemberFull?>(null) }
    var memberToRemove by remember { mutableStateOf<HouseMemberFull?>(null) }

    LaunchedEffect(houseId) {
        authViewModel.checkAdmin(houseId)
        houseViewModel.loadHouseMembers(houseId)
    }

    if (adminState is AdminState.Loading) {
        LoadingScreen()
        return
    }

    val isAdmin = (adminState as AdminState.IsAdmin).admin

    LaunchedEffect(isAdmin) {
        if (!isAdmin) {
            navController.popBackStack()
        }
    }

    if (!isAdmin) {
        LoadingScreen()
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFAFAFA))
            .padding(padding)
            .padding(horizontal = 18.dp, vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Membros",
            fontSize = 26.sp,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        if (uiStatus is HouseUIStatus.Error) {
            Text(
                uiStatus.message,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 12.dp),
                textAlign = TextAlign.Center
            )
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(members, key = { it.id }) { member ->
                ManageMemberRow(
                    member = member,
                    actionsEnabled = isAdmin && uiStatus != HouseUIStatus.Loading,
                    onGiveAdmin = {
                        memberToPromote = member
                    },
                    onDelete = {
                        memberToRemove = member
                    }
                )
            }
        }
    }

    memberToPromote?.let { member ->
        val name = member.displayName()

        AlertDialog(
            onDismissRequest = { memberToPromote = null },
            title = { Text("Dar admin") },
            text = { Text("Deseja tornar $name administrador desta casa?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        houseViewModel.giveAdmin(houseId, member.id)
                        memberToPromote = null
                    }
                ) {
                    Text("Confirmar")
                }
            },
            dismissButton = {
                TextButton(onClick = { memberToPromote = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    memberToRemove?.let { member ->
        val name = member.displayName()

        AlertDialog(
            onDismissRequest = { memberToRemove = null },
            title = { Text("Remover membro") },
            text = { Text("Deseja remover $name desta casa?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        houseViewModel.removeMember(houseId, member.id)
                        memberToRemove = null
                    }
                ) {
                    Text("Remover", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { memberToRemove = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun ManageMemberRow(
    member: HouseMemberFull,
    actionsEnabled: Boolean,
    onGiveAdmin: () -> Unit,
    onDelete: () -> Unit
) {
    val name = member.displayName()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFFD3D3D3), RoundedCornerShape(8.dp))
            .background(Color.White, RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Outlined.AccountCircle,
            contentDescription = null,
            tint = Color(0xFF777777),
            modifier = Modifier.size(32.dp)
        )

        Spacer(Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                name,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
            RoleChip(member.role)
        }

        IconButton(
            onClick = onGiveAdmin,
            enabled = actionsEnabled && member.role != Role.HOUSE_ADMIN
        ) {
            Icon(
                Icons.Outlined.AdminPanelSettings,
                contentDescription = "Dar admin",
                tint = MaterialTheme.colorScheme.primary
            )
        }

        IconButton(
            onClick = onDelete,
            enabled = actionsEnabled
        ) {
            Icon(
                Icons.Outlined.Delete,
                contentDescription = "Remover membro",
                tint = MaterialTheme.colorScheme.error
            )
        }
    }
}

private fun HouseMemberFull.displayName(): String {
    return user.name ?: user.username ?: "Usuário"
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
        shape = RoundedCornerShape(50),
        modifier = Modifier.padding(top = 4.dp)
    ) {
        Text(
            label,
            fontSize = 12.sp,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
        )
    }
}
