package generation.jasmin;

import org.specs.comp.ollir.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class JasminAssistant {
    private ClassUnit ollirClass;
    private StringBuilder code;

    public JasminAssistant(ClassUnit ollirClass) {
        this.code = new StringBuilder();
        this.ollirClass = ollirClass;
    }

    public JasminAssistant generate(){
        this.generateClass();
        return this;
    }

    private void generateClass() {
//            System.out.println("** Name of the package: " + this.classPackage);
//            System.out.println("** Name of the class: " + this.className);
//            System.out.println("\tAccess modifier: " + this.classAccessModifier);
//            System.out.println("\tStatic class: " + this.staticClass);
//            System.out.println("\tFinal class: " + this.finalClass);

        code.append(".class public ").append(ollirClass.getClassName()).append("\n");
        code.append(".super ").append("\n"); // NOT DONE YET

        this.generateFields();

        ArrayList<Method> methods = ollirClass.getMethods();
        for(Method method : methods) {
            this.generateMethod(method);
        }
    }

    private void generateFields() {
        for(Field f : this.ollirClass.getFields()) {
            code.append(".field ").append(f.getFieldAccessModifier().toString().toLowerCase()).append(" ");
            if(f.isFinalField())
                code.append("final ");
            if(f.isStaticField())
                code.append("static ");
            code.append(f.getFieldName());
            code.append(" ");
            code.append(convertType(f.getFieldType().getTypeOfElement()));

            if(f.isInitialized()){
                code.append(" = ");
                code.append(f.getInitialValue());
            }
            code.append("\n");
        }
    }

    private void generateMethod(Method method) {
        code.append(".method ").append(method.getMethodName()).append("(");

        for(int i = 0; i < method.getParams().size(); ++i) {
            Element param = method.getParam(i);
            if(i > 0) {
                code.append(", ");
            }
            code.append(convertType(param.getType().getTypeOfElement()));
        }

        code.append(")").append(convertType(method.getReturnType().getTypeOfElement())).append("\n");
        char prefix = '\t';

        //TODO: change stack and locals limit
        code.append(prefix).append(".limit stack 99\n");
        code.append(prefix).append(".limit locals 99\n");

        for(Instruction instruction : method.getInstructions()) {
            this.generateInstruction(method, instruction);
        }
    }

    private void generateInstruction(Method method, Instruction instruction) {
        switch(instruction.getInstType()) {
            case ASSIGN -> {
                AssignInstruction assignInstruction = (AssignInstruction) instruction;
                Operand leftOp = (Operand) assignInstruction.getDest();
                this.generateInstruction(method, assignInstruction.getRhs());
                HashMap<String, Descriptor> varTable = OllirAccesser.getVarTable(method);
                code.append(convertTypeToInst(leftOp.getType().getTypeOfElement())).append("store_")
                        .append(varTable.get(leftOp.getName()).getVirtualReg()).append("\n");
            }
            case CALL -> {

            }
            case GOTO -> {

            }
            case BRANCH -> {

            }
            case RETURN -> {

            }
            case PUTFIELD -> {

            }
            case GETFIELD -> {

            }
            case UNARYOPER -> {

            }
            case BINARYOPER -> {
                this.generateBiOpInstruction(method, (BinaryOpInstruction) instruction);
            }
            case NOPER -> {
                SingleOpInstruction noOperInstruction = (SingleOpInstruction) instruction;
                Element element = noOperInstruction.getSingleOperand();

                code.append(convertTypeToInst(element.getType().getTypeOfElement()));
                if(element.isLiteral()) {
                    code.append("const_").append(((LiteralElement)element).getLiteral());
                } else {
                    code.append("load_").append(getVirtualReg(method, (Operand) element));
                }
                code.append("\n");
            }
        }
    }

    private void generateBiOpInstruction(Method method, BinaryOpInstruction instruction) {
        List<Element> operands = new ArrayList<>();
        operands.add(instruction.getLeftOperand());
        operands.add(instruction.getRightOperand());

        for(Element operand : operands) {
            code.append(convertTypeToInst(operand.getType().getTypeOfElement()));
            if(operand.isLiteral()) {
                code.append("const_").append(((LiteralElement) operand).getLiteral());
            } else {
                code.append("load_")
                        .append(getVirtualReg(method, (Operand) operand)).append("\n");
            }
            code.append("\n");
        }
        code.append(convertTypeToInst(operands.get(0).getType().getTypeOfElement()));
        String opStr = "";
        switch(instruction.getUnaryOperation().getOpType()) {
            case ADD -> {
                opStr = "add";
            }
            case SUB -> {
                opStr = "sub";
            }
            case MUL -> {
                opStr = "mul";
            }
            case DIV -> {
                opStr = "div";
            }
            case LTH -> {
                opStr = "add";
            }
            case GTH -> {
                opStr = "add";
            }
            case EQ -> {
            }
            case NEQ -> {
            }
            case LTE -> {
                opStr = "add";
            }
            case GTE -> {
                opStr = "add";
            }
            case ANDB -> {
                opStr = "andb";
            }
            case ORB -> {
                opStr = "orb";
            }
        }
        code.append(opStr).append("\n");
    }

    private int getVirtualReg(Method method, Operand operand) {
        return OllirAccesser.getVarTable(method).get(operand.getName()).getVirtualReg();
    }

    private static String convertTypeToInst(ElementType type) {
        return switch (type) {
            case INT32 -> "i";
            case BOOLEAN -> "z";
            case ARRAYREF, OBJECTREF, THIS -> "a";
            case CLASS -> "";
            default -> throw new IllegalStateException("Unexpected value: " + type);
        };
    }

    private static String convertType(ElementType type) {
        switch (type) {
            case INT32 -> {
                return "I";
            }
            case BOOLEAN -> {
                return "Z";
            }
            case ARRAYREF -> {
                return "[I";
            }
            case OBJECTREF -> {
                return "L";
            }
            case CLASS -> {
                return "";
            }
            case THIS -> {
                return "";
            }
            case STRING -> {
                return "Ljava/lang/String;";
            }
            case VOID -> {
                return "V";
            }
            default -> {return "";}
        }
    }

    public String getCode() {
        return code.toString();
    }
}
