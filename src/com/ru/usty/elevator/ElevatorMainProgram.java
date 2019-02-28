package com.ru.usty.elevator;

import com.ru.usty.elevator.visualization.TestSuite;

public class ElevatorMainProgram {
	public static void main(String[] args) {

		try {

			TestSuite.startVisualization();

/***EXPERIMENT HERE BUT THIS WILL BE CHANGED DURING GRADING***/


			/*Thread.sleep(1000);

			TestSuite.runTest(5);

			Thread.sleep(2000);*/


			for(int i = 0; i <= 9; i++) {
				System.out.println("RUNNING TEST " + i);
				TestSuite.runTest(i);
				System.out.println("TEST " + i + " FINISHED!");
				Thread.sleep(2000);
			}

/*************************************************************/

		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		System.exit(0);
	}
}
