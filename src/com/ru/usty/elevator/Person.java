package com.ru.usty.elevator;

public class Person implements Runnable {
    private int sourceFloor, destinationFloor;
    private Elevator elevator;                          //lyftan sem þessi farþegi er í
    private boolean inElevator;
    private ElevatorScene ES;

    Person(int sourceFloor, int destinationFloor){
        this.sourceFloor = sourceFloor;
        this.destinationFloor = destinationFloor;
        this.elevator = null;
        this.ES = ElevatorScene.getInstance();
        this.inElevator = false;
    }

    //Reyna að fara inn í lyftu
    private void enterElevator(Elevator elevator){

        //fá leyfi til að nota lyftuna
        try {
            elevator.elevatorSemaphore.acquire();

            //ef lyftan er ekki full
            if(elevator.getPassengerCount() < 6){

                //vista að þessi persóna er í þessari lyftu
                this.elevator = elevator;

                //láta lyftuna vita að ég er kominn inn
                this.elevator.enterPassenger();
                this.inElevator = true;

                //láta ElevatorScene vita að ég er ekki lengir að bíða á þessari hæð
                this.ES.decrementPersonCount(sourceFloor);
            }

            elevator.elevatorSemaphore.release();

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    //reyna að fara úr lyftunni
    private void exitElevator(){
        if(this.inElevator){

            //láta lyftuna vita að farþegi fór út
            this.elevator.exitPassenger();

            //láta ElevatorScene á hvaða hæð þessi persóna fór út
            this.ES.personExitsAtFloor(destinationFloor);
            this.inElevator = false;
        }
    }

    //bíða eftir að lyftan sem ég er í fari á hæðina sem ég vil fara á
    private void waitForFloor(){

        //tjekka alltaf á meðan ég er í lyftunni
        while(this.inElevator){

            //fá leyfi til að lesa úr lyftunni
            try {
                this.elevator.elevatorSemaphore.acquire();

                //er lyftan komin á rétta hæð?
                if(this.elevator.getCurrentFloor() == destinationFloor){

                    //ef svo, förum úr henni
                    this.exitElevator();
                }

                this.elevator.elevatorSemaphore.release();

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    //bíða eftir því að lyfta kommi á mína hæð
    private void waitForElevator(){

        //á meðan ég er ekki í lyftu
        while(!this.inElevator){
            for (Elevator e : ES.elevators) {

                //tjekkum hvort að einhver lyfta sé á minni hæð sem er ekki full
                if(e.getCurrentFloor() == sourceFloor && e.getPassengerCount() < 6){

                    //reynum að fara í lyftuna
                    enterElevator(e);

                    //þurfum ekki að tjekka á fleiri lyftum.
                    break;
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
