package analysis;

import analysis.visitors.*;
import ast.MySymbolTable;
import pt.up.fe.comp.jmm.JmmNode;
import pt.up.fe.comp.jmm.JmmParserResult;
import pt.up.fe.comp.jmm.analysis.JmmAnalysis;
import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.ReportType;
import pt.up.fe.comp.jmm.report.Stage;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AnalysisStage implements JmmAnalysis {

    @Override
    public JmmSemanticsResult semanticAnalysis(JmmParserResult parserResult) {
        List<Report> reports = new ArrayList<>();
        JmmNode root = parserResult.getRootNode();
        MySymbolTable symbolTable = new MySymbolTable();

        SymbolTableVisitor stv = new SymbolTableVisitor(symbolTable, reports);
        try {
            stv.visit(root);
        } catch (Exception e) {
            e.printStackTrace();
            reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, -1, -1,
                    "Internal parser error on SymbolTableVisitor"));
        }

        TypeVerificationVisitor typeVerificationVisitor = new TypeVerificationVisitor(symbolTable, reports);
        try {
            typeVerificationVisitor.visit(root);
        } catch (Exception e) {
            e.printStackTrace();
            reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, -1, -1,
                    "Internal parser error on TypeVerificationVisitor"));
        }

        MethodReturnExtractor methodReturnExtractor = new MethodReturnExtractor(symbolTable, reports);
        try {
            methodReturnExtractor.visit(root);
        } catch (Exception e) {
            e.printStackTrace();
            reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, -1, -1,
                    "Internal parser error on MethodReturnExtractor"));
        }

        InitedVarsVisitor initedVarsVisitor = new InitedVarsVisitor(symbolTable, reports);
        try {
            initedVarsVisitor.visit(root);
        } catch (Exception e) {
            e.printStackTrace();
            reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, -1, -1,
                    "Internal parser error on InitedVarsVisitor"));
        }

        try {
            FileWriter myWriter = new FileWriter("./" + symbolTable.getClassName() + ".json");
            myWriter.write(root.toJson());
            myWriter.close();
//            System.err.println("Successfully wrote to the file.");
        } catch (IOException e) {
            System.err.println("An error occurred writing log to './" + symbolTable.getClassName() + ".json'.");
            e.printStackTrace();
        }

        return new JmmSemanticsResult(root, symbolTable, reports);
    }
}
