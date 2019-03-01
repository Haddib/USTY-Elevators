package com.ru.usty.elevator;

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

    int getCurrentFloor(){
        return currentFloor;
    }

    int getPassengerCount(){
        return passengerCount;
    }

    //Fara upp um eina hæð, nema lyftan sé á efstu hæð
    private void moveUp(){
        if(currentFloor < numberOfFloors - 1){
            currentFloor++;
        }
        else{
            goingUp = false;
            moveDown();
        }
    }

    //Fara niður eina hæð, nema lyftan sé á neðstu hæð
    private void moveDown(){
        if(currentFloor > 0){
            currentFloor--;
        }
        else{
            goingUp = true;
            moveUp();
        }
    }

    //minnka farþegafjöldann um 1
    void exitPassenger(){
        if(passengerCount > 0){
            passengerCount--;
        }
    }

    //hækka farþegafjöldann um 1
    void enterPassenger(){
        if(passengerCount < 6){
            passengerCount++;
        }
    }


    @Override
    public void run() {
        while (keepGoing) {

            //ef einhver er að bíða á einhverri hæð eða ef lyftan er full eða ef enginn er á þessari hæð
            if (ES.waitingPerson() || passengerCount == 6 || (ES.getNumberOfPeopleWaitingAtFloor(currentFloor) == 0)) {

                //fá leyfi til að nota lyftuna
                try {
                    this.elevatorSemaphore.acquire();
                    loop = 0;

                    //fara upp eða niður um eina hæð
                    if (goingUp) {
                        moveUp();
                        this.elevatorSemaphore.release();
                    } else {
                        moveDown();
                        this.elevatorSemaphore.release();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            //bíða í 500ms
            try {
                sleep(ElevatorScene.VISUALIZATION_WAIT_TIME);
                loop++;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            //ef enginn er að bíða neinstaðar og engin hefur beðið á neinni hæð í 10 * 500ms. Hættum keyrslu
            if((loop > 10 && !ES.waitingPerson() && passengerCount == 0)){
                keepGoing = false;
            }
        }
    }
}
