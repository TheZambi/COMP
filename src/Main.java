import analysis.AnalysisStage;
import generation.ollir.OptimizationStage;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.ReportType;
import pt.up.fe.specs.util.SpecsIo;

import java.util.Arrays;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        try {  // TODO: REMOVE THIS BECAUSE INTELLIJ IS BAD :(
            System.out.println("Executing with args: " + Arrays.toString(args));

            var fileContents = SpecsIo.read("./test.txt");
            var parserResult = new SyntacticPhase().parse(fileContents);
            checkReports("parser", parserResult.getReports());
            System.out.println("SEMANTIC");
            var semanticResult = new AnalysisStage().semanticAnalysis(parserResult);
            checkReports("semantic", semanticResult.getReports());
            System.out.println(semanticResult.getReports());

            System.out.println("OLLIR");
            var ollirResult = new OptimizationStage().toOllir(semanticResult);
            if(ollirResult.getReports().size()> 0){
                System.err.println("Failed on ollir generation");
                return;
            }
            System.out.println(ollirResult.getReports());
            System.out.println("JASMIN");
            var jasminResult = new generation.jasmin.BackendStage().toJasmin(ollirResult);
            jasminResult.run();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void checkReports(String phase, List<Report> reports) {
        int errors = 0;
        for (Report r: reports) {
            if (r.getType().equals(ReportType.ERROR))
                errors += 1;
            System.out.println(r);
        }
        if (errors > 0) {
            System.err.println("Failed on " + phase + " phase");
            throw new RuntimeException("Error reports found");
        }
    }
}
