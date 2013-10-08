package de.fuberlin.wiwiss.d2rq.sql;

public class RegexLikeCompiler {
	final private String regex;

	public RegexLikeCompiler(String regex) {
		this.regex = regex;
	}

	private class CompileState {
		public StringBuffer remaining;
		public StringBuilder output;
		public int pos;

		public boolean leadingWild = true;
		public boolean trailingWild = true;

		public CompileState(String regex) {
			remaining = new StringBuffer();
			remaining.append(regex);
			output = new StringBuilder();
			this.pos = 0;
		}

		public String toString() {
			return (leadingWild ? "%" : "") +
				output.toString() +
				(trailingWild ? "%" : "");
		}
	}

	public String compile() {
		CompileState st = new CompileState(this.regex);
		if (compile(st)) {
			return st.toString();
		} else {
			return null;
		}
	}

	private boolean isNonSpecial(char mod) {
		return ((mod >= 'a' && mod <= 'z') ||
			(mod >= 'A' && mod <= 'Z') ||
			(mod >= '0' && mod <= '9') ||
			(mod == ' '));
	}

	private boolean compile(CompileState st) {
		while (st.remaining.length() > 0) {
			char nxt = st.remaining.charAt(0);
			st.remaining.deleteCharAt(0);

			switch (nxt) {
				/* escape sequences */
				case '\\':
					/* an unterminated escape? let's bail out */
					if (st.remaining.length() == 0)
						return false;

					char after = st.remaining.charAt(0);
					st.remaining.deleteCharAt(0);

					switch (after) {
						case 'n':
							st.output.append("\n");
							break;
						case 'r':
							st.output.append("\r");
							break;
						case 't':
							st.output.append("\t");
							break;
						default:
							/* if it's a normal letter it might be meant to be a special alias
							   like \s for whitespace. to be safe, let's bail out */
							if (isNonSpecial(after)) {
								return false;
							} else {
								st.output.append(after);
							}
					}
					break;

				case '^':
					/* we only handle ^ at the start of the string */
					if (st.pos == 0) {
						st.leadingWild = false;
					} else {
						return false;
					}
					break;

				case '$':
					/* only handle $ at the end of the string */
					if (st.remaining.length() == 0) {
						st.trailingWild = false;
					} else {
						return false;
					}
					break;

				/* we can handle . or .* or .+, but anything else we'll have
				   to bail out on */
				case '.':
					if (st.remaining.length() == 0) {
						st.output.append("_");
					} else {
						char mod = st.remaining.charAt(0);
						/* don't eat the modifier unless it's a + or * */
						switch (mod) {
							case '*':
								st.output.append("%");
								st.remaining.deleteCharAt(0);
								break;
							case '+':
								st.output.append("_%");
								st.remaining.deleteCharAt(0);
								break;
							default:
								if (isNonSpecial(mod) || mod == '.') {
									st.output.append("_");
								} else {
									return false;
								}
						}
					}
					break;

				/* escape the metacharacters if they're in the source regex */
				case '%':
				case '_':
					st.output.append('\\');
					st.output.append(nxt);
					break;

				default:
					if (isNonSpecial(nxt)) {
						st.output.append(nxt);
					} else {
						return false;
					}
			}

			/* increment position and keep going! */
			st.pos++;
		}
		return true;
	}
}
