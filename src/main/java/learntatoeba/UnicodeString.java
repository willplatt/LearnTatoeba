package learntatoeba;

import java.util.List;
import java.util.stream.Collectors;

public class UnicodeString {
	private final List<String> characters;
	
	public UnicodeString(String str) {
		characters = stringToCharacters(str);
	}
	
	public String getCharacter(int characterNumber) {
		return characters.get(characterNumber);
	}
	
	public int length() {
		return characters.size();
	}
	
	public UnicodeString prepend(String strToPrepend) {
		return new UnicodeString(strToPrepend + toString());
	}
	
	public String finalCharacter() {
		return characters.get(characters.size() - 1);
	}
	
	public UnicodeString getUnicodeSubstring(int beginIndex) {
		return getUnicodeSubstring(beginIndex, length());
	}
	
	public UnicodeString getUnicodeSubstring(int beginIndex, int endIndex) {
		return new UnicodeString(getSubstring(beginIndex, endIndex));
	}
	
	public String getSubstring(int beginIndex, int endIndex) {
		return charactersToString(characters.subList(beginIndex, endIndex));
	}
	
	public int indexOf(UnicodeString str) {
		for (int i = 0; i < length(); i++) {
			if (getCharacter(i).equals(str.getCharacter(0))) {
				List<String> candidateSubList = characters.subList(i, i + str.length());
				if (candidateSubList.equals(str.characters)) {
					return i;
				}
			}
		}
		return -1;
	}
	
	@Override
	public String toString() {
		return charactersToString(characters);
	}
	
	private List<String> stringToCharacters(String str) {
		return str.codePoints().mapToObj(Character::toString).collect(Collectors.toList());
	}
	
	private String charactersToString(List<String> characters) {
		String str = "";
		for (String character : characters) {
			str += character;
		}
		return str;
	}
}
