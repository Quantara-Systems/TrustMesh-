package com.trustmesh.presentation.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.trustmesh.designsystem.theme.TrustMeshTheme
import com.trustmesh.domain.model.LedgerEntry

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LedgerScreen(
    entries: List<LedgerEntry>,
    isChainValid: Boolean?,
    onVerifyChain: () -> Unit
) {
    val colors = TrustMeshTheme.colors
    val typography = TrustMeshTheme.typography

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Audit Alignment Ledger", style = typography.headlineLarge.copy(fontWeight = FontWeight.Bold)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colors.backgroundBase,
                    titleContentColor = colors.textPrimary
                ),
                actions = {
                    IconButton(onClick = onVerifyChain) {
                        Icon(imageVector = Icons.Default.Verified, contentDescription = "Verify Ledger", tint = colors.primary)
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
            // Verification status banner
            item {
                Surface(
                    color = when (isChainValid) {
                        true -> colors.success.copy(alpha = 0.1f)
                        false -> colors.danger.copy(alpha = 0.1f)
                        else -> colors.surfaceElevated1
                    },
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(
                        width = 1.dp,
                        color = when (isChainValid) {
                            true -> colors.success
                            false -> colors.danger
                            else -> colors.divider
                        }
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = when (isChainValid) {
                                true -> Icons.Default.CheckCircle
                                false -> Icons.Default.Warning
                                else -> Icons.Default.Info
                            },
                            contentDescription = null,
                            tint = when (isChainValid) {
                                true -> colors.success
                                false -> colors.danger
                                else -> colors.textSecondary
                            }
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = when (isChainValid) {
                                    true -> "Chain Audit Passed"
                                    false -> "Chain Validation Failed!"
                                    else -> "Chain Verification Pending"
                                },
                                style = typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                color = when (isChainValid) {
                                    true -> colors.success
                                    false -> colors.danger
                                    else -> colors.textPrimary
                                }
                            )
                            Text(
                                text = when (isChainValid) {
                                    true -> "Every transaction's hash-chain successfully matches stated rules on-device."
                                    false -> "Warning: Ledger discrepancy detected! Verify node connection."
                                    else -> "Click Verify to validate SHA-256 blocks."
                                },
                                style = typography.caption,
                                color = colors.textSecondary
                            )
                        }
                    }
                }
            }

            // Ledger entries list
            if (entries.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No ledger logs written.", style = typography.bodySmall, color = colors.textSecondary)
                    }
                }
            } else {
                items(entries) { entry ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(colors.surfaceElevated1)
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Agent: ${entry.agentId.take(8)}",
                                style = typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                color = colors.textPrimary
                            )
                            Text(
                                text = entry.outcome,
                                style = typography.caption.copy(fontWeight = FontWeight.Bold),
                                color = if (entry.outcome.contains("DRIFT", ignoreCase = true)) colors.danger else colors.success
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))

                        Text("Stated Intent", style = typography.caption, color = colors.primary)
                        Text(entry.statedIntentSnapshot, style = typography.bodySmall, color = colors.textPrimary)
                        
                        Spacer(modifier = Modifier.height(4.dp))

                        Text("Action Taken", style = typography.caption, color = colors.primary)
                        Text(entry.actionTaken, style = typography.bodySmall, color = colors.textPrimary)

                        Spacer(modifier = Modifier.height(12.dp))
                        Divider(color = colors.divider, thickness = 1.dp)
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Previous Hash", style = typography.caption, color = colors.textSecondary)
                                Text(
                                    text = entry.previousHash.take(16) + "...",
                                    style = typography.monospace,
                                    color = colors.textSecondary
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Current Hash", style = typography.caption, color = colors.textSecondary)
                                Text(
                                    text = entry.hash.take(16) + "...",
                                    style = typography.monospace,
                                    color = colors.primary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
