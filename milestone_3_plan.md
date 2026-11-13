## How will you coordinate this work?

### Who will coordinate the work?
Our project manager, Garvit Bhasin, coordinates the work. He keeps track of what needs to be done, who is responsible for each piece, and when it is due, and he follows up with teammates when something is falling behind.

### What will our project management practices be? STILL NEED TO DO THIS 


### Will we have meetings? How frequently? Who plans the agenda?
We will have a team meeting once a week, typically on Wednesdays, to review progress and plan the week ahead. Garvit plans the agenda and shares it in the group chat at least a day before the meeting. Teammates can add items to the agenda before then. A typical agenda covers what was finished since the last meeting, what is blocked, what each person will do this week, and any decisions the team needs to make. Meeting notes and action items are written in a shared Google Doc.
## What tools will you use to communicate?


### Team chat: Instagram group chat
- **Alternatives:** Discord, Microsoft Teams
- **Why Instagram is the best choice:** Every team member already has Instagram and checks it daily, so messages are seen quickly and nobody has to install or sign up for anything new. Discord and Slack organize conversations better with channels and searchable history, but a new app risks teammates forgetting to check it. Email is too slow for day-to-day coordination.
- **Trade-off and mitigation:** Older messages get buried in a long chat. We will pin the GitHub repository link and meeting times, and post important decisions as short one-line messages.

### Code and task tracking: GitHub
- **Alternatives:** Google Drive, Trello, Notion, sharing files through chat.
- **Why GitHub is the best choice:** The course already requires it, so our requirements, architecture, and code all live in one place with full version history. GitHub Issues let us assign and track tasks, and pull requests let teammates review each other's changes before they are merged. Trello or Notion would add a second place to track work, and files sent through chat are hard to find later and cannot be versioned.

### Shared documents and meeting notes: Google Docs
- **Alternatives:** Microsoft Word with OneDrive, Notion, Markdown files in GitHub.
- **Why Google Docs is the best choice:** It is free, needs no install, and lets several people edit the same document at once with live cursors, comments, and suggested edits. Version history lets us undo mistakes. Word with OneDrive can do the same but is slower to set up for everyone, and Markdown files in GitHub are better for the course deliverables than for quick drafts and notes that teammates want to comment on.
- **Trade-off and mitigation:** Final versions of the course deliverables (such as the requirements and architecture) must end up in the GitHub repository. We will copy them there once the team agrees on them, so GitHub stays the source of truth.

### Meetings and screen sharing: Instagram video calls (Zoom or Discord as backup)
- **Alternatives:** Zoom, Google Meet, Discord, meeting in person.
- **Why Instagram calls are the best choice:** They start directly from our group chat, so there are no links or schedules to set up. When we need to walk through code and share a screen, we will use Zoom or Discord, which handle screen sharing better.
## Who will own component in your architecture?


Each component has exactly one owner. Owning a component means being responsible for writing it and making sure it is functional and correct.
| Component | Layer | Where it runs | Owner |
|-----------|-------|---------------|-------|
| Credentials | Model | Shared (client and server) | Rory |
| GroupAccess | Model | Shared (client and server) | Rory |
| Result | Model | Shared (client and server) | Rory |
| NewBill | Model | Shared (client and server) | Tyler |
| GroupSummary | Model | Shared (client and server) | Garvit |
| Member | Model | Shared (client and server) | Eason |
| BillShare | Model | Shared (client and server) | Tyler |
| Bill | Model | Shared (client and server) | Tyler |
| GroupSnapshot | Model | Shared (client and server) | Eason |
| InputValidator | Model | Shared (client and server) | Garvit |
| Formatting | Model | Shared (client and server) | Nik |
| FailureReasons | Model | Shared (client and server) | Eason |
| User | Model | Server only | Nik |
| GroupMember | Model | Server only | Nik |
| Group | Model | Server only | Nik |
| UserRepository | Model | Server only | Nik |
| GroupRepository | Model | Server only | Nik |
| SplitCalculator | Model | Server only | Nik |
| GroupCodeGenerator | Model | Server only | Nik |
| TokenGenerator | Model | Server only | Nik |
| SessionModel | Model | Client only | Rory |
| GroupListModel | Model | Client only | Nik |
| GroupModel | Model | Client only | Eason |
| ConnectionModel | Model | Client only | Rory |
| BillFormModel | Model | Client only | Tyler |
| OutstandingModel | Model | Client only | Tyler |
| AuthGuard | Controller | Server only | Nik |
| GroupLockManager | Controller | Server only | Nik |
| UserApiController | Controller | Server only | Rory |
| GroupApiController | Controller | Server only | Garvit |
| BillApiController | Controller | Server only | Tyler |
| ServerConnection | Controller | Client only | Rory |
| AppController | Controller | Client only | Eason |
| UsernameController | Controller | Client only | Rory |
| HomeController | Controller | Client only | Garvit |
| GroupAdminController | Controller | Client only | Garvit |
| BillActionController | Controller | Client only | Tyler |
| SyncController | Controller | Client only | Eason |
| UsernameView | View | Client only | Rory |
| HomeView | View | Client only | Garvit |
| CreateGroupView | View | Client only | Tyler |
| JoinGroupView | View | Client only | Rory |
| GroupCodePromptView | View | Client only | Garvit |
| GroupView | View | Client only | Eason |
| MemberListView | View | Client only | Eason |
| BillListView | View | Client only | Tyler |
| BillDetailsView | View | Client only | Eason |
| CreateBillView | View | Client only | Tyler |
| ConnectionBannerView | View | Client only | Nik |
| ConfirmDialogView | View | Client only | Tyler |
| ErrorMessageView | View | Client only | Rory |

### Summary by person

| Owner | Components | Count |
|-------|------------|-------|
| Nik | GroupRepository, UserRepository, Group, SplitCalculator, GroupLockManager, AuthGuard, TokenGenerator, GroupCodeGenerator, Formatting, ConnectionBannerView, GroupListModel, User, GroupMember | 13 |
| Rory | ServerConnection, SessionModel, UsernameView, UserApiController, UsernameController, ConnectionModel, ErrorMessageView, JoinGroupView, Credentials, GroupAccess, Result | 11 |
| Garvit | GroupApiController, InputValidator, HomeController, HomeView, GroupAdminController, GroupCodePromptView, GroupSummary | 7 |
| Tyler | BillApiController, BillFormModel, BillActionController, CreateBillView, BillListView, OutstandingModel, ConfirmDialogView, CreateGroupView, NewBill, BillShare, Bill | 11 |
| Eason | SyncController, GroupView, BillDetailsView, AppController, GroupModel, MemberListView, Member, GroupSnapshot, FailureReasons | 9 |

Two additional source files that are not in the architecture document, `ServerMain` and `ClientMain` (the programs that start the server and the client and connect everything), are owned by Rory.
## What is your timeline?
Our team meets every Wednesday (Nov 18 and Nov 25), and those meetings are our checkpoints. Our own target dates are set a day or more before each course deadline to leave time for review.

This is our tentative deadline schedule leading up to the 1.0 release of our project. 

| Deadline | Milestone / Goal |
|----------|------------------|
| Fri, Nov 13 | **Milestone 3: Plan** submitted on the `milestone-3` branch |
| Sun, Nov 15 | Tasks created as GitHub Issues and assigned to teammates |
| Fri, Nov 20 | Server and shared code complete |
| Sun, Nov 22 | Client complete and connected to the server (all features working end to end) |
| Tue, Nov 24 | Testing against our requirements finished and bugs logged |
| Thu, Nov 26 | Bugs fixed and a final full test done, so we are ready to submit a day early |
| Fri, Nov 27 | **Milestone 4: Release** submitted |

If we fall behind, we cut the least essential features rather than miss a deadline, since late submissions receive a 0.
## How will you verify you met your requirements?

This is our acceptance testing plan. We have five teammates and about two weeks, so we picked methods we can actually finish. Every requirement ID in the requirements document is covered below, either by a check or by a written reason why it isn't checked.

**Assumptions**
- Tests of Java code use JUnit. If the client ends up in JavaScript (GEN-1), client tests use that language's test tool instead (for example Jest). The idea is the same.
- Where the requirements disagree with each other (USR-2 says the Create Username screen has a server address field, GEN-5 says users never enter one), we test the final decision and fix the requirements doc to match.


---

### 1. The four methods and how each one works

| Method | How it works | What counts as passing | Who |
|---|---|---|---|
| **Test** | A JUnit test written in code. Each test has the requirement ID in its name (for example `USR_3_rejects_two_character_username`). Tests that need data use a fresh temporary database. Tests that involve dates use a fake clock we control. | Every assertion in the test is true: the result (success or failure, and the failure reason) and the saved data are what we expected. | The owner of the component writes it. A teammate reads it in the pull request. |
| **Script** | A numbered manual script (section 3). Two people (or two browser windows) follow it step by step. Each step has a **Do** and an **Expect**. | Every **Expect** is seen. If one is not, the script fails and we log a GitHub Issue with the requirement ID. | A teammate who did **not** write that feature. |
| **Review** | A teammate inspects the code using a checklist (section 4). They read the listed files, search the code for specific patterns, and trace each server operation from start to finish. | Every checklist question is answered "yes", with the file and line they looked at. | A teammate who is **not** the component owner in our ownership table. |
| **Proof** | A short written argument (section 5) plus a test with 1,000 random cases. | The argument holds, and the random test finds no case that breaks it. | Whoever owns `SplitCalculator`, checked by one other teammate. |

---

### 2. Requirement by requirement

### Users (USR-1 to USR-18)

| Requirements | Method | What exactly we do | Passes when |
|---|---|---|---|
| USR-3, 4, 5, 6, 7, 10 | Test | `validateUsername` on lengths 0, 2, 3, 20, 21; on `abc_123`; on a name with a space, `-`, `@`. Create `Alice`, then `alice`, then `ALICE`. Create two users and compare tokens. For USR-5, give the client controller a bad name using a pretend server. | Bad names are rejected with a non-empty reason, and the pretend server gets no call. `alice` and `ALICE` fail as "taken". The stored name is `Alice`. Tokens are at least 32 characters and differ. |
| USR-14, 15 | Test + Review R2 | Call every server operation with no token, a wrong token, and user A's name with user B's token. Convert every response to text and search it for any token. | Every call fails. A token appears only in the `createUser` response. |
| USR-11, 12, 16 | Test + Script M1 | Save and reload the config file. Try a missing file and a garbage file. | Reloads correctly. A bad file means "no identity" with no crash. |
| USR-1, 2, 8, 13, 17, 18 | Script M1, M2 | See script. | Every Expect is seen. |
| USR-9 | Review R3 | Is there any code or operation that changes a username? | No. |

### Groups (GRP-1 to GRP-25)

| Requirements | Method | What exactly we do | Passes when |
|---|---|---|---|
| GRP-6, 7, 8 | Test | Create groups named `""`, `"   "`, `" Trip "`, and two groups with the same name. | Blank is rejected. Both same-name groups exist. |
| GRP-9, 10, 11, 12 | Test + Review R2 | Generate 1,000 codes and check each matches 6 characters from A–Z (no I, O) and 2–9. Create a group and read it back. Make the generator return a used code first, then a new one. Review: the generator uses a random source. | All codes are valid. The creator is member #1 and owner with the name from the form. The second code is used. |
| GRP-14, 18, 20 | Test | Open a group as a member and a non-member, with the right code, a wrong code, an old code, and a lowercase code. Check that group A's response contains nothing from group B. | Only a member with the current code (any letter case) gets in. Failures return the expected failure reason. |
| GRP-1–5, 13, 15, 16, 17, 19, 21–25 | Script M2 | See script. | Every Expect is seen. |

### Group owner (OWN-1 to OWN-21)

| Requirements | Method | What exactly we do | Passes when |
|---|---|---|---|
| OWN-4–9, 12, 15, 16, 18 | Test | Rename, generate a new code, and cancel all bills, as the owner and as a normal member. Check the code field in a normal member's group data. Use the old code after regenerating. | A normal member is rejected with a reason and nothing changes. Their group data has no code. The old code fails and the new one works. Members and bills are the same before and after. |
| OWN-1, 19, 20 | Test + Review R4 | The owner leaves a group of 3, then of 2. Force the database to fail halfway through a leave. | The earliest remaining member becomes owner. There is always exactly one owner. After the forced failure, nothing changed. |
| OWN-3 | Test + Review R3 | Try to join with a wrong code. Review: the only code that adds a member is `joinGroup`. | The join fails. No other path exists. |
| OWN-2, 10, 11, 13, 14, 17, 21 | Script M2, M3 | See scripts. | Every Expect is seen. |

### Members (MEM-1 to MEM-31)

| Requirements | Method | What exactly we do | Passes when |
|---|---|---|---|
| MEM-5–15 | Test | Join with a lowercase code, twice, and with an unknown code. Names `" Bob "`, `""`, and a name matching a current member in different letter case. One user joins two groups under different names. Two users use the same name in different groups. | Each case succeeds or fails as the requirement says, with the right message. Names are trimmed. The new member is last in the list with a join time. |
| MEM-20–24, 26–29, 31 | Test + Review R3 | A member with unpaid bills leaves. Then try to access the group as them. The last member leaves. A former member rejoins. Review: no operation removes another member. | Bills, weights, and shares are identical before and after leaving. The former member is rejected. The last member leaving deletes the group and its bills. The rejoined member is at the end of the list. A former member's unpaid share can still be confirmed. |
| MEM-1–4, 16–19, 25, 30 | Script M2, M7 | See scripts. | Every Expect is seen. |

### Creating bills (BIL-1 to BIL-37) and splitting (SPL-1 to SPL-9)

| Requirements | Method | What exactly we do | Passes when |
|---|---|---|---|
| BIL-10–14, 20, 34 | Test | Parse amounts `12.5`, `12.50`, `0.1`, `0.30`, `12.345`, `abc`, `0`, `-5`. Parse weights `0`, `1`, `1.00`, `0.005`, `1.01`, `-0.1`. Description `"  hi "` and `"   "`. | Dollars become exact cents (`0.30` is exactly 30). Bad inputs are rejected. The description is trimmed. |
| BIL-16, 17, 18, 19 | Test + Proof P2 | Weight sums of 0.99, 1.00, 1.01. Split Equally for 1 to 20 members. | Only exactly 1.00 is accepted. The equal split sums to 1.00 and the first `100 mod n` members get the extra hundredth. |
| BIL-8, 30, 31, 32, 33, 35 | Test | Submit bills with a bad amount, a weight for someone who left, a missing member, a bad weight sum, a past date, and an empty description. Then submit a good bill. | Each bad bill is rejected and nothing is saved. The good bill is saved with all required fields, the caller as owner, and the owner's own share already confirmed. |
| BIL-37 | Test | Give the client controller a pretend server that returns a failure. | The form text is unchanged and an error message is shown. |
| BIL-1–7, 9, 15, 21–29, 36 | Script M4 | See script. | Every Expect is seen. |
| SPL-1–7 | Test | $10.00 split three ways equally. $0.01 split two ways. One person with weight 1.00. A zero weight. | $3.34, $3.33, $3.33. $0.01 / $0.00. The full amount to the one person. $0 for the zero weight. |
| SPL-4 | Proof P1 + Test | 1,000 random bills (random total, members, weights summing to 1.00). | In every case the shares add up to exactly the total, and no one gets more than 1 cent above their rounded-down share. |
| SPL-8, 9 | Test + Script M5 | Create a bill, change the membership, re-read the bill. Show a bill with a zero-weight member. | Shares are unchanged. The zero-weight member is not shown. |

### Viewing bills, dates, payments, cancelling (BLD, DAT, PAY, CAN)

| Requirements | Method | What exactly we do | Passes when |
|---|---|---|---|
| BLD-8, 9, 10 | Test | Format 1250 cents, 5 cents, -1250 cents, weight 25, a date, and no date. | `$12.50`, `$0.05`, `-$12.50`, `0.25`, `2026-11-20`, `No date`. |
| BLD-1–7, 11–14 | Script M5 | See script. | Every Expect is seen. |
| DAT-3, 4, 8, 10 | Test | Dates `2026-02-30`, `26-1-1`, yesterday, today, tomorrow (fake clock). A non-owner changes a date. Move the clock 30 days past a suggested date. | Only valid, not-in-the-past dates are accepted. The non-owner is rejected. Nothing changes when the date passes. |
| DAT-1, 2, 5, 6, 7, 9, 11 | Script M5 | See script. | Every Expect is seen. |
| PAY-1, 4–8, 12 | Test | The bill owner, another member, and the group owner (bill owner present, then bill owner left) each try to confirm. Confirm the same share twice. Confirm one share and check the others. | Only the bill owner (or group owner if the bill owner left) succeeds. The second confirm fails with no change. Other shares are untouched. No operation turns a Paid share back to Unpaid. |
| PAY-2, 3, 9, 10 | Script M6 | See script. | Every Expect is seen. |
| PAY-11 | Review R3 | Is there any code that moves money? | No. |
| CAN-7, 8, 10, 12, 18–21, 24 | Test | Cancel one bill and cancel all bills as bill owner, group owner, and another member. Cancel a bill that is already gone. Cancel a bill whose owner left. | Only allowed roles succeed. Only the targeted bills disappear (with their shares). Group, members, and owner are unchanged. A missing bill returns "bill not found". |
| CAN-1–6, 9, 11, 13–17, 22, 23 | Script M6 | See script. | Every Expect is seen. |

### Outstanding amounts (OUT-1 to OUT-9)

| Requirements | Method | What exactly we do | Passes when |
|---|---|---|---|
| OUT-3, 4, 5, 6 | Test + Review R3 | Build bills by hand with known answers: a bill owner who left, a former member's unpaid share, an owner's own share, all paid. Also check total "owes" equals total "is owed". Review: amounts are calculated, never stored. | Amounts match our hand calculation. The two totals are always equal. |
| OUT-1, 2, 7, 8, 9 | Script M6, M7 | See scripts. | Every Expect is seen. |

### Syncing, saving, and general (SYN, PER, GEN)

| Requirements | Method | What exactly we do | Passes when |
|---|---|---|---|
| SYN-1–11, 14, 17 | Script M8 | See script. | Every Expect is seen. |
| SYN-12 | Test | 50 users confirm the same share at the same moment. 20 users join with the same name at the same moment. | Exactly one succeeds each time and the data is consistent. |
| SYN-13, 15, 16 | Test | Pretend server returns "not a member", "group not found", "share already confirmed". | The client shows a reason and refreshes. For the first two it returns to the home screen with a message. |
| PER-1–8, 13 | Test | Create users, groups, bills, and payments. Stop and restart the server on the same database. Start with no database file. Look at how money and weights are stored. Delete a group. | Everything is identical after restart. A new empty database is created. Money is whole cents and weights are whole hundredths. No rows of a deleted group remain. |
| PER-3, 9 | Test | Force the database to fail partway through multi-step changes. Make the database unreadable. | Nothing is partly saved. The server returns an error and never reports success. |
| PER-10, 11, 12 | Review R6, R3 | What does the client save? Does every operation in the PER-11 list exist and return success or a reason? | Only the allowed items are saved. All operations exist and return a result with a reason on failure. |
| GEN-1 | Review R6 | Which languages are the server and client written in? | Server in Java. Client in JavaScript, HTML, CSS. |
| GEN-9 | Review R1 | See checklist. | See checklist. |
| GEN-10 | Test | A database with 20 members and 1,000 bills. Time each server operation. | Every operation takes under 1 second. |
| GEN-12, 13 | Test + Review R5 | Send garbage, empty, and huge requests to the server. Give the client garbage and cut-off responses from a pretend server. | No crash. A failed result and a message instead. |
| GEN-2, 3, 4, 5, 6, 7, 8, 11, 14 | Script M1, M8, M9 | See scripts. | Every Expect is seen. |

---

### 3. Manual test scripts

Use two browser windows: **A** (Alice, username `Alice_1`, group name "Al") and **B** (Bob, username `Bob_1`, group name "Bo"). Start with a fresh database.

### M1: First launch (USR-1, 2, 8, 12, 13, 16, 17, 18; GEN-4, 5)

| # | Do | Expect |
|---|---|---|
| 1 | Open the app in A with no saved data | Create Username screen shows first. It has a username box and a Create button. No password or email box. |
| 2 | Type `ab` | Create is disabled |
| 3 | Type `Alice_1` | Create is enabled |
| 4 | Stop the server | Create is disabled |
| 5 | Start the server and press Create | Home screen shows `Alice_1` |
| 6 | Reload the page | Goes straight to Home, no Create screen. `Alice_1` still shown. |
| 7 | Damage the saved config and reload | Create Username screen with a notice that the old name can't be recovered |

### M2: Groups, joining, codes (GRP-1–5, 13, 15, 16, 17, 19, 21–25; OWN-3, 13, 14, 17; MEM-1–4, 16, 17)

| # | Do | Expect |
|---|---|---|
| 1 | In A (new user), look at Home | "No groups yet" message and a Create Group button |
| 2 | Create a group "Trip" with your name "Al" | Group screen opens with the name "Trip". Members list shows "Al" with "Owner" and "you". |
| 3 | In A, show the group code | A 6-character code appears |
| 4 | In B (new user `Bob_1`), press Join Group | Submit is disabled while code or name is empty |
| 5 | Enter the code in lowercase and the name "Bo" | Submit is enabled. After submitting, the form closes and the group screen opens. B's Home lists "Trip". |
| 6 | In A, wait up to 10 seconds | "Bo" appears in the Members list. B's name is different from `Bob_1`. |
| 7 | In B, press Back to Home, then open "Trip" again | Opens without asking for a code |
| 8 | In A, press Generate New Code | A shows the new code and is not asked for anything |
| 9 | In B, wait up to 10 seconds | B is returned to the Home screen with a message, then asked for the code when opening "Trip" |
| 10 | In B, enter the old code, then the new code | Old code shows an error and does not open. New code opens the group. |
| 11 | In B, try to join "Trip" again with the new code | A message says you are already a member |

### M3: Owner-only controls (OWN-2, 10, 11, 13, 14, 21)

| # | Do | Expect |
|---|---|---|
| 1 | Look at B's group screen | No Rename Group, Show Group Code, Generate New Code, or Cancel All Bills controls |
| 2 | Look at A's group screen | All four owner controls are present |
| 3 | In A, rename the group to "Trip 2" | A's header changes immediately. B's within 10 seconds. |
| 4 | In A, press Leave Group and confirm | A returns to Home with no group. After 10 seconds, B shows "Owner" next to Bo and shows the owner controls. |

*(Re-join A to the group with the code before continuing.)*

### M4: Create Bill form (BIL-1–7, 9, 15, 21–29, 36)

| # | Do | Expect |
|---|---|---|
| 1 | Open the Create Bill form in A | Description, amount, one weight box per current member (all 0), an optional date box. No way to choose a bill owner. Create Bill is disabled. |
| 2 | Type a description only | Create Bill still disabled |
| 3 | Enter amount `12.345` | Disabled |
| 4 | Enter amount `10.00` and weight `1.5` for Al | The weight box shows an error mark. Disabled. |
| 5 | Enter weights `0.50` and `0.40` | Weight sum shows `0.90` marked as an error. Disabled. |
| 6 | Press Split Equally | Both weights `0.50`, sum `1.00`, Create Bill enabled |
| 7 | Enter date `2026-02-30` | Disabled |
| 8 | Clear the date | Enabled |
| 9 | Stop the server | Disabled. Start the server again. |
| 10 | Press Create Bill | Form clears (text boxes empty, weights back to 0). The bill appears at the top of the Bills list at once. |

### M5: Viewing bills and dates (BLD-1–7, 11–14; DAT-1, 2, 5, 6, 7, 9, 11; SPL-8, 9)

| # | Do | Expect |
|---|---|---|
| 1 | In A, create a bill "Pizza", $10.00, 0.50 / 0.50, with a valid future date | Row shows "Pizza", `$10.00`, "Al", the date as `yyyy-mm-dd`, and "0 of 1 paid" |
| 2 | Select the row | Details show Al `0.50` `$5.00` Paid, Bo `0.50` `$5.00` Unpaid. The bill is marked Open. |
| 3 | Look at the bill in B | Same details, readable by B. Change Date is disabled or hidden for B. |
| 4 | In A, change the date, then clear it | Row shows the new date, then "No date". Shares and Paid/Unpaid are unchanged. |
| 5 | Create a second bill with weight 1.00 for Al and 0 for Bo | Details do not list Bo. The newest bill is at the top. |
| 6 | Let a refresh happen with a bill selected | The same bill stays selected |
| 7 | Create enough bills to overflow the list | The list scrolls |

### M6: Payments and cancelling (PAY-2, 3, 9, 10; CAN-1–6, 9, 11, 13–17, 22, 23; OUT-1, 2, 7, 8)

| # | Do | Expect |
|---|---|---|
| 1 | In the Members area, look at Al and Bo | Bo "owes" `$5.00`, Al "is owed" `$5.00` (amounts use `$` and two decimals) |
| 2 | In B, select "Pizza" | Confirm Payment and Cancel Bill are disabled |
| 3 | In A, select "Pizza" | Confirm Payment is next to Bo's Unpaid share |
| 4 | Stop the server | Confirm Payment is disabled. Start the server again. |
| 5 | In A, press Confirm Payment | Bo's share shows Paid, the row shows "1 of 1 paid", the bill shows Settled, and both owes/is-owed amounts are `$0.00`, all immediately |
| 6 | In A, press Cancel Bill | A dialog shows the bill's description and amount and says it will be removed permanently |
| 7 | Choose Cancel in the dialog | Nothing changes |
| 8 | Press Cancel Bill and confirm | The bill disappears and the selection clears. B's list updates within 10 seconds. |
| 9 | In B, look for Cancel All Bills | Not visible or disabled |
| 10 | In A, press Cancel All Bills | A dialog says every bill, open or settled, will be removed. Cancel changes nothing. Confirm removes everything. "No bills yet" shows and all amounts are `$0.00` in both browsers. |

### M7: Leaving a group (MEM-17–19, 22, 25, 30; OUT-8, 9)

| # | Do | Expect |
|---|---|---|
| 1 | In B, create a bill "Snacks" (Al 0.50, Bo 0.50) | Bill appears |
| 2 | In B, press Leave Group | A confirm dialog appears first. Nothing is sent until confirmed. |
| 3 | Confirm | B returns to Home and "Trip" is gone from B's list |
| 4 | In A, wait up to 10 seconds | "Bo" is gone from the Members list. "Snacks" shows the owner as "Bo (left)". The Create Bill form has no weight box for Bo. |

### M8: Syncing and connection loss (SYN-1–11, 14, 17; GEN-4)

| # | Do | Expect |
|---|---|---|
| 1 | In B, rejoin the group. In A, start typing a description in Create Bill. | A new weight row for Bo appears in A's form within 10 seconds, and A's typed description is still there |
| 2 | In B, create a bill | It appears in A within 10 seconds without reloading |
| 3 | Leave both windows alone | Screens don't flicker or redraw when nothing changed |
| 4 | Stop the server | Within about 5 seconds a banner says the server can't be reached. Old data stays. Create Group, Join Group, Rename, Generate Code, Leave, Create Bill, Change Date, Confirm Payment, Cancel Bill, and Cancel All are disabled. |
| 5 | While the server is down, try to change something | Nothing is queued |
| 6 | Start the server again | Within about 5 seconds the banner goes away, controls are enabled, and the latest data shows |

### M9: Layout (GEN-2, 3, 6, 7, 8, 11, 14)

| # | Do | Expect |
|---|---|---|
| 1 | Open the app in desktop Chrome, desktop Firefox, a narrow window, and a real phone | Works in all. No install needed. |
| 2 | Go through all screens | Only three: Create Username, Home, Group |
| 3 | Look at the group screen | Four areas: header with group name and controls, Members, Bills with details, Create Bill form. Header has Back to Home, Leave Group, Cancel All Bills, plus owner controls for the owner. |
| 4 | Check every button | Each has a visible text label |
| 5 | Cause an error (for example, a wrong group code) | The message can be dismissed and does not block the screen permanently |

---

### 4. Code review checklists

**How we review.** The reviewer opens the files named in the checklist, answers each question with the file and line they looked at, and writes "yes" or "no" in the pull request. Where a checklist says *search*, they use the IDE's find-in-project or `grep` and look at every hit. Where it says *trace*, they pick each server operation and follow it from the controller to the repository and back.

| # | Requirements | Files | What the reviewer does | Passes when |
|---|---|---|---|---|
| **R1** Money and weights | GEN-9, PER-4, PER-5, BIL-13 | Whole repo, database setup | *Search* for `float`, `double`, `Double`, `parseDouble`, `Math.round`. For every hit, decide if it touches money or weights. Open the table creation code and check money and weight columns are integer types. | No hit touches money or weights. Money and weight columns are integers. |
| **R2** Server security | USR-10, 14, 15, GRP-11, 18, 20, OWN-8, 9 | `UserApiController`, `GroupApiController`, `BillApiController`, `AuthGuard`, `TokenGenerator`, `GroupCodeGenerator` | *Trace* every operation in the three controllers. Check the first thing it does is call `AuthGuard` (except `createUser`). *Search* for every read of the token and check none ends up in a response. Check the group code is put in a response only when the caller is the owner. Check the token and code generators use a secure random source. | Every operation except `createUser` authenticates first. Token appears only in the `createUser` response. Code is sent only to the owner. Generators use a random source. |
| **R3** Forbidden things | USR-9, 17, MEM-31, OWN-3, PAY-11, PAY-12, OUT-6, PER-11, PER-12 | All controllers, `GroupRepository` | List every server operation and every database update. Check none renames a user, removes another member, turns Paid back to Unpaid, or moves money. Check members are only added in `joinGroup` (and `createGroup` for the creator). Check outstanding amounts are calculated, not stored. Check every PER-11 operation exists and returns a result with a reason. | No forbidden operation exists. Every PER-11 operation exists. |
| **R4** Changes are safe | OWN-20, PER-3, PER-13, SYN-12, MEM-28 | `GroupApiController`, `BillApiController`, `GroupRepository`, `GroupLockManager` | *Trace* every operation that changes data. Check the order is: lock, reload the group, re-check the rules, one repository call, unlock in a `finally` block. Check each repository write is one database transaction (leave + new owner, delete group + bills). | Every change follows that order. Multi-step writes are single transactions. |
| **R5** No crashes | GEN-12, 13, PER-9 | All controllers, `ServerConnection` | Check every controller method turns `StorageException` and bad input into a failed `Result`. Check `ServerConnection` handles timeouts and badly formed responses without throwing. | No path lets an exception escape. |
| **R6** Local storage and setup | PER-10, GEN-1 | `SessionModel`, project folders | Read what `SessionModel` writes to the file. Check the languages of the server and client. | Only username, token, server address, and group codes are saved. Server is Java, client is JavaScript/HTML/CSS. |

---

### 5. Proofs

**P1: shares always add up to the total (SPL-4).**
*Property:* for any total `T` in cents and weights that sum to exactly 1.00, the shares from `SplitCalculator` add up to exactly `T`, and nobody gets more than one extra cent.

*Why it's true:* Each person's exact share is a number like 333.33 cents. Rounding it down loses a bit less than 1 cent. All the exact shares add up to `T`, so the cents lost add up to a whole number. That whole number is smaller than the number of people (each person loses less than 1). So if we give one leftover cent each to the first few people, we never run out of people, nobody gets two, and the total becomes exactly `T`.

**P2: Split Equally adds up to 1.00 (BIL-19).**
*Property:* for any number of members `n`, the equal weights add up to exactly 100 hundredths.

*Why it's true:* Everyone gets `100 ÷ n` rounded down. The leftover is `100 mod n`, which is always less than `n`. Giving one extra hundredth to that many members (the first ones in order) uses up the leftover exactly.

For both, the 1,000-random-case test checks that our *code* does what the argument says.

---

### 6. How this fits into our process

| When | What happens | Who |
|---|---|---|
| **Every build** | The tests run as part of the build (`mvn test` or `gradle test`). A build with a failing test counts as a failed build. | Everyone, on their own machine |
| **Before every commit** | Run the tests for the component you changed. If one is red, don't commit. | The person committing |
| **Before every merge into `main`** | Run the whole test suite. A teammate who is not the component owner reads the code and fills in the review checklists that apply (section 4). The pull request is merged only when all tests pass and the reviewer agrees. | The author and one reviewer |
| **Wed Nov 18 meeting** | Look at which tests exist and which fail. Do reviews R1 and R3 on the code that exists so far. | Whole team, Garvit leads the agenda |
| **Fri Nov 20 (server and shared code complete)** | Run the full server test suite. Do reviews R2, R4, R5, and R6 on the server. For example, `AuthGuard` and `GroupLockManager` (owned by Nik) are reviewed by Rory and Garvit. | Reviewers are never the owner of the code |
| **Sun Nov 22 (client complete)** | Run all tests. Run scripts M1 to M3 right away to catch big problems. | Anyone who did not write the feature |
| **Tue Nov 24 (testing day)** | Run every script M1 to M9 and any tests not yet run. Log each failure as a GitHub Issue with the requirement ID in the title. | Teammates who did not write each feature. Garvit assigns the scripts. |
| **Wed Nov 25 meeting** | Review the open Issues and decide what to fix first | Whole team |
| **Thu Nov 26** | Rerun all tests and the scripts that failed before. Update the table. | Whole team |
| **Always** | `docs/acceptance-matrix.md` lists every requirement ID, its method, and whether it passed | Garvit keeps it up to date |

If we run out of time, we finish tests and reviews for money, splitting, and permissions first (SPL, BIL, PAY, CAN, OWN, USR-14/15, GRP-14/18/20), and shorten M9 (layout) last. This matches our rule of cutting the least essential features instead of missing a deadline.

---

### 7. What we are not verifying, and why

| Requirement | What we skip | Why |
|---|---|---|
| GEN-10 (speed) | Speed on slow or very busy real networks | The requirement only promises speed under normal network conditions. We test the server's speed locally with 20 members and 1,000 bills. |
| GEN-2 (all modern browsers and devices) | Every browser and device | We test desktop Chrome, desktop Firefox, a narrow window, and one real phone. Testing every device is not possible with our time and equipment. |
| SYN-4 (10-second update) | Timing with many users at once | We test with two browsers. The class is a small group app, so this is enough. |
| USR-10, GRP-11 (randomness) | Proving the numbers are truly random | A test cannot prove randomness. We test length and allowed characters, and review that the secure random source is used. |
| Weight-sum edge cases like 0.99 vs 1.00 shown on phone screens | Pixel-level display checks | We check what text appears. We don't check exact fonts or spacing since the requirements don't specify them. |

---

### 8. Why we made these choices

- **Tests for rules.** Validation, splitting, permissions, and saving have clear right answers, and a mistake would break trust in the app (wrong money, wrong permissions). Tests can be rerun in seconds after every change, which matters because five people are changing the same code for two weeks.
- **Temporary database and fake clock.** Saving, restart, and date rules (DAT-4, DAT-10) only mean something against a real database and a clock we control. Pretend versions would hide the exact bugs we care about.
- **Pretend server for client tests.** We can then test "server says no" and "server unreachable" without breaking the real server, which would be slow and hard to repeat.
- **Scripts for screens and syncing.** Whether a button is disabled, a banner shows, or another browser updates within 10 seconds is something only a person watching can judge. Writing the steps down makes it repeatable and means everyone checks the same thing.
- **Reviews for "never" and security requirements.** A test proves something only for the cases we thought of. "The token is never sent anywhere else" is about every path in the code, and a reader tracing each operation can find a path nobody tested.
- **Proofs for the two arithmetic rules.** Our app handles money, and a few example tests can't show that the shares add up for *every* total and set of weights. The proofs cover all cases and the random test checks our code matches them.
- **A different person checks each piece.** The author tends to test what they already know works. A teammate follows the written script or checklist and notices what the author misses.
- **Tests on every build and before every commit, plus review before every merge.** Catching a problem seconds after it's written is far cheaper than finding it on Nov 24. The review before merging stops one person's mistake from breaking everyone else's work.
- **Review and test dates after the matching milestone.** Server reviews wait until Nov 20 because the server is not complete before then. Full scripts wait until Nov 24 because the client isn't connected until Nov 22. That leaves Nov 24 to Nov 26 to fix what we find.
- **Written reasons for what we skip.** We would rather say plainly what we do not check than pretend to check it.
