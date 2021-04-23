package analysis;

import pt.up.fe.comp.jmm.JmmNode;
import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.Type;

import java.util.ArrayList;
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

    public static String getMethodCallName(JmmNode node) {
        if (!node.getKind().equals("MethodCall"))
            throw new RuntimeException("Node is not a MethodCall");

        return node.getChildren().get(1).get("methodName");
    }

    public static String getMethodName(JmmNode node) {
        if (!node.getKind().equals("MethodDeclaration"))
            throw new RuntimeException("Node is not a MethodDeclaration");

        if (isMethodMain(node))
            return "main";

        JmmNode sNode = node.getChildren().get(0);
        return sNode.getChildren().get(0).get("name");
    }

    public static List<Symbol> getMethodParams(JmmNode node) {
        if (!node.getKind().equals("MethodDeclaration"))
            throw new RuntimeException("Node is not a MethodDeclaration");

        if (isMethodMain(node))
            throw new RuntimeException("Node is main (unsupported operation)");

        JmmNode header = node.getChildren().get(0);
        List<Symbol> symbols = new ArrayList<>();
        for (int i = 1; i < header.getNumChildren(); i++) {
            symbols.add(AstUtils.getChildSymbol(header, i));
        }

        return symbols;
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
    public static String getUniqueMethodName(JmmNode node, MySymbolTable symbolTable) {
        if (!node.getKind().equals("MethodDeclaration"))
            throw new RuntimeException("Node is not a MethodDeclaration");

        if (isMethodMain(node))
            return "main";

        return symbolTable.getUniqueNameFromSymbols(getMethodName(node), getMethodParams(node));
    }

    public static Type getValueType(JmmNode node, MySymbolTable symbolTable) {
        if (!node.getKind().equals("Value"))
            throw new RuntimeException("Node is not a Value");

        if (!node.get("type").equals(NOT_LITERAL))
            return new Type(node.get("type"), false);

        // Lookup value on symbol table
        return getObjectType(node, symbolTable);
    }

    public static Type getObjectType(JmmNode node, MySymbolTable symbolTable) {
        Optional<JmmNode> ancestorOpt = node.getAncestor("MethodDeclaration");
        if(ancestorOpt.isPresent()) {
            JmmNode ancestor = ancestorOpt.get();
            String uniqueName = AstUtils.getUniqueMethodName(ancestor, symbolTable);

            List<Symbol> localVariables = symbolTable.getLocalVariables(uniqueName);
            List<Symbol> parameters = symbolTable.getParameters(uniqueName);

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
