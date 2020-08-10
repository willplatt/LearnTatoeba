package learntatoeba.account.practice;

import learntatoeba.account.Account;
import learntatoeba.language.Language;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.text.Normalizer;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static java.lang.Integer.parseInt;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static java.text.Normalizer.Form.NFKC;

public class VocabManager {
	private static final Set<String> VALID_STATUSES = Set.of("0", "1", "2", "3", "4", "5", "8", "9");
	private static final Set<String> VALID_FILE_STATUSES = Set.of("1", "2", "3", "4", "5", "98", "99");
	
	private final Map<String, Integer> phraseToStatusMap = new HashMap<>();
	private final Map<String, Integer> statusUpdates = new HashMap<>();
	private final File vocabFile;
	private final File backupVocabFile;
	
	public VocabManager(Account account, Language practiceLanguage) throws IOException {
		vocabFile = new File(account.getVocabDirectory(), practiceLanguage.getName() + "_Words.csv");
		backupVocabFile = new File(account.getVocabDirectory(), practiceLanguage.getName() + "_Words.bak");
		readVocab();
	}
	
	public boolean isEmpty() {
		return phraseToStatusMap.isEmpty() && statusUpdates.isEmpty();
	}
	
	public int getStatusOfPhrase(String phrase) {
		String normalizedPhrase = normalizePhrase(phrase);
		Integer updatedStatus = statusUpdates.get(normalizedPhrase);
		if (updatedStatus != null) {
			return updatedStatus;
		}
		return phraseToStatusMap.getOrDefault(normalizedPhrase, 0);
	}
	
	public boolean updateVocab(String updateCommand) {
		if (updateCommand.length() == 0) {
			return true;
		}
		String[] subcommands = updateCommand.toLowerCase().split(", ");
		if (subcommandsAreValid(subcommands)) {
			for (String subcommand : subcommands) {
				String[] terms = subcommand.split(": ");
				statusUpdates.put(normalizePhrase(terms[0]), statusStringToInt(terms[1]));
			}
			return true;
		} else {
			return false;
		}
	}
	
	public void pushUpdatesToFile() throws IOException {
		if (!vocabFile.exists()) {
			vocabFile.createNewFile();
		}
		try (BufferedReader vocabReader = Files.newBufferedReader(vocabFile.toPath(), UTF_8);
		     BufferedWriter backupWriter = Files.newBufferedWriter(backupVocabFile.toPath(), UTF_8)) {
			String line;
			while ((line = vocabReader.readLine()) != null) {
				int indexOfLastTab = line.lastIndexOf('\t');
				String phrase = normalizePhrase(line.substring(indexOfLastTab + 1));
				Integer updatedStatus = statusUpdates.get(phrase);
				if (updatedStatus != null) {
					int indexOfSecondToLastTab = line.lastIndexOf('\t', indexOfLastTab - 1);
					line = line.substring(0, indexOfSecondToLastTab + 1) + statusToFileString(updatedStatus) + line.substring(indexOfLastTab);
					statusUpdates.remove(phrase);
				}
				if (updatedStatus == null || updatedStatus != 0) {
					backupWriter.write(line + "\n");
				}
			}
			for (Map.Entry<String, Integer> entry : statusUpdates.entrySet()) {
				String phrase = entry.getKey();
				int updatedStatus = entry.getValue();
				if (updatedStatus != 0) {
					String lineForPhrase = phrase + "\t\t\t\t" + statusToFileString(updatedStatus) + "\t" + phrase;
					backupWriter.write(lineForPhrase + "\n");
				}
			}
		}
		Files.copy(backupVocabFile.toPath(), vocabFile.toPath(), REPLACE_EXISTING);
	}
	
	private void readVocab() throws IOException {
		if (vocabFile.exists()) {
			try (BufferedReader vocabReader = Files.newBufferedReader(vocabFile.toPath(), UTF_8)) {
				String line;
				while ((line = vocabReader.readLine()) != null) {
					int indexOfLastTab = line.lastIndexOf('\t');
					String phrase = line.substring(indexOfLastTab + 1);
					int indexOfSecondToLastTab = line.lastIndexOf('\t', indexOfLastTab - 1);
					String status = line.substring(indexOfSecondToLastTab + 1, indexOfLastTab);
					if (VALID_FILE_STATUSES.contains(status)) {
						phraseToStatusMap.put(normalizePhrase(phrase), statusStringToInt(status));
					}
				}
			}
		}
	}
	
	private boolean subcommandsAreValid(String[] subcommands) {
		for (String subcommand : subcommands) {
			String[] terms = subcommand.split(": ");
			if (terms.length != 2 || !VALID_STATUSES.contains(terms[1])) {
				return false;
			}
		}
		return true;
	}
	
	private String normalizePhrase(String phrase) {
		return Normalizer.normalize(phrase, NFKC);
	}
	
	private String statusToFileString(int status) {
		switch (status) {
			case 8:
				return "98";
			case 9:
				return "99";
			default:
				return String.valueOf(status);
		}
	}
	
	private int statusStringToInt(String statusString) {
		switch (statusString) {
			case "98":
				return 8;
			case "99":
				return 9;
			default:
				return parseInt(statusString);
		}
	}
}
