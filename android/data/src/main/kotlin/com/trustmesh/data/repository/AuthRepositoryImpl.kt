package com.trustmesh.data.repository

import com.trustmesh.data.local.*
import com.trustmesh.data.remote.*
import com.trustmesh.domain.model.User
import com.trustmesh.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val userDao: UserDao,
    private val api: TrustMeshApi,
    private val prefs: AuthPreferences
) : AuthRepository {

    override fun getSessionUser(): Flow<User?> {
        return userDao.getUser().map { it?.toDomain() }
    }

    override suspend fun signup(email: String, password: String, displayName: String): Result<User> {
        return try {
            val response = api.signup(SignupRequest(email, password, displayName))
            if (response.isSuccessful && response.body() != null) {
                val data = response.body()!!
                prefs.saveTokens(data.token, data.refreshToken)
                val user = User(
                    id = data.user.id,
                    email = data.user.email,
                    displayName = data.user.displayName,
                    biometricEnabled = data.user.biometricEnabled,
                    createdAt = data.user.createdAt
                )
                userDao.insertUser(UserEntity.fromDomain(user))
                Result.success(user)
            } else {
                Result.failure(Exception(response.message().ifEmpty { "Signup failed" }))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun login(email: String, password: String): Result<User> {
        return try {
            val response = api.login(AuthRequest(email, password))
            if (response.isSuccessful && response.body() != null) {
                val data = response.body()!!
                prefs.saveTokens(data.token, data.refreshToken)
                val user = User(
                    id = data.user.id,
                    email = data.user.email,
                    displayName = data.user.displayName,
                    biometricEnabled = data.user.biometricEnabled,
                    createdAt = data.user.createdAt
                )
                userDao.insertUser(UserEntity.fromDomain(user))
                Result.success(user)
            } else {
                Result.failure(Exception(response.message().ifEmpty { "Login failed" }))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun logout(): Result<Unit> {
        return try {
            api.logout()
            prefs.clearTokens()
            userDao.clearUser()
            Result.success(Unit)
        } catch (e: Exception) {
            prefs.clearTokens()
            userDao.clearUser()
            Result.success(Unit)
        }
    }

    override suspend fun setBiometricEnabled(enabled: Boolean): Result<Unit> {
        return try {
            api.updateBiometrics(enabled)
            val currentUser = userDao.getUser().firstOrNull()
            if (currentUser != null) {
                userDao.insertUser(currentUser.copy(biometricEnabled = enabled))
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getActiveSessions(): Result<List<String>> {
        return try {
            val response = api.getActiveSessions()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.sessions)
            } else {
                Result.failure(Exception("Failed to fetch active sessions"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun revokeSession(sessionId: String): Result<Unit> {
        return try {
            val response = api.revokeSession(sessionId)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to revoke session"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun googleLogin(idToken: String): Result<User> {
        return try {
            val response = api.googleLogin(GoogleAuthRequest(idToken))
            if (response.isSuccessful && response.body() != null) {
                val data = response.body()!!
                prefs.saveTokens(data.token, data.refreshToken)
                val user = User(
                    id = data.user.id,
                    email = data.user.email,
                    displayName = data.user.displayName,
                    biometricEnabled = data.user.biometricEnabled,
                    createdAt = data.user.createdAt
                )
                userDao.insertUser(UserEntity.fromDomain(user))
                Result.success(user)
            } else {
                Result.failure(Exception(response.message().ifEmpty { "Google Sign-In failed" }))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
