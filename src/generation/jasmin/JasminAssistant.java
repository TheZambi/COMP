package generation.jasmin;

import org.specs.comp.ollir.*;

import java.util.ArrayList;
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

            }
            case NOPER -> {
                
            }
        }
>>>>>>> 08d77568b663c1f0cee05ca969b157f14e1dcf02
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
                return "Ljava/lang/String";
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
