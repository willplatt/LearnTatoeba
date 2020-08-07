package learntatoeba;

import java.util.List;
import java.util.stream.Collectors;

public class UnicodeString {
	private List<String> characters;
	
	public UnicodeString(String str) {
		this.characters = stringToCharacters(str);
	}
	
	public String getCharacter(int characterNumber) {
		return characters.get(characterNumber);
	}
	
	public int length() {
		return characters.size();
	}
	
	public void prepend(String strToPrepend) {
		List<String> newCharacterList = stringToCharacters(strToPrepend);
		newCharacterList.addAll(characters);
		characters = newCharacterList;
	}
	
	public String finalCharacter() {
		return characters.get(characters.size() - 1);
	}
	
	public void removeFinalCharacter() {
		removeCharacter(length() - 1);
	}
	
	public void removeCharacter(int index) {
		characters.remove(index);
	}
	
	@Override
	public String toString() {
		String str = "";
		for (String character : characters) {
			str += character;
		}
		return str;
	}
	
	private List<String> stringToCharacters(String str) {
		return str.codePoints().mapToObj(Character::toString).collect(Collectors.toList());
	}
}
