package com.example.newrobot;

import android.util.Log;

public class WallFollowingCalculations {
	// final constants for PW's
	public static final int MIDDLEPW = 1550;
	public static final int FASTFORWARDMOTOR = 1425;
	public static final int FORWARDMOTOR = 1455; //1455
	public static final int BACKMOTOR = 1600;
	public static final int FORWARDSTOP = 1550;
	public static final int BACKSTOP = 1550;
	public static final int ACTUALSTOP = 1550;

	// motor values
	double motorPW;

	// wheel values
	int MIDWHEEL = 1400;
	static int WHEELMIN = 1900; //Full left
	static int WHEELMAX = 900; //Full right
	double wheelPW;

	// booleans for current state of the vehicle
	private boolean goingForward = false;
	private boolean turningLeft = false;
	private boolean turningRight = false;
	private boolean followingLeft = false;
	private boolean followingRight = false;
	boolean flippedWheels; //used for backup
	
	//states
	public static final String FOLLOW = "FOLLOW";
	public static final String FIND= "FIND";
	public static final String BACKUP = "BACKUP";
	public static final String TURN = "TURN";
	public static final String STOP= "STOP";
	public static String currentState;

	// instance of the IR calculations
	public IRCalculations irc;

	// Makes check to see where the robot is starting off and sets boolean
	// appropriately
	// currently passed in from ioio thread.
	public WallFollowingCalculations(int startPosition) {
		irc = new IRCalculations();
		motorPW = ACTUALSTOP;
		wheelPW = MIDWHEEL;
		currentState = FIND; //CHANGE TO STOP for no movement
	}

//	public double calculateWheelPW() {
//		double tooFar = 1.4;
//		double tooClose = 2.7;
//		double kindaFar = 1.6;
//		double kindaClose = 2.4;
//		double targetRangeFar = 2.2;
//		double targetRangeClose = 1.7;
//
//		wheelPW = MIDWHEEL;
//		
//		return wheelPW;
//	}
//
//	public double calculateMotorPW() {
//
//		motorPW = FORWARDMOTOR;
//		return motorPW;
//	}

	// utility methods for checking and setting up
	public double[] getServoPW() {
		double[] pws = new double[2];
		pws[0] = motorPW;
		pws[1] = wheelPW;
		return pws;
	}

	public double[] getSetupInfo() {
		double[] info = new double[2];
		info[0] = ACTUALSTOP;
		info[1] = MIDWHEEL;
		return info;
	}

	public void wheelPWCheck() {
		if (wheelPW < WHEELMIN)
			wheelPW = WHEELMIN;
		if (wheelPW > WHEELMAX)
			wheelPW = WHEELMAX;
	}

	// methods should be used to determine what state we should be in
	public class IRCalculations {

		// voltage values from the previous loop
		double frontIRVoltage, leftDiagVoltage, rightIRVoltage, lSideVoltage,
				rSideVoltage;
		
		//front voltage threshold for starting back-up state 
		public static final double FRONTBACKUPVOLTAGE = 1.5;
		public static final double FRONTTURNVOLTAGE = 0.7; //VOLTAGE AT WHICH FRONT SHOULD START TO TURN
		public static final double GOODLEFTVOLTAGE = 1.7; //Voltage at which we want to keep wall following
		public static final double GOODDIAGVOLTAGE = 0.7; // A VOLTAGE THIS SHOULD NEVER BE LESS THAN
		public static final double LEFTDIAGBACKUPVOLTAGE = 2.0;
		public static final double MINWALLVOLTAGE = 0.7;
		public static final double MAXWALLVOLTAGE = 2.7;
		//RANGE FOR WALL FOLLOWING: 0.7 to 2.7
		//TOTAL = 2.0 VOLTS
		//MIDDLE = 1.7
		
		//USED TO CALCULATE WALL FOLLOW CORRECTION
		public static final double lowFollowVoltage = 0.7;
		public static final double highFollowVoltage = 2.7;
		public double pulseWidthDifference = (WHEELMIN - WHEELMAX)/2;
		public double WallFollowVoltageDifference = (highFollowVoltage - lowFollowVoltage)/2; //difference between middle and top/bottom
		public double middleWallFollowVoltage = lowFollowVoltage + WallFollowVoltageDifference;
		
		///calculates the wheel PW for following
		//Voltage: the voltage of the left IR sensor
		double calculateNewWheelPW(double voltage) {
			Log.d("New 1", "" + voltage);
			Log.d("New 2", "" + middleWallFollowVoltage);
			Log.d("New 3", "" + WallFollowVoltageDifference);

			double multiplier = (voltage - middleWallFollowVoltage)/WallFollowVoltageDifference;
			double newPW = (MIDWHEEL) - (multiplier * pulseWidthDifference);
			Log.d("New calculed PW", "" + newPW);
			Log.d("New multiplier", "" + multiplier);
			return newPW;
		}

		public IRCalculations() {
		}

		public void setVoltage(float IRFront, float IRLeft, float IRRight,
				float IRRSide, float IRLSide) {
			frontIRVoltage = IRFront;
			leftDiagVoltage = IRLeft;
			rightIRVoltage = IRRight;
			rSideVoltage = IRRSide;
			lSideVoltage = IRLSide;
		}

		public void checkStates() {
			
			if (currentState == FIND)
				findState();
			if (currentState == TURN)
				turnState();
			if (currentState == BACKUP)
				backupState();
			if (currentState == FOLLOW)
				followState();
			if (currentState == STOP) {
				stopState();
			}
			
		}

		private void followState() {
			motorPW = FORWARDMOTOR;
			if (lSideVoltage < 0.7) {
				currentState = FIND;
				return;
			}
			
			if (frontIRVoltage > FRONTBACKUPVOLTAGE || (leftDiagVoltage > LEFTDIAGBACKUPVOLTAGE)) {
				Log.d("State", "Going from FOLLOW to BACKUP");
				motorPW = FORWARDSTOP;
				currentState = BACKUP;
				return;
			}
			
			if (frontIRVoltage > FRONTTURNVOLTAGE) {
				Log.d("State", "Going from follow to TURN");
				currentState = TURN;
				return;
			}
			
			if (leftDiagVoltage > lSideVoltage) { //new addition, maybe change?
				currentState = TURN;				
				return;
			}
			
			//Steady-state(fix for dynamic steering)
			if (lSideVoltage < GOODLEFTVOLTAGE) {
				wheelPW = calculateNewWheelPW(lSideVoltage);
				Log.d("New wheelPW", "New WheelPW:" + wheelPW);
			}
			if (lSideVoltage > GOODLEFTVOLTAGE) {
				wheelPW = calculateNewWheelPW(lSideVoltage);
				Log.d("New wheelPW", "New WheelPW:" + wheelPW);

			}
			if (lSideVoltage < MINWALLVOLTAGE) {
				currentState = FIND;
				return;
			}
		}

		private void backupState() {
			motorPW = BACKMOTOR;

			if (!flippedWheels) {
				flippedWheels = true;
				// reverses direction
				double difference = MIDWHEEL - wheelPW;
				wheelPW = MIDWHEEL + difference;
				motorPW = BACKSTOP;
				return;
			}
			
			if (frontIRVoltage < 1 && leftDiagVoltage < LEFTDIAGBACKUPVOLTAGE) {
				motorPW = BACKSTOP;
				currentState = FIND;
				flippedWheels = false;
				return;
			}
		}

		private void turnState() {
			wheelPW = WHEELMAX; //turn right 
			motorPW = FASTFORWARDMOTOR;
			
			
			//TOO CLOSE
			if (frontIRVoltage > FRONTBACKUPVOLTAGE || leftDiagVoltage > LEFTDIAGBACKUPVOLTAGE) {
				motorPW = FORWARDSTOP;
				currentState = BACKUP;
				return;
			}
			if (frontIRVoltage < 0.7 && lSideVoltage > 1.0) {
				currentState = FOLLOW;
				return;
			}			
		}
		
		private void stopState() {
			motorPW = ACTUALSTOP;
			wheelPW = MIDWHEEL;
		}

		private void findState() {
			//go forward
			motorPW = FORWARDMOTOR;
			wheelPW = MIDWHEEL;
			
			//|| leftDiagVoltage > 1.3
			
			if (lSideVoltage > MINWALLVOLTAGE) {
				currentState = FOLLOW;
				return;
			}
			
			if (frontIRVoltage > 0.7) {
				currentState = TURN;
				return;
			}
		}
	}
}
