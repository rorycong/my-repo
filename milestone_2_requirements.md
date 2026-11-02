# Shared Expense Splitter: Requirements

**Conventions**
- "Cents" means integer amounts in hundredths of a dollar. All money is stored and calculated in cents.
- "Member list order" means the order in which members were added to the group.
- A **balance** is stored in cents. Positive means the group owes the member; negative means the member owes the group.
- "Main screen" means the screen containing the Members and Balances area, the Expense Log, and the Add Expense form.

**Sections**
1. [Group and Members](#1-group-and-members) ← *Design: Members and balances*
2. [Balance Display](#2-balance-display) ← *Design: Members and balances*
3. [Add Expense Form](#3-add-expense-form) ← *Design: Add expense form*
4. [Expense Splitting and Rounding](#4-expense-splitting-and-rounding) ← *Design: Add expense form; Amounts*
5. [Balance Computation](#5-balance-computation) ← *Design: Add expense form; Settle up*
6. [Expense Log](#6-expense-log) ← *Design: Expense log*
7. [Deleting Expenses](#7-deleting-expenses) ← *Design: Expense log*
8. [Settle Up Screen](#8-settle-up-screen) ← *Design: Settle up screen*
9. [Persistence](#9-persistence) ← *Design: Saving and loading*
10. [Reset](#10-reset) ← *Design: Reset*
11. [General and Layout](#11-general-and-layout) ← *Design: Interface*

---

## 1. Group and Members

*Design reference: Members and balances.*

| ID | Requirement |
|----|-------------|
| MEM-1 | The application shall manage exactly one group at a time. |
| MEM-2 | The group shall be a named list of members. |
| MEM-3 | The main screen shall provide a text field for entering a new member's name and an Add Member button. |
| MEM-4 | When the user activates the Add Member button with a valid name, the system shall append a member with that name to the end of the member list. |
| MEM-5 | The system shall strip leading and trailing whitespace from a member name before validating or storing it. |
| MEM-6 | The system shall reject a member name that is empty after whitespace is stripped. |
| MEM-7 | The system shall reject a member name that matches an existing member's name, compared case-insensitively. |
| MEM-8 | When a member name is rejected, the system shall display an error message stating the reason (empty or duplicate) and shall not change the member list. |
| MEM-9 | After a member is added successfully, the system shall clear the name text field. |
| MEM-10 | A newly added member shall have a balance of 0 cents. |
| MEM-11 | The main screen shall provide a way to remove a selected member. |
| MEM-12 | The system shall refuse to remove a member whose balance is not 0 and shall display an error message stating that the balance must be zero. |
| MEM-13 | When a member with a balance of 0 is removed, the system shall delete that member from the member list. |
| MEM-14 | The system shall refuse to remove a member who is the payer or a participant of any expense currently in the expense log, and shall display an error message stating the reason. |
| MEM-15 | Removing a member shall preserve the relative order of the remaining members. |

## 2. Balance Display

*Design reference: Members and balances.*

| ID | Requirement |
|----|-------------|
| BAL-1 | The Members and Balances area shall list every member in member list order. |
| BAL-2 | Each row shall show the member's name and that member's balance. |
| BAL-3 | Balances shall be displayed in dollars with exactly two decimal places and a dollar sign (for example, `$12.50`). |
| BAL-4 | A balance greater than 0 shall be displayed in green. |
| BAL-5 | A balance less than 0 shall be displayed in red. |
| BAL-6 | A balance equal to 0 shall be displayed in gray. |
| BAL-7 | A negative balance shall be displayed with a leading minus sign before the dollar sign (for example, `-$12.50`). |
| BAL-8 | The displayed balances shall update immediately whenever an expense is added, an expense is deleted, or a payment is marked as paid. |
| BAL-9 | The Members and Balances area shall be scrollable when the member rows exceed the visible height. |

## 3. Add Expense Form

*Design reference: Add expense form.*

| ID | Requirement |
|----|-------------|
| FRM-1 | The Add Expense form shall contain a text field for the description. |
| FRM-2 | The Add Expense form shall contain a text field for the amount in dollars. |
| FRM-3 | The Add Expense form shall contain a drop-down menu for the payer whose options are exactly the current members, in member list order. |
| FRM-4 | The payer drop-down shall have no member selected initially and after the form is cleared. |
| FRM-5 | The Add Expense form shall contain one checkbox per current member to select participants. |
| FRM-6 | Each participant checkbox shall have an adjacent weight field. |
| FRM-7 | A weight field shall default to the value 1. |
| FRM-8 | The system shall accept as a weight only a positive whole number. |
| FRM-9 | The system shall show an error indication on a weight field containing a value that is not a positive whole number and shall treat the form as invalid until corrected. |
| FRM-10 | The system shall accept an amount entered as a non-negative decimal number with at most two digits after the decimal point. |
| FRM-11 | The system shall treat an amount that cannot be parsed under FRM-10 as invalid. |
| FRM-12 | The system shall convert a valid amount to whole cents by multiplying by 100 using exact decimal arithmetic (not binary floating point). |
| FRM-13 | The Add Expense button shall be disabled while the description is empty after whitespace is stripped. |
| FRM-14 | The Add Expense button shall be disabled while the amount is invalid or not greater than 0. |
| FRM-15 | The Add Expense button shall be disabled while no payer is selected. |
| FRM-16 | The Add Expense button shall be disabled while no participant checkbox is checked. |
| FRM-17 | The Add Expense button shall be disabled while any checked participant's weight is not a positive whole number. |
| FRM-18 | The Add Expense button shall be enabled if and only if all conditions in FRM-13 through FRM-17 are satisfied. |
| FRM-19 | The system shall re-evaluate the enabled state of the Add Expense button after every change to the description, amount, payer, participant checkboxes, or weight fields. |
| FRM-20 | The payer shall be allowed to be, or not be, a checked participant. |
| FRM-21 | Unchecking a participant shall exclude that member's weight from the split without clearing the weight field's value. |
| FRM-22 | When the member list changes, the form's payer options and participant rows shall update to match the current members. |
| FRM-23 | When the user activates the enabled Add Expense button, the system shall record the expense (see section 4). |
| FRM-24 | After an expense is recorded, the system shall clear the description field and the amount field, reset the payer drop-down to no selection, uncheck all participants, and reset all weight fields to 1. |

## 4. Expense Splitting and Rounding

*Design reference: Add expense form; Amounts are stored in whole cents.*

| ID | Requirement |
|----|-------------|
| SPL-1 | Each recorded expense shall store a description, an amount in cents, a payer, a list of participants with their weights, and a timestamp of when it was recorded. |
| SPL-2 | The description stored shall be the entered description with leading and trailing whitespace removed. |
| SPL-3 | The system shall compute the total weight as the sum of the weights of all checked participants. |
| SPL-4 | For each participant, the system shall compute a base share as the floor of (amount in cents × participant weight) ÷ total weight, using integer arithmetic. |
| SPL-5 | The system shall compute the leftover cents as the amount minus the sum of all base shares. |
| SPL-6 | The leftover cents shall be distributed one cent at a time to participants in member list order, beginning with the first participant in that order, each receiving at most one extra cent. |
| SPL-7 | The final shares of an expense shall sum to exactly the expense amount in cents. |
| SPL-8 | When there is exactly one participant, that participant's share shall equal the full amount. |
| SPL-9 | When all participants have weight 1, the shares shall differ from each other by at most 1 cent. |
| SPL-10 | A participant with weight 2 shall receive a base share that is exactly twice the base share of a participant with weight 1 when the division is exact. |
| SPL-11 | The computed shares of an expense shall be stored with the expense so that later deletion reverses exactly the shares originally applied. |

## 5. Balance Computation

*Design reference: Add expense form; Settle up screen.*

| ID | Requirement |
|----|-------------|
| CMP-1 | When an expense is recorded, the system shall increase the payer's balance by the full amount of the expense. |
| CMP-2 | When an expense is recorded, the system shall decrease each participant's balance by that participant's share. |
| CMP-3 | When the payer is also a participant, the payer's net balance change shall equal the amount minus the payer's own share. |
| CMP-4 | The sum of all member balances shall equal 0 at all times. |
| CMP-5 | A member's balance shall equal the sum of the effects (CMP-1, CMP-2) of all recorded expenses plus the effects of all recorded payments (SET-12, SET-13). |
| CMP-6 | The system shall derive balances from the stored expense and payment records so that balances are identical before and after a save and load. |

## 6. Expense Log

*Design reference: Expense log.*

| ID | Requirement |
|----|-------------|
| LOG-1 | The main screen shall contain an Expense Log below the Members and Balances area. |
| LOG-2 | The Expense Log shall list all recorded expenses, ordered from newest to oldest by time recorded. |
| LOG-3 | The Expense Log shall be vertically scrollable when its rows exceed the visible height. |
| LOG-4 | Each row shall display the expense description. |
| LOG-5 | Each row shall display the expense amount in dollars with two decimal places. |
| LOG-6 | Each row shall display the payer's name. |
| LOG-7 | Each row shall display the names of all participants. |
| LOG-8 | A newly recorded expense shall appear at the top of the Expense Log immediately after it is recorded. |
| LOG-9 | The Expense Log shall allow one row to be selected at a time. |
| LOG-10 | The Expense Log shall display an empty-state message (for example, "No expenses yet") when no expenses exist. |
| LOG-11 | Recorded payments (SET-12) shall not appear in the Expense Log. |

## 7. Deleting Expenses

*Design reference: Expense log.*

| ID | Requirement |
|----|-------------|
| DEL-1 | The Expense Log area shall contain a Delete button. |
| DEL-2 | The Delete button shall be disabled while no expense row is selected. |
| DEL-3 | When the user activates the enabled Delete button, the system shall remove the selected expense from the expense log. |
| DEL-4 | When an expense is deleted, the system shall decrease the payer's balance by the full amount of the expense. |
| DEL-5 | When an expense is deleted, the system shall increase each participant's balance by the share stored for that expense (SPL-11). |
| DEL-6 | After a deletion, the displayed balances shall update immediately. |
| DEL-7 | After a deletion, no row shall be selected. |
| DEL-8 | Deleting an expense shall not delete or alter any recorded payment. |

## 8. Settle Up Screen

*Design reference: Settle up screen.*

| ID | Requirement |
|----|-------------|
| SET-1 | The main screen shall contain a Settle Up button. |
| SET-2 | When the user activates the Settle Up button, the system shall open a second screen showing suggested payments. |
| SET-3 | The Settle Up screen shall provide a way to return to the main screen. |
| SET-4 | The system shall generate suggested payments by repeating the following step until every balance is 0: match the member with the most negative balance (the largest debtor) with the member with the most positive balance (the largest creditor). |
| SET-5 | Each suggested payment shall have an amount equal to the smaller of the debtor's absolute balance and the creditor's balance, in cents. |
| SET-6 | After each suggested payment is generated, the system shall apply it to working copies of the balances before choosing the next match. |
| SET-7 | When two or more members tie for the largest debt or largest credit, the system shall choose the tied member who appears earliest in member list order. |
| SET-8 | Suggested payments shall be generated in the order they were produced by SET-4. |
| SET-9 | Each suggested payment shall be displayed as text in the form "`<payer name>` pays `<payee name>` `$<amount>`" with the amount having two decimal places (for example, "Priya pays Sam $12.50"). |
| SET-10 | The number of suggested payments shall be at most one fewer than the number of members with a nonzero balance. |
| SET-11 | Each suggested payment shall have an adjacent Mark as Paid button. |
| SET-12 | When the user activates a Mark as Paid button, the system shall record a payment containing the payer, the payee, the amount in cents, and a timestamp. |
| SET-13 | When a payment is recorded, the system shall increase the paying member's balance by the payment amount and decrease the receiving member's balance by the payment amount. |
| SET-14 | After a payment is recorded, the system shall regenerate the list of suggested payments from the updated balances. |
| SET-15 | After a payment is recorded, the displayed balances on the main screen shall reflect the payment. |
| SET-16 | When every member's balance is 0, the Settle Up screen shall display the message "All settled up" and shall display no suggested payments. |
| SET-17 | When there are no members or no expenses, the Settle Up screen shall display the message "All settled up". |
| SET-18 | Suggested payments shall not be written to the save file; only recorded payments shall be saved. |

## 9. Persistence

*Design reference: Saving and loading.*

| ID | Requirement |
|----|-------------|
| PER-1 | The system shall store the group name, members, expenses, and payments in a single local file. |
| PER-2 | The system shall write the save file after every change to the members, expenses, or payments. |
| PER-3 | The system shall write the save file in a human-readable text format (for example, JSON). |
| PER-4 | The save file shall store all monetary values as integer cents. |
| PER-5 | The save file shall preserve member list order. |
| PER-6 | The save file shall preserve the recorded order of expenses and payments. |
| PER-7 | The save file shall store each expense's computed shares (SPL-11). |
| PER-8 | When the application starts and a save file exists, the system shall load the group, expenses, and payments from it before displaying the main screen. |
| PER-9 | When the application starts and no save file exists, the system shall start with an empty group, no expenses, and no payments. |
| PER-10 | After a save and subsequent load, the members, balances, expenses, and payments shall be identical to those before the save. |
| PER-11 | The system shall write the save file to a temporary file first and then replace the existing save file, so that a failure during writing does not corrupt the previous save. |
| PER-12 | When the save file cannot be parsed, the system shall display an error message, shall not overwrite the unreadable file without the user's confirmation, and shall not crash. |
| PER-13 | When the save file cannot be written, the system shall display an error message and shall continue operating with in-memory data. |
| PER-14 | The save file's location shall be a fixed path in the user's home directory or application data directory. |

## 10. Reset

*Design reference: Reset.*

| ID | Requirement |
|----|-------------|
| RST-1 | The main screen shall contain a Reset button. |
| RST-2 | When the user activates the Reset button, the system shall display a confirmation dialog before changing any data. |
| RST-3 | The confirmation dialog shall state that all members, expenses, and payments will be permanently removed. |
| RST-4 | The confirmation dialog shall offer a Confirm option and a Cancel option. |
| RST-5 | When the user cancels the confirmation dialog, the system shall make no change to any data. |
| RST-6 | When the user confirms the dialog, the system shall remove all members, expenses, and payments. |
| RST-7 | When the user confirms the dialog, the system shall update the save file to reflect the empty state. |
| RST-8 | After a confirmed reset, the main screen shall show an empty member list, an empty Expense Log, and cleared form fields. |

## 11. General and Layout

*Design reference: Interface.*

| ID | Requirement |
|----|-------------|
| GEN-1 | The application shall be implemented in Java. |
| GEN-2 | The application shall have one main screen containing three areas: Members and Balances, Expense Log, and Add Expense form. |
| GEN-3 | The Members and Balances area shall appear above the Expense Log. |
| GEN-4 | The application shall have one additional screen, the Settle Up screen. |
| GEN-5 | The application shall run without requiring a network connection. |
| GEN-6 | The application shall not require the user to create an account. |
| GEN-7 | The system shall never display or store a monetary amount using floating-point arithmetic for calculations. |
| GEN-8 | Each user action in this document shall complete and update the display in under 1 second for groups of up to 20 members and 1,000 expenses. |
| GEN-9 | Every button shall have a visible text label. |
| GEN-10 | The system shall not terminate with an unhandled exception as a result of any user input permitted by the interface. |

---

## Assumptions to Confirm (resolve before the final sprint)

These are places where the design specification is silent or ambiguous. The requirements above encode the assumption listed; change both if you decide differently.

1. **Single group.** The design describes "a group" but no way to create, switch, or rename groups. Assumed one group (MEM-1). The group name has no editing requirement yet.
2. **Member removal.** The design says a member "cannot be removed while their balance is nonzero," which implies removal exists but never specifies the control. MEM-11 adds one. MEM-14 (blocking removal of members referenced by existing expenses) is an added requirement to keep the log consistent.
3. **Desktop vs. touch wording.** The design says "tapping" and "Players." These requirements use "activates" and "user."
4. **Weights.** The design does not say whether weights may be fractional or zero. Assumed positive whole numbers (FRM-8).
5. **Case-insensitive uniqueness.** The design says names must be unique but not how compared. Assumed case-insensitive after trimming (MEM-7).
6. **Payments after expense deletion.** Deleting an expense does not undo payments (DEL-8), so balances may remain nonzero or reverse sign afterward.
7. **No undo for payments.** The design has no way to reverse a Mark as Paid action; none is required.
8. **Tie-breaking in settle up.** The design does not specify ties; SET-7 uses member list order.
9. **Remainder distribution.** The design says "in the order they appear in the member list." SPL-6 interprets this as only among participants of that expense.
10. **Corrupt save file handling.** Not in the design; PER-12 and PER-13 are added as obvious error cases.
