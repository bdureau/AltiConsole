package AltiConsole;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;

//import java.awt.Dialog;
import javax.swing.JDialog;

//import java.awt.Frame;
import java.awt.BorderLayout;
//import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;


//import javax.swing.JDialog;
import javax.swing.JComboBox;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import l10n.Translator;
//import java.util.Locale;


import net.miginfocom.swing.MigLayout;
//import net.sf.openrocket.gui.dialogs.preferences.PreferencesDialog;
import config.UserPref;
import config.Named;
import config.Utils;
//import net.sf.openrocket.util.Named;
//import net.sf.openrocket.util.Prefs;
//import net.sf.openrocket.util.Utils;
//import java.awt.Dimension;

public class Preferences extends JDialog {

	private static final Translator trans = Application.getTranslator();

	private static final long serialVersionUID = 1L;
	private JPanel jContentPane = null;
	private JButton close = null;
	private JComboBox jComboBoxLangage = null;
	private JComboBox jComboBoxUnit = null;
	private JComboBox jComboBoxComSpeed = null;
	private JPanel jPanelTop = null;
	private JTextArea commandText = null;
	private JTextArea commandTextConfig = null;
	private JSpinner spinTimeOut = null;

	private Preferences(JFrame/*Window*/ parent) {
		//// Preferences
		//super(parent, "Pref", Dialog.ModalityType.APPLICATION_MODAL);
		super(parent, true);
		this.setTitle(trans.get("title.ApplicationConfig"));
		initialize(parent);
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize(JFrame/*Window*/ parent) {
		this.setSize(543, 250);
		this.setLocationRelativeTo(parent);
		this.setContentPane(getJContentPane());
		pack();
	}

	/**
	 * This method initializes jContentPane
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJContentPane() {
		if (jContentPane == null) {
			jContentPane = new JPanel(new MigLayout("fillx, ins 30lp n n n"));
			//		JPanel panel = new JPanel(new MigLayout("fillx, ins 30lp n n n"));

			//jContentPane.setLayout(new BorderLayout());
		}

		//Close
		close = new JButton(trans.get("dlg.but.close"));
		close.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				//this.setVisible(false);
				//jContentPane.close();//dispose();
				Preferences.this.dispose();
			}
		});
		System.out.println("pref1\n");
		//language
		jContentPane.add(new JLabel(trans.get("lbl.language")), "gapright para");
		jContentPane.add(getJComboBoxLangage(),"wrap rel, growx, sg combos");
		
		//Units
		jContentPane.add(new JLabel(trans.get("choose.lbl.unit")), "gapright para");	
		jContentPane.add(getJComboBoxUnits(),"wrap rel, growx, sg combos");

		//Default Com port speed
		jContentPane.add(new JLabel(trans.get("def.lbl.ComSpeed")), "gapright para");
		jContentPane.add(getJComboBoxComSpeed(),"wrap rel, growx, sg combos");
		
		//Data retrieval timeout
		jContentPane.add(new JLabel(trans.get("def.lbl.RetrievalTimeOut")), "gapright para");
		jContentPane.add(getJSpinerTimeOut(),"wrap rel, growx, sg combos");
		System.out.println("pref10\n");
		//Avrdude path
		jContentPane.add(new JLabel(trans.get("def.lbl.avrdudepath")), "gapright para");
		commandText = new JTextArea();
		System.out.println("pref10-1\n");
		if (UserPref.getAvrdudePath() !=null)
			if (!UserPref.getAvrdudePath().equals(""))
				commandText.setText(UserPref.getAvrdudePath());
		else
			commandText.setText(System.getProperty("user.dir")+ "\\avrdude");
		System.out.println("pref11\n");
		commandText.getDocument().addDocumentListener(new DocumentListener() {
			
			@Override
			public void insertUpdate(DocumentEvent e) {
				//preferences.setDecalEditorPreference(false, commandText.getText());
				UserPref.setAvrdudePath(commandText.getText());
			}
			
			@Override
			public void removeUpdate(DocumentEvent e) {
				//preferences.setDecalEditorPreference(false, commandText.getText());
				UserPref.setAvrdudePath(commandText.getText());
			}
			
			@Override
			public void changedUpdate(DocumentEvent e) {
				//preferences.setDecalEditorPreference(false, commandText.getText());
				UserPref.setAvrdudePath(commandText.getText());
			}
			
		});
		System.out.println("pref12\n");
		jContentPane.add(commandText, "growx");
		final JButton chooser = new JButton("...");
		chooser.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser fc = new JFileChooser();
				int action = fc.showOpenDialog(SwingUtilities.windowForComponent(Preferences.this));
				if (action == JFileChooser.APPROVE_OPTION) {
					String commandLine = fc.getSelectedFile().getAbsolutePath();
					commandText.setText(commandLine);
					//preferences.setDecalEditorPreference(false, commandLine);
					UserPref.setAvrdudePath(commandLine);
				}
				
			}
		});
		jContentPane.add(chooser, "wrap");
		
		System.out.println("pref13\n");
		//Avrdude config path
		jContentPane.add(new JLabel(trans.get("def.lbl.avrdudeconfigpath")), "gapright para");
		commandTextConfig = new JTextArea();
		commandTextConfig.setText(UserPref.getAvrdudeConfigPath());
		System.out.println("pref14\n");
		if (UserPref.getAvrdudeConfigPath()!=null)
		if (!UserPref.getAvrdudeConfigPath().equals(""))
			commandTextConfig.setText(UserPref.getAvrdudeConfigPath());
		else
			commandTextConfig.setText(System.getProperty("user.dir")+ "\\avrdude.conf");
		jContentPane.add(commandTextConfig, "growx");
		commandTextConfig.getDocument().addDocumentListener(new DocumentListener() {
			
			@Override
			public void insertUpdate(DocumentEvent e) {
				//preferences.setDecalEditorPreference(false, commandText.getText());
				UserPref.setAvrdudeConfigPath(commandTextConfig.getText());
			}
			
			@Override
			public void removeUpdate(DocumentEvent e) {
				//preferences.setDecalEditorPreference(false, commandText.getText());
				UserPref.setAvrdudeConfigPath(commandTextConfig.getText());
			}
			
			@Override
			public void changedUpdate(DocumentEvent e) {
				//preferences.setDecalEditorPreference(false, commandText.getText());
				UserPref.setAvrdudeConfigPath(commandTextConfig.getText());
			}
			
		});
		System.out.println("pref15\n");
		final JButton chooserConf = new JButton("...");
		chooserConf.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser fc2 = new JFileChooser();
				int action = fc2.showOpenDialog(SwingUtilities.windowForComponent(Preferences.this));
				if (action == JFileChooser.APPROVE_OPTION) {
					String commandLine2 = fc2.getSelectedFile().getAbsolutePath();
					commandTextConfig.setText(commandLine2);
					//preferences.setDecalEditorPreference(false, commandLine);
					UserPref.setAvrdudeConfigPath(commandLine2);
				}
				
			}
		});
		jContentPane.add(chooserConf, "wrap");
		jContentPane.add(close, "wrap");
		return jContentPane;
	}

	/**
	 * This method initializes jComboBoxLangage	
	 * 	
	 * @return javax.swing.JComboBox	
	 */
	private JComboBox getJComboBoxLangage() {
		System.out.println("pref2\n");
		if (jComboBoxLangage == null) {
			//jComboBoxLangage = new JComboBox();
			//List<Locale> locales = new ArrayList<Locale>();
			System.out.println("pref3\n");
			////
			//// Language selector
			Locale userLocale = UserPref.getApplicationLocale();
			System.out.println("pref4\n");
			List<Named<Locale>> locales = new ArrayList<Named<Locale>>();
			for (Locale l : UserPref.getSupportedLocales()) {
				locales.add(new Named<Locale>(l, l.getDisplayLanguage()));
			}
			System.out.println("pref5\n");
			Collections.sort(locales);
			locales.add(0, new Named<Locale>(null, trans.get("languages.default")));
			
			//final JComboBox 
			jComboBoxLangage = new JComboBox(locales.toArray());
			for (int i = 0; i < locales.size(); i++) {
				if (Utils.equals(userLocale, locales.get(i).get())) {
					jComboBoxLangage.setSelectedIndex(i);
				}
			}
			jComboBoxLangage.addActionListener(new ActionListener() {
				@Override
				@SuppressWarnings("unchecked")
				public void actionPerformed(ActionEvent e) {
					Named<Locale> selection = (Named<Locale>) jComboBoxLangage.getSelectedItem();
					UserPref.setApplicationLocale(selection.get());
				}
			});
			
			///
		}
		return jComboBoxLangage;
	}

	private JComboBox getJComboBoxUnits() {
		System.out.println("pref6\n");
		if (jComboBoxUnit == null) {
			//jComboBoxLangage = new JComboBox();

			String userUnit = UserPref.getAppUnits();
			jComboBoxUnit = new JComboBox();
			
			jComboBoxUnit.insertItemAt(trans.get("Unit.Metrics"), 0);
			jComboBoxUnit.insertItemAt(trans.get("Unit.Imperial"), 1);
			//jComboBoxUnit.setSelectedIndex(0);
			if (Utils.equals(userUnit, "Unit.Metrics")) {
				jComboBoxUnit.setSelectedIndex(0);
			}
			else
				jComboBoxUnit.setSelectedIndex(1);
			
			
			jComboBoxUnit.addActionListener(new ActionListener() {
				@Override
//				@SuppressWarnings("unchecked")
				public void actionPerformed(ActionEvent e) {
					String selection =  (String) jComboBoxUnit.getSelectedItem();
					if (Utils.equals(selection, trans.get("Unit.Metrics")))
						UserPref.setAppUnits("Unit.Metrics");
					else
						UserPref.setAppUnits("Unit.Imperial");
				}
			});

		}
		return jComboBoxUnit;
	}
	private JComboBox getJComboBoxComSpeed() {
		System.out.println("pref7\n");
		if (jComboBoxComSpeed == null) {

			//combo serial rate
			String[] serialRateStrings = {
				    	      "300","1200","2400","4800","9600","14400",
				    	      "19200","28800","38400","57600","115200"
				    	    };
			String ComSpeed = UserPref.getDefComSpeed();
			jComboBoxComSpeed = new JComboBox();
			for (int i = 0; i < serialRateStrings.length; i++)
			{
				jComboBoxComSpeed.addItem(serialRateStrings[i] ); 
				if (Utils.equals(ComSpeed, serialRateStrings[i])) {
					jComboBoxComSpeed.setSelectedIndex(i);
				}
			}
			
			jComboBoxComSpeed.addActionListener(new ActionListener() {
				@Override
//				@SuppressWarnings("unchecked")
				public void actionPerformed(ActionEvent e) {
					String selection =  (String) jComboBoxComSpeed.getSelectedItem();
					UserPref.setDefComSpeed(selection);
				}
			});

		}
		return jComboBoxComSpeed;
	}

	
	private JSpinner getJSpinerTimeOut() {
		System.out.println("pref8\n");
		if (spinTimeOut == null) {

			SpinnerModel spinModel = new SpinnerNumberModel(0, //initial value
	                0, //min
	                100000, //max
	                1);  //step 
			spinTimeOut = new JSpinner(spinModel);
			System.out.println(UserPref.getRetrievalTimeout());
			System.out.println("pref8-1\n");
			if (UserPref.getRetrievalTimeout()!=null )
			{
				if( UserPref.getRetrievalTimeout()!="")
				{
					System.out.println("pref8-1-1\n");
					System.out.println(UserPref.getRetrievalTimeout());
					spinTimeOut.setValue((Integer)Integer.parseInt(UserPref.getRetrievalTimeout())); 
					System.out.println("pref8-1-1-1\n");
					System.out.println(UserPref.getRetrievalTimeout());
				}
			}
			else
			{
				System.out.println("pref8-1-2\n");
				spinTimeOut.setValue(0); 
			}
			System.out.println("pref8-2\n");
			spinTimeOut.setToolTipText(trans.get("AltiConfigDlg.ttip.deployAltitude"));
			System.out.println("pref8-3\n");
			spinTimeOut.addChangeListener(new ChangeListener() {      
				  @Override
				  public void stateChanged(ChangeEvent e) {
				    // handle click
					  UserPref.setRetrievalTimeout(Integer.toString((Integer) spinTimeOut.getValue()));
				  }
				});
			System.out.println("pref8-4\n");
			

		}
		return spinTimeOut;
	}
	/**
	 * This method initializes jPanelTop	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJPanelTop() {
		System.out.println("pref9\n");
		if (jPanelTop == null) {
			/*GridBagConstraints gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.fill = GridBagConstraints.VERTICAL;
			gridBagConstraints.gridy = 0;
			gridBagConstraints.weightx = 1.0;
			gridBagConstraints.gridx = 0;*/
			jPanelTop = new JPanel();
			jPanelTop.setLayout(new  BorderLayout());
			jPanelTop.add(getJComboBoxLangage(), BorderLayout.EAST);//, gridBagConstraints);
		}
		return jPanelTop;
	}

	
	private static Preferences dialog = null;
	public static void showPreferences(JFrame/*Window*/ parent) {
		if (dialog != null) {
			dialog.dispose();
		}
		dialog = new Preferences(parent);
		dialog.setVisible(true);
	}
	

}  //  @jve:decl-index=0:visual-constraint="10,10"

