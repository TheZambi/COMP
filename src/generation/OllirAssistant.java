package generation;

import pt.up.fe.comp.jmm.analysis.table.Type;

import java.util.List;

public class OllirAssistant {
    private OllirAssistantType type;
    private String value;
    private Type varType;
    private String auxCode;

    OllirAssistant(OllirAssistantType type, String value, String auxCode, Type varType) {
        this.type = type;
        this.value = value;
        this.auxCode = auxCode;
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

    public String getAuxCode() {
        return auxCode;
    }

    public void setAuxCode(String auxCode) {
        this.auxCode = auxCode;
    }

    public static String biOpToString(List<String> values, Type opType, String op) {
        String opString = switch (op) {
            case "MULTIPLY" -> "*";
            case "ADD" -> "+";
            case "SUBTRACT" -> "-";
            case "AND" -> "&&";
            case "DIVIDE" -> "/";
            case "LESSER" -> "<";
            default -> throw new RuntimeException(op + "is not a binary operation");
        };

        return values.get(0) + " " + opString + convertTypeToString(opType) + " " + values.get(1);
    }

    public static StringBuilder addAllAuxCode(StringBuilder auxCode, List<OllirAssistant> elements) {

        for(OllirAssistant element : elements) {
            auxCode.append(element.getAuxCode());
        }

        return auxCode;
    }

    public static String convertTypeToString(Type type) {
        String name = type.getName();
        boolean isArray = type.isArray();

        switch(name) {
            case "int":
                if(isArray)
                    return ".array.i32";
                return ".i32";
            case "boolean":
                return ".bool";
            case "this": //TODO: TO CHECK
                return "";
            default:
                return "." + name;
        }
    }


    @Override
    public String toString() {
        String varTypeStr = varType == null ? ", varType = null" : ", varType='" + varType.getName() + " " + varType.isArray() + '\'';
        return "OllirAssistant{" +
                "type=" + type +
                ", value='" + value + '\'' +
                varTypeStr +
                ", auxCode='" + auxCode + '\'' +
                '}';
    }
}
