import org.junit.Test;
import pt.up.fe.comp.TestUtils;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.specs.util.SpecsIo;

import java.util.List;

public class OllirTest {

    private void testOllir(String filename) {
        System.out.println("TEST " + filename);

        OllirResult ollirResult = TestUtils.optimize(SpecsIo.getResource(filename), false);

        printReports(ollirResult.getReports());

        TestUtils.noErrors(ollirResult.getReports());
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
        testOllir("fixtures/public/WhileAndIF.jmm");
    }

    @Test
    public void testFindMaximum() {
        testOllir("fixtures/public/FindMaximum.jmm");
    }

    @Test
    public void testHelloWorld() {
        testOllir("fixtures/public/HelloWorld.jmm");
    }

    @Test
    public void testLazysort() {
        testOllir("fixtures/public/Lazysort.jmm");
    }

    @Test
    public void testLife() {
        testOllir("fixtures/public/Life.jmm");
    }

    @Test
    public void testMonteCarloPi() {
        testOllir("fixtures/public/MonteCarloPi.jmm");
    }

    @Test
    public void testQuickSort() {
        testOllir("fixtures/public/QuickSort.jmm");
    }

    @Test
    public void testSimple() {
        testOllir("fixtures/public/Simple.jmm");
    }

    @Test
    public void testTicTacToe() {
        testOllir("fixtures/public/TicTacToe.jmm");
    }

    // CUSTOM TESTS
    @Test
    public void testOverloading() {
        testOllir("fixtures/custom/Overloading.jmm");
    }

    @Test
    public void testOverloadingWithIncludes() {
        testOllir("fixtures/custom/OverloadingWithIncludes.jmm");
    }

    @Test
    public void testBlackJack() {
        testOllir("fixtures/custom/TestBlackJack.jmm");
    }

    @Test
    public void testBubbleSort() {
        testOllir("fixtures/custom/TestBubbleSort.jmm");
    }

    @Test
    public void testBinarySearch() {
        testOllir("fixtures/custom/BinarySearch.jmm");
    }

    @Test
    public void testUninitializedVar() {
        testOllir("fixtures/custom/TestUninitializedVars.jmm");
    }
}
