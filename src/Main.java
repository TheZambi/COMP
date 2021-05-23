import analysis.AnalysisStage;
import generation.ollir.OptimizationStage;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.ReportType;
import pt.up.fe.comp.jmm.report.Stage;
import pt.up.fe.specs.util.SpecsIo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        try {  // TODO: REMOVE THIS BECAUSE INTELLIJ IS BAD :(
            System.out.println("Executing with args: " + Arrays.toString(args));

            var fileContents = SpecsIo.read("./test.txt");
            var parserResult = new SyntacticPhase().parse(fileContents);
            checkReports("PARSER", Stage.SYNTATIC, parserResult.getReports());

            var semanticResult = new AnalysisStage().semanticAnalysis(parserResult);
            checkReports("SEMANTIC", Stage.SEMANTIC, semanticResult.getReports());

            var ollirResult = new OptimizationStage().toOllir(semanticResult);
            checkReports("OLLIR", Stage.LLIR, ollirResult.getReports());

            var jasminResult = new generation.jasmin.BackendStage().toJasmin(ollirResult);
            checkReports("JASMIN", Stage.GENERATION, jasminResult.getReports());

            List<String> jasminArgs = new ArrayList();
            List<String> jasminClasspath = new ArrayList();
            jasminClasspath.add("compiled");
            jasminResult.run(jasminArgs, jasminClasspath);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void checkReports(String phase, Stage stage, List<Report> reports) {
        System.out.println("<" + "-".repeat(10) + " " + phase + " " + "-".repeat(10) + ">");
        int errors = 0, warnings = 0;
        for (Report r: reports) {
            if (r.getStage() == stage) {
                switch (r.getType()) {
                    case ERROR:
                        errors += 1; break;
                    case WARNING:
                        warnings += 1; break;
                    default: break;
                }
                System.out.println(r);
            }
        }
        if (errors > 0) {
            System.err.println(phase + " PHASE WITH " + errors + " ERROR(S)");
            System.err.println(phase + " PHASE WITH " + warnings + " WARNING(S)");
            throw new RuntimeException("Found " + errors + " error reports");
        } else if (warnings > 0) {
            System.out.println(phase + " PHASE WITH " + warnings + " WARNING(S)");
        } else {
            System.out.println(phase + " PHASE OK");
        }
        System.out.println();
    }
}
