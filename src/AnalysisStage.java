import analysis.MySymbolTable;
import analysis.visitors.InitedVarsVisitor;
import analysis.visitors.PrintVisitor;
import analysis.visitors.TypeVerificationVisitor;
import pt.up.fe.comp.jmm.JmmNode;
import pt.up.fe.comp.jmm.JmmParserResult;
import pt.up.fe.comp.jmm.analysis.JmmAnalysis;
import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.report.Report;
import analysis.visitors.SymbolTableVisitor;

import java.util.List;

public class AnalysisStage implements JmmAnalysis {

    private MySymbolTable symbolTable;
    private List<Report> reports;
    private JmmNode root;

    @Override
    public JmmSemanticsResult semanticAnalysis(JmmParserResult parserResult) {
        this.reports = parserResult.getReports();
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

        return new JmmSemanticsResult(root, symbolTable, reports);
    }
}
