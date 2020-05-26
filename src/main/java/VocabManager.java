import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

public class VocabManager {
	private Map<String, Integer> wordToStatusMap = new HashMap<>();
	
	public VocabManager(Account account, String practiceLanguage) throws IOException {
		readVocab(account, practiceLanguage);
	}
	
	public int getStatusOfWord(String word) {
		return wordToStatusMap.getOrDefault(word, 0);
	}
	
	private void readVocab(Account account, String practiceLanguage) throws IOException {
		File vocabFile = new File(account.getVocabDirectory(), practiceLanguage + "_Words.csv");
		if (vocabFile.exists()) {
			try (BufferedReader vocabReader = Files.newBufferedReader(vocabFile.toPath())) {
				String line;
				while ((line = vocabReader.readLine()) != null) {
					int indexOfFirstTab = line.indexOf('\t');
					String word = line.substring(0, indexOfFirstTab);
					int indexOfLastTab = line.lastIndexOf('\t');
					int indexOfSecondToLastTab = line.lastIndexOf('\t', indexOfLastTab - 1);
					String status = line.substring(indexOfSecondToLastTab + 1, indexOfLastTab);
					switch (status) {
						case "1":
							wordToStatusMap.put(word, 1);
							break;
						case "2":
							wordToStatusMap.put(word, 2);
							break;
						case "3":
							wordToStatusMap.put(word, 3);
							break;
						case "4":
							wordToStatusMap.put(word, 4);
							break;
						case "5":
							wordToStatusMap.put(word, 5);
							break;
					}
				}
			}
		}
	}
}
