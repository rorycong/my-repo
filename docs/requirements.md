# IOU: Requirements

The system is a client-server application. Users run the web based client on their own devices; a server holds the shared data. Each person creates their own unique username. People belong to groups. A group has a group owner and a group code. Inside a group, any member can create a bill; the member who creates a bill is its bill owner and confirms when other members have paid their shares.

## Table of Contents

- [Definitions](#definitions)
- [1. Users and Identity](#1-users-and-identity)
- [2. Groups and Group Access](#2-groups-and-group-access)
- [3. Group Owner](#3-group-owner)
- [4. Members](#4-members)
- [5. Creating a Bill](#5-creating-a-bill)
- [6. Splitting a Bill into Shares](#6-splitting-a-bill-into-shares)
- [7. Viewing Bills](#7-viewing-bills)
- [8. Suggested Pay-Back Date](#8-suggested-pay-back-date)
- [9. Confirming Payment](#9-confirming-payment)
- [10. Cancelling Bills](#10-cancelling-bills)
- [11. Outstanding Amounts](#11-outstanding-amounts)
- [12. Synchronization and Connectivity](#12-synchronization-and-connectivity)
- [13. Server and Persistence](#13-server-and-persistence)
- [14. General and Layout](#14-general-and-layout)

## Definitions

| Term | Meaning |
|------|---------|
| **Cents** | Integer amounts in hundredths of a dollar. All money is stored, transmitted, and calculated in cents. |
| **Weight** | A decimal number from 0 to 1 with at most two decimal places, stored as an integer number of hundredths (0 to 100). |
| **Username** | The unique name a person creates for themselves the first time they use the client. It identifies the person across all groups. |
| **Login token** | A long random secret the server gives the client when a username is created. The client sends it with every request to prove it belongs to that username. |
| **Member** | A user together with the name they use in one particular group (which can differ from the username). |
| **Current member** | A member who has not left the group. |
| **Former member** | A member who has left. |
| **Member list order** | The order in which members were added to the group. Earlier means more senior. |
| **Group owner** | The one current member of a group who has the owner designation. |
| **Bill owner** | The member who created a bill. |
| **Share** | The amount in cents that one member owes the bill owner for one bill. |
| **Confirmed (Paid)** / **Unconfirmed (Unpaid)** | Whether the bill owner has confirmed that a share was paid. |
| **Client** | The Java desktop application. |
| **Server** | The central application storing all data. |

### Sections and design references

| # | Section | Design reference |
|---|---------|------------------|
| 1 | [Users and Identity](#1-users-and-identity) | Item 2 |
| 2 | [Groups and Group Access](#2-groups-and-group-access) | Items 1, 2.1 |
| 3 | [Group Owner](#3-group-owner) | Items 1.3 to 1.4 |
| 4 | [Members](#4-members) | Items 1 (joining with the group code), 3, 6 |
| 5 | [Creating a Bill](#5-creating-a-bill) | Items 5, 7, 7.1 |
| 6 | [Splitting a Bill into Shares](#6-splitting-a-bill-into-shares) | Item 5 |
| 7 | [Viewing Bills](#7-viewing-bills) | Item 7 |
| 8 | [Suggested Pay-Back Date](#8-suggested-pay-back-date) | Item 7.4 |
| 9 | [Confirming Payment](#9-confirming-payment) | Item 7.2 |
| 10 | [Cancelling Bills](#10-cancelling-bills) | Item 7.3, plus the rule that a bill owner can cancel their own bill |
| 11 | [Outstanding Amounts](#11-outstanding-amounts) | Derived from items 5 and 7 |
| 12 | [Synchronization and Connectivity](#12-synchronization-and-connectivity) | Multiple users on separate computers |
| 13 | [Server and Persistence](#13-server-and-persistence) | Data kept between uses |
| 14 | [General and Layout](#14-general-and-layout) | Item 4 |

---

## 1. Users and Identity

> *Design reference: item 2 (each person has a username that they create themselves).*

| ID | Requirement |
|----|-------------|
| `USR-1` | When the client starts and has no saved username, it shall display a Create Username screen before any other screen. |
| `USR-2` | The Create Username screen shall contain a username field, a server address field (GEN-5), and a Create button. |
| `USR-3` | The system shall accept a username only if it is 3 to 20 characters long. |
| `USR-4` | The system shall accept a username only if every character is a letter A to Z or a to z, a digit 0 to 9, or an underscore. |
| `USR-5` | When a username is rejected under USR-3 or USR-4, the client shall display an error message stating the reason and shall not create a user. |
| `USR-6` | The server shall reject a username that equals, compared case-insensitively, the username of any existing user, and the client shall display a message that the username is already taken. |
| `USR-7` | The system shall display a username with the letter case the user entered. |
| `USR-8` | The Create button shall be enabled if and only if the username meets USR-3 and USR-4 and the client is connected to the server. |
| `USR-9` | A username shall never change after it is created. |
| `USR-10` | When the server accepts a new username, it shall create the user and return a login token of at least 32 characters chosen at random. |
| `USR-11` | The client shall save the username and the login token in a local configuration file. |
| `USR-12` | When the client starts and has a saved username and login token, it shall show the home screen without showing the Create Username screen. |
| `USR-13` | The client shall display the user's own username on the home screen at all times. |
| `USR-14` | The server shall reject every request, except creating a new user, that does not carry a username and login token that belong to the same existing user. |
| `USR-15` | The server shall not include any user's login token in a response, except in the response that creates that user. |
| `USR-16` | When the local configuration file is missing or unreadable, the client shall show the Create Username screen with a notice that the previous username cannot be recovered. |
| `USR-17` | The system shall not require the user to enter a password or an email address. |
| `USR-18` | A member's name in a group shall be allowed to differ from the user's username (MEM-1). |

## 2. Groups and Group Access

> *Design reference: items 1 and 2.1.*

| ID | Requirement |
|----|-------------|
| `GRP-1` | The client shall have a home screen listing every group of which the user is a current member. |
| `GRP-2` | Each row on the home screen shall show the group's name. |
| `GRP-3` | A user shall be allowed to be a current member of any number of groups at the same time. |
| `GRP-4` | The home screen shall display a message (for example, "No groups yet") when the user is a member of no groups. |
| `GRP-5` | The home screen shall provide a Create Group button. |
| `GRP-6` | The Create Group form shall require a group name and the creator's name for that group. |
| `GRP-7` | The system shall reject a group name that is empty after leading and trailing whitespace is stripped. |
| `GRP-8` | The system shall allow two different groups to have the same group name. |
| `GRP-9` | When a group is created, the server shall add the creator as the first member of the group, using the creator's name from the form. |
| `GRP-10` | When a group is created, the server shall give the creator the group owner designation. |
| `GRP-11` | When a group is created, the server shall generate a group code of exactly 6 characters, each chosen at random from the letters A to Z (excluding I and O) and the digits 2 to 9. |
| `GRP-12` | The server shall ensure that no two existing groups have the same group code. |
| `GRP-13` | When the user selects a group on the home screen and the client has no saved group code for that group, the client shall prompt the user to enter the group code. |
| `GRP-14` | The server shall grant access to a group only when the request's username and login token belong to a current member of that group and the entered code matches the group's current code, compared case-insensitively. |
| `GRP-15` | When an entered group code is incorrect, the client shall display an error message and shall not open the group. |
| `GRP-16` | When the correct group code is entered, the client shall open the group screen and shall save the code locally for that group. |
| `GRP-17` | When the client has a saved group code for a group, selecting that group on the home screen shall open it without prompting for the code. |
| `GRP-18` | The server shall reject every request about a group that does not carry both the username and login token of a current member and the group's current group code. |
| `GRP-19` | When the server rejects a request because the group code is no longer current, the client shall delete its saved code for that group and prompt the user for the new code. |
| `GRP-20` | The server shall not include data belonging to one group in a response to a request that was authorized for a different group. |
| `GRP-21` | The group screen shall provide a button that returns to the home screen. |
| `GRP-22` | The group screen shall display the group's name. |
| `GRP-23` | A user who is not a current member of a group shall not see that group on the home screen. |
| `GRP-24` | The home screen shall provide a Join Group button that opens a form containing a group code field and a field for the user's name in that group. |
| `GRP-25` | The Join Group form's submit button shall be enabled if and only if the group code field is not empty, the name field is not empty after leading and trailing whitespace is stripped, and the client is connected to the server. |

## 3. Group Owner

> *Design reference: items 1.3 and 1.4 (group owner designation and powers).*

| ID | Requirement |
|----|-------------|
| `OWN-1` | While a group has at least one current member, it shall have exactly one group owner. |
| `OWN-2` | The Members area shall label the group owner with the text "Owner". |
| `OWN-3` | A person shall become a member of a group only by joining with the group's current group code (MEM-5), so the group owner, who alone can see the code, controls whom to give it to. |
| `OWN-4` | Only the group owner shall be allowed to view the group code. |
| `OWN-5` | Only the group owner shall be allowed to rename the group. |
| `OWN-6` | Only the group owner shall be allowed to generate a new group code. |
| `OWN-7` | Only the group owner shall be allowed to cancel all bills (section 10). |
| `OWN-8` | The server shall reject any request for the actions in OWN-4 through OWN-7 that comes from a member who is not the group owner, and shall return a failure reason. |
| `OWN-9` | The server shall not send the group code to any client except in response to a request from the group owner. |
| `OWN-10` | The client shall hide or disable every control for the actions in OWN-4 through OWN-7 when the current user is not the group owner. |
| `OWN-11` | The group screen shall provide a Rename Group control for the group owner. |
| `OWN-12` | When the group owner submits a new group name that passes GRP-7, the server shall replace the group's name with it. |
| `OWN-13` | The group screen shall provide the group owner with a way to display the group code. |
| `OWN-14` | The group screen shall provide the group owner with a Generate New Code button. |
| `OWN-15` | When the group owner activates Generate New Code, the server shall create a new group code that meets GRP-11 and GRP-12 and replace the group's current code with it. |
| `OWN-16` | After a new group code is generated, the server shall reject requests that carry the previous code. |
| `OWN-17` | After a new group code is generated, the owner's client shall display the new code and replace its saved code for that group. |
| `OWN-18` | Generating a new group code shall not remove any member or change any bill. |
| `OWN-19` | When the group owner leaves the group and other current members remain, the server shall assign the group owner designation to the remaining current member who is earliest in member list order. |
| `OWN-20` | The server shall assign the new group owner (OWN-19) in the same operation that records the previous owner leaving, so that the group is never without an owner. |
| `OWN-21` | After a change of group owner, every client shall show the "Owner" label on the new owner and shall show owner-only controls only to the new owner after its next refresh. |

## 4. Members

> *Design reference: item 1 (joining with the group code the owner provides), item 3 (leaving), and item 6 (names).*

| ID | Requirement |
|----|-------------|
| `MEM-1` | A member shall consist of a username and a name that is used only in that group. |
| `MEM-2` | The Members area shall list every current member in member list order. |
| `MEM-3` | Each row in the Members area shall show the member's name. |
| `MEM-4` | The Members area shall mark the row that belongs to the current user with the text "you". |
| `MEM-5` | When a user submits the Join Group form, the server shall add that user as a member of the group whose current group code matches the entered code, compared case-insensitively, using the entered name. |
| `MEM-6` | The server shall reject a Join Group request from a user who is already a current member of that group, and the client shall display a message that the user is already a member. |
| `MEM-7` | The server shall reject a Join Group request whose group code matches no group, and the client shall display a message that the group code was not found. |
| `MEM-8` | The server shall not accept a group code that has been replaced under OWN-15 for joining a group. |
| `MEM-9` | The system shall strip leading and trailing whitespace from a member name before validating or storing it. |
| `MEM-10` | The system shall reject a member name that is empty after whitespace is stripped. |
| `MEM-11` | The system shall reject a member name that equals, compared case-insensitively, the name of another current member of the same group. |
| `MEM-12` | When a member name is rejected, the client shall display an error message stating the reason and the member list shall not change. |
| `MEM-13` | The system shall allow the same user to have different names in different groups. |
| `MEM-14` | The system shall allow the same name to be used by different members in different groups. |
| `MEM-15` | When a user joins a group successfully, the server shall append the new member to the end of the member list and record the time joined. |
| `MEM-16` | After joining successfully, the client shall close and clear the Join Group form, save the group code locally (GRP-16), and open the group screen. |
| `MEM-17` | After a user joins a group, that group shall appear on that user's home screen. |
| `MEM-18` | The group screen shall provide every member with a Leave Group button. |
| `MEM-19` | When a member activates Leave Group, the client shall display a confirmation dialog before sending any request. |
| `MEM-20` | The server shall allow any current member to leave the group at any time, regardless of that member's outstanding amounts or bills. |
| `MEM-21` | When a member leaves, the server shall mark that member as a former member and remove the member from the list of current members. |
| `MEM-22` | After a member leaves, the client shall return to the home screen, and the group shall no longer appear in that user's group list. |
| `MEM-23` | After a member leaves, the server shall reject requests about that group from that user. |
| `MEM-24` | Leaving a group shall not delete or change any bill, weight, share, or payment status. |
| `MEM-25` | The Bills area shall display the name of a former member followed by the text "(left)". |
| `MEM-26` | A former member's Unconfirmed shares shall remain Unconfirmed and shall remain confirmable under section 9. |
| `MEM-27` | When a member who owns one or more bills leaves, the server shall treat the current group owner as having the authority to confirm payments for those bills. |
| `MEM-28` | When the last current member leaves a group, the server shall delete the group and all of its members and bills. |
| `MEM-29` | A user who has left a group shall be allowed to join it again with the group's current group code, which shall create a new member entry at the end of the member list. |
| `MEM-30` | A former member shall not appear in the Add Bill form's weight list (BIL-5). |
| `MEM-31` | The system shall provide no way for the group owner or any other member to remove another member from the group; a member can leave only by their own action (MEM-18). |

## 5. Creating a Bill

> *Design reference: items 5 (weights) and 7 (bills).*

| ID | Requirement |
|----|-------------|
| `BIL-1` | The group screen shall contain a Create Bill form. |
| `BIL-2` | Any current member shall be allowed to create a bill. |
| `BIL-3` | The Create Bill form shall contain a description text field. |
| `BIL-4` | The Create Bill form shall contain a total amount text field in dollars. |
| `BIL-5` | The Create Bill form shall contain one weight field for each current member, listed in member list order and labeled with the member's name. |
| `BIL-6` | Each weight field shall default to 0. |
| `BIL-7` | The Create Bill form shall contain an optional suggested pay-back date field (see [section 8](#8-suggested-pay-back-date)). |
| `BIL-8` | The server shall record the member who creates a bill as the bill owner of that bill. |
| `BIL-9` | The Create Bill form shall not allow the user to choose a different bill owner. |
| `BIL-10` | The system shall accept as a total amount a decimal number with at most two digits after the decimal point. |
| `BIL-11` | The system shall treat a total amount that cannot be parsed under BIL-10 as invalid. |
| `BIL-12` | The system shall treat a total amount that is not greater than 0 as invalid. |
| `BIL-13` | The client shall convert a valid total amount to whole cents using exact decimal arithmetic (not binary floating point). |
| `BIL-14` | The system shall accept as a weight a decimal number from 0 to 1 inclusive with at most two digits after the decimal point. |
| `BIL-15` | The client shall show an error indication on a weight field whose text is not a valid weight under BIL-14. |
| `BIL-16` | The system shall require the sum of all weights in a bill to equal exactly 1.00. |
| `BIL-17` | The client shall display the current sum of the weight fields beside the Create Bill form and shall mark the sum as an error when it is not exactly 1.00. |
| `BIL-18` | The Create Bill form shall provide a Split Equally button. |
| `BIL-19` | When the user activates Split Equally, the client shall set each current member's weight to floor(100 ÷ n) hundredths, where n is the number of current members, then add one hundredth each to the first (100 mod n) members in member list order, so the weights sum to exactly 1.00. |
| `BIL-20` | The system shall treat a bill description that is empty after leading and trailing whitespace is stripped as invalid. |
| `BIL-21` | The Create Bill button shall be disabled while the description is invalid. |
| `BIL-22` | The Create Bill button shall be disabled while the total amount is invalid. |
| `BIL-23` | The Create Bill button shall be disabled while any weight is invalid. |
| `BIL-24` | The Create Bill button shall be disabled while the weights do not sum to exactly 1.00. |
| `BIL-25` | The Create Bill button shall be disabled while the suggested date is non-empty and invalid (DAT-2, DAT-3). |
| `BIL-26` | The Create Bill button shall be disabled while the client is not connected to the server. |
| `BIL-27` | The Create Bill button shall be enabled if and only if none of the conditions in BIL-21 through BIL-26 holds. |
| `BIL-28` | The client shall re-evaluate the enabled state of the Create Bill button after every change to the description, amount, any weight, the suggested date, or the connection status. |
| `BIL-29` | When the user activates the enabled Create Bill button, the client shall send the bill to the server to be recorded. |
| `BIL-30` | The server shall independently validate every submitted bill against BIL-10 through BIL-16, BIL-20, and DAT-3 through DAT-4, and shall reject an invalid bill. |
| `BIL-31` | The server shall reject a bill that gives a weight to a user who is not a current member of the group. |
| `BIL-32` | The server shall reject a bill that omits a weight for any current member. |
| `BIL-33` | Each recorded bill shall store an id, description, total amount in cents, bill owner, time created, suggested date (or none), and for each current member at the time of creation: the weight, the share in cents, and the payment status. |
| `BIL-34` | The description stored shall be the entered description with leading and trailing whitespace removed. |
| `BIL-35` | When the bill owner has a share greater than 0, the server shall record that share as Confirmed at the time the bill is created. |
| `BIL-36` | After the server confirms a bill was recorded, the client shall clear the description field and the total amount field, reset every weight field to 0, and clear the suggested date field. |
| `BIL-37` | When the server rejects a bill or cannot be reached, the client shall keep the form contents unchanged and display an error message. |

## 6. Splitting a Bill into Shares

> *Design reference: item 5 (weights).*

| ID | Requirement |
|----|-------------|
| `SPL-1` | The server shall compute the share for every member of a bill whose weight is greater than 0 as floor(total amount in cents × weight in hundredths ÷ 100), using integer arithmetic, to give that member's base share. |
| `SPL-2` | The server shall compute the leftover cents as the total amount minus the sum of all base shares. |
| `SPL-3` | The server shall distribute the leftover cents one cent at a time to members with weight greater than 0, in member list order, beginning with the first such member, each receiving at most one extra cent. |
| `SPL-4` | The final shares of a bill shall sum to exactly the total amount in cents. |
| `SPL-5` | A member whose weight is 0 shall have a share of 0 cents. |
| `SPL-6` | When exactly one member has weight 1.00, that member's share shall equal the full total amount. |
| `SPL-7` | When two members have weights of 0.50 each, their shares shall each equal half of the total, with any odd cent given to the member earlier in member list order. |
| `SPL-8` | The server shall store each computed share with the bill so that later changes to the member list do not change it. |
| `SPL-9` | A member whose share is 0 cents shall not be shown as owing in the bill details (BLD-5) and shall not be counted among the shares in BLD-3. |

## 7. Viewing Bills

> *Design reference: item 7.*

| ID | Requirement |
|----|-------------|
| `BLD-1` | The group screen shall contain a Bills area listing every bill of the group, ordered from newest to oldest by the time the server recorded them. |
| `BLD-2` | The Bills area shall be vertically scrollable when its rows exceed the visible height. |
| `BLD-3` | Each row shall display the bill's description, total amount, bill owner's name, suggested date (or the text "No date"), and the number of Confirmed shares out of the number of shares owed by members other than the bill owner (for example, "2 of 3 paid"). |
| `BLD-4` | The Bills area shall allow one bill to be selected at a time. |
| `BLD-5` | When a bill is selected, the client shall display its details: for each member with a share greater than 0, the member's name, weight, share in dollars, and status "Paid" or "Unpaid". |
| `BLD-6` | The client shall mark a bill "Settled" when every share owed to the bill owner is Confirmed, and "Open" otherwise. |
| `BLD-7` | The Bills area shall display a message (for example, "No bills yet") when the group has no bills. |
| `BLD-8` | The client shall display money amounts in dollars with exactly two decimal places and a dollar sign (for example, `$12.50`). |
| `BLD-9` | The client shall display weights as decimals with exactly two decimal places (for example, `0.25`). |
| `BLD-10` | The client shall display dates in the format yyyy-mm-dd. |
| `BLD-11` | A newly recorded bill shall appear at the top of the Bills area of the client that recorded it immediately after the server confirms it. |
| `BLD-12` | When a refresh changes the Bills area, the client shall keep the same bill selected if it still exists, and otherwise clear the selection. |
| `BLD-13` | Any current member shall be allowed to view every bill in the group, including bills of other members. |
| `BLD-14` | A bill owner's own share, when greater than 0, shall be displayed as "Paid". |

## 8. Suggested Pay-Back Date

> *Design reference: item 7.4.*

| ID | Requirement |
|----|-------------|
| `DAT-1` | The bill owner shall be allowed to set a suggested pay-back date on a bill. |
| `DAT-2` | The suggested date shall be optional when creating a bill. |
| `DAT-3` | The system shall accept a suggested date only in the format yyyy-mm-dd that is a valid calendar date. |
| `DAT-4` | The server shall reject a suggested date that is earlier than the current date according to the server. |
| `DAT-5` | The bill details shall provide a Change Date control to the bill owner. |
| `DAT-6` | The client shall hide or disable the Change Date control for members who are not the bill owner. |
| `DAT-7` | The bill owner shall be allowed to clear the suggested date of a bill. |
| `DAT-8` | The server shall reject a request to change a suggested date from any member other than the bill owner. |
| `DAT-9` | Changing or clearing a suggested date shall not change any share or payment status. |
| `DAT-10` | The suggested date shall be advisory only: the system shall not change any bill, share, or status when the date passes. |
| `DAT-11` | The suggested date of a bill shall be visible to every current member (BLD-3, BLD-5). |

## 9. Confirming Payment

> *Design reference: item 7.2.*

| ID | Requirement |
|----|-------------|
| `PAY-1` | Only the bill owner shall be allowed to confirm payment of the shares of a bill. |
| `PAY-2` | The bill details shall display a Confirm Payment button next to each Unpaid share. |
| `PAY-3` | The client shall display the Confirm Payment buttons enabled only to the bill owner. |
| `PAY-4` | When the user activates a Confirm Payment button, the server shall mark that share as Paid and record the time of confirmation. |
| `PAY-5` | The server shall reject a Confirm Payment request from any member other than the bill owner (or, under MEM-27, the group owner) and shall return a failure reason. |
| `PAY-6` | A member shall not be able to mark his or her own share as paid unless that member is the bill owner. |
| `PAY-7` | The server shall reject a request to confirm a share that is already Paid, shall make no change, and the client shall display a message that the share was already confirmed. |
| `PAY-8` | Confirming one share shall not change the status of any other share. |
| `PAY-9` | The Confirm Payment buttons shall be disabled while the client is not connected to the server. |
| `PAY-10` | After the server confirms a Confirm Payment request, the client shall display the share as "Paid" and update the bill's "n of m paid" count and the outstanding amounts immediately. |
| `PAY-11` | The system shall not move money; confirming payment shall only record the bill owner's statement that the payment was received. |
| `PAY-12` | The system shall provide no way to change a Paid share back to Unpaid. |

## 10. Cancelling Bills

> *Design reference: item 7.3 (the group owner can cancel all bills) and the rule that a bill owner can cancel their own bill. The group owner can also cancel any single bill.*

| ID | Requirement |
|----|-------------|
| `CAN-1` | The group screen shall contain a Cancel All Bills button. |
| `CAN-2` | The Cancel All Bills button shall be visible and enabled only to the group owner and only while the client is connected to the server. |
| `CAN-3` | When the group owner activates Cancel All Bills, the client shall display a confirmation dialog before sending any request. |
| `CAN-4` | The confirmation dialog shall state that every bill in the group, whether open or settled, will be permanently removed. |
| `CAN-5` | The confirmation dialog shall offer a Confirm option and a Cancel option. |
| `CAN-6` | When the group owner chooses Cancel in the dialog, the system shall make no change to any data. |
| `CAN-7` | When the group owner confirms the dialog, the server shall delete every bill of the group. |
| `CAN-8` | Cancelling all bills shall not change the group's name, group code, members, member list order, or group owner. |
| `CAN-9` | After all bills are cancelled, every client shall show an empty Bills area and all outstanding amounts equal to $0.00 after its next refresh. |
| `CAN-10` | The system shall provide no way for any member other than the group owner to cancel all bills at once. |
| `CAN-11` | The bill details shall provide a Cancel Bill button for the selected bill. |
| `CAN-12` | Only the bill owner of a bill and the group owner shall be allowed to cancel that bill. |
| `CAN-13` | The client shall display the Cancel Bill button enabled only to the bill owner of the selected bill and to the group owner, and only while the client is connected to the server. |
| `CAN-14` | When the user activates Cancel Bill, the client shall display a confirmation dialog before sending any request. |
| `CAN-15` | The confirmation dialog for Cancel Bill shall state the bill's description and total amount and that the bill will be permanently removed, including any shares already confirmed as paid. |
| `CAN-16` | The confirmation dialog for Cancel Bill shall offer a Confirm option and a Cancel option. |
| `CAN-17` | When the user chooses Cancel in the Cancel Bill dialog, the system shall make no change to any data. |
| `CAN-18` | When the user confirms the Cancel Bill dialog, the server shall delete that bill together with its weights, shares, and payment statuses. |
| `CAN-19` | The server shall reject a Cancel Bill request from any member who is neither the bill owner nor the group owner, and shall return a failure reason. |
| `CAN-20` | Cancelling one bill shall not change any other bill, the member list, or the group's name, group code, or group owner. |
| `CAN-21` | When a Cancel Bill request names a bill that no longer exists, the server shall reject it with a not-found result, and the client shall display a message that the bill was already cancelled and refresh. |
| `CAN-22` | After the server confirms a Cancel Bill request, the client shall remove the bill from the Bills area, clear the selection, and update the outstanding amounts immediately. |
| `CAN-23` | After a bill is cancelled, every other client shall no longer show it, and shall show updated outstanding amounts, after its next refresh. |
| `CAN-24` | A bill whose bill owner has left the group shall remain cancellable by the group owner. |

## 11. Outstanding Amounts

> *Derived from the bill and payment-confirmation design (items 5 and 7). Gives members a summary of where they stand.*

| ID | Requirement |
|----|-------------|
| `OUT-1` | The Members area shall display, beside each current member's name, the amount that member owes. |
| `OUT-2` | The Members area shall display, beside each current member's name, the amount that member is owed. |
| `OUT-3` | A member's "owes" amount shall equal the sum of that member's Unpaid shares in bills whose bill owner is another member, current or former. |
| `OUT-4` | A member's "is owed" amount shall equal the sum of all Unpaid shares, belonging to any other member current or former, in bills owned by that member. |
| `OUT-5` | A bill owner's own share shall never be counted in either amount. |
| `OUT-6` | Outstanding amounts shall be calculated from the stored bills and payment statuses and shall not be stored separately. |
| `OUT-7` | Outstanding amounts shall be displayed in dollars with two decimal places and a dollar sign. |
| `OUT-8` | The displayed outstanding amounts shall update immediately after the client's own bill creation, payment confirmation, cancel-bill, or cancel-all action succeeds. |
| `OUT-9` | The Members area shall be scrollable when the member rows exceed the visible height. |

## 12. Synchronization and Connectivity

> *Design reference: multiple users on separate computers.*

| ID | Requirement |
|----|-------------|
| `SYN-1` | While a group screen is open, the client shall request the group's current data from the server every 5 seconds. |
| `SYN-2` | While the home screen is open, the client shall request the user's list of groups from the server every 5 seconds. |
| `SYN-3` | The client shall also request current data immediately after each of its own successful changes. |
| `SYN-4` | A change recorded by the server shall appear on every other connected client that is showing the affected group or home screen within 10 seconds of the server recording it, under normal network conditions. |
| `SYN-5` | When a refresh returns data identical to what the client already displays, the client shall not redraw the screen. |
| `SYN-6` | A refresh shall not clear text the user has typed into the Create Bill form or the Join Group form. |
| `SYN-7` | When a request to the server fails or does not receive a response within 5 seconds, the client shall display a visible banner stating that the server cannot be reached. |
| `SYN-8` | While the server cannot be reached, the client shall continue to display the most recently received data. |
| `SYN-9` | While the server cannot be reached, the client shall disable every control that changes data (Create Group, Join Group, Rename Group, Generate New Code, Leave Group, Create Bill, Change Date, Confirm Payment, Cancel Bill, Cancel All Bills). |
| `SYN-10` | While the server cannot be reached, the client shall retry the refresh every 5 seconds. |
| `SYN-11` | When a retry succeeds, the client shall remove the banner, re-enable the controls, and display the data just received. |
| `SYN-12` | The server shall process changes to one group one at a time, so that simultaneous changes do not interleave. |
| `SYN-13` | The server shall reject a change that is not valid for the group's current state (for example, confirming a share that was already confirmed, or weights naming a member who left), and the client shall display the reason and refresh. |
| `SYN-14` | The client shall not queue changes made while the server is unreachable. |
| `SYN-15` | When a refresh shows that the user is no longer a current member of the open group, the client shall return to the home screen and display a message. |
| `SYN-16` | When a refresh shows that the open group no longer exists, the client shall return to the home screen and display a message. |
| `SYN-17` | When a refresh shows that the open group's membership list has changed, the Create Bill form shall update its weight fields to match the current members without clearing the description and amount fields. |

## 13. Server and Persistence

> *Design reference: data kept between uses.*

| ID | Requirement |
|----|-------------|
| `PER-1` | The server shall store every user, group, member, bill, share, and payment status in a database. |
| `PER-2` | The server shall commit each change to the database before sending a success response for that change. |
| `PER-3` | A change that fails partway shall not be partly applied (each change shall be a single database transaction). |
| `PER-4` | The server shall store all monetary values as integer cents. |
| `PER-5` | The server shall store all weights as integer hundredths. |
| `PER-6` | The server shall preserve member list order and the recorded order of bills. |
| `PER-7` | After the server is stopped and restarted, all users, groups, members, bills, shares, and payment statuses shall be unchanged. |
| `PER-8` | When the server starts with no existing database, it shall create an empty database. |
| `PER-9` | When the server cannot read or write the database, it shall return an error response to the client and shall not report success. |
| `PER-10` | The client shall store locally only the user's username, login token, the server address, and the saved group code for each group. |
| `PER-11` | The server shall provide operations to: create a user, list a user's groups, create a group, open a group (verify the group code), get a group's data, rename a group, generate a new group code, join a group, leave a group, create a bill, change a bill's suggested date, confirm a payment, cancel a bill, and cancel all bills. |
| `PER-12` | Each operation in PER-11 shall return a success or failure result and, on failure, a human-readable reason. |
| `PER-13` | When a group is deleted under MEM-28, the server shall delete all of that group's bills and members in the same database transaction. |

## 14. General and Layout

> *Design reference: item 4 (web based application accessible on desktop and mobile devices).*

| ID | Requirement |
|----|-------------|
| `GEN-1` | The servers should be implemented in Java. The client will be implemented using Javascript, HTML, and CSS. |
| `GEN-2` | The client shall be a responsive web application that runs in modern web browsers on desktop computers, tablets, and mobile phones. |
| `GEN-3` | Users shall access the application through a web browser. The system shall not require a separately installed desktop or native mobile application. |
| `GEN-4` | The web client shall require an internet or network connection to communicate with the server, display shared data, and make changes. |
| `GEN-5` | The web client shall connect to the application's configured server endpoint without requiring users to enter the server address. |
| `GEN-6` | The client shall have three screens: the Create Username screen, the home screen, and the group screen. |
| `GEN-7` | The group screen shall contain four areas: a header with the group name and group controls, the Members area, the Bills area with the bill details, and the Create Bill form. |
| `GEN-8` | The group screen header shall contain the Back to Home, Leave Group, and Cancel All Bills buttons, and for the group owner also the Rename Group, Show Group Code, and Generate New Code controls. |
| `GEN-9` | The client and the server shall perform all money calculations in integer cents and all weight calculations in integer hundredths, never in floating-point arithmetic. |
| `GEN-10` | Under normal network conditions, each user action shall complete and update that client's display within 1 second for groups of up to 20 members and 1,000 bills. |
| `GEN-11` | Every button shall have a visible text label. |
| `GEN-12` | The client shall not terminate with an unhandled exception as a result of any user input permitted by the interface or any server response, including malformed responses. |
| `GEN-13` | The server shall not terminate with an unhandled exception as a result of any request it receives. |
| `GEN-14` | The client shall display error messages in a way that does not hide the screen contents permanently (for example, a dismissible message or dialog). |
