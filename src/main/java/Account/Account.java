package Account;

import Language.Language;

public class Account {
	private final String name;
	private final String directoryName;
	
	private Language nativeLanguage;
	private String vocabDirectory;
	private int autoblacklistDuration;
	
	public Account(String name, String directoryName, Language nativeLanguage, String vocabDirectory, int autoblacklistDuration) {
		this.name = name;
		this.directoryName = directoryName;
		this.nativeLanguage = nativeLanguage;
		this.vocabDirectory = vocabDirectory;
		this.autoblacklistDuration = autoblacklistDuration;
	}
	
	public String getName() {
		return name;
	}
	
	public String getDirectoryName() {
		return directoryName;
	}
	
	public Language getNativeLanguage() {
		return nativeLanguage;
	}
	
	public void setNativeLanguage(Language newNativeLanguage) {
		nativeLanguage = newNativeLanguage;
	}
	
	public String getVocabDirectory() {
		return vocabDirectory;
	}
	
	public void setVocabDirectory(String newVocabDirectory) {
		vocabDirectory = newVocabDirectory;
	}
	
	public int getAutoblacklistDuration() {
		return autoblacklistDuration;
	}
	
	public void setAutoblacklistDuration(int newAutoblacklistDuration) {
		autoblacklistDuration = newAutoblacklistDuration;
	}
}
