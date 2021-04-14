package analysis.visitors;

import analysis.AstUtils;
import analysis.MySymbolTable;
import pt.up.fe.comp.jmm.JmmNode;
import pt.up.fe.comp.jmm.analysis.Method;
import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.specs.util.SpecsCheck;

import java.util.*;
import java.util.function.Consumer;

public class SymbolTableVisitor {

    private final Map<String, Consumer<JmmNode>> visitMap;

    private final MySymbolTable symbolTable;

    public SymbolTableVisitor(MySymbolTable symbolTable) {
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
            // TODO: Handle main :)
            if (AstUtils.isMethodMain(ancestor))
                return;

            Method m = symbolTable.getMethod(AstUtils.getMethodName(ancestor));
            m.addLocalVar(s);
        } else {
            // Class field
            symbolTable.addField(s);
        }
    }

    private void methodDeclarationVisit(JmmNode node) {
        // TODO: Handle main :)
        if (AstUtils.isMethodMain(node))
            return;

        JmmNode header = node.getChildren().get(0);
        List<Symbol> symbols = new ArrayList<>();
        for (int i = 1; i < header.getNumChildren(); i++) {
            symbols.add(AstUtils.getChildSymbol(header, i));
        }
        symbolTable.addMethod(new Method(AstUtils.getChildSymbol(header, 0), symbols));
    }

    private void classDeclarationVisit(JmmNode node) {
        symbolTable.setClassName(node.get("name"));
        if(node.getAttributes().contains("extends"))
            symbolTable.setSuperClass(node.get("extends"));
    }

    private void importDeclarationVisit(JmmNode node) {
        symbolTable.addImport(node.get("import"));
    }

    public void visit(JmmNode jmmNode) {
        SpecsCheck.checkNotNull(jmmNode, () -> "Node should not be null");

        Consumer<JmmNode> visit = this.visitMap.get(jmmNode.getKind());

        // Preorder: 1st visit the node
        if (visit != null)
            visit.accept(jmmNode);

        // Preorder: then, visit each children
        for (var child : jmmNode.getChildren())
            visit(child);
    }

}
