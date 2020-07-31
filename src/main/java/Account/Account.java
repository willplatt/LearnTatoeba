package Account;

import Language.Language;
import Language.BlacklistDuration;

public class Account {
	private final String name;
	private final String directoryName;
	
	private Language nativeLanguage;
	private String vocabDirectory;
	private BlacklistDuration autoblacklistDuration;
	
	public Account(String name, String directoryName, Language nativeLanguage, String vocabDirectory, BlacklistDuration autoblacklistDuration) {
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
	
	public BlacklistDuration getAutoblacklistDuration() {
		return autoblacklistDuration;
	}
	
	public void setAutoblacklistDuration(BlacklistDuration newAutoblacklistDuration) {
		autoblacklistDuration = newAutoblacklistDuration;
	}
}
