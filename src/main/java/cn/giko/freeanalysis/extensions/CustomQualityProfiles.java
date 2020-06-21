package cn.giko.freeanalysis.extensions;

import cn.giko.freeanalysis.extensions.base.FreeAnalysisQualityProfiles;
import cn.giko.freeanalysis.extensions.base.FreeAnalysisRulesDefinition;
import org.sonar.api.SonarRuntime;
import org.sonar.api.rule.Severity;

public class CustomQualityProfiles extends FreeAnalysisQualityProfiles {

    /**
     * constructor
     *
     * @param sonarRuntime runtime environment
     */
    public CustomQualityProfiles(SonarRuntime sonarRuntime) {
        super(sonarRuntime);
    }

    /**
     * Here is example for inspection result from intellij
     *
     * @param freeWay quality profile object
     */
    @Override
    protected void activateExternalRule(NewBuiltInQualityProfile freeWay) {
        NewBuiltInActiveRule rule = freeWay.activateRule(FreeAnalysisRulesDefinition.FREE_ANALYSIS_REPO, CustomSensor.RULE_KEY_INSPECTION.rule());
        rule.overrideSeverity(Severity.BLOCKER);
    }
}
