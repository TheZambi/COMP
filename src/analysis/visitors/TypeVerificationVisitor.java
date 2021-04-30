package analysis.visitors;

import ast.AstUtils;
import ast.Method;
import ast.MySymbolTable;
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

    public static final Type ignore = new Type("-ignore", false);

    private final Map<String, Function<JmmNode, Type>> visitMap;
    private final Set<String> typesToCheck;

    private final MySymbolTable symbolTable;
    private final List<Report> reports;

    private List<Type> types;

    private final Set<String> validTypes;

    public TypeVerificationVisitor(MySymbolTable symbolTable, List<Report> reports) {
        this.symbolTable = symbolTable;
        this.reports = reports;
        this.visitMap = new HashMap<>();
        this.visitMap.put("BinaryOp", this::binaryOpVisit);
        this.visitMap.put("Statement", this::assignmentVisit);
        this.visitMap.put("Length", this::lengthVisit);
        this.visitMap.put("MethodCall", this::methodCallVisit);
        this.visitMap.put("Value", this::valueVisit);
        this.visitMap.put("Indexing", this::indexingVisit);
        this.visitMap.put("UnaryOp", this::unaryOpVisit);
        this.visitMap.put("ClassObj", this::classObjVisit);
        this.visitMap.put("Array", this::arrayVisit);
        this.visitMap.put("Type", this::typeVisit);

        this.typesToCheck = new HashSet<>();
        this.typesToCheck.add("BinaryOp");
        this.typesToCheck.add("UnaryOp");
        this.typesToCheck.add("MethodCall");
        this.typesToCheck.add("Statement");
        this.typesToCheck.add("Length");
        this.typesToCheck.add("Indexing");
        this.typesToCheck.add("Array");

        this.validTypes = new HashSet<>();
        this.validTypes.add("int");
        this.validTypes.add("boolean");
        this.validTypes.add(symbolTable.getClassName());
        this.validTypes.addAll(symbolTable.getImports());
    }

    private Type typeVisit(JmmNode node) {
        if (!this.validTypes.contains(node.get("type"))) {
            this.reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(node.get("line")), Integer.parseInt(node.get("col")),
                    "Invalid type, found <" + node.get("type") + ">"));
        }

        return null;
    }

    private Type arrayVisit(JmmNode node) {
        if (this.types.size() != 1)
            throw new RuntimeException("Array instantiation without 1 child");

        if (!this.types.get(0).equals(new Type("int", false)))
            this.reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(node.get("line")), Integer.parseInt(node.get("col")),
                    "Array must be instantiated with an integer size, <" + this.types.get(0).getName() + "> received"));

        return new Type("int", true);
    }

    private Type classObjVisit(JmmNode node) {
        if (!node.get("type").equals(AstUtils.NOT_LITERAL)) {
            this.reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(node.get("line")), Integer.parseInt(node.get("col")),
                    "Cannot call new on a <" + node.get("type") + ">"));
            return null;
        }

        return new Type(node.get("classObj"), false);
    }

    private Type valueVisit(JmmNode node) {
        Type t = AstUtils.getValueType(node, symbolTable);
        // Test variables missing from the scope and invalid imports
        if (t == null && !symbolTable.getImports().contains(node.get("object"))) {
            this.reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(node.get("line")), Integer.parseInt(node.get("col")),
                    "Identifier <" + node.get("object") + "> is undeclared in this scope"));
            return ignore;
        } else if (t == null) {
            t = new Type(node.get("object"), false);
        }

        return t;
    }

    private Type methodCallVisit(JmmNode node) {

        Type calledOn = types.get(0);
        // True if callee was ignored already or is unknown (eg: import)
        if (ignore == calledOn)
            return ignore;


        switch (calledOn.getName()) {
            case "int":
            case "boolean":
                this.reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(node.get("line")), Integer.parseInt(node.get("col")),
                        "Cannot call method on primitive data types"));
                return ignore;
            case "this":
                calledOn = new Type(symbolTable.getClassName(), false);
                break;
        }

        if (!calledOn.getName().equals(symbolTable.getClassName())) {
            // Outside method
            return ignore;
        }

        String name = AstUtils.getMethodCallName(node);
        String uniqueName = symbolTable.getUniqueName(name, types.subList(1, types.size()));
        Method method = symbolTable.getMethod(uniqueName);

        if (method == null) {
            if (symbolTable.getSuper() == null) {
                // If the method does not exist
                this.reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(node.get("line")), Integer.parseInt(node.get("col")),
                        "Method <" + name + "> with params " + types.subList(1, types.size()) + " of class <" + calledOn.getName() + "> is undeclared in this scope"));
            }
            return ignore;
        }

        return method.getReturnType();
    }

    private Type unaryOpVisit(JmmNode node) {
        if (this.types.size() != 1)
            throw new RuntimeException("Unary operation without 1 child");

        Type ret;
        switch (UnaryOperator.valueOf(node.get("op"))) {
            case NEG:
                ret = new Type("boolean", false);
                if (ignore != this.types.get(0) && !this.types.get(0).equals(ret))
                    this.reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(node.get("line")), Integer.parseInt(node.get("col")),
                            "Can only negate a boolean, received <" + this.types.get(0).getName() + "> instead"));
                break;
            case NEW:
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

        if (ignore != this.types.get(0) && !this.types.get(0).isArray())
            this.reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(node.get("line")), Integer.parseInt(node.get("col")),
                    "Can only index an array, <" + this.types.get(0).getName() + "> is not an array"));
        if (ignore != this.types.get(1) && !this.types.get(1).equals(new Type("int", false)))
            this.reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(node.get("line")), Integer.parseInt(node.get("col")),
                    "Indexing index must be an int, was <" + this.types.get(1).getName() + ">"));

        return new Type("int", false);
    }

    private Type lengthVisit(JmmNode node) {
        if (ignore != this.types.get(0) && !this.types.get(0).isArray())
            this.reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, -1, -1, "length must only be used on arrays: " + this.types.get(0) + " is not an array"));
        return new Type("int", false);
    }

    private Type assignmentVisit(JmmNode node) {
        if (!AstUtils.isAssignment(node))
            return null;

        if (this.types.size() != 2)
            throw new RuntimeException("Assignment without 2 children");

        if (!(ignore == this.types.get(0) || ignore == this.types.get(1))) {
            // Check types
            if (!this.types.get(0).equals(this.types.get(1)))
                this.reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(node.get("line")), Integer.parseInt(node.get("col")),
                        "Assignment types must match, tried to assign a <" + this.types.get(0).getName() + "> to a <" + this.types.get(1).getName() + ">"));
        }

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

        if (ignore != left && !leftExpected.equals(left)) {
            this.reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(node.get("line")), Integer.parseInt(node.get("col")),
                    node.get("op") + " left operand type must be " + leftExpected.getName() + " but was <" + left.getName() + ">"));
        } else if (ignore != right && !rightExpected.equals(right)) {
            this.reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(node.get("line")), Integer.parseInt(node.get("col")),
                    node.get("op") + " right operand type must be " + rightExpected.getName() + " but was <" + right.getName() + ">"));
        }
        return ret;
    }

    public Type visit(JmmNode node) {
        SpecsCheck.checkNotNull(node, () -> "Node should not be null");

        List<Type> childList = null, oldList = null;
        if (this.typesToCheck.contains(node.getKind())) {
            childList = new ArrayList<>();
            oldList = this.types;
            this.types = childList;
        }

        Type res = null;
        // Postorder: 1st visit each children
        for (JmmNode child : node.getChildren()) {
            res = visit(child);
            if (this.types != null && res != null) {
                this.types.add(res);
                res = null;
            }
        }

        // Postorder: then, visit the node
        Function<JmmNode, Type> visit = this.visitMap.get(node.getKind());
        if (visit != null)
            res = visit.apply(node);


        if (oldList != null)
            this.types = oldList;

        return res;
    }
}
