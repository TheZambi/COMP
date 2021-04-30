package ast;

import pt.up.fe.comp.jmm.JmmNode;
import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.specs.util.SpecsCheck;

import java.util.*;
import java.util.function.Function;

public class MethodGetter {

    private enum BiOperator {
        AND,
        LESSER,
        ADD,
        SUBTRACT,
        MULTIPLY,
        DIVIDE
    }

    private enum UnaryOperator {
        NEG,
        NEW
    }

    private final Map<String, Function<JmmNode, Type>> visitMap;

    private final MySymbolTable symbolTable;

    public MethodGetter(MySymbolTable symbolTable) {
        this.symbolTable = symbolTable;
        this.visitMap = new HashMap<>();
        this.visitMap.put("BinaryOp", this::binaryOpVisit);
        this.visitMap.put("Length", this::lengthVisit);
        this.visitMap.put("MethodCall", this::methodCallVisit);
        this.visitMap.put("Value", this::valueVisit);
        this.visitMap.put("Indexing", this::indexingVisit);
        this.visitMap.put("UnaryOp", this::unaryOpVisit);
        this.visitMap.put("ClassObj", this::classObjVisit);
        this.visitMap.put("Array", this::arrayVisit);
    }

    private Type arrayVisit(JmmNode node) {
        return new Type("int", true);
    }

    private Type classObjVisit(JmmNode node) {
        return new Type(node.get("classObj"), false);
    }

    private Type valueVisit(JmmNode node) {
        Type t = AstUtils.getValueType(node, symbolTable);
        // Test variables missing from the scope and invalid imports
        if (t == null && !symbolTable.getImports().contains(node.get("object"))) {
            return null;
        } else if (t == null) {
            t = new Type(node.get("object"), false);
        }

        return t;
    }

    private Type methodCallVisit(JmmNode node) {
        Method m = this.getMethodFromMethod(node.getChildren().get(1));
        return m == null ? null : m.getReturnType();
    }

    private Type unaryOpVisit(JmmNode node) {
        switch (UnaryOperator.valueOf(node.get("op"))) {
            case NEG:
                return new Type("boolean", false);
            case NEW:
                JmmNode child = node.getChildren().get(0);
                if (child.getKind().equals("Array"))
                    return new Type("int", true);
                else
                    return new Type(child.get("classObj"), false);
            default:
                return null;
        }
    }

    private Type indexingVisit(JmmNode node) {
        return new Type("int", false);
    }

    private Type lengthVisit(JmmNode node) {
       return new Type("int", false);
    }

    private Type binaryOpVisit(JmmNode node) {
        switch (BiOperator.valueOf(node.get("op"))) {
            case AND:
            case LESSER:
                return new Type("boolean", false);
            case ADD:
            case SUBTRACT:
            case MULTIPLY:
            case DIVIDE:
                return new Type("int", false);
            default:
                throw new RuntimeException("Unsupported binary operation");
        }
    }

    public Method getMethodFromMethodDeclaration(JmmNode node) {
        SpecsCheck.checkNotNull(node, () -> "Node should not be null");

        if (!node.getKind().equals("MethodDeclaration"))
            throw new RuntimeException("Node is not a MethodDeclaration: <" + node.getKind() + ">");

        if (AstUtils.isMethodMain(node))
            return symbolTable.getMethod("main");

        return getMethodFromMethodHeader(node.getChildren().get(0));
    }

    public Method getMethodFromMethodHeader(JmmNode node) {
        SpecsCheck.checkNotNull(node, () -> "Node should not be null");

        if (!node.getKind().equals("MethodHeader"))
            throw new RuntimeException("Node is not a MethodHeader: <" + node.getKind() + ">");

        String name = AstUtils.getChildSymbol(node, 0).getName();
        List<Type> args = AstUtils.reduceSymbolsToTypes(AstUtils.getMethodParams(node.getParent()));

        return symbolTable.getMethod(symbolTable.getUniqueName(name, args));
    }

    public Method getMethodFromMethod(JmmNode node) {
        SpecsCheck.checkNotNull(node, () -> "Node should not be null");

        if (!node.getKind().equals("Method"))
            throw new RuntimeException("Node is not a Method: <" + node.getKind() + ">");

        String name = node.get("methodName");

        List<Type> args = new ArrayList<>();
        Function<JmmNode, Type> visit = null;

        // Get types of args (if any)
        for (JmmNode child : node.getChildren()) {
            visit = this.visitMap.get(child.getKind());
            if (visit != null)
                args.add(visit.apply(child));
        }

        return symbolTable.getMethod(symbolTable.getUniqueName(name, args));
    }
}
