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

	private static ElevatorScene instance; 				// Til að Elevator og Person geti kallað á föll í þessum klasa

	private int numberOfFloors;
	private int numberOfElevators;

	private ArrayList<Integer> personCount; 			//Heldur utan um hversu margir eru að bíða á hverri hæð
	private ArrayList<Integer> exitedCount = null; 		//Heldur utan um hversu margir hafa farið út á hverri hæð

	ArrayList<Elevator> elevators = null; 				//Listi af lyftur
	private ArrayList<Person> persons = null;			//Listi af fólki
	private ArrayList<Thread> elevatorThreads = null;	//Listi af lyftuþráðunum
	private Semaphore exitedCountMutex;					//Semaphore fyrir exitedCount
	private Semaphore personCountSemaphore;				//Semaphore fyrir personCount
	private Semaphore elevatorFloorSemaphore;			//Semaphore fyrir elevatorFloor
	private Semaphore elevatorListSemaphore;			//Semaphore fyrir bæði elevator og elevatorThreads
	//Base function: definition must not change
	//Necessary to add your code in this one
	public void restartScene(int numberOfFloors, int numberOfElevators) {

		//instance af ElevatorScene til að Elevator og Person geti kallað á föll úr ElevatorScene
		instance = this;

		//Ganga frá þráðum sem eru en á lífi. Eyðir líka úr elevators og elevatorThreads
		KillThreads();

		//initializa semaphorur
		exitedCountMutex = new Semaphore(1);
		personCountSemaphore = new Semaphore(1);
		elevatorFloorSemaphore = new Semaphore(1);
		elevatorListSemaphore = new Semaphore(2);

		//Fá leyfi til að nota elevator og elevatorThreads til að fylla þá
		try {

			elevatorListSemaphore.acquire();

			for(int i = 0 ; i < numberOfElevators ; i++){
				int floor = 0;

				//Þetta er til þess að allar lyftur byrji ekki á sömu hæð. Aðalega upp á útlitið
				if(i < numberOfFloors){
					floor = i;
				}

				Elevator e = new Elevator(numberOfFloors, floor);
				elevators.add(e);
				Thread et = new Thread(e);
				elevatorThreads.add(et);
				et.start();
			}

			elevatorListSemaphore.release();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		setNumberOfFloors(numberOfFloors);
		setNumberOfElevators(numberOfElevators);

		//frumstilla persons
		if(persons == null){
			persons = new ArrayList<>();
		}
		else{
			persons.clear();
		}

		//fá leyfi til að nota personCount til að frumstilla það
		try {
			personCountSemaphore.acquire();

			//frumstilla personCount
			if(personCount == null){
				personCount = new ArrayList<>();
			}
			else{
				personCount.clear();
			}
			for(int i = 0; i < numberOfFloors; i++) {
				this.personCount.add(0);
			}

			personCountSemaphore.release();

		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		//fá leyfi til að nota exitedCount til að frumstilla það
		try {

			exitedCountMutex.acquire();

			//frumstilla exitedCount
			if(exitedCount == null) {
				exitedCount = new ArrayList<>();
			}
			else {
				exitedCount.clear();
			}
			for(int i = 0; i < getNumberOfFloors(); i++) {
				this.exitedCount.add(0);
			}

			exitedCountMutex.release();

		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	//static til þess að aðrir klasar geti náð í instance
	static ElevatorScene getInstance(){			//þarf að vera static svo að aðrir klasar hafi aðgang
		return instance;
	}

	//búa til nýja persónu og keyra hana á nýjum þræði
	public Thread addPerson(int sourceFloor, int destinationFloor) {

		Person person = new Person(sourceFloor, destinationFloor);
		persons.add(person);
		Thread personThread = new Thread(person);
		personThread.start();

		//fá leyfi til að nota personCount til að geta hækkað það um einn fyrir þá hæð sem persónan er á
		try {
			personCountSemaphore.acquire();
			personCount.set(sourceFloor, personCount.get(sourceFloor) + 1);
			personCountSemaphore.release();

		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		return personThread;  //this means that the testSuite will not wait for the threads to finish
	}

	//bara notað af visualizer
	public int getCurrentFloorForElevator(int elevator) {

		//fá leyfi til að lesa úr elevators, elevatorThreads og elevatorFloor
		try {
			elevatorListSemaphore.acquire();
			elevatorFloorSemaphore.acquire();

			int floor = 0;

			//tjekka hvort elevators sé til
			if(elevators != null){

				//tjekka hvort indexið sé innan marka
				if(elevator < elevators.size()){
					floor = elevators.get(elevator).getCurrentFloor();
				}
			}

			elevatorFloorSemaphore.release();
			elevatorListSemaphore.release();

			return floor;
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		return 0;
	}

	//bara notað af visualizer
	public int getNumberOfPeopleInElevator(int elevator) {

		//fá leyfi til að lesa úr elevators og elevatorThreads
		try {

			elevatorListSemaphore.acquire();

			int count = 0;

			//tjekka hvort elevators sé til
			if(elevators != null){

				//tjekka hvort indexið sé innan marka
				if(elevator < elevators.size()){
					count = elevators.get(elevator).getPassengerCount();
				}
			}

			elevatorListSemaphore.release();

			return count;
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return 0;
	}

	//tjekka hversu maargir eru að bíða á ákveðinni hæð
	public int getNumberOfPeopleWaitingAtFloor(int floor) {

		//tjekka hvort personCount sé til
		if(personCount != null){
			return personCount.get(floor);
		}
		return 0;
	}

	//Base function: definition must not change, but add your code if needed
	public int getNumberOfFloors() {
		return numberOfFloors;
	}

	//Base function: definition must not change, but add your code if needed
	private void setNumberOfFloors(int numberOfFloors) {
		this.numberOfFloors = numberOfFloors;
	}

	//Base function: definition must not change, but add your code if needed
	public int getNumberOfElevators() {
		return numberOfElevators;
	}

	//Base function: definition must not change, but add your code if needed
	private void setNumberOfElevators(int numberOfElevators) {
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

	//láta vita að manneskja fór út á þessari hæð
	void personExitsAtFloor(int floor) {

		//fá leyfi til að breyta exitedCount
		try {
			exitedCountMutex.acquire();

			exitedCount.set(floor, (exitedCount.get(floor) + 1));

			exitedCountMutex.release();

		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	//láta vita að manneskja er ekki lengur að bíða á þessari hæð
	void decrementPersonCount(int floor){

		//fá leyfi til að breyta personCount
		try {
			personCountSemaphore.acquire();

			//tjekka hvort það sé ekki örugglega manneskja á þessari hæð
			if(personCount.get(floor) > 0){
				personCount.set(floor, personCount.get(floor) - 1);
			}

			personCountSemaphore.release();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	//tjekka hversu margir eru að bíða á ákveðinni hæð
	public int getExitedCountAtFloor(int floor) {

		//tjekka hvort það séu ekki örugglega þetta margar hæðir
		if(floor < getNumberOfFloors()) {
			return exitedCount.get(floor);
		}
		else {
			return 0;
		}
	}

	//tjekkar hvort það sé einhver að bíða á einhverri hæð
	boolean waitingPerson(){

		//tjekka hvort personCount sé til
		if(personCount != null){
			for (Integer integer : personCount) {

				//ef eitthvað stak í personCount er ekki 0, er einhver að bíða
				if (integer != 0) {
					return true;
				}
			}
		}

		//ef við komumst hingað er enginn að bíða
		return false;
	}

	//drepur alla lyftuþræði ef þeir eru til
	private void KillThreads(){

		//tjekka hvort þessu semaphora sé til. Ef hún er til, fá leyfi til að nota hana.
		if(elevatorListSemaphore != null){
			try {
				elevatorListSemaphore.acquire();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		//frumstilla elevators ef þess þarf
		if(elevators == null) {
			elevators = new ArrayList<>();
		}

		//ef það þarf ekki að frumstilla elevators, segja öllum sem eru í gangi að stoppa.
		else{
			for (Elevator el: elevators) {
				el.keepGoing = false;
			}

			//eyða svo úr elevators (þræðirnir eru samt en til hér)
			elevators.clear();
		}

		//frumstilla elevatorThreads ef þess þarf
		if(elevatorThreads == null){
			elevatorThreads = new ArrayList<>();
		}

		//ef það þarf ekki að frumstilla, joinum alla þræði sem eru en á lífi
		else{
			for (Thread t : elevatorThreads) {			//Ef einhverjir þræðir eru í gangi, joina þá.
				if(t.isAlive()) {
					try {
						t.join();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}

			//eyðum svo úr elevatorThreads (nú eru allir þræðir stopp)
			elevatorThreads.clear();
		}

		//ef semaphoran er til, sleppum henni
		if(elevatorListSemaphore != null){
			elevatorListSemaphore.release();
		}
	}
}
