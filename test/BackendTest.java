
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
import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.jasmin.JasminResult;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.ReportType;
import pt.up.fe.specs.util.SpecsIo;
import pt.up.fe.specs.util.SpecsStrings;

import java.util.List;

public class BackendTest {

    private void testBackend(String filename, String desiredOutput) {
        System.out.println("TEST " + filename);

        JasminResult jasminResult = TestUtils.backend(SpecsIo.getResource(filename));

        printReports(jasminResult.getReports());

        TestUtils.noErrors(jasminResult.getReports());

        var output = jasminResult.run();
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
        testBackend("fixtures/public/WhileAndIF.jmm", "10\r\n10\r\n10\r\n10\r\n10\r\n10\r\n10\r\n10\r\n10\r\n10");
    }

    @Test
    public void testFindMaximum() {
        testBackend("fixtures/public/FindMaximum.jmm", "Result: 28");
    }

    @Test
    public void testHelloWorld() {
        testBackend("fixtures/public/HelloWorld.jmm", "Hello, World!");
    }

//        Requires setting the random seed
//    @Test
//    public void testLazysort() {
//        testBackend("fixtures/public/Lazysort.jmm", "1\r\n2\r\n3\r\n4\r\n5\r\n6\r\n7\r\n8\r\n9\r\n10");
//    }

    //        Requires Input
    @Test
    public void testLife() {
        testBackend("fixtures/public/Life.jmm", "");
    }

//        Requires Input
//    @Test
//    public void testMonteCarloPi() {
//        testBackend("fixtures/public/MonteCarloPi.jmm", "");
//    }

    @Test
    public void testQuickSort() {
        testBackend("fixtures/public/QuickSort.jmm", "1\r\n2\r\n3\r\n4\r\n5\r\n6\r\n7\r\n8\r\n9\r\n10");
    }

    @Test
    public void testSimple() {
        testBackend("fixtures/public/Simple.jmm", "30");
    }

//        Requires Input
//    @Test
//    public void testTicTacToe() {
//        testBackend("fixtures/public/TicTacToe.jmm", "");
//    }

    // CUSTOM TESTS
    @Test
    public void testOverloading() {
        testBackend("fixtures/public/Overloading.jmm", "");
    }

    @Test
    public void testOverloadingWithIncludes() {
        testBackend("fixtures/public/OverloadingWithIncludes.jmm", "1\r\n2\r\n3\r\n4");
    }
}
