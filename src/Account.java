public class Account {
	private final String name;
	private final String directoryName;
	
	private String nativeLanguage;
	private String vocabDirectory;
	
	public Account(String name, String directoryName, String nativeLanguage, String vocabDirectory) {
		this.name = name;
		this.directoryName = directoryName;
		this.nativeLanguage = nativeLanguage;
		this.vocabDirectory = vocabDirectory;
	}
	
	public String getName() {
		return name;
	}
	
	public String getDirectoryName() {
		return directoryName;
	}
	
	public String getNativeLanguage() {
		return nativeLanguage;
	}
	
	public void setNativeLanguage(String newNativeLanguage) {
		nativeLanguage = newNativeLanguage;
	}
	
	public String getVocabDirectory() {
		return vocabDirectory;
	}
	
	public void setVocabDirectory(String newVocabDirectory) {
		vocabDirectory = newVocabDirectory;
	}
}
