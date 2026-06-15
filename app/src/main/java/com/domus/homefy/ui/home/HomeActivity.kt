package com.domus.homefy.ui.home

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.domus.homefy.data.House
import com.domus.homefy.ui.auth.AuthViewModel
import com.domus.homefy.ui.house.HouseUIStatus
import com.domus.homefy.ui.house.HouseViewModel
import com.domus.homefy.ui.quote.DailyQuoteCard
import com.domus.homefy.ui.quote.DailyQuoteViewModel
import com.domus.homefy.ui.shared.LoadingScreen
import org.koin.androidx.compose.koinViewModel

@Composable
fun HomeScreen(
    navController: NavController,
    padding: PaddingValues,
    authViewModel: AuthViewModel = koinViewModel(),
    houseViewModel: HouseViewModel = koinViewModel(),
    dailyQuoteViewModel: DailyQuoteViewModel = koinViewModel()
) {
    val houses = houseViewModel.housesList
    var codeInput by remember { mutableStateOf("") }
    var searchInput by remember { mutableStateOf("") }
    val uiStatus = houseViewModel.uiStatus
    val quoteUiStatus = dailyQuoteViewModel.uiStatus
    val filteredHouses = remember(houses, searchInput) {
        val normalizedSearch = searchInput.trim()

        if (normalizedSearch.isBlank()) {
            houses
        } else {
            houses.filter { house ->
                house.name.contains(normalizedSearch, ignoreCase = true) ||
                        house.access_code.orEmpty().contains(normalizedSearch, ignoreCase = true)
            }
        }
    }

    LaunchedEffect(Unit) {
        houseViewModel.loadHouses()
        authViewModel.resetIsAdmin()
        dailyQuoteViewModel.loadDailyQuote()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(16.dp),
        contentPadding = PaddingValues(bottom = 112.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            HomeHeader(
                housesCount = houses.size,
                onCreateHouse = {
                    navController.navigate("create-house") {
                        launchSingleTop = true
                    }
                }
            )
        }

        item {
            DailyQuoteCard(uiStatus = quoteUiStatus)
        }

        item {
            JoinHouseCard(
                codeInput = codeInput,
                onCodeChange = { value ->
                    codeInput = value
                        .filter { it.isLetterOrDigit() }
                        .take(6)
                        .uppercase()
                },
                onJoinHouse = {
                    houseViewModel.joinHouse(codeInput)
                    codeInput = ""
                }
            )
        }

        if (houses.isNotEmpty()) {
            item {
                OutlinedTextField(
                    value = searchInput,
                    onValueChange = { searchInput = it },
                    label = { Text("Buscar casa") },
                    placeholder = { Text("Nome ou codigo") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null
                        )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        when (uiStatus) {
            is HouseUIStatus.Loading -> item {
                LoadingScreen()
            }

            is HouseUIStatus.Error -> item {
                ErrorBanner(message = uiStatus.message)
            }

            is HouseUIStatus.Sucesso -> {
                if (houses.isEmpty()) {
                    item {
                        EmptyHousesState(
                            onCreateHouse = {
                                navController.navigate("create-house") {
                                    launchSingleTop = true
                                }
                            }
                        )
                    }
                } else if (filteredHouses.isEmpty()) {
                    item {
                        EmptySearchState(searchInput = searchInput)
                    }
                } else {
                    item {
                        Text(
                            text = "Suas casas",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    items(
                        items = filteredHouses,
                        key = { house -> house.id ?: house.name }
                    ) { house ->
                        HouseCard(
                            house = house,
                            onClick = {
                                val codigo = house.access_code ?: "VAZIO"
                                val status = house.is_code_active ?: false
                                navController.navigate("edit-house/${house.id}/${house.name}/$codigo/$status")
                            }
                        )
                    }
                }
            }

            else -> {}
        }
    }
}

@Composable
private fun HomeHeader(
    housesCount: Int,
    onCreateHouse: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Minhas Casas",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = if (housesCount == 1) {
                    "1 casa conectada"
                } else {
                    "$housesCount casas conectadas"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        FilledTonalIconButton(onClick = onCreateHouse) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Criar casa"
            )
        }
    }
}

@Composable
private fun JoinHouseCard(
    codeInput: String,
    onCodeChange: (String) -> Unit,
    onJoinHouse: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                IconBadge(
                    icon = Icons.Default.Key,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Entrar por convite",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Use o codigo de 6 caracteres da casa.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = codeInput,
                    onValueChange = onCodeChange,
                    label = { Text("Codigo") },
                    placeholder = { Text("A1B2C3") },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.weight(1f)
                )

                Button(
                    onClick = onJoinHouse,
                    enabled = codeInput.length == 6,
                    shape = RoundedCornerShape(16.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = "Entrar"
                    )
                }
            }
        }
    }
}

@Composable
private fun HouseCard(
    house: House,
    onClick: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            IconBadge(
                icon = Icons.Filled.Home,
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = house.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AssistChip(
                        onClick = {},
                        label = {
                            Text(
                                text = house.access_code ?: "Sem codigo",
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Key,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    )

                    if (house.is_code_active == true) {
                        AssistChip(
                            onClick = {},
                            label = { Text("Aberta") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        )
                    }
                }
            }

            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun EmptyHousesState(onCreateHouse: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            IconBadge(
                icon = Icons.Outlined.Home,
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Voce ainda nao tem nenhuma casa",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "Crie uma casa ou entre com um codigo de convite para comecar.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            FilledTonalButton(onClick = onCreateHouse) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Criar casa")
            }
        }
    }
}

@Composable
private fun EmptySearchState(searchInput: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Text(
            text = "Nenhuma casa encontrada para \"$searchInput\".",
            modifier = Modifier.padding(18.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ErrorBanner(message: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.errorContainer
    ) {
        Text(
            text = message,
            modifier = Modifier.padding(16.dp),
            color = MaterialTheme.colorScheme.onErrorContainer,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun IconBadge(
    icon: ImageVector,
    containerColor: Color,
    contentColor: Color
) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(containerColor),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = contentColor
        )
    }
}
