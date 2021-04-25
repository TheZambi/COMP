package analysis.visitors;

import analysis.AstUtils;
import analysis.MySymbolTable;
import pt.up.fe.comp.jmm.JmmNode;
import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.ReportType;
import pt.up.fe.comp.jmm.report.Stage;
import pt.up.fe.specs.util.SpecsCheck;

import java.util.*;
import java.util.function.Consumer;

public class InitedVarsVisitor {

    private final Map<String, Consumer<JmmNode>> visitMap, previsitMap;

    private final List<Report> reports;

    private final MySymbolTable symbolTable;

    private enum InitStatus {
        UNINITIALIZED,
        CONDITIONAL,
        INITIALIZED
    }

    private Map<String, InitStatus> initedVars;


    public InitedVarsVisitor(MySymbolTable symbolTable, List<Report> reports) {
        this.reports = reports;

        this.symbolTable = symbolTable;
        this.visitMap = new HashMap<>();
        this.visitMap.put("Value", this::valueVisit);
        this.visitMap.put("VarDeclaration", this::varDeclarationVisit);
        this.visitMap.put("Statement", this::assignmentVisit);

        this.previsitMap = new HashMap<>();
        this.previsitMap.put("MethodDeclaration", this::methodDeclarationPreVisit);

    }

    private void valueVisit(JmmNode node) {
        Optional<JmmNode> ancestorOpt = node.getAncestor("Statement");
        if (ancestorOpt.isPresent()) {
            JmmNode ancestor = ancestorOpt.get();
            // Skip when this is the value being attributed
            if (AstUtils.isAssignment(ancestor) && ancestor.getChildren().get(0) == node) {
                return;
            }
        }

        if (!node.get("type").equals("object"))
            return;

        String name = node.get("object");
        InitStatus st = this.initedVars.get(name);
        if (st == null)
            return;

        switch (st) {
            case UNINITIALIZED:
                reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(node.get("line")), Integer.parseInt(node.get("col")),
                        "Local variable <" + name + "> was not initialized"));
                break;
//            case CONDITIONAL:
//                reports.add(new Report(ReportType.WARNING, Stage.SEMANTIC, Integer.parseInt(node.get("line")), Integer.parseInt(node.get("col")),
//                        "Local variable <" + name + "> may not have been initialized"));
//                break;
            default:
                break;
        }
    }

    private void varDeclarationVisit(JmmNode node) {
        Symbol s = AstUtils.getChildSymbol(node, 0);
        if (this.initedVars != null)
            this.initedVars.put(s.getName(), InitStatus.UNINITIALIZED);
    }

    private void methodDeclarationPreVisit(JmmNode node) {
        this.initedVars = new HashMap<>();
    }

    private void assignmentVisit(JmmNode node) {
        if (!AstUtils.isAssignment(node))
            return;

        JmmNode firstChild = node.getChildren().get(0);
        if (firstChild.getKind().equals("Indexing"))
            return;

        String name = firstChild.get("object");
        boolean isConditional = AstUtils.isInsideConditionalBranch(node);

        InitStatus st = this.initedVars.get(name);
        if (st == InitStatus.UNINITIALIZED)
            this.initedVars.put(name, isConditional ? InitStatus.CONDITIONAL : InitStatus.INITIALIZED);
        else if (st == InitStatus.CONDITIONAL && !isConditional)
            this.initedVars.put(name, InitStatus.INITIALIZED);
    }

    public void visit(JmmNode node) {
        SpecsCheck.checkNotNull(node, () -> "Node should not be null");

        Consumer<JmmNode> visit = this.visitMap.get(node.getKind());
        Consumer<JmmNode> preVisit = this.previsitMap.get(node.getKind());

        if (preVisit != null)
            preVisit.accept(node);

        // Preorder: 1st visit each children
        for (var child : node.getChildren())
            visit(child);

        // Preorder: then visit the node
        if (visit != null)
            visit.accept(node);
    }

}
