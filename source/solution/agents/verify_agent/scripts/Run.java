import java.nio.charset.StandardCharsets;

public class Run {
    public static void main(String[] args) throws Exception {
        String input = new String(System.in.readAllBytes(), StandardCharsets.UTF_8);
        String preview = input.length() > 240 ? input.substring(0, 240) : input;
        preview = preview.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", " ");
        System.out.println("{\n"
                + "  \"agent\": \"verify_agent\",\n"
                + "  \"role\": \"basic verifier\",\n"
                + "  \"verdict\": \"checked\",\n"
                + "  \"summary\": \"示例 packaged sub-agent 已完成基础复核。参赛者可替换为自己的 sub-agent 编排。\",\n"
                + "  \"context_preview\": \"" + preview + "\"\n"
                + "}");
    }
}
