import java.io.File;

public class Account {
	private final String name;
	private final String nativeLanguage;
	private final String directoryName;
	
	private String vocabDirectory;
	
	public Account(String name, String directoryName, String vocabDirectory) {
		this.name = name;
		this.nativeLanguage = "English";
		this.directoryName = directoryName;
		this.vocabDirectory = vocabDirectory;
	}
	
	public String getName() {
		return name;
	}
	
	public String getNativeLanguage() {
		return nativeLanguage;
	}
	
	public String getDirectoryName() {
		return directoryName;
	}
	
	public String getVocabDirectory() {
		return vocabDirectory;
	}
	
	public void setVocabDirectory(String newVocabDirectory) {
		vocabDirectory = newVocabDirectory;
	}
}
