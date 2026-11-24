// Shared types matching the Milestone 2 & 3 Java architecture

export interface Result<T> {
  success: boolean;
  data?: T;
  failureReason?: string;
}

export interface Credentials {
  username: string;
  loginToken: string;
}

export interface GroupSummary {
  groupId: number;
  name: string;
}

export interface Member {
  name: string;
  isOwner: boolean;
  hasLeft: boolean;
  joinedAt: number;
}

export interface BillShare {
  memberName: string;
  weightHundredths: number;
  shareCents: number;
  isConfirmed: boolean;
  confirmedAt?: number;
}

export interface Bill {
  id: number;
  description: string;
  totalCents: number;
  billOwnerMemberName: string;
  createdAt: number;
  suggestedDate?: string | null;
  shares: BillShare[];
}

export interface GroupSnapshot {
  groupId: number;
  groupName: string;
  currentMemberName: string;
  isOwner: boolean;
  groupCode?: string;
  members: Member[];
  bills: Bill[];
}

export interface NewBillPayload {
  description: string;
  totalCents: number;
  suggestedDate: string | null;
  weightsHundredths: Record<string, number>;
}

export interface AppState {
  username: string | null;
  loginToken: string | null;
  serverAddress: string;
  savedCodes: Record<number, string>;
  connected: boolean;
  groups: GroupSummary[];
  currentGroup: GroupSnapshot | null;
  selectedBillId: number | null;
  currentScreen: 'username' | 'home' | 'group';
}
