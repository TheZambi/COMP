package generation.ollir.visitors;

import analysis.enums.SemBinaryOp;
import analysis.enums.SemUnaryOp;
import ast.AstUtils;
import ast.Method;
import ast.MySymbolTable;
import pt.up.fe.comp.jmm.JmmNode;
import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNodeImpl;
import pt.up.fe.specs.util.SpecsCheck;

import java.util.*;
import java.util.function.Consumer;

public class DeleteUnusedVarVisitor {

    private final Map<String, Consumer<JmmNode>> visitMap;

    private final List<JmmNode> nodesToDelete = new ArrayList<>();
    private String varToDelete;

    public DeleteUnusedVarVisitor() {
        this.visitMap = new HashMap<>();
        this.visitMap.put("Statement", this::statementVisit);
    }

    private void statementVisit(JmmNode node) {
        if (!AstUtils.isAssignment(node))
            return;

        JmmNode child = node.getChildren().get(0);

        if (child.getKind().equals("Value") && child.get("object").equals(varToDelete)) {
            nodesToDelete.add(node);
        }
    }

    private void visit(JmmNode node) {
        SpecsCheck.checkNotNull(node, () -> "Node should not be null");

        Consumer<JmmNode> visit = this.visitMap.get(node.getKind());

        // Preorder: 1st visit each children
        for (var child : node.getChildren())
            visit(child);

        // Preorder: then visit the node
        if (visit != null)
            visit.accept(node);
    }

    public void deleteVar(Method m, String varName, JmmNode methodDeclaration, JmmNode varDeclarationNode) {
        // Remove var declaration from method body
        JmmNodeImpl parent = (JmmNodeImpl) varDeclarationNode.getParent();
        parent.removeChild(varDeclarationNode);

        varToDelete = varName;
        nodesToDelete.clear();

        // Find declarations to delete
        visit(methodDeclaration);

        // Get original type of var being deleted
        Type varType = null;
        for (Symbol sym: m.getLocalVariables()) {
            if (sym.getName().equals(varName)) {
                varType = sym.getType();
                break;
            }
        }
        m.removeLocalVar(varName);

        StatementMethodExtractor extractor = new StatementMethodExtractor();

        for (JmmNode node : nodesToDelete) {
            parent = (JmmNodeImpl) node.getParent();

            // Removes statement from method body
            int index = parent.removeChild(node);

            // Find methods to extract
            List<JmmNode> methodsToInsert = extractor.extract(node);
            for (JmmNode methodNode : methodsToInsert) {

                JmmNodeImpl statementWrapper = new JmmNodeImpl("Statement");
                statementWrapper.put("line", node.get("line"));
                statementWrapper.put("col", node.get("col"));

                statementWrapper.add(methodNode);

                // Add new
                parent.add(statementWrapper, index);
                index += 1;
            }

        }
    }
}
