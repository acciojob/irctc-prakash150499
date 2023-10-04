package com.driver.services;

import com.driver.EntryDto.AddTrainEntryDto;
import com.driver.EntryDto.SeatAvailabilityEntryDto;
import com.driver.model.Passenger;
import com.driver.model.Station;
import com.driver.model.Ticket;
import com.driver.model.Train;
import com.driver.repository.TrainRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Service
public class TrainService {

    @Autowired
    TrainRepository trainRepository;

    public Integer addTrain(AddTrainEntryDto trainEntryDto){

        //Add the train to the trainRepository
        //and route String logic to be taken from the Problem statement.
        //Save the train and return the trainId that is generated from the database.
        //Avoid using the lombok library

        Train train=new Train();
        train.setNoOfSeats(trainEntryDto.getNoOfSeats());
        train.setDepartureTime(trainEntryDto.getDepartureTime());
        String route= stationListToString(trainEntryDto.getStationRoute());
        train.setRoute(route);
        Train train1=trainRepository.save(train);


    return train1.getTrainId();
    }

    private String stationListToString(List<Station> stationRoute) {
        String route="";

        for(int i=0;i<stationRoute.size();i++)
        {
            route+=stationRoute.get(i).toString();

            if (i != stationRoute.size() - 1) {
                route += ",";
            }

        }
        return route;

    }

    public Integer calculateAvailableSeats(SeatAvailabilityEntryDto seatAvailabilityEntryDto){

        //Calculate the total seats available
        //Suppose the route is A B C D
        //And there are 2 seats avaialble in total in the train
        //and 2 tickets are booked from A to C and B to D.
        //The seat is available only between A to C and A to B. If a seat is empty between 2 station it will be counted to our final ans
        //even if that seat is booked post the destStation or before the boardingStation
        //Inshort : a train has totalNo of seats and there are tickets from and to different locations
        //We need to find out the available seats between the given 2 stations.

        Train train = trainRepository.findById(seatAvailabilityEntryDto.getTrainId()).get();
        List<Ticket> bookedTickets = train.getBookedTickets();
        String[] routeStations = train.getRoute().split(",");
        HashMap<String, Integer> stationIndexMap = new HashMap<>();

        for (int i = 0; i < routeStations.length; i++) {
            stationIndexMap.put(routeStations[i], i);
        }

        String fromStationName = seatAvailabilityEntryDto.getFromStation().toString();
        String toStationName = seatAvailabilityEntryDto.getToStation().toString();

        if (!stationIndexMap.containsKey(fromStationName) || !stationIndexMap.containsKey(toStationName)) {
            return 0;
        }

        int totalSeats = train.getNoOfSeats();
        int bookedSeats = 0;

        for (Ticket ticket : bookedTickets) {
            bookedSeats += ticket.getPassengersList().size();
        }

        int availableSeats = totalSeats - bookedSeats;

        for (Ticket bookedTicket : bookedTickets) {
            String bookedFromStation = bookedTicket.getFromStation().toString();
            String bookedToStation = bookedTicket.getToStation().toString();

            if (stationIndexMap.get(toStationName) <= stationIndexMap.get(bookedFromStation)) {
                availableSeats++;
            } else if (stationIndexMap.get(fromStationName) >= stationIndexMap.get(bookedToStation)) {
                availableSeats++;
            }
        }

        return availableSeats + 2;
    }

    public Integer calculatePeopleBoardingAtAStation(Integer trainId,Station station) throws Exception{

        //We need to find out the number of people who will be boarding a train from a particular station
        //if the trainId is not passing through that station
        //throw new Exception("Train is not passing from this station");
        //  in a happy case we need to find out the number of such people.

        Train selectedTrain = trainRepository.findById(trainId).get();
        String requestedStation = station.toString();
        String[] routeStations = selectedTrain.getRoute().split(",");
        boolean isStationFound = false;

        for (String stationName : routeStations) {
            if (stationName.equals(requestedStation)) {
                isStationFound = true;
                break;
            }
        }

        if (!isStationFound) {
            throw new Exception("Train is not passing through this station");
        }

        int totalPassengersAtStation = 0;
        List<Ticket> bookedTickets = selectedTrain.getBookedTickets();

        for (Ticket ticket : bookedTickets) {
            if (ticket.getFromStation().toString().equals(requestedStation)) {
                totalPassengersAtStation += ticket.getPassengersList().size();
            }
        }

        return totalPassengersAtStation;

    }

    public Integer calculateOldestPersonTravelling(Integer trainId){

        //Throughout the journey of the train between any 2 stations
        //We need to find out the age of the oldest person that is travelling the train
        //If there are no people travelling in that train you can return 0

        Train train=trainRepository.findById(trainId).get();

        if(train.getBookedTickets().size()==0)return 0;
        List<Ticket>ticketList=train.getBookedTickets();
        int oldPerson=0;

        for(Ticket ticket:ticketList)
        {
            List<Passenger>passengerList=ticket.getPassengersList();
            for(Passenger passenger:passengerList)
            {
                oldPerson=Math.max(oldPerson,passenger.getAge());
            }
        }
        return oldPerson;
    }

    public List<Integer> trainsBetweenAGivenTime(Station station, LocalTime startTime, LocalTime endTime){

        //When you are at a particular station you need to find out the number of trains that will pass through a given station
        //between a particular time frame both start time and end time included.
        //You can assume that the date change doesn't need to be done ie the travel will certainly happen with the same date (More details
        //in problem statement)
        //You can also assume the seconds and milli seconds value will be 0 in a LocalTime format.

        List<Integer> trainList=new ArrayList<>();

        List<Train>trains=trainRepository.findAll();

        for(Train train:trains)
        {
            String[] route=train.getRoute().split(",");

            for(int i=0;i<route.length;i++)
            {
                if(route[i].equals(String.valueOf(station)))
                {
                    int startTimeInMin=startTime.getHour()*60+startTime.getMinute();
                    int endTimeInMin=endTime.getHour()*60+endTime.getMinute();

                    int departTimeInMin=train.getDepartureTime().getHour()*60+train.getDepartureTime().getMinute();
                    int timeAtGivenStation=departTimeInMin+(i*60);

                    if(timeAtGivenStation<=endTimeInMin&&timeAtGivenStation>=startTimeInMin)
                    {

                        trainList.add(train.getTrainId());
                    }


                }
            }
        }

        return trainList;
    }

}
