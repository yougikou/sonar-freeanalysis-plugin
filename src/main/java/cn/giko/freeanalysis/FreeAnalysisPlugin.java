package cn.giko.freeanalysis;

import cn.giko.freeanalysis.extensions.CustomQualityProfiles;
import cn.giko.freeanalysis.extensions.CustomRulesDefinition;
import cn.giko.freeanalysis.extensions.CustomSensor;
import cn.giko.freeanalysis.extensions.base.FreeAnalysisLanguage;
import cn.giko.freeanalysis.extensions.base.FreeAnalysisQualityProfiles;
import cn.giko.freeanalysis.extensions.base.FreeAnalysisSensor;
import cn.giko.freeanalysis.extensions.base.FreeAnalysisRulesDefinition;
import org.sonar.api.Plugin;
import org.sonar.api.config.PropertyDefinition;
import org.sonar.api.resources.Qualifiers;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;


/**
 * This class is the entry point for all extensions. It is referenced in pom.xml.
 *
 * Implementation pattern:
 * 1. Only add custom rules
 *  - add check in rule package
 *
 * 2. Beside #1, add custom rule key for external result as well(But use same repository)
 *  - implement analysis sensor to add rule key
 *  - implement quality profiles to add rules to repository
 *  - implement rule definition to activate rules
 *  - implement analysis sensor to add pre-process(import external result)
 *
 * 3. Instead use existing repository, create new repository for external result
 *  - create new sensor class to use new repository
 *  - others similar to #2
 */
public class FreeAnalysisPlugin implements Plugin {

    public static final String FILE_SUFFIXES_KEY = "sonar.freeanalysis.file.suffixes";
    public static final String FILE_INSPECTION_PATH_KEY = "sonar.freeanalysis.inspection.path";

    private static final Logger LOGGER = Loggers.get(FreeAnalysisPlugin.class);

    /**
     * Add extensions to plugin
     *
     * Extend base class, created custom extensions
     * Modify below code to replace to custom extensions
     *
     * @param context
     */
    public void define(Context context) {
        context.addExtension(
                PropertyDefinition.builder(FreeAnalysisPlugin.FILE_SUFFIXES_KEY)
                        .name("File Suffixes")
                        .description("Comma-separated list of suffixes for files to analyze.")
                        .defaultValue(String.join(",", FreeAnalysisLanguage.DEFAULT_SUFFIXES))
                        .multiValues(true)
                        .category("FreeAnalysis")
                        .onQualifiers(Qualifiers.PROJECT)
                        .build());

        // Example for custom analysis parameters
        context.addExtension(
                PropertyDefinition.builder(FreeAnalysisPlugin.FILE_INSPECTION_PATH_KEY)
                        .name("Inspection result folder path")
                        .description("Path to inspection result files' folder.")
                        .multiValues(false)
                        .category("FreeAnalysis")
                        .onQualifiers(Qualifiers.PROJECT)
                        .build());

        context.addExtension(FreeAnalysisLanguage.class);
        LOGGER.info("Extension {} added.", FreeAnalysisLanguage.class.getSimpleName());
        context.addExtension(CustomQualityProfiles.class);
        LOGGER.info("Extension {} added.", CustomQualityProfiles.class.getSimpleName());
        context.addExtension(CustomRulesDefinition.class);
        LOGGER.info("Extension {} added.", CustomRulesDefinition.class.getSimpleName());
        context.addExtension(CustomSensor.class);
        LOGGER.info("Extension {} added.", CustomSensor.class.getSimpleName());
    }
}
