package com.trustmesh.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.trustmesh.designsystem.theme.TrustMeshTheme
import com.trustmesh.domain.model.Category
import com.trustmesh.domain.model.EscalationRule
import com.trustmesh.domain.model.WindowType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateAgentWizard(
    onBack: () -> Unit,
    onCreateAgent: (String, String, List<Category>, Double, WindowType, List<EscalationRule>) -> Unit
) {
    val colors = TrustMeshTheme.colors
    val typography = TrustMeshTheme.typography

    var currentStep by remember { mutableStateOf(1) }

    // Step 1 data
    var agentName by remember { mutableStateOf("") }
    val selectedCategories = remember { mutableStateListOf<Category>() }

    // Step 2 data
    var intentStatement by remember { mutableStateOf("") }

    // Step 3 data
    var limitAmount by remember { mutableStateOf("200") }
    var windowType by remember { mutableStateOf(WindowType.WEEKLY) }

    // Step 4 data
    var escalationLimit by remember { mutableStateOf("150") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Deploy AI Agent (Step $currentStep/5)", style = typography.headlineLarge.copy(fontWeight = FontWeight.Bold)) },
                navigationIcon = {
                    IconButton(onClick = {
                        if (currentStep > 1) currentStep-- else onBack()
                    }) {
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                when (currentStep) {
                    1 -> {
                        Text("Core Definition", style = typography.headlineMedium, color = colors.textPrimary)
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedTextField(
                            value = agentName,
                            onValueChange = { agentName = it },
                            label = { Text("Agent Identity (e.g. Grocery Assistant)", color = colors.textSecondary) },
                            textStyle = typography.bodyMedium.copy(color = colors.textPrimary),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = colors.primary),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Text("Authorization Category Scope", style = typography.labelLarge, color = colors.textPrimary)
                        Spacer(modifier = Modifier.height(8.dp))
                        Category.values().forEach { cat ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = selectedCategories.contains(cat),
                                    onCheckedChange = { checked ->
                                        if (checked) selectedCategories.add(cat) else selectedCategories.remove(cat)
                                    },
                                    colors = CheckboxDefaults.colors(checkedColor = colors.primary)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(cat.name, style = typography.bodyMedium, color = colors.textPrimary)
                            }
                        }
                    }
                    2 -> {
                        Text("Stated Intent Boundary", style = typography.headlineMedium, color = colors.textPrimary)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "This statement is cryptographically bound to the agent. Actions deviating from this intent trigger alignment alerts.",
                            style = typography.caption,
                            color = colors.textSecondary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedTextField(
                            value = intentStatement,
                            onValueChange = { intentStatement = it },
                            placeholder = { Text("Purchase weekly grocery items, aiming to minimize price and buy organic alternatives where possible.", color = colors.textSecondary) },
                            textStyle = typography.bodyMedium.copy(color = colors.textPrimary),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = colors.primary),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                        )
                    }
                    3 -> {
                        Text("Envelope Caps", style = typography.headlineMedium, color = colors.textPrimary)
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedTextField(
                            value = limitAmount,
                            onValueChange = { limitAmount = it },
                            label = { Text("Spend cap (₹)", color = colors.textSecondary) },
                            textStyle = typography.bodyMedium.copy(color = colors.textPrimary),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = colors.primary),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Text("Window Cycle", style = typography.labelLarge, color = colors.textPrimary)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            WindowType.values().forEach { type ->
                                FilterChip(
                                    selected = windowType == type,
                                    onClick = { windowType = type },
                                    label = { Text(type.name) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = colors.primary,
                                        selectedLabelColor = colors.backgroundBase
                                    )
                                )
                            }
                        }
                    }
                    4 -> {
                        Text("Escalation Gateways", style = typography.headlineMedium, color = colors.textPrimary)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Set values that trigger manual biometric authorization on the client.",
                            style = typography.caption,
                            color = colors.textSecondary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedTextField(
                            value = escalationLimit,
                            onValueChange = { escalationLimit = it },
                            label = { Text("Ask approval for transactions above (₹)", color = colors.textSecondary) },
                            textStyle = typography.bodyMedium.copy(color = colors.textPrimary),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = colors.primary),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    5 -> {
                        Text("Review Authorization Contract", style = typography.headlineMedium, color = colors.textPrimary)
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text("Identity", style = typography.labelLarge, color = colors.primary)
                        Text(agentName, style = typography.bodyMedium, color = colors.textPrimary)
                        Spacer(modifier = Modifier.height(12.dp))

                        Text("Stated Intent", style = typography.labelLarge, color = colors.primary)
                        Text(intentStatement, style = typography.bodyMedium, color = colors.textPrimary)
                        Spacer(modifier = Modifier.height(12.dp))

                        Text("Budget Cap", style = typography.labelLarge, color = colors.primary)
                        Text("₹$limitAmount / $windowType", style = typography.bodyMedium, color = colors.textPrimary)
                        Spacer(modifier = Modifier.height(12.dp))

                        Text("Authorized Scope", style = typography.labelLarge, color = colors.primary)
                        Text(selectedCategories.joinToString { it.name }, style = typography.bodyMedium, color = colors.textPrimary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Navigation actions
            Button(
                onClick = {
                    if (currentStep < 5) {
                        currentStep++
                    } else {
                        val limitVal = limitAmount.toDoubleOrNull() ?: 200.0
                        val escVal = escalationLimit.toDoubleOrNull() ?: 150.0
                        val rulesList = listOf(
                            EscalationRule("SINGLE_TRANSACTION_LIMIT", escVal, "Require verification above ₹$escVal")
                        )
                        onCreateAgent(agentName, intentStatement, selectedCategories.toList(), limitVal, windowType, rulesList)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text(
                    text = if (currentStep == 5) "Confirm & Deploy" else "Continue",
                    style = typography.labelLarge.copy(color = colors.backgroundBase, fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}
