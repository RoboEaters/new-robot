package com.example.aroboeater;
import android.util.Log;
import ioio.lib.api.AnalogInput;
import ioio.lib.api.DigitalOutput;
import ioio.lib.api.IOIO;
import ioio.lib.api.PwmOutput;
import ioio.lib.api.exception.ConnectionLostException;
import ioio.lib.util.BaseIOIOLooper;

public class IOIOThread extends BaseIOIOLooper 
{
		private PwmOutput pwmOutputx;
		private PwmOutput pwmOutputy;
		private PwmOutput motorOutput;
		private PwmOutput wheelOutput;
		private static RoboEaterMain the_gui;
		private static WallFollowingCalculations servos;
		 //600 TO 2500 
		
		boolean goingBackwards;
		
		int lastMotorPW;
		double[] PWs = new double[4];
		
		//IRs
		private AnalogInput IRFront, IRLeft, IRRight, IRRSide, IRLSide, IRBack;

		//passes in a reference to sample2view FROM sample2nativecamera. bad programming?
		public IOIOThread(RoboEaterMain ui)
		{
			the_gui = ui;

			Thread.currentThread().setName("IOIOThread");
			Log.d("IOIOThread", "IOIOThread has been created");

		}
		
		@Override
		public void setup() throws ConnectionLostException 
		{
			try {
				Log.d("IOIOThread", "Trying to finish setup of IOIO");
				double[] info = servos.getSetupInfo();
				double PWx = info[0];
				double PWy = info[1];
				double motorPW = info[2];
				double wheelPW = info[3];
				
				pwmOutputx = ioio_.openPwmOutput(11, 100);
				pwmOutputy = ioio_.openPwmOutput(12, 100);
				motorOutput = ioio_.openPwmOutput(5, 100);
				wheelOutput = ioio_.openPwmOutput(10,100);
				IRFront = ioio_.openAnalogInput(43);
				IRLeft = ioio_.openAnalogInput(42);
				IRRight = ioio_.openAnalogInput(41);
				IRRSide = ioio_.openAnalogInput(40);
				IRLSide = ioio_.openAnalogInput(44);
				
				pwmOutputx.setPulseWidth((int)  PWx);
				pwmOutputy.setPulseWidth((int)  PWy); 
				motorOutput.setPulseWidth((int) motorPW);
				wheelOutput.setPulseWidth((int) wheelPW);
				servos.irc.setVoltage(IRFront.getVoltage(), IRLeft.getVoltage(), IRRight.getVoltage(), IRRSide.getVoltage(), IRLSide.getVoltage());

				}
			catch (Exception e) 
			{
				e.printStackTrace();
			}
			finally
			{
				Log.d("IOIO_Tread", "IOIO thread sucessfully set up");
			}
		}
		
		public void loop() throws ConnectionLostException, InterruptedException
		{	
			//we can calculate the PW values right here in the IOIO loop
			//INSTEAD OF from the controller class
			//MUST BE IN THIS ORDER
			//Still using calculations done based on the tracking of the ball (reliant on mountX and mountY)
			//Need to either change or make new methods.
        	servos.calculateWheelPW();
        	servos.calculateMotorPW();
        	servos.irc.checkStates();
			
			servos.irc.setVoltage(IRFront.getVoltage(), IRLeft.getVoltage(), IRRight.getVoltage(), IRRSide.getVoltage(), IRLSide.getVoltage());
			PWs = servos.getServoPW();
			 int motorPW = (int) PWs[0];
			 int wheelPW = (int) PWs[1];

			wheelOutput.setPulseWidth(wheelPW);

			//for going between backwards and forwards
			//WARNING: MINIMIZE GOING FROM FULL SPEED FORWARD TO FULL SPEED BACKWARDS
			//DOING SO CAN DAMAGE THE GEARS
			if (motorPW <  WallFollowingCalculations.ACTUALSTOP)
				motorOutput.setPulseWidth(WallFollowingCalculations.ACTUALSTOP);
			
			motorOutput.setPulseWidth((int) motorPW);
			lastMotorPW = (int) motorPW;
			
			//values represent all of the newly calculated values done by the ServoCalculation class.
			//All of these values will be transported to the Main activity so that the text fields can be updated
			//in the UI.
			//0:MountX
			//1:MountY
			//2:MotorPW
			//3:WheelPW
			//4:Front IR
			//5:Diag Left IR
			//6:Diag Right IR
			//7:Side Left IR
			//8:Side Right IR
			//9:Back IR
			double[] values = new double[10];
			values[0] = 0;
			values[1] = 0;
			values[2] = motorPW;
			values[3] = wheelPW;
			values[4] = IRFront.getVoltage();
			values[5] = IRLeft.getVoltage();
			values[6] = IRRight.getVoltage();
			values[7] = IRLSide.getVoltage();
			values[8] = IRRSide.getVoltage();
			values[9] = 0;

			//Need to post PW and IR readings back to the GUI Here!!
			the_gui.setTextFields(values);
			
			//determines how fast calculations are done
			Thread.sleep(50);
		}
	}
