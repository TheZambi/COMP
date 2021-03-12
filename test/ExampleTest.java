import static org.junit.Assert.*;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Properties;
import java.io.StringReader;

import pt.up.fe.comp.TestUtils;
import pt.up.fe.specs.util.SpecsIo;

public class ExampleTest {


    @Test
    public void testWhileAndIF() {
//        assertEquals("Expression", TestUtils.parse("2+3\n").getRootNode().getKind());
        var fileContents = SpecsIo.read("./test/fixtures/public/WhileAndIF.jmm");
        System.out.println( TestUtils.parse(fileContents).getRootNode().getKind());
    }

    @Test
    public void testFindMaximum() {
//        assertEquals("Expression", TestUtils.parse("2+3\n").getRootNode().getKind());
        var fileContents = SpecsIo.read("./test/fixtures/public/FindMaximum.jmm");
        System.out.println( TestUtils.parse(fileContents).getRootNode().getKind());
    }

    @Test
    public void testHelloWorld() {
//        assertEquals("Expression", TestUtils.parse("2+3\n").getRootNode().getKind());
        var fileContents = SpecsIo.read("./test/fixtures/public/HelloWorld.jmm");
        System.out.println( TestUtils.parse(fileContents).getRootNode().getKind());
    }

    @Test
    public void testLazysort() {
//        assertEquals("Expression", TestUtils.parse("2+3\n").getRootNode().getKind());
        var fileContents = SpecsIo.read("./test/fixtures/public/Lazysort.jmm");
        System.out.println( TestUtils.parse(fileContents).getRootNode().getKind());
    }

    @Test
    public void testLife() {
//        assertEquals("Expression", TestUtils.parse("2+3\n").getRootNode().getKind());
        var fileContents = SpecsIo.read("./test/fixtures/public/Life.jmm");
        System.out.println( TestUtils.parse(fileContents).getRootNode().getKind());
    }

    @Test
    public void testMonteCarloPi() {
//        assertEquals("Expression", TestUtils.parse("2+3\n").getRootNode().getKind());
        var fileContents = SpecsIo.read("./test/fixtures/public/MonteCarloPi.jmm");
        System.out.println( TestUtils.parse(fileContents).getRootNode().getKind());
    }

    @Test
    public void testQuickSort() {
//        assertEquals("Expression", TestUtils.parse("2+3\n").getRootNode().getKind());
        var fileContents = SpecsIo.read("./test/fixtures/public/QuickSort.jmm");
        System.out.println( TestUtils.parse(fileContents).getRootNode().getKind());
    }

    @Test
    public void testSimple() {
//        assertEquals("Expression", TestUtils.parse("2+3\n").getRootNode().getKind());
        var fileContents = SpecsIo.read("./test/fixtures/public/Simple.jmm");
        System.out.println( TestUtils.parse(fileContents).getRootNode().getKind());
    }

    @Test
    public void testTicTacToe() {
//        assertEquals("Expression", TestUtils.parse("2+3\n").getRootNode().getKind());
        var fileContents = SpecsIo.read("./test/fixtures/public/TicTacToe.jmm");
        System.out.println( TestUtils.parse(fileContents).getRootNode().getKind());
    }

    @Test
    public void failCompleteWhileTest() {
//        assertEquals("Expression", TestUtils.parse("2+3\n").getRootNode().getKind());
        var fileContents = SpecsIo.read("./test/fixtures/public/fail/syntactical/CompleteWhileTest.jmm");
        System.out.println( TestUtils.parse(fileContents).getRootNode().getKind());
    }
}
