# TrustMesh Issues and Pull Request Documentation

This document logs the details of the two security and audit issues created for the TrustMesh platform, along with the Pull Request structure that implements their resolution.

---

## Issue #1: Enforce Strict Backend-Side Category Scope Validation for Agent Transactions

### Description
Currently, when an autonomous agent requests transaction approval via `/api/v1/transactions/request`, the backend only validates the transaction amount against the agent's spend envelope limit. It completely bypasses the agent's authorized `categoryScope` (e.g., `"ELECTRONICS,OTHER"`). 

This allows agents to purchase items in unauthorized categories (e.g., `TRAVEL`, `FOOD`) as long as they stay within the total spend envelope limit, creating a severe policy bypassing vulnerability.

### Expected Behavior
1. The backend must parse the `categoryScope` comma-separated string from the `Agents` table.
2. The transaction request must fail validation if the transaction's category (`req.merchantCategory`) is not present in the agent's authorized `categoryScope`.
3. Instead of releasing the transaction directly, the transaction must be placed in escrow with status `"PENDING_CONDITION"`.
4. A corresponding `EscrowItem` must be inserted with `conditionType = "CATEGORY_MISMATCH"` and `conditionThreshold = 0.0`.
5. A ledger audit entry must be appended with action `"Attempted purchase of Category <Category> at <Merchant> of $<Amount> violated category scope constraints"` and outcome `"DRIFT_DETECTED"`.
6. A WebSocket notification `"ESCROW_HOLD|<TransactionId>"` must be broadcast to the client.

---

## Issue #2: Implement Cryptographic Ledger Chain Integrity Audit API Endpoint

### Description
TrustMesh guarantees historical transaction integrity via a cryptographically linked hash ledger. However, verification of the hash chain is currently only implemented client-side in the Android application. There is no automated backend capability or API endpoint to audit the database ledger entries globally for tampering.

If a malicious database administrator or intruder modifies a historical transaction block directly in the database, the server remains unaware.

### Expected Behavior
1. Create a `verifyChain()` routine on the backend that:
   * Queries all `LedgerEntries` chronologically (`orderBy(timestamp to ASC)`).
   * Verifies the cryptographic link: the first block's `previousHash` must be `"GENESIS"`, and each subsequent block's `previousHash` must match the previous block's hash.
   * Recalculates the SHA-256 hash of each block using `$id|$agentId|$timestamp|$statedIntentSnapshot|$actionTaken|$outcome|$previousHash` and compares it to the stored hash.
2. Expose this verification via an authenticated API endpoint `GET /api/v1/ledger/verify`.
3. The endpoint should respond with:
   * `isValid: true` if the chain is uncorrupted.
   * `isValid: false` along with error details specifying the exact block ID where tampering was detected if corrupted.
4. Ensure timestamps used in hashing are truncated to second-precision (`now.withNano(0)`) to prevent fractional-second serialization mismatches between JVM memory and PostgreSQL storage.

---

## Pull Request: Feature - Strict Category Scope Enforcement & Ledger Chain Verification

### Overview
This PR addresses **Issue #1** and **Issue #2** by introducing backend-side validation of category scopes and adding an administrative ledger chain verification API endpoint.

### Proposed Changes
1. **[TransactionRoutes.kt](file:///c:/Users/harsh/Desktop/Hackathon%20Folders/Demo's/backend/src/main/kotlin/com/trustmesh/transaction/TransactionRoutes.kt)**:
   * Parsed the agent's `categoryScope` into a list of allowed categories.
   * Intercepted transaction creation: if the request's category is mismatching, the transaction status is set to `PENDING_CONDITION`, an escrow item of type `CATEGORY_MISMATCH` is created, and the drift is logged in the ledger.
2. **[LedgerService.kt](file:///c:/Users/harsh/Desktop/Hackathon%20Folders/Demo's/backend/src/main/kotlin/com/trustmesh/ledger/LedgerService.kt)**:
   * Truncated timestamps in `appendEntry` using `.withNano(0)` to prevent second-precision mismatch when converting `LocalDateTime` to/from the database.
   * Added `verifyChain(): Pair<Boolean, String>` that traverses all entries, recomputes SHA-256 hashes, and checks the hash linkage.
3. **[LedgerRoutes.kt](file:///c:/Users/harsh/Desktop/Hackathon%20Folders/Demo's/backend/src/main/kotlin/com/trustmesh/ledger/LedgerRoutes.kt)**:
   * Registered `GET /ledger/verify` inside the authenticated JWT routing block.

### Testing Instructions
1. **Category Scope Test**:
   * Deploy the Ktor server and database.
   * Submit a POST request to `/api/v1/transactions/request` with category `FOOD` (which is outside the default agent's scope of `"ELECTRONICS,OTHER"`).
   * Confirm the response status is `202 Accepted` and response contains `"status": "ESCROW_HOLD"`.
   * Verify that `escrow_items` contains a record with `condition_type: "CATEGORY_MISMATCH"`.
2. **Ledger Verification Test**:
   * Authenticate and submit a GET request to `/api/v1/ledger/verify`.
   * Confirm the response returns `{"isValid": true, "message": "Ledger cryptographic integrity verified successfully"}`.
   * Tamper with a ledger action in the database (e.g. `UPDATE ledger_entries SET action_taken = 'Tampered Action' WHERE id = ...`).
   * Call `GET /api/v1/ledger/verify` again. Confirm it returns `{"isValid": false}` and specifies the tampered block ID.
