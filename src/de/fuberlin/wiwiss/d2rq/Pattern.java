/*
 * $Id: Pattern.java,v 1.1 2004/08/02 22:48:44 cyganiak Exp $
 */
package de.fuberlin.wiwiss.d2rq;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A pattern that combines one or more database columns into a String. Often
 * used as an UriPattern for generating URIs from a column's primary key.
 *
 * @author Richard Cyganiak <richard@cyganiak.de>
 */
class Pattern implements ValueSource {
	private String pattern;
	private String firstLiteralPart = null;
	private List columns = new ArrayList(3);
	private List literalParts = new ArrayList(3);
	private Set columnsAsSet;

	/**
	 * Constructs a new Pattern instance from a pattern syntax string
	 * @param pattern a pattern syntax string
	 * @throws D2RQException on malformed pattern
	 */
	public Pattern(String pattern) {
		this.pattern = pattern;
		parsePattern();
		this.columnsAsSet = new HashSet(this.columns);
	}

	public boolean couldFit(String value) {
		if (value == null) {
			return false;
		}
		if (this.columns.size() == 0) {
			return value.equals(this.firstLiteralPart);
		}
		if (!value.startsWith(this.firstLiteralPart)) {
			return false;
		}
		int index = 0;
		int offset = this.firstLiteralPart.length();
		while (index < this.columns.size() - 1) {
			String literalPart = (String) this.literalParts.get(index);
			offset = value.indexOf(literalPart, offset);
			if (offset == -1) {
				return false;
			}
			offset += literalPart.length();
			index++;
		}
		return value.endsWith((String) this.literalParts.get(index));
	}

	/* (non-Javadoc)
	 * @see de.fuberlin.wiwiss.d2rq.ValueSource#getColumns()
	 */
	public Set getColumns() {
		return this.columnsAsSet;
	}

	/**
	 * Extracts column values according to the pattern from a value string. The
	 * keys are {@link Column}s, the values are strings.
	 * @param value value to be checked.
	 * @return a map with <tt>Column</tt> keys and string values
	 * @see de.fuberlin.wiwiss.d2rq.ValueSource#getColumnValues(java.lang.String)
	 */
	public Map getColumnValues(String value) {
		Map result = new HashMap();
		if (value == null || this.columns.size() == 0 ||
				!value.startsWith(this.firstLiteralPart)) {
			return result;
		}
		int index = 0;
		int fieldStart = this.firstLiteralPart.length();
		int fieldEnd;
		while (index < this.columns.size() - 1) {
			String literalPart = (String) this.literalParts.get(index);
			fieldEnd = value.indexOf(literalPart, fieldStart);
			if (fieldEnd == -1) {
				return new HashMap();
			}
			result.put(this.columns.get(index), value.substring(fieldStart, fieldEnd));
			fieldStart = fieldEnd + literalPart.length();
			index++;
		}
		String lastLiteralPart = (String) this.literalParts.get(index);
		if (!value.endsWith(lastLiteralPart)) {
			return new HashMap();
		}
		result.put(this.columns.get(index),
				value.substring(fieldStart, value.length() - lastLiteralPart.length()));
		return result;
	}

	/**
	 * Constructs a String from the pattern using the given database row.
	 * @param row a database row
	 * @param columnNameNumberMap a map from qualified column names to indices
	 * 							into the row array
	 * @return the pattern's value for the given row
	 */
	public String getValue(String[] row, Map columnNameNumberMap) {
		int index = 0;
		StringBuffer result = new StringBuffer(this.firstLiteralPart);
		while (index < this.columns.size()) {
			Column column = (Column) this.columns.get(index);
			Integer fieldNumber = (Integer) columnNameNumberMap.get(column.getQualifiedName());
			if (fieldNumber == null) {
				Logger.instance().error("Illegal pattern: '" + this.pattern + "'");
			}
			if (row[fieldNumber.intValue()] == null) {
				return null;
			}
			result.append(row[fieldNumber.intValue()]);
			result.append(this.literalParts.get(index));
			index++;
		}
		return result.toString();
	}
	
	public String toString() {
		return "D2RQ pattern: \"" + this.pattern + "\"";
	}

	private void parsePattern() {
		int fieldStart = this.pattern.indexOf(D2RQ.deliminator);
		if (fieldStart == -1) {
			fieldStart = this.pattern.length();
		}
		// get text before first field
		this.firstLiteralPart = this.pattern.substring(0, fieldStart);
		int fieldEnd;
		while (fieldStart < this.pattern.length()) {
			// find end of field
			fieldStart += D2RQ.deliminator.length();
			fieldEnd = this.pattern.indexOf(D2RQ.deliminator, fieldStart + 1);
			if (fieldEnd == -1) {
				Logger.instance().error("Illegal pattern: '" + this.pattern + "'");
			}
			// get field
			String columnName = this.pattern.substring(fieldStart, fieldEnd).trim();
			this.columns.add(new Column(columnName));
			// find end of text
			fieldEnd += D2RQ.deliminator.length();
			fieldStart = this.pattern.indexOf(D2RQ.deliminator, fieldEnd);
			if (fieldStart == -1) {
				fieldStart = this.pattern.length();
			}
			// get text
			this.literalParts.add(this.pattern.substring(fieldEnd, fieldStart));
		}
	}
}