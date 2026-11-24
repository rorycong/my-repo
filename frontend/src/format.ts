import type { Member, Bill } from './types';

/**
 * Format integer cents into dollar display, e.g. 1050 -> "$10.50", -500 -> "-$5.00"
 */
export function formatCents(cents: number): string {
  const negative = cents < 0;
  const abs = Math.abs(cents);
  const dollars = Math.floor(abs / 100);
  const rem = abs % 100;
  return `${negative ? '-' : ''}$${dollars}.${rem < 10 ? '0' : ''}${rem}`;
}

/**
 * Format integer hundredths into percentage display, e.g. 3334 -> "33.34"
 */
export function formatWeight(hundredths: number): string {
  const units = Math.floor(hundredths / 100);
  const frac = Math.abs(hundredths % 100);
  return `${units}.${frac < 10 ? '0' : ''}${frac}`;
}

/**
 * Parses user dollar text input into integer cents strictly without float imprecision.
 * Returns null if invalid or negative/zero.
 */
export function parseAmountToCents(text: string): number | null {
  if (!text) return null;
  let s = text.trim();
  if (s.startsWith('$')) s = s.substring(1).trim();
  if (s.startsWith('-')) return null;

  const parts = s.split('.');
  if (parts.length > 2) return null;

  let dollars = 0;
  let cents = 0;

  if (parts[0].length > 0) {
    if (!/^\d+$/.test(parts[0])) return null;
    dollars = parseInt(parts[0], 10);
  }

  if (parts.length === 2) {
    if (parts[1].length > 2) return null;
    if (parts[1].length > 0) {
      if (!/^\d+$/.test(parts[1])) return null;
      cents = parts[1].length === 1 ? parseInt(parts[1], 10) * 10 : parseInt(parts[1], 10);
    }
  }

  const total = dollars * 100 + cents;
  return total > 0 ? total : null;
}

/**
 * Parses weight text (e.g. "0.50" or "50.00") into hundredths (e.g. 5000 or 50).
 * In the UI:
 * Weights sum to 1.00 (100 hundredths) or 100.00 (10000 hundredths).
 * Standardized to 100 hundredths (1.00 total) matching the UI indicator.
 */
export function parseWeightHundredths(text: string): number | null {
  if (!text) return null;
  const s = text.trim();
  if (s.startsWith('-')) return null;

  const parts = s.split('.');
  if (parts.length > 2) return null;

  let units = 0;
  let frac = 0;

  if (parts[0].length > 0) {
    if (!/^\d+$/.test(parts[0])) return null;
    units = parseInt(parts[0], 10);
  }

  if (parts.length === 2) {
    if (parts[1].length > 2) return null;
    if (parts[1].length > 0) {
      if (!/^\d+$/.test(parts[1])) return null;
      frac = parts[1].length === 1 ? parseInt(parts[1], 10) * 10 : parseInt(parts[1], 10);
    }
  }

  const total = units * 100 + frac;
  return total >= 0 && total <= 100 ? total : null;
}

/**
 * Validates username per USR-3 and USR-4:
 * 3 to 20 characters, alphanumeric and underscore only.
 */
export function isValidUsername(raw: string): boolean {
  if (!raw || raw.length < 3 || raw.length > 20) return false;
  return /^[a-zA-Z0-9_]+$/.test(raw);
}

/**
 * Safely escapes HTML special characters.
 */
export function escapeHtml(str: string): string {
  if (!str) return '';
  return str
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#039;');
}

/**
 * Computes owes and isOwed balances dynamically from current members and active bills.
 * Implements OUT-3: The bill owner's own share is never counted towards their owes or isOwed.
 */
export function computeBalances(
  currentMembers: Member[],
  bills: Bill[],
): Record<string, { owes: number; isOwed: number }> {
  const map: Record<string, { owes: number; isOwed: number }> = {};
  currentMembers.forEach((m) => {
    map[m.name] = { owes: 0, isOwed: 0 };
  });

  (bills || []).forEach((b) => {
    const billOwner = b.billOwnerMemberName;
    (b.shares || []).forEach((s) => {
      if (s.isConfirmed || s.shareCents <= 0) return;
      // OUT-3: Bill owner's own share is never counted
      if (s.memberName.toLowerCase() === billOwner.toLowerCase()) return;

      // Debtor owes
      for (const name in map) {
        if (name.toLowerCase() === s.memberName.toLowerCase()) {
          map[name].owes += s.shareCents;
        }
      }
      // Creditor is owed
      for (const name in map) {
        if (name.toLowerCase() === billOwner.toLowerCase()) {
          map[name].isOwed += s.shareCents;
        }
      }
    });
  });

  return map;
}
