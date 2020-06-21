package cn.giko.freeanalysis.extensions.base;

import cn.giko.freeanalysis.LineCounter;
import cn.giko.freeanalysis.rules.CheckList;
import cn.giko.freeanalysis.rules.base.AbstractCheck;
import cn.giko.freeanalysis.rules.base.TargetFile;
import org.sonar.api.SonarProduct;
import org.sonar.api.batch.fs.FilePredicate;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.rule.CheckFactory;
import org.sonar.api.batch.rule.Checks;
import org.sonar.api.batch.sensor.Sensor;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.internal.google.common.annotations.VisibleForTesting;
import org.sonar.api.measures.FileLinesContextFactory;
import org.sonar.api.rule.RuleKey;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonarsource.analyzer.commons.ProgressReport;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class FreeAnalysisSensor implements Sensor {
    private final Checks<Object> checks;
    private final FileSystem fileSystem;
    private final FilePredicate mainFilesPredicate;
    private final FileLinesContextFactory fileLinesContextFactory;

    private static final Logger logger = Loggers.get(FreeAnalysisSensor.class);

    public static final RuleKey RULE_KEY_EXTERNAL = RuleKey.of(FreeAnalysisRulesDefinition.FREE_ANALYSIS_REPO, "ExternalCheck");

    /**
     * constructor
     *
     * @param fileSystem file system object
     * @param checkFactory check factory
     * @param fileLinesContextFactory file lines context factory
     */
    public FreeAnalysisSensor(FileSystem fileSystem, CheckFactory checkFactory, FileLinesContextFactory fileLinesContextFactory) {
        this.fileLinesContextFactory = fileLinesContextFactory;
        this.checks = checkFactory.create(FreeAnalysisRulesDefinition.FREE_ANALYSIS_REPO).addAnnotatedChecks((Iterable<?>) CheckList.getCheckClasses());;
        this.fileSystem = fileSystem;
        this.mainFilesPredicate = fileSystem.predicates().and(
                fileSystem.predicates().hasType(InputFile.Type.MAIN),
                fileSystem.predicates().hasLanguage(FreeAnalysisLanguage.KEY));
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }

    @Override
    public void describe(SensorDescriptor descriptor) {
        descriptor
                .onlyOnLanguage(FreeAnalysisLanguage.KEY)
                .name("FreeAnalysis Sensor");
    }

    protected void preProcess(SensorContext context) {
        logger.debug("Override preProcess to add custom process before analysis.");
    }

    /**
     * execute sensor
     *
     * @param context context object
     */
    @Override
    public void execute(SensorContext context) {
        boolean isSonarLintContext = context.runtime().getProduct() == SonarProduct.SONARLINT;

        // only run info log and pre-process when bulk analysis
        if (!isSonarLintContext) {
            logger.info("{} is running on baseDir: {}",
                    this.getClass().getName(), this.fileSystem.baseDir().getAbsolutePath());
            preProcess(context);
        }

        List<InputFile> inputFiles = new ArrayList<>();
        fileSystem.inputFiles(mainFilesPredicate).forEach(inputFiles::add);

        if (inputFiles.isEmpty()) {
            return;
        }

        ProgressReport progressReport = new ProgressReport("Report about progress of free analyzer", TimeUnit.SECONDS.toMillis(10));
        progressReport.start(inputFiles.stream().map(InputFile::toString).collect(Collectors.toList()));

        boolean cancelled = false;
        try {
            for (InputFile inputFile : inputFiles) {
                if (context.isCancelled()) {
                    cancelled = true;
                    break;
                }
                scanFile(context, inputFile, isSonarLintContext);
                progressReport.nextFile();
            }
        } finally {
            if (!cancelled) {
                progressReport.stop();
            } else {
                progressReport.cancel();
            }
        }
    }

    private void scanFile(SensorContext context, InputFile inputFile, boolean isSonarLintContext) {
        try {
            TargetFile targetFile = TargetFile.create(inputFile);
            if (!isSonarLintContext) {
                LineCounter.analyse(context, fileLinesContextFactory, targetFile);
                // todo confirm why need highlighting
//                XmlHighlighting.highlight(context, targetFile);
            }
            runChecks(context, targetFile);
        } catch (Exception e) {
            logger.error(String.format("Unable to analyse file %s;", inputFile.uri()));
            logger.debug("Cause: {}", e.getMessage());
        }
    }

    private void runChecks(SensorContext context, TargetFile newTargetFile) {
        checks.all().stream()
                .map(AbstractCheck.class::cast)
                // checks.ruleKey(check) is never null because "check" is part of "checks.all()"
                .forEach(check -> runCheck(context, check, checks.ruleKey(check), newTargetFile));
    }

    @VisibleForTesting
    void runCheck(SensorContext context, AbstractCheck check, RuleKey ruleKey, TargetFile newTargetFile) {
        try {
            check.scanFile(context, ruleKey, newTargetFile);
        } catch (Exception e) {
            logFailingRule(ruleKey, newTargetFile.getInputFile().uri(), e);
        }
    }

    private static void logFailingRule(RuleKey rule, URI fileLocation, Exception e) {
        logger.error(String.format("Unable to execute rule %s on %s", rule, fileLocation), e);
    }
}
