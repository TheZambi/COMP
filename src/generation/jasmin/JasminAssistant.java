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
            code.append(convertType(f.getFieldType()));

            if(f.isInitialized()){
                code.append(" = ");
                code.append(f.getInitialValue());
            }
            code.append("\n");
        }
    }

    private void generateMethod(Method method) {

        code.append("\n.method ");
        if(method.isConstructMethod()) {
            code.append("public ")
                    .append("<init>()V\n\taload_0\n\tinvokespecial java/lang/Object/<init>()V\n\treturn\n.end method\n");
            return;
        }
        code.append(convertAccessModifier(method.getMethodAccessModifier())).append(" ")
                .append(method.getMethodName()).append("(");

        for(int i = 0; i < method.getParams().size(); ++i) {
            Element param = method.getParam(i);
            if(i > 0) {
                code.append(", ");
            }
            code.append(convertType(param.getType()));
        }

        code.append(")").append(convertType(method.getReturnType())).append("\n");
        char prefix = '\t';

        //TODO: change stack and locals limit
        code.append(prefix).append(".limit stack 99\n");
        code.append(prefix).append(".limit locals 99\n");

        for(Instruction instruction : method.getInstructions()) {
            String instructionCode = this.generateInstruction(method, instruction);
            String [] lines = instructionCode.split("\n");
            for(String line : lines) {
                if(line.length() > 0)
                    code.append("\t").append(line).append("\n");
            }
        }
        code.append(".end method\n");
    }

    private String generateInstruction(Method method, Instruction instruction) {
        StringBuilder instCode = new StringBuilder();

        switch(instruction.getInstType()) {
            case ASSIGN -> {
                AssignInstruction assignInstruction = (AssignInstruction) instruction;
                instCode.append(this.generateAssign(method, assignInstruction));
            }
            case CALL -> {
                CallInstruction callInstruction = (CallInstruction) instruction;
                instCode.append(this.generateCall(method, callInstruction));
            }
            case GOTO -> {

            }
            case BRANCH -> {

            }
            case RETURN -> {
                Element element = ((ReturnInstruction) instruction).getOperand();
                instCode.append(getElement(method, element));
                String typeStr = convertTypeToInst(element.getType().getTypeOfElement());
                instCode.append(typeStr).append("return\n");
             }
            case PUTFIELD -> {

            }
            case GETFIELD -> {

            }
            case UNARYOPER -> {

            }
            case BINARYOPER -> {
                instCode.append(this.generateBiOpInstruction(method, (BinaryOpInstruction) instruction));
            }
            case NOPER -> {
                SingleOpInstruction noOperInstruction = (SingleOpInstruction) instruction;
                Element element = noOperInstruction.getSingleOperand();

                instCode.append(getElement(method, element));
            }
        }

        return instCode.toString();
    }

    private String generateAssign(Method method, AssignInstruction assignInstruction) {
        StringBuilder instCode = new StringBuilder();

        Operand leftOp = (Operand) assignInstruction.getDest();

        if(assignInstruction.getRhs().getInstType() == InstructionType.CALL
            && OllirAccesser.getCallInvocation((CallInstruction)(assignInstruction.getRhs())) == CallType.NEW) {
            CallInstruction callInstruction = (CallInstruction) (assignInstruction.getRhs());
            if(callInstruction.getReturnType().getTypeOfElement() == ElementType.ARRAYREF) {

            } else {
                instCode.append("new ").append(((ClassType) callInstruction.getReturnType()).getName()).append("\n");
                instCode.append("dup").append("\n");
            }
        } else {
            instCode.append(this.generateInstruction(method, assignInstruction.getRhs()));
            instCode.append(convertTypeToInst(leftOp.getType().getTypeOfElement())).append("store_")
                    .append(getVirtualReg(method, leftOp)).append("\n");

        }
        return instCode.toString();
    }

    private String generateCall(Method method, CallInstruction callInstruction) {
        String invokeString = "";
        StringBuilder callCode = new StringBuilder();
        switch(OllirAccesser.getCallInvocation(callInstruction)) {
            case invokevirtual:
                callCode.append(this.generateInvVirtual(method, callInstruction));
                break;
            case invokeinterface:
                break;
            case invokespecial: //does not work for class constructor, only for NEW OBJREF operations!!
                callCode.append(this.generateInvSpecial(method, callInstruction));
                break;
            case invokestatic:

                break;
            case NEW:

            case arraylength:

            case ldc:

                return "";
            default:
                throw new RuntimeException("Invalid call type!");
        }
        return callCode.toString();
    }

    private String generateInvSpecial(Method method, CallInstruction callInstruction) {
        StringBuilder instCode = new StringBuilder("");

        StringBuilder params = new StringBuilder();
        for(Element param : callInstruction.getListOfOperands()) {
            params.append(convertType(param.getType()));
            if(param.isLiteral()) {
                instCode.append(getElement(method, param));
            }
        }

        instCode.append("invokespecial <init>(");

        instCode.append(params);

        instCode.append(")V\n");
        instCode.append("astore_").append(getVirtualReg(method, (Operand) callInstruction.getFirstArg())).append("\n");

        return instCode.toString();
    }

    private String generateInvVirtual(Method method, CallInstruction callInstruction) {
        StringBuilder instCode = new StringBuilder("");
        Operand firstArg = (Operand) callInstruction.getFirstArg();
        LiteralElement methodElement = (LiteralElement) callInstruction.getSecondArg();

        instCode.append(getElement(method, callInstruction.getFirstArg()));

        StringBuilder params = new StringBuilder();
        for(Element param : callInstruction.getListOfOperands()) {
            params.append(convertType(param.getType()));
            if(param.isLiteral()) {
                instCode.append(getElement(method, param));
            }
        }

        instCode.append("invokevirtual ").append(getMethodName(firstArg, methodElement)).append("(");

        instCode.append(params);

        instCode.append(")").append(convertType(callInstruction.getReturnType())).append("\n");

        return instCode.toString();
    }

    private String getMethodName(Operand object, LiteralElement methodElement) {
        if(object.getName().equals("this")) {
            return ollirClass.getClassName() + '.' + methodElement.getLiteral().replaceAll("\"", "");
        } else {
            return ""; //TODO: methods without 'this'
        }
    }

    private String generateBiOpInstruction(Method method, BinaryOpInstruction instruction) {
        List<Element> operands = new ArrayList<>();
        operands.add(instruction.getLeftOperand());
        operands.add(instruction.getRightOperand());
        StringBuilder instCode = new StringBuilder();

        for(Element operand : operands) {
            instCode.append(getElement(method, operand));
        }
        instCode.append(convertTypeToInst(operands.get(0).getType().getTypeOfElement()));
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
        instCode.append(opStr).append("\n");
        return instCode.toString();
    }

    private String getElement(Method method, Element operand) {
        StringBuilder instCode = new StringBuilder();
        instCode.append(convertTypeToInst(operand.getType().getTypeOfElement()));
        if(operand.isLiteral()) {
            instCode.append("const_").append(((LiteralElement) operand).getLiteral());
        } else {
            instCode.append("load_")
                    .append(getVirtualReg(method, (Operand) operand));
        }
        instCode.append("\n");
        return instCode.toString();
    }

    private int getVirtualReg(Method method, Operand operand) {
        if(operand.getName().equals("this"))
            return 0;
        return OllirAccesser.getVarTable(method).get(operand.getName()).getVirtualReg();
    }

    private static String convertAccessModifier(AccessModifiers accessModifier) {
        return accessModifier.toString().toLowerCase();
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

    private static String convertType(Type type) {
        switch (type.getTypeOfElement()) {
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
                return "L" + ((ClassType) type).getName();
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
