package com.trustmesh.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.trustmesh.designsystem.components.pressClickable
import com.trustmesh.designsystem.theme.TrustMeshTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    biometricsEnabled: Boolean,
    onBiometricsChange: (Boolean) -> Unit,
    isDarkTheme: Boolean,
    onThemeChange: (Boolean) -> Unit,
    onNavigateToProfileSecurity: () -> Unit,
    onExportData: (format: String) -> Unit, // CSV or JSON
    onDeleteAccount: () -> Unit,
    onLogout: () -> Unit
) {
    val colors = TrustMeshTheme.colors
    val typography = TrustMeshTheme.typography

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings & Privacy", style = typography.headlineLarge.copy(fontWeight = FontWeight.Bold)) },
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
            // Biometrics preference
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.surfaceElevated1)
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Fingerprint, contentDescription = null, tint = colors.primary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Biometric Authorization Gate", style = typography.labelLarge, color = colors.textPrimary)
                            Text("Confirm agent deployments & budget updates", style = typography.caption, color = colors.textSecondary)
                        }
                    }
                    Switch(
                        checked = biometricsEnabled,
                        onCheckedChange = onBiometricsChange,
                        colors = SwitchDefaults.colors(checkedThumbColor = colors.primary)
                    )
                }
            }

            // Theme mode toggle
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.surfaceElevated1)
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Brightness4, contentDescription = null, tint = colors.primary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Light Theme Mode", style = typography.labelLarge, color = colors.textPrimary)
                            Text("Switch background colors into white or dark", style = typography.caption, color = colors.textSecondary)
                        }
                    }
                    Switch(
                        checked = !isDarkTheme,
                        onCheckedChange = { onThemeChange(!it) },
                        colors = SwitchDefaults.colors(checkedThumbColor = colors.primary)
                    )
                }
            }

            // Export Data options card
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = colors.surfaceElevated1),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Export Audit Logs", style = typography.headlineMedium.copy(fontWeight = FontWeight.Bold), color = colors.textPrimary)
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Button(
                                onClick = { onExportData("CSV") },
                                colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Default.Share, contentDescription = null, tint = colors.backgroundBase, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Export CSV", style = typography.labelLarge.copy(color = colors.backgroundBase))
                                }
                            }

                            Button(
                                onClick = { onExportData("JSON") },
                                colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Default.Code, contentDescription = null, tint = colors.backgroundBase, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Export JSON", style = typography.labelLarge.copy(color = colors.backgroundBase))
                                }
                            }
                        }
                    }
                }
            }

            // Security controls list
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.surfaceElevated1)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .pressClickable { onNavigateToProfileSecurity() }
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = colors.primary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Sessions & Devices", style = typography.labelLarge, color = colors.textPrimary)
                        }
                        Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = colors.textSecondary)
                    }
                }
            }

            // Logout & Delete buttons
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                        onClick = onLogout,
                        colors = ButtonDefaults.buttonColors(containerColor = colors.divider),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Text("Log Out", style = typography.labelLarge.copy(color = colors.textPrimary))
                    }

                    Button(
                        onClick = onDeleteAccount,
                        colors = ButtonDefaults.buttonColors(containerColor = colors.danger.copy(alpha = 0.1f)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Text("Terminate Account", style = typography.labelLarge.copy(color = colors.danger, fontWeight = FontWeight.Bold))
                    }
                }
            }
        }
    }
}
