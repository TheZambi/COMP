package analysis.visitors;

import ast.AstUtils;
import ast.Method;
import ast.MySymbolTable;
import pt.up.fe.comp.jmm.JmmNode;
import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNodeImpl;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.ReportType;
import pt.up.fe.comp.jmm.report.Stage;
import pt.up.fe.specs.util.SpecsCheck;

import java.util.*;
import java.util.function.Consumer;

public class InitedVarsVisitor {

    private enum InitStatus {
        UNINITIALIZED,
        CONDITIONAL,
        INITIALIZED
    }

    private class VarHolder {
        public InitStatus initStatus;
        public boolean used = false;
        public JmmNodeImpl node;

        public VarHolder(InitStatus initStatus, JmmNodeImpl node) {
            this.initStatus = initStatus;
            this.node = node;
        }
    }


    private final Map<String, Consumer<JmmNode>> visitMap, previsitMap;

    private final List<Report> reports;

    private final MySymbolTable symbolTable;

    private Map<String, VarHolder> initedVars;
    private List<JmmNode> nodesToInit;

    public InitedVarsVisitor(MySymbolTable symbolTable, List<Report> reports) {
        this.reports = reports;

        this.symbolTable = symbolTable;
        this.visitMap = new HashMap<>();
        this.visitMap.put("Value", this::valueVisit);
        this.visitMap.put("VarDeclaration", this::varDeclarationVisit);
        this.visitMap.put("Statement", this::assignmentVisit);
        this.visitMap.put("MethodDeclaration", this::methodDeclarationVisit);

        this.previsitMap = new HashMap<>();
        this.previsitMap.put("MethodDeclaration", this::methodDeclarationPreVisit);

    }

    private void valueVisit(JmmNode node) {
        if (!node.get("type").equals("object"))
            return;

        String name = node.get("object");
        VarHolder holder = this.initedVars.get(name);
        if (holder == null)
            return;

        Optional<JmmNode> ancestorOpt = node.getAncestor("Statement");
        if (ancestorOpt.isPresent()) {
            JmmNode ancestor = ancestorOpt.get();
            // Skip when this is the value being attributed
            if (AstUtils.isAssignment(ancestor) && ancestor.getChildren().get(0) == node) {
                holder.used = true;
                return;
            }
        }

        switch (holder.initStatus) {
            case UNINITIALIZED:
                this.nodesToInit.add(node);
                holder.initStatus = InitStatus.INITIALIZED;
                reports.add(new Report(ReportType.WARNING, Stage.SEMANTIC, Integer.parseInt(node.get("line")), Integer.parseInt(node.get("col")),
                        "Local variable <" + name + "> was not initialized: Initialized it with a default value"));
                break;
            case CONDITIONAL:
//                this.nodesToInit.add(node);
//                holder.initStatus = InitStatus.INITIALIZED;
//                reports.add(new Report(ReportType.WARNING, Stage.SEMANTIC, Integer.parseInt(node.get("line")), Integer.parseInt(node.get("col")),
//                        "Local variable <" + name + "> may not have been initialized: Initialized with default value ''"));
                break;
            default:
                break;
        }
    }

    private String addInit(JmmNode node) {
        String ret = "";
        JmmNodeImpl methodNode = (JmmNodeImpl) node.getAncestor("MethodDeclaration").get();
        Method method = symbolTable.getMethod(methodNode.get("uniqueName"));
        String varName = node.get("object");
        Type varType = null;
        List<Symbol> localVariables = method.getLocalVariables();

        for (Symbol s: localVariables) {
            if (s.getName().equals(varName)) {
                varType = s.getType();
                break;
            }
        }

        methodNode = (JmmNodeImpl) methodNode.getChildren().get(1);
        int assignmentNodeIndex = 0;
        for (int i = 0; i < methodNode.getNumChildren(); i++) {
            JmmNode child = methodNode.getChildren().get(i);
            if (!child.getKind().equals("VarDeclaration")) {
                assignmentNodeIndex = i;
                break;
            }
        }

        JmmNodeImpl assignment = new JmmNodeImpl("Statement");
        assignment.put("col", "-1");
        assignment.put("line", "-1");
        methodNode.add(assignment, assignmentNodeIndex);

        JmmNodeImpl var = new JmmNodeImpl("Value");
        var.put("col", "-1");
        var.put("line", "-1");
        var.put("type", node.get("type"));
        var.put("object", node.get("object"));
        assignment.add(var);

        if (varType.isArray()) {
            JmmNodeImpl unaryOp = new JmmNodeImpl("UnaryOp");
            unaryOp.put("col", "-1");
            unaryOp.put("line", "-1");
            unaryOp.put("op", "NEW");
            assignment.add(unaryOp);

            JmmNodeImpl array = new JmmNodeImpl("Array");
            array.put("col", "-1");
            array.put("line", "-1");
            unaryOp.add(array);

            JmmNodeImpl arraySize = new JmmNodeImpl("Value");
            arraySize.put("col", "-1");
            arraySize.put("line", "-1");
            arraySize.put("type", "int");
            arraySize.put("object", "0");
            array.add(arraySize);

            ret = "new int[0]";
        } else {
            JmmNodeImpl init = new JmmNodeImpl("Value");
            init.put("col", "-1");
            init.put("line", "-1");

            if (varType.getName().equals("boolean")) {
                init.put("type", "boolean");
                init.put("object", "false");
                ret = "false";
            } else if (varType.getName().equals("int")) {
                init.put("type", "int");
                init.put("object", "0");
                ret = "0";
            } else {
                System.err.println("We do not initialize classes");
            }
            assignment.add(init);
        }

        return ret;
    }

    private void varDeclarationVisit(JmmNode node) {
        Symbol s = AstUtils.getChildSymbol(node, 0);
        if (this.initedVars != null)
            this.initedVars.put(s.getName(), new VarHolder(InitStatus.UNINITIALIZED, (JmmNodeImpl) node));
    }

    private void methodDeclarationPreVisit(JmmNode node) {
        this.initedVars = new HashMap<>();
        this.nodesToInit = new ArrayList<>();
    }

    private void methodDeclarationVisit(JmmNode node) {
        Method m = symbolTable.getMethod(node.get("uniqueName"));

        // Init needed vars
        for (JmmNode n : this.nodesToInit)
            addInit(n);

//        // Remove unused vars
//        for (Map.Entry<String, VarHolder> entry : initedVars.entrySet()) {
//            if (!entry.getValue().used) {
//                m.removeLocalVar(entry.getKey());
//                JmmNodeImpl parent = (JmmNodeImpl) entry.getValue().node.getParent();
//                parent.removeChild(entry.getValue().node);
//                reports.add(new Report(ReportType.WARNING, Stage.SEMANTIC, Integer.parseInt(node.get("line")), Integer.parseInt(node.get("col")),
//                        "Local variable <" + entry.getKey() + "> is declared but never used."));
//            }
//        }
    }

    private void assignmentVisit(JmmNode node) {
        if (!AstUtils.isAssignment(node))
            return;

        JmmNode firstChild = node.getChildren().get(0);
        if (firstChild.getKind().equals("Indexing"))
            return;

        String name = firstChild.get("object");
        boolean isConditional = AstUtils.isInsideConditionalBranch(node);

        VarHolder holder = this.initedVars.get(name);
        if (holder == null)
            return;

        if (holder.initStatus == InitStatus.UNINITIALIZED)
            holder.initStatus = isConditional ? InitStatus.CONDITIONAL : InitStatus.INITIALIZED;
        else if (holder.initStatus == InitStatus.CONDITIONAL && !isConditional)
            holder.initStatus = InitStatus.INITIALIZED;
    }

    public void visit(JmmNode node) {
        SpecsCheck.checkNotNull(node, () -> "Node should not be null");

        Consumer<JmmNode> visit = this.visitMap.get(node.getKind());
        Consumer<JmmNode> preVisit = this.previsitMap.get(node.getKind());

        if (preVisit != null)
            preVisit.accept(node);

        // Preorder: 1st visit each children
        for (var child : node.getChildren())
            visit(child);

        // Preorder: then visit the node
        if (visit != null)
            visit.accept(node);
    }

}
