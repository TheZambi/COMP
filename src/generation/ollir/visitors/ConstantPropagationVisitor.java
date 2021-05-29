package generation.ollir.visitors;

import ast.AstUtils;
import pt.up.fe.comp.jmm.JmmNode;
import pt.up.fe.specs.util.SpecsCheck;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class ConstantPropagationVisitor {

    private final Map<String, Consumer<JmmNode>> visitMap, previsitMap;
    private final Map<String, VarDescriptor> valuesMap;

    private class VarDescriptor{
        String value;
        String type;

        public VarDescriptor(String value, String type){
            this.value = value;
            this.type = type;
        }
    }

    public ConstantPropagationVisitor() {
        this.visitMap = new HashMap<>();
        this.valuesMap = new HashMap<>();

        this.visitMap.put("Statement", this::statementVisit);
        this.visitMap.put("Value", this::valueVisit);

        this.previsitMap = new HashMap<>();

    }

    private void valueVisit(JmmNode node) {
        if (!node.get("type").equals("object"))
            return;
        if (AstUtils.isBeingAssigned(node) || node.getAncestor("IterationStatement").isPresent() || node.getAncestor("Indexing").isPresent())
            return;

        String name = node.get("object");
        VarDescriptor holder = this.valuesMap.get(name);

        if (holder != null) {
            node.put("type", holder.type);
            node.put("object", holder.value);
        }
    }

    private void statementVisit(JmmNode node) {
        if(!AstUtils.isAssignment(node))
            return;

        if(!node.getChildren().get(0).getKind().equals("Value") || !node.getChildren().get(1).getKind().equals("Value"))
            return;

        String name = node.getChildren().get(0).get("object"),
                type = node.getChildren().get(1).get("type");

        switch (type) {
            case "int", "boolean" -> this.valuesMap.put(name, new VarDescriptor(node.getChildren().get(1).get("object"), node.getChildren().get(1).get("type")));
            default -> this.valuesMap.remove(name);
        }
    }

    public void visit(JmmNode node) {
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
}
