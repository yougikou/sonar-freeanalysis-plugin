package cn.giko.freeanalysis.extensions.base;

import cn.giko.freeanalysis.extensions.base.FreeAnalysisLanguage;
import cn.giko.freeanalysis.extensions.base.FreeAnalysisQualityProfiles;
import cn.giko.freeanalysis.rules.CheckList;
import org.sonar.api.SonarRuntime;
import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonarsource.analyzer.commons.RuleMetadataLoader;

public class FreeAnalysisRulesDefinition implements RulesDefinition {
    private final SonarRuntime sonarRuntime;
    private static final Logger logger = Loggers.get(FreeAnalysisRulesDefinition.class);

    public static final String FREE_ANALYSIS_REPO = "FreeAnalysis";

    /**
     * constructor
     *
     * @param sonarRuntime runtime environment
     */
    public FreeAnalysisRulesDefinition(SonarRuntime sonarRuntime) {
        this.sonarRuntime = sonarRuntime;
    }

    /**
     * Load and define rules
     *
     * @param context scanner context
     */
    public void define(Context context) {
        defineFreeAnalysisRule(context);
    }

    private void defineFreeAnalysisRule(Context context) {
        NewRepository repository = context.createRepository(FREE_ANALYSIS_REPO, FreeAnalysisLanguage.KEY).setName(FREE_ANALYSIS_REPO);
        RuleMetadataLoader ruleMetadataLoader =
                new RuleMetadataLoader(FreeAnalysisQualityProfiles.FREE_ANALYSIS_RESOURCE_PATH, FreeAnalysisQualityProfiles.FREE_ANALYSIS_WAY_PATH, sonarRuntime);
        ruleMetadataLoader.addRulesByAnnotatedClass(repository, CheckList.getCheckClasses());
        addExternalRule(repository);
        repository.done();
    }

    /**
     * Override this method if need create rule from external analysis result
     * Rule is pre-defined in code or metadata.
     * For external analysis result
     *  - usually define several rule(key) in code and create depends external result.
     *
     * If want to add rules in code.
     * Please define rule key in sensor class, and refer to below example.
     *
     * Example:
     * NewRule rule = repository.createRule(FreeAnalysisSensor.RULE_KEY_EXTERNAL.rule());
     * rule.setSeverity(Severity.BLOCKER);
     * rule.setName("Custom Problem");
     * rule.setHtmlDescription("Custom language problems found by Inspections");
     *
     * @param repository repository object
     */
    protected void addExternalRule(NewRepository repository){
        logger.debug("Override addExternalRule to add custom rules.");
    };
}
