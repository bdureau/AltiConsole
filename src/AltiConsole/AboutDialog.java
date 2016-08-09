package AltiConsole;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.MissingResourceException;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.ImageIcon;

import config.UserPref;
import l10n.DebugTranslator;
import l10n.ResourceBundleTranslator;
import l10n.Translator;
import net.miginfocom.swing.MigLayout;
//import config.UserPref;
import config.Utils;
import config.URLLabel;
import config.StyledLabel;
import config.Chars;
import config.Icons;
import config.BuildProperties;
import config.StyledLabel.Style;
import config.DescriptionArea;
import javax.swing.ImageIcon;


public class AboutDialog extends JDialog{
	public static final String BEAR_ALTIMETER_URL = "http://rocket.payload.free.fr/";
	private static final Translator trans = Application.getTranslator();
	
	private static final String CREDITS = "<html><center>" +
			"<font size=\"+1\"><b>" + trans.get("AboutDialog.lbl1") + "</b></font><br><br>" +
			"Boris du Reau <br>" +
			"<b>" + trans.get("AboutDialog.lbl2") + "</b><br>" +
			"David Sari\u00F1ena (Spanish translations)<br>" +
			"Mauro Biasutti (Italian translations)<br>" +
			"<b>" + trans.get("AboutDialog.lbl3") + "</b><br><br>" +
			"MiG Layout (http://www.miglayout.com/)<br>" +
			"JFreeChart (http://www.jfree.org/jfreechart/)<br>" +
			"RxtxSerial (http://rxtx.qbang.org/)<br>";
	
	
	public AboutDialog(JFrame parent) {
		super(parent, true);
		
		final String version = BuildProperties.getVersion();
		
		JPanel panel = new JPanel(new MigLayout("fill"));
		JPanel sub;
		
		
		// Bear Altimeter logo
		
		ImageIcon mylogo = Icons.loadImageIcon("pix/bear_altimeters-small.png", "Bear Altimeter");
		
		if (mylogo == null)
			mylogo = Icons.loadImageIcon("bear_altimeters-small.png", "Bear Altimeter");
		
		panel.add(new JLabel(mylogo), "top");

		
		// Alti console version info + copyright
		sub = new JPanel(new MigLayout("fill"));
		
		sub.add(new StyledLabel("AltiConsole", 20), "ax 50%, growy, wrap para");
		sub.add(new StyledLabel(trans.get("lbl.version").trim() + " " + version, 3), "ax 50%, growy, wrap rel");
		sub.add(new StyledLabel("Copyright " + Chars.COPY + " 2012-2013 Boris du Reau"), "ax 50%, growy, wrap para");
		
		sub.add(new URLLabel(BEAR_ALTIMETER_URL), "ax 50%, growy, wrap para");
		panel.add(sub, "grow");

			sub = new JPanel(new MigLayout("fill"));

			
			panel.add(sub);
		
		DescriptionArea info = new DescriptionArea(5);
		info.setText(CREDITS);
		panel.add(info, "newline, width 10px, height 150lp, grow, spanx, wrap para");
		
		//Close button
		JButton close = new JButton(trans.get("button.close"));
		close.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				AboutDialog.this.dispose();
			}
		});
		panel.add(close, "spanx, right");
		
		this.add(panel);
		this.setTitle("AltiConsole " + version);
		this.pack();
		this.setResizable(false);
		this.setLocationRelativeTo(parent);
	
	}
	private static AboutDialog dialog = null;
	public static void showPreferences(JFrame parent) {
		if (dialog != null) {
			dialog.dispose();
		}
		dialog = new AboutDialog(parent);
		dialog.setVisible(true);
	}
}
