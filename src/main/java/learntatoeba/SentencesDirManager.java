package learntatoeba;

import java.io.*;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;

import learntatoeba.fileutil.DirCreator;
import learntatoeba.language.Language;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;

import static learntatoeba.Constants.INSTALL_DIR;
import static java.nio.charset.StandardCharsets.UTF_8;

public class SentencesDirManager {
	public static final File SENTENCES_DIR = new File(INSTALL_DIR, "sentences");
	private static final File LINKS_FILE = new File(SENTENCES_DIR, "links.csv");
	public static final String SUFFIX_OF_SENTENCE_FILES = "_sentences_detailed.tsv";
	
	public static boolean hasFileForLanguage(Language language) {
		File sentencesFile = new File(SENTENCES_DIR, language.getTatoebaCode() + SUFFIX_OF_SENTENCE_FILES);
		return sentencesFile.exists();
	}
	
	private static void downloadSentenceLinks() throws IOException {
		boolean dirNowExists = DirCreator.createDirIfNecessary(SENTENCES_DIR);
		if (!dirNowExists) {
			throw new IOException("Directory \"" + SENTENCES_DIR + "\" could not be found or created.");
		}
		File bZipFile = new File(SENTENCES_DIR, "links.tar.bz2");
		downloadFile("https://downloads.tatoeba.org/exports/links.tar.bz2", bZipFile);
		File targetTarFile = new File(SENTENCES_DIR, "links.tar");
		extractBZip2(bZipFile, targetTarFile);
		extractTarToLinksFile(targetTarFile);
	}
	
	public static void downloadFileForLanguage(Language language) throws IOException {
		if (!LINKS_FILE.exists()) {
			downloadSentenceLinks();
		}
		String languageCode = language.getTatoebaCode();
		File bZipFile = new File(SENTENCES_DIR, languageCode + SUFFIX_OF_SENTENCE_FILES + ".bz2");
		downloadFile("https://downloads.tatoeba.org/exports/per_language/" + languageCode + "/" + languageCode + SUFFIX_OF_SENTENCE_FILES + ".bz2", bZipFile);
		File targetExtractionFile = new File(SENTENCES_DIR, languageCode + SUFFIX_OF_SENTENCE_FILES);
		extractBZip2(bZipFile, targetExtractionFile);
		SentenceFileLinker.addLinkedIdsToSentences(targetExtractionFile);
	}
	
	private static void downloadFile(String urlStr, File file) throws IOException {
		URL url = new URL(urlStr);
		try (ReadableByteChannel urlChannel = Channels.newChannel(url.openStream());
		     FileOutputStream fileOutputStream = new FileOutputStream(file)) {
			fileOutputStream.getChannel().transferFrom(urlChannel, 0, Long.MAX_VALUE);
		}
	}
	
	private static void extractBZip2(File bZipFile, File targetExtractionFile) throws IOException {
		try (BZip2CompressorInputStream compressedInputStream = new BZip2CompressorInputStream(new BufferedInputStream(Files.newInputStream(bZipFile.toPath())));
		     OutputStream targetOutputStream = Files.newOutputStream(targetExtractionFile.toPath())) {
			final byte[] buffer = new byte[1024];
			int n;
			while (-1 != (n = compressedInputStream.read(buffer))) {
				targetOutputStream.write(buffer, 0, n);
			}
			Files.delete(bZipFile.toPath());
		}
	}
	
	private static void extractTarToLinksFile(File tarFile) throws IOException {
		try (TarArchiveInputStream tarInputStream = new TarArchiveInputStream(new BufferedInputStream(Files.newInputStream(tarFile.toPath())));
		     OutputStream targetOutputStream = Files.newOutputStream(LINKS_FILE.toPath())) {
			tarInputStream.getNextTarEntry();
			byte[] buffer = new byte[1024];
			int n;
			while (-1 != (n = tarInputStream.read(buffer))) {
				targetOutputStream.write(buffer, 0, n);
			}
			Files.delete(tarFile.toPath());
		}
	}
	
	private static class SentenceFileLinker {
		private static String linkLine;
		private static int linkId;
		
		private static void addLinkedIdsToSentences(File sentencesFile) throws IOException {
			File tempSentencesFile = new File(SENTENCES_DIR, "temp.tsv");
			try (BufferedReader sentencesReader = Files.newBufferedReader(sentencesFile.toPath(), UTF_8);
			     BufferedReader linksReader = Files.newBufferedReader(SentencesDirManager.LINKS_FILE.toPath(), UTF_8);
			     BufferedWriter tempWriter = Files.newBufferedWriter(tempSentencesFile.toPath(), UTF_8)) {
				String sentenceLine;
				linkLine = null;
				linkId = 0;
				while ((sentenceLine = sentencesReader.readLine()) != null) {
					int sentenceId = getIdAtStartOfLine(sentenceLine);
					while (linkId < sentenceId && (linkLine = linksReader.readLine()) != null) {
						linkId = getIdAtStartOfLine(linkLine);
					}
					String lineAppendage = getTabSeparatedLinkedIds(sentenceId, linksReader);
					int indexOfLastTab = sentenceLine.lastIndexOf('\t');
					int indexOfSecondToLastTab = sentenceLine.lastIndexOf('\t', indexOfLastTab - 1);
					tempWriter.write(sentenceLine.substring(0, indexOfSecondToLastTab) + lineAppendage + "\n");
				}
			}
			Files.delete(sentencesFile.toPath());
			Files.move(tempSentencesFile.toPath(), sentencesFile.toPath());
		}
		
		private static int getIdAtStartOfLine(String line) {
			int indexOfFirstTab = line.indexOf('\t');
			return Integer.parseInt(line.substring(0, indexOfFirstTab));
		}
		
		private static String getTabSeparatedLinkedIds(int sentenceId, BufferedReader linksReader) throws IOException {
			String linkedIds = "";
			while (linkId == sentenceId && linkLine != null) {
				int linkedId = getIdAtEndOfLine(linkLine);
				linkedIds += "\t" + linkedId;
				linkLine = linksReader.readLine();
				if (linkLine != null) {
					linkId = getIdAtStartOfLine(linkLine);
				}
			}
			return linkedIds;
		}
		
		private static int getIdAtEndOfLine(String line) {
			int indexOfLastTab = line.lastIndexOf('\t');
			return Integer.parseInt(line.substring(indexOfLastTab + 1));
		}
	}
}
