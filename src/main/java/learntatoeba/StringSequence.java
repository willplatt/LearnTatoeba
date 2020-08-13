package learntatoeba;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class StringSequence {
	private final Pattern GRAPHEME_PATTERN = Pattern.compile("\\X");
	
	public enum ItemType {CHARACTER, GRAPHEME}
	
	private final List<String> items;
	private final ItemType itemType;
	
	public StringSequence(String str, ItemType itemType) {
		this.itemType = itemType;
		switch (itemType) {
			case CHARACTER:
				this.items = stringToCharacters(str);
				break;
			default:
				this.items = stringToGraphemes(str);
		}
	}
	
	public String item(int itemNumber) {
		return items.get(itemNumber);
	}
	
	public int length() {
		return items.size();
	}
	
	public StringSequence prepend(String strToPrepend) {
		return new StringSequence(strToPrepend + toString(), itemType);
	}
	
	public String finalItem() {
		return items.get(items.size() - 1);
	}
	
	public StringSequence subsequence(int beginIndex) {
		return subsequence(beginIndex, length());
	}
	
	public StringSequence subsequence(int beginIndex, int endIndex) {
		return new StringSequence(getSubstring(beginIndex, endIndex), itemType);
	}
	
	public int indexOf(StringSequence sequence) {
		for (int i = 0; i < length(); i++) {
			if (item(i).equals(sequence.item(0))) {
				List<String> candidateSubList = items.subList(i, i + sequence.length());
				if (candidateSubList.equals(sequence.items)) {
					return i;
				}
			}
		}
		return -1;
	}
	
	@Override
	public String toString() {
		return listToString(items);
	}
	
	private List<String> stringToCharacters(String str) {
		return str.codePoints().mapToObj(Character::toString).collect(Collectors.toList());
	}
	
	private List<String> stringToGraphemes(String str) {
		List<String> graphemes = new ArrayList<>();
		Matcher matcher = GRAPHEME_PATTERN.matcher(str);
		while (matcher.find()) {
			graphemes.add(str.substring(matcher.start(), matcher.end()));
		}
		return graphemes;
	}
	
	private String getSubstring(int beginIndex, int endIndex) {
		return listToString(items.subList(beginIndex, endIndex));
	}
	
	private String listToString(List<String> characters) {
		String str = "";
		for (String character : characters) {
			str += character;
		}
		return str;
	}
}
