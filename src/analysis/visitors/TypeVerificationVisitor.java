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

public class TypeVerificationVisitor {
    private final Map<String, Consumer<JmmNode>> visitMap;

    private final MySymbolTable symbolTable;

    public TypeVerificationVisitor(MySymbolTable symbolTable) {
        this.symbolTable = symbolTable;
        this.visitMap = new HashMap<>();
        this.visitMap.put("BinaryOp", this::binaryOpVerification);
    }

    private void binaryOpVerification(JmmNode node) {
        JmmNode child0 = node.getChildren().get(0);
        JmmNode child1 = node.getChildren().get(1);


        if(child0.getKind().equals("Value"))

        switch(node.get("op")) {
            case "ADD":

                break;
            case "SUBTRACT":
                break;
            case "AND":
                break;
            case "LESSER":
                break;
            case "MULTIPLY":
                break;
            case "DIVIDE":
                break;
            default:
                break;
        }
    }

    private void arrayAccessVerification(JmmNode node) {

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
