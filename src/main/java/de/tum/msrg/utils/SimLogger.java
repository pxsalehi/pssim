package de.tum.msrg.utils;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

import jist.runtime.JistAPI;

public class SimLogger {

	private static Logger logger = Logger.getLogger("simulation");
	
	static {
		FileAppender fa = new FileAppender();
		fa.setName("FileLogger");
		fa.setFile("sim.log");
		fa.setLayout(new PatternLayout("%-5p %m%n"));
		fa.setThreshold(Level.DEBUG);
		fa.setAppend(false);
		fa.activateOptions();
		Logger.getRootLogger().addAppender(fa);
		
		Logger.getRootLogger().setLevel((Level) Level.ERROR);

		ConsoleAppender console = new ConsoleAppender(); // create appender
		console.setLayout(new PatternLayout("%-5p %m%n"));
		console.setThreshold(Level.DEBUG);
		console.activateOptions();
		// add appender to any Logger (here is root)
		Logger.getRootLogger().addAppender(console);
	}
	
	public static void setLevel(Level l) {
		logger.setLevel((Level)l);
	}
	
	public static void debug(Object o) {
		logger.debug("[sim_time:" + JistAPI.getTime() + "] " + o);
	}
	
	public static void info(Object o) {
		logger.info("[sim_time:" + JistAPI.getTime() + "] " + o);
	}
	
	public static void warn(Object o) {
		logger.warn("[sim_time:" + JistAPI.getTime() + "] " + o);
	}

	public static void warn(Object o, Throwable t) {
		logger.warn("[sim_time:" + JistAPI.getTime() + "] " + o, t);
	}
	
	public static void error(Object o) {
		logger.error("[sim_time:" + JistAPI.getTime() + "] " + o);
	}

	public static void error(Object o, Throwable t) {
		logger.error("[sim_time:" + JistAPI.getTime() + "] " + o, t);
	}
	
	public static void fatal(Object o) {
		logger.fatal("[sim_time:" + JistAPI.getTime() + "] " + o);
	}

	public static void fatal(Object o, Throwable t) {
		logger.fatal("[sim_time:" + JistAPI.getTime() + "] " + o, t);
	}
	
	public static boolean isDebugEnabled() {
		return logger.isDebugEnabled();
	}
	
	public static boolean isInfoEnabled() {
		return logger.isInfoEnabled();
	}
	
	public static void main(String[] args) {
		SimLogger.debug("Debug message!");
	}
}
