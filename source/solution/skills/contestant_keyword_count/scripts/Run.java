import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Run {
    public static void main(String[] args) throws Exception {
        String input = new String(System.in.readAllBytes(), StandardCharsets.UTF_8);
        String text = unescape(matchString(input, "text"));
        List<String> keywords = matchArray(input, "keywords");
        StringBuilder out = new StringBuilder();
        out.append("{\n  \"counts\": {");
        for (int i = 0; i < keywords.size(); i++) {
            String keyword = keywords.get(i);
            if (i > 0) {
                out.append(",");
            }
            out.append("\n    \"").append(escape(keyword)).append("\": ").append(count(text, keyword));
        }
        if (!keywords.isEmpty()) {
            out.append("\n  ");
        }
        out.append("}\n}");
        System.out.println(out);
    }

    private static int count(String text, String keyword) {
        if (keyword.isEmpty()) {
            return 0;
        }
        int count = 0;
        int index = 0;
        while ((index = text.indexOf(keyword, index)) >= 0) {
            count++;
            index += keyword.length();
        }
        return count;
    }

    private static String matchString(String json, String key) {
        Matcher matcher = Pattern.compile("\"" + Pattern.quote(key) + "\"\\s*:\\s*\"((?:\\\\.|[^\"])*)\"").matcher(json);
        return matcher.find() ? matcher.group(1) : "";
    }

    private static List<String> matchArray(String json, String key) {
        Matcher matcher = Pattern.compile("\"" + Pattern.quote(key) + "\"\\s*:\\s*\\[(.*?)\\]", Pattern.DOTALL).matcher(json);
        List<String> values = new ArrayList<>();
        if (!matcher.find()) {
            return values;
        }
        Matcher item = Pattern.compile("\"((?:\\\\.|[^\"])*)\"").matcher(matcher.group(1));
        while (item.find()) {
            values.add(unescape(item.group(1)));
        }
        return values;
    }

    private static String unescape(String text) {
        return text.replace("\\n", "\n").replace("\\\"", "\"").replace("\\\\", "\\");
    }

    private static String escape(String text) {
        return text.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
