package splitter.server.model;

import splitter.shared.Bill;
import splitter.shared.BillShare;
import splitter.shared.GroupSummary;
import splitter.shared.InputValidator;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Transactional repository for groups, members, bills, and shares.
 * Persists all state atomically to disk.
 */
public class GroupRepository {
    private final Path dbFile;
    private final Map<Long, Group> groupsById = new LinkedHashMap<>();
    private final AtomicLong nextGroupId = new AtomicLong(1);
    private final AtomicLong nextBillId = new AtomicLong(1);
    private final Object fileLock = new Object();

    public GroupRepository() {
        this(null);
    }

    public GroupRepository(Path dbFile) {
        this.dbFile = dbFile;
        if (dbFile != null) {
            load();
        }
    }

    private void load() {
        synchronized (fileLock) {
            groupsById.clear();
            if (dbFile == null || !Files.exists(dbFile)) {
                return;
            }
            try (BufferedReader reader = Files.newBufferedReader(dbFile, StandardCharsets.UTF_8)) {
                String line;
                Group currentGroup = null;
                List<BillShare> currentShares = new ArrayList<>();
                PendingBill pendingBill = null;

                while ((line = reader.readLine()) != null) {
                    if (line.isBlank()) continue;
                    String[] parts = line.split("\t", -1);
                    String tag = parts[0];

                    switch (tag) {
                        case "META":
                            if (parts.length >= 3) {
                                nextGroupId.set(Long.parseLong(parts[1]));
                                nextBillId.set(Long.parseLong(parts[2]));
                            }
                            break;
                        case "GROUP":
                            if (pendingBill != null && currentGroup != null) {
                                currentGroup.getBills().add(new Bill(pendingBill.id, pendingBill.desc, pendingBill.total,
                                        pendingBill.bowner, pendingBill.created, pendingBill.sdate, currentShares));
                                pendingBill = null;
                                currentShares = new ArrayList<>();
                            }
                            // GROUP \t id \t name \t currentCode \t ownerUsername
                            long gid = Long.parseLong(parts[1]);
                            String gname = unescape(parts[2]);
                            String code = parts[3];
                            String ownerUser = parts[4];
                            currentGroup = new Group(gid, gname, code, ownerUser, new ArrayList<>(), new ArrayList<>());
                            groupsById.put(gid, currentGroup);
                            if (gid >= nextGroupId.get()) {
                                nextGroupId.set(gid + 1);
                            }
                            break;
                        case "MEMBER":
                            // MEMBER \t username \t name \t joinedEpochMilli \t hasLeft
                            if (currentGroup != null) {
                                String u = parts[1];
                                String mname = unescape(parts[2]);
                                Instant joined = Instant.ofEpochMilli(Long.parseLong(parts[3]));
                                boolean hasLeft = Boolean.parseBoolean(parts[4]);
                                currentGroup.getAllMembers().add(new GroupMember(u, mname, joined, hasLeft));
                            }
                            break;
                        case "BILL":
                            if (pendingBill != null && currentGroup != null) {
                                currentGroup.getBills().add(new Bill(pendingBill.id, pendingBill.desc, pendingBill.total,
                                        pendingBill.bowner, pendingBill.created, pendingBill.sdate, currentShares));
                                pendingBill = null;
                                currentShares = new ArrayList<>();
                            }
                            // BILL \t id \t description \t totalCents \t ownerMemberName \t createdEpochMilli \t dateStr
                            if (currentGroup != null) {
                                long bid = Long.parseLong(parts[1]);
                                String desc = unescape(parts[2]);
                                long total = Long.parseLong(parts[3]);
                                String bowner = unescape(parts[4]);
                                Instant created = Instant.ofEpochMilli(Long.parseLong(parts[5]));
                                LocalDate sdate = parts[6].isEmpty() ? null : LocalDate.parse(parts[6]);
                                pendingBill = new PendingBill(bid, desc, total, bowner, created, sdate);
                                if (bid >= nextBillId.get()) {
                                    nextBillId.set(bid + 1);
                                }
                            }
                            break;
                        case "SHARE":
                            // SHARE \t memberName \t weight \t shareCents \t confirmed \t confirmedEpochMilliOrEmpty
                            String smember = unescape(parts[1]);
                            int weight = Integer.parseInt(parts[2]);
                            long shareCents = Long.parseLong(parts[3]);
                            boolean conf = Boolean.parseBoolean(parts[4]);
                            Instant confAt = parts[5].isEmpty() ? null : Instant.ofEpochMilli(Long.parseLong(parts[5]));
                            currentShares.add(new BillShare(smember, weight, shareCents, conf, confAt));
                            break;
                    }
                }
                if (pendingBill != null && currentGroup != null) {
                    currentGroup.getBills().add(new Bill(pendingBill.id, pendingBill.desc, pendingBill.total,
                            pendingBill.bowner, pendingBill.created, pendingBill.sdate, currentShares));
                }
            } catch (Exception e) {
                throw new StorageException("Failed to load group database", e);
            }
        }
    }

    private static class PendingBill {
        final long id;
        final String desc;
        final long total;
        final String bowner;
        final Instant created;
        final LocalDate sdate;

        PendingBill(long id, String desc, long total, String bowner, Instant created, LocalDate sdate) {
            this.id = id;
            this.desc = desc;
            this.total = total;
            this.bowner = bowner;
            this.created = created;
            this.sdate = sdate;
        }
    }

    private void save() {
        if (dbFile == null) return;
        synchronized (fileLock) {
            try {
                Path parent = dbFile.getParent();
                if (parent != null && !Files.exists(parent)) {
                    Files.createDirectories(parent);
                }
                Path tempFile = Files.createTempFile(parent != null ? parent : Path.of("."), "groups", ".tmp");
                try (BufferedWriter writer = Files.newBufferedWriter(tempFile, StandardCharsets.UTF_8)) {
                    writer.write("META\t" + nextGroupId.get() + "\t" + nextBillId.get() + "\n");
                    for (Group g : groupsById.values()) {
                        writer.write("GROUP\t" + g.getId() + "\t" + escape(g.getName()) + "\t" +
                                g.getCurrentCode() + "\t" + g.getOwnerUsername() + "\n");
                        for (GroupMember m : g.getAllMembers()) {
                            writer.write("MEMBER\t" + m.getUsername() + "\t" + escape(m.getName()) + "\t" +
                                    m.getJoinedAt().toEpochMilli() + "\t" + m.hasLeft() + "\n");
                        }
                        for (Bill b : g.getBills()) {
                            String dstr = b.getSuggestedDate().map(LocalDate::toString).orElse("");
                            writer.write("BILL\t" + b.getId() + "\t" + escape(b.getDescription()) + "\t" +
                                    b.getTotalCents() + "\t" + escape(b.getBillOwnerMemberName()) + "\t" +
                                    b.getCreatedAt().toEpochMilli() + "\t" + dstr + "\n");
                            for (BillShare s : b.getShares()) {
                                String cstr = s.getConfirmedAt().map(inst -> String.valueOf(inst.toEpochMilli())).orElse("");
                                writer.write("SHARE\t" + escape(s.getMemberName()) + "\t" + s.getWeightHundredths() + "\t" +
                                        s.getShareCents() + "\t" + s.isConfirmed() + "\t" + cstr + "\n");
                            }
                        }
                    }
                }
                Files.move(tempFile, dbFile, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (IOException e) {
                throw new StorageException("Failed to persist group database", e);
            }
        }
    }

    private String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\t", "\\t").replace("\n", "\\n");
    }

    private String unescape(String s) {
        if (s == null) return "";
        return s.replace("\\n", "\n").replace("\\t", "\t").replace("\\\\", "\\");
    }

    public synchronized boolean codeExists(String code) {
        if (code == null) return false;
        String normalized = InputValidator.normalizeGroupCode(code);
        for (Group g : groupsById.values()) {
            if (InputValidator.normalizeGroupCode(g.getCurrentCode()).equals(normalized)) {
                return true;
            }
        }
        return false;
    }

    public synchronized long createGroup(String name, String code, String ownerUsername, String ownerMemberName) {
        long gid = nextGroupId.getAndIncrement();
        String normalizedCode = InputValidator.normalizeGroupCode(code);
        List<GroupMember> members = new ArrayList<>();
        members.add(new GroupMember(ownerUsername, ownerMemberName, Instant.now(), false));
        Group group = new Group(gid, name, normalizedCode, ownerUsername, members, new ArrayList<>());
        groupsById.put(gid, group);
        save();
        return gid;
    }

    public synchronized Optional<Group> loadGroup(long groupId) {
        Group g = groupsById.get(groupId);
        if (g == null) return Optional.empty();
        return Optional.of(cloneGroup(g));
    }

    public synchronized Optional<Group> loadGroupByCode(String code) {
        if (code == null) return Optional.empty();
        String normalized = InputValidator.normalizeGroupCode(code);
        for (Group g : groupsById.values()) {
            if (InputValidator.normalizeGroupCode(g.getCurrentCode()).equals(normalized)) {
                return Optional.of(cloneGroup(g));
            }
        }
        return Optional.empty();
    }

    public synchronized List<GroupSummary> listGroupsOfUser(String username) {
        if (username == null) return Collections.emptyList();
        List<GroupSummary> summaries = new ArrayList<>();
        for (Group g : groupsById.values()) {
            if (g.findCurrentMemberByUsername(username).isPresent()) {
                summaries.add(new GroupSummary(g.getId(), g.getName()));
            }
        }
        return summaries;
    }

    public synchronized void renameGroup(long groupId, String newName) {
        Group g = groupsById.get(groupId);
        if (g == null) throw new StorageException("Group not found: " + groupId);
        g.setName(newName);
        save();
    }

    public synchronized void replaceCode(long groupId, String newCode) {
        Group g = groupsById.get(groupId);
        if (g == null) throw new StorageException("Group not found: " + groupId);
        g.setCurrentCode(InputValidator.normalizeGroupCode(newCode));
        save();
    }

    public synchronized void addMember(long groupId, String username, String memberName) {
        Group g = groupsById.get(groupId);
        if (g == null) throw new StorageException("Group not found: " + groupId);
        g.getAllMembers().add(new GroupMember(username, memberName, Instant.now(), false));
        save();
    }

    public synchronized void leaveGroup(long groupId, String username, String newOwnerUsername) {
        Group g = groupsById.get(groupId);
        if (g == null) throw new StorageException("Group not found: " + groupId);
        for (GroupMember m : g.getAllMembers()) {
            if (m.getUsername().equalsIgnoreCase(username) && !m.hasLeft()) {
                m.markLeft();
            }
        }
        if (newOwnerUsername != null) {
            g.setOwnerUsername(newOwnerUsername);
        }
        save();
    }

    public synchronized void deleteGroup(long groupId) {
        groupsById.remove(groupId);
        save();
    }

    public synchronized long addBill(long groupId, Bill bill) {
        Group g = groupsById.get(groupId);
        if (g == null) throw new StorageException("Group not found: " + groupId);
        long bid = nextBillId.getAndIncrement();
        Bill createdBill = new Bill(bid, bill.getDescription(), bill.getTotalCents(),
                bill.getBillOwnerMemberName(), bill.getCreatedAt(), bill.getSuggestedDateValue(), bill.getShares());
        // Bills are stored newest first (PER-6)
        g.getBills().add(0, createdBill);
        save();
        return bid;
    }

    public synchronized boolean setBillDate(long groupId, long billId, LocalDate date) {
        Group g = groupsById.get(groupId);
        if (g == null) return false;
        List<Bill> bills = g.getBills();
        for (int i = 0; i < bills.size(); i++) {
            Bill b = bills.get(i);
            if (b.getId() == billId) {
                Bill updated = new Bill(b.getId(), b.getDescription(), b.getTotalCents(),
                        b.getBillOwnerMemberName(), b.getCreatedAt(), date, b.getShares());
                bills.set(i, updated);
                save();
                return true;
            }
        }
        return false;
    }

    public synchronized boolean confirmShare(long groupId, long billId, String memberName, Instant when) {
        Group g = groupsById.get(groupId);
        if (g == null) return false;
        List<Bill> bills = g.getBills();
        for (int i = 0; i < bills.size(); i++) {
            Bill b = bills.get(i);
            if (b.getId() == billId) {
                List<BillShare> updatedShares = new ArrayList<>();
                boolean found = false;
                for (BillShare s : b.getShares()) {
                    if (InputValidator.namesEqual(s.getMemberName(), memberName)) {
                        updatedShares.add(new BillShare(s.getMemberName(), s.getWeightHundredths(),
                                s.getShareCents(), true, when));
                        found = true;
                    } else {
                        updatedShares.add(s);
                    }
                }
                if (found) {
                    Bill updated = new Bill(b.getId(), b.getDescription(), b.getTotalCents(),
                            b.getBillOwnerMemberName(), b.getCreatedAt(), b.getSuggestedDateValue(), updatedShares);
                    bills.set(i, updated);
                    save();
                    return true;
                }
            }
        }
        return false;
    }

    public synchronized boolean deleteBill(long groupId, long billId) {
        Group g = groupsById.get(groupId);
        if (g == null) return false;
        boolean removed = g.getBills().removeIf(b -> b.getId() == billId);
        if (removed) {
            save();
        }
        return removed;
    }

    public synchronized void deleteAllBills(long groupId) {
        Group g = groupsById.get(groupId);
        if (g == null) throw new StorageException("Group not found: " + groupId);
        g.getBills().clear();
        save();
    }

    private Group cloneGroup(Group g) {
        List<GroupMember> members = new ArrayList<>();
        for (GroupMember m : g.getAllMembers()) {
            members.add(new GroupMember(m.getUsername(), m.getName(), m.getJoinedAt(), m.hasLeft()));
        }
        List<Bill> bills = new ArrayList<>(g.getBills());
        return new Group(g.getId(), g.getName(), g.getCurrentCode(), g.getOwnerUsername(), members, bills);
    }
}
