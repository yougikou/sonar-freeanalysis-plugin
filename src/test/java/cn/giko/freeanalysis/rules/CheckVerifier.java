package cn.giko.freeanalysis.rules;

import cn.giko.freeanalysis.extensions.base.FreeAnalysisLanguage;
import cn.giko.freeanalysis.rules.base.TargetFile;
import cn.giko.freeanalysis.rules.base.AbstractCheck;
import com.sonarsource.checks.verifier.SingleFileVerifier;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.TextRange;
import org.sonar.api.batch.fs.internal.DefaultInputFile;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;
import org.sonar.api.batch.rule.internal.ActiveRulesBuilder;
import org.sonar.api.batch.rule.internal.NewActiveRule;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.batch.sensor.issue.Issue;
import org.sonar.api.batch.sensor.issue.IssueLocation;
import org.sonar.api.rule.RuleKey;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

public class CheckVerifier {
    private static final Path BASE_DIR = Paths.get("src/test/resources/checks/");
    private static final RuleKey RULE_KEY = RuleKey.of("repoKey", "ruleKey");

    private final Collection<Issue> issues;
    private final TargetFile file;

    private CheckVerifier(TargetFile file, Collection<Issue> issues) {
        this.file = file;
        this.issues = issues;
    }

    public static void verifyIssueOnFile(String relativePath, AbstractCheck check, String expectedIssueMessage, int... secondaryLines) {
        createVerifier(relativePath, check).checkIssueOnFile(expectedIssueMessage, secondaryLines);
    }

    public static void verifyIssues(String relativePath, AbstractCheck check) {
        createVerifier(relativePath, check).checkIssues();
    }

    public static void verifyNoIssue(String relativePath, AbstractCheck check) {
        createVerifier(relativePath, check).checkNoIssues();
    }

    private static CheckVerifier createVerifier(String fileName, AbstractCheck check) {
        File file = new File(new File(BASE_DIR.toFile(), check.getClass().getSimpleName()), fileName);

        SensorContextTester context = SensorContextTester.create(BASE_DIR)
                .setActiveRules(new ActiveRulesBuilder().addRule(new NewActiveRule.Builder().setRuleKey(RULE_KEY).build()).build());

        String filePath = file.getPath();
        String content;
        try (Stream<String> lines = Files.lines(file.toPath())) {
            content = lines.collect(Collectors.joining("\n"));
        } catch (IOException e) {
            throw new IllegalStateException(String.format("Unable to load content of file %s", filePath), e);
        }

        DefaultInputFile defaultInputFile = TestInputFileBuilder.create("", filePath)
                .setType(InputFile.Type.MAIN)
                .initMetadata(content)
                .setLanguage(FreeAnalysisLanguage.NAME)
                .setCharset(StandardCharsets.UTF_8)
                .build();

        context.fileSystem().add(defaultInputFile);

        TargetFile targetFile;
        try {
            targetFile = TargetFile.create(defaultInputFile);
        } catch (IOException e) {
            throw new IllegalStateException(String.format("Unable to scan xml file %s", filePath), e);
        }

        check.scanFile(context, RULE_KEY, targetFile);
        return new CheckVerifier(targetFile, context.allIssues());
    }

    private void checkIssues() {
        SingleFileVerifier fileVerifier = SingleFileVerifier.create(file.getInputFile().path(), StandardCharsets.UTF_8);
//        addComments(fileVerifier, file.getDocument());

        issues.forEach(issue -> {
            IssueLocation loc = issue.primaryLocation();
            TextRange textRange = loc.textRange();
            SingleFileVerifier.Issue actualIssue = fileVerifier
                    .reportIssue(loc.message())
                    .onRange(
                            textRange.start().line(),
                            textRange.start().lineOffset() + 1,
                            textRange.end().line(),
                            textRange.end().lineOffset());

            issue.flows().forEach(flow -> {
                IssueLocation secondaryLocation = flow.locations().get(0);
                TextRange secondaryRange = secondaryLocation.textRange();
                actualIssue.addSecondary(
                        secondaryRange.start().line(),
                        secondaryRange.start().lineOffset() + 1,
                        secondaryRange.end().line(),
                        secondaryRange.end().lineOffset(),
                        secondaryLocation.message());
            });
        });

        fileVerifier.assertOneOrMoreIssues();
    }

//    private static void addComments(SingleFileVerifier fileVerifier, Node node) {
//        if (node.getNodeType() == Node.COMMENT_NODE) {
//            Comment comment = (Comment) node;
//            XmlTextRange range = XmlFile.nodeLocation(node);
//            fileVerifier.addComment(range.getStartLine(), range.getStartColumn() + "<!--".length() + 1, comment.getNodeValue(), 0, 0);
//        }
//
//        XmlFile.children(node).forEach(child -> addComments(fileVerifier, child));
//    }

    private void checkIssueOnFile(String expectedIssueMessage, int... secondaryLines) {
        assertThat(issues).hasSize(1);
        Issue issue = issues.iterator().next();
        assertThat(issue.primaryLocation().message()).isEqualTo(expectedIssueMessage);
        assertThat(issue.primaryLocation().textRange()).isNull();

        List<Issue.Flow> flows = issue.flows();
        // secondaries are N flows of size 1
        assertThat(flows).hasSize(secondaryLines.length);
        assertThat(flows.stream().map(Issue.Flow::locations).collect(Collectors.toList())).allMatch(flow -> flow.size() == 1);

        // only contains lines
        Integer[] expectedLines = IntStream.of(secondaryLines).boxed().toArray(Integer[]::new);
        assertThat(flows.stream().map(Issue.Flow::locations).map(locs -> locs.get(0).textRange().start().line()).collect(Collectors.toList()))
                .containsExactly(expectedLines);
    }

    private void checkNoIssues() {
        assertThat(issues).isEmpty();
    }


}
