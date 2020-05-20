import java.io.*;
import java.nio.file.Files;
import java.util.*;

import static java.lang.Integer.parseInt;

public class SentenceChooser {
	private static final File LINKS_FILE = new File(SentencesDirManager.SENTENCES_DIR, "links.csv");
	private static final String SUFFIX_OF_SENTENCE_FILES = "_sentences.tsv";
	
	private List<String> nextSentences;
	private List<List<String>> nextTranslations;
	private BufferedReader sentencesReader;
	private RandomAccessFile linksReader;
	private RandomAccessFile nativeTranslationReader;
	
	public SentenceChooser(Account account, String language) throws IOException {
		File sentencesFile =  new File(SentencesDirManager.SENTENCES_DIR, LanguageCodeHandler.getCodeForLanguage(language) + SUFFIX_OF_SENTENCE_FILES);
		sentencesReader = Files.newBufferedReader(sentencesFile.toPath());
		linksReader = new RandomAccessFile(LINKS_FILE.toString(), "r");
		File translationsFile = new File(SentencesDirManager.SENTENCES_DIR, LanguageCodeHandler.getCodeForLanguage(account.getNativeLanguage()) + SUFFIX_OF_SENTENCE_FILES);
		nativeTranslationReader = new RandomAccessFile(translationsFile.toString(), "r");
	}
	
	public String getNextSentence() throws IOException {
		if (nextSentences == null || nextSentences.isEmpty()) {
			computeNextSentences();
		}
		int indexOfFinalSentence = nextSentences.size() - 1;
		String sentence = nextSentences.get(indexOfFinalSentence);
		nextSentences.remove(indexOfFinalSentence);
		return sentence;
	}
	
	public List<String> getNextTranslations() {
		int indexOfFinalTranslation = nextTranslations.size() - 1;
		List<String> translations = nextTranslations.get(indexOfFinalTranslation);
		nextTranslations.remove(indexOfFinalTranslation);
		return translations;
	}
	
	public void close() {
		try {
			sentencesReader.close();
			linksReader.close();
			nativeTranslationReader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void computeNextSentences() throws IOException {
		nextSentences = new ArrayList<>();
		nextTranslations = new ArrayList<>();
		String line;
		while (nextTranslations.size() == 0 && (line = sentencesReader.readLine()) != null) {
			int indexOfFirstTab = line.indexOf('\t');
			String sentenceId = line.substring(0, indexOfFirstTab);
			List<String> translations = getNativeTranslations(sentenceId);
			if (!translations.isEmpty()) {
				int indexOfSecondTab = line.indexOf('\t', indexOfFirstTab + 1);
				nextSentences.add(line.substring(indexOfSecondTab + 1));
				nextTranslations.add(translations);
			}
		}
	}
	
	private List<String> getNativeTranslations(String sentenceId) throws IOException {
		return getNativeTranslationsForIds(getTranslationIds(sentenceId));
	}
	
	private List<Integer> getTranslationIds(String sentenceId) throws IOException {
		int desiredId = parseInt(sentenceId);
		long middleIndex = binarySearchBetween(desiredId, 0, linksReader.length(), linksReader);
		Set<Integer> translationIds = new HashSet<>();
		long index = middleIndex;
		while (desiredId == getFirstIdOfLineAt(index, linksReader) && index < linksReader.length()) {
			translationIds.add(getSecondIdOfLineAt(index, linksReader));
			index += 3;
		}
		index = middleIndex - 3;
		while (desiredId == getFirstIdOfLineAt(index, linksReader) && index >= 0) {
			translationIds.add(getSecondIdOfLineAt(index, linksReader));
			index -= 3;
		}
		return new ArrayList<>(translationIds);
	}
	
	private List<String> getNativeTranslationsForIds(List<Integer> ids) throws IOException {
		Collections.sort(ids);
		List<String> translations = new ArrayList<>();
		long startIndex = 0;
		long fileLength = nativeTranslationReader.length();
		int i = 0;
		while (i < ids.size() && startIndex < fileLength - 1) {
			int desiredId = ids.get(i);
			startIndex = binarySearchBetween(desiredId, startIndex, fileLength, nativeTranslationReader);
			if (desiredId == getFirstIdOfLineAt(startIndex, nativeTranslationReader)) {
				translations.add(getTextAfterSecondTabOfLineAt(startIndex, nativeTranslationReader));
			}
			i++;
		}
		return translations;
	}
	
	private long binarySearchBetween(int desiredId, long startIndex, long endIndex, RandomAccessFile reader) throws IOException {
		if (startIndex >= endIndex) {
			return endIndex;
		}
		long middleIndex = (startIndex + endIndex) / 2;
		int id = getFirstIdOfLineAt(middleIndex, reader);
		if (id == desiredId) {
			return middleIndex;
		} else if (id > desiredId) {
			return binarySearchBetween(desiredId, startIndex, middleIndex - 1, reader);
		} else {
			return binarySearchBetween(desiredId, middleIndex + 1, endIndex, reader);
		}
	}
	
	private int getFirstIdOfLineAt(long index, RandomAccessFile reader) throws IOException {
		String line = readLineAt(index, reader);
		int indexOfTab = line.indexOf('\t');
		return parseInt(line.substring(0, indexOfTab));
	}
	
	private int getSecondIdOfLineAt(long index, RandomAccessFile reader) throws IOException {
		String line = readLineAt(index, reader);
		int indexOfTab = line.indexOf('\t');
		return parseInt(line.substring(indexOfTab + 1));
	}
	
	private String getTextAfterSecondTabOfLineAt(long index, RandomAccessFile reader) throws IOException {
		String line = readLineAt(index, reader);
		int indexOfFirstTab = line.indexOf('\t');
		int indexOfSecondTab = line.indexOf('\t', indexOfFirstTab + 1);
		return line.substring(indexOfSecondTab + 1);
	}
	
	private String readLineAt(long index, RandomAccessFile reader) throws IOException {
		long receedingIndex = index;
		int lineLength = 1;
		while (receedingIndex + lineLength > index && receedingIndex >= 0) {
			receedingIndex -= 3;
			reader.seek(receedingIndex);
			String line = reader.readLine();
			lineLength = line.getBytes().length + 1;
		}
		reader.seek(receedingIndex + lineLength);
		return reader.readLine();
	}
}
