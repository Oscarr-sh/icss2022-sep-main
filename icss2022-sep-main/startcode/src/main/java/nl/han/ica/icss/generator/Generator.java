package nl.han.ica.icss.generator;


import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.literals.ColorLiteral;
import nl.han.ica.icss.ast.literals.PercentageLiteral;
import nl.han.ica.icss.ast.literals.PixelLiteral;

public class Generator {

	public String generate(AST ast) {
        return generateStylesheet(ast.root);
	}
	private String generateStylesheet(Stylesheet stylesheet) {
		String css = "";
		for(ASTNode child : stylesheet.body){
			css += generateStylerule((Stylerule) child);
		}
		return css;
	}

	private String generateStylerule(Stylerule stylerule) {
		String css = stylerule.selectors.get(0) + " {\n";
		for(ASTNode child : stylerule.body){
			css += generateDeclaration((Declaration) child);
		}
		css += "}\n\n";
		return css;
	}

	private String generateDeclaration(Declaration declaration) {
		String css = "";
		css += "  " + declaration.property.name + ": " + generateLiteral(declaration.expression) + ";\n";
		return css;
	}

	private String generateLiteral(Expression expression) {
		String css = "";
		if(expression instanceof PixelLiteral){
			return ((PixelLiteral) expression).value + "px";
		} else if(expression instanceof PercentageLiteral){
			return ((PercentageLiteral) expression).value + "%";
		} else if(expression instanceof ColorLiteral){
			return ((ColorLiteral) expression).value;
		}
		return css;
	}



	
}
