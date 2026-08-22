package com.trustmesh

import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.currentBackStackEntryAsState
import com.trustmesh.domain.model.AgentStatus
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.trustmesh.designsystem.theme.TrustMeshTheme
import com.trustmesh.presentation.navigation.Screen
import com.trustmesh.presentation.screen.*
import com.trustmesh.presentation.viewmodel.*
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    private val authViewModel: AuthViewModel by viewModels()
    private val dashboardViewModel: DashboardViewModel by viewModels()
    private val agentsViewModel: AgentsViewModel by viewModels()
    private val transactionsViewModel: TransactionsViewModel by viewModels()
    private val accountsViewModel: AccountsViewModel by viewModels()
    private val ledgerViewModel: LedgerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val isDark by authViewModel.isDarkTheme.collectAsState()
            TrustMeshTheme(darkTheme = isDark) {
                MainAppNavHost()
            }
        }
    }

    @OptIn(ExperimentalLayoutApi::class)
    @Composable
    fun MainAppNavHost() {
        val navController = rememberNavController()
        val user by authViewModel.user.collectAsState()

        val isUserLoggedIn = user != null
        val isOnline by remember { com.trustmesh.presentation.util.NetworkMonitor(this@MainActivity).isOnline }.collectAsState(initial = true)

        val currentBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = currentBackStackEntry?.destination

        val showBottomBar = currentDestination?.route?.let { route ->
            // Show bottom bar on primary screens only
            route.contains("Dashboard") || route.contains("AgentsList") ||
                    route.contains("EscrowApprovals") || route.contains("TransactionsFeed") ||
                    route.contains("Settings")
        } ?: false

        Scaffold(
            topBar = {
                if (!isOnline) {
                    Surface(
                        color = TrustMeshTheme.colors.danger,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "OFFLINE: Utilizing Local Cached Room Database",
                            color = androidx.compose.ui.graphics.Color.White,
                            style = TrustMeshTheme.typography.caption,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier.padding(6.dp)
                        )
                    }
                }
            },
            bottomBar = {
                if (showBottomBar) {
                    NavigationBar(
                        containerColor = TrustMeshTheme.colors.surfaceElevated1,
                        tonalElevation = 8.dp
                    ) {
                        NavigationBarItem(
                            selected = currentDestination?.route?.contains("Dashboard") == true,
                            onClick = {
                                navController.navigate(Screen.Dashboard) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(Icons.Default.Dashboard, contentDescription = "Home") },
                            label = { Text("Home", style = TrustMeshTheme.typography.caption) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = TrustMeshTheme.colors.primary,
                                unselectedIconColor = TrustMeshTheme.colors.textSecondary
                            )
                        )
                        NavigationBarItem(
                            selected = currentDestination?.route?.contains("AgentsList") == true,
                            onClick = {
                                navController.navigate(Screen.AgentsList) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(Icons.Default.SmartToy, contentDescription = "Agents") },
                            label = { Text("Agents", style = TrustMeshTheme.typography.caption) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = TrustMeshTheme.colors.primary,
                                unselectedIconColor = TrustMeshTheme.colors.textSecondary
                            )
                        )
                        NavigationBarItem(
                            selected = currentDestination?.route?.contains("EscrowApprovals") == true,
                            onClick = {
                                navController.navigate(Screen.EscrowApprovals) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(Icons.Default.Shield, contentDescription = "Escrow") },
                            label = { Text("Holds", style = TrustMeshTheme.typography.caption) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = TrustMeshTheme.colors.primary,
                                unselectedIconColor = TrustMeshTheme.colors.textSecondary
                            )
                        )
                        NavigationBarItem(
                            selected = currentDestination?.route?.contains("TransactionsFeed") == true,
                            onClick = {
                                navController.navigate(Screen.TransactionsFeed) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(Icons.Default.History, contentDescription = "Feed") },
                            label = { Text("Feed", style = TrustMeshTheme.typography.caption) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = TrustMeshTheme.colors.primary,
                                unselectedIconColor = TrustMeshTheme.colors.textSecondary
                            )
                        )
                        NavigationBarItem(
                            selected = currentDestination?.route?.contains("Settings") == true,
                            onClick = {
                                navController.navigate(Screen.Settings) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                            label = { Text("Settings", style = TrustMeshTheme.typography.caption) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = TrustMeshTheme.colors.primary,
                                unselectedIconColor = TrustMeshTheme.colors.textSecondary
                            )
                        )
                    }
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = Screen.Splash,
                modifier = Modifier.padding(innerPadding)
            ) {
                composable<Screen.Splash> {
                    SplashScreen(
                        isUserLoggedIn = isUserLoggedIn,
                        onNavigateToOnboarding = { navController.navigate(Screen.Onboarding) { popUpTo(Screen.Splash) { inclusive = true } } },
                        onNavigateToDashboard = { navController.navigate(Screen.Dashboard) { popUpTo(Screen.Splash) { inclusive = true } } }
                    )
                }

                composable<Screen.Onboarding> {
                    OnboardingScreen(
                        onNavigateToAuth = { navController.navigate(Screen.Auth) { popUpTo(Screen.Onboarding) { inclusive = true } } }
                    )
                }

                 composable<Screen.Auth> {
                    val loading by authViewModel.loading.collectAsState()
                    val error by authViewModel.error.collectAsState()
                    AuthScreen(
                        loading = loading,
                        errorMessage = error,
                        onSignUp = { email, pass, name ->
                            authViewModel.signup(email, pass, name) {
                                navController.navigate(Screen.Dashboard) { popUpTo(Screen.Auth) { inclusive = true } }
                            }
                        },
                        onLogIn = { email, pass ->
                            authViewModel.login(email, pass) {
                                navController.navigate(Screen.Dashboard) { popUpTo(Screen.Auth) { inclusive = true } }
                            }
                        },
                        onGoogleSignIn = { idToken ->
                            authViewModel.loginWithGoogle(idToken) {
                                navController.navigate(Screen.Dashboard) { popUpTo(Screen.Auth) { inclusive = true } }
                            }
                        },
                        onClearError = { authViewModel.clearError() }
                    )
                }

                composable<Screen.Dashboard> {
                    val totalExposure by dashboardViewModel.totalExposure.collectAsState()
                    val anomalyAlert by dashboardViewModel.anomalyAlert.collectAsState()
                    val recentTransactions by dashboardViewModel.transactions.collectAsState()
                    val agentsList by dashboardViewModel.agents.collectAsState()

                    DashboardScreen(
                        totalExposure = totalExposure,
                        anomalyAlert = anomalyAlert,
                        recentTransactions = recentTransactions,
                        agentsList = agentsList,
                        onNavigateToAgentDetail = { id ->
                            navController.navigate(Screen.AgentDetail(id))
                        },
                        onNavigateToAccounts = { navController.navigate(Screen.FinancialAccounts) },
                        onNavigateToEscrow = { navController.navigate(Screen.EscrowApprovals) },
                        onNavigateToLedger = { navController.navigate(Screen.AlignmentLedger) },
                        onRefresh = { dashboardViewModel.syncDashboardData() }
                    )
                }

                composable<Screen.AgentsList> {
                    val agentsList by agentsViewModel.agents.collectAsState()
                    AgentsScreen(
                        agents = agentsList,
                        onNavigateToCreateAgent = { navController.navigate(Screen.CreateAgent) },
                        onNavigateToAgentDetail = { id ->
                            navController.navigate(Screen.AgentDetail(id))
                        }
                    )
                }

                composable<Screen.AgentDetail> { backStackEntry ->
                    val route: Screen.AgentDetail = backStackEntry.toRoute()
                    LaunchedEffect(route.agentId) {
                        agentsViewModel.selectAgent(route.agentId)
                    }

                    val agent by agentsViewModel.selectedAgent.collectAsState()
                    val transactions by agentsViewModel.selectedAgentTransactions.collectAsState()
                    val isSimulating by agentsViewModel.isSimulating.collectAsState()
                    
                    val breakdown = remember(agent, transactions) {
                        agent?.let { agentsViewModel.getTrustBreakdown(it, transactions) } ?: emptyMap()
                    }

                    AgentDetailScreen(
                        agent = agent,
                        transactions = transactions,
                        trustBreakdown = breakdown,
                        isSimulating = isSimulating,
                        onStartSimulation = { agent?.let { agentsViewModel.startSimulation(it) } },
                        onStopSimulation = { agentsViewModel.stopSimulation() },
                        onBack = { navController.popBackStack() },
                        onUpdateEnvelope = { limit, window ->
                            agent?.let { agentsViewModel.updateEnvelope(it.id, limit, window) }
                        },
                        onUpdateStatus = { status ->
                            agent?.let { agentsViewModel.updateStatus(it.id, status) }
                        },
                        onTriggerBiometrics = { onSuccess ->
                            triggerBiometricAuthentication(onSuccess)
                        }
                    )
                }

                composable<Screen.CreateAgent> {
                    CreateAgentWizard(
                        onBack = { navController.popBackStack() },
                        onCreateAgent = { name, intent, categories, limit, window, rules ->
                            agentsViewModel.createAgent(name, intent, categories, limit, window, rules) {
                                navController.popBackStack()
                            }
                        }
                    )
                }

                composable<Screen.TransactionsFeed> {
                    val txList by transactionsViewModel.transactions.collectAsState()
                    TransactionsScreen(
                        transactions = txList,
                        onRefresh = { transactionsViewModel.syncData() }
                    )
                }

                composable<Screen.EscrowApprovals> {
                    val escrowItems by transactionsViewModel.escrowItems.collectAsState()
                    EscrowScreen(
                        escrowItems = escrowItems,
                        onApprove = { id -> transactionsViewModel.approveEscrow(id) },
                        onDeny = { id -> transactionsViewModel.denyEscrow(id) },
                        onTriggerBiometrics = { onSuccess ->
                            triggerBiometricAuthentication(onSuccess)
                        }
                    )
                }

                composable<Screen.FinancialAccounts> {
                    val accountsList by accountsViewModel.accounts.collectAsState()
                    val agentsList by dashboardViewModel.agents.collectAsState()
                    val totalLimit = remember(agentsList) {
                        agentsList.filter { it.status == AgentStatus.ACTIVE }.sumOf { it.spendEnvelope.amountLimit }
                    }

                    LinkedAccountsScreen(
                        accounts = accountsList,
                        totalAgentLimit = totalLimit,
                        onLinkBank = {
                            accountsViewModel.startPlaidFlow { mockToken ->
                                // Plaid Sandbox Mock Linking
                                accountsViewModel.completePlaidFlow(mockToken)
                            }
                        },
                        onRefresh = { accountsViewModel.syncAccounts() }
                    )
                }

                composable<Screen.AlignmentLedger> {
                    val entries by ledgerViewModel.ledgerEntries.collectAsState()
                    val isChainValid by ledgerViewModel.isChainValid.collectAsState()

                    LedgerScreen(
                        entries = entries,
                        isChainValid = isChainValid,
                        onVerifyChain = { ledgerViewModel.verifyChain() }
                    )
                }

                composable<Screen.Settings> {
                    val isBiometricEnabled = user?.biometricEnabled == true
                    val isDark by authViewModel.isDarkTheme.collectAsState()
                    SettingsScreen(
                        biometricsEnabled = isBiometricEnabled,
                        onBiometricsChange = { enabled -> authViewModel.setBiometrics(enabled) },
                        isDarkTheme = isDark,
                        onThemeChange = { authViewModel.toggleTheme() },
                        onNavigateToProfileSecurity = { navController.navigate(Screen.ProfileSecurity) },
                        onExportData = { format ->
                            Toast.makeText(this@MainActivity, "Data exported in $format format via SAF.", Toast.LENGTH_LONG).show()
                        },
                        onDeleteAccount = {},
                        onLogout = {
                            authViewModel.logout {
                                navController.navigate(Screen.Auth) { popUpTo(Screen.Dashboard) { inclusive = true } }
                            }
                        }
                    )
                }

                composable<Screen.ProfileSecurity> {
                    LaunchedEffect(Unit) {
                        authViewModel.fetchSessions()
                    }
                    val sessionsList by authViewModel.sessions.collectAsState()
                    ProfileSecurityScreen(
                        sessionsList = sessionsList,
                        onBack = { navController.popBackStack() },
                        onRevokeSession = { sessionId -> authViewModel.revokeSession(sessionId) }
                    )
                }
            }
        }
    }

    private fun triggerBiometricAuthentication(onSuccess: () -> Unit) {
        val executor = ContextCompat.getMainExecutor(this)
        val biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    onSuccess()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    Toast.makeText(this@MainActivity, "Verification error: $errString", Toast.LENGTH_SHORT).show()
                }
            })

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Identity Authorization Required")
            .setSubtitle("Confirm identity using biometric checks or passcode PIN.")
            .setAllowedAuthenticators(androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG or androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL)
            .build()

        biometricPrompt.authenticate(promptInfo)
    }
}
