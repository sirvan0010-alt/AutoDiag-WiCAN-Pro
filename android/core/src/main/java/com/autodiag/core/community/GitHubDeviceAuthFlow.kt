package com.autodiag.core.community

/**
 * GitHub Device Flow state model for an Android public client.
 *
 * This module deliberately does not store tokens or perform HTTP. The Android app
 * should keep tokens in platform secure storage and use a GitHub App configured with
 * the minimum repository permission required for community contributions.
 */
sealed interface GitHubDeviceAuthState {
    data object Idle : GitHubDeviceAuthState
    data object RequestingCode : GitHubDeviceAuthState
    data class AwaitingUserAuthorization(
        val userCode: String,
        val verificationUri: String,
        val expiresInSeconds: Long,
        val intervalSeconds: Long
    ) : GitHubDeviceAuthState
    data object Authorizing : GitHubDeviceAuthState
    data class Authorized(val expiresAtEpochSeconds: Long?) : GitHubDeviceAuthState
    data class Failed(val message: String) : GitHubDeviceAuthState
}

data class GitHubDeviceCode(
    val deviceCode: String,
    val userCode: String,
    val verificationUri: String,
    val expiresInSeconds: Long,
    val intervalSeconds: Long
)

data class GitHubAccessToken(
    val token: String,
    val expiresAtEpochSeconds: Long? = null
)

class GitHubDeviceAuthFlow {
    var state: GitHubDeviceAuthState = GitHubDeviceAuthState.Idle
        private set

    fun beginRequest() {
        state = GitHubDeviceAuthState.RequestingCode
    }

    fun codeReceived(code: GitHubDeviceCode) {
        require(code.deviceCode.isNotBlank())
        require(code.userCode.isNotBlank())
        require(code.verificationUri.isNotBlank())
        require(code.expiresInSeconds > 0)
        require(code.intervalSeconds > 0)
        state = GitHubDeviceAuthState.AwaitingUserAuthorization(
            userCode = code.userCode,
            verificationUri = code.verificationUri,
            expiresInSeconds = code.expiresInSeconds,
            intervalSeconds = code.intervalSeconds
        )
    }

    fun beginAuthorization() {
        check(state is GitHubDeviceAuthState.AwaitingUserAuthorization)
        state = GitHubDeviceAuthState.Authorizing
    }

    fun authorized(token: GitHubAccessToken) {
        require(token.token.isNotBlank())
        state = GitHubDeviceAuthState.Authorized(token.expiresAtEpochSeconds)
    }

    fun fail(message: String) {
        state = GitHubDeviceAuthState.Failed(message.ifBlank { "GitHub authorization failed" })
    }

    fun reset() {
        state = GitHubDeviceAuthState.Idle
    }
}
