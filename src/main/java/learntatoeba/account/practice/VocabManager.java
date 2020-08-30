package learntatoeba.account.practice;

import learntatoeba.account.Account;
import learntatoeba.language.Language;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.text.Normalizer;
import java.util.*;

import static java.lang.Integer.parseInt;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static java.text.Normalizer.Form.NFKC;

public class VocabManager {
	private static final Set<String> VALID_FILE_STATUSES = Set.of("1", "2", "3", "4", "5", "98", "99");
	
	private final Map<String, Integer> phraseToStatusMap = new HashMap<>();
	private final Map<String, VocabUpdate> vocabUpdates = new HashMap<>();
	private final File vocabFile;
	private final File backupVocabFile;
	
	public VocabManager(Account account, Language practiceLanguage) throws IOException {
		vocabFile = new File(account.getVocabDirectory(), practiceLanguage.getName() + "_Words.csv");
		backupVocabFile = new File(account.getVocabDirectory(), practiceLanguage.getName() + "_Words.bak");
		readVocab();
	}
	
	public boolean isEmpty() {
		return phraseToStatusMap.isEmpty() && vocabUpdates.isEmpty();
	}
	
	public int getStatusOfPhrase(String phrase) {
		String normalizedPhrase = normalizePhrase(phrase);
		VocabUpdate vocabUpdate = vocabUpdates.get(normalizedPhrase);
		if (vocabUpdate != null) {
			return vocabUpdate.getStatus();
		}
		return phraseToStatusMap.getOrDefault(normalizedPhrase, 0);
	}
	
	public boolean updateVocab(String updateCommand) {
		try {
			List<VocabUpdate> updates = parseCommandIntoVocabUpdates(updateCommand);
			for (VocabUpdate update : updates) {
				String normalizedPhrase = normalizePhrase(update.getPhrase());
				if (vocabUpdates.containsKey(normalizedPhrase)) {
					update = vocabUpdates.get(normalizedPhrase).mergeWithNewerUpdate(update);
				}
				vocabUpdates.put(normalizedPhrase, update);
			}
			return true;
		} catch (IllegalArgumentException | IndexOutOfBoundsException e) {
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
				String[] values = line.split("\t", -1);
				String normalizedPhrase = normalizePhrase(values[5]);
				VocabUpdate update = vocabUpdates.get(normalizedPhrase);
				if (update != null) {
					values[0] = update.getPhrase();
					if (update.getTranslation() != null) {
						values[1] = update.getTranslation();
					}
					if (update.getRomanization() != null) {
						values[3] = update.getRomanization();
					}
					values[4] = statusToFileString(update.getStatus());
					values[5] = normalizedPhrase;
					line = String.join("\t", values);
					vocabUpdates.remove(normalizedPhrase);
				}
				if (update == null || update.getStatus() != 0) {
					backupWriter.write(line + "\n");
				}
			}
			for (Map.Entry<String, VocabUpdate> entry : vocabUpdates.entrySet()) {
				String normalizedPhrase = entry.getKey();
				VocabUpdate update = entry.getValue();
				int status = update.getStatus();
				String translation = update.getTranslation() == null ? "" : update.getTranslation();
				String romanization = update.getRomanization() == null ? "" : update.getRomanization();
				if (status != 0) {
					String lineForPhrase = update.getPhrase() + "\t" + translation + "\t\t" + romanization + "\t" + statusToFileString(status) + "\t" + normalizedPhrase;
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
	
	private List<VocabUpdate> parseCommandIntoVocabUpdates(String command) {
		List<VocabUpdate> updates = new ArrayList<>();
		while (command.length() > 0) {
			String translation = null;
			String romanization = null;
			String[] split = command.split(": ", 2);
			String phrase = split[0];
			command = split[1];
			String status = command.substring(0, 1);
			command = command.substring(1);
			if (command.startsWith("{")) {
				translation = command.substring(1, command.indexOf('}'));
				command = command.substring(translation.length() + 2);
			}
			if (command.startsWith("[")) {
				romanization = command.substring(1, command.indexOf(']'));
				command = command.substring(romanization.length() + 2);
			}
			updates.add(new VocabUpdate(phrase, status, translation, romanization));
			if (command.startsWith(", ")) {
				command = command.substring(2);
			} else if (command.length() != 0) {
				throw new IllegalArgumentException("Invalid update command. Expected \", \" or end of command, but instead found \"" + command + "\".");
			}
		}
		return updates;
	}
	
	private String normalizePhrase(String phrase) {
		return Normalizer.normalize(phrase.toLowerCase(), NFKC);
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
