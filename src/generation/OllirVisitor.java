package generation;

import analysis.AstUtils;
import analysis.MySymbolTable;
import org.specs.comp.ollir.Ollir;
import pt.up.fe.comp.jmm.JmmNode;
import pt.up.fe.comp.jmm.analysis.Method;
import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.specs.util.SpecsCheck;

import javax.naming.event.ObjectChangeListener;
import java.util.*;
import java.util.function.BiFunction;

import static generation.OllirAssistant.convertTypeToString;

public class OllirVisitor {

    private final Map<String, BiFunction<JmmNode, List<OllirAssistant>, OllirAssistant>> visitMap;

    private final MySymbolTable symbolTable;

    private StringBuilder ollirCode;

    private int auxVarCounter;

    public OllirVisitor(SymbolTable symbolTable) {
        this.symbolTable = (MySymbolTable) symbolTable;
        this.visitMap = new HashMap<>();
        this.ollirCode = new StringBuilder("");
        this.visitMap.put("Value", this::handleValue);
        this.visitMap.put("BinaryOp", this::handleBinaryOp);
        this.visitMap.put("Statement", this::handleStatement);
        this.visitMap.put("Indexing", this::handleIndexing);
        this.visitMap.put("MethodCall", this::handleMethodCall);
        this.visitMap.put("Method", this::handleMethod);
        this.visitMap.put("Args", this::handleArgs);
        this.visitMap.put("VarDeclaration", this::handleVarDeclaration);
        this.visitMap.put("Symbol", this::handleSymbol);
        this.visitMap.put("Type", this::handleType);
        this.visitMap.put("MethodDeclaration", this::handleMethodDeclaration);
        this.visitMap.put("MethodHeader", this::handleMethodHeader);
        this.visitMap.put("MethodBody", this::handleMethodBody);
        this.visitMap.put("Return", this::handleReturn);
        auxVarCounter = 0;
    }

    private OllirAssistant handleValue(JmmNode node, List<OllirAssistant> childrenResults) {
        String value = "";
        Type type = AstUtils.getValueType(node, symbolTable);
        String name = node.get("object");

        Optional<JmmNode> ancestorOpt = node.getAncestor("MethodDeclaration");
        JmmNode ancestor = ancestorOpt.get();
        JmmNode methodHeader = ancestor.getChildren().get(0);

        for(int i=1; i< methodHeader.getChildren().size();i++)
        {
            if(methodHeader.getChildren().get(i).get("name").equals(name))
            {
                value += "$" + i + ".";
                break;
            }
        }

        value += name + convertTypeToString(type);
        OllirAssistant result = new OllirAssistant(OllirAssistantType.VALUE, value, "", type);
        System.out.println(result);
        return result;
    }

    private OllirAssistant handleVarDeclaration(JmmNode node, List<OllirAssistant> childrenResults) {
//        String value = ".field private " + childrenResults.get(0).getValue();
//        return new OllirAssistant(OllirAssistantType.VAR_DECLARATION, value, "", null);
        return null;
    }

    private OllirAssistant handleType(JmmNode node, List<OllirAssistant> childrenResults) {
//        String value = convertTypeToString(AstUtils.getChildType(node, 0));
//        OllirAssistant result = new OllirAssistant(OllirAssistantType.TYPE, value, "", null);
//        return result;
        return null;
    }

    private OllirAssistant handleSymbol(JmmNode node, List<OllirAssistant> childrenResults) {
        return null;
    }

    private OllirAssistant handleMethodDeclaration(JmmNode node, List<OllirAssistant> childrenResults) {
//        String value = childrenResults.get(0).getValue() + childrenResults.get(1).getValue();
//
//        OllirAssistant result = new OllirAssistant(OllirAssistantType.METHOD_DECLARATION, value.toString(), "", null);
//        System.out.println(result);

//        return result;
        return null;
    }

    private OllirAssistant handleMethodHeader(JmmNode node, List<OllirAssistant> childrenResults) {
        StringBuilder value = new StringBuilder(".method public ");
        Method method = symbolTable.getMethod(node.getChildren().get(0).get("name"));

        value.append(method.getName()).append("(");
        for(Symbol s : method.getParameters())
        {
            value.append(s.getName()).append(convertTypeToString(s.getType()));
        }

        value.append(")").append(convertTypeToString(method.getReturnType()));
        OllirAssistant result = new OllirAssistant(OllirAssistantType.METHOD_HEADER, value.toString(), "", null);
        System.out.println(result);
        return result;
    }

    private OllirAssistant handleMethodBody(JmmNode node, List<OllirAssistant> childrenResults) {
        StringBuilder value = new StringBuilder("");
        value.append("{\n");

        for(OllirAssistant oa : childrenResults)
        {
            if(oa == null) {
                continue;
            }
            value.append(oa.getAuxCode());
            value.append(oa.getValue()).append("\n");
        }
        value.append("}\n");

        System.out.println("\n\n\n");
        OllirAssistant result = new OllirAssistant(OllirAssistantType.METHOD_BODY, value.toString(), "", null);
        System.out.println(result);
        return result;
    }

    private OllirAssistant handleIndexing(JmmNode node, List<OllirAssistant> childrenResults) {
        //Exemple value : C[i.i32].i32
        //filho 0 = variavel
        //filho 1 = index

        StringBuilder auxCode = new StringBuilder("");
        StringBuilder value = new StringBuilder("");

        OllirAssistant childVar = childrenResults.get(0);
        OllirAssistant childIndex = childrenResults.get(1);

        OllirAssistant.addAllAuxCode(auxCode, childrenResults);

        String varValue = childrenResults.get(0).getValue().split("\\.")[0];

        switch (childVar.getType()) {
            case VALUE -> value.append(varValue);
            case METHODCALL -> {
                String auxVar = createAux(varValue, childrenResults.get(0).getVarType(), auxCode);
                value.append(auxVar);
            }
        }

        value.append("[");

        switch (childIndex.getType()) {
            case VALUE:
                value.append(childIndex.getValue());
                break;
            case METHODCALL:
            case BIN_OP:
                String auxVar = createAux(childIndex.getValue(), childIndex.getVarType(), auxCode);
                value.append(auxVar);
                break;
        }

        Type arrayValueType = new Type(childVar.getVarType().getName(), false);
        value.append("]").append(convertTypeToString(arrayValueType));

        OllirAssistant result = new OllirAssistant(OllirAssistantType.INDEXING, value.toString(), auxCode.toString(), arrayValueType);
        System.out.println(result);
        return result;
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

    private OllirAssistant handleReturn(JmmNode node, List<OllirAssistant> childrenResults) {
        StringBuilder value = new StringBuilder("ret");
        StringBuilder auxCode = new StringBuilder("");
        OllirAssistant.addAllAuxCode(auxCode, childrenResults);
        OllirAssistant returnChild = childrenResults.get(0);

        value.append(convertTypeToString(returnChild.getVarType())).append(" ");

        switch(returnChild.getType()) {
            case VALUE:
            case INDEXING:
                value.append(returnChild.getValue());
                break;
            case BIN_OP:
            case UN_OP_NEG:
            case UN_OP_NEW:
            case METHODCALL:
                String auxVar = createAux(returnChild.getValue(), returnChild.getVarType(), auxCode);
                value.append(auxVar);
                break;
        }

        OllirAssistant result = new OllirAssistant(OllirAssistantType.RETURN, value.toString(), auxCode.toString(), returnChild.getVarType());
        System.out.println(result);

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

        OllirAssistant.addAllAuxCode(auxCode, childrenResults);

        if(node.getChildren().size() == 2) {
            statementType = OllirAssistantType.STATEMENT_ASSIGN;
            String typeString = convertTypeToString(childrenResults.get(0).getVarType());

            value.append(childrenResults.get(0).getValue()).append(" :=").append(typeString).append(" ");
            value.append(childrenResults.get(1).getValue());
        }
        else statementType = OllirAssistantType.STATEMENT;

        OllirAssistant result = new OllirAssistant(statementType, value.toString(), auxCode.toString(), null);

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
