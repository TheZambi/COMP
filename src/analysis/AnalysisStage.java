package analysis;

import ast.MySymbolTable;
import analysis.visitors.InitedVarsVisitor;
import analysis.visitors.TypeVerificationVisitor;
import pt.up.fe.comp.jmm.JmmNode;
import pt.up.fe.comp.jmm.JmmParserResult;
import pt.up.fe.comp.jmm.analysis.JmmAnalysis;
import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.report.Report;
import analysis.visitors.SymbolTableVisitor;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AnalysisStage implements JmmAnalysis {

    private MySymbolTable symbolTable;
    private List<Report> reports;
    private JmmNode root;

    @Override
    public JmmSemanticsResult semanticAnalysis(JmmParserResult parserResult) {
        this.reports = new ArrayList<>();
        this.root = parserResult.getRootNode();
        this.symbolTable = new MySymbolTable();

        SymbolTableVisitor stv = new SymbolTableVisitor(symbolTable, reports);
        try {
            stv.visit(root);
        } catch (Exception e) {
            e.printStackTrace();
        }

        TypeVerificationVisitor typeVerificationVisitor = new TypeVerificationVisitor(symbolTable, reports);
        try {
            typeVerificationVisitor.visit(root);
        } catch (Exception e) {
            e.printStackTrace();
        }

        InitedVarsVisitor initedVarsVisitor = new InitedVarsVisitor(symbolTable, reports);
        try {
            initedVarsVisitor.visit(root);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            FileWriter myWriter = new FileWriter("./out_semantic.json");
            myWriter.write(root.toJson());
            myWriter.close();
            System.err.println("Successfully wrote to the file.");
        } catch (IOException e) {
            System.err.println("An error occurred.");
            e.printStackTrace();
        }

        return new JmmSemanticsResult(root, symbolTable, reports);
    }
}
