/**
 * 
 */
package com.chronosystems.log.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.chronosystems.log.helper.PropertiesHelper;
import com.chronosystems.log.model.Environment;

/**
 * @author andre.silva
 *
 */
public class ServerLogsService {

	private static final String LOG_SERVER_URL = "log.server.url";
	private static final String LOG_ENVIRONMENT_URL = "log.environment.url";
	private static final String LOG_DOWNLOAD_KEY = "log.download.key";

	public static List<String> collectLinksToDownload(Environment...environments) throws IOException {
		final String logServerUrl = PropertiesHelper.getProperty(LOG_SERVER_URL);
		final String logEnvironmentUrl = PropertiesHelper.getProperty(LOG_ENVIRONMENT_URL);
		final String logDownloadKey = PropertiesHelper.getProperty(LOG_DOWNLOAD_KEY);

		final List<String> resultList = new LinkedList<String>();

		for (Environment environment : environments) {

			// need http protocol
			final Document doc = Jsoup.connect(
					logServerUrl + 
					String.format(
							logEnvironmentUrl, 
							environment.getServer(), 
							environment.getPath(), 
							environment.getServer())).get();

			// get all links
			final Elements links = doc.select("a[href]");
			for (final Element link : links) {

				if (logDownloadKey.equals(link.text())) {
					// get the value from href attribute
					final String href = link.attr("href");
					resultList.add(logServerUrl + href);
				}
			}
		}
		return resultList;
	}

	public static void downloadFiles(Environment environment, String outputFolder, String downloadUrl) throws Exception {

		if (!outputFolder.endsWith(File.separator)) {
			outputFolder += File.separator;
		}

		final String absolutePath = outputFolder + 
				File.separator + 
				environment.getPath() + 
				File.separator + 
				environment.getServer();

		saveLog(absolutePath, downloadUrl);
	}

	private static void saveLog(final String absolutePath, final String downloadUrl) throws Exception {
		//System.out.println("downloadUrl: " + downloadUrl);

		final File directory = new File(absolutePath);
		if (!directory.exists()) {
			directory.mkdirs();
		}

		final String fileName = FilenameUtils.getName(downloadUrl);
		final File file = new File(absolutePath + File.separator + fileName);

		try (FileOutputStream fos = new FileOutputStream(file)) {

			final URL website = new URL(downloadUrl);
			final ReadableByteChannel rbc = Channels.newChannel(website.openStream());
			fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);

			cleanFile(website.openStream(), file);
		} catch (MalformedURLException e) {
			throw e;
		} catch (IOException e) {
			throw e;
		}
	}

	private static void cleanFile(final InputStream is, final File file) throws IOException {

		try {
			final List<String> updatedLines = new LinkedList<>(); 
			final List<String> lines = FileUtils.readLines(file);
			for (String line : lines) {
				if (line.contains("DOCTYPE")) {
					break;
				}
				updatedLines.add(line);
			}
			FileUtils.writeLines(file, updatedLines, false);
		} catch (IOException e) {
			throw e;
		}
	}

	public static void main(String[] args) throws Throwable {
		
		final InputStream is = ServerLogsService.class.getResourceAsStream("/access.log");
		final File log = new File("teste.txt");
		FileUtils.copyInputStreamToFile(is, log);

		final List<String> updatedLines = new LinkedList<>(); 
		final List<String> lines = FileUtils.readLines(log);
		for (String line : lines) {
			if (line.contains("DOCTYPE")) {
				break;
			}
			updatedLines.add(line);
		}
		final File file = new File("teste_resul.log");
		FileUtils.writeLines(file, updatedLines, false);
	}
}
