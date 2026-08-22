package com.trustmesh.presentation.screen

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.trustmesh.designsystem.components.pressClickable
import com.trustmesh.designsystem.theme.TrustMeshTheme

@Composable
fun SplashScreen(
    isUserLoggedIn: Boolean?,
    onNavigateToOnboarding: () -> Unit,
    onNavigateToDashboard: () -> Unit
) {
    val colors = TrustMeshTheme.colors
    val typography = TrustMeshTheme.typography

    LaunchedEffect(isUserLoggedIn) {
        kotlinx.coroutines.delay(1500)
        if (isUserLoggedIn == true) {
            onNavigateToDashboard()
        } else {
            onNavigateToOnboarding()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.backgroundBase),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.Security,
                contentDescription = "TrustMesh Logo",
                tint = colors.primary,
                modifier = Modifier.size(80.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "TRUSTMESH",
                style = typography.displayMedium.copy(fontWeight = FontWeight.Black),
                color = colors.textPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Autonomous Agent Commerce Wallet",
                style = typography.caption,
                color = colors.textSecondary
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    onNavigateToAuth: () -> Unit
) {
    val colors = TrustMeshTheme.colors
    val typography = TrustMeshTheme.typography

    val pages = listOf(
        OnboardingPageData(
            title = "Agent Spend Control",
            description = "Assign shopping tasks to AI agents with strict category boundaries and budget caps. Your money stays in your control.",
            icon = Icons.Default.Wallet
        ),
        OnboardingPageData(
            title = "Compositional Trust Scores",
            description = "Track agent performance metrics in real time. We analyze drift, price deviation, and task compliance.",
            icon = Icons.Default.Analytics
        ),
        OnboardingPageData(
            title = "Cryptographic Auditing",
            description = "Every negotiation event is recorded in a secure, local hash-chained ledger. Complete proof of alignment.",
            icon = Icons.Default.Verified
        )
    )

    val pagerState = rememberPagerState { pages.size }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.backgroundBase)
            .padding(24.dp)
    ) {
        Spacer(modifier = Modifier.weight(0.5f))

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth()
        ) { pageIndex ->
            val page = pages[pageIndex]
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .clip(CircleShape)
                        .background(colors.surfaceElevated1),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = page.icon,
                        contentDescription = null,
                        tint = colors.primary,
                        modifier = Modifier.size(72.dp)
                    )
                }
                Spacer(modifier = Modifier.height(32.dp))
                Text(
                    text = page.title,
                    style = typography.displayMedium,
                    color = colors.textPrimary,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = page.description,
                    style = typography.bodyMedium,
                    color = colors.textSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.weight(0.5f))

        // Pager indicator
        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            repeat(pages.size) { index ->
                val active = pagerState.currentPage == index
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .height(8.dp)
                        .width(if (active) 24.dp else 8.dp)
                        .clip(CircleShape)
                        .background(if (active) colors.primary else colors.divider)
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Next / Finish button
        Button(
            onClick = {
                if (pagerState.currentPage < pages.size - 1) {
                    scope.launch {
                        pagerState.animateScrollToPage(pagerState.currentPage + 1)
                    }
                } else {
                    onNavigateToAuth()
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .pressClickable { },
            shape = RoundedCornerShape(14.dp)
        ) {
            Text(
                text = if (pagerState.currentPage == pages.size - 1) "Get Started" else "Next",
                style = typography.labelLarge.copy(color = colors.backgroundBase, fontWeight = FontWeight.Bold)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

private data class OnboardingPageData(
    val title: String,
    val description: String,
    val icon: ImageVector
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    loading: Boolean,
    errorMessage: String?,
    onSignUp: (String, String, String) -> Unit,
    onLogIn: (String, String) -> Unit,
    onGoogleSignIn: (String) -> Unit,
    onClearError: () -> Unit
) {
    val colors = TrustMeshTheme.colors
    val typography = TrustMeshTheme.typography

    var isSignUp by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var displayName by remember { mutableStateOf("") }

    var focusedField by remember { mutableStateOf("") } // "name", "email", "password", or ""
    var showGoogleDialog by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    if (showGoogleDialog) {
        AlertDialog(
            onDismissRequest = { showGoogleDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = null,
                        tint = colors.primary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Google Sign-In",
                        style = typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        color = colors.textPrimary
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Sign in to TrustMesh using Google credentials.\n\nWeb Client ID: 1010947873386-jh3dcg1lu3c9db3h1eabhm6bughahp3s.apps.googleusercontent.com",
                        style = typography.caption,
                        color = colors.textSecondary
                    )
                    Divider(color = colors.divider)
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(colors.surfaceElevated2)
                            .pressClickable {
                                showGoogleDialog = false
                                onGoogleSignIn("mock_google_token_harshal")
                            }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Harshal", style = typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = colors.textPrimary)
                            Text("harshal@gmail.com", style = typography.caption, color = colors.textSecondary)
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(colors.surfaceElevated2)
                            .pressClickable {
                                showGoogleDialog = false
                                onGoogleSignIn("mock_google_token_harshal.work")
                            }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Harshal (Work)", style = typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = colors.textPrimary)
                            Text("harshal.work@gmail.com", style = typography.caption, color = colors.textSecondary)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showGoogleDialog = false }) {
                    Text("Cancel", style = typography.labelLarge.copy(color = colors.primary))
                }
            },
            containerColor = colors.surfaceElevated1
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.backgroundBase)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(scrollState)
        ) {
            Icon(
                imageVector = Icons.Default.Security,
                contentDescription = "TrustMesh Logo",
                tint = colors.primary,
                modifier = Modifier.size(72.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = if (isSignUp) "Create Account" else "Welcome Back",
                style = typography.displayMedium.copy(fontWeight = FontWeight.Black),
                color = colors.textPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (isSignUp) "Join the trust mesh network" else "Sign in to manage your AI agents",
                style = typography.bodySmall,
                color = colors.textSecondary
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (errorMessage != null) {
                Surface(
                    color = colors.danger.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, colors.danger),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = colors.danger)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = errorMessage,
                            style = typography.bodySmall,
                            color = colors.danger,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = onClearError, modifier = Modifier.size(24.dp)) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Dismiss", tint = colors.danger)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            if (isSignUp) {
                OutlinedTextField(
                    value = displayName,
                    onValueChange = { displayName = it },
                    label = { Text("Display Name", color = colors.textSecondary) },
                    textStyle = typography.bodyMedium.copy(color = colors.textPrimary),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colors.primary,
                        unfocusedBorderColor = colors.divider
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { if (it.isFocused) focusedField = "name" }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email Address", color = colors.textSecondary) },
                textStyle = typography.bodyMedium.copy(color = colors.textPrimary),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colors.primary,
                    unfocusedBorderColor = colors.divider
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { if (it.isFocused) focusedField = "email" }
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password", color = colors.textSecondary) },
                visualTransformation = PasswordVisualTransformation(),
                textStyle = typography.bodyMedium.copy(color = colors.textPrimary),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colors.primary,
                    unfocusedBorderColor = colors.divider
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { if (it.isFocused) focusedField = "password" }
            )

            // Dynamic suggestions/tips box based on focused input
            AnimatedVisibility(
                visible = focusedField.isNotEmpty(),
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                val tipText = when (focusedField) {
                    "name" -> "Name suggested: Put your full name so AI agents can address you properly."
                    "email" -> "Email suggested: We verification-check emails to sync your Plaid bank link."
                    "password" -> "Password suggested: Must contain at least 8 characters with numbers."
                    else -> ""
                }
                if (tipText.isNotEmpty()) {
                    Column {
                        Spacer(modifier = Modifier.height(12.dp))
                        Surface(
                            color = colors.primary.copy(alpha = 0.05f),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, colors.primary.copy(alpha = 0.2f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = colors.primary, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = tipText, style = typography.caption, color = colors.textSecondary)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (loading) {
                CircularProgressIndicator(color = colors.primary)
            } else {
                Button(
                    onClick = {
                        if (isSignUp) {
                            onSignUp(email, password, displayName)
                        } else {
                            onLogIn(email, password)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Text(
                        text = if (isSignUp) "Sign Up" else "Log In",
                        style = typography.labelLarge.copy(color = colors.backgroundBase, fontWeight = FontWeight.Bold)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Firebase Google Sign In Button
                OutlinedButton(
                    onClick = { showGoogleDialog = true },
                    border = BorderStroke(1.dp, colors.divider),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.textPrimary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "Google Logo",
                            tint = colors.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Sign In with Google",
                            style = typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                TextButton(
                    onClick = {
                        isSignUp = !isSignUp
                        onClearError()
                        focusedField = ""
                    }
                ) {
                    Text(
                        text = if (isSignUp) "Already have an account? Log In" else "New to TrustMesh? Sign Up",
                        style = typography.bodySmall.copy(color = colors.primary, fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }
}
