package generation;

import pt.up.fe.comp.jmm.analysis.table.Type;

public class OllirAssistant {
    private OllirAssistantType type;
    private String value;
    private Type varType;

    OllirAssistant(OllirAssistantType type, String value, Type varType) {
        this.type = type;
        this.value = value;
        this.varType = varType;
    }

    public OllirAssistantType getType() {
        return type;
    }

    public String getValue() {
        return value;
    }

    public Type getVarType() {
        return varType;
    }

    public static String biOpToString(String value1, String value2, String op) {
        String opString = switch (op) {
            case "MULTIPLY" -> "*";
            case "ADD" -> "+";
            case "SUBTRACT" -> "-";
            case "AND" -> "&&";
            case "DIVIDE" -> "/";
            case "LESSER" -> "<";
            default -> throw new RuntimeException(op + "is not a binary operation");
        };

        return value1 + opString + value2;
    }
}
