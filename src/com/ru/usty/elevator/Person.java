package com.ru.usty.elevator;

public class Person implements Runnable {
    private int sourceFloor, destinationFloor;
    private Elevator elevator;                                       //lyftan sem þessu farþegi er í
    private boolean inElevator;
    private ElevatorScene ES;

    Person(int sourceFloor, int destinationFloor){
        this.sourceFloor = sourceFloor;
        this.destinationFloor = destinationFloor;
        this.elevator = null;                                       //farþegi byrjar ekki í lyftu
        this.ES = ElevatorScene.getInstance();
        this.inElevator = false;
    }

    private void enterElevator(Elevator elevator){
        try {
            //this.ES.personCountSemaphore.acquire();                 //fá leyfi til að nota personCount í ElevatorScene
            elevator.elevatorSemaphore.acquire();                   //fá leyfi til að nota lyftuna
            //System.out.println("Person " + Thread.currentThread().getId() + " acquiring " + elevator.elevatorSemaphore.toString());


            if(elevator.getPassengerCount() < 6){                   //ef lyftan er ekki full
                this.elevator = elevator;                           //vista þessa lyftu í klasanum
                this.elevator.enterPassenger();                     //láta lyftuna vita að farþegi er kominn inn.
                this.inElevator = true;
                this.ES.decrementPersonCount(sourceFloor);          //láta ElevatorScene vita að þessi persóna er ekki lengur að bíða á þessari hæð
            }

            //this.ES.personCountSemaphore.release();                 //búinn með personCount
            elevator.elevatorSemaphore.release();                   //búinn með lyftuna
            //System.out.println("Person " + Thread.currentThread().getId() + " releasing " + elevator.elevatorSemaphore.toString());

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void exitElevator(){
        if(this.inElevator){                                        //ef þessi persóna er í lyftu
            this.elevator.exitPassenger();                          //láta lyftuna vita að farþegi fór út
            this.ES.personExitsAtFloor(destinationFloor);           //láta ElevatorScene á hvaða hæð þessi persóna fór út
            this.inElevator = false;

        }
    }

    private void waitForFloor(){
        while(this.inElevator){                                     //ef þessi per´sona er í lyftu
            try {
                this.elevator.elevatorSemaphore.acquire();          //fá leyfi til að nota lyftuna
                if(this.elevator.getCurrentFloor() == destinationFloor){ //ef lyftan er á éttri hæð
                    this.exitElevator();                            //förum úr lyftunni
                }
                this.elevator.elevatorSemaphore.release();          //búinn með lyftuna
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void waitForElevator(){
        while(!this.inElevator){                                    //ef þessi farþegi er ekki í lyftunni
            for (Elevator e : ES.elevators) {                       //tjekkum hvort að einhver lyfta sé á þessari hæð
                if(e.getCurrentFloor() == sourceFloor && e.getPassengerCount() < 6){
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
