import java.io.File;

public class Account {
	private String name;
	private String directoryName;
	private String vocabDirectory;
	
	public Account(String name, String directoryName, String vocabDirectory) {
		this.name = name;
		this.directoryName = directoryName;
		this.vocabDirectory = vocabDirectory;
	}
	
	public String getName() {
		return name;
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
