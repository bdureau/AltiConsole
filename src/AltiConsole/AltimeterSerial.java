package AltiConsole;

//This class:
// - Starts up the communication with the altimeter.
// - Reads the data coming in from the altimeter and
//   converts that data in to a useful form.
// - Closes communication with the altimeter.

//Load Libraries
import java.awt.Color;
import java.io.*;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.TooManyListenersException;

import l10n.Translator;
import AltiConfig.AltiConfigData;
//Load RXTX Library
import gnu.io.*;

public class AltimeterSerial implements SerialPortEventListener {
	// passed from main GUI
	AltiConsoleMainScreen window = null;

	// ////////////////////////////////////
	// variable to return the result of the command
	// ////////////////////////////////////

	// Store number of flight
	public int NbrOfFlight = 0;
	public int currentFlightNbr = 0;
	public FlightData MyFlight = null;
	public AltiConfigData AltiCfg = null;
	private static boolean DataReady = false;
	public long lastReceived=0;
	public String commandRet="";
	//private String AltiUnit ="Meters";
	private double FEET_IN_METER =1;

	// ///////////////
	// for containing the ports that will be found
	private Enumeration ports = null;
	// map the port names to CommPortIdentifiers
	private HashMap portMap = new HashMap();

	// this is the object that contains the opened port
	private CommPortIdentifier selectedPortIdentifier = null;
	private SerialPort serialPort = null;

	// input and output streams for sending and receiving data
	private InputStream input = null;
	private OutputStream output = null;

	// just a boolean flag that i use for enabling
	// and disabling buttons depending on whether the program
	// is connected to a serial port or not
	private boolean bConnected = false;

	// the timeout value for connecting with the port
	final static int TIMEOUT = 2000;

	// a string for recording what goes on in the program
	// this string is written to the GUI
	String logText = "";
	
	public AltimeterSerial(AltiConsoleMainScreen window) {
		this.window = window;
	}

	public static void setDataReady(boolean value)
	{
		if (value)
			System.out.println("Changed to true");
		else 
			System.out.println("Changed to false");
		DataReady = value;
	}
	// we'll often want to remove junk from the input buffer before exchanging
		// data with the logger. This method does just that.
	public void clearInput() {
			try { input.skip(input.available()); } catch (IOException e) {}
		}
	
	public static boolean getDataReady()
	{
		//boolean value;
		//System.out.println("call get dataready\n");
		if (DataReady)
			System.out.println("Show true\n");
		/*else 
			System.out.println("false");*/
		return DataReady ;
	}
	// search for all the serial ports
	// pre: none
	// post: adds all the found ports to a combo box on the GUI
	public void searchForPorts() {
		window.comPorts.removeAllItems();
		ports = CommPortIdentifier.getPortIdentifiers();

		while (ports.hasMoreElements()) {
			CommPortIdentifier curPort = (CommPortIdentifier) ports
					.nextElement();

			window.comPorts.addItem(curPort.getName());
			portMap.put(curPort.getName(), curPort);

		}
	}

	public void initFlightData(String pAltiUnit) {
		MyFlight = new FlightData();
		System.out.println(pAltiUnit);
		if(pAltiUnit.equals( "Unit.Metrics"))
		{
			System.out.println("Unit = meters\n");
		 FEET_IN_METER =1;
		}
		else
		{
			FEET_IN_METER = 3.28084;
			System.out.println("Unit = feet\n");
		}
		
		System.out.println(pAltiUnit);
	}

	public void connect() {
		String selectedPort = (String) window.comPorts.getSelectedItem();
		selectedPortIdentifier = (CommPortIdentifier) portMap.get(selectedPort);
		System.out.println("connecting ....\n");
		CommPort commPort = null;
		System.out.println("Port:" + selectedPort);
		AltiCfg = new AltiConfigData();
		try {
			// the method below returns an object of type CommPort
			commPort = selectedPortIdentifier
					.open("TigerControlPanel", TIMEOUT);
			// the CommPort object can be casted to a SerialPort object
			serialPort = (SerialPort) commPort;
			System.out.println("Speed: "
					+ (String) window.serialRates.getSelectedItem());
			serialPort.setSerialPortParams(Integer
					.parseInt((String) window.serialRates.getSelectedItem()),
					serialPort.DATABITS_8, serialPort.STOPBITS_1,
					serialPort.PARITY_NONE);

			// for controlling GUI elements
			setConnected(true);

			// logging
			logText = selectedPort + " opened successfully.";
			System.out.println(selectedPort + " opened successfully.");
			window.txtLog.setForeground(Color.black);
			window.txtLog.append(logText + "\n");

		} catch (PortInUseException e) {
			logText = selectedPort + " is in use. (" + e.toString() + ")";

			window.txtLog.setForeground(Color.RED);
			window.txtLog.append(logText + "\n");
		} catch (Exception e) {
			logText = "Failed to open " + selectedPort + "(" + e.toString()
					+ ")";
			window.txtLog.append(logText + "\n");
			window.txtLog.setForeground(Color.RED);
		}
	}

	// open the input and output streams
	// pre: an open port
	// post: initialized input and output streams for use to communicate data
	public boolean initIOStream() {
		// return value for weither opening the streams is successful or not
		boolean successful = false;

		try {

			input = serialPort.getInputStream();
			output = serialPort.getOutputStream();

			// (new Thread(new SerialWriter(output))).start();
			//writeData(0, 0);

			successful = true;
			return successful;
		} catch (IOException e) {
			logText = "I/O Streams failed to open. (" + e.toString() + ")";
			window.txtLog.setForeground(Color.red);
			window.txtLog.append(logText + "\n");
			return successful;
		}
	}

	// starts the event listener that knows whenever data is available to be
	// read
	// pre: an open serial port
	// post: an event listener for the serial port that knows when data is
	// recieved
	public boolean initListener() {
		// return value for whether opening the initListener is successful or
		// not
		boolean successful = false;
		try {
			serialPort.addEventListener(this);
			serialPort.notifyOnDataAvailable(true);
			successful = true;
			return successful;
		} catch (TooManyListenersException e) {
			logText = "Too many listeners. (" + e.toString() + ")";
			window.txtLog.setForeground(Color.red);
			window.txtLog.append(logText + "\n");
			return successful;
		}
	}

	// disconnect the serial port
	// pre: an open serial port
	// post: closed serial port
	public void disconnect() {
		class CloseThread extends Thread {
			public void run() {
				serialPort.removeEventListener();
				serialPort.close();
			}
		}
		// close the serial port
		try {
			writeData(0, 0);

			System.out.println("write data ok\n");

			output.flush();

			input.close();
			System.out.println("input closed\n");
			output.close();
			System.out.println("output closed\n");

			serialPort.removeEventListener();
			System.out.println("Listener removed\n");

			serialPort.close();
			System.out.println("port closed\n");
			serialPort = null;
			AltiCfg = null;

			setConnected(false);

			System.out.println("Sucessfully disconnected\n");
			logText = "Disconnected.";
			window.txtLog.setForeground(Color.red);
			window.txtLog.append(logText + "\n");
		} catch (Exception e) {
			System.out.println("Error disconnected\n");
			logText = "Failed to close " + serialPort.getName() + "("
					+ e.toString() + ")";
			window.txtLog.setForeground(Color.red);
			window.txtLog.append(logText + "\n");
		}
	}

	final public boolean getConnected() {
		return bConnected;
	}

	public void setConnected(boolean bConnected) {
		this.bConnected = bConnected;
	}

	// method that can be called to send data
	// pre: open serial port
	// post: data sent to the other device
	public void writeData(int leftThrottle, int rightThrottle) {
		try {
			output.write(leftThrottle);

			output.flush();

			output.write(rightThrottle);
			output.flush();

		} catch (Exception e) {
			logText = "Failed to write data. (" + e.toString() + ")";
			window.txtLog.setForeground(Color.red);
			window.txtLog.append(logText + "\n");
		}
	}

	public void writeData(String command) {
		try {

			window.txtLog.append("trying new func\n");

			window.txtLog.append(command);
			System.out.print(command);
			output.write(command.getBytes());
			output.flush();

		} catch (Exception e) {
			logText = "Failed to write data. (" + e.toString() + ")";
			window.txtLog.setForeground(Color.red);
			window.txtLog.append(logText + "\n");
		}
	}

	public Sentence readSentence(String tempBuff) {
		// we have a sentence let's find out what it is
		String tempArray[] = tempBuff.split(",");
		Sentence sentence = new Sentence();
		
		System.out.println("read temp array " +tempArray[0]+"#\n");
		sentence.keyword = tempArray[0];

		if (tempArray.length > 1)
			sentence.value1 = Integer.parseInt(tempArray[1]);

		if (tempArray.length > 2)
			sentence.value2 = Integer.parseInt(tempArray[2]);

		if (tempArray.length > 3)
			sentence.value3 = Integer.parseInt(tempArray[3]);
		if (tempArray.length > 4)
			sentence.value4 = Integer.parseInt(tempArray[4]);
		if (tempArray.length > 5)
			sentence.value5 = Integer.parseInt(tempArray[5]);
		if (tempArray.length > 6)
			sentence.value6 = Integer.parseInt(tempArray[6]);
		if (tempArray.length > 7)
			sentence.value7 = Integer.parseInt(tempArray[7]);
		if (tempArray.length > 8)
			sentence.value8 = tempArray[8];
		if (tempArray.length > 9)
			sentence.value9 = Integer.parseInt(tempArray[9]);
		if (tempArray.length > 10)
			sentence.value10 = Integer.parseInt(tempArray[10]);
		if (tempArray.length > 11)
			sentence.value11= Integer.parseInt(tempArray[11]);
		if (tempArray.length > 12)
			sentence.value12=Integer.parseInt(tempArray[12]);
		if (tempArray.length > 13)
			sentence.value13=Integer.parseInt(tempArray[13]);
		if (tempArray.length > 14)
			sentence.value14=Integer.parseInt(tempArray[14]);
		return sentence;
	}

	// Reads the incoming data packets from altimeter.
	public void serialEvent(SerialPortEvent event) {

		// Reads in data while data is available
		// while (event.getEventType()== SerialPortEvent.DATA_AVAILABLE )
		if (event.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
			//DataReady = false;
			lastReceived = System.currentTimeMillis();
			try {
				while (input.available() > 0) {

					// Read in the available character
					char ch = (char) input.read();
					System.out.print("#"+ch+"#");
					if (ch == '$') 
					{

						// read entire sentence until the end
						String tempBuff = "";
						while (ch != ';') 
						{
							// this is not the end of our command
							ch = (char) input.read();
							if (ch != '\r')
							if (ch != '\n')
								if (ch != ';')
									tempBuff = tempBuff
											+ Character.toString(ch);
						}
						if (ch == ';')
							//System.out.println("warning\n");
							ch = (char) input.read();

						Sentence currentSentence = null;
						if (!tempBuff.isEmpty()) {
							System.out.println(tempBuff);
							currentSentence = readSentence(tempBuff);
							System.out.println(currentSentence.keyword);
						}
						else 
						{
							System.out.println("Empty buffer\n");
						}
						if (currentSentence.keyword.equals("OK"))
							System.out.println("my keyword: " + currentSentence.keyword );
						else
							System.out.println("my keyword: " + currentSentence.keyword +"#");
						
						switch (currentSentence.keyword) {
						case "data":
							// System.out.println(tempBuff);
							// Value 1 contain the flight number
							currentFlightNbr = (int) currentSentence.value1+1;
							// Value 2 contain the time
							// Value 3 contain the altitude
							MyFlight.AddToFlight(currentSentence.value2,
									(long )(currentSentence.value3*FEET_IN_METER), "Flight "
											+ currentFlightNbr);
							System.out.println("Flight " + currentFlightNbr);
							System.out.println("X=" + currentSentence.value2);
							System.out.println("Y=" + currentSentence.value3);
							break;
						case "alticonfig":
							System.out.println("Receiving alti config\n");
							System.out.println(tempBuff);
							// Value 1 contain the units
							AltiCfg.setUnits((int) currentSentence.value1);
							System.out.println(AltiCfg.getUnits());
							// Value 2 contain beepingMode
							AltiCfg.setBeepingMode((int) currentSentence.value2);
							System.out.println(AltiCfg.getBeepingMode());
							// Value 3 contain output1
							AltiCfg.setOutput1((int) currentSentence.value3);
							System.out.println(AltiCfg.getOutput1());
							// Value 4 contain output2
							AltiCfg.setOutput2((int) currentSentence.value4);
							System.out.println(AltiCfg.getOutput2());
							// Value 5 contain output3
							AltiCfg.setOutput3((int) currentSentence.value5);
							System.out.println(AltiCfg.getOutput3());
							// Value 6 contain supersonicYesNo
							AltiCfg.setSupersonicYesNo((int) currentSentence.value6);
							//AltiCfg.setSupersonicYesNo(0);
							System.out.println(AltiCfg.getSupersonicYesNo());
							// Value 7 contain mainAltitude
							AltiCfg.setMainAltitude((int) currentSentence.value7);
							System.out.println(AltiCfg.getMainAltitude());
							// Value 8 contain AltimeterName
							AltiCfg.setAltimeterName(currentSentence.value8);
							System.out.println(AltiCfg.getAltimeterName());
							// Value 9 contain the altimeter major version
							AltiCfg.setAltiMajorVersion((int)currentSentence.value9);
							System.out.println(AltiCfg.getAltiMajorVersion());
							
							// Value 10 contain the altimeter minor version
							AltiCfg.setAltiMinorVersion((int)currentSentence.value10);
							System.out.println(AltiCfg.getAltiMinorVersion());
							AltiCfg.setOutput1Delay((int)currentSentence.value11);
							
							AltiCfg.setOutput2Delay((int)currentSentence.value12);
							AltiCfg.setOutput3Delay((int)currentSentence.value13);
							AltiCfg.setBeepingFrequency((int)currentSentence.value14);
							//DataReady = true;
							//System.out.println("DataReady is true\n");
							System.out.println("alti config\n");
							break;
						case "nbrOfFlight":
							// Value 1 contains the number of flight
							NbrOfFlight = (int) currentSentence.value1;
							System.out.println("DataReady is ???\n");
							break;
						case "start":
							// We are starting reading data
							setDataReady(false);
							System.out.println("DataReady is false\n");
							break;
						case "end":
							// We have finished reading data
							setDataReady(true);
							System.out.println("DataReady is true\n");
							break;
						case "OK":
							setDataReady(true);
							commandRet = currentSentence.keyword;
							System.out.println("DataReady is true\n");
							break;
						case "KO":
							setDataReady(true);
							commandRet = currentSentence.keyword;
							System.out.println("DataReady is true\n");
							break;
						case "UNKNOWN":
							setDataReady(true);
							commandRet = currentSentence.keyword;
							System.out.println("DataReady is true\n");
							break;
						default:
							System.out.println("?????????\n");
						}
						/*if (currentSentence.keyword.equals("data")) 
						{

							// System.out.println(tempBuff);
							// Value 1 contain the flight number
							currentFlightNbr = (int) currentSentence.value1;
							// Value 2 contain the time
							// Value 3 contain the altitude
							MyFlight.AddToFlight(currentSentence.value2,
									(long )(currentSentence.value3*FEET_IN_METER), "Flight "
											+ currentFlightNbr);
							System.out.println("Flight " + currentFlightNbr);
							System.out.println("X=" + currentSentence.value2);
							System.out.println("Y=" + currentSentence.value3);
							
						} else if (currentSentence.keyword.equals("alticonfig")) {

							System.out.println("Receiving alti config\n");
							System.out.println(tempBuff);
							// Value 1 contain the units
							AltiCfg.setUnits((int) currentSentence.value1);
							System.out.println(AltiCfg.getUnits());
							// Value 2 contain beepingMode
							AltiCfg.setBeepingMode((int) currentSentence.value2);
							System.out.println(AltiCfg.getBeepingMode());
							// Value 3 contain output1
							AltiCfg.setOutput1((int) currentSentence.value3);
							System.out.println(AltiCfg.getOutput1());
							// Value 4 contain output2
							AltiCfg.setOutput2((int) currentSentence.value4);
							System.out.println(AltiCfg.getOutput2());
							// Value 5 contain output3
							AltiCfg.setOutput3((int) currentSentence.value5);
							System.out.println(AltiCfg.getOutput3());
							// Value 6 contain supersonicYesNo
							AltiCfg.setSupersonicYesNo((int) currentSentence.value6);
							//AltiCfg.setSupersonicYesNo(0);
							System.out.println(AltiCfg.getSupersonicYesNo());
							// Value 7 contain mainAltitude
							AltiCfg.setMainAltitude((int) currentSentence.value7);
							System.out.println(AltiCfg.getMainAltitude());
							// Value 8 contain AltimeterName
							AltiCfg.setAltimeterName(currentSentence.value8);
							System.out.println(AltiCfg.getAltimeterName());
							// Value 9 contain the altimeter major version
							AltiCfg.setAltiMajorVersion((int)currentSentence.value9);
							System.out.println(AltiCfg.getAltiMajorVersion());
							
							// Value 10 contain the altimeter minor version
							AltiCfg.setAltiMinorVersion((int)currentSentence.value10);
							System.out.println(AltiCfg.getAltiMinorVersion());
							AltiCfg.setOutput1Delay((int)currentSentence.value11);
							
							AltiCfg.setOutput2Delay((int)currentSentence.value12);
							AltiCfg.setOutput3Delay((int)currentSentence.value13);
							AltiCfg.setBeepingFrequency((int)currentSentence.value14);
							//DataReady = true;
							//System.out.println("DataReady is true\n");
							System.out.println("alti config\n");
							// AltiCfg
						} else if (currentSentence.keyword.equals("nbrOfFlight")) {
							// Value 1 contains the number of flight
							NbrOfFlight = (int) currentSentence.value1;
							System.out.println("DataReady is ???\n");
						} else if (currentSentence.keyword.equals("start")) {
							// We are starting reading data
							DataReady = false;
							System.out.println("DataReady is false\n");
						} else if (currentSentence.keyword.equals("end")) {
							// We have finished reading data
							DataReady = true;
							System.out.println("DataReady is true\n");
						} else if (currentSentence.keyword.equals("OK")) {
							DataReady = true;
							commandRet = currentSentence.keyword;
						} else if (currentSentence.keyword.equals("KO")) {
							DataReady = true;
							commandRet = currentSentence.keyword;	
							System.out.println("DataReady is true\n");
						} else if (currentSentence.keyword.equals("UNKNOWN")) {
							DataReady = true;
							commandRet = currentSentence.keyword;
							System.out.println("DataReady is true\n");
						}
						else
						{
							System.out.println("????\n");
						}*/
					}
				}
			} catch (IOException e) {
			}

		}
	}

}

class Sentence {

	public String keyword;
	public long value1;
	public long value2;
	public long value3;
	public long value4;
	public long value5;
	public long value6;
	public long value7;
	public String value8;
	public long value9;
	public long value10;
	public long value11;
	public long value12;
	public long value13;
	public long value14;
}
