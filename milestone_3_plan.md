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

### How will you verify you met your requirements?

Five teammates, about two weeks, so we use simple methods we can finish. Every requirement ID is covered below, or listed in [what we skip](#7-what-we-skip-and-why) with a reason.

**Assumptions:** Java code is tested with JUnit (client tests use the client language's tool, e.g. Jest, if it is JavaScript). USR-2 (server address field) and GEN-5 (no address entry) contradict each other, so we test whichever one we keep and fix the requirements doc.

#### 1. Methods

| Method | How it works | Passes when | Who |
|---|---|---|---|
| **Test** | JUnit test named with the requirement ID (e.g. `USR_3_rejects_short_name`). Uses a fresh temporary database and a fake clock. | Every assertion is true (result, failure reason, saved data). | Component owner writes it; a teammate reads it in the PR. |
| **Script** | Numbered manual steps (section 3) in two browsers, each with a Do and an Expect. | Every Expect is seen. Failures become GitHub Issues tagged with the requirement ID. | A teammate who didn't write the feature. |
| **Review** | A teammate follows a checklist (section 4): open the listed files, search the code for patterns, trace each server operation. | Every question answered "yes" with file and line. | A teammate who isn't the component owner. |
| **Proof** | Short argument (section 5) plus a 1,000-random-case test. | The argument holds and no random case breaks it. | `SplitCalculator` owner, checked by one other person. |

#### 2. Requirement by requirement

##### Users (USR)

| IDs | Method | What we do | Passes when |
|---|---|---|---|
| 3–7, 10 | Test | Names of length 0, 2, 3, 20, 21; `abc_123`; names with space, `-`, `@`. Create `Alice`, `alice`, `ALICE`. Two tokens compared. | Bad names rejected with a reason and no server call. `alice` fails as taken. Stored name is `Alice`. Tokens ≥ 32 chars and different. |
| 14, 15 | Test + R2 | Every operation with no, wrong, and mismatched token. Search every response for a token. | All fail. Token only in the `createUser` response. |
| 11, 12, 16 | Test + M1 | Save and reload config. Missing and garbage file. | Reloads. Bad file means "no identity", no crash. |
| 1, 2, 8, 13, 17, 18 | M1, M2 | Script. | All Expects seen. |
| 9 | R3 | Any code that changes a username? | No. |

##### Groups (GRP)

| IDs | Method | What we do | Passes when |
|---|---|---|---|
| 6–8 | Test | Group names `""`, `"   "`, `" Trip "`; two same-name groups. | Blank rejected. Both groups exist. |
| 9–12 | Test + R2 | 1,000 codes: 6 chars from A–Z (no I, O) and 2–9. Create and read back a group. Generator returns a used code, then a new one. | All valid. Creator is member 1 and owner. Second code used. |
| 14, 18, 20 | Test | Open as member / non-member with right, wrong, old, lowercase code. Check no other group's data appears. | Only a member with the current code (any case) gets in. |
| 1–5, 13, 15–17, 19, 21–25 | M2 | Script. | All Expects seen. |

##### Owner (OWN)

| IDs | Method | What we do | Passes when |
|---|---|---|---|
| 4–9, 12, 15, 16, 18 | Test | Rename, new code, cancel all as owner and as member. Check a member's group data for the code. Use the old code. | Member rejected with reason, nothing changes, no code visible. Old code fails. Members and bills unchanged. |
| 1, 19, 20 | Test + R4 | Owner leaves groups of 3 and 2. Force a database failure mid-leave. | Earliest remaining member becomes the only owner. After the failure nothing changed. |
| 3 | Test + R3 | Join with a wrong code. | Fails. `joinGroup` is the only way in. |
| 2, 10, 11, 13, 14, 17, 21 | M2, M3 | Script. | All Expects seen. |

##### Members (MEM)

| IDs | Method | What we do | Passes when |
|---|---|---|---|
| 5–15 | Test | Join in lowercase, twice, with an unknown code. Names `" Bob "`, `""`, case-duplicate. Same user in two groups with different names. | Each succeeds or fails as specified with the right message. Names trimmed. New member last, with join time. |
| 20–24, 26–29, 31 | Test + R3 | Member with unpaid bills leaves, then tries access. Last member leaves. Former member rejoins. | Bills unchanged by leaving. Former member rejected. Last leave deletes group and bills. Rejoin goes to the end. Former member's unpaid share still confirmable. No remove-member operation exists. |
| 1–4, 16–19, 25, 30 | M2, M7 | Script. | All Expects seen. |

##### Bills and splitting (BIL, SPL)

| IDs | Method | What we do | Passes when |
|---|---|---|---|
| BIL 10–14, 20, 34 | Test | Amounts `12.5`, `12.50`, `0.30`, `12.345`, `abc`, `0`, `-5`. Weights `0`, `1`, `0.005`, `1.01`, `-0.1`. Descriptions `"  hi "`, `"   "`. | Exact cents (`0.30` is 30). Bad input rejected. Description trimmed. |
| BIL 16–19 | Test + P2 | Weight sums 0.99, 1.00, 1.01. Split Equally for 1–20 members. | Only 1.00 accepted. Equal split sums to 1.00. |
| BIL 8, 30–33, 35 | Test | Bad amount, weight for someone who left, missing member, bad sum, past date, empty description. Then a good bill. | Bad bills rejected, nothing saved. Good bill saved with caller as owner and own share confirmed. |
| BIL 37 | Test | Pretend server returns a failure. | Form text kept, error shown. |
| BIL 1–7, 9, 15, 21–29, 36 | M4 | Script. | All Expects seen. |
| SPL 1–7 | Test | $10.00 three ways; $0.01 two ways; one person at 1.00; a zero weight. | $3.34/$3.33/$3.33; 1¢/0¢; full amount; $0. |
| SPL 4 | P1 + Test | 1,000 random bills. | Shares sum to the total; nobody gets >1¢ extra. |
| SPL 8, 9 | Test + M5 | Change membership after a bill. Zero-weight member in details. | Shares unchanged. Zero-share member not shown. |

##### Viewing, dates, payment, cancelling (BLD, DAT, PAY, CAN)

| IDs | Method | What we do | Passes when |
|---|---|---|---|
| BLD 8–10 | Test | Format 1250¢, 5¢, -1250¢, weight 25, a date, no date. | `$12.50`, `$0.05`, `-$12.50`, `0.25`, `2026-11-20`, `No date`. |
| BLD 1–7, 11–14; DAT 1, 2, 5–7, 9, 11 | M5 | Script. | All Expects seen. |
| DAT 3, 4, 8, 10 | Test | `2026-02-30`, `26-1-1`, yesterday/today/tomorrow (fake clock). Non-owner changes date. Clock 30 days past the date. | Only valid, non-past dates. Non-owner rejected. Nothing changes when the date passes. |
| PAY 1, 4–8, 12 | Test | Bill owner, other member, group owner (owner present / left) confirm. Confirm twice. Check other shares. | Only bill owner (or group owner if owner left) succeeds. Repeat fails. Other shares untouched. No un-confirm operation. |
| PAY 2, 3, 9, 10; CAN 1–6, 9, 11, 13–17, 22, 23 | M6 | Script. | All Expects seen. |
| PAY 11 | R3 | Any code that moves money? | No. |
| CAN 7, 8, 10, 12, 18–21, 24 | Test | Cancel one / all as bill owner, group owner, other member. Cancel a missing bill. Cancel a bill whose owner left. | Only allowed roles succeed. Only targeted bills deleted. Group unchanged. Missing bill gives "bill not found". |

##### Outstanding amounts (OUT)

| IDs | Method | What we do | Passes when |
|---|---|---|---|
| 3–6 | Test + R3 | Hand-built bills: owner left, former member's share, own share, all paid. Check total owes = total owed. | Matches hand calculation. Totals equal. Amounts are calculated, not stored. |
| 1, 2, 7, 8, 9 | M6, M7 | Script. | All Expects seen. |

##### Sync, saving, general (SYN, PER, GEN)

| IDs | Method | What we do | Passes when |
|---|---|---|---|
| SYN 1–11, 14, 17; GEN 4 | M8 | Script. | All Expects seen. |
| SYN 12 | Test | 50 users confirm one share at once; 20 join with one name at once. | Exactly one succeeds each time. |
| SYN 13, 15, 16 | Test | Pretend server says "not a member", "group not found", "already confirmed". | Reason shown and refresh. First two return to Home with a message. |
| PER 1–8, 13 | Test | Create data, restart server on same database, start with no database, check storage types, delete a group. | Identical after restart. New empty database created. Cents and hundredths stored as integers. No rows left for a deleted group. |
| PER 3, 9 | Test | Force a failure mid-change. Make database unreadable. | Nothing partly saved. Error returned, never "success". |
| PER 10–12; GEN 1 | R6, R3 | See checklists. | See checklists. |
| GEN 9 | R1 | See checklist. | See checklist. |
| GEN 10 | Test | 20 members, 1,000 bills; time each server operation. | Under 1 second. |
| GEN 12, 13 | Test + R5 | Garbage, empty, huge requests; garbage and cut-off responses to the client. | No crash; failed result and message instead. |
| GEN 2, 3, 5–8, 11, 14 | M1, M9 | Script. | All Expects seen. |

#### 3. Manual scripts

Two browsers: **A** (`Alice_1`, group name "Al") and **B** (`Bob_1`, group name "Bo"). Fresh database.

##### M1: First launch (USR 1, 2, 8, 12, 13, 16–18; GEN 4, 5)

| # | Do | Expect |
|---|---|---|
| 1 | Open app in A, no saved data | Create Username screen: username box, Create button, no password or email |
| 2 | Type `ab`, then `Alice_1`, then stop the server | Disabled, enabled, disabled |
| 3 | Restart server, press Create | Home shows `Alice_1`. Reload goes straight to Home. |
| 4 | Damage the config file, reload | Create screen with a notice that the old name can't be recovered |

##### M2: Groups, joining, codes (GRP 1–5, 13, 15–17, 19, 21–25; OWN 3, 13, 14, 17; MEM 1–4, 16, 17)

| # | Do | Expect |
|---|---|---|
| 1 | A: look at Home | "No groups yet" and a Create Group button |
| 2 | A: create group "Trip" as "Al" | Group screen. Members: "Al" with "Owner" and "you". |
| 3 | A: show code. B: Join Group | Submit disabled until code and name are filled |
| 4 | B: enter code in lowercase, name "Bo" | Form closes, group screen opens, Home lists "Trip" |
| 5 | A: wait up to 10 s | "Bo" appears (different from `Bob_1`) |
| 6 | B: Back to Home, reopen "Trip" | Opens with no code prompt |
| 7 | A: Generate New Code | A shows the new code, no prompt |
| 8 | B: wait up to 10 s, reopen with old code, then new code | Returned Home with a message. Old code errors. New code opens. |
| 9 | B: join again with the new code | "Already a member" message |

##### M3: Owner controls (OWN 2, 10, 11, 13, 14, 21)

| # | Do | Expect |
|---|---|---|
| 1 | Compare A's and B's group screens | Only A has Rename, Show Code, Generate New Code, Cancel All Bills |
| 2 | A: rename to "Trip 2" | Changes in A at once, in B within 10 s |
| 3 | A: Leave Group and confirm | A returns Home. B shows "Owner" on Bo and owner controls within 10 s. |

*Then have A rejoin with the code.*

##### M4: Create Bill form (BIL 1–7, 9, 15, 21–29, 36)

| # | Do | Expect |
|---|---|---|
| 1 | Open form in A | Description, amount, one weight per member (all 0), date box, no owner choice. Create disabled. |
| 2 | Description only; amount `12.345`; amount `10.00` with weight `1.5` | Create stays disabled; weight shows an error mark |
| 3 | Weights `0.50` and `0.40` | Sum `0.90` marked as error; disabled |
| 4 | Split Equally | `0.50` / `0.50`, sum `1.00`, enabled |
| 5 | Date `2026-02-30`, then clear it | Disabled, then enabled |
| 6 | Stop server, restart | Disabled while down |
| 7 | Press Create Bill | Form clears; bill appears at top at once |

##### M5: Viewing bills and dates (BLD 1–7, 11–14; DAT 1, 2, 5–7, 9, 11; SPL 8, 9)

| # | Do | Expect |
|---|---|---|
| 1 | A: create "Pizza", $10.00, 0.50/0.50, future date | Row: "Pizza", `$10.00`, "Al", date, "0 of 1 paid" |
| 2 | Select the row | Al `0.50` `$5.00` Paid; Bo `0.50` `$5.00` Unpaid; Open |
| 3 | Look in B | Same details. Change Date disabled or hidden. |
| 4 | A: change date, then clear it | New date, then "No date". Shares and statuses unchanged. |
| 5 | A: create a bill with Al 1.00, Bo 0 | Bo not listed. Newest bill on top. |
| 6 | Wait for a refresh with a bill selected; add many bills | Selection stays; list scrolls |

##### M6: Payments and cancelling (PAY 2, 3, 9, 10; CAN 1–6, 9, 11, 13–17, 22, 23; OUT 1, 2, 7, 8)

| # | Do | Expect |
|---|---|---|
| 1 | Look at Members | Bo owes `$5.00`; Al is owed `$5.00` |
| 2 | B: select "Pizza" | Confirm Payment and Cancel Bill disabled |
| 3 | A: select "Pizza", stop server | Confirm Payment shown, then disabled while down |
| 4 | Restart, A: Confirm Payment | Share Paid, "1 of 1 paid", Settled, both amounts `$0.00`, immediately |
| 5 | A: Cancel Bill | Dialog shows description and amount and says permanent. Cancel changes nothing. |
| 6 | Confirm | Bill gone, selection cleared, B updates within 10 s |
| 7 | B: look for Cancel All Bills | Hidden or disabled |
| 8 | A: Cancel All Bills | Dialog says every bill will be removed. Confirm leaves "No bills yet" and `$0.00` in both browsers. |

##### M7: Leaving (MEM 17–19, 22, 25, 30; OUT 8, 9)

| # | Do | Expect |
|---|---|---|
| 1 | B: create "Snacks" (0.50/0.50), then Leave Group | Confirm dialog first, nothing sent until confirmed |
| 2 | Confirm | B is Home; "Trip" gone from B's list |
| 3 | A: wait up to 10 s | "Bo" gone from Members; "Snacks" owner shows "Bo (left)"; no weight box for Bo |

##### M8: Sync and connection loss (SYN 1–11, 14, 17; GEN 4)

| # | Do | Expect |
|---|---|---|
| 1 | B rejoins; A types a description | New weight row for Bo in A within 10 s; typed text stays |
| 2 | B: create a bill | Appears in A within 10 s without reload |
| 3 | Leave both alone | No flicker when nothing changed |
| 4 | Stop the server | Within ~5 s a banner appears. Old data stays. All data-changing controls disabled. |
| 5 | Try a change while down | Nothing is queued |
| 6 | Restart | Within ~5 s the banner goes and controls return |

##### M9: Layout (GEN 2, 3, 6–8, 11, 14)

| # | Do | Expect |
|---|---|---|
| 1 | Open in desktop Chrome, Firefox, a narrow window, a real phone | Works in all. No install. |
| 2 | Go through screens | Only Create Username, Home, Group. Group has header, Members, Bills + details, Create Bill form. |
| 3 | Check buttons; cause an error | Every button has text. Error message can be dismissed. |

#### 4. Review checklists

The reviewer opens the files, *searches* (find-in-project or `grep`) and looks at every hit, or *traces* each server operation from controller to repository. They answer in the PR with file and line.

| # | Requirements | Files | What the reviewer does | Passes when |
|---|---|---|---|---|
| **R1** | GEN-9, PER-4, PER-5, BIL-13 | Whole repo, database setup | Search `float`, `double`, `Double`, `parseDouble`, `Math.round`. Check each hit. Check money and weight columns are integer types. | No hit touches money or weights. Columns are integers. |
| **R2** | USR-10, 14, 15, GRP-11, 18, 20, OWN-8, 9 | 3 API controllers, `AuthGuard`, token and code generators | Trace each operation: `AuthGuard` is called first (except `createUser`). Search every token read. Check the code is sent only to the owner. Check generators use a secure random source. | Authenticates first everywhere. Token only in `createUser`. Code only to owner. Random source used. |
| **R3** | USR-9, 17, MEM-31, OWN-3, PAY-11, 12, OUT-6, PER-11, 12 | Controllers, `GroupRepository` | List every operation and database update. None renames a user, removes another member, un-confirms, or moves money. Members only added in `joinGroup` and `createGroup`. Outstanding amounts not stored. All PER-11 operations exist and return a reason on failure. | No forbidden operation. All PER-11 operations exist. |
| **R4** | OWN-20, PER-3, 13, SYN-12, MEM-28 | Group/Bill controllers, `GroupRepository`, `GroupLockManager` | Trace every change: lock, reload, re-check, one repository call, unlock in `finally`. Multi-step writes are one transaction. | Every change follows this order. |
| **R5** | GEN-12, 13, PER-9 | Controllers, `ServerConnection` | Check every controller turns `StorageException` and bad input into a failed `Result`. Check the client handles timeouts and malformed responses. | No exception can escape. |
| **R6** | PER-10, GEN-1 | `SessionModel`, project folders | Read what is saved locally. Check the languages used. | Only username, token, address, group codes saved. Java server; JS/HTML/CSS client. |

#### 5. Proofs

**P1: shares add up to the total (SPL-4).** *Property:* for any total `T` in cents and weights summing to 1.00, shares sum to exactly `T` and nobody gets more than one extra cent. *Why:* rounding each exact share down loses under 1¢ per person. The exact shares sum to `T`, so the lost cents sum to a whole number smaller than the number of people. One leftover cent each to the first few people therefore never runs out of people, and the total becomes `T`.

**P2: Split Equally sums to 1.00 (BIL-19).** *Property:* for any `n` members, the weights sum to 100 hundredths. *Why:* everyone gets `100 ÷ n` rounded down; the leftover `100 mod n` is less than `n`, so one extra hundredth to that many members uses it exactly.

The 1,000-random-case test checks that our code matches these arguments.

#### 6. Process

| When | What | Who |
|---|---|---|
| Every build | Tests run with the build (`mvn test` / `gradle test`). Failing test = failed build. | Everyone locally |
| Before every commit | Run tests for the code you changed. Red means don't commit. | The committer |
| Before every merge | Full test suite passes. A non-owner reviews and fills the checklists that apply. | Author + one reviewer |
| Wed Nov 18 | Review which tests exist or fail. Do R1, R3 on existing code. | Whole team; Garvit leads |
| Fri Nov 20 | Server tests. Do R2, R4, R5, R6 (e.g. Rory and Garvit review Nik's `AuthGuard` and `GroupLockManager`). | Reviewers ≠ owners |
| Sun Nov 22 | All tests; run M1–M3 as a smoke check. | Non-authors |
| Tue Nov 24 | Run M1–M9. Failures become GitHub Issues with the requirement ID. | Non-authors; Garvit assigns |
| Wed Nov 25 | Triage open Issues. | Whole team |
| Thu Nov 26 | Rerun all tests and failed scripts. | Whole team |
| Always | `docs/acceptance-matrix.md` lists each requirement, method, and result. | Garvit |

If time runs short, money, splitting, and permissions (SPL, BIL, PAY, CAN, OWN, USR-14/15, GRP-14/18/20) are finished first and M9 is shortened last.

#### 7. What we skip and why

| Skipped | Why |
|---|---|
| Speed on slow or busy real networks (GEN-10) | The requirement only covers normal conditions. We time the server locally. |
| Every browser and device (GEN-2) | We test Chrome, Firefox, a narrow window, and one phone. That's what we have time and equipment for. |
| Timing with many users (SYN-4) | Two browsers are enough for a small group app. |
| Proving true randomness (USR-10, GRP-11) | A test can't prove it. We test length and characters and review the secure random source. |
| Pixel-level look | The requirements don't specify fonts or spacing. We check text and behavior only. |

#### 8. Why these choices

- **Tests for rules:** validation, splitting, permissions, and saving have clear right answers, and mistakes would break trust (wrong money, wrong permissions). Five people editing the same code for two weeks need checks that rerun in seconds.
- **Temporary database, fake clock:** persistence and date rules (DAT-4, DAT-10) only mean something against a real database and a clock we control.
- **Pretend server:** lets us test "server says no" and "server unreachable" repeatably.
- **Scripts:** whether a button is disabled or another browser updates in 10 seconds needs a person watching. Written steps make everyone check the same thing.
- **Reviews:** a test covers only cases we thought of. "The token is never sent anywhere else" concerns every code path, which a reader tracing each operation can check.
- **Proofs:** our app handles money, and a few examples can't show the shares add up for every total and weight set.
- **Different person checks each piece:** authors test what they already know works.
- **Tests on every build and commit, review before merge:** a problem caught seconds after it's written is far cheaper than one found on Nov 24, and review stops one person's mistake from breaking everyone's work.
- **Dates:** server reviews wait for Nov 20 (server complete); full scripts wait for Nov 24 (client connected Nov 22), leaving Nov 24–26 for fixes.
