package com.trustmesh.presentation.screen

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.trustmesh.designsystem.components.pressClickable
import com.trustmesh.designsystem.theme.TrustMeshTheme
import com.trustmesh.domain.model.Category
import com.trustmesh.domain.model.Transaction
import com.trustmesh.domain.model.TransactionStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsScreen(
    transactions: List<Transaction>,
    onRefresh: () -> Unit
) {
    val colors = TrustMeshTheme.colors
    val typography = TrustMeshTheme.typography

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<Category?>(null) }
    val expandedTxIds = remember { mutableStateListOf<String>() }

    val filteredList = remember(transactions, searchQuery, selectedCategory) {
        transactions.filter {
            (selectedCategory == null || it.merchantCategory == selectedCategory) &&
            (it.merchantName.contains(searchQuery, ignoreCase = true) ||
             it.negotiationDetail.contains(searchQuery, ignoreCase = true))
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Transactions Stream", style = typography.headlineLarge.copy(fontWeight = FontWeight.Bold)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colors.backgroundBase,
                    titleContentColor = colors.textPrimary
                ),
                actions = {
                    IconButton(onClick = onRefresh) {
                        Text("Refresh", style = typography.bodySmall.copy(color = colors.primary, fontWeight = FontWeight.Bold))
                    }
                }
            )
        },
        containerColor = colors.backgroundBase
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            // Search field
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search transactions...", color = colors.textSecondary) },
                leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = colors.textSecondary) },
                textStyle = typography.bodyMedium.copy(color = colors.textPrimary),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colors.primary,
                    unfocusedBorderColor = colors.divider
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )

            // Category filter chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedCategory == null,
                    onClick = { selectedCategory = null },
                    label = { Text("All") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = colors.primary,
                        selectedLabelColor = colors.backgroundBase
                    )
                )
                Category.values().take(3).forEach { cat ->
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = { selectedCategory = cat },
                        label = { Text(cat.name) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = colors.primary,
                            selectedLabelColor = colors.backgroundBase
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Feed list
            if (filteredList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No transactions match criteria.", style = typography.bodySmall, color = colors.textSecondary)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredList) { tx ->
                        val expanded = expandedTxIds.contains(tx.id)
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(colors.surfaceElevated1)
                                .pressClickable {
                                    if (expanded) expandedTxIds.remove(tx.id) else expandedTxIds.add(tx.id)
                                }
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(tx.merchantName, style = typography.labelLarge.copy(fontWeight = FontWeight.Bold), color = colors.textPrimary)
                                    Text(tx.createdAt.take(16), style = typography.caption, color = colors.textSecondary)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "-₹${String.format("%.2f", tx.amount)}",
                                        style = typography.monetaryMedium,
                                        color = if (tx.status == TransactionStatus.DISPUTED) colors.danger else colors.textPrimary
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Icon(
                                        imageVector = Icons.Default.ChevronRight,
                                        contentDescription = null,
                                        tint = colors.textSecondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            AnimatedVisibility(
                                visible = expanded,
                                enter = expandVertically() + fadeIn(),
                                exit = shrinkVertically() + fadeOut()
                            ) {
                                Column(modifier = Modifier.padding(top = 12.dp)) {
                                    Divider(color = colors.divider, thickness = 1.dp)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Negotiation Log", style = typography.labelLarge, color = colors.primary)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(tx.negotiationDetail, style = typography.bodySmall, color = colors.textSecondary)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Agent constraints state: ${tx.status}", style = typography.caption, color = colors.primary)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
