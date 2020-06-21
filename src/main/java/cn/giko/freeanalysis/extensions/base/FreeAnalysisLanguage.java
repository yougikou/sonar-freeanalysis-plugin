package cn.giko.freeanalysis.extensions.base;

import cn.giko.freeanalysis.FreeAnalysisPlugin;
import org.sonar.api.config.Configuration;
import org.sonar.api.resources.AbstractLanguage;

import java.util.ArrayList;
import java.util.List;

/**
 * Language definition class
 * Because free analysis, just add required properties
 *
 * Suffix setting can be changed at server side
 * Key is unique identify a language
 *  - use swift because sonarlint support language can't customize
 */
public final class FreeAnalysisLanguage extends AbstractLanguage {
  private final Configuration configuration;

  public static final String KEY = "swift";
  public static final String NAME = "FreeAnalysis";
  public static final String[] DEFAULT_SUFFIXES = {".txt"};

  /**
   * constructor
   *
   * @param configuration analysis configuration
   */
  public FreeAnalysisLanguage(Configuration configuration) {
    super(KEY, NAME);
    this.configuration = configuration;
  }

  /**
   * Get target file suffixes
   *
   * @return array of suffixes
   */
  public String[] getFileSuffixes() {
    String[] suffixes = filterEmptyStrings(configuration.getStringArray(FreeAnalysisPlugin.FILE_SUFFIXES_KEY));
    if (suffixes.length == 0) {
      suffixes = FreeAnalysisLanguage.DEFAULT_SUFFIXES;
    }
    return suffixes;
  }

  private static String[] filterEmptyStrings(String[] stringArray) {
    List<String> nonEmptyStrings = new ArrayList<>();
    for (String string : stringArray) {
      if (!string.trim().isEmpty()) {
        nonEmptyStrings.add(string.trim());
      }
    }
    return nonEmptyStrings.toArray(new String[]{});
  }
}
