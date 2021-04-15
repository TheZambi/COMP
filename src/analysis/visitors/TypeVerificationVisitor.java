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
import java.util.function.Function;

public class TypeVerificationVisitor {
    private final Map<String, Function<JmmNode, Type>> visitMap;
    private final Set<String> typesToCheck = new HashSet<>();

    private final MySymbolTable symbolTable;

    private List<Type> types;

    public TypeVerificationVisitor(MySymbolTable symbolTable) {
        this.symbolTable = symbolTable;
        this.visitMap = new HashMap<>();
        this.visitMap.put("BinaryOp", this::binaryOpVerification);
        this.visitMap.put("AssignmentStatement", this::binaryOpVerification);
        this.visitMap.put("Length", this::lengthVisitor);
        this.visitMap.put("MethodCall", this::methodVisit);
        this.visitMap.put("Value", this::valueVisit);

        this.typesToCheck.add("BinaryOp");
        this.typesToCheck.add("UnaryOp");
        this.typesToCheck.add("MethodCall");
        this.typesToCheck.add("AssignmentStatement");
        this.typesToCheck.add("Length");
    }

    private Type valueVisit(JmmNode node) {
        
        return new Type(node.get("type"), false);
    }

    private Type methodVisit(JmmNode node) {
//        System.out.println(this.types);
//        if (this.types.size() != 2) {
//            System.out.printf("Children of %s\n\t", node.getKind());
//            System.out.println(node.getChildren());
//        }
        return new Type("int", false);
    }

    private Type lengthVisitor(JmmNode node) {
        return new Type("int", false);
    }

    private Type binaryOpVerification(JmmNode node) {
        System.out.println(this.types);
        if (this.types.size() != 2) {
            System.out.printf("Children of %s\n\t", node.getKind());
            System.out.println(node.getChildren());
        }
        return this.types.get(0);
    }

    public Type visit(JmmNode node) {
        SpecsCheck.checkNotNull(node, () -> "Node should not be null");

        Function<JmmNode, Type> visit = this.visitMap.get(node.getKind());

        List<Type> childList = null, oldList = null;
        if (this.typesToCheck.contains(node.getKind())) {
            childList = new ArrayList<>();
            oldList = this.types;
            this.types = childList;
        }

        Type res = null;

        // Postorder: 1st visit each children
        for (var child : node.getChildren()) {
            res = visit(child);
            if (res != null)
                this.types.add(res);
        }

        // Postorder: then, visit the node
        if (visit != null)
            res = visit.apply(node);


        if (oldList != null)
            this.types = oldList;

        return res;
    }
}
