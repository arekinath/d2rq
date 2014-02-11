package de.fuberlin.wiwiss.d2rq.expr;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import de.fuberlin.wiwiss.d2rq.algebra.AliasMap;
import de.fuberlin.wiwiss.d2rq.algebra.Attribute;
import de.fuberlin.wiwiss.d2rq.algebra.ColumnRenamer;
import de.fuberlin.wiwiss.d2rq.sql.ConnectedDB;

public class Regexp extends Expression {
	private final Expression target;
	private final String regexp;
	private final String options;

	public class InvalidRegexpException extends Error
	{
		public InvalidRegexpException() {
			super("Invalid regular expression");
		}
	}

	public Regexp(Expression target, String regexp, String options) {
		this.target = target;
		this.regexp = regexp;
		if (regexp == null)
			throw new InvalidRegexpException();
		this.options = options;
	}

	public Set<Attribute> attributes() {
		return this.target.attributes();
	}

	public boolean isFalse() {
		return false;
	}

	public boolean isTrue() {
		return false;
	}

	public Expression renameAttributes(ColumnRenamer columnRenamer) {
		return new Regexp(target.renameAttributes(columnRenamer), regexp, options);
	}

	public String toSQL(ConnectedDB database, AliasMap aliases) {
		String fragment = target.toSQL(database, aliases);
		return database.vendor().getRegexExpression(fragment, regexp, options);
	}

	public boolean equals(Object other) {
		if (other == null) return false;
		if (!(other instanceof Regexp)) return false;
		Regexp othrx = (Regexp)other;

		if (target == null && othrx.target != null) return false;
		if (target != null && !target.equals(othrx.target)) return false;

		if (options == null && othrx.options != null) return false;
		if (options != null && !options.equals(othrx.options)) return false;

		if (!regexp.equals(othrx.regexp)) return false;

		return true;
	}

	public int hashCode() {
		return target.hashCode() ^ 234645;
	}

	public String toString() {
		StringBuffer result = new StringBuffer("Regexp(");
		result.append(target);
		result.append(", /");
		result.append(regexp);
		result.append("/");
		result.append(options);
		result.append(")");
		return result.toString();
	}
}
