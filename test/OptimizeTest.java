
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

import org.junit.Test;

import pt.up.fe.comp.TestUtils;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.ReportType;
import pt.up.fe.specs.util.SpecsIo;

import java.util.List;

public class OptimizeTest {

    private void testOptimize(String filename) {
        System.out.println("TEST " + filename);

        OllirResult ollirResult = TestUtils.optimize(SpecsIo.getResource(filename));

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
        testOptimize("fixtures/public/WhileAndIF.jmm");
    }

    @Test
    public void testFindMaximum() {
        testOptimize("fixtures/public/FindMaximum.jmm");
    }

    @Test
    public void testHelloWorld() {
        testOptimize("fixtures/public/HelloWorld.jmm");
    }

    @Test
    public void testLazysort() {
        testOptimize("fixtures/public/Lazysort.jmm");
    }

    @Test
    public void testLife() {
        testOptimize("fixtures/public/Life.jmm");
    }

    @Test
    public void testMonteCarloPi() {
        testOptimize("fixtures/public/MonteCarloPi.jmm");
    }

    @Test
    public void testQuickSort() {
        testOptimize("fixtures/public/QuickSort.jmm");
    }

    @Test
    public void testSimple() {
        testOptimize("fixtures/public/Simple.jmm");
    }

    @Test
    public void testTicTacToe() {
        testOptimize("fixtures/public/TicTacToe.jmm");
    }

    // CUSTOM TESTS
    @Test
    public void testOverloading() {
        testOptimize("fixtures/public/Overloading.jmm");
    }
}
