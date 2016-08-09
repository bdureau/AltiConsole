package config;

import l10n.L10N;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;



public class UserPref {
	
	private static final List<Locale> SUPPORTED_LOCALES;
	static {
		List<Locale> list = new ArrayList<Locale>();
		for (String lang : new String[] { "en", "it", "es", "fr" }) {
			list.add(new Locale(lang));
		}
		SUPPORTED_LOCALES = Collections.unmodifiableList(list);
	}
	private static final Preferences PREFNODE;
	private static final boolean DEBUG;
	
	static {
		DEBUG = false;
	}
	
	/**
	 * Whether to clear all preferences at application startup.  This has an effect only
	 * if DEBUG is true.
	 */
	private static final boolean CLEARPREFS = true;
	
	/**
	 * The node name to use in the Java preferences storage.
	 */
	private static final String NODENAME = "AltiConsole";
	
	static {
		Preferences root = Preferences.userRoot();
		if (DEBUG && CLEARPREFS) {
			try {
				if (root.nodeExists(NODENAME)) {
					root.node(NODENAME).removeNode();
				}
			} catch (BackingStoreException e) {
				//throw new BugException("Unable to clear preference node", e);
			}
		}
		PREFNODE = root.node(NODENAME);
		//NODE = PREFNODE;
	}
	/*
	 * Load property file only when necessary.
	 */
	private static class BuildPropertyHolder {
		
		public static final Properties PROPERTIES;
		public static final String BUILD_VERSION;
		public static final String BUILD_SOURCE;
		public static final boolean DEFAULT_CHECK_UPDATES;
		
		static {
			try {
				InputStream is = ClassLoader.getSystemResourceAsStream("build.properties");
				if (is == null) {
					throw new MissingResourceException(
							"build.properties not found, distribution built wrong" +
									"   classpath:" + System.getProperty("java.class.path"),
							"build.properties", "build.version");
				}
				
				PROPERTIES = new Properties();
				PROPERTIES.load(is);
				is.close();
				
				String version = PROPERTIES.getProperty("build.version");
				if (version == null) {
					throw new MissingResourceException(
							"build.version not found in property file",
							"build.properties", "build.version");
				}
				BUILD_VERSION = version.trim();
				
				BUILD_SOURCE = PROPERTIES.getProperty("build.source");
				if (BUILD_SOURCE == null) {
					throw new MissingResourceException(
							"build.source not found in property file",
							"build.properties", "build.source");
				}
				
				String value = PROPERTIES.getProperty("build.checkupdates");
				if (value != null)
					DEFAULT_CHECK_UPDATES = Boolean.parseBoolean(value);
				else
					DEFAULT_CHECK_UPDATES = true;
				
			} catch (IOException e) {
				throw new MissingResourceException(
						"Error reading build.properties",
						"build.properties", "build.version");
			}
		}
	}
	/**
	 * Return the OpenRocket version number.
	 */
	public static String getVersion() {
		return BuildPropertyHolder.BUILD_VERSION;
	}
	/**
	 * Store the current OpenRocket version into the preferences to allow for preferences migration.
	 */
	private static void storeVersion() {
		PREFNODE.put("AltiConsoleVersion", getVersion());
	}
	/**
	 * Returns a limited-range integer value from the preferences.  If the value 
	 * in the preferences is negative or greater than max, then the default value 
	 * is returned.
	 * 
	 * @param key  The preference to retrieve.
	 * @param max  Maximum allowed value for the choice.
	 * @param def  Default value.
	 * @return   The preference value.
	 */
	public static int getChoise(String key, int max, int def) {
		int v = PREFNODE.getInt(key, def);
		if ((v < 0) || (v > max))
			return def;
		return v;
	}
	
	
	/**
	 * Helper method that puts an integer choice value into the preferences.
	 * 
	 * @param key     the preference key.
	 * @param value   the value to store.
	 */
	public static void putChoise(String key, int value) {
		PREFNODE.putInt(key, value);
		storeVersion();
	}
	
	
	/**
	 * Return a string preference.
	 * 
	 * @param key	the preference key.
	 * @param def	the default if no preference is stored
	 * @return		the preference value
	 */
	public static String getString(String key, String def) {
		return PREFNODE.get(key, def);
	}
	
	/**
	 * Set a string preference.
	 * 
	 * @param key		the preference key
	 * @param value		the value to set, or <code>null</code> to remove the key
	 */
	public static void putString(String key, String value) {
		if (value == null) {
			PREFNODE.remove(key);
			return;
		}
		PREFNODE.put(key, value);
		storeVersion();
	}
	
	
	/**
	 * Retrieve an enum value from the user preferences.
	 * 
	 * @param <T>	the enum type
	 * @param key	the key
	 * @param def	the default value, cannot be null
	 * @return		the value in the preferences, or the default value
	 */
	public static <T extends Enum<T>> T getEnum(String key, T def) {
		if (def == null) {
			//throw new BugException("Default value cannot be null");
			//throw "Default value cannot be null";
		}
		
		String value = getString(key, null);
		if (value == null) {
			return def;
		}
		
		try {
			return Enum.valueOf(def.getDeclaringClass(), value);
		} catch (IllegalArgumentException e) {
			return def;
		}
	}
	
	/**
	 * Store an enum value to the user preferences.
	 * 
	 * @param key		the key
	 * @param value		the value to store, or null to remove the value
	 */
	public static void putEnum(String key, Enum<?> value) {
		if (value == null) {
			putString(key, null);
		} else {
			putString(key, value.name());
		}
	}
	
	
	/**
	 * Return a boolean preference.
	 * 
	 * @param key	the preference key
	 * @param def	the default if no preference is stored
	 * @return		the preference value
	 */
	public static boolean getBoolean(String key, boolean def) {
		return PREFNODE.getBoolean(key, def);
	}
	
	/**
	 * Set a boolean preference.
	 * 
	 * @param key		the preference key
	 * @param value		the value to set
	 */
	public static void putBoolean(String key, boolean value) {
		PREFNODE.putBoolean(key, value);
		storeVersion();
	}
	
	
	/**
	 * Return a preferences object for the specified node name.
	 * 
	 * @param nodeName	the node name
	 * @return			the preferences object for that node
	 */
	public static Preferences getNode(String nodeName) {
		return PREFNODE.node(nodeName);
	}
	
	

	public static List<Locale> getSupportedLocales() {
		return SUPPORTED_LOCALES;
	}
	
	public static Locale getUserLocale() {
		String locale = getString("locale", null);
		return L10N.toLocale(locale);
	}
	
	public static void setUserLocale(Locale l) {
		if (l == null) {
			putString("locale", null);
		} else {
			putString("locale", l.toString());
		}
	}

	public static Locale getApplicationLocale() {
		String locale = getString("application_locale", null);
		return L10N.toLocale(locale);
	}
	
	public static void setApplicationLocale(Locale l) {
		if (l == null) {
			putString("application_locale", null);
		} else {
			putString("application_locale", l.toString());
		}
	}
	
	public static String getDefComSpeed() {
		String comSpeed = getString("com_speed", null);
		return comSpeed;
	}
	
	public static void setDefComSpeed(String comSpeed) {
		if (comSpeed == null) {
			putString("com_speed", null);
		} else {
			putString("com_speed", comSpeed);
		}
	}
	
	public static String getAppUnits() {
		String unit = getString("app_unit", null);
		return unit;
	}
	
	public static void setAppUnits(String l) {
		if (l == null) {
			putString("app_unit", null);
		} else {
			putString("app_unit", l);
		}
	}
	
	public static String getAvrdudePath() {
		String AvrdudePath = getString("avrdude_path", null);
		return AvrdudePath;
	}
	
	public static void setAvrdudePath(String l) {
		if (l == null) {
			putString("avrdude_path", null);
		} else {
			putString("avrdude_path", l);
		}
	}
	public static String getAvrdudeConfigPath() {
		String AvrdudeConfigPath = getString("avrdude_config_path", null);
		return AvrdudeConfigPath;
	}
	
	public static void setAvrdudeConfigPath(String l) {
		if (l == null) {
			putString("avrdude_config_path", null);
		} else {
			putString("avrdude_config_path", l);
		}
	}
	
	public static String getRetrievalTimeout() {
		String RetrievalTimeout = getString("retrieval_timeout", null);
		return RetrievalTimeout;
	}
	
	public static void setRetrievalTimeout(String l) {
		if (l == null) {
			putString("retrieval_timeout", null);
		} else {
			putString("retrieval_timeout", l);
		}
	}

}
