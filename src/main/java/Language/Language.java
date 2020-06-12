package Language;

public class Language {
	private final String DEFAULT_WORD_CHAR_REG_EXP = "\\\\-'a-zA-ZÀ-ÖØ-ö\\u00F8-\\u01BF\\u01C4-\\u024F\\u0370-\\u052F\\u1E00-\\u1FFF";
	
	private final String name;
	private final String tatoebaCode;
	private final String twoCharCode;
	private final String wordCharRegExp;
	private final boolean isRightToLeft;
	
	public Language(String line) {
		String[] values = line.split("\t", -1);
		this.name = values[0];
		this.tatoebaCode = values[1];
		this.twoCharCode = values[2];
		this.wordCharRegExp = values[3].equals("") ? DEFAULT_WORD_CHAR_REG_EXP : values[3];
		this.isRightToLeft = values[4].equals("rtl");
	}
	
	public String getName() {
		return name;
	}
	
	public String getTatoebaCode() {
		return tatoebaCode;
	}
	
	public String getTwoCharCode() {
		return twoCharCode;
	}
	
	public String getWordCharRegExp() {
		return wordCharRegExp;
	}
	
	public boolean isRightToLeft() {
		return isRightToLeft;
	}
}
