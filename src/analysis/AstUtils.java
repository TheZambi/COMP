package analysis;

import pt.up.fe.comp.jmm.JmmNode;
import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;

public class AstUtils {
    public static Symbol getChildSymbol(JmmNode node, int index) {
        JmmNode sNode = node.getChildren().get(index);
        if (!sNode.getKind().equals("Symbol"))
            throw new RuntimeException("Child note at index " + index + " is not a Symbol");
        Type t = new Type(sNode.getChildren().get(0).get("type"), sNode.getChildren().get(0).get("array").equals("true"));
        return new Symbol(t, sNode.get("name"));
    }

    public static String getMethodName(JmmNode node) {
        if (!node.getKind().equals("MethodDeclaration"))
            throw new RuntimeException("Node is not a MethodDeclaration");
        JmmNode sNode = node.getChildren().get(0);
        return sNode.getChildren().get(0).get("name");
    }

    public static boolean isMethodMain(JmmNode node) {
        return node.getChildren().get(0).getKind().equals("Main");
    }
}
