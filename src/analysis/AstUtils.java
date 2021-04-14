package analysis;

import pt.up.fe.comp.jmm.JmmNode;
import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;

import java.util.List;
import java.util.Optional;

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

    public static Type getChildType(JmmNode node, int index, MySymbolTable symbolTable) {
        JmmNode child = node.getChildren().get(index);

        if(child.getKind().equals("Value") && child.get("varType") != null) {
            if(child.get("varType").equals("object")) {
                Type objectType = AstUtils.getObjectType(child, symbolTable);
                if(objectType == null) {
                    //TODO: add to reports - uninitialized variable
                    return null;
                }
                return objectType;
            }
            return new Type(child.get("varType"), false);

        } else if(child.getKind().equals("Value")) {

//            AstUtils.getChildType(child.getChildren().get(0), )
        }
    }

    public static Type getObjectType(JmmNode node, MySymbolTable symbolTable) {
        Optional<JmmNode> ancestorOpt = node.getAncestor("MethodDeclaration");
        if(ancestorOpt.isPresent()) {
            JmmNode ancestor = ancestorOpt.get();
            List<Symbol> localVariables = symbolTable.getLocalVariables(AstUtils.getMethodName(ancestor));

            for(Symbol s : localVariables) {
                if(s.getName().equals(node.get("object"))) {
                    return s.getType();
                }
            }

            for(Symbol s : symbolTable.getFields()) {
                if(s.getName().equals(node.get("object"))) {
                    return s.getType();
                }
            }
        }
        return null;
    }
}
