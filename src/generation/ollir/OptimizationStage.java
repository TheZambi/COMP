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
        OllirAssistant result = visitor.visit(root);

        // Convert the AST to a String containing the equivalent OLLIR code
        String ollirCode = ""; // Convert node ...

        ollirCode = result.getValue();

        try {
            FileWriter myWriter = new FileWriter("./ollirCode.ollir");
            myWriter.write(ollirCode);
            myWriter.close();
            System.err.println("Successfully wrote to the file.");
        } catch (IOException e) {
            System.err.println("An error occurred.");
            e.printStackTrace();
        }

        // More reports from this stage
        List<Report> reports = new ArrayList<>();

        return new OllirResult(semanticsResult, ollirCode, reports);
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
            FileWriter myWriter = new FileWriter("./out_optimized.json");
            myWriter.write(root.toJson());
            myWriter.close();
            System.err.println("Successfully wrote to the file.");
        } catch (IOException e) {
            System.err.println("An error occurred.");
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
