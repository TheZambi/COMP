
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

import java.util.List;

public class BackendTest {

    private void testBackend(String filename, String desiredOutput) {
        System.out.println("TEST " + filename);

        JasminResult jasminResult = TestUtils.backend(SpecsIo.getResource(filename));

        printReports(jasminResult.getReports());

        TestUtils.noErrors(jasminResult.getReports());

        var output = jasminResult.run();
        assertEquals(desiredOutput, output.trim());
    }

    private static void printReports(List<Report> reports) {
        if (reports.size() == 0)
            System.out.println("No errors found");
        for (Report r: reports) {
            System.out.println(r);
        }
    }

//    @Test
//    public void testWhileAndIF() {
//        testBackend("fixtures/public/WhileAndIF.jmm");
//    }
//
//    @Test
//    public void testFindMaximum() {
//        testBackend("fixtures/public/FindMaximum.jmm");
//    }

    @Test
    public void testHelloWorld() {
        testBackend("fixtures/public/HelloWorld.jmm", "Hello, World!");
    }

//    @Test
//    public void testLazysort() {
//        testBackend("fixtures/public/Lazysort.jmm");
//    }
//
//    @Test
//    public void testLife() {
//        testBackend("fixtures/public/Life.jmm");
//    }
//
//    @Test
//    public void testMonteCarloPi() {
//        testBackend("fixtures/public/MonteCarloPi.jmm");
//    }
//
//    @Test
//    public void testQuickSort() {
//        testBackend("fixtures/public/QuickSort.jmm");
//    }

    @Test
    public void testSimple() {
        testBackend("fixtures/public/Simple.jmm", "30");
    }

//    @Test
//    public void testTicTacToe() {
//        testBackend("fixtures/public/TicTacToe.jmm");
//    }
//
//    // CUSTOM TESTS
//    @Test
//    public void testOverloading() {
//        testBackend("fixtures/public/Overloading.jmm");
//    }
}
