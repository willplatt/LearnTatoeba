import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;

import static java.lang.Integer.parseInt;
import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;

public class SentenceChooser {
	private static final File LINKS_FILE = new File(SentencesDirManager.SENTENCES_DIR, "links.csv");
	private static final String SUFFIX_OF_SENTENCE_FILES = "_sentences.tsv";
	
	private Map<Integer, List<String>> statusToWordsMap;
	private int statusIndex = 5;
	private int wordIndex = 0;
	private File sentencesFile;
	private List<String> nextSentences = new ArrayList<>();
	private List<List<String>> nextTranslations = new ArrayList<>();
	private RandomAccessFile linksReader;
	private RandomAccessFile nativeTranslationReader;
	
	public SentenceChooser(Account account, String practiceLanguage) throws IOException {
		this.statusToWordsMap = VocabDirManager.readVocab(account, practiceLanguage);
		this.sentencesFile =  new File(SentencesDirManager.SENTENCES_DIR, LanguageCodeHandler.getCodeForLanguage(practiceLanguage) + SUFFIX_OF_SENTENCE_FILES);
		this.linksReader = new RandomAccessFile(LINKS_FILE.toString(), "r");
		File translationsFile = new File(SentencesDirManager.SENTENCES_DIR, LanguageCodeHandler.getCodeForLanguage(account.getNativeLanguage()) + SUFFIX_OF_SENTENCE_FILES);
		this.nativeTranslationReader = new RandomAccessFile(translationsFile.toString(), "r");
	}
	
	public String getNextSentence() throws IOException {
		while (nextSentences.isEmpty() && statusIndex >= 1) {
			List<String> wordsList = statusToWordsMap.get(statusIndex);
			while (statusIndex <= 5 && wordsList.size() == wordIndex) {
				statusIndex--;
				wordsList = statusToWordsMap.get(statusIndex);
				wordIndex = 0;
			}
			if (statusIndex >= 1) {
				computeNextSentencesContaining(singleton(wordsList.get(wordIndex)));
				wordIndex++;
			}
		}
		if (statusIndex < 1) {
			computeNextSentencesContaining(emptySet());
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
	
	public void close() {
		try {
			linksReader.close();
			nativeTranslationReader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void computeNextSentencesContaining(Collection<String> phrases) throws IOException {
		try (BufferedReader sentencesReader = Files.newBufferedReader(sentencesFile.toPath())) {
			String line;
			while (nextTranslations.size() < 10 && (line = sentencesReader.readLine()) != null) {
				int indexOfFirstTab = line.indexOf('\t');
				int indexOfSecondTab = line.indexOf('\t', indexOfFirstTab + 1);
				String sentence = line.substring(indexOfSecondTab + 1);
				boolean containsPhrases = true;
				for (String phrase : phrases) {
					int offsetIntoSentence = 0;
					int lastIndexOfPhrase = sentence.lastIndexOf(phrase);
					boolean foundOccurrence = false;
					while (!foundOccurrence && offsetIntoSentence < lastIndexOfPhrase) {
						offsetIntoSentence = sentence.indexOf(phrase, offsetIntoSentence + 1);
						int endOffsetOfPhrase = offsetIntoSentence + phrase.length();
						if ((offsetIntoSentence == 0 || !Character.isLetter(sentence.charAt(offsetIntoSentence - 1))) &&
								(endOffsetOfPhrase == sentence.length() || !Character.isLetter(sentence.charAt(endOffsetOfPhrase)))) {
							foundOccurrence = true;
						}
					}
					if (!foundOccurrence) {
						containsPhrases = false;
						break;
					}
				}
				if (containsPhrases) {
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
			String line = readUtf8Line(reader);
			lineLength = line.getBytes().length + 1;
		}
		reader.seek(receedingIndex + lineLength);
		return readUtf8Line(reader);
	}
	
	private String readUtf8Line(RandomAccessFile reader) throws IOException {
		String rawEncodedString = reader.readLine();
		return new String(rawEncodedString.getBytes(ISO_8859_1), UTF_8);
	}
}
