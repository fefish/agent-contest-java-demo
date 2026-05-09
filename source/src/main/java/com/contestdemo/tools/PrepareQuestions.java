package com.contestdemo.tools;

import com.contestdemo.runtime.QuestionIO;

import java.nio.file.Path;
import java.util.Map;
import java.util.List;
import java.util.stream.Collectors;

public final class PrepareQuestions {
    public static void main(String[] args) throws Exception {
        Path source = Path.of(args.length > 0 ? args[0] : "source/examples/official_tasks.json");
        Path output = Path.of(args.length > 1 ? args[1] : "source/examples/questions.json");
        List<Map<String, Object>> questions = QuestionIO.loadTasks(source).stream()
                .map(QuestionIO::publicQuestion)
                .collect(Collectors.toList());
        QuestionIO.writeJson(output, questions);
        System.out.println("wrote " + questions.size() + " public questions to " + output.toAbsolutePath());
    }
}
