/******************************************************************************************************************
* File:DoorSensor.java
* Course: 17655
* Project: Assignment A3
* Copyright: Copyright (c) 2009 Carnegie Mellon University
* Versions:
*	1.0 March 2009 - Initial rewrite of original assignment 3 (ajl).
*
* Description:
*
* This class simulates a door sensor. This class gets the action from simulation monitor, which gets the direct input
* user by simulation console.
*
* Parameters: IP address of the message manager (on command line). If blank, it is assumed that the message manager is
* on the local machine.
*
* Internal Methods:
*   PostDoorState(MessageManagerInterface ei, boolean State)
*
******************************************************************************************************************/
import InstrumentationPackage.*;
import MessagePackage.*;

class DoorSensor
{
	public static void main(String args[])
	{
		String MsgMgrIP;				// Message Manager IP address
		Message Msg = null;				// Message object
		MessageQueue eq = null;			// Message Queue
		MessageManagerInterface em = null;// Interface object to the message manager
		boolean DoorState = false;	// Door state: false == safe, true == broken
		boolean SensorState = true;     // Door state: true == armed, false == disarmed
		int	Delay = 2500;				// The loop delay (2.5 seconds)
		boolean Done = false;			// Loop termination flag

		/////////////////////////////////////////////////////////////////////////////////
		// Get the IP address of the message manager
		/////////////////////////////////////////////////////////////////////////////////

 		if ( args.length == 0 )
 		{
			// message manager is on the local system

			System.out.println("\n\nAttempting to register on the local machine..." );

			try
			{
				// Here we create an message manager interface object. This assumes
				// that the message manager is on the local machine

				em = new MessageManagerInterface();
			}

			catch (Exception e)
			{
				System.out.println("Error instantiating message manager interface: " + e);

			} // catch

		} else {

			// message manager is not on the local system

			MsgMgrIP = args[0];

			System.out.println("\n\nAttempting to register on the machine:: " + MsgMgrIP );

			try
			{
				// Here we create an message manager interface object. This assumes
				// that the message manager is NOT on the local machine

				em = new MessageManagerInterface( MsgMgrIP );
			}

			catch (Exception e)
			{
				System.out.println("Error instantiating message manager interface: " + e);

			} // catch

		} // if

		// Here we check to see if registration worked. If ef is null then the
		// message manager interface was not properly created.


		if (em != null)
		{

			// We create a message window. Note that we place this panel about 1/2 across
			// and 1/3 down the screen

			float WinPosX = 0.5f; 	//This is the X position of the message window in terms
								 	//of a percentage of the screen height
			float WinPosY = 0.3f; 	//This is the Y position of the message window in terms
								 	//of a percentage of the screen height

			MessageWindow mw = new MessageWindow("Door Sensor", WinPosX, WinPosY );

			mw.WriteMessage("Registered with the message manager." );

	    	try
	    	{
				mw.WriteMessage("   Participant id: " + em.GetMyId() );
				mw.WriteMessage("   Registration Time: " + em.GetRegistrationTime() );

			} // try

	    	catch (Exception e)
			{
				mw.WriteMessage("Error:: " + e);

			} // catch

			mw.WriteMessage("\nInitializing Sensor Simulation::" );

			/********************************************************************
			** Here we start the main simulation loop
			*********************************************************************/

			mw.WriteMessage("Beginning Simulation... ");


			while ( !Done )
			{
				// Post the current window state only if the sensor is armed
				if (SensorState)
				{
					PostDoorState(em, DoorState);
					if (DoorState)
					{
						mw.WriteMessage("Current status::  Door(s) broke");
					} else
					{
						mw.WriteMessage("Current status::  All doors are safe");
					}
				}
				
				// Get the message queue

				try
				{
					eq = em.GetMessageQueue();

				} // try

				catch( Exception e )
				{
					mw.WriteMessage("Error getting message queue::" + e );

				} // catch

				// If there are messages in the queue, we read through them.
				// We are looking for MessageIDs = 25, this means the the sensor
				// has been armed/disarmed. Note that we get all the messages
				// at once... there is a 2.5 second delay between samples,.. so
				// the assumption is that there should only be a message at most.
				// If there are more, it is the last message that will effect the
				// output of the window sensor as it would in reality.

				int qlen = eq.GetSize();

				for ( int i = 0; i < qlen; i++ )
				{
					Msg = eq.GetMessage();

					if ( Msg.GetMessageId() == 26 )
					{
						if (Msg.GetMessage().equalsIgnoreCase("db1")) // Sensor is armed
						{
							SensorState = true;
							mw.WriteMessage("Door sensor is now armed... ");

						} // if

						if (Msg.GetMessage().equalsIgnoreCase("db0")) // Sensor is disarmed
						{
							SensorState = false;
							mw.WriteMessage("Door sensor is now disarmed... ");
							SendSensorStateConfirmation(em,"db0");

						} 				
						
						
						if (Msg.GetMessage().equalsIgnoreCase("db2") && SensorState) // Sensor is armed and door(s) broke
						{
							DoorState = true;

						} // if
						
						if (Msg.GetMessage().equalsIgnoreCase("db3") && SensorState) // Sensor is armed and doors are safe
						{
							DoorState = false;

						} // if

					} // if
					
					// If the message ID == 99 then this is a signal that the simulation
					// is to end. At this point, the loop termination flag is set to
					// true and this process unregisters from the message manager.

					if ( Msg.GetMessageId() == 99 )
					{
						Done = true;

						try
						{
							em.UnRegister();

				    	} // try

				    	catch (Exception e)
				    	{
							mw.WriteMessage("Error unregistering: " + e);

				    	} // catch

				    	mw.WriteMessage("\n\nSimulation Stopped. \n");

					} // if

				} // for


				// Here we wait for a 2.5 seconds before we start the next sample

				try
				{
					Thread.sleep( Delay );

				} // try

				catch( Exception e )
				{
					mw.WriteMessage("Sleep error:: " + e );

				} // catch

			} // while

		} else {

			System.out.println("Unable to register with the message manager.\n\n" );

		} // if

	} // main

	/***************************************************************************
	* CONCRETE METHOD:: PostWindowState
	* Purpose: This method posts the specified temperature value to the
	* specified message manager. This method assumes an message ID of 1.
	*
	* Arguments: MessageManagerInterface ei - this is the messagemanger interface
	*			 where the message will be posted.
	*
	*			 boolean state - this is the state of the door.
	*
	* Returns: none
	*
	* Exceptions: None
	*
	***************************************************************************/

	static private void PostDoorState(MessageManagerInterface ei, boolean State)
	{
		// Here we create the message.
		Message msg = null;
		if (State)
		{
			msg = new Message( (int) 30, "D1" );
		}
		if (!State)
		{
			msg = new Message( (int) 30, "D0" );
		}
		// Here we send the message to the message manager.

		try
		{
			ei.SendMessage( msg );
			//System.out.println( "Sent Door Message" );

		} // try

		catch (Exception e)
		{
			System.out.println( "Error Posting Door state:: " + e );

		} // catch

	} // PostDoorState
	
	static private void SendSensorStateConfirmation(MessageManagerInterface ei, String State)
	{
		// Here we create the message.
			Message msg = new Message( (int) -26, State );
		// Here we send the message to the message manager.

		try
		{
			ei.SendMessage( msg );
			//System.out.println( "Sent Window Message" );

		} // try

		catch (Exception e)
		{
			System.out.println( "Error Sending Door Alarm Confirmation:: " + e );

		} // catch

	} // SendSensorStateConfirmation

} // DoorSensor