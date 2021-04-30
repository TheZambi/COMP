package analysis.visitors;

import ast.AstUtils;
import ast.MySymbolTable;
import pt.up.fe.comp.jmm.JmmNode;
import ast.Method;
import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.ReportType;
import pt.up.fe.comp.jmm.report.Stage;
import pt.up.fe.specs.util.SpecsCheck;

import java.util.*;
import java.util.function.Consumer;

public class SymbolTableVisitor {

    private final Map<String, Consumer<JmmNode>> visitMap;

    private final List<Report> reports;

    private final MySymbolTable symbolTable;

    public SymbolTableVisitor(MySymbolTable symbolTable, List<Report> reports) {
        this.reports = reports;

        this.symbolTable = symbolTable;
        this.visitMap = new HashMap<>();
        this.visitMap.put("VarDeclaration", this::varDeclarationVisit);
        this.visitMap.put("MethodDeclaration", this::methodDeclarationVisit);
        this.visitMap.put("ClassDeclaration", this::classDeclarationVisit);
        this.visitMap.put("ImportDeclaration", this::importDeclarationVisit);
    }

    private void varDeclarationVisit(JmmNode node) {
        Optional<JmmNode> ancestorOpt = node.getAncestor("MethodDeclaration");
        Symbol s = AstUtils.getChildSymbol(node, 0);
        if (ancestorOpt.isPresent()) {
            // Method Local var
            JmmNode ancestor = ancestorOpt.get();
            Method m;
            if (AstUtils.isMethodMain(ancestor))
                m = symbolTable.getMethod("main");
            else
                m = symbolTable.getMethod(AstUtils.getUniqueMethodName(ancestor, symbolTable));

            if (!m.addLocalVar(s))
                reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(node.get("line")), Integer.parseInt(node.get("col")),
                        "Method defines multiple times the same variable with the name <" + s.getName() + ">"));
        } else {
            // Class field
            if (!symbolTable.addField(s))
                reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(node.get("line")), Integer.parseInt(node.get("col")),
                        "Class has multiple fields with the same name <" + s.getName() + ">"));
        }
    }

    private void methodDeclarationVisit(JmmNode node) {
        if (AstUtils.isMethodMain(node)) {
            List<Symbol> symbols = new ArrayList<>();
            symbols.add(new Symbol(new Type("String",false), node.getChildren().get(0).get("stringArrayName")));
            if (!symbolTable.addMethod(new Method("main", new Type("void", false), symbols)))
                reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(node.get("line")), Integer.parseInt(node.get("col")),
                    "A class cannot have multiple main declarations"));
            return;
        }

        List<Symbol> symbols = AstUtils.getMethodParams(node);
        Symbol methodHead = AstUtils.getChildSymbol(node.getChildren().get(0), 0);
        Method m = new Method(methodHead, symbols);
        if (!symbolTable.addMethod(m))
            reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(node.get("line")), Integer.parseInt(node.get("col")),
                    "Found multiple declarations of method with the same prototype: <" + m.getName() + "> with params <" + m.getParameters() + ">"));
    }

    private void classDeclarationVisit(JmmNode node) {
        symbolTable.setClassName(node.get("name"));
        if(node.getAttributes().contains("extends"))
            symbolTable.setSuperClass(node.get("extends"));
    }

    private void importDeclarationVisit(JmmNode node) {
        symbolTable.addImport(node.get("import"));
    }

    public void visit(JmmNode node) {
        SpecsCheck.checkNotNull(node, () -> "Node should not be null");

        Consumer<JmmNode> visit = this.visitMap.get(node.getKind());

        // Preorder: 1st visit the node
        if (visit != null)
            visit.accept(node);

        // Preorder: then, visit each children
        for (var child : node.getChildren())
            visit(child);
    }

}
