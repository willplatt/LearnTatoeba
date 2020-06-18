package Language;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Integer.parseInt;

public class Sentence {
	private final int id;
	private final String text;
	private final String author;
	
	private final List<Sentence> translations = new ArrayList<Sentence>();
	
	public Sentence(String tsvLine) {
		String[] values = tsvLine.split("\t");
		this.id = parseInt(values[0]);
		this.text = values[2];
		this.author = values[3];
	}
	
	public int getId() {
		return id;
	}
	
	public String getText() {
		return text;
	}
	
	public String getAuthor() {
		return author;
	}
	
	public void addTranslations(List<Sentence> translationsToAdd) {
		translations.addAll(translationsToAdd);
	}
	
	public List<Sentence> getTranslations() {
		return new ArrayList<>(translations);
	}
}
