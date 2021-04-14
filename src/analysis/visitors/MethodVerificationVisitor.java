package analysis.visitors;

import analysis.MySymbolTable;
import pt.up.fe.comp.jmm.JmmNode;
import pt.up.fe.specs.util.SpecsCheck;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class MethodVerificationVisitor {
    private final Map<String, Consumer<JmmNode>> visitMap;

    private final MySymbolTable symbolTable;

    public MethodVerificationVisitor(MySymbolTable symbolTable) {
        this.symbolTable = symbolTable;
        this.visitMap = new HashMap<>();
//        this.visitMap.put();
    }

    private void arrayAccessVerification(JmmNode node) {

    }

    public void visit(JmmNode jmmNode) {
        SpecsCheck.checkNotNull(jmmNode, () -> "Node should not be null");

        Consumer<JmmNode> visit = this.visitMap.get(jmmNode.getKind());

        // Postorder: first, visit the children
        for (var child : jmmNode.getChildren())
            visit(child);

        // Postorder: then visit the 1st node
        if (visit != null)
            visit.accept(jmmNode);
    }
}
