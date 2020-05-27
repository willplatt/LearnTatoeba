import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.util.*;

import static java.lang.Integer.parseInt;
import static java.nio.charset.StandardCharsets.UTF_8;

public class VocabManager {
	private static final Set<String> VALID_STATUSES = Set.of("1", "2", "3", "4", "5");
	
	private Map<String, Integer> wordToStatusMap = new HashMap<>();
	private File vocabFile;
	
	public VocabManager(Account account, String practiceLanguage) throws IOException {
		vocabFile = new File(account.getVocabDirectory(), practiceLanguage + "_Words.csv");
		readVocab();
	}
	
	public int getStatusOfWord(String word) {
		return wordToStatusMap.getOrDefault(word, 0);
	}
	
	public boolean updateVocab(String updateCommand) throws IOException {
		String[] words = updateCommand.toLowerCase().split(" ");
		List<String> wordsToUpdate = new ArrayList<>();
		boolean hasCorrectFormat = words.length % 2 == 0;
		for (int i = 1; i < words.length; i += 2) {
			wordsToUpdate.add(words[i-1]);
			String status = words[i];
			if (!VALID_STATUSES.contains(status)) {
				hasCorrectFormat = false;
			}
		}
		if (!hasCorrectFormat) {
			return false;
		} else {
			if (!vocabFile.exists()) {
				vocabFile.createNewFile();
			}
			try (RandomAccessFile vocabReadWriter = new RandomAccessFile(vocabFile, "rw")) {
				Set<String> wordsLeftToUpdate = new HashSet<>(wordsToUpdate);
				String line;
				while (!wordsLeftToUpdate.isEmpty() && (line = RandomAccessReaderHelper.readUtf8Line(vocabReadWriter)) != null) {
					int indexOfLastTab = line.lastIndexOf('\t');
					String word = line.substring(indexOfLastTab + 1);
					int indexOfWordInCommand = wordsToUpdate.indexOf(word);
					if (indexOfWordInCommand != -1) {
						int indexOfSecondToLastTab = line.lastIndexOf('\t', indexOfLastTab - 1);
						long endOfLine = vocabReadWriter.getFilePointer();
						int byteOffsetBetweenEndOfLineAndSecondToLastTab = line.substring(indexOfSecondToLastTab).getBytes(UTF_8).length;
						if (endOfLine == vocabReadWriter.length()) {
							vocabReadWriter.seek(endOfLine - byteOffsetBetweenEndOfLineAndSecondToLastTab);
						} else {
							vocabReadWriter.seek((endOfLine - 1) - byteOffsetBetweenEndOfLineAndSecondToLastTab);
						}
						String newStatusOfWord = words[indexOfWordInCommand * 2 + 1];
						vocabReadWriter.writeBytes(newStatusOfWord);
						vocabReadWriter.seek(endOfLine);
						wordToStatusMap.put(word, parseInt(newStatusOfWord));
						wordsLeftToUpdate.remove(word);
					}
				}
				if (!wordsLeftToUpdate.isEmpty()) {
					for (String word : wordsLeftToUpdate) {
						int indexOfWordInCommand = wordsToUpdate.indexOf(word);
						String newStatusOfWord = words[indexOfWordInCommand * 2 + 1];
						String lineForWord = word + "\t\t\t\t" + newStatusOfWord + "\t" + word + "\n";
						for (byte b : lineForWord.getBytes(UTF_8)) {
							vocabReadWriter.writeByte(b);
						}
						wordToStatusMap.put(word, parseInt(newStatusOfWord));
					}
				}
				return true;
			}
		}
	}
	
	private void readVocab() throws IOException {
		if (vocabFile.exists()) {
			try (BufferedReader vocabReader = Files.newBufferedReader(vocabFile.toPath())) {
				String line;
				while ((line = vocabReader.readLine()) != null) {
					int indexOfLastTab = line.lastIndexOf('\t');
					String word = line.substring(indexOfLastTab + 1);
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
