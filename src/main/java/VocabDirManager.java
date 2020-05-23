import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VocabDirManager {
	public static Map<Integer, List<String>> readVocab(Account account, String practiceLanguage) throws IOException {
		File vocabFile = new File(account.getVocabDirectory(), practiceLanguage + "_Words.csv");
		if (!vocabFile.exists()) {
			return new HashMap<>();
		}
		try (BufferedReader vocabReader = Files.newBufferedReader(vocabFile.toPath())) {
			String line;
			List<String> status1Words = new ArrayList<>();
			List<String> status2Words = new ArrayList<>();
			List<String> status3Words = new ArrayList<>();
			List<String> status4Words = new ArrayList<>();
			List<String> status5Words = new ArrayList<>();
			while ((line = vocabReader.readLine()) != null) {
				int indexOfFirstTab = line.indexOf('\t');
				String word = line.substring(0, indexOfFirstTab);
				int indexOfLastTab = line.lastIndexOf('\t');
				int indexOfSecondToLastTab = line.lastIndexOf('\t', indexOfLastTab - 1);
				String status = line.substring(indexOfSecondToLastTab + 1, indexOfLastTab);
				switch (status) {
					case "1":
						status1Words.add(word);
						break;
					case "2":
						status2Words.add(word);
						break;
					case "3":
						status3Words.add(word);
						break;
					case "4":
						status4Words.add(word);
						break;
					case "5":
						status5Words.add(word);
						break;
				}
			}
			Map<Integer, List<String>> statusToWordsMap = new HashMap<>();
			statusToWordsMap.put(1, status1Words);
			statusToWordsMap.put(2, status2Words);
			statusToWordsMap.put(3, status3Words);
			statusToWordsMap.put(4, status4Words);
			statusToWordsMap.put(5, status5Words);
			return statusToWordsMap;
		}
	}
}
