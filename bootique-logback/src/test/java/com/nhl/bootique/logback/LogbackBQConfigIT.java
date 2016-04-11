package com.nhl.bootique.logback;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.joining;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Map;

import org.junit.Rule;
import org.junit.Test;

import ch.qos.logback.classic.Logger;

public class LogbackBQConfigIT {

	@Rule
	public LogbackTestFactory LOGGER_STACK = new LogbackTestFactory();

	@Test
	public void testFileAppender() {

		LOGGER_STACK.prepareLogDir("target/logs/rotate");
		Logger logger = LOGGER_STACK.newRootLogger("classpath:com/nhl/bootique/logback/test-file-appender.yml");
		logger.info("info-log-to-file");

		// must stop to ensure logs are flushed...
		LOGGER_STACK.stop();

		Map<String, String[]> logfileContents = LOGGER_STACK.loglines("target/logs/rotate", "logfile1.log");

		assertEquals(1, logfileContents.size());
		String[] lines = logfileContents.get("logfile1.log");
		String oneLine = asList(lines).stream().collect(joining("\n"));

		assertTrue("Unexpected logs: " + oneLine, oneLine.endsWith("ROOT: info-log-to-file"));
	}

	@Test
	public void testFileAppender_Rotate_ByTime() throws InterruptedException, IOException {

		LOGGER_STACK.prepareLogDir("target/logs/rotate-by-time");

		Logger logger = LOGGER_STACK
				.newRootLogger("classpath:com/nhl/bootique/logback/test-file-appender-10sec-rotation.yml");
		logger.info("info-log-to-file1");
		logger.info("info-log-to-file2");

		// file rotation happens every second... so wait at least that long
		Thread.sleep(1001);

		logger.info("info-log-to-file3");
		logger.info("info-log-to-file4");

		// must stop to ensure logs are flushed...
		LOGGER_STACK.stop();

		Map<String, String[]> logfileContents = LOGGER_STACK.loglines("target/logs/rotate-by-time", "logfile-");

		assertTrue(logfileContents.size() > 1);
		logfileContents.forEach((f, lines) -> assertTrue(lines.length > 0));
		assertEquals(4, logfileContents.values().stream().flatMap(array -> asList(array).stream())
				.filter(s -> s.contains("info-log-to-file")).count());
	}

	@Test
	public void testFileAppender_Rotate_BySize() throws InterruptedException, IOException {

		LOGGER_STACK.prepareLogDir("target/logs/rotate-by-size");

		Logger logger = LOGGER_STACK
				.newRootLogger("classpath:com/nhl/bootique/logback/test-file-appender-size-rotation.yml");
		logger.info("10bytelog1");
		logger.info("10bytelog2");

		// add a wait period to let size rotation to happen
		Thread.sleep(1001);

		logger.info("10bytelog3");
		logger.info("10bytelog4");

		// must stop to ensure logs are flushed...
		LOGGER_STACK.stop();

		Map<String, String[]> logfileContents = LOGGER_STACK.loglines("target/logs/rotate-by-size", "logfile-");

		assertTrue(logfileContents.size() > 1);
		logfileContents.forEach((f, lines) -> assertTrue(lines.length > 0));
		assertEquals(4, logfileContents.values().stream().flatMap(array -> asList(array).stream())
				.filter(s -> s.contains("10bytelog")).count());
	}

	@Test
	public void testFileAppender_Rotate_MaxFiles() throws InterruptedException, IOException {

		LOGGER_STACK.prepareLogDir("target/logs/rotate-maxfiles");

		Logger logger = LOGGER_STACK
				.newRootLogger("classpath:com/nhl/bootique/logback/test-file-appender-maxfiles-rotation.yml");
		logger.info("log1");
		logger.info("log2");

		// file rotation happens every second... so wait at least that long
		Thread.sleep(1001);

		logger.info("log3");
		logger.info("log4");

		// file rotation happens every second... so wait at least that long
		Thread.sleep(1001);

		logger.info("log5");
		logger.info("log6");

		// file rotation happens every second... so wait at least that long
		Thread.sleep(1001);

		logger.info("log7");
		logger.info("log8");

		// must stop to ensure logs are flushed...
		LOGGER_STACK.stop();

		Map<String, String[]> logfileContents = LOGGER_STACK.loglines("target/logs/rotate-maxfiles", "logfile-");

		// 3 = 1 current file + 2 archived
		assertEquals(3, logfileContents.size());
		logfileContents.forEach((f, lines) -> assertTrue(lines.length > 0));
	}
}