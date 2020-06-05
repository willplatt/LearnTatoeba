package Language;

import Account.Account;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.Integer.parseInt;

public class SentenceChooser {
	private static final String SUFFIX_OF_SENTENCE_FILES = "_sentences.tsv";
	private static final int MAX_SCORE_UPPER_LIMIT = 100;
	private static final String WORD_SEPARATOR_REGEX = "(\\s+[^\\w]*\\s*)|(\\s*[^\\w]*\\s+)";
	
	private VocabManager vocabManager;
	private File sentencesFile;
	private List<String> nextSentences = new ArrayList<>();
	private List<List<String>> nextTranslations = new ArrayList<>();
	private RandomAccessFile nativeTranslationReader;
	private int sentenceScoreUpperLimit;
	
	public SentenceChooser(Account account, String practiceLanguage) throws IOException {
		this.vocabManager =  new VocabManager(account, practiceLanguage);
		this.sentencesFile =  new File(SentencesDirManager.SENTENCES_DIR, LanguageCodeHandler.getCodeForLanguage(practiceLanguage) + SUFFIX_OF_SENTENCE_FILES);
		File translationsFile = new File(SentencesDirManager.SENTENCES_DIR, LanguageCodeHandler.getCodeForLanguage(account.getNativeLanguage()) + SUFFIX_OF_SENTENCE_FILES);
		this.nativeTranslationReader = new RandomAccessFile(translationsFile, "r");
		this.sentenceScoreUpperLimit = 50;
	}
	
	public String getNextSentence() throws IOException {
		while (nextSentences.isEmpty() && sentenceScoreUpperLimit < MAX_SCORE_UPPER_LIMIT) {
			System.out.println("Computing more sentences...");
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
		String trimmedSentence = trimOuterPunctuation(sentence);
		int charsTrimmedFromStart = sentence.indexOf(trimmedSentence);
		String annotation = " ".repeat(charsTrimmedFromStart);
		String remainingToAnnotate = trimmedSentence;
		while (remainingToAnnotate.length() > 0) {
			String phrase = getLongestPhrasePrefix(remainingToAnnotate);
			String phraseAnnotation = getPhraseAnnotation(phrase);
			annotation += phraseAnnotation;
			remainingToAnnotate = remainingToAnnotate.substring(phrase.length());
			int wordSeparatorLength = getLengthOfFirstWordSeparatorOrZero(remainingToAnnotate);
			int annotationOverrun = phraseAnnotation.length() - phrase.length();
			int numberOfSpaces = Math.max(0, wordSeparatorLength - annotationOverrun);
			annotation += " ".repeat(numberOfSpaces);
			remainingToAnnotate = remainingToAnnotate.substring(wordSeparatorLength);
		}
		return annotation;
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
		try (BufferedReader sentencesReader = Files.newBufferedReader(sentencesFile.toPath())) {
			String line;
			while (nextSentences.size() < 30 && (line = sentencesReader.readLine()) != null) {
				int indexOfFirstTab = line.indexOf('\t');
				int indexOfSecondTab = line.indexOf('\t', indexOfFirstTab + 1);
				int indexOfThirdTab = line.indexOf('\t', indexOfSecondTab + 1);
				int indexOfThirdTabOrEndOfLine = indexOfThirdTab == -1 ? line.length() : indexOfThirdTab;
				String sentence = line.substring(indexOfSecondTab + 1, indexOfThirdTabOrEndOfLine);
				int score = getScoreForSentence(sentence);
				if (score > minScore && score < maxScore) {
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
			int wordSeparatorLength = getLengthOfFirstWordSeparatorOrZero(remainingToScore);
			remainingToScore = remainingToScore.substring(wordSeparatorLength);
		}
		return score;
	}
	
	private String trimOuterPunctuation(String sentence) {
		int start = 0;
		int end = sentence.length();
		while (start < end && !sentence.substring(start, start + 1).matches("\\w")) {
			start++;
		}
		while (start < end && !sentence.substring(end - 1, end).matches("\\w")) {
			end--;
		}
		return sentence.substring(start, end);
	}
	
	private String getLongestPhrasePrefix(String sentenceFragment) {
		int wordLength = indexOfWordSeparatorOrEnd(sentenceFragment);
		int phraseLength = sentenceFragment.length();
		String phrase = sentenceFragment.substring(0, phraseLength);
		while (phraseLength != wordLength && vocabManager.getStatusOfPhrase(phrase) == 0) {
			phraseLength = lastIndexOfWordSeparator(phrase);
			phrase = sentenceFragment.substring(0, phraseLength);
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
	
	private int indexOfWordSeparatorOrEnd(String str) {
		Pattern pattern = Pattern.compile(SentenceChooser.WORD_SEPARATOR_REGEX);
		Matcher matcher = pattern.matcher(str);
		return matcher.find() ? matcher.start() : str.length();
	}
	
	private int lastIndexOfWordSeparator(String str) {
		Pattern pattern = Pattern.compile(SentenceChooser.WORD_SEPARATOR_REGEX);
		Matcher matcher = pattern.matcher(str);
		int index = -1;
		while (matcher.find()) {
			index = matcher.start();
		}
		return index;
	}
	
	private int getLengthOfFirstWordSeparatorOrZero(String str) {
		Pattern pattern = Pattern.compile(SentenceChooser.WORD_SEPARATOR_REGEX);
		Matcher matcher = pattern.matcher(str);
		return matcher.find() ? matcher.end() - matcher.start() : 0;
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
