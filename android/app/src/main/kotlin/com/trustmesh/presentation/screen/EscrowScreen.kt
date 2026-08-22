package com.trustmesh.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.trustmesh.designsystem.theme.TrustMeshTheme
import com.trustmesh.domain.model.EscrowItem
import com.trustmesh.domain.model.EscrowState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EscrowScreen(
    escrowItems: List<EscrowItem>,
    onApprove: (String) -> Unit,
    onDeny: (String) -> Unit,
    onTriggerBiometrics: (onSuccess: () -> Unit) -> Unit
) {
    val colors = TrustMeshTheme.colors
    val typography = TrustMeshTheme.typography

    val pendingItems = escrowItems.filter { it.state == EscrowState.PENDING }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Approvals Queue", style = typography.headlineLarge.copy(fontWeight = FontWeight.Bold)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colors.backgroundBase,
                    titleContentColor = colors.textPrimary
                )
            )
        },
        containerColor = colors.backgroundBase
    ) { innerPadding ->
        if (pendingItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = colors.primary,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("System Aligned", style = typography.headlineMedium, color = colors.textPrimary)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("No transactions are currently held in escrow.", style = typography.bodySmall, color = colors.textSecondary)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(pendingItems) { item ->
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
                                    text = "Held: ${item.conditionType.name}",
                                    style = typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                                    color = colors.secondary
                                )
                                Text(
                                    text = "Threshold: ₹${item.conditionThreshold}",
                                    style = typography.caption,
                                    color = colors.textSecondary
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            Text(
                                text = "Transaction held in escrow safety lock. Click Approve to sign and authorize release or Deny to reject.",
                                style = typography.bodySmall,
                                color = colors.textSecondary
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Button(
                                    onClick = {
                                        onTriggerBiometrics {
                                            onApprove(item.id)
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = colors.backgroundBase)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Approve", style = typography.labelLarge.copy(color = colors.backgroundBase))
                                    }
                                }

                                Button(
                                    onClick = { onDeny(item.id) },
                                    colors = ButtonDefaults.buttonColors(containerColor = colors.danger),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(imageVector = Icons.Default.Close, contentDescription = null, tint = Color.White)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Deny", style = typography.labelLarge.copy(color = Color.White))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
