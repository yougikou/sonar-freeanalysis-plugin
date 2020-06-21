package cn.giko.freeanalysis.rules.base;

import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.TextRange;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.issue.NewIssue;
import org.sonar.api.batch.sensor.issue.NewIssueLocation;
import org.sonar.api.rule.RuleKey;

import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.List;

public abstract class AbstractCheck {
    private SensorContext context;
    private InputFile inputFile;
    private RuleKey ruleKey;

    public AbstractCheck() {
    }

    public final void scanFile(SensorContext context, RuleKey ruleKey, TargetFile file) {
        this.context = context;
        this.inputFile = file.getInputFile();
        this.ruleKey = ruleKey;
        this.scanFile(file);
    }

    public final InputFile inputFile() {
        return this.inputFile;
    }

    public final RuleKey ruleKey() {
        return this.ruleKey;
    }

    public abstract void scanFile(TargetFile targetFile);

    public final void reportIssueOnFile(String message, List<Integer> secondaryLocationLines) {
        NewIssue issue = this.context.newIssue();
        NewIssueLocation location = issue.newLocation().on(this.inputFile).message(message);
        Iterator var5 = secondaryLocationLines.iterator();

        while(var5.hasNext()) {
            Integer line = (Integer)var5.next();
            NewIssueLocation secondary = issue.newLocation().on(this.inputFile).at(this.inputFile.selectLine(line));
            issue.addLocation(secondary);
        }

        issue.at(location).forRule(this.ruleKey).save();
    }

    public final void reportIssue(TextRange textRange, String message, List<AbstractCheck.Secondary> secondaries) {
        NewIssue issue = this.context.newIssue();
        NewIssueLocation location = this.getLocation(textRange, issue).message(message);
        secondaries.forEach((secondary) -> {
            NewIssueLocation secondaryLocation = this.getLocation(secondary.range, issue);
            if (secondary.message != null) {
                secondaryLocation.message(secondary.message);
            }

            issue.addLocation(secondaryLocation);
        });
        issue.at(location).forRule(this.ruleKey).save();
    }

    private NewIssueLocation getLocation(TextRange textRange, NewIssue issue) {
        return issue.newLocation().on(this.inputFile).at(
                this.inputFile.newRange(
                        textRange.start().line(), textRange.start().lineOffset(), textRange.end().line(), textRange.end().lineOffset()));
    }

    public static class Secondary {
        final TextRange range;
        @Nullable
        final String message;

        public Secondary(TextRange range, @Nullable String message) {
            this.range = range;
            this.message = message;
        }
    }
}
