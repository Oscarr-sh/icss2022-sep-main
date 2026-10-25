package nl.han.ica.icss.checker;

import nl.han.ica.datastructures.HANLinkedList;
import nl.han.ica.datastructures.IHANLinkedList;
import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.literals.*;
import nl.han.ica.icss.ast.operations.MultiplyOperation;
import nl.han.ica.icss.ast.types.ExpressionType;

import java.util.HashMap;


public class Checker {

    private IHANLinkedList<HashMap<String, ExpressionType>> variableTypes;

    public void check(AST ast) {
        variableTypes = new HANLinkedList<>();
        checkStylesheet(ast.root);

    }

    private void checkStylesheet(Stylesheet stylesheet) {
        variableTypes.addFirst(new HashMap<>());

        for (ASTNode child : stylesheet.body) {
            if (child instanceof Stylerule) {
                checkStylerule((Stylerule) child);
            } else if (child instanceof VariableAssignment) {
                checkVariableAssignment((VariableAssignment) child);
            }
        }
        variableTypes.removeFirst();
    }

    private void checkStylerule(Stylerule stylerule) {
        variableTypes.addFirst(new HashMap<>());

        for (ASTNode child : stylerule.body) {
            if (child instanceof Declaration) {
                checkDeclaration((Declaration) child);
            } else if (child instanceof IfClause) {
                checkIfClause((IfClause) child);
            } else if (child instanceof VariableAssignment) {
                checkVariableAssignment((VariableAssignment) child);
            }
        }
        variableTypes.removeFirst();
    }

    private void checkDeclaration(Declaration declaration) {
        ExpressionType type = getExpressionType(declaration.expression);
        String property = declaration.property.name;

        if (type == ExpressionType.UNDEFINED) {
            return;
        }

        if (property.equals("width") || property.equals("height")) {
            if (type != ExpressionType.PIXEL && type != ExpressionType.PERCENTAGE) {
                declaration.setError("Property " + property + " moet een pixel of percentagewaarde hebben");
            }
        } else if (property.equals("color") || property.equals("background-color")) {
            if (type != ExpressionType.COLOR) {
                declaration.setError("Property " + property + " moet een color zijn");
            }
        } else {
            declaration.setError("Property " + property + " is undefined");
        }
    }

    private void checkIfClause(IfClause ifClause) {
        ExpressionType conditionType = getExpressionType(ifClause.conditionalExpression);
        if (conditionType != ExpressionType.BOOL && conditionType != ExpressionType.UNDEFINED) {
            ifClause.setError("De conditie van een if moet een boolean zijn");
        }

        variableTypes.addFirst(new HashMap<>());
        for (ASTNode child : ifClause.body) {
            if (child instanceof IfClause) {
                checkIfClause((IfClause) child);
            } else if (child instanceof VariableAssignment) {
                checkVariableAssignment((VariableAssignment) child);
            } else if (child instanceof Declaration) {
                checkDeclaration((Declaration) child);
            }
        }
        variableTypes.removeFirst();

        if (ifClause.elseClause != null) {
            checkElseClause(ifClause.elseClause);
        }
    }

    private void checkElseClause(ElseClause elseClause) {
        variableTypes.addFirst(new HashMap<>());
        for (ASTNode child : elseClause.body) {
            if (child instanceof VariableAssignment) {
                checkVariableAssignment((VariableAssignment) child);
            } else if (child instanceof Declaration) {
                checkDeclaration((Declaration) child);
            } else if (child instanceof IfClause) {
                checkIfClause((IfClause) child);
            }
        }
        variableTypes.removeFirst();
    }

    private void checkVariableAssignment(VariableAssignment variableAssignment) {
        String naam = variableAssignment.name.name;
        ExpressionType type = getExpressionType(variableAssignment.expression);
        variableTypes.getFirst().put(naam, type);
    }

    private ExpressionType getExpressionType(Expression expression) {
        if (expression instanceof PixelLiteral) {
            return ExpressionType.PIXEL;
        } else if (expression instanceof PercentageLiteral) {
            return ExpressionType.PERCENTAGE;
        } else if (expression instanceof ColorLiteral) {
            return ExpressionType.COLOR;
        } else if (expression instanceof ScalarLiteral) {
            return ExpressionType.SCALAR;
        } else if (expression instanceof BoolLiteral) {
            return ExpressionType.BOOL;
        } else if (expression instanceof VariableReference) {
            String name = ((VariableReference) expression).name;
            ExpressionType type = findVariableType(name);
            if (type == null) {
                expression.setError("Variabele " + name + " is niet gedefinieerd");
                return ExpressionType.UNDEFINED;
            }
            return type;
        } else if (expression instanceof Operation) {
            return handleOperations((Operation) expression);
        }
        return ExpressionType.UNDEFINED;
    }

    private ExpressionType handleOperations(Operation operation) {
        ExpressionType left = getExpressionType(operation.lhs);
        ExpressionType right = getExpressionType(operation.rhs);

        if (left == ExpressionType.COLOR || right == ExpressionType.COLOR) {
            operation.setError("Een kleur mag niet gebruikt worden in een som");
            return ExpressionType.UNDEFINED;
        }
        if (left == ExpressionType.UNDEFINED || right == ExpressionType.UNDEFINED) {
            return ExpressionType.UNDEFINED;
        }
        if (operation instanceof MultiplyOperation) {
            if (left == ExpressionType.SCALAR) {
                return right;
            } else if (right == ExpressionType.SCALAR) {
                return left;
            } else {
                operation.setError("Bij een keer som mag maar een kant van de som een eenheid zijn");
                return ExpressionType.UNDEFINED;
            }
        }

        if (left == right) {
            return left;
        }
        operation.setError("Bij een plus of min som moeten beide kanten hetzelfde type hebben");
        return ExpressionType.UNDEFINED;
    }

    private ExpressionType findVariableType(String name) {
        for (int i = 0; i < variableTypes.getSize(); i++) {
            ExpressionType type = variableTypes.get(i).get(name);
            if (type != null) {
                return type;
            }
        }
        return null;
    }


}
