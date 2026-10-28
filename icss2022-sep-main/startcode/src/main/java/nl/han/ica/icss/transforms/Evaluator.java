package nl.han.ica.icss.transforms;

import nl.han.ica.datastructures.HANLinkedList;
import nl.han.ica.datastructures.IHANLinkedList;
import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.literals.BoolLiteral;
import nl.han.ica.icss.ast.literals.PercentageLiteral;
import nl.han.ica.icss.ast.literals.PixelLiteral;
import nl.han.ica.icss.ast.literals.ScalarLiteral;
import nl.han.ica.icss.ast.operations.AddOperation;
import nl.han.ica.icss.ast.operations.SubtractOperation;


import java.util.ArrayList;
import java.util.HashMap;

public class Evaluator implements Transform {

    private IHANLinkedList<HashMap<String, Literal>> variableValues;

    public Evaluator() {
    }

    @Override
    public void apply(AST ast) {
        variableValues = new HANLinkedList<>();
        evalStylesheet(ast.root);

    }

    private void evalStylesheet(Stylesheet stylesheet) {
        variableValues.addFirst(new HashMap<>());
        ArrayList<ASTNode> newBody = new ArrayList<>();

        for (ASTNode child : stylesheet.body) {
            if (child instanceof VariableAssignment) {
                evalVariableAssignment((VariableAssignment) child);
            } else if (child instanceof Stylerule) {
                evalStylerule((Stylerule) child);
                newBody.add(child);
            }
        }
        stylesheet.body = newBody;
        variableValues.removeFirst();
    }

    private void evalVariableAssignment(VariableAssignment variableAssignment) {
        String name = variableAssignment.name.name;
        Literal value = evalExpression(variableAssignment.expression);

        //Nodig om variabele te overschrijven. Zonder deze for loop komt er 0 uit wanneer je het voorbeeld uit de casus test.
        for (int i = 0; i < variableValues.getSize(); i++) {
            if (variableValues.get(i).containsKey(name)) {
                variableValues.get(i).put(name, value);
                return;
            }
        }
        variableValues.getFirst().put(name, value);
    }

    private void evalStylerule(Stylerule stylerule) {
        stylerule.body = evalBody(stylerule.body);
    }

    private Literal evalExpression(Expression expression) {
        if (expression instanceof Literal) {
            return (Literal) expression;
        } else if (expression instanceof VariableReference) {
            String name = ((VariableReference) expression).name;
            Literal value = findVariableValue(name);
            return value;
        } else if (expression instanceof Operation) {
            return evalOperations((Operation) expression);
        }
        return null;
    }

    private void evalDeclaration(Declaration declaration) {
        Literal value = evalExpression(declaration.expression);
        declaration.expression = value;
    }

    private Literal findVariableValue(String name) {
        for (int i = 0; i < variableValues.getSize(); i++) {
            Literal value = variableValues.get(i).get(name);
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    private ArrayList<ASTNode> evalIfClause(IfClause ifClause) {
        ArrayList<ASTNode> newBody = new ArrayList<>();
        BoolLiteral condition = (BoolLiteral) evalExpression(ifClause.conditionalExpression);
        if (condition.value) {
            newBody.addAll(evalBody(ifClause.body));
        } else if (ifClause.elseClause != null) {
            newBody.addAll(evalBody(ifClause.elseClause.body));
        }
        return newBody;
    }

    private ArrayList<ASTNode> evalBody(ArrayList<ASTNode> body) {
        variableValues.addFirst(new HashMap<>());
        ArrayList<ASTNode> newBody = new ArrayList<>();
        for (ASTNode child : body) {
            if (child instanceof VariableAssignment) {
                evalVariableAssignment((VariableAssignment) child);
            } else if (child instanceof Declaration) {
                evalDeclaration((Declaration) child);
                newBody.add(child);
            } else if (child instanceof IfClause) {
                newBody.addAll(evalIfClause((IfClause) child));
            }
        }
        variableValues.removeFirst();
        return newBody;
    }

    private int getValue(Literal literal) {
        if (literal instanceof PixelLiteral) {
            return ((PixelLiteral) literal).value;
        } else if (literal instanceof PercentageLiteral) {
            return ((PercentageLiteral) literal).value;
        } else if (literal instanceof ScalarLiteral) {
            return ((ScalarLiteral) literal).value;
        }
        return 0;
    }

    private Literal evalOperations(Operation operation) {
        Literal left = evalExpression(operation.lhs);
        Literal right = evalExpression(operation.rhs);
        int leftValue = getValue(left);
        int rightValue = getValue(right);

        if (operation instanceof AddOperation) {
            return makeSolvedOperationLiteral(left, leftValue + rightValue);
        } else if (operation instanceof SubtractOperation) {
            return makeSolvedOperationLiteral(left, leftValue - rightValue);
        } else {
            if (left instanceof ScalarLiteral) {
                return makeSolvedOperationLiteral(right, leftValue * rightValue);
            }
            return makeSolvedOperationLiteral(left, leftValue * rightValue);
        }
    }

    private Literal makeSolvedOperationLiteral(Literal expression, int value) {
        if (expression instanceof PixelLiteral) {
            return new PixelLiteral(value);
        } else if (expression instanceof PercentageLiteral) {
            return new PercentageLiteral(value);
        } else if (expression instanceof ScalarLiteral) {
            return new ScalarLiteral(value);
        }
        return null;
    }


}
