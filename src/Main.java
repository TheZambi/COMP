import analysis.AnalysisStage;
import com.sun.tools.jconsole.JConsoleContext;
import generation.jasmin.BackendStage;
import generation.ollir.OptimizationStage;
import pt.up.fe.comp.jmm.ollir.JmmOptimization;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.ReportType;
import pt.up.fe.comp.jmm.report.Stage;
import pt.up.fe.specs.util.SpecsIo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Main {
    public static void main(String[] args) {

        System.out.println("Executing with args: " + Arrays.toString(args));

        int fileIndex = 0;
        if (args.length > 1 && args[0].startsWith("-r="))
            fileIndex++;

        boolean optimize = Arrays.asList(args).contains("-o");
        if (optimize)
            fileIndex++;

        String filename = null;
        if (fileIndex < args.length)
            filename = args[fileIndex];

        if (filename == null) {
            printHelp();
            return;
        }

        var fileContents = SpecsIo.read(filename);
        var parserResult = new SyntacticPhase().parse(fileContents);
        checkReports("PARSER", Stage.SYNTATIC, parserResult.getReports());

        var semanticResult = new AnalysisStage().semanticAnalysis(parserResult);
        checkReports("SEMANTIC", Stage.SEMANTIC, semanticResult.getReports());

        JmmOptimization optimization = new OptimizationStage();
        if (optimize)
            semanticResult = optimization.optimize(semanticResult);
        OllirResult ollirResult = optimization.toOllir(semanticResult, optimize);
        if (optimize)
            ollirResult = optimization.optimize(ollirResult);

        checkReports("OLLIR", Stage.LLIR, ollirResult.getReports());

        var jasminResult = new BackendStage().toJasmin(ollirResult);
        checkReports("JASMIN", Stage.GENERATION, jasminResult.getReports());

//        List<String> jasminArgs = new ArrayList();
//        List<String> jasminClasspath = new ArrayList();
//        jasminClasspath.add("compiled");
//        jasminResult.run(jasminArgs, jasminClasspath);
    }

    public static void printHelp() {
        System.out.println("java Main [-r=<num>] [-o] <input_file.jmm>");
        System.out.println("   or");
        System.out.println("java -jar COMP2021-1D.jar [-r=<num>] [-o] <input_file.jmm>");
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
