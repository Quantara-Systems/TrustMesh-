package com.trustmesh.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.trustmesh.designsystem.components.TrustMeshGauge
import com.trustmesh.designsystem.components.pressClickable
import com.trustmesh.designsystem.theme.TrustMeshTheme
import com.trustmesh.domain.model.Agent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgentsScreen(
    agents: List<Agent>,
    onNavigateToCreateAgent: () -> Unit,
    onNavigateToAgentDetail: (String) -> Unit
) {
    val colors = TrustMeshTheme.colors
    val typography = TrustMeshTheme.typography

    var searchQuery by remember { mutableStateOf("") }
    var sortBy by remember { mutableStateOf("NAME") } // "NAME", "LIMIT", "STATUS"

    val filteredAgents = remember(agents, searchQuery, sortBy) {
        agents.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
            it.intentStatement.contains(searchQuery, ignoreCase = true)
        }.sortedWith { a, b ->
            when (sortBy) {
                "LIMIT" -> b.spendEnvelope.amountLimit.compareTo(a.spendEnvelope.amountLimit)
                "STATUS" -> a.status.name.compareTo(b.status.name)
                else -> a.name.compareTo(b.name, ignoreCase = true)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Agents Registry", style = typography.headlineLarge.copy(fontWeight = FontWeight.Bold)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colors.backgroundBase,
                    titleContentColor = colors.textPrimary
                ),
                actions = {
                    IconButton(onClick = onNavigateToCreateAgent) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "New Agent", tint = colors.primary)
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
                placeholder = { Text("Search agents...", color = colors.textSecondary) },
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

            // Sorting chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = sortBy == "NAME",
                    onClick = { sortBy = "NAME" },
                    label = { Text("Name") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = colors.primary,
                        selectedLabelColor = colors.backgroundBase
                    )
                )
                FilterChip(
                    selected = sortBy == "LIMIT",
                    onClick = { sortBy = "LIMIT" },
                    label = { Text("Budget Cap") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = colors.primary,
                        selectedLabelColor = colors.backgroundBase
                    )
                )
                FilterChip(
                    selected = sortBy == "STATUS",
                    onClick = { sortBy = "STATUS" },
                    label = { Text("Status") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = colors.primary,
                        selectedLabelColor = colors.backgroundBase
                    )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Agents list
            if (filteredAgents.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No agents matching filters.", style = typography.bodySmall, color = colors.textSecondary)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(filteredAgents) { agent ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(colors.surfaceElevated1)
                                .pressClickable { onNavigateToAgentDetail(agent.id) }
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Radial Gauge for spend envelope utilization
                            val utilization = if (agent.spendEnvelope.amountLimit > 0) {
                                agent.spendEnvelope.currentUtilization / agent.spendEnvelope.amountLimit
                            } else 0.0
                            TrustMeshGauge(
                                progress = utilization.toFloat(),
                                strokeWidth = 6.dp,
                                valueText = "${(utilization * 100).toInt()}%",
                                modifier = Modifier.size(72.dp)
                            )

                            Spacer(modifier = Modifier.width(16.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = agent.name,
                                    style = typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                                    color = colors.textPrimary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = agent.intentStatement,
                                    style = typography.bodySmall,
                                    color = colors.textSecondary,
                                    maxLines = 1
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Cap: ₹${agent.spendEnvelope.amountLimit} / ${agent.spendEnvelope.windowType}",
                                    style = typography.caption.copy(fontWeight = FontWeight.SemiBold),
                                    color = colors.primary
                                )
                            }

                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = colors.textSecondary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
