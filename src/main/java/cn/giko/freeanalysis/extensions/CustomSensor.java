package cn.giko.freeanalysis.extensions;

import cn.giko.freeanalysis.FreeAnalysisPlugin;
import cn.giko.freeanalysis.extensions.base.FreeAnalysisRulesDefinition;
import cn.giko.freeanalysis.extensions.base.FreeAnalysisSensor;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.rule.CheckFactory;
import org.sonar.api.batch.rule.Severity;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.issue.NewIssue;
import org.sonar.api.batch.sensor.issue.NewIssueLocation;
import org.sonar.api.measures.FileLinesContextFactory;
import org.sonar.api.rule.RuleKey;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;

public class CustomSensor extends FreeAnalysisSensor {

    /** Here is example for inspection result from intellij **/
    public static final RuleKey RULE_KEY_INSPECTION = RuleKey.of(FreeAnalysisRulesDefinition.FREE_ANALYSIS_REPO, "Inspection");

    private static final Logger logger = Loggers.get(CustomSensor.class);

    /**
     * constructor
     *
     * @param fileSystem              file system object
     * @param checkFactory            check factory
     * @param fileLinesContextFactory file lines context factory
     */
    public CustomSensor(FileSystem fileSystem, CheckFactory checkFactory, FileLinesContextFactory fileLinesContextFactory) {
        super(fileSystem, checkFactory, fileLinesContextFactory);
    }

    @Override
    protected void preProcess(SensorContext context) {
        String inspectionPathStr = context.config().get(FreeAnalysisPlugin.FILE_INSPECTION_PATH_KEY).get();
        if (inspectionPathStr == null || inspectionPathStr.length() == 0) {
            logger.warn("Pre-process is implemented but inspection path is not set!");
            return;
        }

        try {
            File dir = new File(inspectionPathStr);
            if (dir.exists() && dir.isDirectory()) {
                File[] resultFiles = dir.listFiles();
                for (File resultFile : resultFiles) {
                    // Create issues based on result files
                    logger.debug("Create issues in result file: {}", resultFile.getName());
                    workWithResultFile(context, resultFile);
                }
            } else {
                logger.warn("Inspection results folder: [{}] not exists!!", inspectionPathStr);
            }
        } catch (Exception e) {
            logger.error("Inspection result import failed: {}", e.getMessage());
        }
    }

    private void workWithResultFile(SensorContext context, File file) {
        logger.debug("Working with {}", file.getName());
        try {
            // Parse xml file to document object
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(file);

            // Read issue node
            NodeList fileNodes = doc.getElementsByTagName("file");
            NodeList lineNodes = doc.getElementsByTagName("line");
            NodeList descNodes = doc.getElementsByTagName("description");
            NodeList problemClass = doc.getElementsByTagName("problem_class");

            for (int i = 0; i < fileNodes.getLength(); i++) {
                String severity = problemClass.item(i).getAttributes().getNamedItem("severity").getTextContent();
                Node fileItem = fileNodes.item(i);

                if (fileItem == null) continue;

                String fName = getFileName(fileItem);
                int lineNumber = Integer.parseInt(lineNodes.item(i).getTextContent());
                String desc = descNodes.item(i).getTextContent();
                desc = sanitizeDescription(desc);
                logger.debug("{}:{} {} {}", fName, lineNumber, severity, desc);

                File targetFile = new File(fName);
                InputFile processedFile = context.fileSystem().inputFile(context.fileSystem().predicates().is(targetFile));

                if (processedFile == null) continue;

                createIssue(context, processedFile, lineNumber, severity, desc);
            }
        } catch (Exception e) {
            logger.error("Error with {} : {}", file.getName(), e.getMessage());
        }
    }

    private String sanitizeDescription(String desc) {
        desc=desc.replaceAll("\\<.{0,1}code\\>","");
        return desc;
    }

    private String getFileName(Node fileItem) {
        String textContent = fileItem.getTextContent();
        return textContent.replace("file://$PROJECT_DIR$", System.getProperty("user.dir"));
    }

    private void createIssue(SensorContext context, InputFile processedFile, int lineNumber, String severity, String desc) {
        Severity sonarSeverity = mapToSeverity(severity);
        RuleKey ruleType = RULE_KEY_INSPECTION;
        String filename = processedFile.filename();
        logger.debug("{} {}", filename, ruleType);

        NewIssue newIssue = context.<String>newIssue().forRule(ruleType).overrideSeverity(sonarSeverity);
        newIssue.gap(2.0);

        NewIssueLocation primaryLocation = newIssue.newLocation()
                .on(processedFile)
                .at(processedFile.selectLine(lineNumber))
                .message(desc);
        primaryLocation.message(desc);
        newIssue.at(primaryLocation);
        newIssue.save();
    }

    private Severity mapToSeverity(String severity) {
        switch (severity) {
            case "ERROR":
                return Severity.MAJOR;
            case "WARNING":
            case "WEAK_WARNING":
            case "TYPO":
                return Severity.MINOR;
            case "INFO":
                return Severity.INFO;
            default:
                return Severity.BLOCKER;
        }
    }
}
