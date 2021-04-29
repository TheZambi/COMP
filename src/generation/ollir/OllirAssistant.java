package generation.ollir;

import pt.up.fe.comp.jmm.analysis.table.Type;

import java.util.List;

public class OllirAssistant {
    private final OllirAssistantType type;
    private final String value;
    private final Type varType;
    private final String auxCode;
    private boolean isVariable = false;

    OllirAssistant(OllirAssistantType type, String value, String auxCode, Type varType) {
        this.type = type;
        this.value = value;
        this.auxCode = auxCode;
        this.varType = varType;
    }

    public void setIsVariable()
    {
        this.isVariable = true;
    }

    public boolean isVariable()
    {
        return isVariable;
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

    public static String biOpToString(List<String> values, Type opType, String op) {
        String opString;
        switch (op) {
            case "MULTIPLY":
                opString = "*";
                break;
            case "ADD":
                opString = "+";
                break;
            case "SUBTRACT":
                opString = "-";
                break;
            case "AND":
                opString = "&&";
                break;
            case "DIVIDE":
                opString = "/";
                break;
            case "LESSER":
                opString = "<";
                break;
            default:
                throw new RuntimeException(op + "is not a binary operation");
        }

        return values.get(0) + " " + opString + convertTypeToString(opType) + " " + values.get(1);
    }

    public static void addAllAuxCode(StringBuilder auxCode, List<OllirAssistant> elements) {

        for (OllirAssistant element : elements) {
            auxCode.append(element.getAuxCode());
        }

    }

    public static String convertTypeToString(Type type) {
        String name = type.getName();
        boolean isArray = type.isArray();

        switch (name) {
            case "int":
                if (isArray)
                    return ".array.i32";
                return ".i32";
            case "boolean":
                return ".bool";
            case "lib":
                return "";
            case "void":
                return ".V";
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
