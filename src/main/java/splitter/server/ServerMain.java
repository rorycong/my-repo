package splitter.server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import splitter.server.controller.*;
import splitter.server.model.*;
import splitter.shared.*;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.*;

/**
 * Main HTTP server application: initializes controllers, handles HTTP endpoints,
 * and serves the responsive web interface.
 */
public class ServerMain {
    private final int port;
    private final Path dataDir;
    private HttpServer server;

    private final UserRepository userRepository;
    private final GroupRepository groupRepository;
    private final TokenGenerator tokenGenerator;
    private final GroupCodeGenerator groupCodeGenerator;
    private final GroupLockManager lockManager;
    private final AuthGuard authGuard;
    private final UserApiController userApiController;
    private final GroupApiController groupApiController;
    private final BillApiController billApiController;

    public ServerMain() {
        this(8080, Path.of("data"));
    }

    public ServerMain(int port) {
        this(port, Path.of("data"));
    }

    public ServerMain(int port, Path dataDir) {
        this.port = port;
        this.dataDir = dataDir;

        Path usersDb = dataDir != null ? dataDir.resolve("users.db") : null;
        Path groupsDb = dataDir != null ? dataDir.resolve("groups.db") : null;

        this.userRepository = new UserRepository(usersDb);
        this.groupRepository = new GroupRepository(groupsDb);
        this.tokenGenerator = new TokenGenerator();
        this.groupCodeGenerator = new GroupCodeGenerator();
        this.lockManager = new GroupLockManager();
        this.authGuard = new AuthGuard(userRepository, groupRepository);
        this.userApiController = new UserApiController(userRepository, tokenGenerator);
        this.groupApiController = new GroupApiController(authGuard, groupRepository, groupCodeGenerator, lockManager);
        this.billApiController = new BillApiController(authGuard, groupRepository, new SplitCalculator(), lockManager);
    }

    public void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress(port), 0);

        server.createContext("/api/user/create", exchange -> handlePost(exchange, body -> {
            String username = (String) body.get("username");
            return userApiController.createUser(username);
        }));

        server.createContext("/api/group/list", exchange -> handlePost(exchange, body -> {
            Credentials creds = extractCredentials(body);
            return groupApiController.listGroups(creds);
        }));

        server.createContext("/api/group/create", exchange -> handlePost(exchange, body -> {
            Credentials creds = extractCredentials(body);
            String gName = (String) body.get("groupName");
            String mName = (String) body.get("memberName");
            return groupApiController.createGroup(creds, gName, mName);
        }));

        server.createContext("/api/group/join", exchange -> handlePost(exchange, body -> {
            Credentials creds = extractCredentials(body);
            String code = (String) body.get("groupCode");
            String mName = (String) body.get("memberName");
            return groupApiController.joinGroup(creds, code, mName);
        }));

        server.createContext("/api/group/get", exchange -> handlePost(exchange, body -> {
            GroupAccess access = extractGroupAccess(body);
            return groupApiController.getGroup(access);
        }));

        server.createContext("/api/group/rename", exchange -> handlePost(exchange, body -> {
            GroupAccess access = extractGroupAccess(body);
            String newName = (String) body.get("newName");
            return groupApiController.renameGroup(access, newName);
        }));

        server.createContext("/api/group/generate-code", exchange -> handlePost(exchange, body -> {
            GroupAccess access = extractGroupAccess(body);
            return groupApiController.generateNewCode(access);
        }));

        server.createContext("/api/group/leave", exchange -> handlePost(exchange, body -> {
            GroupAccess access = extractGroupAccess(body);
            return groupApiController.leaveGroup(access);
        }));

        server.createContext("/api/bill/create", exchange -> handlePost(exchange, body -> {
            GroupAccess access = extractGroupAccess(body);
            @SuppressWarnings("unchecked")
            Map<String, Object> billMap = (Map<String, Object>) body.get("bill");
            String desc = (String) billMap.get("description");
            long total = ((Number) billMap.get("totalCents")).longValue();
            String dateStr = (String) billMap.get("suggestedDate");
            LocalDate date = (dateStr != null && !dateStr.isEmpty()) ? LocalDate.parse(dateStr) : null;
            @SuppressWarnings("unchecked")
            Map<String, Object> rawWeights = (Map<String, Object>) billMap.get("weightsHundredths");
            Map<String, Integer> weights = new LinkedHashMap<>();
            if (rawWeights != null) {
                for (Map.Entry<String, Object> entry : rawWeights.entrySet()) {
                    weights.put(entry.getKey(), ((Number) entry.getValue()).intValue());
                }
            }
            NewBill newBill = new NewBill(desc, total, weights, date);
            return billApiController.createBill(access, newBill);
        }));

        server.createContext("/api/bill/change-date", exchange -> handlePost(exchange, body -> {
            GroupAccess access = extractGroupAccess(body);
            long billId = ((Number) body.get("billId")).longValue();
            String dateStr = (String) body.get("newDate");
            LocalDate newDate = (dateStr != null && !dateStr.isEmpty()) ? LocalDate.parse(dateStr) : null;
            return billApiController.changeSuggestedDate(access, billId, newDate);
        }));

        server.createContext("/api/bill/confirm-payment", exchange -> handlePost(exchange, body -> {
            GroupAccess access = extractGroupAccess(body);
            long billId = ((Number) body.get("billId")).longValue();
            String memberName = (String) body.get("memberName");
            return billApiController.confirmPayment(access, billId, memberName);
        }));

        server.createContext("/api/bill/cancel", exchange -> handlePost(exchange, body -> {
            GroupAccess access = extractGroupAccess(body);
            long billId = ((Number) body.get("billId")).longValue();
            return billApiController.cancelBill(access, billId);
        }));

        server.createContext("/api/bill/cancel-all", exchange -> handlePost(exchange, body -> {
            GroupAccess access = extractGroupAccess(body);
            return billApiController.cancelAllBills(access);
        }));

        // Static files handler for web UI
        server.createContext("/", new StaticFileHandler());

        server.setExecutor(null);
        server.start();
        System.out.println("Shared Expense Splitter server listening on port " + port);
    }

    public void stop() {
        if (server != null) {
            server.stop(0);
        }
    }

    public int getPort() {
        return server != null ? server.getAddress().getPort() : port;
    }

    @FunctionalInterface
    private interface RequestAction {
        Result<?> execute(Map<String, Object> body) throws Exception;
    }

    private void handlePost(HttpExchange exchange, RequestAction action) throws IOException {
        try {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendCors(exchange);
                exchange.sendResponseHeaders(204, -1);
                return;
            }
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }

            String bodyStr = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            @SuppressWarnings("unchecked")
            Map<String, Object> body = (Map<String, Object>) SimpleJson.parse(bodyStr);
            if (body == null) body = Collections.emptyMap();

            Result<?> result = action.execute(body);
            String jsonResp = SimpleJson.toJson(result);

            sendCors(exchange);
            byte[] bytes = jsonResp.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        } catch (Exception e) {
            // GEN-13: The server shall not terminate with an unhandled exception as a result of any request it receives
            Result<?> errResult = Result.fail("Server error: " + e.getMessage());
            String jsonResp = SimpleJson.toJson(errResult);
            byte[] bytes = jsonResp.getBytes(StandardCharsets.UTF_8);
            sendCors(exchange);
            exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        }
    }

    private void sendCors(HttpExchange exchange) {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "POST, GET, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
    }

    private Credentials extractCredentials(Map<String, Object> body) {
        @SuppressWarnings("unchecked")
        Map<String, Object> credsMap = (Map<String, Object>) body.get("credentials");
        if (credsMap == null) return null;
        String u = (String) credsMap.get("username");
        String t = (String) credsMap.get("loginToken");
        return new Credentials(u, t);
    }

    private GroupAccess extractGroupAccess(Map<String, Object> body) {
        Credentials creds = extractCredentials(body);
        long groupId = body.containsKey("groupId") ? ((Number) body.get("groupId")).longValue() : 0L;
        String groupCode = (String) body.get("groupCode");
        return new GroupAccess(creds, groupId, groupCode);
    }

    private static class StaticFileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            if (path == null || path.equals("/") || path.isEmpty()) {
                path = "/index.html";
            }
            String resourcePath = "/web" + path;
            InputStream is = getClass().getResourceAsStream(resourcePath);
            if (is == null) {
                // If not found in classpath, try file system
                File file = new File("src/main/resources" + resourcePath);
                if (file.exists() && file.isFile()) {
                    is = new FileInputStream(file);
                }
            }

            if (is == null) {
                exchange.sendResponseHeaders(404, -1);
                return;
            }

            String contentType = "text/html; charset=UTF-8";
            if (path.endsWith(".css")) contentType = "text/css; charset=UTF-8";
            else if (path.endsWith(".js")) contentType = "application/javascript; charset=UTF-8";
            else if (path.endsWith(".png")) contentType = "image/png";

            byte[] bytes = is.readAllBytes();
            is.close();

            exchange.getResponseHeaders().set("Content-Type", contentType);
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        }
    }

    public static void main(String[] args) throws IOException {
        int port = 8080;
        String envPort = System.getenv("PORT");
        if (envPort != null && !envPort.isBlank()) {
            try {
                port = Integer.parseInt(envPort.trim());
            } catch (NumberFormatException ignored) {}
        }
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException ignored) {}
        }
        ServerMain server = new ServerMain(port);
        server.start();
    }
}
