
import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.KeyEvent;

import InstrumentationPackage.Indicator;
import InstrumentationPackage.MessageWindow;
import MessagePackage.Message;
import MessagePackage.MessageManagerInterface;
import MessagePackage.MessageQueue;

class SecurityMonitor extends Thread {
	private static MessageManagerInterface em = null;
	private String MsgMgrIP = null;
	private static boolean startSprinkle = true;
	boolean Registered = true;
	MessageWindow mw = null; // This is the message window
	Indicator wi; // Window break indicator
	Indicator di; // Door break indicator
	Indicator mi; // Motion Detection indicator
	Indicator fi; // Fire detection indicator
	SecurityConsole sc;
	public SecurityMonitor() {
		// message manager is on the local system

		try {
			// Here we create an message manager interface object. This assumes
			// that the message manager is on the local machine
			em = new MessageManagerInterface();
		}

		catch (Exception e) {
			System.out.println("SecurityMonitor::Error instantiating message manager interface: " + e);
			Registered = false;

		} // catch

	} // Constructor

	public SecurityMonitor(String MsgIpAddress) {
		// message manager is not on the local system

		MsgMgrIP = MsgIpAddress;

		try {
			// Here we create an message manager interface object. This assumes
			// that the message manager is NOT on the local machine

			em = new MessageManagerInterface(MsgMgrIP);
		}

		catch (Exception e) {
			System.out.println("SecurityMonitor::Error instantiating message manager interface: " + e);
			Registered = false;

		} // catch

	} // Constructor

	public SecurityMonitor(String MsgIpAddress,SecurityConsole instance)
	{
		// message manager is not on the local system

		MsgMgrIP = MsgIpAddress;

		try {
			// Here we create an message manager interface object. This assumes
			// that the message manager is NOT on the local machine

			em = new MessageManagerInterface(MsgMgrIP);
			sc = instance;
		}

		catch (Exception e) {
			System.out.println("SecurityMonitor::Error instantiating message manager interface: " + e);
			Registered = false;

		} // catch

	}//constructor
	
	public SecurityMonitor(SecurityConsole instance)
	{

		try {
			// Here we create an message manager interface object. This assumes
			// that the message manager is NOT on the local machine

			em = new MessageManagerInterface();
			sc = instance;
		}

		catch (Exception e) {
			System.out.println("SecurityMonitor::Error instantiating message manager interface: " + e);
			Registered = false;

		} // catch

	}//constructor
	@Override
	public void run() {
		Message Msg = null; // Message object
		MessageQueue eq = null; // Message Queue
		int MsgId = 0; // User specified message ID
		String SecurityMsg = null; // The Security status
		int Delay = 1000; // The loop delay (1 second)
		boolean Done = false; // Loop termination flag

		if (em != null) {

			mw = new MessageWindow("Security Monitoring Console", 0, 0);
			wi = new Indicator("Window Sensor", mw.GetX() + mw.Width(), (int) (mw.Height() / 2),1);
			di = new Indicator("Door Sensor", mw.GetX() + mw.Width() + (int) (wi.Width() * 2), (int) (mw.Height() / 2),1);
			mi = new Indicator("Motion Sensor", mw.GetX() + mw.Width() + (int) (wi.Width() * 4),
					(int) (mw.Height() / 2), 1);
			fi = new Indicator("Fire Sensor", mw.GetX() + mw.Width() + (int) (wi.Width() * 6), (int) (mw.Height() / 2),1);

			
			
			mw.WriteMessage("Registered with the message manager.");

			try {
				mw.WriteMessage("   Participant id: " + em.GetMyId());
				mw.WriteMessage("   Registration Time: " + em.GetRegistrationTime());

			} // try

			catch (Exception e) {
				System.out.println("Error:: " + e);

			} // catch

			/********************************************************************
			 ** Here we start the main simulation loop
			 *********************************************************************/

			while (!Done) {
				// Here we get our message queue from the message manager

				try {
					eq = em.GetMessageQueue();

				} // try

				catch (Exception e) {
					mw.WriteMessage("Error getting message queue::" + e);

				} // catch

				// If there are messages in the queue, we read through them.
				// We are looking for MessageIDs = 1 or 2 or 3. Message IDs of 1
				// are window break
				// readings from the window sensor; message IDs of 2 are door
				// sensor
				// readings.And messgae IDs of 3 are motion sensor Note that we
				// get all the messages at once... there is a 1
				// second delay between samples,.. so the assumption is that
				// there should
				// only be a message at most.

				int qlen = eq.GetSize();

				for (int i = 0; i < qlen; i++) {
					Msg = eq.GetMessage();

					// Received data from sensors
					if (Msg.GetMessageId() == 30) {
						try {
							SecurityMsg = String.valueOf(Msg.GetMessage());
							// Write message to the message window and set alarm
							// accordingly
							if (SecurityMsg.equalsIgnoreCase("W0")) {
								mw.WriteMessage("Window Status:: Safe");
								PostSecurityAlarmStatus(em, "WA0");
								wi.SetLampColorAndMessage("Window Safe", 1);
							}
							if (SecurityMsg.equalsIgnoreCase("W1")) {
								mw.WriteMessage("Window Status:: Broken");
								PostSecurityAlarmStatus(em, "WA1");
								wi.SetLampColorAndMessage("Window Breaks", 3);
							}
							if (SecurityMsg.equalsIgnoreCase("D0")) {
								mw.WriteMessage("Door Status:: Safe");
								PostSecurityAlarmStatus(em, "DA0");
								di.SetLampColorAndMessage("Door Safe", 1);
							}
							if (SecurityMsg.equalsIgnoreCase("D1")) {
								mw.WriteMessage("Door Status:: Broken");
								PostSecurityAlarmStatus(em, "DA1");
								di.SetLampColorAndMessage("Door Breaks", 3);
							}
							if (SecurityMsg.equalsIgnoreCase("M0")) {
								mw.WriteMessage("Motion Status:: No motion");
								PostSecurityAlarmStatus(em, "MA0");
								mi.SetLampColorAndMessage("No motion", 1);
							}
							if (SecurityMsg.equalsIgnoreCase("M1")) {
								mw.WriteMessage("Motion Status:: Motion detected");
								PostSecurityAlarmStatus(em, "MA1");
								mi.SetLampColorAndMessage("Motion detected", 3);
							}
						} // try

						catch (Exception e) {
							mw.WriteMessage("Error reading window: " + e);

						} // catch

					} // if

					if (Msg.GetMessageId() == -25) {
						try {
							SecurityMsg = String.valueOf(Msg.GetMessage());
							// Write message to the message window and set alarm
							// accordingly
							if (SecurityMsg.equalsIgnoreCase("WB0")) {
								wi.SetLampColorAndMessage("Window Disarmed", 0);
							}
						} catch (Exception e) {
							mw.WriteMessage("Error reading window: " + e);

						} // catch
					}

					if (Msg.GetMessageId() == -26) {
						try {
							SecurityMsg = String.valueOf(Msg.GetMessage());
							// Write message to the message window and set alarm
							// accordingly
							if (SecurityMsg.equalsIgnoreCase("DB0")) {
								di.SetLampColorAndMessage("Door Disarmed", 0);
							}
						} catch (Exception e) {
							mw.WriteMessage("Error reading window: " + e);

						} // catch
					}

					if (Msg.GetMessageId() == -27) {
						try {
							SecurityMsg = String.valueOf(Msg.GetMessage());
							// Write message to the message window and set alarm
							// accordingly
							if (SecurityMsg.equalsIgnoreCase("MB0")) {
								mi.SetLampColorAndMessage("Motion Disarmed", 0);
							}
						} catch (Exception e) {
							mw.WriteMessage("Error reading window: " + e);

						} // catch
					}

					if (Msg.GetMessageId() == 10) {
						try {
							SecurityMsg = String.valueOf(Msg.GetMessage());
							// Write message to the message window and set alarm
							// accordingly
							if (SecurityMsg.equalsIgnoreCase("F0")) {
								mw.WriteMessage("Fire Status:: Safe");
								PostFireAlarmStatus(em, "F0");
								PostSprinklerStatus(em, "F0");
								fi.SetLampColorAndMessage("No Fire", 1);
								startSprinkle = false;
								SecurityConsole.isFire = false;
							}

							if (SecurityMsg.equalsIgnoreCase("F1")) {
//								System.out.println("We are on fire!!");
								startSprinkle = true;
								SecurityConsole.generateAlert();
								mw.WriteMessage("Fire Status:: FIRE");
								PostFireAlarmStatus(em, "F1");
								fi.SetLampColorAndMessage("FIRE", 3);
								
								SecurityConsole.showMessage = true;
								Thread.sleep(10000);
								if (startSprinkle)
								{
									PostSprinklerStatus(em, "F1");
									SecurityConsole.sprinkleStarted = true;
									SecurityConsole.generatePostAlert();
									startSprinkle = false;
									
								}
								
							}

						} catch (Exception e) {
							mw.WriteMessage("Error reading window: " + e);

						} // catch
					}

					// If the message ID == 99 then this is a signal that the
					// simulation
					// is to end. At this point, the loop termination flag is
					// set to
					// true and this process unregisters from the message
					// manager.

					if (Msg.GetMessageId() == 99) {
						Done = true;

						try {
							em.UnRegister();

						} // try

						catch (Exception e) {
							mw.WriteMessage("Error unregistering: " + e);

						} // catch

						mw.WriteMessage("\n\nSimulation Stopped. \n");

						// Get rid of the indicators. The message panel is left
						// for the
						// user to exit so they can see the last message posted.

						wi.dispose();
						di.dispose();
						mi.dispose();
						fi.dispose();

					} // if

				} // for

				// This delay slows down the sample rate to Delay milliseconds

				try {
					Thread.sleep(Delay);

				} // try

				catch (Exception e) {
					System.out.println("Sleep error:: " + e);

				} // catch

			} // while

		} else {

			System.out.println("Unable to register with the message manager.\n\n");

		} // if

	} // main

	/***************************************************************************
	 * CONCRETE METHOD:: IsRegistered Purpose: This method returns the
	 * registered status
	 *
	 * Arguments: none
	 *
	 * Returns: boolean true if registered, false if not registered
	 *
	 * Exceptions: None
	 *
	 ***************************************************************************/

	public boolean IsRegistered() {
		return (Registered);

	} // SetTemperatureRange

	/***************************************************************************
	 * CONCRETE METHOD:: Halt Purpose: This method posts an message that stops
	 * the environmental control system.
	 *
	 * Arguments: none
	 *
	 * Returns: none
	 *
	 * Exceptions: Posting to message manager exception
	 *
	 ***************************************************************************/

	public void Halt() {
		mw.WriteMessage("***HALT MESSAGE RECEIVED - SHUTTING DOWN SYSTEM***");

		// Here we create the stop message.

		Message msg;

		msg = new Message(99, "XXX");

		// Here we send the message to the message manager.

		try {
			em.SendMessage(msg);

		} // try

		catch (Exception e) {
			System.out.println("Error sending halt message:: " + e);

		} // catch

	} // Halt

	public void ArmWindowBreak(boolean status) {

		Message msg = null;

		if (status) {
			msg = new Message(25, "wb1"); // arm

		} else {

			msg = new Message(25, "wb0"); // disarm

		} // if

		// Here we send the message to the message manager.

		try {
			em.SendMessage(msg);

		} // try

		catch (Exception e) {
			System.out.println("Error sending control message:: " + e);

		} // catch

	}

	public void ArmDoorBreak(boolean status) {

		Message msg = null;

		if (status) {
			msg = new Message(26, "db1"); // arm

		} else {

			msg = new Message(26, "db0"); // disarm

		} // if

		// Here we send the message to the message manager.

		try {
			em.SendMessage(msg);

		} // try

		catch (Exception e) {
			System.out.println("Error sending control message:: " + e);

		} // catch

	}

	public void ArmMotionDetection(boolean status) {

		Message msg = null;

		if (status) {
			msg = new Message(27, "md1"); // arm

		} else {

			msg = new Message(27, "md0"); // disarm

		} // if

		// Here we send the message to the message manager.

		try {
			em.SendMessage(msg);

		} // try

		catch (Exception e) {
			System.out.println("Error sending control message:: " + e);

		} // catch

	}

	/**********
	 * Send window, door, and motion simulation data to the sensors
	 ***********/

	public void SendWindowData(int status) {
		// Here we create the message.

		Message msg = null;

		if (status == 1) {
			msg = new Message(25, "wb2"); // window breaks

		} else if (status == 0) {

			msg = new Message(25, "wb3"); // window safe

		} // if

		// Here we send the message to the message manager.

		try {
			em.SendMessage(msg);

		} // try

		catch (Exception e) {
			System.out.println("Error sending control message:: " + e);

		} // catch

	}

	public void SendDoorData(int status) {
		// Here we create the message.

		Message msg = null;

		if (status == 1) {
			msg = new Message(26, "db2"); // door breaks

		} else if (status == 0) {

			msg = new Message(26, "db3"); // door safe

		} // if

		// Here we send the message to the message manager.

		try {
			em.SendMessage(msg);

		} // try

		catch (Exception e) {
			System.out.println("Error sending control message:: " + e);

		} // catch

	}

	public void SendMotionData(int status) {
		// Here we create the message.

		Message msg = null;
		if (status == 1) {
			msg = new Message(27, "md2"); // motion detected

		} else if (status == 0) {

			msg = new Message(27, "md3"); // no motion

		} // if

		// Here we send the message to the message manager.

		try {
			em.SendMessage(msg);

		} // try

		catch (Exception e) {
			System.out.println("Error sending control message::  " + e);

		} // catch

	}

	public void SendFireData(int status) {
		// Here we create the message.

		Message msg = null;
		if (status == 1) {
			msg = new Message(10, "F1"); // FIRE

		} else if (status == 0) {

			msg = new Message(10, "F0"); // no fire

		} // if

		// Here we send the message to the message manager.

		try {
			em.SendMessage(msg);

		} // try

		catch (Exception e) {
			System.out.println("Error sending control message::  " + e);

		} // catch

	}

	/***************************************************************************
	 * CONCRETE METHOD:: PostWindowState Purpose: This method posts the
	 * specified temperature value to the specified message manager. This method
	 * assumes an message ID of 1.
	 *
	 * Arguments: MessageManagerInterface ei - this is the messagemanger
	 * interface where the message will be posted.
	 *
	 * boolean state - this is the state of the window.
	 *
	 * Returns: none
	 *
	 * Exceptions: None
	 *
	 ***************************************************************************/

	static private void PostSecurityAlarmStatus(MessageManagerInterface ei, String State) {
		// Here we create the message.
		Message msg = new Message((int) 35, State);

		// Here we send the message to the message manager.

		try {
			ei.SendMessage(msg);
			// System.out.println( "Sent Window Message" );

		} // try

		catch (Exception e) {
			System.out.println("Error Posting Window state:: " + e);

		} // catch

	} // PostWindowState

	/***************************************************************************
	 * CONCRETE METHOD:: PostFireAlarmStatus Purpose: This method posts the
	 * specified temperature value to the specified message manager. This method
	 * assumes an message ID of 1.
	 *
	 * Arguments: MessageManagerInterface ei - this is the messagemanger
	 * interface where the message will be posted.
	 *
	 * boolean state - this is the state of the window.
	 *
	 * Returns: none
	 *
	 * Exceptions: None
	 *
	 ***************************************************************************/

	static private void PostFireAlarmStatus(MessageManagerInterface ei, String State) {
		// Here we create the message.
		Message msg = new Message((int) 11, State);

		// Here we send the message to the message manager.

		try {
			ei.SendMessage(msg);
			// System.out.println( "Sent Window Message" );

		} // try

		catch (Exception e) {
			System.out.println("Error Posting Window state:: " + e);

		} // catch

	} // PostFireState

	static public void changeSprinkleState(boolean state, String first) {
		startSprinkle = state;
		if (first==null) {
			if (startSprinkle)
			{
				SecurityConsole.sprinkleStarted = true;
				PostSprinklerStatus(em, "F1");
			}
			else
			{
				SecurityConsole.sprinkleStarted = false;
				PostSprinklerStatus(em, "F0");
			}
		}
	}

	/***************************************************************************
	 * CONCRETE METHOD:: PostFireAlarmStatus Purpose: This method posts the
	 * specified temperature value to the specified message manager. This method
	 * assumes an message ID of 1.
	 *
	 * Arguments: MessageManagerInterface ei - this is the messagemanger
	 * interface where the message will be posted.
	 *
	 * boolean state - this is the state of the window.
	 *
	 * Returns: none
	 *
	 * Exceptions: None
	 *
	 ***************************************************************************/

	static private void PostSprinklerStatus(MessageManagerInterface ei, String State) {
		// Here we create the message.
		Message msg = new Message((int) 12, State);

		// Here we send the message to the message manager.

		try {
			ei.SendMessage(msg);
			// System.out.println( "Sent Window Message" );

		} // try

		catch (Exception e) {
			System.out.println("Error Posting Window state:: " + e);

		} // catch

	} // PostFireState

}