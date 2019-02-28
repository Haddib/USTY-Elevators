package com.ru.usty.elevator;

import java.io.Console;
import java.util.concurrent.Semaphore;

import static java.lang.Thread.sleep;

public class Elevator implements Runnable {

    private int currentFloor, numberOfFloors, passengerCount;
    private boolean goingUp;
    private ElevatorScene ES;
    Semaphore elevatorSemaphore;
    Boolean keepGoing;
    private int loop;

    Elevator(int numberOfFloors, int currentFloor){
        this.currentFloor = currentFloor;
        this.passengerCount = 0;
        this.goingUp = true;
        this.numberOfFloors = numberOfFloors;
        this.ES = ElevatorScene.getInstance();
        this.elevatorSemaphore = new Semaphore(1);
        keepGoing = true;
    }

    /*Elevator getInstance(){                   //Instance til að person geti kallað á þessa lyftu.
        return this;
    }*/

    int getCurrentFloor(){
        return currentFloor;
    }

    int getPassengerCount(){
        return passengerCount;
    }

    private void moveUp(){                                   //Fara upp um eina hæð, nema lyftan sé á efstu hæð
        if(currentFloor < numberOfFloors - 1){
            //System.out.println(Thread.currentThread().getName() + " going up");
            currentFloor++;
        }
        else{
            goingUp = false;
            moveDown();
        }
    }

    private void moveDown(){                                 //Fara niður eina hæð, nema lyftan sé á neðstu hæð
        if(currentFloor > 0){
            //System.out.println(Thread.currentThread().getName() + " going down");
            currentFloor--;
        }
        else{
            goingUp = true;
            moveUp();
        }
    }

    void exitPassenger(){
        if(passengerCount > 0){
            passengerCount--;
        }
    }

    void enterPassenger(){
        if(passengerCount < 6){
            passengerCount++;
        }
    }


    @Override
    public void run() {
        while (keepGoing) {
            if (ES.waitingPerson() || passengerCount == 6 || (ES.getNumberOfPeopleWaitingAtFloor(currentFloor) == 0)) { //Ef lyftan er full EÐA ef engin er að bíða á þessari hæð
                try {
                    this.elevatorSemaphore.acquire();               //fá leyfi til að nota lyftuna
                    //System.out.println("Elevator " + Thread.currentThread().getId() + " acquiring " + this.elevatorSemaphore.toString());
                    loop = 0;
                    if (goingUp) {                                    //fara upp eða niður um eina hæð
                        moveUp();
                        this.elevatorSemaphore.release();
                    } else {
                        moveDown();
                        this.elevatorSemaphore.release();
                    }
                    //System.out.println("Elevator " + Thread.currentThread().getId() + " releasing " + this.elevatorSemaphore.toString());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            try {
                sleep(ElevatorScene.VISUALIZATION_WAIT_TIME);       //ef lyftan er ekki full eða ef einhver er að bíða á þessari hæð. Bíðum 500ms
                loop++;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            //System.out.println("Elevator " + Thread.currentThread().getId() + " loop: " + loop + " passengers: " + passengerCount);
            System.out.println("Passengers left on floor " + currentFloor + ": " + ES.getNumberOfPeopleWaitingAtFloor(currentFloor));

            if(!ES.waitingPerson()){
                System.out.println("No one is waiting anywhere!");
            }

            if((loop > 10 && !ES.waitingPerson() && passengerCount == 0)){
                keepGoing = false;
                System.out.println(Thread.currentThread().getName() + " quitting");
            }
        }
    }
}
