package generation.ollir.visitors;

import ast.AstUtils;
import ast.Method;
import ast.MySymbolTable;
import pt.up.fe.comp.jmm.JmmNode;
import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.ast.JmmNodeImpl;
import pt.up.fe.specs.util.SpecsCheck;

import java.util.*;
import java.util.function.Consumer;

public class UnusedVarsVisitor {

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

        @Override
        public String toString() {
            return "VarHolder{" +
                    "initStatus=" + initStatus +
                    ", used=" + used +
                    '}';
        }
    }

    private final MySymbolTable symbolTable;

    private final Map<String, Consumer<JmmNode>> visitMap, previsitMap;

    private Map<String, VarHolder> initedVars;
    private List<JmmNode> nodesToInit;

    public UnusedVarsVisitor(MySymbolTable symbolTable) {
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
            // or it being used in a method call
            if (AstUtils.hasMethodCall(ancestor)
                || ancestor.getChildren().get(0) != node && AstUtils.isAssignment(ancestor)) {
                holder.used = true;
                return;
            }
        } else {
            holder.used = true;
            return;
        }

        switch (holder.initStatus) {
            case UNINITIALIZED:
                this.nodesToInit.add(node);
                holder.initStatus = InitStatus.INITIALIZED;
                break;
            case CONDITIONAL:
//                this.nodesToInit.add(node);
//                holder.initStatus = InitStatus.INITIALIZED;
                break;
            default:
                break;
        }
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

        DeleteUnusedVarVisitor deleteUnusedVarVisitor = new DeleteUnusedVarVisitor();
        // Remove unused vars
        for (Map.Entry<String, VarHolder> entry : initedVars.entrySet()) {
            if (!entry.getValue().used) {
                deleteUnusedVarVisitor.deleteVar(m, entry.getKey(), node, entry.getValue().node);
//                System.out.println("Deleted " + entry.getKey() + " from method " + m.getName());
            }
        }
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
