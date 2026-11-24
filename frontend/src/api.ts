import type {
  Result,
  Credentials,
  GroupSummary,
  GroupSnapshot,
  NewBillPayload,
} from './types';

// State references needed by API layer
let getCredentialsFn: () => Credentials | null = () => null;
let getServerAddressFn: () => string = () => window.location.origin;
let onConnectedFn: () => void = () => {};
let onDisconnectedFn: (msg: string) => void = () => {};

export function configureApi(
  getCreds: () => Credentials | null,
  getServerAddr: () => string,
  onConnected: () => void,
  onDisconnected: (msg: string) => void,
) {
  getCredentialsFn = getCreds;
  getServerAddressFn = getServerAddr;
  onConnectedFn = onConnected;
  onDisconnectedFn = onDisconnected;
}

export async function apiCall<T>(
  endpoint: string,
  payload: Record<string, unknown> = {},
): Promise<Result<T>> {
  const serverAddr = getServerAddressFn() || window.location.origin;
  const url = `${serverAddr.replace(/\/+$/, '')}${endpoint}`;

  const fullPayload: Record<string, unknown> = { ...payload };
  const creds = getCredentialsFn();
  if (creds) {
    fullPayload.credentials = {
      username: creds.username,
      loginToken: creds.loginToken,
    };
  }

  try {
    const resp = await fetch(url, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json; charset=UTF-8' },
      body: JSON.stringify(fullPayload),
    });

    if (!resp.ok) {
      throw new Error(`Server returned HTTP ${resp.status}`);
    }

    const data: Result<T> = await resp.json();
    onConnectedFn();
    return data;
  } catch (e: unknown) {
    const msg = e instanceof Error ? e.message : 'Connection failed';
    onDisconnectedFn(msg);
    return { success: false, failureReason: 'server unreachable' };
  }
}

// Typed API functions
export function apiCreateUser(username: string) {
  return apiCall<Credentials>('/api/user/create', { username });
}

export function apiListGroups() {
  return apiCall<GroupSummary[]>('/api/group/list', {});
}

export function apiCreateGroup(groupName: string, memberName: string) {
  return apiCall<GroupSnapshot>('/api/group/create', { groupName, memberName });
}

export function apiJoinGroup(groupCode: string, memberName: string) {
  return apiCall<GroupSnapshot>('/api/group/join', { groupCode, memberName });
}

export function apiGetGroup(groupId: number, groupCode: string) {
  return apiCall<GroupSnapshot>('/api/group/get', { groupId, groupCode });
}

export function apiRenameGroup(groupId: number, groupCode: string, newName: string) {
  return apiCall<void>('/api/group/rename', { groupId, groupCode, newName });
}

export function apiGenerateGroupCode(groupId: number, groupCode: string) {
  return apiCall<string>('/api/group/generate-code', { groupId, groupCode });
}

export function apiLeaveGroup(groupId: number, groupCode: string) {
  return apiCall<void>('/api/group/leave', { groupId, groupCode });
}

export function apiCreateBill(
  groupId: number,
  groupCode: string,
  bill: NewBillPayload,
) {
  return apiCall<number>('/api/bill/create', { groupId, groupCode, bill });
}

export function apiChangeDate(
  groupId: number,
  groupCode: string,
  billId: number,
  newDate: string | null,
) {
  return apiCall<void>('/api/bill/change-date', { groupId, groupCode, billId, newDate });
}

export function apiConfirmPayment(
  groupId: number,
  groupCode: string,
  billId: number,
  memberName: string,
) {
  return apiCall<void>('/api/bill/confirm-payment', {
    groupId,
    groupCode,
    billId,
    memberName,
  });
}

export function apiCancelBill(
  groupId: number,
  groupCode: string,
  billId: number,
) {
  return apiCall<void>('/api/bill/cancel', { groupId, groupCode, billId });
}

export function apiCancelAllBills(
  groupId: number,
  groupCode: string,
) {
  return apiCall<void>('/api/bill/cancel-all', { groupId, groupCode });
}
