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

    public static Elevator getInstance(){
        return instance;
    }

    public void moveUp(){
        if(currentFloor < numberOfFloors - 1){
            currentFloor++;
        }
        else{
            goingUp = false;
            moveDown();
        }
    }

    public void moveDown(){
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
            if(passengerCount == 6 || (ES.getNumberOfPeopleWaitingAtFloor(currentFloor) == 0)){
                try {
                    this.elevatorSemaphore.acquire();
                    if(goingUp){
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
                sleep(ElevatorScene.VISUALIZATION_WAIT_TIME);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
