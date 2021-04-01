import pt.up.fe.specs.util.SpecsIo;

import java.util.Arrays;

public class Main {
    public static void main(String[] args) {
        System.out.println("Executing with args: " + Arrays.toString(args));
//        if (args[0].contains("fail")) {
//            throw new RuntimeException("It's supposed to fail");
//        }
        var fileContents = SpecsIo.read("./test.txt");
        var parserResult = new SyntacticPhase().parse(fileContents);

        var semanticResult = new AnalysisStage().semanticAnalysis(parserResult);
    }
}
