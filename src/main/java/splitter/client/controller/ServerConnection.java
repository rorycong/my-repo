package splitter.client.controller;

import splitter.client.model.ConnectionModel;
import splitter.client.model.SessionModel;
import splitter.server.SimpleJson;
import splitter.shared.*;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.*;

/**
 * Controller managing HTTP communication with the server.
 */
public class ServerConnection {
    private final SessionModel session;
    private final ConnectionModel connection;
    private final HttpClient httpClient;

    public ServerConnection(SessionModel session, ConnectionModel connection) {
        this.session = Objects.requireNonNull(session, "SessionModel cannot be null");
        this.connection = Objects.requireNonNull(connection, "ConnectionModel cannot be null");
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
    }

    private String getBaseUrl(String serverAddressOverride) {
        String addr = serverAddressOverride != null && !serverAddressOverride.isBlank()
                ? serverAddressOverride
                : session.getServerAddress();
        if (addr == null || addr.isBlank()) {
            addr = "http://localhost:8080";
        }
        if (!addr.startsWith("http://") && !addr.startsWith("https://")) {
            addr = "http://" + addr;
        }
        if (addr.endsWith("/")) {
            addr = addr.substring(0, addr.length() - 1);
        }
        return addr;
    }

    private Object post(String endpoint, Map<String, Object> payload, String serverAddressOverride) throws Exception {
        String url = getBaseUrl(serverAddressOverride) + endpoint;
        String jsonBody = SimpleJson.toJson(payload);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(5))
                .header("Content-Type", "application/json; charset=UTF-8")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new RuntimeException("Server responded with HTTP " + response.statusCode());
        }
        return SimpleJson.parse(response.body());
    }

    private Map<String, Object> basePayload() {
        Map<String, Object> map = new LinkedHashMap<>();
        Credentials creds = session.getCredentials();
        if (creds != null) {
            Map<String, Object> cMap = new LinkedHashMap<>();
            cMap.put("username", creds.getUsername());
            cMap.put("loginToken", creds.getLoginToken());
            map.put("credentials", cMap);
        }
        return map;
    }

    private Map<String, Object> groupPayload(long groupId, String groupCodeOverride) {
        Map<String, Object> map = basePayload();
        map.put("groupId", groupId);
        String code = groupCodeOverride != null ? groupCodeOverride : session.getSavedGroupCode(groupId).orElse("");
        map.put("groupCode", code);
        return map;
    }

    public Result<Credentials> createUser(String username, String serverAddress) {
        try {
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("username", username);

            Object parsed = post("/api/user/create", payload, serverAddress);
            connection.markConnected();

            @SuppressWarnings("unchecked")
            Map<String, Object> respMap = (Map<String, Object>) parsed;
            boolean success = Boolean.TRUE.equals(respMap.get("success"));
            if (!success) {
                return Result.fail((String) respMap.get("failureReason"));
            }
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) respMap.get("data");
            Credentials creds = new Credentials((String) data.get("username"), (String) data.get("loginToken"));
            return Result.ok(creds);
        } catch (Exception e) {
            connection.markDisconnected(e.getMessage());
            return Result.fail(FailureReasons.SERVER_UNREACHABLE);
        }
    }

    public Result<List<GroupSummary>> listGroups() {
        try {
            Map<String, Object> payload = basePayload();
            Object parsed = post("/api/group/list", payload, null);
            connection.markConnected();

            @SuppressWarnings("unchecked")
            Map<String, Object> respMap = (Map<String, Object>) parsed;
            boolean success = Boolean.TRUE.equals(respMap.get("success"));
            if (!success) {
                return Result.fail((String) respMap.get("failureReason"));
            }
            @SuppressWarnings("unchecked")
            List<Object> list = (List<Object>) respMap.get("data");
            List<GroupSummary> summaries = new ArrayList<>();
            if (list != null) {
                for (Object item : list) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> m = (Map<String, Object>) item;
                    long gid = ((Number) m.get("groupId")).longValue();
                    String name = (String) m.get("name");
                    summaries.add(new GroupSummary(gid, name));
                }
            }
            return Result.ok(summaries);
        } catch (Exception e) {
            connection.markDisconnected(e.getMessage());
            return Result.fail(FailureReasons.SERVER_UNREACHABLE);
        }
    }

    public Result<GroupSnapshot> createGroup(String groupName, String memberName) {
        try {
            Map<String, Object> payload = basePayload();
            payload.put("groupName", groupName);
            payload.put("memberName", memberName);

            Object parsed = post("/api/group/create", payload, null);
            connection.markConnected();

            @SuppressWarnings("unchecked")
            Map<String, Object> respMap = (Map<String, Object>) parsed;
            boolean success = Boolean.TRUE.equals(respMap.get("success"));
            if (!success) {
                return Result.fail((String) respMap.get("failureReason"));
            }
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) respMap.get("data");
            return Result.ok(parseSnapshot(data));
        } catch (Exception e) {
            connection.markDisconnected(e.getMessage());
            return Result.fail(FailureReasons.SERVER_UNREACHABLE);
        }
    }

    public Result<GroupSnapshot> joinGroup(String groupCode, String memberName) {
        try {
            Map<String, Object> payload = basePayload();
            payload.put("groupCode", groupCode);
            payload.put("memberName", memberName);

            Object parsed = post("/api/group/join", payload, null);
            connection.markConnected();

            @SuppressWarnings("unchecked")
            Map<String, Object> respMap = (Map<String, Object>) parsed;
            boolean success = Boolean.TRUE.equals(respMap.get("success"));
            if (!success) {
                return Result.fail((String) respMap.get("failureReason"));
            }
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) respMap.get("data");
            return Result.ok(parseSnapshot(data));
        } catch (Exception e) {
            connection.markDisconnected(e.getMessage());
            return Result.fail(FailureReasons.SERVER_UNREACHABLE);
        }
    }

    public Result<GroupSnapshot> getGroup(long groupId, String groupCode) {
        try {
            Map<String, Object> payload = groupPayload(groupId, groupCode);
            Object parsed = post("/api/group/get", payload, null);
            connection.markConnected();

            @SuppressWarnings("unchecked")
            Map<String, Object> respMap = (Map<String, Object>) parsed;
            boolean success = Boolean.TRUE.equals(respMap.get("success"));
            if (!success) {
                return Result.fail((String) respMap.get("failureReason"));
            }
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) respMap.get("data");
            return Result.ok(parseSnapshot(data));
        } catch (Exception e) {
            connection.markDisconnected(e.getMessage());
            return Result.fail(FailureReasons.SERVER_UNREACHABLE);
        }
    }

    public Result<Void> renameGroup(long groupId, String newName) {
        try {
            Map<String, Object> payload = groupPayload(groupId, null);
            payload.put("newName", newName);

            Object parsed = post("/api/group/rename", payload, null);
            connection.markConnected();

            @SuppressWarnings("unchecked")
            Map<String, Object> respMap = (Map<String, Object>) parsed;
            boolean success = Boolean.TRUE.equals(respMap.get("success"));
            if (!success) {
                return Result.fail((String) respMap.get("failureReason"));
            }
            return Result.ok();
        } catch (Exception e) {
            connection.markDisconnected(e.getMessage());
            return Result.fail(FailureReasons.SERVER_UNREACHABLE);
        }
    }

    public Result<String> generateNewCode(long groupId) {
        try {
            Map<String, Object> payload = groupPayload(groupId, null);
            Object parsed = post("/api/group/generate-code", payload, null);
            connection.markConnected();

            @SuppressWarnings("unchecked")
            Map<String, Object> respMap = (Map<String, Object>) parsed;
            boolean success = Boolean.TRUE.equals(respMap.get("success"));
            if (!success) {
                return Result.fail((String) respMap.get("failureReason"));
            }
            return Result.ok((String) respMap.get("data"));
        } catch (Exception e) {
            connection.markDisconnected(e.getMessage());
            return Result.fail(FailureReasons.SERVER_UNREACHABLE);
        }
    }

    public Result<Void> leaveGroup(long groupId) {
        try {
            Map<String, Object> payload = groupPayload(groupId, null);
            Object parsed = post("/api/group/leave", payload, null);
            connection.markConnected();

            @SuppressWarnings("unchecked")
            Map<String, Object> respMap = (Map<String, Object>) parsed;
            boolean success = Boolean.TRUE.equals(respMap.get("success"));
            if (!success) {
                return Result.fail((String) respMap.get("failureReason"));
            }
            return Result.ok();
        } catch (Exception e) {
            connection.markDisconnected(e.getMessage());
            return Result.fail(FailureReasons.SERVER_UNREACHABLE);
        }
    }

    public Result<Long> createBill(long groupId, NewBill bill) {
        try {
            Map<String, Object> payload = groupPayload(groupId, null);
            Map<String, Object> billMap = new LinkedHashMap<>();
            billMap.put("description", bill.getDescription());
            billMap.put("totalCents", bill.getTotalCents());
            billMap.put("suggestedDate", bill.getSuggestedDate().map(LocalDate::toString).orElse(null));
            billMap.put("weightsHundredths", bill.getWeightsHundredths());
            payload.put("bill", billMap);

            Object parsed = post("/api/bill/create", payload, null);
            connection.markConnected();

            @SuppressWarnings("unchecked")
            Map<String, Object> respMap = (Map<String, Object>) parsed;
            boolean success = Boolean.TRUE.equals(respMap.get("success"));
            if (!success) {
                return Result.fail((String) respMap.get("failureReason"));
            }
            long bid = ((Number) respMap.get("data")).longValue();
            return Result.ok(bid);
        } catch (Exception e) {
            connection.markDisconnected(e.getMessage());
            return Result.fail(FailureReasons.SERVER_UNREACHABLE);
        }
    }

    public Result<Void> changeSuggestedDate(long groupId, long billId, LocalDate newDate) {
        try {
            Map<String, Object> payload = groupPayload(groupId, null);
            payload.put("billId", billId);
            payload.put("newDate", newDate != null ? newDate.toString() : null);

            Object parsed = post("/api/bill/change-date", payload, null);
            connection.markConnected();

            @SuppressWarnings("unchecked")
            Map<String, Object> respMap = (Map<String, Object>) parsed;
            boolean success = Boolean.TRUE.equals(respMap.get("success"));
            if (!success) {
                return Result.fail((String) respMap.get("failureReason"));
            }
            return Result.ok();
        } catch (Exception e) {
            connection.markDisconnected(e.getMessage());
            return Result.fail(FailureReasons.SERVER_UNREACHABLE);
        }
    }

    public Result<Void> confirmPayment(long groupId, long billId, String memberName) {
        try {
            Map<String, Object> payload = groupPayload(groupId, null);
            payload.put("billId", billId);
            payload.put("memberName", memberName);

            Object parsed = post("/api/bill/confirm-payment", payload, null);
            connection.markConnected();

            @SuppressWarnings("unchecked")
            Map<String, Object> respMap = (Map<String, Object>) parsed;
            boolean success = Boolean.TRUE.equals(respMap.get("success"));
            if (!success) {
                return Result.fail((String) respMap.get("failureReason"));
            }
            return Result.ok();
        } catch (Exception e) {
            connection.markDisconnected(e.getMessage());
            return Result.fail(FailureReasons.SERVER_UNREACHABLE);
        }
    }

    public Result<Void> cancelBill(long groupId, long billId) {
        try {
            Map<String, Object> payload = groupPayload(groupId, null);
            payload.put("billId", billId);

            Object parsed = post("/api/bill/cancel", payload, null);
            connection.markConnected();

            @SuppressWarnings("unchecked")
            Map<String, Object> respMap = (Map<String, Object>) parsed;
            boolean success = Boolean.TRUE.equals(respMap.get("success"));
            if (!success) {
                return Result.fail((String) respMap.get("failureReason"));
            }
            return Result.ok();
        } catch (Exception e) {
            connection.markDisconnected(e.getMessage());
            return Result.fail(FailureReasons.SERVER_UNREACHABLE);
        }
    }

    public Result<Void> cancelAllBills(long groupId) {
        try {
            Map<String, Object> payload = groupPayload(groupId, null);
            Object parsed = post("/api/bill/cancel-all", payload, null);
            connection.markConnected();

            @SuppressWarnings("unchecked")
            Map<String, Object> respMap = (Map<String, Object>) parsed;
            boolean success = Boolean.TRUE.equals(respMap.get("success"));
            if (!success) {
                return Result.fail((String) respMap.get("failureReason"));
            }
            return Result.ok();
        } catch (Exception e) {
            connection.markDisconnected(e.getMessage());
            return Result.fail(FailureReasons.SERVER_UNREACHABLE);
        }
    }

    private GroupSnapshot parseSnapshot(Map<String, Object> data) {
        long gid = ((Number) data.get("groupId")).longValue();
        String gname = (String) data.get("groupName");
        String curMember = (String) data.get("currentMemberName");
        boolean isOwner = Boolean.TRUE.equals(data.get("isOwner"));
        String code = (String) data.get("groupCode");

        List<Member> members = new ArrayList<>();
        @SuppressWarnings("unchecked")
        List<Object> mList = (List<Object>) data.get("members");
        if (mList != null) {
            for (Object obj : mList) {
                @SuppressWarnings("unchecked")
                Map<String, Object> m = (Map<String, Object>) obj;
                String name = (String) m.get("name");
                boolean own = Boolean.TRUE.equals(m.get("isOwner"));
                boolean left = Boolean.TRUE.equals(m.get("hasLeft"));
                Instant joined = Instant.ofEpochMilli(((Number) m.get("joinedAt")).longValue());
                members.add(new Member(name, own, left, joined));
            }
        }

        List<Bill> bills = new ArrayList<>();
        @SuppressWarnings("unchecked")
        List<Object> bList = (List<Object>) data.get("bills");
        if (bList != null) {
            for (Object obj : bList) {
                @SuppressWarnings("unchecked")
                Map<String, Object> b = (Map<String, Object>) obj;
                long bid = ((Number) b.get("id")).longValue();
                String desc = (String) b.get("description");
                long total = ((Number) b.get("totalCents")).longValue();
                String bowner = (String) b.get("billOwnerMemberName");
                Instant created = Instant.ofEpochMilli(((Number) b.get("createdAt")).longValue());
                String dateStr = (String) b.get("suggestedDate");
                LocalDate sdate = (dateStr != null && !dateStr.isEmpty()) ? LocalDate.parse(dateStr) : null;

                List<BillShare> shares = new ArrayList<>();
                @SuppressWarnings("unchecked")
                List<Object> sList = (List<Object>) b.get("shares");
                if (sList != null) {
                    for (Object sobj : sList) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> s = (Map<String, Object>) sobj;
                        String sname = (String) s.get("memberName");
                        int weight = ((Number) s.get("weightHundredths")).intValue();
                        long cents = ((Number) s.get("shareCents")).longValue();
                        boolean conf = Boolean.TRUE.equals(s.get("isConfirmed"));
                        Instant confAt = s.get("confirmedAt") != null ? Instant.ofEpochMilli(((Number) s.get("confirmedAt")).longValue()) : null;
                        shares.add(new BillShare(sname, weight, cents, conf, confAt));
                    }
                }
                bills.add(new Bill(bid, desc, total, bowner, created, sdate, shares));
            }
        }

        return new GroupSnapshot(gid, gname, curMember, isOwner, code, members, bills);
    }
}
