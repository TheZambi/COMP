package analysis;

import pt.up.fe.comp.jmm.JmmNode;
import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.Type;

import java.util.List;
import java.util.Optional;

public class AstUtils {

    public static String NOT_LITERAL = "object";

    public static Symbol getChildSymbol(JmmNode node, int index) {
        JmmNode sNode = node.getChildren().get(index);
        if (!sNode.getKind().equals("Symbol"))
            throw new RuntimeException("Child note at index " + index + " is not a Symbol");
        return new Symbol(getChildType(sNode, 0), sNode.get("name"));
    }

    public static Type getChildType(JmmNode node, int index) {
        return getType(node.getChildren().get(index));
    }

    public static Type getType(JmmNode node) {
        return new Type(node.get("type"), node.get("array").equals("true"));
    }

    public static String getMethodName(JmmNode node) {
        if (!node.getKind().equals("MethodDeclaration"))
            throw new RuntimeException("Node is not a MethodDeclaration");

        if (isMethodMain(node))
            return "main";

        JmmNode sNode = node.getChildren().get(0);
        return sNode.getChildren().get(0).get("name");
    }

    public static boolean isMethodMain(JmmNode node) {
        return node.getChildren().get(0).getKind().equals("Main");
    }

    public static boolean isAssignment(JmmNode node) {
        if (!node.getKind().equals("Statement"))
            throw new RuntimeException("Node is not a Statement");
        return node.getNumChildren() == 2;
    }



    // POST SYMBOL TABLE
    public static Type getMethodCallType(JmmNode node, MySymbolTable symbolTable) {
        if(!node.getKind().equals("MethodCall"))
            throw new RuntimeException("Node is not a MethodCall");

        JmmNode objectNode = node.getChildren().get(0);
        String methodName = node.getChildren().get(1).get("methodName");

        if(objectNode.getKind().equals("Value") && objectNode.get("varType").equals("this")) {
            if(symbolTable.getMethods().contains(methodName)) {
                return symbolTable.getReturnType(methodName);
            } else {
                //TODO: error report - invalid method on 'this'
                return null;
            }
        } else if(objectNode.getKind().equals("Value") && objectNode.get("varType").equals("object")){
            return new Type("", false);

        } else {
            //TODO: erro report - cant call method on non objects
            return null;
        }

    }

    public static Type getValueType(JmmNode node, SymbolTable symbolTable) {
        if (!node.getKind().equals("Value"))
            throw new RuntimeException("Node is not a Value");

        if (!node.get("type").equals(NOT_LITERAL))
            return new Type(node.get("type"), false);

        // Lookup value on symbol table
        return getObjectType(node, symbolTable);
    }

    public static Type getObjectType(JmmNode node, SymbolTable symbolTable) {
        Optional<JmmNode> ancestorOpt = node.getAncestor("MethodDeclaration");
        if(ancestorOpt.isPresent()) {
            JmmNode ancestor = ancestorOpt.get();
            String methodName = AstUtils.getMethodName(ancestor);

            List<Symbol> localVariables = symbolTable.getLocalVariables(methodName);
            List<Symbol> parameters = symbolTable.getParameters(methodName);

            for(Symbol s : parameters) {
                if (s.getName().equals(node.get("object"))) {
                    return s.getType();
                }
            }

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
