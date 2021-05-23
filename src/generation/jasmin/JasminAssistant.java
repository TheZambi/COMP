package generation.jasmin;

import org.specs.comp.ollir.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JasminAssistant {
    private final ClassUnit ollirClass;
    private final StringBuilder code;
    private int lthBranch;
    private String superClass;
    private HashMap<String, Integer> stackLimits;
    private int currentInstructionLimit;

    public JasminAssistant(ClassUnit ollirClass) {
        this.code = new StringBuilder();
        this.ollirClass = ollirClass;
        this.lthBranch = 0;
        stackLimits = new HashMap<>();
    }

    public JasminAssistant generate(){
        this.generateClass();
        return this;
    }

    private void generateClass() {
        code.append(".class public ").append(ollirClass.getClassName()).append("\n");
        code.append(".super ");
        if(ollirClass.getSuperClass() == null) {
            superClass = "java/lang/Object";
            code.append(superClass);
        }
        else {
            superClass = ollirClass.getSuperClass();
            code.append(superClass);
        }
        code.append("\n");

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

        stackLimits.put(method.getMethodName(), 0);

        code.append("\n.method ");
        if(method.isConstructMethod()) {
            code.append("public ")
                    .append("<init>()V\n\taload_0\n\tinvokespecial ").append(superClass).append(".<init>()V\n\treturn\n.end method\n");
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
            code.append(convertType(param.getType()));
        }

        code.append(")").append(convertType(method.getReturnType())).append("\n");
        char prefix = '\t';

        StringBuilder tempCode = new StringBuilder();
        //TODO: change stack and locals limit

        int locals_size = method.getVarTable().size();
        if(method.getVarTable().get("this") == null)
            locals_size++;


        tempCode.append(prefix).append(".limit locals ").append(locals_size).append("\n");

        for(Instruction instruction : method.getInstructions()) {
            currentInstructionLimit = 0;
            String instructionCode = this.generateInstruction(method, instruction, false);

            if(currentInstructionLimit > stackLimits.get(method.getMethodName())) {
                stackLimits.replace(method.getMethodName(), currentInstructionLimit);
            }

            String [] lines = instructionCode.split("\n");
            for(String line : lines) {
                if(line.length() > 0)
                    tempCode.append("\t").append(line).append("\n");
            }
        }

        int lastInstIndex = method.getInstructions().size()-1;
        if(lastInstIndex < 0 || method.getInstructions().get(lastInstIndex).getInstType() != InstructionType.RETURN) {
            tempCode.append("\treturn\n");
        }

        tempCode.append(".end method\n");

        code.append(prefix).append(".limit stack ").append(stackLimits.get(method.getMethodName())).append("\n");
        code.append(tempCode);
    }

    private String generateInstruction(Method method, Instruction instruction, boolean inAssign) {
        StringBuilder instCode = new StringBuilder();

        // Find if instruction has labels
        for(Map.Entry<String, Instruction> entry : method.getLabels().entrySet()) {
            if(entry.getValue().equals(instruction)) {
                instCode.append(processLabelName(entry.getKey())).append(":\n");
            }
        }

        switch(instruction.getInstType()) {
            case ASSIGN -> {
                AssignInstruction assignInstruction = (AssignInstruction) instruction;
                instCode.append(this.generateAssign(method, assignInstruction));
            }
            case CALL -> {
                CallInstruction callInstruction = (CallInstruction) instruction;
                instCode.append(this.generateCall(method, callInstruction));
                if(!inAssign && !callInstruction.getReturnType().getTypeOfElement().equals(ElementType.VOID))
                    instCode.append("pop\n");
            }
            case GOTO -> {
                GotoInstruction gotoInstruction = (GotoInstruction) instruction;
                instCode.append(this.generateGoto(method, gotoInstruction));
            }
            case BRANCH -> {
                CondBranchInstruction branchInstruction = (CondBranchInstruction) instruction;
                instCode.append(this.generateBranch(method, branchInstruction));
            }
            case RETURN -> {
                ReturnInstruction returnInstruction = (ReturnInstruction) instruction;

                if(returnInstruction.hasReturnValue()) {
                    currentInstructionLimit += 1;
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
                currentInstructionLimit++;
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
            && ((CallInstruction)(assignInstruction.getRhs())).getInvocationType() == CallType.NEW) {  //Assign with 'new' operation
            CallInstruction callInstruction = (CallInstruction) (assignInstruction.getRhs()); //right side of the assignment
            if(callInstruction.getReturnType().getTypeOfElement() == ElementType.ARRAYREF) { //new array
                currentInstructionLimit++;
                instCode.append(getElement(method, callInstruction.getListOfOperands().get(0)));
                instCode.append("\nnewarray int\n");
                instCode.append(createStoreInst(ElementType.ARRAYREF, false, getVirtualReg(method, leftOp))).append("\n");
            } else { //new class object
                currentInstructionLimit+=2;
                instCode.append("new ").append(((ClassType) callInstruction.getReturnType()).getName()).append("\n");
                instCode.append("dup").append("\n");
            }
        } else {
            boolean isArrayAssign = false;

            // Array element assignment
            if(leftOp instanceof ArrayOperand) {
                currentInstructionLimit += 2;
                isArrayAssign = true;
                instCode.append(getElementArrayAssign(method, assignInstruction.getDest()));
            }
            instCode.append(this.generateInstruction(method, assignInstruction.getRhs(), true));
            instCode.append(createStoreInst(leftOp.getType().getTypeOfElement(), isArrayAssign, getVirtualReg(method, leftOp))).append("\n");
        }
        return instCode.toString();
    }

    private String generateGetfield(Method method, GetFieldInstruction instruction) {
        currentInstructionLimit += 2;
        return getElement(method, instruction.getFirstOperand()) +
                "getfield " + ollirClass.getClassName() + '/' + ((Operand) instruction.getSecondOperand()).getName() +
                " " + convertType(instruction.getSecondOperand().getType()) + "\n";
    }

    private String generatePutfield(Method method, PutFieldInstruction instruction) {
        currentInstructionLimit += 2;
        return getElement(method, instruction.getFirstOperand()) +
                getElement(method, instruction.getThirdOperand()) +
                "putfield " + ollirClass.getClassName() + '/' + ((Operand) instruction.getSecondOperand()).getName() +
                " " + convertType(instruction.getSecondOperand().getType()) + "\n";
    }

    private String generateCall(Method method, CallInstruction callInstruction) {
        StringBuilder callCode = new StringBuilder();
        switch(callInstruction.getInvocationType()) {
            case invokevirtual:
                callCode.append(this.generateInvVirtual(method, callInstruction));
                break;
            case invokespecial: //does not work for class constructor, only for NEW OBJREF operations!!
                callCode.append(this.generateInvSpecial(method, callInstruction));
                break;
            case invokestatic:
                callCode.append(this.generateInvStatic(method, callInstruction));
                break;
            case arraylength:
                currentInstructionLimit++;
                callCode.append(createLoadInst(callInstruction.getFirstArg().getType().getTypeOfElement(),
                        true, getVirtualReg(method, (Operand)callInstruction.getFirstArg())));
                callCode.append("\narraylength\n");
                break;
            case invokeinterface:
            case NEW:
            case ldc:
                break;
            default:
                throw new RuntimeException("Invalid call type!");
        }
        return callCode.toString();
    }

    private String generateInvStatic(Method method, CallInstruction callInstruction) {
        StringBuilder instCode = new StringBuilder();
        Operand firstArg = (Operand) callInstruction.getFirstArg();
        LiteralElement methodElement = (LiteralElement) callInstruction.getSecondArg();

        StringBuilder params = new StringBuilder();
        for(Element param : callInstruction.getListOfOperands()) {
            params.append(convertType(param.getType()));
            instCode.append(getElement(method, param));
        }

        instCode.append("invokestatic ").append(getMethodName(firstArg, methodElement)).append("(");

        instCode.append(params);

        Type returnType = callInstruction.getReturnType();

        instCode.append(")").append(convertType(returnType)).append("\n");

        int returnVoid = returnType.getTypeOfElement() == ElementType.VOID ? 0 : 1;
        currentInstructionLimit += Math.max(returnVoid, callInstruction.getListOfOperands().size());

        return instCode.toString();
    }

    private String generateInvSpecial(Method method, CallInstruction callInstruction) {
        StringBuilder instCode = new StringBuilder();

        StringBuilder params = new StringBuilder();
        for(Element param : callInstruction.getListOfOperands()) {
            params.append(convertType(param.getType()));
            instCode.append(getElement(method, param));
        }

        instCode.append("invokespecial ").append(((ClassType)callInstruction.getFirstArg().getType()).getName())
                .append(".<init>(");

        instCode.append(params);

        instCode.append(")V\n");
        instCode.append(createStoreInst(ElementType.CLASS, false, getVirtualReg(method, (Operand) callInstruction.getFirstArg())));

        currentInstructionLimit += Math.max(callInstruction.getListOfOperands().size(), 1);

        return instCode.toString();
    }

    private String generateInvVirtual(Method method, CallInstruction callInstruction) {
        StringBuilder instCode = new StringBuilder();
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

        currentInstructionLimit += 1 + callInstruction.getListOfOperands().size();

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
        currentInstructionLimit += 2;

        OperationType opType = instruction.getUnaryOperation().getOpType();

        List<Element> operands = new ArrayList<>();
        operands.add(instruction.getLeftOperand());
        operands.add(instruction.getRightOperand());
        StringBuilder instCode = new StringBuilder();

        if(opType != OperationType.NOTB && opType != OperationType.ANDB) {
            for (Element operand : operands) {
                instCode.append(getElement(method, operand));
            }
        } else { //if this operation is a negation, we only want to load one side of the op as both sides will be the same
            instCode.append(getElement(method, operands.get(0)));
        }

        if(opType == OperationType.LTH) {
            /*
            if arg1 >= arg2 goto 0
            -> const true
            goto 1
            0: -> const false
            1: --continue---
            */
            instCode.append("if_icmpge ").append(lthBranch++)
                    .append("\niconst_1\ngoto ").append(lthBranch++)
                    .append("\n").append(lthBranch-2).append(": iconst_0\n")
                    .append(lthBranch-1).append(": ");

            return instCode.toString();
        } else if(opType == OperationType.NOTB) {
            currentInstructionLimit--;
            /*
            if a != false goto 0
            -> const true
            goto 1
            0: -> const false
            1: --continue--
            */
            instCode.append("ifne ").append(lthBranch++)
                    .append("\niconst_1\ngoto ").append(lthBranch++)
                    .append("\n").append(lthBranch-2).append(": iconst_0\n")
                    .append(lthBranch-1).append(": ");

            return instCode.toString();
        } else if(opType == OperationType.ANDB) {
            instCode.append("ifeq ").append(lthBranch).append("\n")
                    .append(getElement(method, operands.get(1)))
                    .append("\nifeq ").append(lthBranch++)
                    .append("\niconst_1\ngoto ").append(lthBranch++)
                    .append("\n").append(lthBranch-2).append(": iconst_0\n")
                    .append(lthBranch-1).append(": ");

            return instCode.toString();
        }

        instCode.append(convertTypeToInst(operands.get(0).getType().getTypeOfElement()));
        String opStr = "";
        switch(instruction.getUnaryOperation().getOpType()) {
            case ADD -> opStr = "add";
            case SUB -> opStr = "sub";
            case MUL -> opStr = "mul";
            case DIV -> opStr = "div";
//            case ANDB -> opStr = "and";
//            case NOTB -> opStr = "neg";
        }
        instCode.append(opStr).append("\n");
        return instCode.toString();
    }

    private String generateBranch(Method method, CondBranchInstruction instruction) {
        currentInstructionLimit++;
        StringBuilder instCode = new StringBuilder();

        instCode.append(getElement(method, instruction.getLeftOperand()));

        String firstLabel = processLabelName(instruction.getLabel());

        OperationType operationType = instruction.getCondOperation().getOpType();

        //'not' operation only needs one of the operands. 'and' operation will only check the left first and then right
        if(operationType != OperationType.NOTB && operationType != OperationType.ANDB && operationType != OperationType.OR) {
            instCode.append(getElement(method, instruction.getRightOperand()));
        }

        switch(operationType) {
            case ANDB -> {
                instCode.append("ifeq ").append(firstLabel).append("\n");
                instCode.append(getElement(method, instruction.getRightOperand()));
                instCode.append("ifeq ").append(firstLabel).append("\n");
            }
            case OR -> {
            }
            case LTH -> {
                instCode.append("if_icmpge ").append(firstLabel).append("\n");
            }
            case NOTB -> {
                instCode.append("ifne ").append(firstLabel).append("\n");
            }
        }
//        instCode.append(" ").append(processLabelName(instruction.getLabel()));

        return instCode.toString();
    }

    private String generateGoto(Method method, GotoInstruction instruction) {
        return "goto " + instruction.getLabel() + "\n";
    }

    /*Making sure labels have not the same name as Jasmin generated labels (not and lth)*/
    static String processLabelName(String labelName) {
        labelName.replaceAll("_", "__");
        if(labelName.matches("^-?\\d+$")) //label name is integer (can be the same name as a generated label, so we add a character)
            labelName += "_";
        return labelName;
    }

    private String getElement(Method method, Element operand) {
        boolean isArray = operand instanceof ArrayOperand;

        StringBuilder instCode = new StringBuilder();
        ElementType elementType = operand.getType().getTypeOfElement();
        if(operand.isLiteral()) { //if literal, create a const instruction
            int value = Integer.parseInt(((LiteralElement) operand).getLiteral());
            instCode.append(createConstInst(value));
        } else { //if its a variable, load with its virtual reg
            if(operand.getType().getTypeOfElement() != ElementType.CLASS) {

                if(isArray) {
                    currentInstructionLimit++;
                    instCode.append(createLoadInst(elementType, isArray, getVirtualReg(method, (Operand) operand))).append("\n");
                    instCode.append(getElement(method, ((ArrayOperand) operand).getIndexOperands().get(0))).append("\n");
                    instCode.append(convertTypeToInst(elementType)).append("aload\n");
                } else {
                    instCode.append(createLoadInst(elementType, isArray, getVirtualReg(method, (Operand) operand)));
                }
            }
        }
        instCode.append("\n");
        return instCode.toString();
    }

    // to be used in assignments with array
    private String getElementArrayAssign(Method method, Element operand) {
        StringBuilder instCode = new StringBuilder();
        ElementType elementType = operand.getType().getTypeOfElement();

        instCode.append(createLoadInst(elementType, true, getVirtualReg(method, (Operand) operand))).append("\n");
        instCode.append(getElement(method, ((ArrayOperand) operand).getIndexOperands().get(0)));

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

    private String createStoreInst(ElementType type, boolean isArray, int virtualReg) {
        StringBuilder storeInst = new StringBuilder();
        storeInst.append(convertTypeToInst(type)).append(isArray ? "a" : "").append("store");

        if(!isArray) {
            if (virtualReg <= 3) { // istore_1
                storeInst.append("_");
            } else storeInst.append(" "); //istore 6

            storeInst.append(virtualReg);
        }
        return storeInst.toString();
    }

    private String createLoadInst(ElementType type, boolean isArray, int virtualReg) { //TODO: repeated code - see createStoreInst
        StringBuilder storeInst = new StringBuilder();
        storeInst.append(isArray ? "a" : convertTypeToInst(type)).append("load");

        if (virtualReg <= 3) { // iload_1
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
