package com.trustmesh.presentation.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.trustmesh.designsystem.components.pressClickable
import com.trustmesh.designsystem.theme.TrustMeshTheme
import com.trustmesh.domain.model.Agent
import com.trustmesh.domain.model.Transaction
import com.trustmesh.domain.model.TransactionStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    totalExposure: Double,
    anomalyAlert: String?,
    recentTransactions: List<Transaction>,
    agentsList: List<Agent>,
    onNavigateToAgentDetail: (String) -> Unit,
    onNavigateToAccounts: () -> Unit,
    onNavigateToEscrow: () -> Unit,
    onNavigateToLedger: () -> Unit,
    onRefresh: () -> Unit
) {
    val colors = TrustMeshTheme.colors
    val typography = TrustMeshTheme.typography

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("TRUSTMESH", style = typography.headlineLarge.copy(fontWeight = FontWeight.Black)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colors.backgroundBase,
                    titleContentColor = colors.textPrimary
                ),
                actions = {
                    IconButton(onClick = onRefresh) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = "Sync", tint = colors.primary)
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
            // Anomaly warning banner
            if (anomalyAlert != null) {
                item {
                    Surface(
                        color = colors.danger.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, colors.danger),
                        onClick = onNavigateToLedger
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = colors.danger)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = anomalyAlert,
                                style = typography.bodyMedium.copy(color = colors.danger, fontWeight = FontWeight.Bold),
                                modifier = Modifier.weight(1f)
                            )
                            Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = colors.danger)
                        }
                    }
                }
            }

            // Total active authority exposure card
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = colors.surfaceElevated1),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth().pressClickable { onNavigateToAccounts() }
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Shield, contentDescription = null, tint = colors.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Total Exposure Active", style = typography.caption, color = colors.textSecondary)
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "₹${String.format("%.2f", totalExposure)}",
                            style = typography.monetaryLarge,
                            color = colors.textPrimary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Linked to Plaid balance constraints", style = typography.caption, color = colors.primary)
                    }
                }
            }

            // Visual custom Canvas area charting for limit trends
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = colors.surfaceElevated1),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Spending Velocity (30 Days)", style = typography.headlineMedium, color = colors.textPrimary)
                        Spacer(modifier = Modifier.height(16.dp))
                        val points = if (recentTransactions.isEmpty()) {
                            listOf(1200f, 4500f, 3200f, 8500f, 6200f, 9500f)
                        } else {
                            val values = recentTransactions.map { it.amount.toFloat() }.take(7).reversed()
                            if (values.size < 2) listOf(1500f, 3500f, 2500f, 6000f)
                            else values
                        }
                        com.trustmesh.designsystem.components.TrustMeshAreaChart(
                            points = points,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                        )
                    }
                }
            }

            // Recent activity transactions quick list
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Live Activity", style = typography.headlineMedium, color = colors.textPrimary)
                    TextButton(onClick = onNavigateToEscrow) {
                        Text("Approvals Queue", style = typography.bodySmall.copy(color = colors.primary, fontWeight = FontWeight.Bold))
                    }
                }
            }

            if (recentTransactions.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No recent transaction logs.", style = typography.bodySmall, color = colors.textSecondary)
                    }
                }
            } else {
                items(recentTransactions.take(3)) { transaction ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(colors.surfaceElevated1)
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(transaction.merchantName, style = typography.labelLarge, color = colors.textPrimary)
                            Text("Agent ID: ${transaction.agentId.take(8)}", style = typography.caption, color = colors.textSecondary)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "-₹${String.format("%.2f", transaction.amount)}",
                                style = typography.monetaryMedium,
                                color = if (transaction.status == TransactionStatus.DISPUTED) colors.danger else colors.textPrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = if (transaction.status == TransactionStatus.RELEASED) Icons.Default.CheckCircle else Icons.Default.Pending,
                                contentDescription = null,
                                tint = if (transaction.status == TransactionStatus.RELEASED) colors.success else colors.secondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            // Agents list status cards
            item {
                Text("Authorized Agents", style = typography.headlineMedium, color = colors.textPrimary)
            }

            if (agentsList.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Create your first shopping agent profile to start.", style = typography.bodySmall, color = colors.textSecondary)
                    }
                }
            } else {
                items(agentsList) { agent ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(colors.surfaceElevated1)
                            .pressClickable { onNavigateToAgentDetail(agent.id) }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(agent.name, style = typography.headlineMedium, color = colors.textPrimary)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Envelope cap: ₹${agent.spendEnvelope.amountLimit}/${agent.spendEnvelope.windowType.name}", style = typography.caption, color = colors.textSecondary)
                        }
                        
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                color = colors.primary.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = agent.status.name,
                                    style = typography.caption.copy(fontWeight = FontWeight.Bold),
                                    color = colors.primary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = colors.textSecondary)
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
