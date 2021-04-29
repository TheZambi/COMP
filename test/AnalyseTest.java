import org.junit.Test;
import pt.up.fe.comp.TestUtils;
import pt.up.fe.comp.jmm.JmmParserResult;
import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.ReportType;
import pt.up.fe.specs.util.SpecsIo;

import java.util.List;

public class AnalyseTest {

    private void testAnalyse(String filename) {
        System.out.println("TEST " + filename);

        JmmSemanticsResult semanticsResult = TestUtils.analyse(SpecsIo.getResource(filename));
        printReports(semanticsResult.getReports());
        TestUtils.noErrors(semanticsResult.getReports());
    }

    private void testAnalyseFail(String filename) {
        System.out.println("FAIl " + filename);

        JmmSemanticsResult semanticsResult = TestUtils.analyse(SpecsIo.getResource(filename));
        printReports(semanticsResult.getReports());
        TestUtils.mustFail(semanticsResult.getReports());
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
        testAnalyse("fixtures/public/WhileAndIF.jmm");
    }

    @Test
    public void testFindMaximum() {
        testAnalyse("fixtures/public/FindMaximum.jmm");
    }

    @Test
    public void testHelloWorld() {
        testAnalyse("fixtures/public/HelloWorld.jmm");
    }

    @Test
    public void testLazysort() {
        testAnalyse("fixtures/public/Lazysort.jmm");
    }

    @Test
    public void testLife() {
        testAnalyse("fixtures/public/Life.jmm");
    }

    @Test
    public void testMonteCarloPi() {
        testAnalyse("fixtures/public/MonteCarloPi.jmm");
    }

    @Test
    public void testQuickSort() {
        testAnalyse("fixtures/public/QuickSort.jmm");
    }

    @Test
    public void testSimple() {
        testAnalyse("fixtures/public/Simple.jmm");
    }

    @Test
    public void testTicTacToe() {
        testAnalyse("fixtures/public/TicTacToe.jmm");
    }

    // CUSTOM TESTS
    @Test
    public void testOverloading() {
        testAnalyse("fixtures/public/Overloading.jmm");
    }


    // MUST FAIL TESTS FROM HERE ON OUT
    @Test
    public void failArrIndexNotInt() {
        testAnalyseFail("fixtures/public/fail/semantic/arr_index_not_int.jmm");
    }

    @Test
    public void failArrSizeNotInt() {
        testAnalyseFail("fixtures/public/fail/semantic/arr_size_not_int.jmm");
    }

    @Test
    public void failBadArguments() {
        testAnalyseFail("fixtures/public/fail/semantic/badArguments.jmm");
    }

    @Test
    public void failBinopIncomp() {
        testAnalyseFail("fixtures/public/fail/semantic/binop_incomp.jmm");
    }

    @Test
    public void failFuncNotFound() {
        testAnalyseFail("fixtures/public/fail/semantic/funcNotFound.jmm");
    }

    @Test
    public void failSimpleLength() {
        testAnalyseFail("fixtures/public/fail/semantic/simple_length.jmm");
    }

    @Test
    public void failVarExpIncomp() {
        testAnalyseFail("fixtures/public/fail/semantic/var_exp_incomp.jmm");
    }

    @Test
    public void failVarLitIncomp() {
        testAnalyseFail("fixtures/public/fail/semantic/var_lit_incomp.jmm");
    }

    @Test
    public void failVarUndef() {
        testAnalyseFail("fixtures/public/fail/semantic/var_undef.jmm");
    }

    @Test
    public void failVarNotInit() {
        testAnalyseFail("fixtures/public/fail/semantic/varNotInit.jmm");
    }

    @Test
    public void failExtraMissType() {
        testAnalyseFail("fixtures/public/fail/semantic/extra/miss_type.jmm");
    }

}
