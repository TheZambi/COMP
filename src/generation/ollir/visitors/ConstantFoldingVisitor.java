package generation.ollir.visitors;

import ast.AstUtils;
import pt.up.fe.comp.jmm.JmmNode;
import pt.up.fe.comp.jmm.ast.JmmNodeImpl;
import pt.up.fe.specs.util.SpecsCheck;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class ConstantFoldingVisitor {

    private class SimplificationHolder {
        public JmmNode node;
        public Object value;
        public int childIndex;

        public SimplificationHolder(JmmNode node, Object value, int childIndex) {
            this.node = node;
            this.value = value;
            this.childIndex = childIndex;
        }

        private JmmNode createValueNode() {
            JmmNode res = new JmmNodeImpl("Value");
            res.put("col", "-1");
            res.put("line", "-1");
            if (value instanceof Integer) {
                res.put("type", "int");
                res.put("object", ((Integer) value).toString());
            } else if (value instanceof Boolean) {
                res.put("type", "boolean");
                res.put("object", ((Boolean) value).toString());
            } else {
                return null;
            }

            return res;
        }

        public void simplify() {
            JmmNode newNode = createValueNode();
            if (newNode == null)
                return;

            node.removeChild(childIndex);
            node.add(newNode, childIndex);
        }
    }

    private final Map<String, Consumer<JmmNode>> visitMap, previsitMap;
    private final List<SimplificationHolder> toSimplify = new ArrayList<>();
    private boolean dirty;

    public ConstantFoldingVisitor() {
        this.visitMap = new HashMap<>();
        this.visitMap.put("Statement", this::statementVisit);
        this.visitMap.put("Return", this::checkForSimplification);
        this.visitMap.put("SelectionStatement", this::checkForSimplification);
        this.visitMap.put("IterationStatement", this::checkForSimplification);
        this.visitMap.put("MethodDeclaration", this::methodDeclarationVisit);

        this.previsitMap = new HashMap<>();
        this.previsitMap.put("MethodDeclaration", this::methodDeclarationPreVisit);
    }

    private boolean canBeSimplified(JmmNode node, int childIndex) {
        return node.getChildren().get(childIndex).getNumChildren() > 0;
    }

    private void checkForSimplification(JmmNode node) {
        checkForSimplification(node, 0);
    }

    private void checkForSimplification(JmmNode node, int childIndex) {
        if (!canBeSimplified(node, childIndex))
            return;

        Object expressionResult = ConstExpressionEvaluator.evaluate(node);
        if (expressionResult == null)
            return;

        this.toSimplify.add(new SimplificationHolder(node, expressionResult, childIndex));
    }

    private void statementVisit(JmmNode node) {
        int childIndex = AstUtils.isAssignment(node) ? 1 : 0;
        checkForSimplification(node, childIndex);
    }

    private void methodDeclarationPreVisit(JmmNode node) {
        this.toSimplify.clear();
    }

    private void methodDeclarationVisit(JmmNode node) {
        if (!this.toSimplify.isEmpty())
            this.dirty = true;
        for (SimplificationHolder sh : this.toSimplify)
            sh.simplify();
    }

    private void visit(JmmNode node) {
        SpecsCheck.checkNotNull(node, () -> "Node should not be null");

        Consumer<JmmNode> visit = this.visitMap.get(node.getKind()),
            preVisit = this.previsitMap.get(node.getKind());

        if (preVisit != null)
            preVisit.accept(node);

        // Preorder: 1st visit each children
        for (var child : node.getChildren())
            visit(child);

        // Preorder: then visit the node
        if (visit != null)
            visit.accept(node);
    }

    public boolean fold(JmmNode node) {
        dirty = false;
        visit(node);
        return dirty;
    }
}
