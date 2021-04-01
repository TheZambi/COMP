import pt.up.fe.comp.jmm.JmmNode;
import pt.up.fe.comp.jmm.JmmParserResult;
import pt.up.fe.comp.jmm.analysis.JmmAnalysis;
import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.report.Report;

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



        return new JmmSemanticsResult(root, symbolTable, reports);
    }
}
