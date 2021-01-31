package bo.tc.tcplanner.app;

import org.optaplanner.benchmark.api.PlannerBenchmark;
import org.optaplanner.benchmark.api.PlannerBenchmarkFactory;

import java.io.IOException;

import static bo.tc.tcplanner.PropertyConstants.initializeFiles;
import static bo.tc.tcplanner.PropertyConstants.setConstants;


public class Benchmark {

    public static void main(String[] args) throws IOException {
        setConstants();
        initializeFiles();
        PlannerBenchmarkFactory plannerBenchmarkFactory = PlannerBenchmarkFactory.createFromFreemarkerXmlResource(
                "tcplannercoreBenchmarkConfig2.xml.ftl");
//        BenchmarkAggregatorFrame.createAndDisplay(plannerBenchmarkFactory);
        PlannerBenchmark plannerBenchmark = plannerBenchmarkFactory.buildPlannerBenchmark();
        plannerBenchmark.benchmarkAndShowReportInBrowser();
    }
}
