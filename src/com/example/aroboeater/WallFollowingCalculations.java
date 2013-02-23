package com.example.newrobot;

import android.util.Log;

public class WallFollowingCalculations {
	// final constants for PW's
	public static final int MIDDLEPW = 1550;
	public static final int FASTFORWARDMOTOR = 1425;
	public static final int FORWARDMOTOR = 1550; // 1455
	public static final int BACKMOTOR = 1600;
	public static final int FORWARDSTOP = 1550;
	public static final int BACKSTOP = 1550;
	public static final int ACTUALSTOP = 1550;

	// motor values
	double motorPW;

	// wheel values
	int MIDWHEEL = 1400;
	int WHEELMIN = 1900;
	int WHEELMAX = 900;
	double wheelPW;

	// booleans for current state of the vehicle
	private boolean goingForward = false;
	private boolean turningLeft = false;
	private boolean turningRight = false;
	private boolean followingLeft = false;
	private boolean followingRight = false;

	// instance of the IR calculations
	public IRCalculations irc;

	// Makes check to see where the robot is starting off and sets boolean
	// appropriately
	// currently passed in from ioio thread.
	public WallFollowingCalculations(int startPosition) {

		// set boolean according to passed value
		if (startPosition == 0) {
			goingForward = true;
		} else if (startPosition == 1) {
			followingLeft = true;
		} else {
			followingRight = true;
		}
		motorPW = ACTUALSTOP;
		wheelPW = MIDWHEEL;
		irc = new IRCalculations();
	}

	public double calculateWheelPW() {
		double tooFar = 1.4;
		double tooClose = 2.7;
		double kindaFar = 1.6;
		double kindaClose = 2.4;
		double targetRangeFar = 2.2;
		double targetRangeClose = 1.7;

		// use IR Sensor values to determine wheel position.
		// relative to how far away we are from the wall we are following.
		if (goingForward) {
			wheelPW = MIDWHEEL;
		}
		// else if (followingRight) {
		// double frontValue = irc.frontIRVoltage;
		// double rValue = irc.rSideVoltage;
		//
		// //robot is too close to the wall, back up straight and a little to
		// the right
		// if(frontValue > targetRangeClose)
		// {
		// wheelPW = MIDWHEEL + 200;
		// }
		//
		// // in targetRange
		// else if (rValue >= targetRangeFar && rValue <= targetRangeClose) {
		// wheelPW = MIDWHEEL;
		// }
		// // drift away from wall
		// else if (rValue > targetRangeClose) {
		// // hard coded slight left turn
		// wheelPW = MIDWHEEL - 300;
		//
		// if (rValue > tooClose) {
		// wheelPW = MIDWHEEL - 400;
		// }
		// }
		// // drift towards the wall
		// else if (rValue < targetRangeFar) {
		// // hard coded slight right turn
		// wheelPW = MIDWHEEL + 300;
		//
		// if (rValue < tooFar) {
		// wheelPW = MIDWHEEL + 400;
		// }
		// }
		// }
		else if (followingLeft) {
			double frontValue = irc.frontIRVoltage;
			double lValue = irc.lSideVoltage;

			// //robot is too close to the wall, back up straight and a little
			// to the left
			// if(frontValue > targetRangeClose)
			// {
			// wheelPW = MIDWHEEL - 200;
			// }

			// in targetRange
			if (lValue <= targetRangeFar && lValue >= targetRangeClose) {
				wheelPW = MIDWHEEL;
			}
			// drift away from wall
			else if (lValue > targetRangeClose) {
				// slight right turn
				wheelPW = MIDWHEEL - 300;

				// if (lValue > tooClose) {
				// wheelPW = MIDWHEEL + 400;
				// }
				// else if(lValue > kindaClose){
				// wheelPW = MIDWHEEL + 300;
				// }
			}
			// drift towards the wall
			else if (lValue < targetRangeFar) {
				// slight left turn
				wheelPW = MIDWHEEL + 200;

				// if (lValue < tooFar) {
				// wheelPW = MIDWHEEL - 400;
				// }
				// else if(lValue < kindaFar)
				// {
				// wheelPW = MIDWHEEL - 300;
				// }
			}

		}

		return wheelPW;
	}

	public double calculateMotorPW() {
		double frontValue = irc.frontIRVoltage;
		double lDiagValue = irc.leftIRVoltage;
		double lRightValue = irc.rightIRVoltage;

		if (goingForward || followingLeft || followingRight) {
			motorPW = FORWARDMOTOR;
		}

		// HARD CODED FRONT IR CHECK FOR BACKING UP
		else if (frontValue > 2.0) {
			motorPW = ACTUALSTOP;
		}

		return motorPW;
	}

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
		double frontIRVoltage, leftIRVoltage, rightIRVoltage, lSideVoltage,
				rSideVoltage;

		public IRCalculations() {
		}

		public void setVoltage(float IRFront, float IRLeft, float IRRight,
				float IRRSide, float IRLSide) {
			frontIRVoltage = IRFront;
			leftIRVoltage = IRLeft;
			rightIRVoltage = IRRight;
			rSideVoltage = IRRSide;
			lSideVoltage = IRLSide;
		}

		public void checkStates() {
			if (goingForward) {
				// too close time to turn
				if (frontIRVoltage > 2.0) {
					goingForward = false;

					// check the higher diag IR
					if (leftIRVoltage > rightIRVoltage) {
						followingLeft = true;
					} else {
						followingRight = true;
					}
				}
			}
			// else if (followingLeft) {
			// // too close time to turn
			// if (frontIRVoltage > 2.0) {
			// followingLeft = false;
			// turningRight = true;
			// }
			// } else if (followingRight) {
			// // too close time to turn
			// if (frontIRVoltage > 2.0) {
			// // followingRight = false;
			// // turningLeft = true;
			// motorPW = ACTUALSTOP;
			//
			// }
			// } else {
			// // NO CHECKS FOR TURNING IR LOGIC IMPLEMENTED YET
			// }
		}

	}
}
