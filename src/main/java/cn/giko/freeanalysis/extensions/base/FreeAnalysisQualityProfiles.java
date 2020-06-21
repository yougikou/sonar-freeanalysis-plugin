package cn.giko.freeanalysis.extensions.base;

import org.sonar.api.SonarRuntime;
import org.sonar.api.server.profile.BuiltInQualityProfilesDefinition;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonarsource.analyzer.commons.BuiltInQualityProfileJsonLoader;

public class FreeAnalysisQualityProfiles implements BuiltInQualityProfilesDefinition {
  private final SonarRuntime sonarRuntime;
  private static final Logger logger = Loggers.get(FreeAnalysisQualityProfiles.class);

  public static final String FREE_ANALYSIS_WAY_PROFILE_NAME = "FreeAnalysis way";
  public static final String FREE_ANALYSIS_RESOURCE_PATH = "cn/giko/freeanalysis/rules";
  public static final String FREE_ANALYSIS_WAY_PATH = "cn/giko/freeanalysis/rules/Freeanalysis_way_profile.json";

  /**
   * constructor
   *
   * @param sonarRuntime runtime environment
   */
  public FreeAnalysisQualityProfiles(SonarRuntime sonarRuntime) {
    this.sonarRuntime = sonarRuntime;
  }

  /**
   * define quality profiles
   *
   * @param context context object
   */
  public void define(Context context) {
    NewBuiltInQualityProfile freeWay = context.createBuiltInQualityProfile(FREE_ANALYSIS_WAY_PROFILE_NAME, FreeAnalysisLanguage.KEY);
    BuiltInQualityProfileJsonLoader.load(freeWay, FreeAnalysisRulesDefinition.FREE_ANALYSIS_REPO, FREE_ANALYSIS_WAY_PATH, FREE_ANALYSIS_RESOURCE_PATH, sonarRuntime);
    activateExternalRule(freeWay);
    freeWay.done();
  }

  /**
   * Override this method if need activate rule for external analysis result
   *
   * Example:
   * NewBuiltInActiveRule rule = myWay.activateRule(FreeAnalysisRulesDefinition.FREE_ANALYSIS_REPO, FreeAnalysisSensor.RULE_KEY_EXTERNAL.rule());
   * rule.overrideSeverity(Severity.BLOCKER.name());
   *
   * @param freeWay quality profile object
   */
  protected void activateExternalRule(NewBuiltInQualityProfile freeWay){
    logger.debug("Override activateExternalRule to activate custom rule.");
  }
}
