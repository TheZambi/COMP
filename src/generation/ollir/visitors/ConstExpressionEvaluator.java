package generation.ollir.visitors;

import analysis.enums.SemBinaryOp;
import analysis.enums.SemUnaryOp;
import ast.AstUtils;
import pt.up.fe.comp.jmm.JmmNode;
import pt.up.fe.specs.util.SpecsCheck;

import java.util.*;
import java.util.function.Function;

public class ConstExpressionEvaluator {

    private static final Map<String, Function<JmmNode, Object>> visitMap;

    // Instantiating the static map
    static
    {
        visitMap = new HashMap<>();
        visitMap.put("BinaryOp", ConstExpressionEvaluator::binaryOpVisit);
        visitMap.put("Value", ConstExpressionEvaluator::valueVisit);
        visitMap.put("UnaryOp", ConstExpressionEvaluator::unaryOpVisit);
        visitMap.put("Statement", ConstExpressionEvaluator::statementVisit);
        visitMap.put("Return", ConstExpressionEvaluator::checkFirstChild);
        visitMap.put("SelectionStatement", ConstExpressionEvaluator::checkFirstChild);
        visitMap.put("IterationStatement", ConstExpressionEvaluator::checkFirstChild);
    }

    private static Object statementVisit(JmmNode node) {
        int evalIndex = AstUtils.isAssignment(node) ? 1 : 0;
        return evaluate(node.getChildren().get(evalIndex));
    }

    private static Object checkFirstChild(JmmNode node) {
        return evaluate(node.getChildren().get(0));
    }

    private static Object valueVisit(JmmNode node) {
        return switch (node.get("type")) {
            case "int" -> Integer.parseInt(node.get("object"));
            case "boolean" -> Boolean.parseBoolean(node.get("object"));
            default -> null;
        };
    }

    private static Object unaryOpVisit(JmmNode node) {
        Object child = evaluate(node.getChildren().get(0));
        if (child == null)
            return null;

        return switch (SemUnaryOp.valueOf(node.get("op"))) {
            case NEG -> !(Boolean) child;
            default -> null;
        };
    }

    private static Object binaryOpVisit(JmmNode node) {
        Object left = evaluate(node.getChildren().get(0)), right = evaluate(node.getChildren().get(1));
        if (left == null || right == null)
            return null;

        return switch (SemBinaryOp.valueOf(node.get("op"))) {
            case AND -> (Boolean) left && (Boolean) right;
            case LESSER -> (Integer) left < (Integer) right;
            case ADD -> (Integer) left + (Integer) right;
            case SUBTRACT -> (Integer) left - (Integer) right;
            case MULTIPLY -> (Integer) left * (Integer) right;
            case DIVIDE -> (Integer) left / (Integer) right;
            default -> null;
        };
    }

    public static Object evaluate(JmmNode node) {
        SpecsCheck.checkNotNull(node, () -> "Node should not be null");

        Function<JmmNode, Object> visit = visitMap.get(node.getKind());

        return visit != null ? visit.apply(node) : null;
    }
}
