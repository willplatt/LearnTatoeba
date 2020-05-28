import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.util.*;

import static java.lang.Integer.parseInt;
import static java.nio.charset.StandardCharsets.ISO_8859_1;

public class SentenceChooser {
	private static final File LINKS_FILE = new File(SentencesDirManager.SENTENCES_DIR, "links.csv");
	private static final String SUFFIX_OF_SENTENCE_FILES = "_sentences.tsv";
	private static final int MAX_SCORE_UPPER_LIMIT = 100;
	private static final String WORD_SEPARATOR_REGEX = "(\\s+[^\\w]*\\s*)|(\\s*[^\\w]*\\s+)";
	
	private VocabManager vocabManager;
	private File sentencesFile;
	private List<String> nextSentences = new ArrayList<>();
	private List<List<String>> nextTranslations = new ArrayList<>();
	private RandomAccessFile linksReader;
	private RandomAccessFile nativeTranslationReader;
	private int sentenceScoreUpperLimit;
	
	public SentenceChooser(Account account, String practiceLanguage) throws IOException {
		this.vocabManager =  new VocabManager(account, practiceLanguage);
		this.sentencesFile =  new File(SentencesDirManager.SENTENCES_DIR, LanguageCodeHandler.getCodeForLanguage(practiceLanguage) + SUFFIX_OF_SENTENCE_FILES);
		this.linksReader = new RandomAccessFile(LINKS_FILE, "r");
		File translationsFile = new File(SentencesDirManager.SENTENCES_DIR, LanguageCodeHandler.getCodeForLanguage(account.getNativeLanguage()) + SUFFIX_OF_SENTENCE_FILES);
		this.nativeTranslationReader = new RandomAccessFile(translationsFile, "r");
		this.sentenceScoreUpperLimit = 50;
	}
	
	public String getNextSentence() throws IOException {
		while (nextSentences.isEmpty() && sentenceScoreUpperLimit < MAX_SCORE_UPPER_LIMIT) {
			computeNextSentencesWithScoresBetween(0, sentenceScoreUpperLimit);
			sentenceScoreUpperLimit += 20;
		}
		if (nextSentences.isEmpty()) {
			return null;
		} else {
			int indexOfFinalSentence = nextSentences.size() - 1;
			String sentence = nextSentences.get(indexOfFinalSentence);
			nextSentences.remove(indexOfFinalSentence);
			return sentence;
		}
	}
	
	public List<String> getNextTranslations() {
		int indexOfFinalTranslation = nextTranslations.size() - 1;
		List<String> translations = nextTranslations.get(indexOfFinalTranslation);
		nextTranslations.remove(indexOfFinalTranslation);
		return translations;
	}
	
	public String getSentenceAnnotation(String sentence) {
		StringBuilder stringBuilder = new StringBuilder();
		String[] partition = trimOuterPunctuation(sentence).split(WORD_SEPARATOR_REGEX, 2);
		while (partition.length == 2) {
			stringBuilder.append(getWordAnnotation(partition[0]));
			int numberOfSpaces = sentence.indexOf(partition[1], stringBuilder.length()) - stringBuilder.length();
			stringBuilder.append(" ".repeat(numberOfSpaces));
			partition = partition[1].split(WORD_SEPARATOR_REGEX, 2);
		}
		if (partition.length == 1) {
			stringBuilder.append(getWordAnnotation(partition[0]));
		}
		return stringBuilder.toString();
	}
	
	private String getWordAnnotation(String word) {
		String status = String.valueOf(vocabManager.getStatusOfWord(word));
		if (status.equals("0")) {
			status = "-";
		}
		return status + "-".repeat(Math.max(0, word.length() - status.length()));
	}
	
	public boolean updateVocab(String updateCommand) {
		return vocabManager.updateVocab(updateCommand);
	}
	
	public void close() {
		try {
			vocabManager.pushUpdatesToFile();
		} catch (IOException e){
			e.printStackTrace();
		}
		try {
			linksReader.close();
			nativeTranslationReader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void computeNextSentencesWithScoresBetween(int minScore, int maxScore) throws IOException {
		try (BufferedReader sentencesReader = Files.newBufferedReader(sentencesFile.toPath())) {
			String line;
			while (nextSentences.size() < 30 && (line = sentencesReader.readLine()) != null) {
				int indexOfFirstTab = line.indexOf('\t');
				int indexOfSecondTab = line.indexOf('\t', indexOfFirstTab + 1);
				String sentence = line.substring(indexOfSecondTab + 1);
				int score = 0;
				String[] wordsInSentence = trimOuterPunctuation(sentence).split(WORD_SEPARATOR_REGEX);
				for (String word : wordsInSentence) {
					score += getScoreForWord(word);
				}
				if (score > minScore && score < maxScore) {
					String sentenceId = line.substring(0, indexOfFirstTab);
					List<String> translations = getNativeTranslations(sentenceId);
					if (!translations.isEmpty()) {
						nextSentences.add(sentence);
						nextTranslations.add(translations);
					}
				}
			}
		}
	}
	
	private int getScoreForWord(String word) {
		int status = vocabManager.getStatusOfWord(word);
		if (status == 98 || status == 99) {
			status = 5;
		}
		int difference = Math.max(0, 4 - status);
		int differenceSquared = difference * difference;
		return 5 * differenceSquared;
	}
	
	private String trimOuterPunctuation(String sentence) {
		int start = 0;
		int end = sentence.length();
		while (!sentence.substring(start, start + 1).matches("\\w")) {
			start++;
		}
		while (!sentence.substring(end - 1, end).matches("\\w")) {
			end--;
		}
		return sentence.substring(start, end);
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
		while (receedingIndex + lineLength > index && receedingIndex >= 3) {
			receedingIndex -= 3;
			reader.seek(receedingIndex);
			String line = reader.readLine();
			lineLength = line.getBytes(ISO_8859_1).length + 1;
		}
		reader.seek(receedingIndex + lineLength);
		return RandomAccessReaderHelper.readUtf8Line(reader);
	}
}
