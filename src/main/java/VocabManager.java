import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

import static java.lang.Integer.parseInt;

public class VocabManager {
	private static final Set<String> VALID_STATUSES = Set.of("1", "2", "3", "4", "5", "98", "99");
	
	private Map<String, Integer> phraseToStatusMap = new HashMap<>();
	private Map<String, Integer> statusUpdates = new HashMap<>();
	private File vocabFile;
	private File tempVocabFile;
	
	public VocabManager(Account account, String practiceLanguage) throws IOException {
		vocabFile = new File(account.getVocabDirectory(), practiceLanguage + "_Words.csv");
		tempVocabFile = new File(account.getVocabDirectory(), practiceLanguage + "_Words.temp");
		readVocab();
	}
	
	public int getStatusOfPhrase(String phrase) {
		Integer updatedStatus = statusUpdates.get(phrase);
		if (updatedStatus != null) {
			return updatedStatus;
		}
		return phraseToStatusMap.getOrDefault(phrase, 0);
	}
	
	public boolean updateVocab(String updateCommand) {
		String[] terms = updateCommand.toLowerCase().split(" ");
		boolean hasCorrectFormat = terms.length % 2 == 0;
		for (int i = 1; i < terms.length; i += 2) {
			if (!VALID_STATUSES.contains(terms[i])) {
				hasCorrectFormat = false;
				break;
			}
		}
		if (!hasCorrectFormat) {
			return false;
		} else {
			for (int i = 0; i < terms.length; i += 2) {
				statusUpdates.put(terms[i], parseInt(terms[i+1]));
			}
			return true;
		}
	}
	
	public void pushUpdatesToFile() throws IOException {
		if (!vocabFile.exists()) {
			vocabFile.createNewFile();
		}
		try (BufferedReader vocabReader = Files.newBufferedReader(vocabFile.toPath());
		     BufferedWriter bufferedTempWriter = Files.newBufferedWriter(tempVocabFile.toPath())) {
			String line;
			while ((line = vocabReader.readLine()) != null) {
				int indexOfLastTab = line.lastIndexOf('\t');
				String phrase = line.substring(indexOfLastTab + 1);
				Integer updatedStatus = statusUpdates.get(phrase);
				if (updatedStatus != null) {
					int indexOfSecondToLastTab = line.lastIndexOf('\t', indexOfLastTab - 1);
					line = line.substring(0, indexOfSecondToLastTab + 1) + updatedStatus + line.substring(indexOfLastTab);
					statusUpdates.remove(phrase);
				}
				bufferedTempWriter.write(line + "\n");
			}
			for (Map.Entry<String, Integer> entry : statusUpdates.entrySet()) {
				String phrase = entry.getKey();
				int updatedStatus = entry.getValue();
				String lineForPhrase = phrase + "\t\t\t\t" + updatedStatus + "\t" + phrase;
				bufferedTempWriter.write(lineForPhrase + "\n");
			}
		}
		Files.delete(vocabFile.toPath());
		Files.move(tempVocabFile.toPath(), vocabFile.toPath());
	}
	
	private void readVocab() throws IOException {
		if (vocabFile.exists()) {
			try (BufferedReader vocabReader = Files.newBufferedReader(vocabFile.toPath())) {
				String line;
				while ((line = vocabReader.readLine()) != null) {
					int indexOfLastTab = line.lastIndexOf('\t');
					String phrase = line.substring(indexOfLastTab + 1);
					int indexOfSecondToLastTab = line.lastIndexOf('\t', indexOfLastTab - 1);
					String status = line.substring(indexOfSecondToLastTab + 1, indexOfLastTab);
					if (VALID_STATUSES.contains(status)) {
						phraseToStatusMap.put(phrase, parseInt(status));
					}
				}
			}
		}
	}
}
