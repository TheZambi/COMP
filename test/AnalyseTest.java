import org.junit.Test;
import pt.up.fe.comp.TestUtils;
import pt.up.fe.comp.jmm.JmmParserResult;
import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.specs.util.SpecsIo;

public class AnalyseTest {

    private void testAnalyse(String filename) {
        testAnalyse(filename, false);
    }

    private void testAnalyse(String filename, Boolean print) {
        var fileContents = SpecsIo.read(filename);
         JmmSemanticsResult semanticsResult = TestUtils.analyse(fileContents);

        if (print)
            System.out.println(semanticsResult.getReports());
    }

    private void testAnalyseFail(String filename) {
        testAnalyseFail(filename, false);
    }

    private void testAnalyseFail(String filename, Boolean print) {
        var fileContents = SpecsIo.read(filename);
        JmmSemanticsResult semanticsResult = TestUtils.analyse(fileContents);

        if (print)
            System.out.println("Total Reports: " + semanticsResult.getReports().size());

        TestUtils.mustFail(semanticsResult.getReports());
    }

    @Test
    public void testWhileAndIF() {
        testAnalyse("./test/fixtures/public/WhileAndIF.jmm");
    }

    @Test
    public void testFindMaximum() {
        testAnalyse("./test/fixtures/public/FindMaximum.jmm");
    }

    @Test
    public void testHelloWorld() {
        testAnalyse("./test/fixtures/public/HelloWorld.jmm");
    }

    @Test
    public void testLazysort() {
        testAnalyse("./test/fixtures/public/Lazysort.jmm");
    }

    @Test
    public void testLife() {
        testAnalyse("./test/fixtures/public/Life.jmm");
    }

    @Test
    public void testMonteCarloPi() {
        testAnalyse("./test/fixtures/public/MonteCarloPi.jmm");
    }

    @Test
    public void testQuickSort() {
        testAnalyse("./test/fixtures/public/QuickSort.jmm");
    }

    @Test
    public void testSimple() {
        testAnalyse("./test/fixtures/public/Simple.jmm");
    }

    @Test
    public void testTicTacToe() {
        testAnalyse("./test/fixtures/public/TicTacToe.jmm");
    }


    // MUST FAIL TESTS FROM HERE ON OUT
    @Test
    public void failArrIndexNotInt() {
        testAnalyseFail("./test/fixtures/public/fail/semantic/arr_index_not_int.jmm");
    }

    @Test
    public void failArrSizeNotInt() {
        testAnalyseFail("./test/fixtures/public/fail/semantic/arr_size_not_int.jmm");
    }

    @Test
    public void failBadArguments() {
        testAnalyseFail("./test/fixtures/public/fail/semantic/badArguments.jmm");
    }

    @Test
    public void failBinopIncomp() {
        testAnalyseFail("./test/fixtures/public/fail/semantic/binop_incomp.jmm");
    }

    @Test
    public void failFuncNotFound() {
        testAnalyseFail("./test/fixtures/public/fail/semantic/funcNotFound.jmm");
    }

    @Test
    public void failSimpleLength() {
        testAnalyseFail("./test/fixtures/public/fail/semantic/simple_length.jmm");
    }

    @Test
    public void failVarExpIncomp() {
        testAnalyseFail("./test/fixtures/public/fail/semantic/var_exp_incomp.jmm");
    }

    @Test
    public void failVarLitIncomp() {
        testAnalyseFail("./test/fixtures/public/fail/semantic/var_lit_incompt.jmm");
    }

    @Test
    public void failVarUndef() {
        testAnalyseFail("./test/fixtures/public/fail/semantic/var_undef.jmm");
    }

    @Test
    public void failVarNotInit() {
        testAnalyseFail("./test/fixtures/public/fail/semantic/varNotInit.jmm");
    }

    @Test
    public void failExtraMissType() {
        testAnalyseFail("./test/fixtures/public/fail/semantic/extra/miss_type.jmm");
    }

}