package com.trustmesh.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.trustmesh.designsystem.components.TrustMeshRadarChart
import com.trustmesh.designsystem.theme.TrustMeshTheme
import com.trustmesh.domain.model.Agent
import com.trustmesh.domain.model.AgentStatus
import com.trustmesh.domain.model.Transaction
import com.trustmesh.domain.model.WindowType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgentDetailScreen(
    agent: Agent?,
    transactions: List<Transaction>,
    trustBreakdown: Map<String, Float>,
    isSimulating: Boolean,
    onStartSimulation: () -> Unit,
    onStopSimulation: () -> Unit,
    onBack: () -> Unit,
    onUpdateEnvelope: (Double, WindowType) -> Unit,
    onUpdateStatus: (AgentStatus) -> Unit,
    onTriggerBiometrics: (onSuccess: () -> Unit) -> Unit
) {
    val colors = TrustMeshTheme.colors
    val typography = TrustMeshTheme.typography

    if (agent == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = colors.primary)
        }
        return
    }

    var editLimitAmount by remember(agent) { mutableDoubleStateOf(agent.spendEnvelope.amountLimit) }
    var editWindowType by remember(agent) { mutableStateOf(agent.spendEnvelope.windowType) }
    var showEditDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(agent.name, style = typography.headlineLarge.copy(fontWeight = FontWeight.Bold)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = colors.textPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colors.backgroundBase,
                    titleContentColor = colors.textPrimary
                )
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
            // Summary and status
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(colors.surfaceElevated1)
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Intent Statement", style = typography.caption, color = colors.textSecondary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(agent.intentStatement, style = typography.bodyMedium, color = colors.textPrimary)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Surface(
                        color = when (agent.status) {
                            AgentStatus.ACTIVE -> colors.success.copy(alpha = 0.1f)
                            AgentStatus.PAUSED -> colors.secondary.copy(alpha = 0.1f)
                            AgentStatus.REVOKED -> colors.danger.copy(alpha = 0.1f)
                        },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = agent.status.name,
                            color = when (agent.status) {
                                AgentStatus.ACTIVE -> colors.success
                                AgentStatus.PAUSED -> colors.secondary
                                AgentStatus.REVOKED -> colors.danger
                            },
                            style = typography.caption.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            // Trust radar composition chart card
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = colors.surfaceElevated1),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Compositional Trust Radar",
                            style = typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                            color = colors.textPrimary,
                            modifier = Modifier.align(Alignment.Start)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        TrustMeshRadarChart(
                            data = trustBreakdown,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                        )
                    }
                }
            }

            // Spending trend line/area chart card
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = colors.surfaceElevated1),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Spending History Velocity (7 Days)",
                            style = typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                            color = colors.textPrimary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        val points = if (transactions.isEmpty()) {
                            listOf(2000f, 4500f, 3200f, 8500f, 6200f, 9500f, 11000f)
                        } else {
                            val values = transactions.map { it.amount.toFloat() }.take(7).reversed()
                            if (values.size < 2) listOf(2000f, 3500f, 2800f, 7500f)
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

            // Negotiation Simulator control card
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = colors.surfaceElevated1),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Autonomous Negotiation Simulator",
                            style = typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                            color = colors.textPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Simulates autonomous procurement agents querying products and bidding in real time.",
                            style = typography.caption,
                            color = colors.textSecondary
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isSimulating) "Simulating: ACTIVE" else "Simulating: IDLE",
                                style = typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSimulating) colors.primary else colors.textSecondary
                                )
                            )
                            Button(
                                onClick = { if (isSimulating) onStopSimulation() else onStartSimulation() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isSimulating) colors.danger else colors.primary
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = if (isSimulating) "Stop Simulator" else "Start Simulator",
                                    style = typography.labelLarge.copy(color = colors.backgroundBase)
                                )
                            }
                        }
                    }
                }
            }

            // Envelope limits control card
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = colors.surfaceElevated1),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Spend Envelope Constraints",
                                style = typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                                color = colors.textPrimary
                            )
                            IconButton(onClick = { showEditDialog = true }) {
                                Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit Limits", tint = colors.primary)
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Total Utilization limit", style = typography.bodyMedium, color = colors.textSecondary)
                            Text(
                                "₹${agent.spendEnvelope.currentUtilization} / ₹${agent.spendEnvelope.amountLimit}",
                                style = typography.monetaryMedium,
                                color = colors.textPrimary
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Window constraints: ${agent.spendEnvelope.windowType}",
                            style = typography.caption,
                            color = colors.textSecondary
                        )
                    }
                }
            }

            // Transaction negotiation history list
            item {
                Text(
                    "Negotiation History",
                    style = typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = colors.textPrimary
                )
            }

            if (transactions.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No logs recorded for this agent.", style = typography.bodySmall, color = colors.textSecondary)
                    }
                }
            } else {
                items(transactions) { tx ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(colors.surfaceElevated1)
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(tx.merchantName, style = typography.labelLarge.copy(fontWeight = FontWeight.Bold), color = colors.textPrimary)
                            Text("-₹${tx.amount}", style = typography.monetaryMedium, color = colors.textPrimary)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(tx.negotiationDetail, style = typography.bodySmall, color = colors.textSecondary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Time: ${tx.createdAt.take(16)}", style = typography.caption, color = colors.textSecondary)
                    }
                }
            }

            // Action triggers (Pause, Resume, Revoke authority)
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (agent.status == AgentStatus.ACTIVE) {
                        Button(
                            onClick = {
                                onTriggerBiometrics {
                                    onUpdateStatus(AgentStatus.PAUSED)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = colors.secondary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.Pause, contentDescription = null, tint = colors.backgroundBase)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Pause", style = typography.labelLarge.copy(color = colors.backgroundBase))
                            }
                        }
                    } else if (agent.status == AgentStatus.PAUSED) {
                        Button(
                            onClick = {
                                onTriggerBiometrics {
                                    onUpdateStatus(AgentStatus.ACTIVE)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, tint = colors.backgroundBase)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Resume", style = typography.labelLarge.copy(color = colors.backgroundBase))
                            }
                        }
                    }

                    if (agent.status != AgentStatus.REVOKED) {
                        Button(
                            onClick = {
                                onTriggerBiometrics {
                                    onUpdateStatus(AgentStatus.REVOKED)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = colors.danger),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.Delete, contentDescription = null, tint = Color.White)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Revoke", style = typography.labelLarge.copy(color = Color.White))
                            }
                        }
                    }
                }
            }
        }
    }

    // Limit Editor Dialog
    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Edit Spend Envelope constraints", style = typography.headlineLarge, color = colors.textPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    OutlinedTextField(
                        value = editLimitAmount.toString(),
                        onValueChange = { editLimitAmount = it.toDoubleOrNull() ?: editLimitAmount },
                        label = { Text("Limit Amount (₹)", color = colors.textSecondary) },
                        textStyle = typography.bodyMedium.copy(color = colors.textPrimary),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = colors.primary)
                    )

                    Column {
                        Text("Window Cycle", style = typography.bodyMedium, color = colors.textPrimary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            WindowType.values().forEach { type ->
                                val selected = editWindowType == type
                                FilterChip(
                                    selected = selected,
                                    onClick = { editWindowType = type },
                                    label = { Text(type.name) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = colors.primary,
                                        selectedLabelColor = colors.backgroundBase
                                    )
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showEditDialog = false
                        onTriggerBiometrics {
                            onUpdateEnvelope(editLimitAmount, editWindowType)
                        }
                    }
                ) {
                    Text("Save Changes", style = typography.bodyMedium, color = colors.primary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("Cancel", style = typography.bodyMedium, color = colors.textSecondary)
                }
            },
            containerColor = colors.surfaceElevated1
        )
    }
}
