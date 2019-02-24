package com.ru.usty.elevator;

import java.util.concurrent.Semaphore;

public class Person implements Runnable {
    public int sourceFloor, destinationFloor;
    public Elevator elevator;                                       //lyftan sem þessu farþegi er í
    public boolean inElevator;
    public Semaphore personSemaphore;
    public ElevatorScene ES;

    public Person(int sourceFloor, int destinationFloor){
        this.sourceFloor = sourceFloor;
        this.destinationFloor = destinationFloor;
        this.elevator = null;                                       //farþegi byrjar ekki í lyftu
        this.personSemaphore = new Semaphore(1, true);
        this.ES = ElevatorScene.getInstance();
        this.inElevator = false;
    }

    public void enterElevator(Elevator elevator){
        try {
            this.ES.personCountSemaphore.acquire();                 //fá leyfi til að nota personCount í ElevatorScene
            elevator.elevatorSemaphore.acquire();                   //fá leyfi til að nota lyftuna

            if(elevator.passengerCount < 6){                        //ef lyftan er ekki full
                this.elevator = elevator.getInstance();             //vista þessa lyftu í klasanum
                this.elevator.enterPassenger();                     //láta lyftuna vita að farþegi er kominn inn.
                this.inElevator = true;
                this.ES.decrementPersonCount(sourceFloor);          //láta ElevatorScene vita að þessi persóna er ekki lengur að bíða á þessari hæð
            }

            this.ES.personCountSemaphore.release();                 //búinn með personCount
            elevator.elevatorSemaphore.release();                   //búinn með lyftuna
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void exitElevator(){
        if(this.inElevator){                                        //ef þessi persóna er í lyftu
            this.elevator.exitPassenger();                          //láta lyftuna vita að farþegi fór út
            this.ES.personExitsAtFloor(destinationFloor);           //láta ElevatorScene á hvaða hæð þessi persóna fór út
            this.inElevator = false;

        }
    }

    public void waitForFloor(){
        while(this.inElevator){                                     //ef þessi per´sona er í lyftu
            try {
                this.elevator.elevatorSemaphore.acquire();          //fá leyfi til að nota lyftuna
                if(this.elevator.currentFloor == destinationFloor){ //ef lyftan er á éttri hæð
                    this.exitElevator();                            //förum úr lyftunni
                }
                this.elevator.elevatorSemaphore.release();          //búinn með lyftuna
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void waitForElevator(){
        while(!this.inElevator){                                    //ef þessi farþegi er ekki í lyftunni
            for (Elevator e : ES.elevators) {                       //tjekkum hvort að einhver lyfta sé á þessari hæð
                if(e.currentFloor == sourceFloor){
                    enterElevator(e);                               //ef svo, förum í hana
                    break;
                }
            }
        }
    }

    @Override
    public void run() {
        waitForElevator();                                          //bíða eftir lyfti
        waitForFloor();                                             //bíða eftir að lyftan kemst á rétta hæð
    }
}
