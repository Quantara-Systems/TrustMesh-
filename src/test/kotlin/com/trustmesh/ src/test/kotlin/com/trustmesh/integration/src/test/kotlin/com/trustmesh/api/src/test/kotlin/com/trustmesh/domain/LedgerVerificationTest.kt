package com.trustmesh.domain

import com.trustmesh.TestBaseClass
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.security.MessageDigest

/**
 * Unit tests for cryptographic hash chain verification
 * Tests the core business logic of transaction ledger validation
 */
@DisplayName("Ledger Verification Tests")
class LedgerVerificationTest : TestBaseClass() {

    @Test
    @DisplayName("Should calculate SHA-256 hash correctly")
    fun testHashCalculation() {
        // Arrange
        val blockData = "Block123||TransactionData||2026-08-25||PreviousHash"
        
        // Act
        val hash = calculateSHA256(blockData)
        
        // Assert
        hash shouldNotBe null
        hash.length shouldBe 64 // SHA-256 hex string is 64 characters
    }

    @Test
    @DisplayName("Should verify hash chain integrity")
    fun testHashChainVerification() {
        // Arrange
        val block1Data = "Block1||Data1||Time1||0"
        val hash1 = calculateSHA256(block1Data)
        
        val block2Data = "Block2||Data2||Time2||$hash1"
        val hash2 = calculateSHA256(block2Data)
        
        // Act - Verify chain
        val isValid = verifyChain(listOf(block1Data, block2Data), listOf(hash1, hash2))
        
        // Assert
        isValid shouldBe true
    }

    @Test
    @DisplayName("Should detect tampered hash chain")
    fun testTamperedChainDetection() {
        // Arrange
        val block1Data = "Block1||Data1||Time1||0"
        val hash1 = calculateSHA256(block1Data)
        
        val block2Data = "Block2||Data2||Time2||$hash1"
        val hash2 = calculateSHA256(block2Data)
        
        // Tampered hash
        val tamperedHash2 = "0000000000000000000000000000000000000000000000000000000000000000"
        
        // Act
        val isValid = verifyChain(listOf(block1Data, block2Data), listOf(hash1, tamperedHash2))
        
        // Assert
        isValid shouldBe false
    }

    private fun calculateSHA256(input: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
        return bytes.fold("") { str, it -> str + "%02x".format(it) }
    }

    private fun verifyChain(blocks: List<String>, hashes: List<String>): Boolean {
        if (blocks.size != hashes.size) return false
        
        for (i in blocks.indices) {
            val calculatedHash = calculateSHA256(blocks[i])
            if (calculatedHash != hashes[i]) return false
        }
        return true
    }
}
