package cn.giko.freeanalysis.extensions;

import cn.giko.freeanalysis.extensions.base.FreeAnalysisRulesDefinition;
import org.sonar.api.SonarRuntime;
import org.sonar.api.rule.Severity;

public class CustomRulesDefinition extends FreeAnalysisRulesDefinition {

    /**
     * constructor
     *
     * @param sonarRuntime runtime environment
     */
    public CustomRulesDefinition(SonarRuntime sonarRuntime) {
        super(sonarRuntime);
    }

    /**
     * Here is example for inspection result from intellij
     *
     * @param repository repository object
     */
    @Override
    protected void addExternalRule(NewRepository repository) {
        NewRule rule = repository.createRule(CustomSensor.RULE_KEY_INSPECTION.rule());
        rule.setSeverity(Severity.BLOCKER);
        rule.setName("Inspection Problem");
        rule.setHtmlDescription("Inspection problems found by Intellij code inspect");
    }
}
