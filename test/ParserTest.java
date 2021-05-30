import org.junit.Test;

import pt.up.fe.comp.TestUtils;
import pt.up.fe.comp.jmm.JmmParserResult;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.specs.util.SpecsIo;

import java.util.List;

public class ParserTest {

    private void testParse(String filename) {
        System.out.println("TEST " + filename);

        JmmParserResult parserResult = TestUtils.parse(SpecsIo.getResource(filename));
        printReports(parserResult.getReports());
        TestUtils.noErrors(parserResult.getReports());
    }

    private void testParseFail(String filename) {
        System.out.println("FAIl " + filename);

        JmmParserResult parserResult = TestUtils.parse(SpecsIo.getResource(filename));
        printReports(parserResult.getReports());
        TestUtils.mustFail(parserResult.getReports());
    }

    private static void printReports(List<Report> reports) {
        if (reports.size() == 0)
            System.out.println("No errors found");
        for (Report r: reports) {
            System.out.println(r);
        }
    }

    @Test
    public void testWhileAndIF() {
        testParse("fixtures/public/WhileAndIF.jmm");
    }

    @Test
    public void testFindMaximum() {
        testParse("fixtures/public/FindMaximum.jmm");
    }

    @Test
    public void testHelloWorld() {
        testParse("fixtures/public/HelloWorld.jmm");
    }

    @Test
    public void testLazysort() {
        testParse("fixtures/public/Lazysort.jmm");
    }

    @Test
    public void testLife() {
        testParse("fixtures/public/Life.jmm");
    }

    @Test
    public void testMonteCarloPi() {
        testParse("fixtures/public/MonteCarloPi.jmm");
    }

    @Test
    public void testQuickSort() {
        testParse("fixtures/public/QuickSort.jmm");
    }

    @Test
    public void testSimple() {
        testParse("fixtures/public/Simple.jmm");
    }

    @Test
    public void testTicTacToe() {
        testParse("fixtures/public/TicTacToe.jmm");
    }


    // CUSTOM TESTS
    @Test
    public void testOverloading() {
        testParse("fixtures/custom/Overloading.jmm");
    }

    @Test
    public void testOverloadingWithIncludes() {
        testParse("fixtures/custom/OverloadingWithIncludes.jmm");
    }

    @Test
    public void testBlackJack() {
        testParse("fixtures/custom/TestBlackJack.jmm");
    }

    @Test
    public void testBubbleSort() {
        testParse("fixtures/custom/TestBubbleSort.jmm");
    }

    @Test
    public void testBinarySearch() {
        testParse("fixtures/custom/BinarySearch.jmm");
    }

    @Test
    public void testUninitializedVar() {
        testParse("fixtures/custom/TestUninitializedVars.jmm");
    }

    // MUST FAIL TESTS FROM HERE ON OUT
    @Test
    public void failBlowUpTest() {
        testParseFail("fixtures/public/fail/syntactical/BlowUp.jmm");
    }

    @Test
    public void failCompleteWhileTest() {
        testParseFail("fixtures/public/fail/syntactical/CompleteWhileTest.jmm");
    }

    @Test
    public void failLengthError() {
        testParseFail("fixtures/public/fail/syntactical/LengthError.jmm");
    }

    @Test
    public void failMissingRightPar() {
        testParseFail("fixtures/public/fail/syntactical/MissingRightPar.jmm");
    }

    @Test
    public void failMultipleSequential() {
        testParseFail("fixtures/public/fail/syntactical/MultipleSequential.jmm");
    }

    @Test
    public void failNestedLoopTest() {
        testParseFail("fixtures/public/fail/syntactical/NestedLoop.jmm");
    }
}
