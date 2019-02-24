package com.ru.usty.elevator;

import java.util.ArrayList;
import java.util.concurrent.Semaphore;

/**
 * The base function definitions of this class must stay the same
 * for the test suite and graphics to use.
 * You can add functions and/or change the functionality
 * of the operations at will.
 *
 */

public class ElevatorScene {

	//TO SPEED THINGS UP WHEN TESTING,
	//feel free to change this.  It will be changed during grading
	public static final int VISUALIZATION_WAIT_TIME = 500;  //milliseconds

	public static ElevatorScene instance; 				// Til að Elevator og Person geti kallað á föll í þessum klasa

	private int numberOfFloors;
	private int numberOfElevators;

	ArrayList<Integer> personCount; 					//Heldur utan um hversu margir eru að bíða á hverri hæð
	ArrayList<Integer> exitedCount = null; 				//Heldur utan um hversu margir hafa farið út á hverri hæð

	public ArrayList<Elevator> elevators = null; 		//Listi af lyftur
	public ArrayList<Person> persons = null;			//Listi af fólki
	public ArrayList<Thread> elevatorThreads = null;	//Listi af lyftuþráðunum
	public Semaphore exitedCountMutex;					//Semaphore fyrir exitedCount
	public Semaphore personCountSemaphore;				//Semaphore fyrir personCount

	//Base function: definition must not change
	//Necessary to add your code in this one
	public void restartScene(int numberOfFloors, int numberOfElevators) {

		/**
		 * Important to add code here to make new
		 * threads that run your elevator-runnables
		 * 
		 * Also add any other code that initializes
		 * your system for a new run
		 * 
		 * If you can, tell any currently running
		 * elevator threads to stop
		 */
		instance = this;								//instanse er þetta eintak af klasanum

		if(elevators == null){							//frumstilla elevators
			elevators = new ArrayList<>();
		}
		else{
			elevators.clear();
		}

		if(persons == null){							//frumstilla persons
			persons = new ArrayList<>();
		}
		else{
			persons.clear();
		}

		if(elevatorThreads == null){					//frumstilla elevatorThreads
			elevatorThreads = new ArrayList<>();
		}
		else{
			for (Thread t : elevatorThreads) {			//Ef einhverjir þræðir eru í gangi, joina þá.
				try {
					t.join();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			elevatorThreads.clear();
		}

		for(int i = 0 ; i < numberOfElevators ; i++){	//fylla elevator of elevatorThreads af lyftum
			Elevator e = new Elevator(numberOfFloors);
			elevators.add(e);
			Thread et = new Thread(e);
			elevatorThreads.add(et);
			et.start();
		}

		this.numberOfFloors = numberOfFloors;
		this.numberOfElevators = numberOfElevators;

		personCount = new ArrayList<Integer>();
		for(int i = 0; i < numberOfFloors; i++) {
			this.personCount.add(0);
		}

		if(exitedCount == null) {
			exitedCount = new ArrayList<Integer>();
		}
		else {
			exitedCount.clear();
		}
		for(int i = 0; i < getNumberOfFloors(); i++) {
			this.exitedCount.add(0);
		}
		exitedCountMutex = new Semaphore(1);
		personCountSemaphore = new Semaphore(1);

	}

	public static ElevatorScene getInstance(){			//þarf að vera static svo að aðrir klasar hafi aðgang
		return instance;
	}

	//Base function: definition must not change
	//Necessary to add your code in this one
	public Thread addPerson(int sourceFloor, int destinationFloor) {

		/**
		 * Important to add code here to make a
		 * new thread that runs your person-runnable
		 * 
		 * Also return the Thread object for your person
		 * so that it can be reaped in the testSuite
		 * (you don't have to join() yourself)
		 */

		//dumb code, replace it!

		Person person = new Person(sourceFloor, destinationFloor); //búa til nýja persónu og keyra hana á nýjum þræði
		persons.add(person);
		Thread personThread = new Thread(person);
		personThread.start();

		try {
			personCountSemaphore.acquire(); 			//Fá leyfi til að breyta personCount
			personCount.set(sourceFloor, personCount.get(sourceFloor) + 1);
			personCountSemaphore.release();				//Búinn með personCount

		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		return personThread;  //this means that the testSuite will not wait for the threads to finish
	}

	//Base function: definition must not change, but add your code
	public int getCurrentFloorForElevator(int elevator) {
		return elevators.get(elevator).currentFloor;
	}

	//Base function: definition must not change, but add your code
	public int getNumberOfPeopleInElevator(int elevator) {

		return elevators.get(elevator).passengerCount;
	}

	//Base function: definition must not change, but add your code
	public int getNumberOfPeopleWaitingAtFloor(int floor) {

		return personCount.get(floor);
	}

	//Base function: definition must not change, but add your code if needed
	public int getNumberOfFloors() {
		return numberOfFloors;
	}

	//Base function: definition must not change, but add your code if needed
	public void setNumberOfFloors(int numberOfFloors) {
		this.numberOfFloors = numberOfFloors;
	}

	//Base function: definition must not change, but add your code if needed
	public int getNumberOfElevators() {
		return numberOfElevators;
	}

	//Base function: definition must not change, but add your code if needed
	public void setNumberOfElevators(int numberOfElevators) {
		this.numberOfElevators = numberOfElevators;
	}

	//Base function: no need to change unless you choose
	//				 not to "open the doors" sometimes
	//				 even though there are people there
	public boolean isElevatorOpen(int elevator) {

		return isButtonPushedAtFloor(getCurrentFloorForElevator(elevator));
	}
	//Base function: no need to change, just for visualization
	//Feel free to use it though, if it helps
	public boolean isButtonPushedAtFloor(int floor) {

		return (getNumberOfPeopleWaitingAtFloor(floor) > 0);
	}

	//Person threads must call this function to
	//let the system know that they have exited.
	//Person calls it after being let off elevator
	//but before it finishes its run.
	public void personExitsAtFloor(int floor) {
		try {
			exitedCountMutex.acquire();
			exitedCount.set(floor, (exitedCount.get(floor) + 1));
			exitedCountMutex.release();

		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void decrementPersonCount(int floor){
		if(personCount.get(floor) > 0){
			personCount.set(floor, personCount.get(floor) - 1);
		}
	}

	//Base function: no need to change, just for visualization
	//Feel free to use it though, if it helps
	public int getExitedCountAtFloor(int floor) {
		if(floor < getNumberOfFloors()) {
			return exitedCount.get(floor);
		}
		else {
			return 0;
		}
	}

	public boolean waitingPerson(){
		for (int i = 0 ; i < personCount.size() ; i++) {
			if(personCount.get(i) != 0){
				return true;
			}
		}
		return false;
	}


}
