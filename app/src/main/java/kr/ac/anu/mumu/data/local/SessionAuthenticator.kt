package kr.ac.anu.mumu.data.local

import kr.ac.anu.mumu.data.datasource.AuthService
import kr.ac.anu.mumu.data.model.TokenRefreshRequest
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import javax.inject.Inject

class SessionAuthenticator @Inject constructor(
    private val sessionManager: SessionManager,
    private val authService: AuthService
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        val previousToken = response.request.header("Authorization")?.removePrefix("Bearer ") ?: return null
        if (responseCount(response) >= 2) return null

        synchronized(this) {
            val currentToken = sessionManager.accessToken ?: return null
            if (currentToken != previousToken) {
                return response.request.newBuilder().header("Authorization", "Bearer $currentToken").build()
            }

            val refreshToken = sessionManager.refreshToken ?: return null
            val result = try {
                authService.refresh(TokenRefreshRequest(refreshToken)).execute()
            } catch (_: Exception) {
                return null
            }
            val body = result.body()
            if (!result.isSuccessful || body?.success != true) {
                if (result.code() in 400..403) sessionManager.clearTokens()
                return null
            }
            if (sessionManager.refreshToken != refreshToken) return null
            val tokens = body.data
            sessionManager.saveTokens(tokens.accessToken, tokens.refreshToken, tokens.tokenType)
            return response.request.newBuilder()
                .header("Authorization", "Bearer ${tokens.accessToken}")
                .build()
        }
    }

    private fun responseCount(response: Response): Int {
        var current: Response? = response
        var count = 0
        while (current != null) {
            count++
            current = current.priorResponse
        }
        return count
    }
}
