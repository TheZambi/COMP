package analysis.visitors;

import analysis.enums.SemBinaryOp;
import analysis.enums.SemUnaryOp;
import ast.AstUtils;
import ast.Method;
import ast.MySymbolTable;
import pt.up.fe.comp.jmm.JmmNode;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.ReportType;
import pt.up.fe.comp.jmm.report.Stage;
import pt.up.fe.specs.util.SpecsCheck;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class MethodReturnExtractor {

    private final Map<String, Consumer<JmmNode>> visitMap;

    private final List<Report> reports;
    private final MySymbolTable symbolTable;

    public MethodReturnExtractor(MySymbolTable symbolTable, List<Report> reports) {
        this.reports = reports;
        this.symbolTable = symbolTable;

        this.visitMap = new HashMap<>();
        this.visitMap.put("MethodCall", this::methodCallVisit);
    }

    private void methodCallVisit(JmmNode methodNode) {

        Type retType;

        try {
            Method m = symbolTable.getMethod(methodNode.get("uniqueName"));
            retType = m.getReturnType();
        } catch (NullPointerException ignored) {
            retType = getRetType(methodNode, symbolTable);
        }

        if (retType != null) {
            methodNode.put("ret_type", retType.getName());
            methodNode.put("ret_type_array", retType.isArray() ? "true" : "false");
        }
    }

    public void visit(JmmNode node) {
        SpecsCheck.checkNotNull(node, () -> "Node should not be null");

        Consumer<JmmNode> visit = this.visitMap.get(node.getKind());

        // Preorder: 1st visit each children
        for (var child : node.getChildren())
            visit(child);

        // Preorder: then visit the node
        if (visit != null)
            visit.accept(node);
    }


    private Type getRetType(JmmNode methodCallNode, MySymbolTable symbolTable) {

        JmmNode parent = methodCallNode.getParent();
        Type ret = switch (parent.getKind()) {
            case "BinaryOp" -> switch (SemBinaryOp.valueOf(parent.get("op"))) {
                case ADD, SUBTRACT, MULTIPLY, DIVIDE, LESSER -> new Type("int", false);
                case AND -> new Type("boolean", false);
            };

            case "UnaryOp" -> switch (SemUnaryOp.valueOf(parent.get("op"))) {
                case NEG -> new Type("boolean", false);
                case NEW -> null;
            };

            case "Statement" -> {
                if (!AstUtils.isAssignment(parent))
                    yield null;

                JmmNode beingAttributed = parent.getChildren().get(0);

                yield switch (beingAttributed.getKind()) {
                    case "Value" -> AstUtils.getObjectType(beingAttributed, symbolTable);
                    case "Indexing" -> new Type("int", false);
                    default -> null;
                };
            }

            case "Indexing" -> new Type("int", false);

            case "Method" -> {
                try {
                    String uniqueName = parent.getParent().get("uniqueName");
                    Method m = symbolTable.getMethod(uniqueName);

                    if (m == null)
                        yield null;

                    // Find arg index
                    int index = 0;
                    for (JmmNode args: parent.getChildren()) {
                        if (methodCallNode == args)
                            break;
                        index++;
                    }

                    if (index >= parent.getNumChildren())
                        yield null;

                    yield m.getParameters().get(index).getType();
                } catch (NullPointerException e) {
                    yield null;
                }
            }

            case "SelectionStatement", "IterationStatement" -> new Type("boolean", false);

            default -> null;
        };

        if (ret == null) {
            this.reports.add(new Report(ReportType.WARNING, Stage.SEMANTIC, Integer.parseInt(methodCallNode.get("line")), Integer.parseInt(methodCallNode.get("col")),
                    "Could not infer return type of method <" + methodCallNode.getChildren().get(1).get("methodName") + ">, assuming void"));
        }

        return ret;
    }

}
