import java.io.*;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;

public class SentencesDirManager {
	public static final File SENTENCES_DIR = new File("sentences");
	
	public static boolean hasFileForLanguage(String language) throws IOException {
		File sentencesFile = new File(SENTENCES_DIR, LanguageCodeHandler.getCodeForLanguage(language) + "_sentences.tsv");
		return sentencesFile.exists();
	}
	
	public static void downloadFileForLanguage(String language) throws IOException {
		String languageCode = LanguageCodeHandler.getCodeForLanguage(language);
		File compressedFile = new File(SENTENCES_DIR, languageCode + "_sentences.tsv.bz2");
		downloadFile("https://downloads.tatoeba.org/exports/per_language/" + languageCode + "/" + languageCode + "_sentences.tsv.bz2", compressedFile);
		File targetExtractionFile = new File(SENTENCES_DIR, languageCode + "_sentences.tsv");
		extractBZip2(compressedFile, targetExtractionFile);
	}
	
	private static void extractBZip2(File compressedFile, File targetExtractionFile) throws IOException {
		try (BZip2CompressorInputStream compressedInputStream = new BZip2CompressorInputStream(new BufferedInputStream(Files.newInputStream(compressedFile.toPath())));
		     OutputStream targetOutputStream = Files.newOutputStream(targetExtractionFile.toPath())) {
			final byte[] buffer = new byte[1024];
			int n;
			while (-1 != (n = compressedInputStream.read(buffer))) {
				targetOutputStream.write(buffer, 0, n);
			}
		}
	}
	
	private static void downloadFile(String urlStr, File file) throws IOException {
		URL url = new URL(urlStr);
		try (ReadableByteChannel urlChannel = Channels.newChannel(url.openStream());
		     FileOutputStream fileOutputStream = new FileOutputStream(file)) {
			fileOutputStream.getChannel().transferFrom(urlChannel, 0, Long.MAX_VALUE);
		}
	}
}
