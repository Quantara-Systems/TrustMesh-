package com.trustmesh.presentation.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed interface Screen {
    @Serializable
    data object Splash : Screen

    @Serializable
    data object Onboarding : Screen

    @Serializable
    data object Auth : Screen

    @Serializable
    data object Dashboard : Screen

    @Serializable
    data object AgentsList : Screen

    @Serializable
    data class AgentDetail(val agentId: String) : Screen

    @Serializable
    data object CreateAgent : Screen

    @Serializable
    data object TransactionsFeed : Screen

    @Serializable
    data object EscrowApprovals : Screen

    @Serializable
    data object FinancialAccounts : Screen

    @Serializable
    data object MerchantExplorer : Screen

    @Serializable
    data object AlignmentLedger : Screen

    @Serializable
    data object Notifications : Screen

    @Serializable
    data object Settings : Screen

    @Serializable
    data object ProfileSecurity : Screen
}
