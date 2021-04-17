package analysis.visitors;

import analysis.AstUtils;
import analysis.MySymbolTable;
import pt.up.fe.comp.jmm.JmmNode;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.ReportType;
import pt.up.fe.comp.jmm.report.Stage;
import pt.up.fe.specs.util.SpecsCheck;

import java.util.*;
import java.util.function.Function;

public class TypeVerificationVisitor {

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
    private final Set<String> typesToCheck = new HashSet<>();

    private final MySymbolTable symbolTable;
    private final List<Report> reports;

    private List<Type> types;

    public TypeVerificationVisitor(MySymbolTable symbolTable, List<Report> reports) {
        this.symbolTable = symbolTable;
        this.reports = reports;
        this.visitMap = new HashMap<>();
        this.visitMap.put("BinaryOp", this::binaryOpVisit);
        this.visitMap.put("Statement", this::assignmentVisit);
        this.visitMap.put("Length", this::lengthVisit);
        this.visitMap.put("MethodCall", this::methodVisit);
        this.visitMap.put("Value", this::valueVisit);
        this.visitMap.put("Indexing", this::indexingVisit);
        this.visitMap.put("UnaryOp", this::unaryOpVisit);
        this.visitMap.put("ClassObj", this::classObjVisit);
        this.visitMap.put("Array", this::arrayVisit);

        this.typesToCheck.add("BinaryOp");
        this.typesToCheck.add("UnaryOp");
        this.typesToCheck.add("MethodCall");
        this.typesToCheck.add("Statement");
        this.typesToCheck.add("Length");
        this.typesToCheck.add("Indexing");
        this.typesToCheck.add("Array");
    }

    private Type arrayVisit(JmmNode node) {
        if (this.types.size() != 1)
            throw new RuntimeException("Array instantiation without 1 child");

        if (!this.types.get(0).equals(new Type("int", false)))
            this.reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, -1, -1,
                    "Array must be instantiated with an integer size, <" + this.types.get(0).getName() + "> received"));

        return new Type("int", true);
    }

    private Type classObjVisit(JmmNode node) {
        if (!node.get("type").equals(AstUtils.NOT_LITERAL)) {
            this.reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, -1, -1,
                    "Cannot call new on a <" + node.get("type") + ">"));
            return null;
        }

        return new Type(node.get("classObj"), false);
    }

    private Type valueVisit(JmmNode node) {
        return AstUtils.getValueType(node, symbolTable);
    }

    private Type methodVisit(JmmNode node) {
//        System.out.println(this.types);
//        if (this.types.size() != 2) {
//            System.out.printf("Children of %s\n\t", node.getKind());
//            System.out.println(node.getChildren());
//        }
        return new Type("int", false);
    }

    private Type unaryOpVisit(JmmNode node) {
        if (this.types.size() != 1)
            throw new RuntimeException("Unary operation without 1 child");

        Type ret;
        switch (UnaryOperator.valueOf(node.get("op"))) {
            case NEG:
                ret = new Type("boolean", false);
                if (!this.types.get(0).equals(ret))
                    this.reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, -1, -1,
                            "Can only negate a boolean, received <" + this.types.get(0).getName() + "> instead"));
                break;
            case NEW:
                // TODO: Check constructors?
                ret = this.types.get(0);
                break;
            default:
                throw new RuntimeException("Unsupported unary operation");
        }
        return ret;
    }

    private Type indexingVisit(JmmNode node) {
        if (this.types.size() != 2)
            throw new RuntimeException("Indexing without 2 children");

        if (!this.types.get(0).isArray())
            this.reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, -1, -1,
                    "Can only index an array, <" + this.types.get(0).getName() + "> is not an array"));
        if (!this.types.get(1).equals(new Type("int", false)))
            this.reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, -1, -1,
                    "Indexing index must be an int, was <" + this.types.get(1).getName() + ">"));

        return new Type("int", false);
    }

    private Type lengthVisit(JmmNode node) {
        if (!this.types.get(0).isArray())
            this.reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, -1, -1, "length must only be used on arrays: " + this.types.get(0) + " is not an array"));
        return new Type("int", false);
    }

    private Type assignmentVisit(JmmNode node) {
        if (!AstUtils.isAssignment(node))
            return null;

        if (this.types.size() != 2)
            throw new RuntimeException("Assignment without 2 children");

        if (!this.types.get(0).equals(this.types.get(1)))
            this.reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, -1, -1,
                    "Assignment types must match, tried to assign a <" + this.types.get(0).getName() + "> to a <" + this.types.get(1).getName() + ">"));

        return null;
    }

    private Type binaryOpVisit(JmmNode node) {
        if (this.types.size() != 2)
            throw new RuntimeException("Binary Operation without 2 children");

        Type left = this.types.get(0), right = this.types.get(1);
        Type leftExpected, rightExpected, ret;
        switch (BiOperator.valueOf(node.get("op"))) {
            case AND:
                leftExpected = new Type("boolean", false);
                rightExpected = new Type("boolean", false);
                ret = new Type("boolean", false);
                break;
            case LESSER:
                leftExpected = new Type("int", false);
                rightExpected = new Type("int", false);
                ret = new Type("boolean", false);
                break;
            case ADD:
            case SUBTRACT:
            case MULTIPLY:
            case DIVIDE:
                leftExpected = new Type("int", false);
                rightExpected = new Type("int", false);
                ret = new Type("int", false);
                break;
            default:
                throw new RuntimeException("Unsupported binary operation");
        }

        if (!leftExpected.equals(left)) {
            this.reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, -1, -1,
                    "&& left operand type must be boolean but was <" + left.getName() + ">"));
        } else if (!rightExpected.equals(right)) {
            this.reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, -1, -1,
                    "&& right operand type must be boolean but was <" + right.getName() + ">"));
        }
        return ret;
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
