package com.contestdemo.runtime;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class Json {
    private Json() {}

    public static Object parse(String text) {
        return new Parser(text).parse();
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> asObject(Object value) {
        return value instanceof Map ? (Map<String, Object>) value : new LinkedHashMap<>();
    }

    @SuppressWarnings("unchecked")
    public static List<Object> asList(Object value) {
        return value instanceof List ? (List<Object>) value : new ArrayList<>();
    }

    public static String string(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    public static double number(Object value, double defaultValue) {
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        try {
            return Double.parseDouble(string(value));
        } catch (Exception ignored) {
            return defaultValue;
        }
    }

    public static String stringify(Object value) {
        StringBuilder out = new StringBuilder();
        write(value, out, 0);
        return out.toString();
    }

    private static void write(Object value, StringBuilder out, int indent) {
        if (value == null) {
            out.append("null");
        } else if (value instanceof String) {
            writeString((String) value, out);
        } else if (value instanceof Number || value instanceof Boolean) {
            out.append(value);
        } else if (value instanceof Map<?, ?>) {
            Map<?, ?> map = (Map<?, ?>) value;
            out.append("{");
            boolean first = true;
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                if (!first) {
                    out.append(",");
                }
                first = false;
                out.append("\n").append(" ".repeat(indent + 2));
                writeString(String.valueOf(entry.getKey()), out);
                out.append(": ");
                write(entry.getValue(), out, indent + 2);
            }
            if (!map.isEmpty()) {
                out.append("\n").append(" ".repeat(indent));
            }
            out.append("}");
        } else if (value instanceof Iterable<?>) {
            Iterable<?> list = (Iterable<?>) value;
            out.append("[");
            boolean first = true;
            for (Object item : list) {
                if (!first) {
                    out.append(",");
                }
                first = false;
                out.append("\n").append(" ".repeat(indent + 2));
                write(item, out, indent + 2);
            }
            if (list.iterator().hasNext()) {
                out.append("\n").append(" ".repeat(indent));
            }
            out.append("]");
        } else {
            writeString(String.valueOf(value), out);
        }
    }

    private static void writeString(String string, StringBuilder out) {
        out.append('"');
        for (int i = 0; i < string.length(); i++) {
            char c = string.charAt(i);
            switch (c) {
                case '"':
                    out.append("\\\"");
                    break;
                case '\\':
                    out.append("\\\\");
                    break;
                case '\b':
                    out.append("\\b");
                    break;
                case '\f':
                    out.append("\\f");
                    break;
                case '\n':
                    out.append("\\n");
                    break;
                case '\r':
                    out.append("\\r");
                    break;
                case '\t':
                    out.append("\\t");
                    break;
                default:
                    if (c < 0x20) {
                        out.append(String.format("\\u%04x", (int) c));
                    } else {
                        out.append(c);
                    }
                    break;
            }
        }
        out.append('"');
    }

    private static final class Parser {
        private final String text;
        private int pos;

        private Parser(String text) {
            this.text = text == null ? "" : text;
        }

        private Object parse() {
            skipWhitespace();
            Object value = parseValue();
            skipWhitespace();
            return value;
        }

        private Object parseValue() {
            skipWhitespace();
            if (pos >= text.length()) {
                return null;
            }
            char c = text.charAt(pos);
            if (c == '"') {
                return parseString();
            }
            if (c == '{') {
                return parseObject();
            }
            if (c == '[') {
                return parseArray();
            }
            if (text.startsWith("true", pos)) {
                pos += 4;
                return true;
            }
            if (text.startsWith("false", pos)) {
                pos += 5;
                return false;
            }
            if (text.startsWith("null", pos)) {
                pos += 4;
                return null;
            }
            return parseNumber();
        }

        private Map<String, Object> parseObject() {
            Map<String, Object> result = new LinkedHashMap<>();
            pos++;
            skipWhitespace();
            if (peek('}')) {
                pos++;
                return result;
            }
            while (pos < text.length()) {
                String key = parseString();
                skipWhitespace();
                expect(':');
                Object value = parseValue();
                result.put(key, value);
                skipWhitespace();
                if (peek('}')) {
                    pos++;
                    break;
                }
                expect(',');
                skipWhitespace();
            }
            return result;
        }

        private List<Object> parseArray() {
            List<Object> result = new ArrayList<>();
            pos++;
            skipWhitespace();
            if (peek(']')) {
                pos++;
                return result;
            }
            while (pos < text.length()) {
                result.add(parseValue());
                skipWhitespace();
                if (peek(']')) {
                    pos++;
                    break;
                }
                expect(',');
                skipWhitespace();
            }
            return result;
        }

        private String parseString() {
            expect('"');
            StringBuilder out = new StringBuilder();
            while (pos < text.length()) {
                char c = text.charAt(pos++);
                if (c == '"') {
                    break;
                }
                if (c == '\\' && pos < text.length()) {
                    char escaped = text.charAt(pos++);
                    switch (escaped) {
                        case '"':
                            out.append('"');
                            break;
                        case '\\':
                            out.append('\\');
                            break;
                        case '/':
                            out.append('/');
                            break;
                        case 'b':
                            out.append('\b');
                            break;
                        case 'f':
                            out.append('\f');
                            break;
                        case 'n':
                            out.append('\n');
                            break;
                        case 'r':
                            out.append('\r');
                            break;
                        case 't':
                            out.append('\t');
                            break;
                        case 'u':
                            String hex = text.substring(pos, Math.min(pos + 4, text.length()));
                            pos += Math.min(4, hex.length());
                            out.append((char) Integer.parseInt(hex, 16));
                            break;
                        default:
                            out.append(escaped);
                            break;
                    }
                } else {
                    out.append(c);
                }
            }
            return out.toString();
        }

        private Number parseNumber() {
            int start = pos;
            while (pos < text.length()) {
                char c = text.charAt(pos);
                if ((c >= '0' && c <= '9') || c == '-' || c == '+' || c == '.' || c == 'e' || c == 'E') {
                    pos++;
                } else {
                    break;
                }
            }
            String raw = text.substring(start, pos);
            if (raw.contains(".") || raw.contains("e") || raw.contains("E")) {
                return Double.parseDouble(raw);
            }
            return Long.parseLong(raw);
        }

        private boolean peek(char c) {
            return pos < text.length() && text.charAt(pos) == c;
        }

        private void expect(char c) {
            if (!peek(c)) {
                throw new IllegalArgumentException("Expected '" + c + "' at position " + pos);
            }
            pos++;
        }

        private void skipWhitespace() {
            while (pos < text.length() && Character.isWhitespace(text.charAt(pos))) {
                pos++;
            }
        }
    }
}
