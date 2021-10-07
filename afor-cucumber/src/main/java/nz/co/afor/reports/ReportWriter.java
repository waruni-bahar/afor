package nz.co.afor.reports;

import io.cucumber.core.gherkin.messages.FeatureMapping;
import io.cucumber.messages.internal.com.google.common.escape.Escaper;
import io.cucumber.messages.internal.com.google.common.html.HtmlEscapers;
import io.cucumber.plugin.event.*;
import nz.co.afor.reports.charts.PieChart;
import nz.co.afor.reports.charts.PlotChart;
import nz.co.afor.reports.javascript.JavascriptResults;
import nz.co.afor.reports.results.FeatureSummaryResult;
import nz.co.afor.reports.results.ResultSummary;
import nz.co.afor.reports.results.ScenarioTimelineResult;

import java.io.*;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.lang.String.format;

public class ReportWriter implements ReportContext, ReportDurationFormatter {

    private final HtmlWriter htmlWriter;
    private boolean isInitialised = false;
    private int features = 0;
    private int attachments = 0;
    private StringBuilder stepBuffer = new StringBuilder();
    private StringBuilder scenarioBuffer = new StringBuilder();
    private FeatureMapping featureMapping;
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final Escaper ESCAPER = HtmlEscapers.htmlEscaper();
    private final ResultSummary resultSummary = new ResultSummary();
    private final List<ScenarioTimelineResult> scenarioTimelineResults = new ArrayList<>();
    private final List<FeatureSummaryResult> featureSummaryResults = new ArrayList<>();

    public ReportWriter(HtmlWriter htmlWriter) {
        this.htmlWriter = htmlWriter;
    }

    public void writeScenario() {
        initialise();
        stepBuffer = new StringBuilder();
    }

    public void writeStep(TestStepFinished testStepFinished) {
        if (PickleStepTestStep.class.isAssignableFrom(testStepFinished.getTestStep().getClass())) {
            Step step = ((PickleStepTestStep) testStepFinished.getTestStep()).getStep();
            ZonedDateTime startTime = testStepFinished.getInstant().atZone(getTimezone()).minusSeconds(testStepFinished.getResult().getDuration().toSeconds());
            stepBuffer.append("<li class=\"step ")
                    .append(testStepFinished.getResult().getStatus().toString().toLowerCase())
                    .append("\"><span class=\"keyword\">").append(step.getKeyword().trim())
                    .append(":</span><span class=\"name\">").append(ESCAPER.escape(step.getText())).append("</span>")
                    .append("<span class=\"duration\">").append(formatDuration(testStepFinished.getResult().getDuration().toMillis())).append("</span>")
                    .append("<span class=\"startTime\">").append(TIME_FORMAT.format(startTime)).append("</span>");
            if (null != testStepFinished.getResult().getError() && null != testStepFinished.getResult().getError().getStackTrace()) {
                stepBuffer.append("<pre class=\"error\">")
                        .append(ESCAPER.escape(testStepFinished.getResult().getError().toString())).append("\n");
                for (StackTraceElement line : testStepFinished.getResult().getError().getStackTrace()) {
                    stepBuffer.append("\tat ").append(ESCAPER.escape(line.toString())).append("\n");
                }
                stepBuffer.append("</pre>");
            }
            stepBuffer.append("</li>\n");
            resultSummary.getSteps().addResult(testStepFinished.getResult().getStatus());
        }
    }

    public void writeFeature(FeatureMapping featureMapping) {
        initialise();
        if (null == this.featureMapping) {
            setFeatureMapping(featureMapping);
        } else if (!(featureMapping.getHash() == this.featureMapping.getHash()) && !featureMapping.isWritten()) {
            writeFeatureContent(this.featureMapping);
            setFeatureMapping(featureMapping);
        }
    }

    private void writeFeatureContent(FeatureMapping featureMapping) {
        htmlWriter.write(format("<section class=\"blockelement feature\">" +
                "<a class=\"anchor\" name=\"feature-%s\"></a>" +
                "<details %s>" +
                "<summary class=\"header\">" +
                "<span class=\"keyword\" itemprop=\"keyword\">%s:</span>" +
                "<span itemprop=\"name\" class=\"name\">%s</span>" +
                "</summary>" +
                "<div itemprop=\"description\" class=\"description\">%s</div>", features, featureMapping.getFeatureStatus().equals(Status.PASSED) ? "closed" : "open", featureMapping.getKeyword().trim(), ESCAPER.escape(featureMapping.getName()), ESCAPER.escape(featureMapping.getDescription())));
        htmlWriter.write(scenarioBuffer.toString());
        htmlWriter.write("</details></section>");
        resultSummary.getFeatures().addResult(featureMapping.getFeatureStatus());
        featureMapping.setWritten(true);
        features++;
        scenarioBuffer = new StringBuilder();
    }

    public void writeScenario(TestCaseFinished testCaseFinished) {
        ZonedDateTime startTime = testCaseFinished.getInstant().atZone(getTimezone()).minusSeconds(testCaseFinished.getResult().getDuration().toSeconds());
        scenarioBuffer.append("<section class=\"blockelement scenario ")
                .append(testCaseFinished.getResult().getStatus().name().toLowerCase())
                .append("\"><details ")
                .append(testCaseFinished.getResult().getStatus().equals(Status.PASSED) ? "closed" : "open")
                .append("><summary class=\"header\">");
        if (testCaseFinished.getTestCase().getTags().size() > 0) {
            scenarioBuffer.append("<div class=\"tags\">");
            testCaseFinished.getTestCase().getTags().forEach(tag-> scenarioBuffer.append("<span class=\"tag\">").append(ESCAPER.escape(tag)).append("</span>"));
            scenarioBuffer.append("</div>");
        }
        scenarioBuffer.append("<span class=\"keyword\" itemprop=\"keyword\">")
                .append(testCaseFinished.getTestCase().getKeyword().trim())
                .append(":</span><span class=\"name\">")
                .append(ESCAPER.escape(testCaseFinished.getTestCase().getName()))
                .append("</span><span class=\"duration long\">")
                .append(formatDuration(testCaseFinished.getResult().getDuration().toMillis()))
                .append("</span><span class=\"startTime\">")
                .append(TIME_FORMAT.format(startTime))
                .append("</span></summary>\n")
                .append("<ol class=\"steps\">")
                .append(stepBuffer.toString())
                .append("</ol></details></section>");
        featureMapping.setFeatureStatus(testCaseFinished.getResult().getStatus());
        resultSummary.getScenarios().addResult(testCaseFinished.getResult().getStatus());
        featureSummaryResults.get(features).getScenarioResults().addResult(testCaseFinished.getResult().getStatus());
        scenarioTimelineResults.add(new ScenarioTimelineResult(testCaseFinished.getTestCase().getName(), testCaseFinished.getResult().getStatus(), testCaseFinished.getTestCase().getTestSteps().stream().filter(testStep -> PickleStepTestStep.class.isAssignableFrom(testStep.getClass())).count(), testCaseFinished.getResult().getDuration().toMillis()));
    }

    public void writeAttachment(EmbedEvent embedEvent) {
        htmlWriter.addResource("attachment-" + attachments + "." + embedEvent.getMediaType(), embedEvent.getData());
        if (embedEvent.getMediaType().equalsIgnoreCase("png") || embedEvent.getMediaType().equalsIgnoreCase("jpg") || embedEvent.getMediaType().equalsIgnoreCase("gif"))
            stepBuffer.append("<img src=\"" + "attachment-").append(attachments).append(".").append(embedEvent.getMediaType()).append("\">");
        attachments++;
    }

    private void initialise() {
        if (!isInitialised) {
            htmlWriter.initialise(this);
            isInitialised = true;
        }
    }

    public void close() {
        if (features != 0) {
            writeFeatureContent(this.featureMapping);
        }
        htmlWriter.close();
    }

    public void setFeatureMapping(FeatureMapping featureMapping) {
        this.featureMapping = featureMapping;
        featureSummaryResults.add(new FeatureSummaryResult(ESCAPER.escape(featureMapping.getName()), "feature-" + features));
    }

    public void writeCharts(File path) {
        try {
            PieChart.getChart(resultSummary, new FileOutputStream(path.getAbsolutePath() + "/piechart.svg"));
            PlotChart.getChart(resultSummary, scenarioTimelineResults, new FileOutputStream(path.getAbsolutePath() + "/scatterchart.svg"));
        } catch (IOException e) {
            throw new HtmlWriter.WriterException(e);
        }
    }

    public void writeSummaries(File path) {
        try {
            JavascriptResults.writeHighLevelSummary(resultSummary, new FileOutputStream(path.getAbsolutePath() + "/summaryreport.js"));
            JavascriptResults.writePerformanceSummary(scenarioTimelineResults, formatDuration(getTotalRunTime()), new FileOutputStream(path.getAbsolutePath() + "/performancesummaryreport.js"));
            JavascriptResults.writeFeatureSummary(featureSummaryResults, new FileOutputStream(path.getAbsolutePath() + "/featuresummaryreport.js"));
        } catch (IOException e) {
            throw new HtmlWriter.WriterException(e);
        }
    }
}