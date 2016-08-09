package AltiConfig;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.miginfocom.swing.MigLayout;
import l10n.Translator;
import AltiConsole.AltimeterSerial;
import AltiConsole.FlightData;
import AltiConfig.AltiConfigData;


import AltiConsole.Application;

public class AltiConfigDlg extends JDialog implements ActionListener{
	private static final Translator trans = Application.getTranslator();
	private JComboBox jComboBoxUnit = null;
	private JComboBox jComboBoxBeepingMode = null;
	private JComboBox jComboBoxOutputType = null;
	private JComboBox jComboBoxOutputType2 = null;
	private JComboBox jComboBoxOutputType3 = null;
	private JComboBox jComboBoxYesNo = null;
	private JSpinner spin = null;
	private JSpinner spinOutputType = null;
	private JSpinner spinOutputType2 = null;
	private JSpinner spinOutputType3 = null;
	private JSpinner spinBeepingFrequency = null;
	private static AltimeterSerial Serial;
	
	//Altimeter config variable
	private static AltiConfigData Alticonfig=null;
	
	public AltiConfigDlg(JFrame parent) {
		super(parent, true);
				
		JPanel panel = new JPanel(new MigLayout("fill"));

		//Altimeter name
		panel.add(new JLabel(trans.get("lbl.AltiName")), "gapright para");
		panel.add(new JLabel(Alticonfig.getAltimeterName()), "growx");

		//Altimeter version
		panel.add(new JLabel(trans.get("lbl.AltiVersion")+ " " +
				Alticonfig.getAltiMajorVersion()+"."+Alticonfig.getAltiMinorVersion()), "wrap rel, gapright para");

		//Altitude beeping mode
		panel.add(new JLabel(trans.get("lbl.AltBeepingMode")), "gapright para");
		panel.add(getJComboBoxBeepingMode(),"growx, sg combos");
		
		//beeping frequency
		panel.add(new JLabel(trans.get("lbl.BeepingFrequency")), "gapright para");
		panel.add(getSpinBeepingFrequency(),"growx");
		panel.add(new JLabel(trans.get("lbl.BeepingFrequencyUnit")), "wrap rel,gapright para");
			
		//Altitude Unit
		panel.add(new JLabel(trans.get("lbl.AltUnits")), "gapright para");
		panel.add(getJComboBoxUnits(),"wrap rel, growx, sg combos");
		
		//Altitude deployment for the main
		panel.add(new JLabel(trans.get("lbl.MainAlt")), "gapright para");
		
		SpinnerModel spinModel = new SpinnerNumberModel(0, //initial value
                0, //min
                200, //max
                1);  //step 
		spin = new JSpinner(spinModel);
		spin.setValue(Alticonfig.getMainAltitude());  
		spin.setToolTipText(trans.get("AltiConfigDlg.ttip.deployAltitude"));
		spin.addChangeListener(new ChangeListener() {      
			  @Override
			  public void stateChanged(ChangeEvent e) {
			    // handle click
				  Alticonfig.setMainAltitude((Integer)spin.getValue());
			  }
			});

		panel.add(spin,"wrap rel, growx, sg combos");
		
		//Channel 1
		panel.add(new JLabel(trans.get("lbl.Channel1")), "gapright para");
		panel.add(getJComboBoxOutputType(),"growx, sg combos");
		panel.add(new JLabel(trans.get("lbl.Channel1delay")), "gapright para");
		panel.add(getSpinOutput1(),"growx");
		panel.add(new JLabel("ms"), "wrap rel, growx");
		
		//Channel 2
		panel.add(new JLabel(trans.get("lbl.Channel2")), "gapright para");
		panel.add(getJComboBoxOutputType2(),"growx, sg combos");
		panel.add(new JLabel(trans.get("lbl.Channel2delay")), "gapright para");
		panel.add(getSpinOutput2(),"growx");
		panel.add(new JLabel("ms"), "wrap rel, growx");
		
		//Channel 3
		panel.add(new JLabel(trans.get("lbl.Channel3")), "gapright para");
		panel.add(getJComboBoxOutputType3(),"growx, sg combos");
		panel.add(new JLabel(trans.get("lbl.Channel3delay")), "gapright para");
		panel.add(getSpinOutput3(),"growx");
		panel.add(new JLabel("ms"), "wrap rel, growx");

		//Supersonic mode
		panel.add(new JLabel(trans.get("lbl.SupersonicMode")), "gapright para");
		panel.add(getJComboHypersonicYesNo(),"wrap rel, growx, sg combos");
		
		//upload button
		JButton upload = new JButton(trans.get("button.uploadConfig"));
		upload.addActionListener(new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			if (uploadAltiConfig())
				AltiConfigDlg.this.dispose();
			
			}
		});
		
		//Close button
		JButton close = new JButton(trans.get("button.close"));
		close.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				AltiConfigDlg.this.dispose();
			}
		});
		panel.add(upload, "left");
		panel.add(close, "spanx, right");
		
		this.add(panel);
		this.setTitle(trans.get("title.AltimeterConfig"));
		this.pack();
		this.setResizable(false);
		this.setLocationRelativeTo(parent);
	}
	
	private JComboBox getJComboBoxBeepingMode() {
		if (jComboBoxBeepingMode == null) {
		
			jComboBoxBeepingMode = new JComboBox();
			jComboBoxBeepingMode.insertItemAt(trans.get("BeepMode.Mode1"), 0);
			jComboBoxBeepingMode.insertItemAt(trans.get("BeepMode.Mode2"), 1);
			jComboBoxBeepingMode.setSelectedIndex(Alticonfig.getBeepingMode());
			jComboBoxBeepingMode.setActionCommand("ComboBoxBeepingMode");
			jComboBoxBeepingMode.addActionListener(this);
			jComboBoxBeepingMode.setToolTipText(trans.get("AltiConfigDlg.ttip.beepAltitude"));


		}
		return jComboBoxBeepingMode;
	}
	
	private JComboBox getJComboBoxUnits() {
		if (jComboBoxUnit == null) {
		
			jComboBoxUnit = new JComboBox();
			jComboBoxUnit.insertItemAt(trans.get("Unit.Meters"), 0);
			jComboBoxUnit.insertItemAt(trans.get("Unit.Feet"), 1);
			jComboBoxUnit.setSelectedIndex(Alticonfig.getUnits());
			jComboBoxUnit.setActionCommand("ComboBoxUnit");
			jComboBoxUnit.addActionListener(this);
			jComboBoxUnit.setToolTipText(trans.get("AltiConfigDlg.ttip.altitudeUnit"));

		}
		return jComboBoxUnit;
	}
	
	private JComboBox getJComboHypersonicYesNo() {
		if (jComboBoxYesNo == null) {
			
			jComboBoxYesNo = new JComboBox();
			jComboBoxYesNo.insertItemAt(trans.get("supersonic.yes"), 0);
			jComboBoxYesNo.insertItemAt(trans.get("supersonic.no"), 1);
			jComboBoxYesNo.setSelectedIndex(Alticonfig.getSupersonicYesNo());
			jComboBoxYesNo.setActionCommand("ComboBoxYesNo");
			jComboBoxYesNo.addActionListener(this);
			jComboBoxYesNo.setToolTipText(trans.get("AltiConfigDlg.ttip.supersonic"));

		}
		return jComboBoxYesNo;
	}
	
	private JComboBox getJComboBoxOutputType(){
		if (jComboBoxOutputType == null) {
			
			jComboBoxOutputType = new JComboBox();
			jComboBoxOutputType.insertItemAt(trans.get("OutputType.Main"), 0);
			jComboBoxOutputType.insertItemAt(trans.get("OutputType.Drogue"), 1);
			jComboBoxOutputType.insertItemAt(trans.get("OutputType.AirStart"), 2);
			jComboBoxOutputType.insertItemAt(trans.get("OutputType.Disabled"), 3);
			jComboBoxOutputType.setSelectedIndex(Alticonfig.getOutput1());
			jComboBoxOutputType.setActionCommand("ComboBoxOutputType");
			jComboBoxOutputType.addActionListener(this);
			jComboBoxOutputType.setToolTipText(trans.get("AltiConfigDlg.ttip.AssignPyro1"));


		}
		return jComboBoxOutputType;
	}
	
	private JComboBox getJComboBoxOutputType2(){
		if (jComboBoxOutputType2 == null) {
			
			jComboBoxOutputType2 = new JComboBox();
			jComboBoxOutputType2.insertItemAt(trans.get("OutputType.Main"), 0);
			jComboBoxOutputType2.insertItemAt(trans.get("OutputType.Drogue"), 1);
			jComboBoxOutputType2.insertItemAt(trans.get("OutputType.AirStart"), 2);
			jComboBoxOutputType2.insertItemAt(trans.get("OutputType.Disabled"), 3);
			jComboBoxOutputType2.setSelectedIndex(Alticonfig.getOutput2());
			jComboBoxOutputType2.setActionCommand("ComboBoxOutputType2");
			jComboBoxOutputType2.addActionListener(this);
			jComboBoxOutputType2.setToolTipText(trans.get("AltiConfigDlg.ttip.AssignPyro2"));


		}
		return jComboBoxOutputType2;
	}
	
	private JComboBox getJComboBoxOutputType3(){
		if (jComboBoxOutputType3 == null) {
			
			jComboBoxOutputType3 = new JComboBox();
			jComboBoxOutputType3.insertItemAt(trans.get("OutputType.Main"), 0);
			jComboBoxOutputType3.insertItemAt(trans.get("OutputType.Drogue"), 1);
			jComboBoxOutputType3.insertItemAt(trans.get("OutputType.AirStart"), 2);
			jComboBoxOutputType3.insertItemAt(trans.get("OutputType.Disabled"), 3);
			jComboBoxOutputType3.setSelectedIndex(Alticonfig.getOutput3());
			jComboBoxOutputType3.setActionCommand("ComboBoxOutputType3");
			jComboBoxOutputType3.addActionListener(this);
			jComboBoxOutputType3.setToolTipText(trans.get("AltiConfigDlg.ttip.AssignPyro3"));

		}
		return jComboBoxOutputType3;
	}
	
	private JSpinner getSpinBeepingFrequency()
	{
		SpinnerModel spinModel = new SpinnerNumberModel(200, //initial value
                200, //min
                1000, //max
                1);  //step 
		spinBeepingFrequency = new JSpinner(spinModel);
		spinBeepingFrequency.setValue(Alticonfig.getBeepingFrequency()); 
		spinBeepingFrequency.setToolTipText(trans.get("AltiConfigDlg.ttip.beepingFrequency"));
		
		spinBeepingFrequency.addChangeListener(new ChangeListener() {      
			  @Override
			  public void stateChanged(ChangeEvent e) {
			    // handle click
				  Alticonfig.setBeepingFrequency((Integer)spinBeepingFrequency.getValue());
			  }
			});
		return spinBeepingFrequency;
	}
	
	private JSpinner getSpinOutput1()
	{
		SpinnerModel spinModel = new SpinnerNumberModel(0, //initial value
                0, //min
                10000, //max
                1);  //step 
		spinOutputType = new JSpinner(spinModel);
		spinOutputType.setValue(Alticonfig.getOutput1Delay());  
		spinOutputType.setToolTipText(trans.get("Assign a delay fonction to the pyro output 1 \n If delay = 0 then it will fire as soon as triggerd"));

		spinOutputType.addChangeListener(new ChangeListener() {      
			  @Override
			  public void stateChanged(ChangeEvent e) {
			    // handle click
				  Alticonfig.setOutput1Delay((Integer)spinOutputType.getValue());
			  }
			});
		return spinOutputType;
	}
	
	private JSpinner getSpinOutput2()
	{
		SpinnerModel spinModel = new SpinnerNumberModel(0, //initial value
                0, //min
                10000, //max
                1);  //step 
		spinOutputType2 = new JSpinner(spinModel);
		spinOutputType2.setValue(Alticonfig.getOutput2Delay());  
		spinOutputType2.setToolTipText(trans.get("Assign a delay fonction to the pyro output 2 \n If delay = 0 then it will fire as soon as triggerd"));

		spinOutputType2.addChangeListener(new ChangeListener() {      
			  @Override
			  public void stateChanged(ChangeEvent e) {
			    // handle click
				  Alticonfig.setOutput2Delay((Integer)spinOutputType2.getValue());
			  }
			});
		return spinOutputType2;
	}

	private JSpinner getSpinOutput3()
	{
		SpinnerModel spinModel = new SpinnerNumberModel(0, //initial value
                0, //min
                10000, //max
                1);  //step 
		spinOutputType3 = new JSpinner(spinModel);
		spinOutputType3.setValue(Alticonfig.getOutput3Delay()); 
		spinOutputType3.setToolTipText(trans.get("Assign a delay fonction to the pyro output 3 \n If delay = 0 then it will fire as soon as triggerd"));

		spinOutputType3.addChangeListener(new ChangeListener() {      
			  @Override
			  public void stateChanged(ChangeEvent e) {
			    // handle click
				  Alticonfig.setOutput3Delay((Integer)spinOutputType3.getValue());
			  }
			});
		return spinOutputType3;
	}
	private static AltiConfigDlg dialog = null;
	public static void showPreferences(JFrame parent, AltiConfigData pAlticonfig, AltimeterSerial pSerial) {
		Alticonfig =pAlticonfig;
		Serial = pSerial;
		
		if (dialog != null) {
			dialog.dispose();
		}
		dialog = new AltiConfigDlg(parent);
		
		dialog.setVisible(true);
	}
	
	public static  boolean retrieveAltiConfig(){
	
		return true;
	}
	
	public static boolean uploadAltiConfig(){
		long nbrOfMain=0;
		long nbrOfDrogue=0;
		
		if (Alticonfig.getOutput1() == 0)
			nbrOfMain++;
		if (Alticonfig.getOutput2() == 0)
			nbrOfMain++;
		if (Alticonfig.getOutput3() == 0)
			nbrOfMain++;
		
		
		if (Alticonfig.getOutput1() == 1)
			nbrOfDrogue++;
		if (Alticonfig.getOutput2() == 1)
			nbrOfDrogue++;
		if (Alticonfig.getOutput3() == 1)
			nbrOfDrogue++;
		
		if (nbrOfMain > 1)
		{ 
			JOptionPane.showMessageDialog(
					null,
					"Only one main is allowed Please review your config",
					"Invalid config",
					JOptionPane.WARNING_MESSAGE);
			return false;
		}
		if (nbrOfDrogue > 1)
		{ 
			JOptionPane.showMessageDialog(
					null,
					"Only one Drogue is allowed Please review your config",
					"Invalid config",
					JOptionPane.WARNING_MESSAGE);
			return false;
		}
		System.out.println("Send back config\n");
		
		Serial.writeData("s," +
				Alticonfig.getUnits() +","+
		Alticonfig.getBeepingMode()+","+
		Alticonfig.getOutput1()+","+
		Alticonfig.getOutput2()+","+
		Alticonfig.getOutput3()+","+
		Alticonfig.getMainAltitude()+","+
		Alticonfig.getSupersonicYesNo() +","+
		Alticonfig.getOutput1Delay() +","+
		Alticonfig.getOutput2Delay() +","+
		Alticonfig.getOutput3Delay() +","+
		Alticonfig.getBeepingFrequency() +
		";\n");
		return true;
	}
	
	  /**
     * Handles all the actions.
     *
     * @param e  the action event.
     */
    public void actionPerformed(final ActionEvent e) {
    	
    	
    	
    	if (e.getActionCommand().equals("ComboBoxBeepingMode"))
    	{
    		Alticonfig.setBeepingMode(jComboBoxBeepingMode.getSelectedIndex()) ;
    		System.out.println("We have a new selected value for beeping mode\n");
    	}
    	
    	if (e.getActionCommand().equals("ComboBoxUnit"))
    	{
    		Alticonfig.setUnits(jComboBoxUnit.getSelectedIndex()) ;
    		System.out.println("We have a new selected value for units\n");
    	}
    	
    	if (e.getActionCommand().equals("ComboBoxYesNo"))
    	{
    		Alticonfig.setSupersonicYesNo(jComboBoxYesNo.getSelectedIndex()) ;
    		System.out.println("We have a new selected value for supersonic mode\n");
    	}
    	
    	if (e.getActionCommand().equals("ComboBoxOutputType"))
    	{
    		Alticonfig.setOutput1(jComboBoxOutputType.getSelectedIndex()) ;
    		System.out.println("We have a new selected value for output type\n");
    	}
    	
    	if (e.getActionCommand().equals("ComboBoxOutputType2"))
    	{
    		Alticonfig.setOutput2(jComboBoxOutputType2.getSelectedIndex()) ;
    		System.out.println("We have a new selected value for output type\n");
    	}
    	
    	if (e.getActionCommand().equals("ComboBoxOutputType3"))
    	{
    		Alticonfig.setOutput3(jComboBoxOutputType3.getSelectedIndex()) ;
    		System.out.println("We have a new selected value for output type\n");
    	}
    	
    	
    }
	
}
