package nl.han.ica.icss.parser;

import java.util.Stack;


import com.sun.jdi.Value;
import nl.han.ica.datastructures.HANStack;
import nl.han.ica.datastructures.IHANStack;
import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.literals.*;
import nl.han.ica.icss.ast.operations.AddOperation;
import nl.han.ica.icss.ast.operations.MultiplyOperation;
import nl.han.ica.icss.ast.operations.SubtractOperation;
import nl.han.ica.icss.ast.selectors.ClassSelector;
import nl.han.ica.icss.ast.selectors.IdSelector;
import nl.han.ica.icss.ast.selectors.TagSelector;

/**
 * This class extracts the ICSS Abstract Syntax Tree from the Antlr Parse tree.
 */
public class ASTListener extends ICSSBaseListener {
	
	//Accumulator attributes:
	private AST ast;

	//Use this to keep track of the parent nodes when recursively traversing the ast
	private IHANStack<ASTNode> currentContainer;

	public ASTListener() {
		ast = new AST();
		currentContainer = new HANStack<>();
	}
    public AST getAST() {
        return ast;
    }

	@Override
	public void enterStylesheet(ICSSParser.StylesheetContext ctx) {
		ASTNode stylesheet = new Stylesheet();
		currentContainer.push(stylesheet);
	}

	@Override
	public void exitStylesheet(ICSSParser.StylesheetContext ctx) {
		Stylesheet stylesheet = (Stylesheet) currentContainer.pop();
		ast.root = stylesheet;
	}

	@Override
	public void enterStylerule(ICSSParser.StyleruleContext ctx) {
		ASTNode stylerule = new Stylerule();
		currentContainer.push(stylerule);
	}

	@Override
	public void exitStylerule(ICSSParser.StyleruleContext ctx) {
		Stylerule stylerule = (Stylerule) currentContainer.pop();
		currentContainer.peek().addChild(stylerule);
	}

	@Override
	public void enterSelector(ICSSParser.SelectorContext ctx) {
		ASTNode selector;
		if(ctx.LOWER_IDENT() != null) {
			selector = new TagSelector(ctx.getText());
		} else if (ctx.ID_IDENT() != null) {
			selector = new IdSelector(ctx.getText());
		} else {
			selector = new ClassSelector(ctx.getText());
		}
		currentContainer.push(selector);
	}

	@Override
	public void exitSelector(ICSSParser.SelectorContext ctx) {
		Selector selector = (Selector) currentContainer.pop();
		currentContainer.peek().addChild(selector);
	}

	@Override
	public void enterDeclaration(ICSSParser.DeclarationContext ctx) {
		ASTNode declaration = new Declaration();
		currentContainer.push(declaration);
	}

	@Override
	public void exitDeclaration(ICSSParser.DeclarationContext ctx) {
		Declaration declaration = (Declaration) currentContainer.pop();
		currentContainer.peek().addChild(declaration);
	}

	@Override
	public void enterPropertyName(ICSSParser.PropertyNameContext ctx) {
		ASTNode propertyName = new PropertyName(ctx.getText());
		currentContainer.push(propertyName);
	}

	@Override
	public void exitPropertyName(ICSSParser.PropertyNameContext ctx) {
		PropertyName propertyName = (PropertyName) currentContainer.pop();
		currentContainer.peek().addChild(propertyName);
	}

	@Override
	public void enterValue(ICSSParser.ValueContext ctx) {
		ASTNode value;
		if(ctx.COLOR() != null) {
			value = new ColorLiteral(ctx.getText());
		} else if (ctx.PIXELSIZE() != null) {
			value = new PixelLiteral(ctx.getText());
		} else if (ctx.PERCENTAGE() != null) {
			value = new PercentageLiteral(ctx.getText());
		} else if (ctx.variableReference() != null) {
			return;
		} else if (ctx.TRUE() != null) {
			value = new BoolLiteral(ctx.getText());
		} else if (ctx.FALSE() != null) {
			value = new BoolLiteral(ctx.getText());
		} else {
			value = new ScalarLiteral(ctx.getText());
		}
		currentContainer.push(value);
	}

	@Override
	public void exitValue(ICSSParser.ValueContext ctx) {
		if (ctx.variableReference() != null) {
			return;
		}
		ASTNode value = currentContainer.pop();
		currentContainer.peek().addChild(value);
	}

	@Override
	public void enterVariableAssignment(ICSSParser.VariableAssignmentContext ctx) {
		ASTNode variableAssignment = new VariableAssignment();
		currentContainer.push(variableAssignment);
	}

	@Override
	public void exitVariableAssignment(ICSSParser.VariableAssignmentContext ctx) {
		VariableAssignment variableAssignment = (VariableAssignment) currentContainer.pop();
		currentContainer.peek().addChild(variableAssignment);
	}

	@Override
	public void enterVariableReference(ICSSParser.VariableReferenceContext ctx) {
		ASTNode variableReference = new VariableReference(ctx.getText());
		currentContainer.push(variableReference);
	}

	@Override
	public void exitVariableReference(ICSSParser.VariableReferenceContext ctx) {
		VariableReference variableReference = (VariableReference) currentContainer.pop();
		currentContainer.peek().addChild(variableReference);
	}


}

