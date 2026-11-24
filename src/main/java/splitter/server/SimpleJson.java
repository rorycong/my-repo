package splitter.server;

import splitter.shared.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.*;

/**
 * Lightweight JSON encoder and decoder for Splitter protocol.
 * Free of external dependencies.
 */
public final class SimpleJson {
    private SimpleJson() {}

    public static String escape(String s) {
        if (s == null) return "null";
        StringBuilder sb = new StringBuilder();
        sb.append('"');
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\b' -> sb.append("\\b");
                case '\f' -> sb.append("\\f");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> {
                    if (c < ' ') {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
                }
            }
        }
        sb.append('"');
        return sb.toString();
    }

    public static String toJson(Object obj) {
        if (obj == null) return "null";
        if (obj instanceof String s) return escape(s);
        if (obj instanceof Number || obj instanceof Boolean) return obj.toString();
        if (obj instanceof LocalDate d) return escape(d.toString());
        if (obj instanceof Instant inst) return String.valueOf(inst.toEpochMilli());

        if (obj instanceof Result<?> res) {
            StringBuilder sb = new StringBuilder("{");
            sb.append("\"success\":").append(res.isSuccess());
            if (res.isSuccess()) {
                sb.append(",\"data\":").append(toJson(res.getData()));
            } else {
                sb.append(",\"failureReason\":").append(escape(res.getFailureReason()));
            }
            sb.append("}");
            return sb.toString();
        }

        if (obj instanceof Credentials c) {
            return "{\"username\":" + escape(c.getUsername()) +
                    ",\"loginToken\":" + escape(c.getLoginToken()) + "}";
        }

        if (obj instanceof GroupSummary gs) {
            return "{\"groupId\":" + gs.getGroupId() + ",\"name\":" + escape(gs.getName()) + "}";
        }

        if (obj instanceof Member m) {
            return "{\"name\":" + escape(m.getName()) +
                    ",\"isOwner\":" + m.isOwner() +
                    ",\"hasLeft\":" + m.hasLeft() +
                    ",\"joinedAt\":" + m.getJoinedAt().toEpochMilli() + "}";
        }

        if (obj instanceof BillShare s) {
            return "{\"memberName\":" + escape(s.getMemberName()) +
                    ",\"weightHundredths\":" + s.getWeightHundredths() +
                    ",\"shareCents\":" + s.getShareCents() +
                    ",\"isConfirmed\":" + s.isConfirmed() +
                    ",\"confirmedAt\":" + (s.getConfirmedAt().map(inst -> String.valueOf(inst.toEpochMilli())).orElse("null")) + "}";
        }

        if (obj instanceof Bill b) {
            StringBuilder sb = new StringBuilder("{");
            sb.append("\"id\":").append(b.getId()).append(",");
            sb.append("\"description\":").append(escape(b.getDescription())).append(",");
            sb.append("\"totalCents\":").append(b.getTotalCents()).append(",");
            sb.append("\"billOwnerMemberName\":").append(escape(b.getBillOwnerMemberName())).append(",");
            sb.append("\"createdAt\":").append(b.getCreatedAt().toEpochMilli()).append(",");
            sb.append("\"suggestedDate\":").append(b.getSuggestedDate().map(d -> escape(d.toString())).orElse("null")).append(",");
            sb.append("\"shares\":").append(toJson(b.getShares()));
            sb.append("}");
            return sb.toString();
        }

        if (obj instanceof GroupSnapshot snap) {
            StringBuilder sb = new StringBuilder("{");
            sb.append("\"groupId\":").append(snap.getGroupId()).append(",");
            sb.append("\"groupName\":").append(escape(snap.getGroupName())).append(",");
            sb.append("\"currentMemberName\":").append(escape(snap.getCurrentMemberName())).append(",");
            sb.append("\"isOwner\":").append(snap.isOwner()).append(",");
            sb.append("\"groupCode\":").append(snap.getGroupCode().map(SimpleJson::escape).orElse("null")).append(",");
            sb.append("\"members\":").append(toJson(snap.getMembers())).append(",");
            sb.append("\"bills\":").append(toJson(snap.getBills()));
            sb.append("}");
            return sb.toString();
        }

        if (obj instanceof Collection<?> col) {
            StringBuilder sb = new StringBuilder("[");
            boolean first = true;
            for (Object item : col) {
                if (!first) sb.append(",");
                sb.append(toJson(item));
                first = false;
            }
            sb.append("]");
            return sb.toString();
        }

        if (obj instanceof Map<?, ?> map) {
            StringBuilder sb = new StringBuilder("{");
            boolean first = true;
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                if (!first) sb.append(",");
                sb.append(escape(String.valueOf(entry.getKey()))).append(":").append(toJson(entry.getValue()));
                first = false;
            }
            sb.append("}");
            return sb.toString();
        }

        return escape(obj.toString());
    }

    /**
     * Parses a JSON string into nested Map, List, String, Number, Boolean, or null.
     */
    public static Object parse(String json) {
        if (json == null) return null;
        json = json.trim();
        if (json.isEmpty()) return null;
        return new Parser(json).parseValue();
    }

    private static class Parser {
        private final String src;
        private int pos = 0;

        Parser(String src) {
            this.src = src;
        }

        Object parseValue() {
            skipWhitespace();
            if (pos >= src.length()) return null;
            char c = src.charAt(pos);
            if (c == '{') return parseObject();
            if (c == '[') return parseArray();
            if (c == '"') return parseString();
            if (c == 't' || c == 'f') return parseBoolean();
            if (c == 'n') return parseNull();
            if (c == '-' || Character.isDigit(c)) return parseNumber();
            throw new IllegalArgumentException("Unexpected char at " + pos + ": " + c);
        }

        private void skipWhitespace() {
            while (pos < src.length() && Character.isWhitespace(src.charAt(pos))) {
                pos++;
            }
        }

        private Map<String, Object> parseObject() {
            Map<String, Object> map = new LinkedHashMap<>();
            pos++; // skip '{'
            skipWhitespace();
            if (pos < src.length() && src.charAt(pos) == '}') {
                pos++;
                return map;
            }
            while (pos < src.length()) {
                skipWhitespace();
                String key = parseString();
                skipWhitespace();
                if (pos >= src.length() || src.charAt(pos) != ':') {
                    throw new IllegalArgumentException("Expected ':' at " + pos);
                }
                pos++; // skip ':'
                Object val = parseValue();
                map.put(key, val);
                skipWhitespace();
                if (pos < src.length() && src.charAt(pos) == ',') {
                    pos++;
                } else if (pos < src.length() && src.charAt(pos) == '}') {
                    pos++;
                    break;
                } else {
                    throw new IllegalArgumentException("Expected ',' or '}' at " + pos);
                }
            }
            return map;
        }

        private List<Object> parseArray() {
            List<Object> list = new ArrayList<>();
            pos++; // skip '['
            skipWhitespace();
            if (pos < src.length() && src.charAt(pos) == ']') {
                pos++;
                return list;
            }
            while (pos < src.length()) {
                Object val = parseValue();
                list.add(val);
                skipWhitespace();
                if (pos < src.length() && src.charAt(pos) == ',') {
                    pos++;
                } else if (pos < src.length() && src.charAt(pos) == ']') {
                    pos++;
                    break;
                } else {
                    throw new IllegalArgumentException("Expected ',' or ']' at " + pos);
                }
            }
            return list;
        }

        private String parseString() {
            if (src.charAt(pos) != '"') throw new IllegalArgumentException("Expected '\"' at " + pos);
            pos++;
            StringBuilder sb = new StringBuilder();
            while (pos < src.length()) {
                char c = src.charAt(pos++);
                if (c == '"') return sb.toString();
                if (c == '\\') {
                    if (pos >= src.length()) break;
                    char esc = src.charAt(pos++);
                    switch (esc) {
                        case '"' -> sb.append('"');
                        case '\\' -> sb.append('\\');
                        case '/' -> sb.append('/');
                        case 'b' -> sb.append('\b');
                        case 'f' -> sb.append('\f');
                        case 'n' -> sb.append('\n');
                        case 'r' -> sb.append('\r');
                        case 't' -> sb.append('\t');
                        case 'u' -> {
                            if (pos + 4 <= src.length()) {
                                String hex = src.substring(pos, pos + 4);
                                pos += 4;
                                sb.append((char) Integer.parseInt(hex, 16));
                            }
                        }
                        default -> sb.append(esc);
                    }
                } else {
                    sb.append(c);
                }
            }
            return sb.toString();
        }

        private Boolean parseBoolean() {
            if (src.startsWith("true", pos)) {
                pos += 4;
                return true;
            }
            if (src.startsWith("false", pos)) {
                pos += 5;
                return false;
            }
            throw new IllegalArgumentException("Invalid boolean at " + pos);
        }

        private Object parseNull() {
            if (src.startsWith("null", pos)) {
                pos += 4;
                return null;
            }
            throw new IllegalArgumentException("Invalid null at " + pos);
        }

        private Object parseNumber() {
            int start = pos;
            if (pos < src.length() && src.charAt(pos) == '-') pos++;
            while (pos < src.length() && (Character.isDigit(src.charAt(pos)) || src.charAt(pos) == '.')) {
                pos++;
            }
            String s = src.substring(start, pos);
            if (s.contains(".")) {
                return s;
            }
            return Long.parseLong(s);
        }
    }
}
