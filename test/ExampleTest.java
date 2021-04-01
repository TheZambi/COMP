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

public class ExampleTest {

    private void testParse(String filename) {
        testParse(filename, false);
    }

    private void testParse(String filename, Boolean print) {
        var fileContents = SpecsIo.read("./test/fixtures/public/WhileAndIF.jmm");
        JmmParserResult parserResult = TestUtils.parse(fileContents);

        if (print)
            System.out.println(parserResult.getRootNode().getKind());
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

//    @Test
//    public void failCompleteWhileTest() {
//
//        var fileContents = SpecsIo.read("./test/fixtures/public/fail/syntactical/CompleteWhileTest.jmm");
//        JmmParserResult result = TestUtils.parse(fileContents);
//
////        System.out.println( result.getRootNode().toJson()); //prints ast in json format
////
////        for(Report r: result.getReports()) //prints errors in json format
////            System.out.println(r.toJson());
//    }
}
