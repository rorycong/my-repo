# Acceptance Matrix: Requirements Verification

This document lists every requirement defined in Milestone 2 and verified according to the verification methods in `milestone_3_plan.md`.

## Summary
- **Total Requirements Covered:** 175+
- **Unit & Integration Test Suites:** 32 tests (covering 1,000+ random bill test cases and multi-threaded concurrency)
- **Status:** All tests and verification criteria **PASS**

---

## 1. Users and Identity (USR)

| Requirement ID | Verification Method | Verification Details / Test Name | Result |
|---|---|---|---|
| `USR-1` | Script (M1) | Create Username screen displayed on startup when no identity exists | **PASS** |
| `USR-2` | Script (M1) | Username, server address, and Create button displayed | **PASS** |
| `USR-3` | Automated Test | `UserRequirementsTest#USR_3_rejects_short_name`, `USR_3_rejects_long_name`, `USR_3_accepts_valid_length` | **PASS** |
| `USR-4` | Automated Test | `UserRequirementsTest#USR_4_rejects_invalid_chars`, `USR_4_accepts_valid_chars` | **PASS** |
| `USR-5` | Automated Test | `UserRequirementsTest#USR_5_rejects_without_server_call` | **PASS** |
| `USR-6` | Automated Test | `UserRequirementsTest#USR_6_rejects_case_insensitive_duplicate` | **PASS** |
| `USR-7` | Automated Test | `UserRequirementsTest#USR_7_preserves_display_name_casing` | **PASS** |
| `USR-8` | Script (M1) | Create button enabled only when username is valid and server reachable | **PASS** |
| `USR-9` | Review (R3) | Code analysis confirms no endpoint or operation allows changing username | **PASS** |
| `USR-10` | Automated Test | `UserRequirementsTest#USR_10_generates_tokens_at_least_32_chars_and_unique` | **PASS** |
| `USR-11` | Automated Test | `UserRequirementsTest#USR_11_12_16_session_model_saves_and_handles_corrupt_config` | **PASS** |
| `USR-12` | Automated Test | `UserRequirementsTest#USR_11_12_16_session_model_saves_and_handles_corrupt_config` | **PASS** |
| `USR-13` | Script (M1) | Local session file loaded automatically on startup | **PASS** |
| `USR-14` | Automated Test & Review (R2) | `UserRequirementsTest#USR_14_and_15_authenticates_credentials` | **PASS** |
| `USR-15` | Automated Test & Review (R2) | `UserRequirementsTest#USR_14_and_15_authenticates_credentials` | **PASS** |
| `USR-16` | Automated Test | Corrupted config file treated as no identity with notice | **PASS** |
| `USR-17` | Script (M1) & Review (R3) | Username remains immutable across sessions | **PASS** |
| `USR-18` | Script (M1) | Client routes to Home view once username created | **PASS** |

---

## 2. Groups and Group Access (GRP)

| Requirement ID | Verification Method | Verification Details / Test Name | Result |
|---|---|---|---|
| `GRP-1` to `GRP-5` | Script (M2) | Home screen displays group list and Create/Join actions | **PASS** |
| `GRP-6` to `GRP-8` | Automated Test | `GroupAndOwnerRequirementsTest#GRP_6_8_group_names_validation` | **PASS** |
| `GRP-9` to `GRP-12` | Automated Test & Review (R2) | `GroupAndOwnerRequirementsTest#GRP_9_12_code_generator_properties` | **PASS** |
| `GRP-13` | Script (M2) | Group creator becomes owner and first member in member list order | **PASS** |
| `GRP-14` | Automated Test | `GroupAndOwnerRequirementsTest#GRP_14_18_20_access_and_code_visibility` | **PASS** |
| `GRP-15` to `GRP-17` | Script (M2) | Non-owner views group details without seeing group code | **PASS** |
| `GRP-18` | Automated Test & Review (R2) | Non-owner snapshot contains null group code | **PASS** |
| `GRP-19` | Script (M2) | Group screen displays group name and members | **PASS** |
| `GRP-20` | Automated Test | Group code comparison is case-insensitive | **PASS** |
| `GRP-21` to `GRP-25` | Script (M2) | Joining, code prompting, and error states | **PASS** |

---

## 3. Group Owner (OWN)

| Requirement ID | Verification Method | Verification Details / Test Name | Result |
|---|---|---|---|
| `OWN-1` | Automated Test | `GroupAndOwnerRequirementsTest#OWN_1_19_20_owner_leaves_promotes_earliest_remaining_member` | **PASS** |
| `OWN-2` to `OWN-3` | Script (M3) & Automated Test | Owner labeled with "Owner" badge; join code required | **PASS** |
| `OWN-4` to `OWN-9` | Automated Test | `GroupAndOwnerRequirementsTest#OWN_4_to_18_owner_controls_and_permissions` | **PASS** |
| `OWN-10` to `OWN-14` | Script (M3) | Owner controls visible and enabled only for group owner | **PASS** |
| `OWN-15` to `OWN-18` | Automated Test | Old code invalid after new code generation; non-owners rejected | **PASS** |
| `OWN-19` to `OWN-20` | Automated Test & Review (R4) | Earliest remaining member promoted atomically on owner leave | **PASS** |
| `OWN-21` | Script (M3) | Cancel all bills confirmation and execution | **PASS** |

---

## 4. Members (MEM)

| Requirement ID | Verification Method | Verification Details / Test Name | Result |
|---|---|---|---|
| `MEM-1` to `MEM-4` | Script (M2) | Member list displays names, owner marker, and "you" marker | **PASS** |
| `MEM-5` to `MEM-15` | Automated Test | `MemberRequirementsTest#MEM_5_to_15_joining_and_names` | **PASS** |
| `MEM-16` to `MEM-19` | Script (M2, M7) | Real-time member updates across clients | **PASS** |
| `MEM-20` to `MEM-24` | Automated Test | `MemberRequirementsTest#MEM_20_to_31_leaving_and_last_member_deletes_group` | **PASS** |
| `MEM-25` | Script (M7) | Former member labeled as "(left)" on bills | **PASS** |
| `MEM-26` to `MEM-29` | Automated Test | Last member leaving cascades delete to all bills and members | **PASS** |
| `MEM-30` to `MEM-31` | Script (M7) & Review (R3) | No remove-member endpoint exists; leave is self-initiated | **PASS** |

---

## 5. Bills and Splitting (BIL, SPL)

| Requirement ID | Verification Method | Verification Details / Test Name | Result |
|---|---|---|---|
| `BIL-1` to `BIL-7` | Script (M4) | Create Bill form input validation and UI feedback | **PASS** |
| `BIL-8` | Automated Test | `BillAndSplitRequirementsTest#BIL_8_30_to_35_bill_creation_and_validation` | **PASS** |
| `BIL-9` | Script (M4) | Form retains values across membership changes | **PASS** |
| `BIL-10` to `BIL-14` | Automated Test | `BillAndSplitRequirementsTest#BIL_10_to_14_amount_parsing` | **PASS** |
| `BIL-15` | Script (M4) | Running weight sum indicator | **PASS** |
| `BIL-16` to `BIL-19` | Automated Test & Proof P2 | `BillAndSplitRequirementsTest#BIL_16_to_19_weights_sum_and_equal_split_P2_proof` | **PASS** |
| `BIL-20` | Automated Test | Exact integer cents parsed without float/double | **PASS** |
| `BIL-21` to `BIL-29` | Script (M4) | Form field validations | **PASS** |
| `BIL-30` to `BIL-35` | Automated Test | Creator share automatically marked confirmed upon creation | **PASS** |
| `BIL-36` to `BIL-37` | Script (M4) & Automated Test | Form clears on success; error leaves form intact | **PASS** |
| `SPL-1` to `SPL-7` | Automated Test | `BillAndSplitRequirementsTest#SPL_1_to_7_shares_calculation_examples` | **PASS** |
| `SPL-4` | Proof P1 & Automated Test (1,000 cases) | `BillAndSplitRequirementsTest#SPL_4_P1_proof_1000_random_bills` | **PASS** |
| `SPL-8` to `SPL-9` | Automated Test | Membership change does not alter shares; zero-weight omitted | **PASS** |

---

## 6. Viewing, Dates, Payments, and Cancelling (BLD, DAT, PAY, CAN)

| Requirement ID | Verification Method | Verification Details / Test Name | Result |
|---|---|---|---|
| `BLD-1` to `BLD-7` | Script (M5) | Bill list row rendering and details selection | **PASS** |
| `BLD-8` to `BLD-10` | Automated Test | `PaymentAndCancelRequirementsTest#BLD_8_to_10_display_formatting` | **PASS** |
| `BLD-11` to `BLD-14` | Script (M5) | Selection persists across updates; scrollable bills area | **PASS** |
| `DAT-1` to `DAT-2` | Script (M5) | Suggested pay-back date display | **PASS** |
| `DAT-3` to `DAT-4` | Automated Test | `PaymentAndCancelRequirementsTest#DAT_3_4_8_dates_and_changing` | **PASS** |
| `DAT-5` to `DAT-7` | Script (M5) | Date picker and date updates | **PASS** |
| `DAT-8` | Automated Test | Non-bill-owner cannot change date | **PASS** |
| `DAT-9` to `DAT-11` | Script (M5) | Date passing has no monetary side effects | **PASS** |
| `PAY-1` to `PAY-8` | Automated Test | `PaymentAndCancelRequirementsTest#PAY_1_to_12_confirming_payment_permissions_and_state` | **PASS** |
| `PAY-9` to `PAY-10` | Script (M6) | Real-time payment confirmation reflection | **PASS** |
| `PAY-11` to `PAY-12` | Review (R3) & Test | No money transfer; payments cannot be un-confirmed | **PASS** |
| `CAN-1` to `CAN-6` | Script (M6) | Confirmation dialog before cancelling bills | **PASS** |
| `CAN-7` to `CAN-24` | Automated Test | `PaymentAndCancelRequirementsTest#CAN_7_to_24_cancelling_bills` | **PASS** |

---

## 7. Outstanding Amounts (OUT)

| Requirement ID | Verification Method | Verification Details / Test Name | Result |
|---|---|---|---|
| `OUT-1` to `OUT-2` | Script (M6) | Member list displays calculated "owes" and "is owed" | **PASS** |
| `OUT-3` to `OUT-6` | Automated Test & Review (R3) | `OutstandingAmountsTest#OUT_3_to_6_outstanding_calculation_properties` | **PASS** |
| `OUT-7` to `OUT-9` | Script (M6, M7) | Balances update dynamically when payments confirmed or member leaves | **PASS** |

---

## 8. Synchronization, Persistence, and General (SYN, PER, GEN)

| Requirement ID | Verification Method | Verification Details / Test Name | Result |
|---|---|---|---|
| `SYN-1` to `SYN-11` | Script (M8) | Periodic background polling updates client data | **PASS** |
| `SYN-12` | Automated Test (50 threads) | `SyncAndPersistenceTest#SYN_12_concurrent_confirmations` | **PASS** |
| `SYN-13` to `SYN-17` | Automated Test & Script (M8) | Graceful handling of group code change or missing group | **PASS** |
| `PER-1` to `PER-8` | Automated Test | `SyncAndPersistenceTest#PER_1_to_8_13_restart_preserves_state_and_deletes_cascade` | **PASS** |
| `PER-9` | Automated Test | Database failure returns failed Result without throwing | **PASS** |
| `PER-10` | Review (R6) | Local storage only holds credentials, address, and group codes | **PASS** |
| `PER-11` to `PER-12` | Review (R3) | All specified operations return Result with reason on failure | **PASS** |
| `PER-13` | Automated Test | Cascade deletion of group, members, and bills in single transaction | **PASS** |
| `GEN-1` | Review (R6) | Java backend; responsive JS/HTML/CSS frontend | **PASS** |
| `GEN-2` to `GEN-3` | Script (M9) | Responsive in modern browsers on desktop and mobile | **PASS** |
| `GEN-4` to `GEN-5` | Script (M8) | Offline banner displayed when server unreachable | **PASS** |
| `GEN-6` to `GEN-8` | Script (M9) | Three screens with required sub-areas and header actions | **PASS** |
| `GEN-9` | Review (R1) | Strict integer arithmetic: zero float/double for money or weights | **PASS** |
| `GEN-10` | Automated Test | `SyncAndPersistenceTest#GEN_10_performance_under_load` (< 1 second) | **PASS** |
| `GEN-11` | Script (M9) | Every button has a visible, readable text label | **PASS** |
| `GEN-12` to `GEN-13` | Automated Test & Review (R5) | Exception safety: no unhandled exception escapes server or client | **PASS** |
| `GEN-14` | Script (M9) | Dismissible notification banners and modal dialogs | **PASS** |
