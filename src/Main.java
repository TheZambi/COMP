import pt.up.fe.specs.util.SpecsIo;

import java.util.Arrays;

public class Main {
    public static void main(String[] args) {
        try {  // TODO: REMOVE THIS BECAUSE INTELLIJ IS BAD :(
            System.out.println("Executing with args: " + Arrays.toString(args));
//        if (args[0].contains("fail")) {
//            throw new RuntimeException("It's supposed to fail");
//        }
            var fileContents = SpecsIo.read("./test.txt");
            var parserResult = new SyntacticPhase().parse(fileContents);
            if (parserResult.getReports().size() > 0) {
                System.err.println("Failed on parse phase");
                return;
            }
            var semanticResult = new AnalysisStage().semanticAnalysis(parserResult);
            if (semanticResult.getReports().size() > 0) {
                System.err.println("Failed on semantic phase");
                return;
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
