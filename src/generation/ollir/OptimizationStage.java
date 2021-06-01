package generation.ollir;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import generation.ollir.visitors.ConstantFoldingVisitor;
import generation.ollir.visitors.ConstantPropagationVisitor;
import generation.ollir.visitors.UnusedVarsVisitor;
import ast.MySymbolTable;
import pt.up.fe.comp.jmm.JmmNode;
import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.ollir.JmmOptimization;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.ReportType;
import pt.up.fe.comp.jmm.report.Stage;

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

public class OptimizationStage implements JmmOptimization {

    @Override
    public OllirResult toOllir(JmmSemanticsResult semanticsResult) {
        return toOllir(semanticsResult, false);
    }

    @Override
    public OllirResult toOllir(JmmSemanticsResult semanticsResult, boolean optimize) {

        JmmNode root = semanticsResult.getRootNode();
        MySymbolTable symbolTable = (MySymbolTable) semanticsResult.getSymbolTable();

        OllirVisitor visitor = new OllirVisitor(symbolTable, optimize);

        try {
            OllirAssistant result = visitor.visit(root);

            // Convert the AST to a String containing the equivalent OLLIR code
            String ollirCode = ""; // Convert node ...

            ollirCode = result.getValue();

            try {
                FileWriter myWriter = new FileWriter("./" + symbolTable.getClassName() + ".symbols.txt");
                myWriter.write(semanticsResult.getSymbolTable().print());
                myWriter.close();
//            System.err.println("Successfully wrote to the file.");
            } catch (IOException e) {
                System.err.println("An error occurred writing symbol table to './" + symbolTable.getClassName() + ".symbols.txt'");
                e.printStackTrace();
            }

            try {
                FileWriter myWriter = new FileWriter("./" + semanticsResult.getSymbolTable().getClassName() + ".ollir");
                myWriter.write(ollirCode);
                myWriter.close();
//            System.err.println("Successfully wrote to the file.");
            } catch (IOException e) {
                System.err.println("An error occurred writing log to './" + semanticsResult.getSymbolTable().getClassName() + ".ollir'");
                e.printStackTrace();
            }

            // More reports from this stage
            List<Report> reports = new ArrayList<>();

            return new OllirResult(semanticsResult, ollirCode, reports);
        } catch (Exception e) {
            e.printStackTrace();
            semanticsResult.getReports().add(new Report(ReportType.ERROR, Stage.SEMANTIC, -1, -1,
                    "Internal parser error on OllirVisitor"));
        }

        return null;
    }

    @Override
    public JmmSemanticsResult optimize(JmmSemanticsResult semanticsResult) {

        JmmNode root = semanticsResult.getRootNode();
        MySymbolTable symbolTable = (MySymbolTable) semanticsResult.getSymbolTable();

        ConstantPropagationVisitor constantPropagationVisitor = new ConstantPropagationVisitor();
        ConstantFoldingVisitor constantFoldingVisitor = new ConstantFoldingVisitor();

        boolean did_stuff;
        do {
            did_stuff = constantPropagationVisitor.propagate(root);
            did_stuff |= constantFoldingVisitor.fold(root);
        } while (did_stuff);


        UnusedVarsVisitor unusedVarsVisitor = new UnusedVarsVisitor(symbolTable);
        unusedVarsVisitor.visit(root);

        try {
            FileWriter myWriter = new FileWriter("./" + semanticsResult.getSymbolTable().getClassName() + "_optimized.json");
            myWriter.write(root.toJson());
            myWriter.close();
//            System.err.println("Successfully wrote to the file.");
        } catch (IOException e) {
            System.err.println("./" + semanticsResult.getSymbolTable().getClassName() + "_optimized.json");
            e.printStackTrace();
        }

        return semanticsResult;
    }

    @Override
    public OllirResult optimize(OllirResult ollirResult) {
        // THIS IS JUST FOR CHECKPOINT 3
        return ollirResult;
    }



}
