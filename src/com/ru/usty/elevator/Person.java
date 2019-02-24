package com.ru.usty.elevator;

import java.util.concurrent.Semaphore;

public class Person implements Runnable {
    public int sourceFloor, destinationFloor;
    public Elevator elevator;
    public boolean inElevator;
    public Semaphore personSemaphore;
    public ElevatorScene ES;

    public Person(int sourceFloor, int destinationFloor){
        this.sourceFloor = sourceFloor;
        this.destinationFloor = destinationFloor;
        this.elevator = null;
        this.personSemaphore = new Semaphore(1, true);
        this.ES = ElevatorScene.getInstance();
        this.inElevator = false;
    }

    public void enterElevator(Elevator elevator){
        try {
            this.ES.personCountSemaphore.acquire();
            elevator.elevatorSemaphore.acquire();

            if(elevator.passengerCount < 6){
                this.elevator = elevator.getInstance();
                this.elevator.enterPassenger();
                this.inElevator = true;
                this.ES.decrementPersonCount(sourceFloor);
            }

            this.ES.personCountSemaphore.release();
            elevator.elevatorSemaphore.release();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void exitElevator(){
        if(this.inElevator){
            this.elevator.exitPassenger();
            this.ES.personExitsAtFloor(destinationFloor);
            this.inElevator = false;

        }
    }

    public void waitForFloor(){
        while(this.inElevator){
            try {
                this.elevator.elevatorSemaphore.acquire();
                if(this.elevator.currentFloor == destinationFloor){
                    this.exitElevator();
                }
                this.elevator.elevatorSemaphore.release();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void waitForElevator(){
        while(!this.inElevator){
            for (Elevator e : ES.elevators) {
                if(e.currentFloor == sourceFloor){
                    enterElevator(e);
                }
            }
        }
    }

    @Override
    public void run() {
        waitForElevator();
        waitForFloor();
    }
}
