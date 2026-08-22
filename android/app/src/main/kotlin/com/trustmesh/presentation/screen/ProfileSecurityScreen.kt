package com.trustmesh.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.trustmesh.designsystem.theme.TrustMeshTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSecurityScreen(
    sessionsList: List<String>,
    onBack: () -> Unit,
    onRevokeSession: (String) -> Unit
) {
    val colors = TrustMeshTheme.colors
    val typography = TrustMeshTheme.typography

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Active Sessions", style = typography.headlineLarge.copy(fontWeight = FontWeight.Bold)) },
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
            item {
                Text(
                    text = "Verify and manage authorized browser nodes, phone applications, and procurement device hooks linked to your core mesh account.",
                    style = typography.caption,
                    color = colors.textSecondary
                )
            }

            if (sessionsList.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No active sessions synced.", style = typography.bodySmall, color = colors.textSecondary)
                    }
                }
            } else {
                items(sessionsList) { session ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(colors.surfaceElevated1)
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(imageVector = Icons.Default.Devices, contentDescription = null, tint = colors.primary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = session,
                                style = typography.bodyMedium,
                                color = colors.textPrimary
                            )
                        }
                        
                        IconButton(onClick = { onRevokeSession(session) }) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Revoke session", tint = colors.danger)
                        }
                    }
                }
            }
        }
    }
}
