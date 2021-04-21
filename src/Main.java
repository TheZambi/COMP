import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.ReportType;
import pt.up.fe.specs.util.SpecsIo;

import java.util.Arrays;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        try {  // TODO: REMOVE THIS BECAUSE INTELLIJ IS BAD :(
            System.out.println("Executing with args: " + Arrays.toString(args));
//        if (args[0].contains("fail")) {
//            throw new RuntimeException("It's supposed to fail");
//        }
            var fileContents = SpecsIo.read("./test.txt");
            var parserResult = new SyntacticPhase().parse(fileContents);
            checkReports("parser", parserResult.getReports());

            var semanticResult = new AnalysisStage().semanticAnalysis(parserResult);
            checkReports("semantic", semanticResult.getReports());

//            var ollirResult = new OptimizationStage().toOllir(semanticResult);
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
