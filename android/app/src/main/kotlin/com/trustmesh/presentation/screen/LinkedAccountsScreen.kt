package com.trustmesh.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.trustmesh.designsystem.theme.TrustMeshTheme
import com.trustmesh.domain.model.LinkedAccount

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LinkedAccountsScreen(
    accounts: List<LinkedAccount>,
    totalAgentLimit: Double,
    onLinkBank: () -> Unit,
    onRefresh: () -> Unit
) {
    val colors = TrustMeshTheme.colors
    val typography = TrustMeshTheme.typography

    val totalBankBalance = accounts.sumOf { it.availableBalance }
    val remainingUnreserved = (totalBankBalance - totalAgentLimit).coerceAtLeast(0.0)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Financial Caches", style = typography.headlineLarge.copy(fontWeight = FontWeight.Bold)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colors.backgroundBase,
                    titleContentColor = colors.textPrimary
                ),
                actions = {
                    IconButton(onClick = onLinkBank) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Link Plaid", tint = colors.primary)
                    }
                }
            )
        },
        containerColor = colors.backgroundBase
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Reconciliation balance allocation card
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = colors.surfaceElevated1),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = colors.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Envelope Reserve Reconciliation", style = typography.caption, color = colors.textSecondary)
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Total Bank Funds", style = typography.bodyMedium, color = colors.textSecondary)
                            Text("₹${String.format("%.2f", totalBankBalance)}", style = typography.monetaryMedium, color = colors.textPrimary)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Reserved by Agents", style = typography.bodyMedium, color = colors.textSecondary)
                            Text("₹${String.format("%.2f", totalAgentLimit)}", style = typography.monetaryMedium, color = colors.secondary)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Divider(color = colors.divider, thickness = 1.dp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Free Unreserved Funds", style = typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = colors.textPrimary)
                            Text("₹${String.format("%.2f", remainingUnreserved)}", style = typography.monetaryMedium, color = colors.success)
                        }
                    }
                }
            }

            // Linked accounts header
            item {
                Text("Linked Plaid (Sandbox) Accounts", style = typography.headlineMedium.copy(fontWeight = FontWeight.Bold), color = colors.textPrimary)
            }

            if (accounts.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("No bank accounts linked.", style = typography.bodySmall, color = colors.textSecondary)
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = onLinkBank,
                                colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Link via Plaid Sandbox", style = typography.labelLarge.copy(color = colors.backgroundBase))
                            }
                        }
                    }
                }
            } else {
                items(accounts) { acc ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(colors.surfaceElevated1)
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(acc.institutionName, style = typography.labelLarge.copy(fontWeight = FontWeight.Bold), color = colors.textPrimary)
                            Text("ID: ...${acc.plaidAccountId.takeLast(4)}", style = typography.caption, color = colors.textSecondary)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("₹${String.format("%.2f", acc.availableBalance)}", style = typography.monetaryMedium, color = colors.textPrimary)
                            Text("Available", style = typography.caption, color = colors.textSecondary)
                        }
                    }
                }
            }
        }
    }
}
