package de.fuberlin.wiwiss.d2rq.algebra;

import java.util.Set;
import java.util.Iterator;

import de.fuberlin.wiwiss.d2rq.expr.Expression;
import de.fuberlin.wiwiss.d2rq.D2RQException;
import de.fuberlin.wiwiss.d2rq.expr.NotNull;
import de.fuberlin.wiwiss.d2rq.algebra.Attribute;
import de.fuberlin.wiwiss.d2rq.sql.ConnectedDB;

public class ExpressionProjectionSpec implements ProjectionSpec {
	private Expression expression;
	private final String name;
	
	public ExpressionProjectionSpec(Expression expression) {
		this.expression = expression;
		this.name = "expr" + Integer.toHexString(expression.hashCode());
	}
	
	public ProjectionSpec renameAttributes(ColumnRenamer renamer) {
		return new ExpressionProjectionSpec(renamer.applyTo(expression));
	}

	public Set<Attribute> requiredAttributes() {
		return expression.attributes();
	}

	public Expression toExpression() {
		return expression;
	}

	public String toSQL(ConnectedDB database, AliasMap aliases) {
		return expression.toSQL(database, aliases) + " AS " + name;
	}
	
	public Expression notNullExpression(ConnectedDB database, AliasMap aliases) {
		return NotNull.create(expression);
	}
	
	public boolean equals(Object other) {
		return (other instanceof ExpressionProjectionSpec) 
				&& expression.equals(((ExpressionProjectionSpec) other).expression);
	}
	
	public int hashCode() {
		return expression.hashCode() ^ 684036;
	}

	public String toString() {
		return "ProjectionSpec(" + expression + " AS " + name + ")";
	}

	public boolean hasAccess(String apiKey, AliasMap aliases) {
		Set<Attribute> attrs = expression.attributes();
		Iterator<Attribute> it = attrs.iterator();
		while (it.hasNext()) {
			Attribute at = it.next();
			if (!at.hasAccess(apiKey, aliases)) {
				return false;
			}
		}
		return true;
	}

	public void checkAccess(String apiKey, AliasMap aliases) {
		if (!hasAccess(apiKey, aliases)) {
			throw new D2RQException("Access denied to column");
		}
	}

	/**
	 * Compares columns alphanumerically by qualified name, case sensitive.
	 * Attributes with schema are larger than attributes without schema.
	 */
	public int compareTo(ProjectionSpec other) {
		if (!(other instanceof ExpressionProjectionSpec)) {
			return 1;
		}
		ExpressionProjectionSpec otherExpr = (ExpressionProjectionSpec) other;
		return this.name.compareTo(otherExpr.name);
	}
}