/**
 * 
 */
package com.chronosystems.log.helper;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * @author andre.silva
 *
 */
public class PropertiesHelper {
	
	private static final Properties prop;

	static {
		prop = new Properties();
		try {
			prop.load(PropertiesHelper.class.getResourceAsStream("/environment.properties"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static String getProperty(String key) {
        return getProperty(key, null);
    }

    public static String getProperty(String key, String defaultValue) {
        String value = prop.getProperty(key);
        if (value == null) {
            value = defaultValue;
        }
        return value;
    }

    public static List<String> getProperties(String key) {
        return getProperties(new ArrayList<String>(), key, 0);
    }

    private static List<String> getProperties(List<String> currentResult, String key, int index) {
        final String value = getProperty(key + "." + String.valueOf(index));
        if (value == null) {
            return currentResult;
        }
        currentResult.add(value);
        return getProperties(currentResult, key, index+1);
    }
}
