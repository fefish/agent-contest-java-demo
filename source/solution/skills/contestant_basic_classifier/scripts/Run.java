import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Run {
    public static void main(String[] args) throws Exception {
        String input = new String(System.in.readAllBytes(), StandardCharsets.UTF_8);
        String text = unescape(matchString(input, "text"));
        int tokenCount = text.isBlank() ? 0 : text.trim().split("\\s+").length;
        String label = tokenCount >= 20 ? "LONG" : "SHORT";
        System.out.println("{\n"
                + "  \"label\": \"" + label + "\",\n"
                + "  \"token_count\": " + tokenCount + ",\n"
                + "  \"note\": \"示例 packaged skill：参赛者可以替换为自己的自定义 skill。\"\n"
                + "}");
    }

    private static String matchString(String json, String key) {
        Matcher matcher = Pattern.compile("\"" + Pattern.quote(key) + "\"\\s*:\\s*\"((?:\\\\.|[^\"])*)\"").matcher(json);
        return matcher.find() ? matcher.group(1) : "";
    }

    private static String unescape(String text) {
        return text.replace("\\n", "\n").replace("\\\"", "\"").replace("\\\\", "\\");
    }
}
