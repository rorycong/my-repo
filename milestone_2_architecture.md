# Shared Expense Splitter: Description of All Components



## Overview

The system uses a **client-server** architecture. Each roommate runs the **client** (web based interface). Its views display data and forward user actions to client controllers. Client controllers change client models and talk to the server through one component, ServerConnection. A single **server** (Java) holds all data in a database. It has **no views**: its controllers answer requests with data, and its models read and write the database. The only things that cross the network are requests (an operation name plus its arguments) and a Result answer. The only description of a group that crosses the network is a GroupSnapshot.


# Models

## Shared models

These are used by both the client and the server. They are defined once. The data classes are passive; the three utility classes are stateless.

### Credentials
- This component is a model (message data) holding a username and the secret login token that proves the sender owns it (USR-10, USR-14).
- It resides on both the client and the server.
- It does not initiate communication:
  - The **SessionModel** (client) stores the username and token and hands them out as Credentials.
  - The **ServerConnection** (client) sends Credentials with every request except `createUser`.
  - The **AuthGuard** (server) reads Credentials to authenticate each request.
  - The **UserApiController** (server) returns Credentials in the `Result` of `createUser`. That is the only response that ever contains a login token (USR-15).

### GroupAccess
- This component is a model (message data) bundling what the server needs to authorize a request about one group: the caller's Credentials, the `groupId`, and the group code the caller supplies (GRP-18).
- It resides on both the client and the server. The server builds it from the fields of each incoming group request.
- It does not initiate communication. The **GroupApiController** and **BillApiController** receive it as the first argument of every group operation and pass it to the **AuthGuard**'s `authorizeGroupAccess`.

### Result
- This component is a model (message data) describing the outcome of every server operation: a success flag, a human-readable failure reason when it failed, and an optional payload (PER-12).
- It resides on both the client and the server.
- It does not initiate communication:
  - Every server controller returns a Result.
  - The **ServerConnection** (client) returns the server's Result to its caller. If the network fails or the response is malformed, it returns a failed Result itself instead of throwing (GEN-12).
  - The client controllers read the flag, display the failure reason through the **ErrorMessageView**, and compare it with the constants in **FailureReasons**.

### NewBill
- This component is a model (message data) holding a bill exactly as a client submits it, before shares exist: description, total in cents, a weight in hundredths for every current member (keyed by name, in member list order), and an optional suggested date (BIL-29).
- It resides on both the client and the server.
- It does not initiate communication:
  - The **BillFormModel** (client) builds it from the validated form.
  - The **BillActionController** passes it to the **ServerConnection**'s `createBill`, which sends it.
  - The **BillApiController** (server) receives it, validates it again, and gives its weights to the **SplitCalculator**.

### GroupSummary
- This component is a model (message data) with the `groupId` and name of one group, for one row of the home screen's list (GRP-1, GRP-2).
- It resides on both the client and the server.
- It does not initiate communication. The **GroupRepository** (server) produces a list of them, the **GroupApiController** returns that list in `listGroups`, the **SyncController** (client) stores it in the **GroupListModel**, and the **HomeView** displays it.

### Member
- This component is a model (message data) representing one person in a group as other members see them: name in this group, whether they are the owner, whether they have left, and when they joined (MEM-1). It deliberately has no username.
- It resides on both the client and the server.
- It does not initiate communication. The **GroupApiController** (server) builds Members inside every GroupSnapshot from the server's `GroupMember` records. The **GroupModel** (client) holds them, and the **MemberListView**, **BillListView**, **BillDetailsView**, and **BillFormModel** read names, owner flags, and former flags through the GroupModel.

### BillShare
- This component is a model (message data) representing what one member owes the bill owner for one bill: member name, weight, share in cents, whether payment is confirmed, and when (BIL-33).
- It resides on both the client and the server.
- It does not initiate communication. The **BillApiController** (server) creates BillShares from the **SplitCalculator**'s output, the **GroupRepository** stores and loads them (and marks one confirmed), and on the client the **OutstandingModel** and **BillDetailsView** read them through the **GroupModel**.

### Bill
- This component is a model (message data) representing one bill: id, description, total, bill owner's name, creation time, suggested date (or none), and its BillShares in member list order (BIL-33).
- It resides on both the client and the server.
- It does not initiate communication. The **BillApiController** (server) creates it, the **GroupRepository** stores and loads it, the **Group** (server) holds the list of them, and the **GroupApiController** copies them into the GroupSnapshot. On the client, the **GroupModel** holds them and the **BillListView**, **BillDetailsView**, and **OutstandingModel** read them.

### GroupSnapshot
- This component is a model (message data) representing the complete state of one group at one moment: id, name, owner's name, the group code (filled in **only** when the requester is the owner, OWN-9), the requester's own name in the group, every member (current and former) in member list order, and every bill newest first.
- It resides on both the client and the server. The server builds it; the client replaces its copy with each one received.
- It does not initiate communication:
  - The **GroupApiController** (server) builds it from a `Group` and returns it in the Result of `createGroup`, `joinGroup`, and `getGroup`.
  - The **ServerConnection** (client) passes it to the calling controller. The **HomeController** and the **SyncController** give it to the **GroupModel**.

### InputValidator
- This component is a model (stateless rules) that decides whether a typed value is acceptable: usernames, names and descriptions, dollar amounts (converted to cents with exact decimal arithmetic), weights, optional dates, the weight sum, the Split Equally weights, and case-insensitive name and code comparison. Both sides use it so the client's early feedback and the server's independent check always agree (BIL-30).
- It resides on both the client and the server.
- Its methods are `validateUsername`, `validateName`, `parseAmountCents`, `parseWeightHundredths`, `parseOptionalDate`, `validateWeightsSum`, `equalWeights`, `isNotInPast`, `namesEqual`, and `normalizeGroupCode`.
- Callers ask it directly; it communicates with nobody:
  - The **BillFormModel**, **UsernameView**, **JoinGroupView**, **HomeController**, and **UsernameController** (client) ask it to validate input.
  - The **UserApiController**, **GroupApiController**, and **BillApiController** (server) ask it to validate the same input again.
  - Each call returns a `Result` carrying the parsed value or a user-facing reason.

### Formatting
- This component is a model (stateless rules) that turns values into display text: dollars from cents (`$12.50`, `-$12.50`), weights as `0.25`, and dates as `yyyy-mm-dd` or "No date" (BLD-8 to BLD-10).
- It resides on both sides but is used only by client views.
- Its methods are `formatCents`, `formatWeight`, and `formatDateOrNone`. Callers ask it directly; it communicates with nobody. The **MemberListView**, **BillListView**, and **BillDetailsView** ask it to format amounts, weights, and dates.

### FailureReasons
- This component is a model (constants) holding the few failure-reason texts the client must recognize rather than just display: group code outdated, group not found, not a member, and bill not found.
- It resides on both the client and the server.
- It does not initiate communication. The **AuthGuard** and server controllers return these exact texts in failed Results; the **SyncController**, **HomeController**, and **BillActionController** (client) compare a failed Result's reason against them to decide what to do next.

## Server-only models

### User
- This component is a model representing one registered user: the username (as the person typed it) and the login token issued for it.
- It resides only on the server.
- Only the **UserRepository** and the **AuthGuard** communicate with it. The UserRepository stores and loads Users; the AuthGuard reads a User's token to compare it with the one a request carries.

### GroupMember
- This component is a model representing one member as the server stores it: the username, the name used in this group, when the member joined, and whether they have left. It is what links a user to a name in a group (MEM-1).
- It resides only on the server.
- Only the **Group**, **GroupRepository**, **AuthGuard**, and **GroupApiController** communicate with it. The GroupRepository saves and loads them; the others read them to find members and to build the shared `Member` entries (without usernames).

### Group
- This component is a model holding everything the server knows about one group: id, name, current code, owner's username, all GroupMembers in member list order, and all Bills newest first. It answers simple questions about itself: `findCurrentMemberByUsername`, `findCurrentMemberByName` (ignoring case), `currentMembersInOrder`, `findBill`, and `ownerMemberName`.
- It resides only on the server.
- Only the **GroupRepository** and the server controllers communicate with it:
  - The GroupRepository builds a Group when it loads one.
  - The **AuthGuard** returns the loaded Group inside the Result of `authorizeGroupAccess` and reads it to check roles.
  - The **GroupApiController** and **BillApiController** read it to validate requests and to build responses.

### UserRepository
- This component is a model that stores and retrieves users in the database. Every write is one committed transaction (PER-1 to PER-3).
- It resides only on the server.
- Only the **UserApiController** and the **AuthGuard** communicate with it. They can ask it to:
  - `usernameExists(username)`: return whether any user has that username, ignoring letter case (USR-6).
  - `createUser(username, loginToken)`: store a new user.
  - `findByUsername(username)`: return the User, if any.
  - Any call can fail with a `StorageException`, which the caller turns into a failed Result (PER-9).

### GroupRepository
- This component is a model that stores and retrieves groups, members, bills, shares, and payment statuses in the database. Every write is one committed transaction and is never partly applied (PER-2, PER-3).
- It resides only on the server.
- Only the **AuthGuard**, **GroupApiController**, and **BillApiController** communicate with it. They can ask it to:
  - `codeExists(code)`: return whether a group has that code (used with the **GroupCodeGenerator**, GRP-12).
  - `createGroup(name, code, ownerUsername, ownerMemberName)`: store a new group with its owner as first member; return the new `groupId`.
  - `loadGroup(groupId)` and `loadGroupByCode(code)`: return the whole `Group`, or nothing.
  - `listGroupsOfUser(username)`: return a `GroupSummary` for each group the user currently belongs to.
  - `renameGroup(groupId, newName)` and `replaceCode(groupId, newCode)`.
  - `addMember(groupId, username, memberName)`: append a member at the end of the member list with the current time.
  - `leaveGroup(groupId, username, newOwnerUsername)`: mark a member as former and, in the same transaction, make `newOwnerUsername` the owner when it is not null (OWN-20).
  - `deleteGroup(groupId)`: delete the group with all its members and bills (MEM-28, PER-13).
  - `addBill(groupId, bill)`: store a bill with its shares; return its new id.
  - `setBillDate(groupId, billId, date)`, `confirmShare(groupId, billId, memberName, when)`, `deleteBill(groupId, billId)`: each return false when the bill or share no longer exists.
  - `deleteAllBills(groupId)`: delete every bill, leaving the members.

### SplitCalculator
- This component is a model that turns a bill's total and weights into exact shares in cents using integer arithmetic only: floor(total × weight ÷ 100) for each member with a positive weight, then leftover cents given one each to those members in member list order, so the shares sum to exactly the total (SPL-1 to SPL-7).
- It resides only on the server.
- Only the **BillApiController** communicates with it. It can ask the SplitCalculator to `computeShares(totalCents, weightsHundredths)`, passing the weights in member list order, and receives each member's share in cents in the same order.

### GroupCodeGenerator
- This component is a model that produces random group codes of 6 characters from A to Z (excluding I and O) and 2 to 9 (GRP-11).
- It resides only on the server.
- Only the **GroupApiController** communicates with it. It can ask the GroupCodeGenerator to `generateCode()`. The controller then asks the **GroupRepository** whether the code exists and asks for another code if it does (GRP-12).

### TokenGenerator
- This component is a model that produces secret login tokens of at least 32 random characters from a cryptographically secure source (USR-10).
- It resides only on the server.
- Only the **UserApiController** communicates with it. It can ask the TokenGenerator to `generateLoginToken()`.

## Client-only models

### SessionModel
- This component is a model that stores what the client remembers between runs in a small local configuration file: the username, the login token, the server address, and the saved group code for each group (PER-10, USR-11). If the file is missing or unreadable it reports "no identity" and never throws (USR-16).
- It resides only on the client.
- It communicates with these components:
  - The **AppController** can ask it to `load()`, whether it `hasIdentity()`, and whether the file `wasConfigUnreadable()`.
  - The **UsernameController** can ask it to `saveIdentity(username, loginToken)` and `setServerAddress(address)`.
  - The **HomeController** can ask it to `saveGroupCode(groupId, code)` and `getSavedGroupCode(groupId)`, and to `setServerAddress(address)`.
  - The **GroupAdminController** can ask it to `saveGroupCode` (after generating a new code) and `forgetGroupCode(groupId)` (after leaving).
  - The **SyncController** can ask it for the saved code of the open group and to `forgetGroupCode(groupId)` when the server says the code is outdated.
  - The **ServerConnection** can ask it for the credentials, the server address, and the saved group code.
  - The **HomeView** can ask it for the username and server address to display.

### GroupListModel
- This component is a model that stores the client's copy of the list of groups the user belongs to, shown on the home screen (GRP-1). It notifies its listeners only when the list actually changed (SYN-5).
- It resides only on the client.
- It communicates with these components:
  - The **SyncController** can ask it to `replaceGroups(groups)`.
  - The **HomeView** can ask it for `getGroups()`, and is notified when it changes.

### GroupModel
- This component is a model that stores the client's latest `GroupSnapshot` of the open group and which bill is selected. It is the single source of what the group screen shows. It notifies its listeners only when the snapshot actually changed, and clears the selection when the selected bill disappears (BLD-12, SYN-5). It also answers permission questions for the views: is the user the owner, may the user confirm payments, change the date, or cancel a given bill (OWN-10, PAY-3, DAT-6, CAN-13).
- It resides only on the client.
- It communicates with these components:
  - The **HomeController** and **SyncController** can ask it to `replaceSnapshot(snapshot)`.
  - The **AppController** can ask it to `clear()` when the home screen is shown.
  - The **BillActionController** can ask it to `selectBill(billId)` and for `findBill(billId)`, `canConfirmPayments`, `canChangeDate`, and `canCancelBill`.
  - The **GroupAdminController** and **SyncController** can ask it for the open group's id.
  - The **BillFormModel** and **OutstandingModel** read the current members and bills from it and are notified when it changes.
  - The **GroupView**, **MemberListView**, **BillListView**, and **BillDetailsView** read the snapshot, the selected bill, and the permission answers, and are notified when it changes.

### ConnectionModel
- This component is a model that stores whether the server can currently be reached and the latest connection error (SYN-7 to SYN-11). It starts as connected.
- It resides only on the client.
- It communicates with these components:
  - The **ServerConnection** can tell it `markConnected()` after any request that got a response, and `markDisconnected(message)` after one that failed or timed out.
  - It notifies the **ConnectionBannerView**, **UsernameView**, **HomeView**, **JoinGroupView**, **GroupView**, **BillDetailsView**, and **BillFormModel** when the status changes, so they can show the banner and enable or disable controls that change data (SYN-9). They ask it for `isConnected()` and `getErrorMessage()`.

### BillFormModel
- This component is a model that stores the contents of the Create Bill form (description, amount text, one weight text per current member, date text) and works out what is valid: per-field errors, the running weight sum, and whether the Create Bill button is enabled (BIL-3 to BIL-28). It keeps typed text when members join or leave (SYN-17).
- It resides only on the client.
- It communicates with these components:
  - The **BillActionController** can ask it to store a field (`setDescription`, `setAmountText`, `setWeightText`, `setDateText`), to `applyEqualSplit()`, to `buildNewBill()` once the form is valid, and to `clear()` after the server accepts a bill (BIL-36).
  - It asks the **GroupModel** for the current members, and is notified when they change. It asks the **ConnectionModel** for the connection status (BIL-26).
  - It asks **InputValidator** to validate each field and compute the equal split.
  - The **CreateBillView** asks it for the field texts, the weight row names, per-weight errors, the weight sum, and `isSubmitEnabled()`, and is notified when it changes.

### OutstandingModel
- This component is a model that works out each current member's "owes" and "is owed" amounts from the bills in the GroupModel. Nothing is stored; it is recomputed whenever the bills change (OUT-3 to OUT-6). A bill owner's own share is never counted.
- It resides only on the client.
- It communicates with these components:
  - It reads the members and bills from the **GroupModel** and is notified when they change.
  - The **MemberListView** can ask it for `getOutstanding()` (a list of `OutstandingAmounts`, one per current member in member list order) and is notified when it changes.

---

# Controllers

## Server controllers

The server controllers answer requests from the client's **ServerConnection**. Operations that change a group call the **GroupLockManager** to lock the group, reload it, re-validate everything (they never trust that the client already checked), apply the change through the repository, and unlock whether it succeeded or failed (SYN-12). Any `StorageException` becomes a failed Result (PER-9).

### AuthGuard
- This component is a controller that decides whether a request is allowed: who the caller is, whether they may reach the group, and whether they hold the role an action needs.
- It resides only on the server.
- Only the other server controllers communicate with it. They can ask it to:
  - `authenticate(credentials)`: succeed with the User iff the username and login token belong to the same user (USR-14).
  - `authorizeGroupAccess(groupAccess)`: succeed with the loaded `Group` iff the credentials authenticate, the group exists, the user is a current member, and the code matches the group's current code ignoring case. It fails with the **FailureReasons** texts for "group not found", "not a member", or "code outdated" (GRP-14, GRP-18, GRP-19).
  - `requireGroupOwner(group, username)`: succeed iff the caller is the owner (OWN-8).
  - `requireBillOwner(group, bill, username)`: succeed iff the caller owns the bill (DAT-8).
  - `requireMayConfirmPayment(group, bill, username)`: succeed iff the caller owns the bill, or is the group owner while the bill owner has left (PAY-1, MEM-27).
  - `requireBillOwnerOrGroupOwner(group, bill, username)`: succeed iff the caller owns the bill or is the group owner (CAN-12).
- To do this it asks the **UserRepository** for the user by username and the **GroupRepository** to load the group.

### GroupLockManager
- This component is a controller that makes sure only one change to a given group is processed at a time, so two simultaneous requests cannot interleave (SYN-12). Different groups never block each other.
- It resides only on the server.
- Only the **GroupApiController** and **BillApiController** communicate with it. They can ask it to `lock(groupId)`, which returns once no other change to that group is in progress, and to `unlock(groupId)` after the change has been committed or has failed.

### UserApiController
- This component is a controller that creates users. It is the only server operation that needs no credentials.
- It resides only on the server.
- It receives requests from the client's **ServerConnection** and communicates with other server components as follows:
  - `createUser(username)`: asks **InputValidator** to check the username (USR-3, USR-4); asks the **UserRepository** whether it already exists ignoring case (USR-6); asks the **TokenGenerator** for a login token; asks the UserRepository to store the user; returns a Result carrying `Credentials` (the username and the token). On any failure it creates nothing.

### GroupApiController
- This component is a controller that handles creating, joining, listing, opening, renaming, re-coding, and leaving groups.
- It resides only on the server.
- It receives requests from the client's **ServerConnection** and communicates with other server components as follows:
  - `listGroups(credentials)`: asks the **AuthGuard** to authenticate, then asks the **GroupRepository** for the user's groups; returns a list of `GroupSummary`.
  - `createGroup(credentials, groupName, memberName)`: authenticates; asks **InputValidator** to check both names; asks the **GroupCodeGenerator** for a code and the **GroupRepository** whether it exists, repeating until it is unused; asks the GroupRepository to create the group with the caller as first member and owner; returns a `GroupSnapshot` including the code.
  - `joinGroup(credentials, groupCode, memberName)`: authenticates; validates the name; asks the **GroupRepository** to load the group by that code; locks the group; reloads it and fails if the caller is already a current member or the name matches a current member's ignoring case; asks the GroupRepository to add the member; unlocks; returns a `GroupSnapshot`.
  - `getGroup(groupAccess)`: asks the **AuthGuard** to authorize group access; builds a `GroupSnapshot` from the Group, including the group code only if the caller is the owner (OWN-9). This one operation both opens a group and refreshes it.
  - `renameGroup(groupAccess, newName)`: authorizes; asks the AuthGuard to require the owner; validates the name; locks; asks the GroupRepository to rename; unlocks.
  - `generateNewCode(groupAccess)`: authorizes; requires the owner; locks; asks the **GroupCodeGenerator** for codes until the **GroupRepository** says one is unused; asks the GroupRepository to replace the code; unlocks; returns the new code.
  - `leaveGroup(groupAccess)`: authorizes; locks; if the caller is the last current member asks the GroupRepository to delete the group; otherwise, if the caller is the owner, picks the earliest remaining current member as the new owner, and asks the GroupRepository to record the leave and the new owner in one transaction; unlocks.
  - It asks the **GroupLockManager** to lock and unlock the group around every change.

### BillApiController
- This component is a controller that handles creating bills, changing suggested dates, confirming payments, and cancelling bills.
- It resides only on the server.
- It receives requests from the client's **ServerConnection** and communicates with other server components as follows. Every operation first asks the **AuthGuard** to authorize group access; each change then locks the group, reloads it, applies the change through the **GroupRepository**, and unlocks.
  - `createBill(groupAccess, newBill)`: asks **InputValidator** to re-check the description, total, every weight, the weight sum, and the date; checks the date is not before today using its clock; checks the weights name exactly the current members; asks the **SplitCalculator** for the shares; builds a `Bill` owned by the caller whose own share, if above 0, is already confirmed (BIL-35); asks the GroupRepository to store it; returns the new bill's id.
  - `changeSuggestedDate(groupAccess, billId, newDate)`: asks the AuthGuard to require the bill owner; checks the date is not in the past; asks the GroupRepository to `setBillDate`. A missing bill returns a "bill not found" failure.
  - `confirmPayment(groupAccess, billId, memberName)`: asks the AuthGuard to require authority to confirm payment; fails if that share is already confirmed (PAY-7); asks the GroupRepository to `confirmShare` with the current time.
  - `cancelBill(groupAccess, billId)`: asks the AuthGuard to require the bill owner or group owner; asks the GroupRepository to `deleteBill`. A missing bill returns "bill not found" (CAN-21).
  - `cancelAllBills(groupAccess)`: asks the AuthGuard to require the group owner; asks the GroupRepository to `deleteAllBills`.
  - It asks the **GroupLockManager** to lock and unlock the group around each change.

## Client controllers

### ServerConnection
- This component is a controller that sends requests to the server over HTTP and returns the server's `Result`. It is the only client component that uses the network. It attaches the saved credentials (and saved group code, where a group is involved) to every request, and gives up on a request after 5 seconds (SYN-7).
- It resides only on the client.
- It communicates with these components:
  - The **UsernameController**, **HomeController**, **GroupAdminController**, **BillActionController**, and **SyncController** can ask it to send: `createUser(username, serverAddress)`, `listGroups()`, `createGroup(groupName, memberName)`, `joinGroup(groupCode, memberName)`, `getGroup(groupId, groupCode)`, `renameGroup(groupId, newName)`, `generateNewCode(groupId)`, `leaveGroup(groupId)`, `createBill(groupId, newBill)`, `changeSuggestedDate(groupId, billId, newDate)`, `confirmPayment(groupId, billId, memberName)`, `cancelBill(groupId, billId)`, and `cancelAllBills(groupId)`. Each returns the server's Result to the caller.
  - It asks the **SessionModel** for the credentials, server address, and saved group code.
  - After every request it tells the **ConnectionModel** `markConnected()` if a response arrived or `markDisconnected(message)` if the request failed or timed out.
  - On the server side it communicates with the **UserApiController**, **GroupApiController**, and **BillApiController** using the operations above. It turns a malformed response into a failed Result instead of an exception (GEN-12).

### AppController
- This component is a controller that starts the client and decides which of the three screens is showing: Create Username, home, or group (GEN-6).
- It resides only on the client.
- It communicates with these components:
  - At startup it asks the **SessionModel** to `load()` and whether it has an identity. If so it shows the **HomeView** and asks the **SyncController** to start home polling; if not it shows the **UsernameView**, with a notice if the file was unreadable (USR-1, USR-12, USR-16).
  - The **UsernameController** can ask it to show the home screen after a username is created. The **HomeController** can ask it to show the group screen after a group is created, joined, or opened. The **GroupAdminController** can ask it to show the home screen (back, or after leaving).
  - The **SyncController** can tell it `groupUnavailable(message)` when the open group is gone, the user is no longer a member, or the group code changed. It then shows the home screen and asks the **ErrorMessageView** to show the message (GRP-19, SYN-15, SYN-16).
  - When showing the home screen it asks the **GroupModel** to `clear()` and the SyncController to `startHomePolling()`. When showing the group screen it asks the SyncController to `startGroupPolling(groupId)`. When showing the username screen it asks the SyncController to `stop()`.
  - It shows and hides the **UsernameView**, **HomeView**, and **GroupView** through its methods `showUsernameScreen(notice)`, `showHomeScreen()`, and `showGroupScreen(groupId)`, which the other controllers call.

### UsernameController
- This component is a controller that handles the Create Username screen (section 1 of the requirements).
- It resides only on the client.
- It communicates with these components:
  - The **UsernameView** sends it `createUsernameRequested(username, serverAddress)`.
  - It asks the **ServerConnection** to send `createUser(username, serverAddress)`.
  - On success it asks the **SessionModel** to save the username and token and the server address, and tells the **AppController** to show the home screen.
  - On failure (invalid, taken, or server unreachable) it asks the **ErrorMessageView** to show the reason (USR-5, USR-6) and saves nothing.

### HomeController
- This component is a controller that handles the home screen: creating, joining, and opening groups, and the server address.
- It resides only on the client.
- It communicates with these components:
  - The **CreateGroupView** sends it `createGroupRequested(groupName, memberName)`. The **JoinGroupView** sends it `joinGroupRequested(groupCode, memberName)`. The **HomeView** sends it `groupSelected(groupId, groupName)` and `serverAddressChanged(address)`. The **GroupCodePromptView** sends it `groupCodeEntered(groupId, groupCode)`.
  - For create and join it asks the **ServerConnection** to send `createGroup` or `joinGroup`. On success it asks the **GroupModel** to store the returned snapshot, asks the **SessionModel** to `saveGroupCode(groupId, code)` (the snapshot's code when creating, the typed code when joining), and tells the **AppController** to show the group screen (MEM-16). On failure it asks the **ErrorMessageView** to show the reason.
  - For `groupSelected` it asks the **SessionModel** for a saved code. If there is one it asks the ServerConnection to send `getGroup(groupId, savedCode)`; if not it asks the **GroupCodePromptView** to show (GRP-13, GRP-17).
  - For `groupCodeEntered` it asks the ServerConnection to send `getGroup(groupId, enteredCode)`. On success it asks the SessionModel to save the code, the GroupModel to store the snapshot, and the AppController to show the group screen. On failure it shows the reason and saves nothing (GRP-15).
  - For `serverAddressChanged` it asks the SessionModel to `setServerAddress(address)`.

### GroupAdminController
- This component is a controller that handles group-level actions on the group screen: rename, generate a new code, leave, cancel all bills, and going back.
- It resides only on the client.
- It communicates with these components:
  - The **GroupView** sends it `renameGroupRequested(newName)`, `generateNewCodeRequested()`, `leaveGroupRequested()`, `cancelAllBillsRequested()`, and `backToHomeRequested()`.
  - It asks the **GroupModel** for the open group's id, and the **ServerConnection** to send `renameGroup`, `generateNewCode`, `leaveGroup`, or `cancelAllBills`.
  - After a successful rename or cancel-all it asks the **SyncController** to `refreshNow()`. After a successful `generateNewCode` it asks the **SessionModel** to save the new code for the group, then asks the SyncController to refresh (OWN-17).
  - For leave and cancel-all it first asks the **ConfirmDialogView** to `ask(...)`; the request is sent only if the user confirms. After a successful leave it asks the SessionModel to `forgetGroupCode(groupId)` and tells the **AppController** to show the home screen (MEM-22).
  - On any failure it asks the **ErrorMessageView** to show the reason.
  - For `backToHomeRequested` it tells the AppController to show the home screen.

### BillActionController
- This component is a controller that handles everything about bills on the group screen: the Create Bill form, selecting a bill, changing a date, confirming payments, and cancelling a bill.
- It resides only on the client.
- It communicates with these components:
  - The **CreateBillView** sends it `descriptionChanged`, `amountChanged`, `weightChanged(memberName, text)`, `dateChanged`, `splitEquallyRequested`, and `createBillRequested`. It asks the **BillFormModel** to store each field, apply the equal split, or build the bill.
  - On `createBillRequested` it asks the **BillFormModel** for the validated `NewBill` and the **ServerConnection** to send `createBill(groupId, newBill)`. On success it asks the BillFormModel to `clear()` and the **SyncController** to `refreshNow()`; on failure it leaves the form alone and asks the **ErrorMessageView** to show the reason (BIL-37).
  - The **BillListView** sends it `billSelected(billId)`; it asks the **GroupModel** to `selectBill(billId)`.
  - The **BillDetailsView** sends it `changeDateRequested(billId, text)`, `confirmPaymentRequested(billId, memberName)`, and `cancelBillRequested(billId)`. It asks the ServerConnection to send `changeSuggestedDate`, `confirmPayment`, or `cancelBill`, and on success asks the SyncController to refresh. For cancel it first asks the **ConfirmDialogView** to `ask(...)` and sends nothing if the user cancels (CAN-14, CAN-17). When the server answers "bill not found" it asks the ErrorMessageView to say the bill was already cancelled (CAN-21).
  - It asks the GroupModel for the open group's id and the date text is parsed with **InputValidator**.

### SyncController
- This component is a controller that keeps the client's data current by polling the server every 5 seconds, and once immediately whenever it is asked (SYN-1 to SYN-3, SYN-10).
- It resides only on the client.
- It communicates with these components:
  - The **AppController** can ask it to `startHomePolling()`, `startGroupPolling(groupId)`, and `stop()`. The **GroupAdminController** and **BillActionController** can ask it to `refreshNow()`.
  - While home polling it asks the **ServerConnection** to send `listGroups()` and asks the **GroupListModel** to `replaceGroups(...)` with the answer.
  - While group polling it asks the **SessionModel** for the group's saved code, asks the ServerConnection to send `getGroup(groupId, savedCode)`, and asks the **GroupModel** to `replaceSnapshot(...)` with the answer.
  - If a failed Result's reason is "code outdated" it asks the SessionModel to `forgetGroupCode(groupId)` and tells the **AppController** `groupUnavailable("The group code changed...")`. If it is "group not found" or "not a member" it tells the AppController `groupUnavailable(...)` with a matching message (SYN-15, SYN-16).
  - While the server is unreachable it keeps retrying every 5 seconds. The ServerConnection keeps the **ConnectionModel** up to date, so the SyncController does not update it itself.

---

# Views

The server has no views. All views below reside only on the client. Every view only displays data and forwards user actions; none changes a model or talks to the server directly. The user-interface toolkit (Swing or JavaFX) is chosen when the views are implemented; the stubs are toolkit-neutral.

### UsernameView
- This component is a view that shows the Create Username screen: username field, server address field, and Create button, plus an optional notice (USR-1, USR-2).
- It communicates with these components:
  - It sends the **UsernameController** `createUsernameRequested(username, serverAddress)` when the user presses Create.
  - It reads the saved server address from the **SessionModel** and the connection status from the **ConnectionModel** to enable Create only when the username is valid and the connection is up (USR-8); it uses **InputValidator** to check the username.
  - The **AppController** shows and hides it.

### HomeView
- This component is a view that shows the home screen: the user's own username, the list of their groups, Create Group and Join Group buttons, the "No groups yet" message, and the server address setting (GRP-1 to GRP-5, USR-13, GEN-5).
- It communicates with these components:
  - It reads the groups from the **GroupListModel**, the username and server address from the **SessionModel**, and the connection status from the **ConnectionModel**, and redraws when the GroupListModel or ConnectionModel notifies it.
  - It sends the **HomeController** `groupSelected(groupId, groupName)` and `serverAddressChanged(address)`.
  - When the user presses Create Group or Join Group it asks the **CreateGroupView** or **JoinGroupView** to show.
  - The **AppController** shows and hides it.

### CreateGroupView
- This component is a view that shows the Create Group form: group name and the creator's name in the group (GRP-6).
- It communicates with these components:
  - It sends the **HomeController** `createGroupRequested(groupName, memberName)` when the user presses Create, then hides itself.
  - The **HomeView** asks it to show.

### JoinGroupView
- This component is a view that shows the Join Group form: the group code the owner gave the user, and the user's name in the group. Its button is enabled only when both are filled in and the connection is up (GRP-24, GRP-25).
- It communicates with these components:
  - It sends the **HomeController** `joinGroupRequested(groupCode, memberName)` when the user presses Join, then hides itself.
  - It reads the connection status from the **ConnectionModel**.
  - The **HomeView** asks it to show.

### GroupCodePromptView
- This component is a view that asks for a group's code when none is saved, for example after the owner generated a new one (GRP-13, GRP-19).
- It communicates with these components:
  - The **HomeController** asks it to `show(groupId, groupName)`.
  - It sends the **HomeController** `groupCodeEntered(groupId, groupCode)` when the user submits, then hides itself.

### GroupView
- This component is a view that is the group screen's frame (GEN-7, GEN-8). It shows the group name and a header with Back to Home and Leave Group for everyone and, for the group owner only, Rename Group, Show Group Code, Generate New Code, and Cancel All Bills. It contains the **MemberListView**, **BillListView**, **BillDetailsView**, **CreateBillView**, and **ConnectionBannerView**.
- It communicates with these components:
  - It reads the group name, owner status, and group code from the **GroupModel**, and the connection status from the **ConnectionModel**, and redraws when either notifies it. Owner-only controls are shown and enabled only when the user is the owner and the connection is up (OWN-10).
  - It sends the **GroupAdminController** `backToHomeRequested()`, `leaveGroupRequested()`, `renameGroupRequested(newName)`, `generateNewCodeRequested()`, and `cancelAllBillsRequested()`. Showing or hiding the code is local to the view and sends nothing.
  - The **AppController** shows and hides it.

### MemberListView
- This component is a view that shows the Members area: every current member in member list order with the "Owner" label, the "you" marker, and the member's "owes" and "is owed" amounts (MEM-2 to MEM-4, OWN-2, OUT-1, OUT-2).
- It communicates with these components:
  - It reads the members and the user's own name from the **GroupModel** and the amounts from the **OutstandingModel**, and redraws when either notifies it. It formats money with **Formatting**.
  - It sends no messages.

### BillListView
- This component is a view that shows the scrollable Bills area, newest first. Each row has the description, total, bill owner's name (with "(left)" if they left), suggested date or "No date", and "n of m paid" (BLD-1 to BLD-7). It shows "No bills yet" when empty.
- It communicates with these components:
  - It reads the bills and the selected bill from the **GroupModel** and redraws when it notifies it. It formats text with **Formatting**.
  - It sends the **BillActionController** `billSelected(billId)` when the user selects a row.

### BillDetailsView
- This component is a view that shows the details of the selected bill: each member's name, weight, share, and Paid/Unpaid status, the Settled/Open mark, a Confirm Payment button beside each Unpaid share, a Change Date control, and a Cancel Bill button (BLD-5, BLD-6, PAY-2, PAY-3, DAT-5, DAT-6, CAN-11, CAN-13).
- It communicates with these components:
  - It reads the selected bill from the **GroupModel**, and asks it `canConfirmPayments`, `canChangeDate`, and `canCancelBill` to decide which controls to enable. It reads the connection status from the **ConnectionModel**. It redraws when either notifies it.
  - It sends the **BillActionController** `confirmPaymentRequested(billId, memberName)`, `changeDateRequested(billId, newDateText)`, and `cancelBillRequested(billId)`.

### CreateBillView
- This component is a view that shows the Create Bill form: description, total amount, one weight field per current member, the running weight sum (marked as an error unless exactly 1.00), a Split Equally button, the optional suggested date, and the Create Bill button (BIL-1 to BIL-7, BIL-17, BIL-18).
- It communicates with these components:
  - It reads the field texts, weight row names, per-weight errors, weight sum, and the Create Bill button's enabled state from the **BillFormModel**, and redraws when it notifies it.
  - It sends the **BillActionController** `descriptionChanged`, `amountChanged`, `weightChanged(memberName, text)`, `dateChanged`, `splitEquallyRequested`, and `createBillRequested`.

### ConnectionBannerView
- This component is a view that shows a banner stating the server cannot be reached, and hides it when the connection returns (SYN-7, SYN-11).
- It communicates with these components:
  - It reads the status and error message from the **ConnectionModel** and redraws when it notifies it.
  - It sends no messages.

### ConfirmDialogView
- This component is a view that shows a confirmation dialog with Confirm and Cancel, used before leaving a group, cancelling a bill, and cancelling all bills (MEM-19, CAN-3 to CAN-6, CAN-14 to CAN-17).
- It communicates with these components:
  - The **GroupAdminController** and **BillActionController** can ask it to `ask(title, message, onConfirm)`.
  - If the user confirms it runs `onConfirm` once. If the user cancels it does nothing, so no request is sent.

### ErrorMessageView
- This component is a view that shows error and notice messages in a dismissible message or dialog (GEN-14).
- It communicates with these components:
  - The **UsernameController**, **HomeController**, **GroupAdminController**, **BillActionController**, and **AppController** can ask it to `show(message)`.
  - It sends no messages.

