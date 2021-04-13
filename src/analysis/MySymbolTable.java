package analysis;

import pt.up.fe.comp.jmm.analysis.Method;
import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.Type;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MySymbolTable implements SymbolTable {

    private final List<Symbol> fields;
    private final List<String> imports, methodNames;
    private final Map<String, Method> methods;
    private String superClass = null;
    private String className = null;

    public MySymbolTable() {
        this.fields = new ArrayList<>();
        this.imports = new ArrayList<>();
        this.methodNames = new ArrayList<>();
        this.methods = new HashMap<>();
    }

    public void setSuperClass(String superClass) {
        this.superClass = superClass;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public void addField(Symbol symbol) {
        this.fields.add(symbol);
    }

    public void addImport(String importName) {
        this.imports.add(importName);
    }

    public void addMethod(Method method) {
        this.methodNames.add(method.getName());
        this.methods.put(method.getName(), method);
    }

    public Method getMethod(String name) {
        return this.methods.get(name);
    }

    @Override
    public List<String> getImports() {
        return imports;
    }

    @Override
    public String getClassName() {
        return className;
    }

    @Override
    public String getSuper() {
        return superClass;
    }

    @Override
    public List<Symbol> getFields() {
        return fields;
    }

    @Override
    public List<String> getMethods() {
        return methodNames;
    }

    @Override
    public Type getReturnType(String methodName) {
        Method m = methods.get(methodName);
        return m == null ? null : m.getReturnType();
    }

    @Override
    public List<Symbol> getParameters(String methodName) {
        Method m = methods.get(methodName);
        return m == null ? null : m.getParameters();
    }

    @Override
    public List<Symbol> getLocalVariables(String methodName) {
        Method m = methods.get(methodName);
        return m == null ? null : m.getLocalVariables();
    }

    @Override
    public String toString() {
        StringBuilder fieldsString = new StringBuilder();
        for (Symbol s : fields) {
            fieldsString.append("\t").append(s).append("\n");
        }
        StringBuilder methodsString = new StringBuilder();
        for (Method s : methods.values()) {
            methodsString.append("\t").append(s).append("\n");
        }
        return  "className='" + className + "'\n" +
                "superClass='" + superClass + "'\n" +
                "imports=" + imports + "\n" +
                "fields=[\n" + fieldsString + "]\n" +
                "methods=[\n" + methodsString +
                "]\n}";
    }
}
