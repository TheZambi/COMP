import static org.junit.Assert.*;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Properties;
import java.io.StringReader;

import pt.up.fe.comp.TestUtils;
import pt.up.fe.comp.jmm.JmmParserResult;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.specs.util.SpecsIo;

public class ParserTest {

    private void testParse(String filename) {
        testParse(filename, false);
    }

    private void testParse(String filename, Boolean print) {
        var fileContents = SpecsIo.read(filename);
        JmmParserResult parserResult = TestUtils.parse(fileContents);

        if (print)
            System.out.println(parserResult.getRootNode().getKind());
    }

    private void testParseFail(String filename) {
        testParseFail(filename, false);
    }

    private void testParseFail(String filename, Boolean print) {
        var fileContents = SpecsIo.read(filename);
        JmmParserResult parserResult = TestUtils.parse(fileContents);

        if (print)
            System.out.println("Total Reports: " + parserResult.getReports().size());

        TestUtils.mustFail(parserResult.getReports());
    }

    @Test
    public void testWhileAndIF() {
        testParse("./test/fixtures/public/WhileAndIF.jmm");
    }

    @Test
    public void testFindMaximum() {
        testParse("./test/fixtures/public/FindMaximum.jmm");
    }

    @Test
    public void testHelloWorld() {
        testParse("./test/fixtures/public/HelloWorld.jmm");
    }

    @Test
    public void testLazysort() {
        testParse("./test/fixtures/public/Lazysort.jmm");
    }

    @Test
    public void testLife() {
        testParse("./test/fixtures/public/Life.jmm");
    }

    @Test
    public void testMonteCarloPi() {
        testParse("./test/fixtures/public/MonteCarloPi.jmm");
    }

    @Test
    public void testQuickSort() {
        testParse("./test/fixtures/public/QuickSort.jmm");
    }

    @Test
    public void testSimple() {
        testParse("./test/fixtures/public/Simple.jmm");
    }

    @Test
    public void testTicTacToe() {
        testParse("./test/fixtures/public/TicTacToe.jmm");
    }


    // CUSTOM TESTS
    @Test
    public void testOverloading() {
        testParse("fixtures/public/Overloading.jmm");
    }

    @Test
    public void testOverloadingWithIncludes() {
        testParse("fixtures/public/OverloadingWithIncludes.jmm");
    }

    @Test
    public void testBlackJack() {
        testParse("fixtures/public/TestBlackJack.jmm");
    }

    @Test
    public void testBubbleSort() {
        testParse("fixtures/public/TestBubbleSort.jmm");
    }

    @Test
    public void testBinarySearch() {
        testParse("fixtures/public/BinarySearch.jmm");
    }

    @Test
    public void testUninitializedVar() {
        testParse("fixtures/public/testUninitializedVars.jmm");
    }

    // MUST FAIL TESTS FROM HERE ON OUT
    @Test
    public void failBlowUpTest() {
        testParseFail("./test/fixtures/public/fail/syntactical/BlowUp.jmm");
    }

    @Test
    public void failCompleteWhileTest() {
        testParseFail("./test/fixtures/public/fail/syntactical/CompleteWhileTest.jmm");
    }

    @Test
    public void failLengthError() {
        testParseFail("./test/fixtures/public/fail/syntactical/LengthError.jmm");
    }

    @Test
    public void failMissingRightPar() {
        testParseFail("./test/fixtures/public/fail/syntactical/MissingRightPar.jmm");
    }

    @Test
    public void failMultipleSequential() {
        testParseFail("./test/fixtures/public/fail/syntactical/MultipleSequential.jmm");
    }

    @Test
    public void failNestedLoopTest() {
        testParseFail("./test/fixtures/public/fail/syntactical/NestedLoop.jmm");
    }
}
