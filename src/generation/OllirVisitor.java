package generation;

import analysis.AstUtils;
import analysis.MySymbolTable;
import pt.up.fe.comp.jmm.JmmNode;
import pt.up.fe.comp.jmm.analysis.Method;
import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.specs.util.SpecsCheck;

import java.util.*;
import java.util.function.BiFunction;

public class OllirVisitor {

    private final Map<String, BiFunction<JmmNode, List<OllirAssistant>, OllirAssistant>> visitMap;

    private final MySymbolTable symbolTable;

    private StringBuilder ollirCode;

    private int auxVarCounter;

    public OllirVisitor(MySymbolTable symbolTable) {
        this.symbolTable = symbolTable;
        this.visitMap = new HashMap<>();
        this.ollirCode = new StringBuilder("");
        this.visitMap.put("Value", this::handleValue);
        this.visitMap.put("BinaryOp", this::handleBinaryOp);
        auxVarCounter = 0;
    }

    private OllirAssistant handleValue(JmmNode node, List<OllirAssistant> childrenResults) {
        String value = "";
        Type type = AstUtils.getValueType(node, symbolTable);
        String name = type.getName();

        value += name + convertTypeToString(type);

        return new OllirAssistant(OllirAssistantType.VALUE, value, type);
    }

    private String convertTypeToString(Type type) {
        String name = type.getName();

        switch(name) {
            case "int":
                return ".i32";
            case "boolean":
                return ".bool";
            case "this": //TODO: TO CHECK
                return "";
            default:
                return "." + name;
        }
    }

    private OllirAssistant handleBinaryOp(JmmNode node, List<OllirAssistant> childrenResults) {
        List<String> values = new ArrayList<>();
        Type opType = null;

        for(OllirAssistant childResult : childrenResults) {
            if(opType == null)
                opType = childResult.getVarType();

            switch (childResult.getType()) {
                case BIN_OP -> {
                    String typeString = convertTypeToString(childResult.getVarType());
                    String auxVar =  "t" + auxVarCounter++;
                    values.add(auxVar);
                    ollirCode.append(auxVar).append(typeString).append(":=").append(typeString);
                    ollirCode.append(childResult.getValue());
                }
                case UN_OP_NEG -> {
                    //TODO: see how OLLIR NEG
                }
                case VALUE -> {
                    values.add(childResult.getValue());
                }
            }
        }

        return new OllirAssistant(OllirAssistantType.BIN_OP,
                OllirAssistant.biOpToString(values.get(0), values.get(1), node.get("op")), opType);
    }

    public OllirAssistant visit(JmmNode node) {
        SpecsCheck.checkNotNull(node, () -> "Node should not be null");

        OllirAssistant result;
        BiFunction<JmmNode, List<OllirAssistant>, OllirAssistant> visit = this.visitMap.get(node.getKind());

        List<OllirAssistant> returnsChildren = new ArrayList<>();

        // Preorder: then, visit each children
        for (var child : node.getChildren())
            returnsChildren.add(visit(child));

        // Preorder: 1st visit the node
//        if (visit != null)
            result = visit.apply(node, returnsChildren);

        return result;
    }
}
