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

    public static Type getNodeType(JmmNode node, MySymbolTable symbolTable) {

        switch (node.getKind()) {
            case "Value":
                if (node.get("varType").equals("object")) {
                    Type objectType = AstUtils.getObjectType(node, symbolTable);
                    if (objectType == null) {
                        //TODO: add to reports - uninitialized variable
                        return null;
                    }
                    return objectType;
                }
                return new Type(node.get("varType"), false);

            case "MethodCall":
                return AstUtils.getMethodCallType(node, symbolTable);

            case "BinaryOp":
                Type type0 = AstUtils.getNodeType(node.getChildren().get(0), symbolTable);
                Type type1 = AstUtils.getNodeType(node.getChildren().get(1), symbolTable);

                if (type0 == null || type1 == null) {
                    return null;
                }
                if (type0.equals(type1) || type0.getName().equals("") || type1.getName().equals(""))
                    if(type0.getName().equals(""))
                        return type1;
                    else
                        return type0;
                else {
                    //TODO: incompatible types error - add to report
                    return null;
                }
            case "UnaryOp":
                if (node.get("op").equals("NEW")) {
                    JmmNode unaryChild = node.getChildren().get(0);
                    if (unaryChild.getKind().equals("ClassObj"))
                        return new Type(unaryChild.get("classObj"), false);
                    else
                        return new Type("int", true);
                } else {
                    Type unaryChildType = AstUtils.getNodeType(node.getChildren().get(0), symbolTable);
                    if (unaryChildType != null && !unaryChildType.getName().equals("Boolean")) {
                        //TODO: NEGATE ONLY BOOLEANS - add to report
                        return null;
                    } else
                        return unaryChildType;
                }
            default:
                break;
        }
        return null;
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
