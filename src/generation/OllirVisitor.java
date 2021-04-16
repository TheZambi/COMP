package generation;

import analysis.AstUtils;
import org.specs.comp.ollir.Ollir;
import pt.up.fe.comp.jmm.JmmNode;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.specs.util.SpecsCheck;

import javax.naming.event.ObjectChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

import static generation.OllirAssistant.convertTypeToString;

public class OllirVisitor {

    private final Map<String, BiFunction<JmmNode, List<OllirAssistant>, OllirAssistant>> visitMap;

    private final SymbolTable symbolTable;

    private StringBuilder ollirCode;

    private int auxVarCounter;

    public OllirVisitor(SymbolTable symbolTable) {
        this.symbolTable = symbolTable;
        this.visitMap = new HashMap<>();
        this.ollirCode = new StringBuilder("");
        this.visitMap.put("Value", this::handleValue);
        this.visitMap.put("BinaryOp", this::handleBinaryOp);
//        this.visitMap.put("Statement", this::handleStatement);
//        this.visitMap.put("Indexing", this::handleIndexing);
        this.visitMap.put("MethodCall", this::handleMethodCall);
        this.visitMap.put("Method", this::handleMethod);
        this.visitMap.put("Args", this::handleArgs);
        auxVarCounter = 0;
    }

    private OllirAssistant handleValue(JmmNode node, List<OllirAssistant> childrenResults) {
        String value = "";
        Type type = AstUtils.getValueType(node, symbolTable);
        String name = node.get("object");


        value += name + convertTypeToString(type);
        OllirAssistant result = new OllirAssistant(OllirAssistantType.VALUE, value, "", type);
        System.out.println(result);
        return result;
    }

    private OllirAssistant handleIndexing(JmmNode node, List<OllirAssistant> childrenResults) {
        //filho 0 = variavel
        //filho 1 = index
        return null;
    }

    private OllirAssistant handleMethodCall(JmmNode node, List<OllirAssistant> childrenResults) {
        StringBuilder auxCode = new StringBuilder("");
        StringBuilder value = new StringBuilder("invokevirtual(");

        OllirAssistant.addAllAuxCode(auxCode, childrenResults);

        Type methodVarType = childrenResults.get(1).getVarType();

        switch (childrenResults.get(0).getType()) {
            case VALUE:
                value.append(childrenResults.get(0).getValue());
                break;
            case METHODCALL:
                String auxVar = createAux(childrenResults.get(0).getValue(), childrenResults.get(0).getVarType(), auxCode);
                value.append(auxVar);
                break;
        }
        value.append(childrenResults.get(1).getValue()).append(")").append(convertTypeToString(methodVarType));

        OllirAssistant result = new OllirAssistant(OllirAssistantType.METHODCALL, value.toString(), auxCode.toString(), methodVarType);
        System.out.println(result);
        return result;
    }

    private OllirAssistant handleMethod(JmmNode node, List<OllirAssistant> childrenResults) {
        OllirAssistant result;
        Type returnType = symbolTable.getReturnType(node.get("methodName"));
        if(childrenResults.size() > 0) {
            OllirAssistant child = childrenResults.get(0);
            StringBuilder auxCode = new StringBuilder("");
            OllirAssistant.addAllAuxCode(auxCode, childrenResults);

            result = new OllirAssistant(OllirAssistantType.METHOD, ", " + "\"" + node.get("methodName") + "\"" + child.getValue(), auxCode.toString(), returnType);
            System.out.println(result);
        }
        else
        {
            result =  new OllirAssistant(OllirAssistantType.METHOD, ", " + "\"" + node.get("methodName") + "\"", "", returnType);
        }
        return result;
    }

    private OllirAssistant handleArgs(JmmNode node, List<OllirAssistant> childrenResults) {
        StringBuilder sb = new StringBuilder();
        Type opType = null;
        StringBuilder auxCode = new StringBuilder("");
        OllirAssistant.addAllAuxCode(auxCode, childrenResults);

        for(OllirAssistant childResult : childrenResults) {
            opType = childResult.getVarType();
            sb.append(", ");
            switch (childResult.getType()) {
                case BIN_OP -> {
                    String auxVar =  createAux(childResult.getValue(), childResult.getVarType(), auxCode);
                    sb.append(auxVar);
                }
                case UN_OP_NEG -> {
                    //TODO: see how OLLIR NEG
                }
                case VALUE -> {
                    sb.append(childResult.getValue());
                }
            }
        }

        OllirAssistant result = new OllirAssistant(OllirAssistantType.METHOD_ARGS,
                sb.toString(),
                auxCode.toString(),
                opType);

        System.out.println(result);
        return result;
    }

    private OllirAssistant handleBinaryOp(JmmNode node, List<OllirAssistant> childrenResults) {
        List<String> values = new ArrayList<>();
        Type opType = null;
        StringBuilder auxCode = new StringBuilder("");
        OllirAssistant.addAllAuxCode(auxCode, childrenResults);

        for(OllirAssistant childResult : childrenResults) {
            if(opType == null)
                opType = childResult.getVarType();

            switch (childResult.getType()) {
                case BIN_OP -> {
                    String auxVar =  createAux(childResult.getValue(), childResult.getVarType(), auxCode);
                    values.add(auxVar);
                }
                case UN_OP_NEG -> {
                    //TODO: see how OLLIR NEG
                }
                case VALUE -> {
                    values.add(childResult.getValue());
                }
            }
        }
        OllirAssistant result = new OllirAssistant(OllirAssistantType.BIN_OP,
                OllirAssistant.biOpToString(values, opType, node.get("op")),
                auxCode.toString(),
                opType);
        System.out.println(result);
        return result;
    }

    private OllirAssistant handleStatement(JmmNode node, List<OllirAssistant> childrenResults) {
        OllirAssistantType statementType;
        StringBuilder value = new StringBuilder("");
        StringBuilder auxCode = new StringBuilder("");

        if(node.getChildren().size() == 2) {
            statementType = OllirAssistantType.STATEMENT_ASSIGN;
            String typeString = convertTypeToString(childrenResults.get(0).getVarType());
            value.append(node.getChildren().get(0).get("object")).append(typeString).append(" :=").append(typeString).append(" ");
            value.append(childrenResults.get(1).getValue());
        }
        else statementType = OllirAssistantType.STATEMENT;

        OllirAssistant result = new OllirAssistant(statementType, value.toString(), auxCode.toString(), new Type("assignement",false));

        StringBuilder sb = result.addAllAuxCode(new StringBuilder(),childrenResults);
        result.setAuxCode(sb.toString());

        System.out.println(result);

        return result;
    }

    private String createAux(String value, Type type, StringBuilder auxCode) {
        String typeString = convertTypeToString(type);
        String auxVar = "t" + auxVarCounter++ + typeString;
        auxCode.append(auxVar).append(" :=" + typeString + " " + value).append(";\n");
        return auxVar;
    }

    public OllirAssistant visit(JmmNode node) {
        SpecsCheck.checkNotNull(node, () -> "Node should not be null");

        OllirAssistant result = null;
        BiFunction<JmmNode, List<OllirAssistant>, OllirAssistant> visit = this.visitMap.get(node.getKind());

        List<OllirAssistant> returnsChildren = new ArrayList<>();

        // Preorder: then, visit each children
        for (var child : node.getChildren())
            returnsChildren.add(visit(child));

        // Preorder: 1st visit the node
        if (visit != null)
            result = visit.apply(node, returnsChildren);

        return result;
    }
}
