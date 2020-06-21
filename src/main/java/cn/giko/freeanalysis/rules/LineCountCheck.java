package cn.giko.freeanalysis.rules;

import cn.giko.freeanalysis.rules.base.TargetFile;
import cn.giko.freeanalysis.rules.base.AbstractCheck;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;

import java.util.Collections;

@Rule(key = LineCountCheck.RULE_KEY)
public class LineCountCheck extends AbstractCheck {

    public static final String RULE_KEY = "LineCountCheck";
    private static final String MESSAGE = "This source file's line count is over specified limitation: %s actual: %s";

    @RuleProperty(
            key = "maxLinesCount",
            description = "Maximum line count.",
            defaultValue = "300",
            type = "INTEGER")
    private int maxLinesCount = 300;

    @Override
    public void scanFile(TargetFile targetFile) {
        if (targetFile.getLines().size() > maxLinesCount){
            reportIssueOnFile(String.format(MESSAGE, maxLinesCount, targetFile.getLines().size()), Collections.emptyList());
        }
    }
}
