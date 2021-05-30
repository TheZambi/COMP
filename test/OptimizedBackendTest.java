
/**
 * Copyright 2021 SPeCS.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License. under the License.
 */

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import pt.up.fe.comp.TestUtils;
import pt.up.fe.comp.jmm.jasmin.JasminResult;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.specs.util.SpecsIo;
import pt.up.fe.specs.util.SpecsStrings;

import java.util.List;

public class OptimizedBackendTest {

    private void testOptimizedBackend(String filename, String desiredOutput) {
        testOptimizedBackend(filename, desiredOutput, "");
    }

    private void testOptimizedBackend(String filename, String desiredOutput, String input) {
        System.out.println("TEST " + filename);

        var ollirResult = TestUtils.optimize(SpecsIo.getResource(filename), true);
        TestUtils.noErrors(ollirResult.getReports());
        JasminResult jasminResult = TestUtils.backend(ollirResult);

        printReports(jasminResult.getReports());

        TestUtils.noErrors(jasminResult.getReports());

        var output = jasminResult.run(input);
        assertEquals(SpecsStrings.normalizeFileContents(desiredOutput), SpecsStrings.normalizeFileContents(output.trim()));
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
        testOptimizedBackend("fixtures/public/WhileAndIF.jmm", "10\n10\n10\n10\n10\n10\n10\n10\n10\n10");
    }

    @Test
    public void testFindMaximum() {
        testOptimizedBackend("fixtures/public/FindMaximum.jmm", "Result: 28");
    }

    @Test
    public void testHelloWorld() {
        testOptimizedBackend("fixtures/public/HelloWorld.jmm", "Hello, World!");
    }

//    //        Requires setting the random seed
//    @Test
//    public void testLazysort() {
//        testOptimizedBackend("fixtures/public/Lazysort.jmm", "1\n2\n3\n4\n5\n6\n7\n8\n9\n10");
//    }
//
//    //        Infinite loop
//    @Test
//    public void testLife() {
//        testBackend("fixtures/public/Life.jmm", "","1");
//    }

    //        Random
    @Test
    public void testMonteCarloPi() {
        testOptimizedBackend("fixtures/public/MonteCarloPi.jmm", "Insert number: Result: 314", "5000000");
    }

    @Test
    public void testQuickSort() {
        testOptimizedBackend("fixtures/public/QuickSort.jmm", "1\n2\n3\n4\n5\n6\n7\n8\n9\n10");
    }

    @Test
    public void testSimple() {
        testOptimizedBackend("fixtures/public/Simple.jmm", "30");
    }

    //        Requires Input
    @Test
    public void testTicTacToe() {
        testOptimizedBackend("fixtures/public/TicTacToe.jmm",tictactoeString() ,SpecsIo.getResource("fixtures/public/TicTacToe.input"));
    }

    // CUSTOM TESTS
    @Test
    public void testOverloading() {
        testOptimizedBackend("fixtures/custom/Overloading.jmm", "");
    }

    @Test
    public void testOverloadingWithIncludes() {
        testOptimizedBackend("fixtures/custom/OverloadingWithIncludes.jmm", "1\n2\n3\n4");
    }

    @Test
    public void testBinarySearch() {
        testOptimizedBackend("fixtures/custom/BinarySearch.jmm", "3");
    }

    @Test
    public void testUninitializedVar() {
        testOptimizedBackend("fixtures/custom/TestUninitializedVars.jmm", "1");
    }

    // Until the game ends, basically infinite
//    @Test
//    public void testBlackJack() {
//        testBackend("fixtures/public/TestBlackJack.jmm", "", "2");
//    }


    private String tictactoeString() {
        return "0|0|0\n" +
                "- - -\n" +
                "0|0|0\n" +
                "- - -\n" +
                "0|0|0\n" +
                "\n" +
                "Player 1 turn! Enter the row(0-2):  Enter the column(0-2): \n" +
                "1|0|0\n" +
                "- - -\n" +
                "0|0|0\n" +
                "- - -\n" +
                "0|0|0\n" +
                "\n" +
                "Player 2 turn! Enter the row(0-2):  Enter the column(0-2): \n" +
                "1|2|0\n" +
                "- - -\n" +
                "0|0|0\n" +
                "- - -\n" +
                "0|0|0\n" +
                "\n" +
                "Player 1 turn! Enter the row(0-2):  Enter the column(0-2): \n" +
                "1|2|0\n" +
                "- - -\n" +
                "0|1|0\n" +
                "- - -\n" +
                "0|0|0\n" +
                "\n" +
                "Player 2 turn! Enter the row(0-2):  Enter the column(0-2): \n" +
                "1|2|0\n" +
                "- - -\n" +
                "2|1|0\n" +
                "- - -\n" +
                "0|0|0\n" +
                "\n" +
                "Player 1 turn! Enter the row(0-2):  Enter the column(0-2): \n" +
                "1|2|0\n" +
                "- - -\n" +
                "2|1|0\n" +
                "- - -\n" +
                "0|0|1\n" +
                "\n" +
                "Congratulations, 1, you have won the game.";
    }
}
