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
## 3. Who owns each component?

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
