package config;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.util.MissingResourceException;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import AltiConsole.Application;
import l10n.Translator;
import net.miginfocom.swing.MigLayout;


public class LicenseDialog extends JDialog {
	private static final String LICENSE_FILENAME = "LICENSE.TXT";
	private static final Translator trans = Application.getTranslator();
	
	
	private static final String DEFAULT_LICENSE_TEXT =
		"\n" +
		"Error:  Unable to load " + LICENSE_FILENAME + "!\n" +
		"\n" +
		"AltiConsole is licensed under the GNU GPL version 3, with additional permissions.\n" +
		"See http://rocket.payload.free.fr/ for details.";

	public LicenseDialog(JFrame parent) {
		super(parent, true);
		
		JPanel panel = new JPanel(new MigLayout("fill"));
		
		panel.add(new StyledLabel("AltiConsole license", 10), "ax 50%, wrap para");
		
		String licenseText;
		try {
			InputStream is = ClassLoader.getSystemResourceAsStream(LICENSE_FILENAME);
			if (is == null) {
					throw new MissingResourceException(
						"build.properties not found, distribution built wrong" +
								"   classpath:" + System.getProperty("java.class.path"),
						"build.properties", "build.version");
			}
			
			BufferedReader reader = new BufferedReader(
					new InputStreamReader(is));
			StringBuffer sb = new StringBuffer();
			for (String s = reader.readLine(); s != null; s = reader.readLine()) {
				sb.append(s);
				sb.append('\n');
			}
			licenseText = sb.toString();
			
		} catch (IOException e) {

			licenseText = DEFAULT_LICENSE_TEXT;
			
		}
		
		JTextArea text = new JTextArea(licenseText);
		text.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
		text.setRows(20);
		text.setColumns(80);
		text.setEditable(false);
		panel.add(new JScrollPane(text),"grow, wrap para");
		
		//Close button
		JButton close = new JButton(trans.get("dlg.but.close"));
		close.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				LicenseDialog.this.dispose();
			}
		});
		panel.add(close, "right");
		
		this.add(panel);
		this.setTitle("AltiConsole license");
		this.pack();
		this.setLocationRelativeTo(parent);
		
	}
	private static LicenseDialog dialog = null;
	public static void showPreferences(JFrame parent) {
		if (dialog != null) {
			dialog.dispose();
		}
		dialog = new LicenseDialog(parent);
		dialog.setVisible(true);
	}
	
}
