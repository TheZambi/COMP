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
        code.append(".super java/lang/Object").append("\n");

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
        code.append(convertAccessModifier(method.getMethodAccessModifier())).append(" ");

        if(method.isStaticMethod())
            code.append("static ");
        else if(method.isFinalMethod())
            code.append("final ");
        code.append(method.getMethodName()).append("(");

        for(int i = 0; i < method.getParams().size(); ++i) {
            Element param = method.getParam(i);
//            if(i > 0) {
//                code.append(", ");
//            }
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

        int lastInstIndex = method.getInstructions().size()-1;
        if(lastInstIndex < 0 || method.getInstructions().get(lastInstIndex).getInstType() != InstructionType.RETURN) {
            code.append("\treturn\n");
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
                ReturnInstruction returnInstruction = (ReturnInstruction) instruction;

                if(returnInstruction.hasReturnValue()) {
                    Element element = returnInstruction.getOperand();
                    instCode.append(getElement(method, element));
                    String typeStr = convertTypeToInst(element.getType().getTypeOfElement());
                    instCode.append(typeStr);
                }
                instCode.append("return\n");
             }
            case PUTFIELD -> {
                instCode.append(this.generatePutfield(method, (PutFieldInstruction) instruction));
            }
            case GETFIELD -> {
                instCode.append(this.generateGetfield(method, (GetFieldInstruction) instruction));
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
            instCode.append(createStoreInst(leftOp.getType().getTypeOfElement(),getVirtualReg(method, leftOp))).append("\n");

        }
        return instCode.toString();
    }

    private String generateGetfield(Method method, GetFieldInstruction instruction) {

        return getElement(method, instruction.getFirstOperand()) +
                "getfield " + ((Operand) instruction.getSecondOperand()).getName() +
                " " + convertType(instruction.getSecondOperand().getType().getTypeOfElement()) + "\n";
    }

    private String generatePutfield(Method method, PutFieldInstruction instruction) {

        return getElement(method, instruction.getFirstOperand()) +
                getElement(method, instruction.getThirdOperand()) +
                "putfield " + ((Operand) instruction.getSecondOperand()).getName() +
                " " + convertType(instruction.getSecondOperand().getType().getTypeOfElement()) + "\n";
    }

    private String generateCall(Method method, CallInstruction callInstruction) {
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
                callCode.append(this.generateInvStatic(method, callInstruction));
                break;
            case NEW:
                break;
            case arraylength:
                break;
            case ldc:
                break;
            default:
                throw new RuntimeException("Invalid call type!");
        }
        return callCode.toString();
    }

    private String generateInvStatic(Method method, CallInstruction callInstruction) {
        StringBuilder instCode = new StringBuilder("");
        Operand firstArg = (Operand) callInstruction.getFirstArg();
        LiteralElement methodElement = (LiteralElement) callInstruction.getSecondArg();

        StringBuilder params = new StringBuilder();
        for(Element param : callInstruction.getListOfOperands()) {
            params.append(convertType(param.getType()));
            instCode.append(getElement(method, param));
        }

        instCode.append("invokestatic ").append(getMethodName(firstArg, methodElement)).append("(");

        instCode.append(params);

        instCode.append(")").append(convertType(callInstruction.getReturnType())).append("\n");

        return instCode.toString();
    }

    private String generateInvSpecial(Method method, CallInstruction callInstruction) {
        StringBuilder instCode = new StringBuilder("");

        StringBuilder params = new StringBuilder();
        for(Element param : callInstruction.getListOfOperands()) {
            params.append(convertType(param.getType()));
            instCode.append(getElement(method, param));
        }

        instCode.append("invokespecial ").append(((ClassType)callInstruction.getFirstArg().getType()).getName())
                .append(".<init>(");

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
            instCode.append(getElement(method, param));
        }

        instCode.append("invokevirtual ").append(getMethodName(firstArg, methodElement)).append("(");

        instCode.append(params);

        instCode.append(")").append(convertType(callInstruction.getReturnType())).append("\n");

        return instCode.toString();
    }

    private String getMethodName(Operand object, LiteralElement methodElement) {
        String methodName = methodElement.getLiteral().replaceAll("\"", "");
        if(object.getName().equals("this")) {
            return ollirClass.getClassName() + '.' + methodName;
        } else if(object.getType().getTypeOfElement() == ElementType.CLASS) { //static invoke
            return object.getName() + '.' + methodName;
        } else {
            return ((ClassType)object.getType()).getName() + '.' + methodName;
        }
    }

    private String generateBiOpInstruction(Method method, BinaryOpInstruction instruction) {
        List<Element> operands = new ArrayList<>();
        operands.add(instruction.getLeftOperand());
        operands.add(instruction.getRightOperand());
        StringBuilder instCode = new StringBuilder();

        OperationType opType = instruction.getUnaryOperation().getOpType();
        if(opType != OperationType.NOTB) {
            for (Element operand : operands) {
                instCode.append(getElement(method, operand));
            }
        } else { //if this operation is a negation, we only want to load one side of the op as both sides will be the same
            instCode.append(getElement(method, operands.get(0)));
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
                opStr = "and";
            }
            case NOTB -> {
                opStr = "neg";
            }
        }
        instCode.append(opStr).append("\n");
        return instCode.toString();
    }

    private String getElement(Method method, Element operand) {
        StringBuilder instCode = new StringBuilder();
        ElementType elementType = operand.getType().getTypeOfElement();
        if(operand.isLiteral()) { //if literal, create a const instruction
            int value = Integer.parseInt(((LiteralElement) operand).getLiteral());
            instCode.append(createConstInst(value));
        } else { //if its a variable, load with its virtual reg
            if(operand.getType().getTypeOfElement() != ElementType.CLASS) {
                instCode.append(createLoadInst(elementType, getVirtualReg(method, (Operand) operand)));
            }
        }
        instCode.append("\n");
        return instCode.toString();
    }

    private int getVirtualReg(Method method, Operand operand) {
        if(operand.getName().equals("this"))
            return 0;

        return method.getVarTable().get(operand.getName()).getVirtualReg();
    }

    private String createConstInst(int value) {

        if(value == -1) {
            return "iconst_m1";
        } else if (value >= 0 && value <= 5) {
            return "iconst_" + value;
        } else if (value >= -128 && value <= 127) {
            return "bipush " + value;
        } else if (value >= -32768 && value <= 32767) {
            return "sipush " + value;
        } else {
            return "ldc " + value;
        }
    }

    private String createStoreInst(ElementType type, int virtualReg) {
        StringBuilder storeInst = new StringBuilder();
        storeInst.append(convertTypeToInst(type)).append("store");

        if(virtualReg <= 3) { // istore_1
            storeInst.append("_");
        } else storeInst.append(" "); //istore 6

        storeInst.append(virtualReg);
        return storeInst.toString();
    }

    private String createLoadInst(ElementType type, int virtualReg) { //TODO: repeated code - see createStoreInst
        StringBuilder storeInst = new StringBuilder();
        storeInst.append(convertTypeToInst(type)).append("load");

        if(virtualReg <= 3) { // iload_1
            storeInst.append("_");
        } else storeInst.append(" "); //iload 6

        storeInst.append(virtualReg);
        return storeInst.toString();
    }

    private static String convertAccessModifier(AccessModifiers accessModifier) {
        return accessModifier.toString().toLowerCase();
    }

    private static String convertTypeToInst(ElementType type) {
        return switch (type) {
            case INT32, BOOLEAN -> "i";
            case ARRAYREF, OBJECTREF, THIS, CLASS -> "a";
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
                return "[" + convertType(((ArrayType) type).getTypeOfElements());
            }
            case OBJECTREF -> {
                return "L" + ((ClassType) type).getName();
            }
            case CLASS -> {
                return "L" + ((ClassType) type).getName();
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

    private static String convertType(ElementType type) {
        switch (type) {
            case INT32 -> {
                return "I";
            }
            case BOOLEAN -> {
                return "Z";
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
