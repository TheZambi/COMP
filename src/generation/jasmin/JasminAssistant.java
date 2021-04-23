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
        code.append(".method ").append(method.getMethodName()).append("()").append(method.getReturnType().toString());
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
                return "";
            }
            default -> {return "";}
        }
    }

    public String getCode() {
        return code.toString();
    }
}
