package com.trustmesh.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.trustmesh.designsystem.theme.TrustMeshTheme
import com.trustmesh.domain.model.Merchant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MerchantExplorerScreen(
    merchants: List<Merchant>,
    onSearch: (String) -> Unit
) {
    val colors = TrustMeshTheme.colors
    val typography = TrustMeshTheme.typography

    var query by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Merchant Reputation Explorer", style = typography.headlineLarge.copy(fontWeight = FontWeight.Bold)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colors.backgroundBase,
                    titleContentColor = colors.textPrimary
                )
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
                value = query,
                onValueChange = {
                    query = it
                    onSearch(it)
                },
                placeholder = { Text("Search merchants (e.g. Amazon, Target)...", color = colors.textSecondary) },
                leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = colors.textSecondary) },
                textStyle = typography.bodyMedium.copy(color = colors.textPrimary),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colors.primary,
                    unfocusedBorderColor = colors.divider
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (merchants.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Search above to explore merchant scores.", style = typography.bodySmall, color = colors.textSecondary)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(merchants) { m ->
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
                                Text(m.name, style = typography.headlineMedium.copy(fontWeight = FontWeight.Bold), color = colors.textPrimary)
                                Text("Category: ${m.category.name}", style = typography.caption, color = colors.textSecondary)
                            }
                            
                            Column(horizontalAlignment = Alignment.End) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Default.Star, contentDescription = null, tint = colors.primary, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "${(m.internalTrustScore * 100).toInt()}/100",
                                        style = typography.monetaryMedium,
                                        color = colors.textPrimary
                                    )
                                }
                                Text("Mesh Rating", style = typography.caption, color = colors.textSecondary)
                            }
                        }
                    }
                }
            }
        }
    }
}
