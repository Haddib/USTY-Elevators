package com.ru.usty.elevator;

import java.util.concurrent.Semaphore;

import static java.lang.Thread.sleep;

public class Elevator implements Runnable {

    public int currentFloor, numberOfFloors, passengerCount;
    public boolean goingUp;
    public ElevatorScene ES;
    public static Elevator instance;
    public Semaphore elevatorSemaphore;

    public Elevator(int numberOfFloors){
        this.instance = this;
        this.currentFloor = 0;
        this.passengerCount = 0;
        this.goingUp = true;
        this.numberOfFloors = numberOfFloors;
        this.ES = ElevatorScene.getInstance();
        this.elevatorSemaphore = new Semaphore(1);
    }

    public static Elevator getInstance(){                   //Instance til að person geti kallað á þessa lyftu.
        return instance;
    }

    public void moveUp(){                                   //Fara upp um eina hæð, nema lyftan sé á efstu hæð
        if(currentFloor < numberOfFloors - 1){
            currentFloor++;
        }
        else{
            goingUp = false;
            moveDown();
        }
    }

    public void moveDown(){                                 //Fara niður eina hæð, nema lyftan sé á neðstu hæð
        if(currentFloor > 0){
            currentFloor--;
        }
        else{
            goingUp = true;
            moveUp();
        }
    }

    public void exitPassenger(){
        if(passengerCount > 0){
            passengerCount--;
        }
    }

    public void enterPassenger(){
        if(passengerCount < 6){
            passengerCount++;
        }
    }


    @Override
    public void run() {
        while(true){
            if((ES.waitingPerson() || passengerCount != 0) && (passengerCount == 6 || (ES.getNumberOfPeopleWaitingAtFloor(currentFloor) == 0))){ //Ef lyftan er full EÐA ef engin er að bíða á þessari hæð
                try {
                    this.elevatorSemaphore.acquire();               //fá leyfi til að nota lyftuna
                    if(goingUp){                                    //fara upp eða niður um eina hæð
                        moveUp();
                        this.elevatorSemaphore.release();
                    }
                    else{
                        moveDown();
                        this.elevatorSemaphore.release();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            try {
                sleep(ElevatorScene.VISUALIZATION_WAIT_TIME);       //ef lyftan er ekki full eða ef einhver er að bíða á þessari hæð. Bíðum 500ms
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
