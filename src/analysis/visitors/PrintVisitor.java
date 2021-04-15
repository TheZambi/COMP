package analysis.visitors;

import pt.up.fe.comp.jmm.JmmNode;
import pt.up.fe.specs.util.SpecsCheck;

import java.util.Set;

public class PrintVisitor {

    private final Set<String> toPrint;

    public PrintVisitor(Set<String> toPrint) {
        this.toPrint = toPrint;
    }

    public void visit(JmmNode node) {
        SpecsCheck.checkNotNull(node, () -> "Node should not be null");

        // Preorder: 1st visit the node
        if (this.toPrint.contains(node.getKind())) {
            System.out.println(node);
            System.out.println(node.getChildren());
        }

        // Preorder: then, visit each children
        for (var child : node.getChildren())
            visit(child);
    }

}
