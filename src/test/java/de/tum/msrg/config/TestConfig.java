package de.tum.msrg.config;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.junit.Test;

import de.tum.msrg.config.Configuration;
import de.tum.msrg.config.ConfigParserException;

public class TestConfig {
	
	public enum DistType {
		RANDOM, UNIFORM;
	}
	
	private String correctConfig = "[section]\n"
	                             + "name = config \n"
	                             + "size = 70 \n"
	                             + "delay = 2.34 \n"
	                             + "boolean_var = true\n"
	                             + "[work load]\n"
	                             + "distribution = uniform\n"
	                             + "[lists] \n"
	                             + "clients = 4,5,90 \n";

	@Test
	public void testCorrect() throws ConfigParserException, IOException{
		Configuration config = new Configuration(correctConfig);
		assertEquals(config.getStringConfig("section.name"), "config");
		assertEquals(config.getIntConfig("section.size"), 70);
		assertEquals(config.getFloatConfig("section.delay"), 2.34, 0.001);
		assertTrue(config.getBooleanConfig("section.boolean_var"));
		assertEquals(config.getEnumConfig(DistType.class, "work load.distribution"), DistType.UNIFORM);
		List<Integer> actualClients = Arrays.asList(4, 90, 5);
		assertTrue(CollectionUtils.isEqualCollection(config.getList(int.class, "lists.clients"), actualClients));
		config.addProperty("runtime.verbose", false);
		assertFalse(config.getBooleanConfig("runtime.verbose"));
	}

}
