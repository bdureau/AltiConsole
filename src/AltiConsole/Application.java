package AltiConsole;

import l10n.ClassBasedTranslator;
import l10n.DebugTranslator;
import l10n.ExceptionSuppressingTranslator;
import l10n.Translator;

/**
 * A class that provides singleton instances / beans for other classes to utilize.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public final class Application {
	
	private static Translator baseTranslator = new DebugTranslator(null);
	
		
	/**
	 * Return the translator to use for obtaining translated strings.
	 * @return	a translator.
	 */
	public static Translator getTranslator() {
		if (baseTranslator instanceof DebugTranslator) {
			return baseTranslator;
		}
		
		Translator t = baseTranslator;
		t = new ClassBasedTranslator(t, 1);
		t = new ExceptionSuppressingTranslator(t);
		return t;
	}
	
	/**
	 * Set the translator used in obtaining translated strings.
	 * @param translator	the translator to set.
	 */
	public static void setBaseTranslator(Translator translator) {
		Application.baseTranslator = translator;
	}
	

}
