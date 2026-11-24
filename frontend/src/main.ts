// Shared Expense Splitter Client Web App — TypeScript Implementation
import './style.css';
import type { AppState } from './types';
import {
  configureApi,
  apiCreateUser,
  apiListGroups,
  apiCreateGroup,
  apiJoinGroup,
  apiGetGroup,
  apiRenameGroup,
  apiGenerateGroupCode,
  apiLeaveGroup,
  apiCreateBill,
  apiChangeDate,
  apiConfirmPayment,
  apiCancelBill,
  apiCancelAllBills,
} from './api';
import {
  formatCents,
  formatWeight,
  parseAmountToCents,
  parseWeightHundredths,
  isValidUsername,
  escapeHtml,
  computeBalances,
} from './format';

// ── Application State ──────────────────────────────────────────────────────────
const state: AppState = {
  username: localStorage.getItem('splitter_username') || null,
  loginToken: localStorage.getItem('splitter_token') || null,
  serverAddress: localStorage.getItem('splitter_server') || window.location.origin,
  savedCodes: JSON.parse(localStorage.getItem('splitter_codes') || '{}'),
  connected: true,
  groups: [],
  currentGroup: null,
  selectedBillId: null,
  currentScreen: 'username',
};

let pollTimer: ReturnType<typeof setInterval> | null = null;
let pendingConfirmAction: (() => void) | null = null;
let promptingGroupId: number | null = null;

// Configure API client with credentials & status callbacks
configureApi(
  () => {
    if (state.username && state.loginToken) {
      return { username: state.username, loginToken: state.loginToken };
    }
    return null;
  },
  () => state.serverAddress,
  () => markConnected(),
  (msg) => markDisconnected(msg),
);

// ── DOM Elements ─────────────────────────────────────────────────────────────
const banner = document.getElementById('connection-banner') as HTMLDivElement;
const bannerText = document.getElementById('connection-banner-text') as HTMLSpanElement;
const errorDialog = document.getElementById('error-dialog') as HTMLDivElement;
const errorText = document.getElementById('error-message-text') as HTMLSpanElement;
const errorDismiss = document.getElementById('error-dismiss-btn') as HTMLButtonElement;
const confirmModal = document.getElementById('confirm-modal') as HTMLDivElement;
const confirmTitle = document.getElementById('confirm-modal-title') as HTMLHeadingElement;
const confirmMsg = document.getElementById('confirm-modal-message') as HTMLParagraphElement;
const confirmOk = document.getElementById('confirm-ok-btn') as HTMLButtonElement;
const confirmCancel = document.getElementById('confirm-cancel-btn') as HTMLButtonElement;

// Screens
const screenUsername = document.getElementById('screen-username') as HTMLElement;
const usernameInput = document.getElementById('username-input') as HTMLInputElement;
const serverAddrInput = document.getElementById('server-address-input') as HTMLInputElement;
const createUsernameBtn = document.getElementById('create-username-btn') as HTMLButtonElement;

const screenHome = document.getElementById('screen-home') as HTMLElement;
const currentUsernameDisplay = document.getElementById('current-username-display') as HTMLElement;
const groupsList = document.getElementById('groups-list') as HTMLDivElement;
const openCreateGroupBtn = document.getElementById('open-create-group-btn') as HTMLButtonElement;
const openJoinGroupBtn = document.getElementById('open-join-group-btn') as HTMLButtonElement;

const createGroupModal = document.getElementById('create-group-modal') as HTMLDivElement;
const newGroupName = document.getElementById('new-group-name') as HTMLInputElement;
const creatorMemberName = document.getElementById('creator-member-name') as HTMLInputElement;
const createGroupSubmitBtn = document.getElementById('create-group-submit-btn') as HTMLButtonElement;
const createGroupCancelBtn = document.getElementById('create-group-cancel-btn') as HTMLButtonElement;

const joinGroupModal = document.getElementById('join-group-modal') as HTMLDivElement;
const joinGroupCode = document.getElementById('join-group-code') as HTMLInputElement;
const joinMemberName = document.getElementById('join-member-name') as HTMLInputElement;
const joinGroupSubmitBtn = document.getElementById('join-group-submit-btn') as HTMLButtonElement;
const joinGroupCancelBtn = document.getElementById('join-group-cancel-btn') as HTMLButtonElement;

const promptCodeModal = document.getElementById('prompt-code-modal') as HTMLDivElement;
const promptGroupCode = document.getElementById('prompt-group-code') as HTMLInputElement;
const promptCodeSubmitBtn = document.getElementById('prompt-code-submit-btn') as HTMLButtonElement;
const promptCodeCancelBtn = document.getElementById('prompt-code-cancel-btn') as HTMLButtonElement;

const screenGroup = document.getElementById('screen-group') as HTMLElement;
const groupTitle = document.getElementById('group-title') as HTMLHeadingElement;
const groupOwnerBadge = document.getElementById('group-owner-badge') as HTMLSpanElement;
const groupBackBtn = document.getElementById('group-back-btn') as HTMLButtonElement;
const groupLeaveBtn = document.getElementById('group-leave-btn') as HTMLButtonElement;
const ownerControls = document.getElementById('owner-controls') as HTMLSpanElement;
const groupRenameBtn = document.getElementById('group-rename-btn') as HTMLButtonElement;
const groupShowCodeBtn = document.getElementById('group-show-code-btn') as HTMLButtonElement;
const groupGenerateCodeBtn = document.getElementById('group-generate-code-btn') as HTMLButtonElement;
const groupCancelAllBillsBtn = document.getElementById('group-cancel-all-bills-btn') as HTMLButtonElement;
const groupCodeDisplay = document.getElementById('group-code-display') as HTMLDivElement;
const groupCodeText = document.getElementById('group-code-text') as HTMLElement;

const membersList = document.getElementById('members-list') as HTMLDivElement;
const billsScrollList = document.getElementById('bills-scroll-list') as HTMLDivElement;
const billDetailsContainer = document.getElementById('bill-details-container') as HTMLDivElement;
const detailBillDesc = document.getElementById('detail-bill-desc') as HTMLHeadingElement;
const detailBillStatus = document.getElementById('detail-bill-status') as HTMLSpanElement;
const detailBillTotal = document.getElementById('detail-bill-total') as HTMLElement;
const detailDateInput = document.getElementById('detail-date-input') as HTMLInputElement;
const detailChangeDateBtn = document.getElementById('detail-change-date-btn') as HTMLButtonElement;
const detailSharesTbody = document.getElementById('detail-shares-tbody') as HTMLTableSectionElement;
const detailCancelBillBtn = document.getElementById('detail-cancel-bill-btn') as HTMLButtonElement;

const billDescInput = document.getElementById('bill-description-input') as HTMLInputElement;
const billAmountInput = document.getElementById('bill-amount-input') as HTMLInputElement;
const billDateInput = document.getElementById('bill-date-input') as HTMLInputElement;
const splitEquallyBtn = document.getElementById('split-equally-btn') as HTMLButtonElement;
const weightRowsContainer = document.getElementById('weight-rows-container') as HTMLDivElement;
const weightSumDisplay = document.getElementById('weight-sum-display') as HTMLSpanElement;
const submitCreateBillBtn = document.getElementById('submit-create-bill-btn') as HTMLButtonElement;

// ── Notifications and Status ─────────────────────────────────────────────────
function markConnected() {
  if (!state.connected) {
    state.connected = true;
    banner.classList.add('hidden');
    updateControlStates();
  }
}

function markDisconnected(msg: string) {
  if (state.connected) {
    state.connected = false;
    bannerText.textContent = `Server unreachable: ${msg || 'Connection lost'}`;
    banner.classList.remove('hidden');
    updateControlStates();
  }
}

function showError(msg?: string) {
  if (!msg) return;
  errorText.textContent = msg;
  errorDialog.classList.remove('hidden');
}

errorDismiss.addEventListener('click', () => {
  errorDialog.classList.add('hidden');
});

function confirmAction(title: string, message: string, onConfirm: () => void) {
  confirmTitle.textContent = title;
  confirmMsg.textContent = message;
  pendingConfirmAction = onConfirm;
  confirmModal.classList.remove('hidden');
}

confirmOk.addEventListener('click', () => {
  confirmModal.classList.add('hidden');
  if (pendingConfirmAction) {
    const act = pendingConfirmAction;
    pendingConfirmAction = null;
    act();
  }
});

confirmCancel.addEventListener('click', () => {
  confirmModal.classList.add('hidden');
  pendingConfirmAction = null;
});

// ── Screen Router ────────────────────────────────────────────────────────────
function showScreen(screen: 'username' | 'home' | 'group') {
  state.currentScreen = screen;
  screenUsername.classList.add('hidden');
  screenHome.classList.add('hidden');
  screenGroup.classList.add('hidden');

  if (pollTimer) {
    clearInterval(pollTimer);
    pollTimer = null;
  }

  if (screen === 'username') {
    screenUsername.classList.remove('hidden');
  } else if (screen === 'home') {
    screenHome.classList.remove('hidden');
    currentUsernameDisplay.textContent = state.username || '';
    void loadHomeGroups();
    pollTimer = setInterval(() => void loadHomeGroups(), 5000);
  } else if (screen === 'group') {
    screenGroup.classList.remove('hidden');
    renderGroupScreen();
    pollTimer = setInterval(() => void pollGroupSnapshot(), 5000);
  }
  updateControlStates();
}

function updateControlStates() {
  const disabled = !state.connected;
  createUsernameBtn.disabled = disabled || !isValidUsername(usernameInput.value);
  submitCreateBillBtn.disabled = disabled || !isCreateBillValid();
  const joinCode = joinGroupCode.value.trim();
  const joinName = joinMemberName.value.trim();
  joinGroupSubmitBtn.disabled = disabled || !joinCode || !joinName;
}

// ── Screen 1: Username / Auth ─────────────────────────────────────────────────
usernameInput.addEventListener('input', () => {
  createUsernameBtn.disabled = !state.connected || !isValidUsername(usernameInput.value);
});

createUsernameBtn.addEventListener('click', async () => {
  const username = usernameInput.value.trim();
  const serverAddr = serverAddrInput.value.trim();
  if (serverAddr) {
    state.serverAddress = serverAddr;
    localStorage.setItem('splitter_server', serverAddr);
  }

  const res = await apiCreateUser(username);
  if (res.success && res.data) {
    state.username = res.data.username;
    state.loginToken = res.data.loginToken;
    localStorage.setItem('splitter_username', state.username);
    localStorage.setItem('splitter_token', state.loginToken);
    showScreen('home');
  } else {
    showError(res.failureReason);
  }
});

// ── Screen 2: Home / Group List ──────────────────────────────────────────────
async function loadHomeGroups(): Promise<void> {
  const res = await apiListGroups();
  if (res.success && Array.isArray(res.data)) {
    state.groups = res.data;
    renderGroupsList();
  }
}

function renderGroupsList(): void {
  groupsList.innerHTML = '';
  if (state.groups.length === 0) {
    groupsList.innerHTML = '<p class="empty-state">No groups yet</p>';
    return;
  }
  state.groups.forEach((g) => {
    const item = document.createElement('div');
    item.className = 'list-item';
    item.innerHTML = `<strong>${escapeHtml(g.name)}</strong><span>&rarr;</span>`;
    item.addEventListener('click', () => onSelectGroup(g.groupId));
    groupsList.appendChild(item);
  });
}

function onSelectGroup(groupId: number): void {
  const code = state.savedCodes[groupId];
  if (code) {
    void fetchAndOpenGroup(groupId, code);
  } else {
    promptingGroupId = groupId;
    promptGroupCode.value = '';
    promptCodeModal.classList.remove('hidden');
  }
}

async function fetchAndOpenGroup(groupId: number, code: string): Promise<void> {
  const res = await apiGetGroup(groupId, code);
  if (res.success && res.data) {
    state.currentGroup = res.data;
    state.savedCodes[groupId] = code;
    localStorage.setItem('splitter_codes', JSON.stringify(state.savedCodes));
    showScreen('group');
  } else {
    if (res.failureReason === 'code outdated') {
      delete state.savedCodes[groupId];
      localStorage.setItem('splitter_codes', JSON.stringify(state.savedCodes));
      promptingGroupId = groupId;
      promptGroupCode.value = '';
      promptCodeModal.classList.remove('hidden');
    } else {
      showError(res.failureReason);
    }
  }
}

openCreateGroupBtn.addEventListener('click', () => {
  newGroupName.value = '';
  creatorMemberName.value = '';
  createGroupModal.classList.remove('hidden');
});

createGroupCancelBtn.addEventListener('click', () => {
  createGroupModal.classList.add('hidden');
});

createGroupSubmitBtn.addEventListener('click', async () => {
  const groupName = newGroupName.value.trim();
  const memberName = creatorMemberName.value.trim();
  if (!groupName || !memberName) {
    showError('Please fill in both group name and display name');
    return;
  }

  const res = await apiCreateGroup(groupName, memberName);
  if (res.success && res.data) {
    createGroupModal.classList.add('hidden');
    state.currentGroup = res.data;
    if (res.data.groupCode) {
      state.savedCodes[res.data.groupId] = res.data.groupCode;
      localStorage.setItem('splitter_codes', JSON.stringify(state.savedCodes));
    }
    showScreen('group');
  } else {
    showError(res.failureReason);
  }
});

openJoinGroupBtn.addEventListener('click', () => {
  joinGroupCode.value = '';
  joinMemberName.value = '';
  joinGroupModal.classList.remove('hidden');
});

joinGroupCancelBtn.addEventListener('click', () => {
  joinGroupModal.classList.add('hidden');
});

function updateJoinBtn() {
  const code = joinGroupCode.value.trim();
  const name = joinMemberName.value.trim();
  joinGroupSubmitBtn.disabled = !state.connected || !code || !name;
}
joinGroupCode.addEventListener('input', updateJoinBtn);
joinMemberName.addEventListener('input', updateJoinBtn);

joinGroupSubmitBtn.addEventListener('click', async () => {
  const code = joinGroupCode.value.trim().toUpperCase();
  const memberName = joinMemberName.value.trim();
  const res = await apiJoinGroup(code, memberName);
  if (res.success && res.data) {
    joinGroupModal.classList.add('hidden');
    state.currentGroup = res.data;
    state.savedCodes[res.data.groupId] = code;
    localStorage.setItem('splitter_codes', JSON.stringify(state.savedCodes));
    showScreen('group');
  } else {
    showError(res.failureReason);
  }
});

promptCodeCancelBtn.addEventListener('click', () => {
  promptCodeModal.classList.add('hidden');
  promptingGroupId = null;
});

promptCodeSubmitBtn.addEventListener('click', () => {
  const code = promptGroupCode.value.trim().toUpperCase();
  if (promptingGroupId && code) {
    promptCodeModal.classList.add('hidden');
    void fetchAndOpenGroup(promptingGroupId, code);
    promptingGroupId = null;
  }
});

// ── Screen 3: Group View ─────────────────────────────────────────────────────
async function pollGroupSnapshot(): Promise<void> {
  if (!state.currentGroup) return;
  const gid = state.currentGroup.groupId;
  const code = state.savedCodes[gid];
  const res = await apiGetGroup(gid, code);
  if (res.success && res.data) {
    state.currentGroup = res.data;
    renderGroupScreen();
  } else {
    if (['group not found', 'not a member', 'code outdated'].includes(res.failureReason || '')) {
      showScreen('home');
      showError(res.failureReason === 'code outdated' ? 'The group code changed.' : res.failureReason);
    }
  }
}

function renderGroupScreen(): void {
  const g = state.currentGroup;
  if (!g) return;

  groupTitle.textContent = g.groupName;
  if (g.isOwner) {
    groupOwnerBadge.classList.remove('hidden');
    ownerControls.classList.remove('hidden');
    groupCodeText.textContent = g.groupCode || '';
  } else {
    groupOwnerBadge.classList.add('hidden');
    ownerControls.classList.add('hidden');
    groupCodeDisplay.classList.add('hidden');
  }

  renderMembers();
  renderBills();
  renderBillDetails();
  renderCreateBillWeights();
}

function renderMembers(): void {
  const g = state.currentGroup;
  if (!g) return;
  membersList.innerHTML = '';
  const currentMembers = g.members.filter((m) => !m.hasLeft);

  const balances = computeBalances(currentMembers, g.bills);

  currentMembers.forEach((m) => {
    const isYou = m.name.toLowerCase() === g.currentMemberName.toLowerCase();
    const bal = balances[m.name] || { owes: 0, isOwed: 0 };
    const row = document.createElement('div');
    row.className = 'member-row';
    row.innerHTML = `
      <div class="member-info">
        <strong>${escapeHtml(m.name)} ${m.isOwner ? '<span class="badge badge-owner">Owner</span>' : ''} ${isYou ? '<span class="badge">you</span>' : ''}</strong>
      </div>
      <div class="member-balances">
        <span>Owes: ${formatCents(bal.owes)}</span> &bull;
        <span>Is owed: ${formatCents(bal.isOwed)}</span>
      </div>
    `;
    membersList.appendChild(row);
  });
}

function renderBills(): void {
  const g = state.currentGroup;
  if (!g) return;
  billsScrollList.innerHTML = '';
  if (!g.bills || g.bills.length === 0) {
    billsScrollList.innerHTML = '<p class="empty-state">No bills yet</p>';
    return;
  }

  g.bills.forEach((b) => {
    const row = document.createElement('div');
    row.className = `bill-row ${state.selectedBillId === b.id ? 'selected' : ''}`;
    const ownerMember = g.members.find((m) => m.name.toLowerCase() === b.billOwnerMemberName.toLowerCase());
    const ownerDisplay = ownerMember && ownerMember.hasLeft ? `${b.billOwnerMemberName} (left)` : b.billOwnerMemberName;
    const dateDisplay = b.suggestedDate ? b.suggestedDate : 'No date';
    const paidShares = (b.shares || []).filter((s) => s.shareCents > 0 && s.isConfirmed).length;
    const payableShares = (b.shares || []).filter((s) => s.shareCents > 0).length;

    row.innerHTML = `
      <div class="member-info">
        <strong>${escapeHtml(b.description)}</strong>
        <strong>${formatCents(b.totalCents)}</strong>
      </div>
      <div class="bill-meta">
        <span>By ${escapeHtml(ownerDisplay)}</span>
        <span>${dateDisplay}</span>
        <span>${paidShares} of ${payableShares} paid</span>
      </div>
    `;
    row.addEventListener('click', () => {
      state.selectedBillId = b.id;
      renderBills();
      renderBillDetails();
    });
    billsScrollList.appendChild(row);
  });
}

function renderBillDetails(): void {
  const g = state.currentGroup;
  if (!state.selectedBillId || !g) {
    billDetailsContainer.classList.add('hidden');
    return;
  }

  const bill = (g.bills || []).find((b) => b.id === state.selectedBillId);
  if (!bill) {
    state.selectedBillId = null;
    billDetailsContainer.classList.add('hidden');
    return;
  }

  billDetailsContainer.classList.remove('hidden');
  detailBillDesc.textContent = bill.description;
  detailBillTotal.textContent = formatCents(bill.totalCents);
  const fullyPaid = (bill.shares || []).every((s) => s.shareCents === 0 || s.isConfirmed);
  detailBillStatus.textContent = fullyPaid ? 'Settled' : 'Open';
  detailDateInput.value = bill.suggestedDate || '';

  const isBillOwner = g.currentMemberName.toLowerCase() === bill.billOwnerMemberName.toLowerCase();
  const canCancel = isBillOwner || g.isOwner;
  detailCancelBillBtn.style.display = canCancel ? 'inline-block' : 'none';

  detailChangeDateBtn.disabled = !isBillOwner || !state.connected;
  detailDateInput.disabled = !isBillOwner || !state.connected;

  let canConfirm = isBillOwner;
  if (!canConfirm && g.isOwner) {
    const ownerMember = g.members.find((m) => m.name.toLowerCase() === bill.billOwnerMemberName.toLowerCase());
    if (ownerMember && ownerMember.hasLeft) canConfirm = true;
  }

  detailSharesTbody.innerHTML = '';
  (bill.shares || []).forEach((s) => {
    if (s.shareCents === 0 && s.weightHundredths === 0) return;
    const tr = document.createElement('tr');
    const isPaid = s.isConfirmed;
    const confirmBtnHtml =
      !isPaid && canConfirm && state.connected
        ? `<button class="btn btn-secondary btn-sm confirm-share-btn" data-member="${escapeHtml(s.memberName)}">Confirm Payment</button>`
        : '';
    tr.innerHTML = `
      <td>${escapeHtml(s.memberName)}</td>
      <td>${formatWeight(s.weightHundredths)}</td>
      <td>${formatCents(s.shareCents)}</td>
      <td><span class="badge">${isPaid ? 'Paid' : 'Unpaid'}</span></td>
      <td>${confirmBtnHtml}</td>
    `;
    detailSharesTbody.appendChild(tr);
  });

  document.querySelectorAll<HTMLButtonElement>('.confirm-share-btn').forEach((btn) => {
    btn.addEventListener('click', async (e) => {
      const target = e.currentTarget as HTMLElement;
      const memberName = target.getAttribute('data-member');
      if (!memberName) return;
      const res = await apiConfirmPayment(
        g.groupId,
        state.savedCodes[g.groupId],
        bill.id,
        memberName,
      );
      if (res.success) {
        void pollGroupSnapshot();
      } else {
        showError(res.failureReason);
      }
    });
  });
}

detailChangeDateBtn.addEventListener('click', async () => {
  if (!state.selectedBillId || !state.currentGroup) return;
  const newDate = detailDateInput.value;
  const res = await apiChangeDate(
    state.currentGroup.groupId,
    state.savedCodes[state.currentGroup.groupId],
    state.selectedBillId,
    newDate || null,
  );
  if (res.success) {
    void pollGroupSnapshot();
  } else {
    showError(res.failureReason);
  }
});

detailCancelBillBtn.addEventListener('click', () => {
  if (!state.selectedBillId || !state.currentGroup) return;
  confirmAction('Cancel Bill', 'Are you sure you want to cancel this bill? This action cannot be undone.', async () => {
    if (!state.currentGroup || !state.selectedBillId) return;
    const res = await apiCancelBill(
      state.currentGroup.groupId,
      state.savedCodes[state.currentGroup.groupId],
      state.selectedBillId,
    );
    if (res.success) {
      state.selectedBillId = null;
      void pollGroupSnapshot();
    } else {
      showError(res.failureReason);
    }
  });
});

groupBackBtn.addEventListener('click', () => {
  showScreen('home');
});

groupLeaveBtn.addEventListener('click', () => {
  confirmAction('Leave Group', 'Are you sure you want to leave this group?', async () => {
    if (!state.currentGroup) return;
    const gid = state.currentGroup.groupId;
    const res = await apiLeaveGroup(gid, state.savedCodes[gid]);
    if (res.success) {
      delete state.savedCodes[gid];
      localStorage.setItem('splitter_codes', JSON.stringify(state.savedCodes));
      showScreen('home');
    } else {
      showError(res.failureReason);
    }
  });
});

groupRenameBtn.addEventListener('click', () => {
  if (!state.currentGroup) return;
  const currentName = state.currentGroup.groupName;
  const newName = prompt('Enter new group name:', currentName);
  if (newName && newName.trim() && newName.trim() !== currentName) {
    void apiRenameGroup(
      state.currentGroup.groupId,
      state.savedCodes[state.currentGroup.groupId],
      newName.trim(),
    ).then((res) => {
      if (res.success) void pollGroupSnapshot();
      else showError(res.failureReason);
    });
  }
});

groupShowCodeBtn.addEventListener('click', () => {
  groupCodeDisplay.classList.toggle('hidden');
});

groupGenerateCodeBtn.addEventListener('click', async () => {
  if (!state.currentGroup) return;
  const gid = state.currentGroup.groupId;
  const res = await apiGenerateGroupCode(gid, state.savedCodes[gid]);
  if (res.success && res.data) {
    state.savedCodes[gid] = res.data;
    localStorage.setItem('splitter_codes', JSON.stringify(state.savedCodes));
    void pollGroupSnapshot();
  } else {
    showError(res.failureReason);
  }
});

groupCancelAllBillsBtn.addEventListener('click', () => {
  confirmAction(
    'Cancel All Bills',
    'Are you sure you want to cancel all bills in this group? Every bill will be permanently removed.',
    async () => {
      if (!state.currentGroup) return;
      const gid = state.currentGroup.groupId;
      const res = await apiCancelAllBills(gid, state.savedCodes[gid]);
      if (res.success) {
        void pollGroupSnapshot();
      } else {
        showError(res.failureReason);
      }
    },
  );
});

// ── Create Bill Form ─────────────────────────────────────────────────────────
function renderCreateBillWeights(): void {
  const g = state.currentGroup;
  if (!g) return;
  const currentMembers = g.members.filter((m) => !m.hasLeft);

  const existing: Record<string, string> = {};
  document.querySelectorAll<HTMLInputElement>('.weight-input').forEach((inp) => {
    const mem = inp.getAttribute('data-member');
    if (mem) existing[mem] = inp.value;
  });

  weightRowsContainer.innerHTML = '';
  currentMembers.forEach((m) => {
    const row = document.createElement('div');
    row.className = 'weight-row-item';
    const val = existing[m.name] !== undefined ? existing[m.name] : '0.00';
    row.innerHTML = `
      <span>${escapeHtml(m.name)}</span>
      <input type="text" class="weight-input" data-member="${escapeHtml(m.name)}" value="${val}" placeholder="0.00">
    `;
    weightRowsContainer.appendChild(row);
  });

  document.querySelectorAll<HTMLInputElement>('.weight-input').forEach((inp) => {
    inp.addEventListener('input', updateCreateBillState);
  });

  updateCreateBillState();
}

splitEquallyBtn.addEventListener('click', () => {
  const inputs = document.querySelectorAll<HTMLInputElement>('.weight-input');
  const n = inputs.length;
  if (n === 0) return;
  const base = Math.floor(100 / n);
  const remainder = 100 % n;
  inputs.forEach((inp, idx) => {
    const w = base + (idx < remainder ? 1 : 0);
    inp.value = formatWeight(w);
  });
  updateCreateBillState();
});

function isCreateBillValid(): boolean {
  const desc = billDescInput.value.trim();
  if (!desc) return false;
  const amount = parseAmountToCents(billAmountInput.value);
  if (!amount) return false;

  let sum = 0;
  const inputs = document.querySelectorAll<HTMLInputElement>('.weight-input');
  if (inputs.length === 0) return false;

  for (const inp of Array.from(inputs)) {
    const w = parseWeightHundredths(inp.value);
    if (w === null) return false;
    sum += w;
  }
  return sum === 100;
}

function updateCreateBillState(): void {
  let sum = 0;
  let hasErr = false;
  const inputs = document.querySelectorAll<HTMLInputElement>('.weight-input');

  inputs.forEach((inp) => {
    const w = parseWeightHundredths(inp.value);
    if (w === null) {
      inp.classList.add('input-error');
      hasErr = true;
    } else {
      inp.classList.remove('input-error');
      sum += w;
    }
  });

  if (hasErr) {
    weightSumDisplay.textContent = 'Error';
    weightSumDisplay.parentElement?.classList.add('error');
  } else {
    weightSumDisplay.textContent = formatWeight(sum);
    if (sum === 100) {
      weightSumDisplay.parentElement?.classList.remove('error');
    } else {
      weightSumDisplay.parentElement?.classList.add('error');
    }
  }

  submitCreateBillBtn.disabled = !state.connected || !isCreateBillValid();
}

billDescInput.addEventListener('input', updateCreateBillState);
billAmountInput.addEventListener('input', updateCreateBillState);
billDateInput.addEventListener('input', updateCreateBillState);

submitCreateBillBtn.addEventListener('click', async () => {
  if (!isCreateBillValid() || !state.currentGroup) return;

  const desc = billDescInput.value.trim();
  const totalCents = parseAmountToCents(billAmountInput.value);
  if (!totalCents) return;
  const suggestedDate = billDateInput.value || null;

  const weights: Record<string, number> = {};
  document.querySelectorAll<HTMLInputElement>('.weight-input').forEach((inp) => {
    const mem = inp.getAttribute('data-member');
    const w = parseWeightHundredths(inp.value);
    if (mem && w !== null) {
      // In the server model: weightsHundredths sums to 10000 (100.00%)
      // From our UI inputs summing to 100 (1.00), multiply by 100
      weights[mem] = w * 100;
    }
  });

  const res = await apiCreateBill(
    state.currentGroup.groupId,
    state.savedCodes[state.currentGroup.groupId],
    {
      description: desc,
      totalCents,
      suggestedDate,
      weightsHundredths: weights,
    },
  );

  if (res.success) {
    billDescInput.value = '';
    billAmountInput.value = '';
    billDateInput.value = '';
    renderCreateBillWeights();
    void pollGroupSnapshot();
  } else {
    showError(res.failureReason);
  }
});

// ── Application Boot ──────────────────────────────────────────────────────────
if (state.username && state.loginToken) {
  showScreen('home');
} else {
  showScreen('username');
}
