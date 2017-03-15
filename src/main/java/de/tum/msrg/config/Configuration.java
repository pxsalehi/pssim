package de.tum.msrg.config;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.List;

import jist.runtime.JistAPI;
import org.apache.commons.configuration2.INIConfiguration;
import org.apache.commons.configuration2.convert.DefaultListDelimiterHandler;
import org.apache.commons.configuration2.ex.ConfigurationException;

// TODO: change to a singleton
public class Configuration implements JistAPI.Timeless {
	// TODO: automatically detect if jist is running
	public static boolean JistIsRunning = true;
	private String configFileContent = null;
	private INIConfiguration config = new INIConfiguration();


	
	public Configuration(FileReader configFile) throws IOException, ConfigParserException {
		BufferedReader buffReader = new BufferedReader(configFile);
		String line, content = "";
		while((line = buffReader.readLine()) != null)
			content += line + "\n";
		buffReader.close();
		configFileContent = content;
		load();
	}
	
	public Configuration(String config) throws ConfigParserException, IOException {
		configFileContent = config;
		load();
	}
	
	public Configuration() {
	}
	
	private void load() throws ConfigParserException, IOException {
		config.setListDelimiterHandler(new DefaultListDelimiterHandler(','));
		try {
			config.read(new StringReader(configFileContent));
		} catch (ConfigurationException e) {
			throw new ConfigParserException(e);
		}
	}
	
	public float getFloatConfig(String key) {
		return config.getFloat(key);
	}
	
	public int getIntConfig(String key) {
		return config.getInt(key);
	}
	
	public long getLongConfig(String key) {
		return config.getLong(key);
	}
	
	public short getShortConfig(String key) {
		return config.getShort(key);
	}
	
	public boolean getBooleanConfig(String key) {
		return config.getBoolean(key);
	}

	public boolean getBooleanConfig(String key, boolean defaultVal) {
		return config.getBoolean(key, defaultVal);
	}
	
	public String getStringConfig(String key) {
		return config.getString(key);
	}
	
	public <T extends Enum<T>> T getEnumConfig(Class<T> enumType, String key) throws ConfigParserException {
		String value = config.getString(key);
		try {
			return Enum.valueOf(enumType, value.toUpperCase());
		} catch (IllegalArgumentException e) {
			throw new ConfigParserException("Illegal parameter for " + key);
		}
	}
	
	public <T> List<T> getList(Class<T> type, String key) {
		return config.getList(type, key);
	}

	public List<Object> getList(String key) {
		List<Object> ret = config.getList(key);
		return ret;
	}
	
	// if key exists, value is replaced, otherwise added
	public void setProperty(String key, Object value) {
		config.setProperty(key, value);
	}
	
	// if key exists, creates a list by adding new value
	public void addProperty(String key, Object value) {
		config.addProperty(key, value);
	}
}
