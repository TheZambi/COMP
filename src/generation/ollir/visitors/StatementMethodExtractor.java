package generation.ollir.visitors;

import ast.AstUtils;
import ast.Method;
import pt.up.fe.comp.jmm.JmmNode;
import pt.up.fe.comp.jmm.ast.JmmNodeImpl;
import pt.up.fe.specs.util.SpecsCheck;

import java.util.ArrayList;
import java.util.List;

public class StatementMethodExtractor {

    private final List<JmmNode> nodesToExtract = new ArrayList<>();

    private void visit(JmmNode node) {
        SpecsCheck.checkNotNull(node, () -> "Node should not be null");

        if (node.getKind().equals("MethodCall")) {
            this.nodesToExtract.add(node);
            return;
        }

        for (var child : node.getChildren())
            visit(child);
    }

    public List<JmmNode> extract(JmmNode node) {
        nodesToExtract.clear();
        visit(node);
        return nodesToExtract;
    }

}
