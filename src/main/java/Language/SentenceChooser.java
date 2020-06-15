package Language;

import Account.Account;
import Terminal.Terminal;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.Integer.parseInt;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.text.StringEscapeUtils.unescapeJava;

public class SentenceChooser {
	private static final String SUFFIX_OF_SENTENCE_FILES = "_sentences.tsv";
	private static final int MAX_SCORE_UPPER_LIMIT = 100;
	
	private int sentenceScoreUpperLimit = 50;
	private final VocabManager vocabManager;
	private final File sentencesFile;
	private final List<String> nextSentences = new ArrayList<>();
	private final List<List<String>> nextTranslations = new ArrayList<>();
	private final RandomAccessFile nativeTranslationReader;
	private final String wordCharRegex;
	private final boolean isRightToLeft;
	
	public SentenceChooser(Account account, Language practiceLanguage) throws IOException {
		this.vocabManager =  new VocabManager(account, practiceLanguage);
		this.sentencesFile =  new File(SentencesDirManager.SENTENCES_DIR, practiceLanguage.getTatoebaCode() + SUFFIX_OF_SENTENCE_FILES);
		File translationsFile = new File(SentencesDirManager.SENTENCES_DIR, account.getNativeLanguage().getTatoebaCode() + SUFFIX_OF_SENTENCE_FILES);
		this.nativeTranslationReader = new RandomAccessFile(translationsFile, "r");
		this.wordCharRegex = "[" + unescapeJava(practiceLanguage.getWordCharRegExp()) + "]";
		this.isRightToLeft = practiceLanguage.isRightToLeft();
	}
	
	public boolean vocabIsEmpty() {
		return vocabManager.isEmpty();
	}
	
	public String getNextSentence() throws IOException {
		while (nextSentences.isEmpty() && sentenceScoreUpperLimit < MAX_SCORE_UPPER_LIMIT) {
			Terminal.println("Computing more sentences...");
			computeNextSentencesWithScoresBetween(Math.max(1, sentenceScoreUpperLimit - 20), sentenceScoreUpperLimit);
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
		if (isRightToLeft) {
			return getRightToLeftSentenceAnnotation(sentence);
		} else {
			return getLeftToRightSentenceAnnotation(sentence);
		}
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
			nativeTranslationReader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void computeNextSentencesWithScoresBetween(int minScore, int maxScore) throws IOException {
		try (BufferedReader sentencesReader = Files.newBufferedReader(sentencesFile.toPath(), UTF_8)) {
			String line;
			while (nextSentences.size() < 15 && (line = sentencesReader.readLine()) != null) {
				int indexOfFirstTab = line.indexOf('\t');
				int indexOfSecondTab = line.indexOf('\t', indexOfFirstTab + 1);
				int indexOfThirdTab = line.indexOf('\t', indexOfSecondTab + 1);
				int indexOfThirdTabOrEndOfLine = indexOfThirdTab == -1 ? line.length() : indexOfThirdTab;
				String sentence = line.substring(indexOfSecondTab + 1, indexOfThirdTabOrEndOfLine);
				int score = getScoreForSentence(sentence);
				if (score >= minScore && score < maxScore) {
					List<String> translations = getNativeTranslations(line);
					if (!translations.isEmpty()) {
						nextSentences.add(sentence);
						nextTranslations.add(translations);
					}
				}
			}
		}
	}
	
	private int getScoreForSentence(String sentence) {
		String trimmedSentence = trimOuterPunctuation(sentence);
		int score = 0;
		String remainingToScore = trimmedSentence;
		while (remainingToScore.length() > 0) {
			String phrase = getLongestPhrasePrefix(remainingToScore);
			score += getScoreForPhrase(phrase);
			remainingToScore = remainingToScore.substring(phrase.length());
			int wordSeparatorLength = indexOfFirstWordCharOrZero(remainingToScore);
			remainingToScore = remainingToScore.substring(wordSeparatorLength);
		}
		return score;
	}
	
	private String getRightToLeftSentenceAnnotation(String sentence) {
		if (Terminal.isEmulatingBidi()) {
			String reversedLeftToRightAnnotation = new StringBuilder(getLeftToRightSentenceAnnotation(sentence)).reverse().toString();
			return reversedLeftToRightAnnotation.replace("89", "98");
		} else {
			return "\u200F" + getLeftToRightSentenceAnnotation(sentence) + "\u200F";
		}
	}
	
	private String getLeftToRightSentenceAnnotation(String sentence) {
		String trimmedSentence = trimOuterPunctuation(sentence);
		int charsTrimmedFromStart = sentence.indexOf(trimmedSentence);
		String annotation = " ".repeat(charsTrimmedFromStart);
		String remainingToAnnotate = trimmedSentence;
		while (remainingToAnnotate.length() > 0) {
			String phrase = getLongestPhrasePrefix(remainingToAnnotate);
			String phraseAnnotation = getPhraseAnnotation(phrase);
			annotation += phraseAnnotation;
			remainingToAnnotate = remainingToAnnotate.substring(phrase.length());
			int wordSeparatorLength = indexOfFirstWordCharOrZero(remainingToAnnotate);
			int annotationOverrun = phraseAnnotation.length() - phrase.length();
			int numberOfSpaces = Math.max(0, wordSeparatorLength - annotationOverrun);
			annotation += " ".repeat(numberOfSpaces);
			remainingToAnnotate = remainingToAnnotate.substring(wordSeparatorLength);
		}
		annotation += " ".repeat(Math.max(0, sentence.length() - annotation.length()));
		return annotation;
	}
	
	private String trimOuterPunctuation(String sentence) {
		int start = 0;
		int end = sentence.length();
		while (start < end && !sentence.substring(start, start + 1).matches(wordCharRegex)) {
			start++;
		}
		while (start < end && !sentence.substring(end - 1, end).matches(wordCharRegex)) {
			end--;
		}
		return sentence.substring(start, end);
	}
	
	private String getLongestPhrasePrefix(String sentenceFragment) {
		int wordLength = lengthOfFirstWordIn(sentenceFragment);
		String phrase = sentenceFragment;
		while (phrase.length() != wordLength && vocabManager.getStatusOfPhrase(phrase) == 0) {
			phrase = removeLastWordFrom(phrase);
		}
		return phrase;
	}
	
	private int getScoreForPhrase(String phrase) {
		int status = vocabManager.getStatusOfPhrase(phrase);
		if (status == 98 || status == 99) {
			status = 5;
		}
		int difference = Math.max(0, 4 - status);
		int differenceSquared = difference * difference;
		return 5 * differenceSquared;
	}
	
	private int lengthOfFirstWordIn(String phrase) {
		int index = 0;
		while (index < phrase.length() && phrase.substring(index, index + 1).matches(wordCharRegex)) {
			index++;
		}
		return index;
	}
	
	private String removeLastWordFrom(String phrase) {
		int index = phrase.length();
		while (index > 0 && phrase.substring(index - 1, index).matches(wordCharRegex)) {
			index--;
		}
		while (index > 0 && !phrase.substring(index - 1, index).matches(wordCharRegex)) {
			index--;
		}
		return phrase.substring(0, index);
	}
	
	private int indexOfFirstWordCharOrZero(String str) {
		Pattern pattern = Pattern.compile(wordCharRegex);
		Matcher matcher = pattern.matcher(str);
		return matcher.find() ? matcher.start() : 0;
	}
	
	private List<String> getNativeTranslations(String sentenceLine) throws IOException {
		return getNativeTranslationsForIds(getTranslationIds(sentenceLine));
	}
	
	private List<Integer> getTranslationIds(String sentenceLine) {
		String[] ids = sentenceLine.split("\t");
		List<Integer> translationIds = new ArrayList<>();
		for (int i = 3; i < ids.length; i++) {
			translationIds.add(parseInt(ids[i]));
		}
		return translationIds;
	}
	
	private List<String> getNativeTranslationsForIds(List<Integer> ids) throws IOException {
		Collections.sort(ids);
		List<String> translations = new ArrayList<>();
		long startIndex = 0;
		long fileLength = nativeTranslationReader.length();
		int i = 0;
		while (i < ids.size() && startIndex < fileLength - 1) {
			int desiredId = ids.get(i);
			startIndex = FileIdSearcher.getByteIndexOnLineWithId(desiredId, startIndex, fileLength, nativeTranslationReader);
			if (desiredId == FileIdSearcher.getFirstIdOfLineAt(startIndex, nativeTranslationReader)) {
				translations.add(FileIdSearcher.getTextAfterSecondTabOfLineAt(startIndex, nativeTranslationReader));
			}
			i++;
		}
		return translations;
	}
	
	private String getPhraseAnnotation(String phrase) {
		String status = String.valueOf(vocabManager.getStatusOfPhrase(phrase));
		if (status.equals("0")) {
			status = "-";
		}
		return status + "-".repeat(Math.max(0, phrase.length() - status.length()));
	}
}
