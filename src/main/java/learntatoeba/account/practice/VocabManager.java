package learntatoeba.account.practice;

import learntatoeba.language.Language;
import learntatoeba.account.Account;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

import static java.lang.Integer.parseInt;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class VocabManager {
	private static final Set<String> VALID_STATUSES = Set.of("1", "2", "3", "4", "5", "98", "99");
	
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
		Integer updatedStatus = statusUpdates.get(phrase);
		if (updatedStatus != null) {
			return updatedStatus;
		}
		return phraseToStatusMap.getOrDefault(phrase, 0);
	}
	
	public boolean updateVocab(String updateCommand) {
		if (updateCommand.length() == 0) {
			return true;
		}
		String[] subcommands = updateCommand.toLowerCase().split(", ");
		if (subcommandsAreValid(subcommands)) {
			for (String subcommand : subcommands) {
				String[] terms = subcommand.split(": ");
				statusUpdates.put(terms[0], parseInt(terms[1]));
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
				String phrase = line.substring(indexOfLastTab + 1);
				Integer updatedStatus = statusUpdates.get(phrase);
				if (updatedStatus != null) {
					int indexOfSecondToLastTab = line.lastIndexOf('\t', indexOfLastTab - 1);
					line = line.substring(0, indexOfSecondToLastTab + 1) + updatedStatus + line.substring(indexOfLastTab);
					statusUpdates.remove(phrase);
				}
				backupWriter.write(line + "\n");
			}
			for (Map.Entry<String, Integer> entry : statusUpdates.entrySet()) {
				String phrase = entry.getKey();
				int updatedStatus = entry.getValue();
				String lineForPhrase = phrase + "\t\t\t\t" + updatedStatus + "\t" + phrase;
				backupWriter.write(lineForPhrase + "\n");
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
					if (VALID_STATUSES.contains(status)) {
						phraseToStatusMap.put(phrase, parseInt(status));
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
}
