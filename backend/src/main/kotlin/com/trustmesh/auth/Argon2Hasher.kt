package com.trustmesh.auth

import de.mkammerer.argon2.Argon2Factory

object Argon2Hasher {
    private val argon2 = Argon2Factory.create(Argon2Factory.Argon2Types.ARGON2id)

    fun hash(password: String): String {
        // Section 8: Argon2id hash (10 iterations, 64MB memory, 1 parallel thread)
        return argon2.hash(10, 65536, 1, password.toCharArray())
    }

    fun verify(hash: String, password: String): Boolean {
        return argon2.verify(hash, password.toCharArray())
    }
}
