package com.driver.services;


import com.driver.EntryDto.BookTicketEntryDto;
import com.driver.model.Passenger;
import com.driver.model.Ticket;
import com.driver.model.Train;
import com.driver.repository.PassengerRepository;
import com.driver.repository.TicketRepository;
import com.driver.repository.TrainRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class TicketService {

    @Autowired
    TicketRepository ticketRepository;
    @Autowired
    TrainRepository trainRepository;
    @Autowired
    PassengerRepository passengerRepository;
    public Integer bookTicket(BookTicketEntryDto bookTicketEntryDto)throws Exception{

        //Check for validity
        //Use bookedTickets List from the TrainRepository to get bookings done against that train
        // Incase the there are insufficient tickets
        // throw new Exception("Less tickets are available");
        //otherwise book the ticket, calculate the price and other details
        //Save the information in corresponding DB Tables
        //Fare System : Check problem statement
        //Incase the train doesn't pass through the requested stations
        //throw new Exception("Invalid stations");
        //Save the bookedTickets in the train Object
        //Also in the passenger Entity change the attribute bookedTickets by using the attribute bookingPersonId.
       //And the end return the ticketId that has come from db

        //1. Get train using traing Id form dto
            int trainId=bookTicketEntryDto.getTrainId();
            Train train=trainRepository.findById(trainId).get();

        //2.Get already booked seats of the given train

        List<Ticket>ticketList=train.getBookedTickets();
        int bookedSeats=0;
        for(Ticket ticket:ticketList)
        {
            bookedSeats+=ticket.getPassengersList().size();
        }

        if(bookedSeats+bookTicketEntryDto.getNoOfSeats()>train.getNoOfSeats())
        {
            throw new Exception("Less tickets are available");
        }

        //3. Get all Passengers

            List<Integer> passengers=bookTicketEntryDto.getPassengerIds();
            List<Passenger>passengerList=new ArrayList<>();
            for(int id:passengers)
            {
                passengerList.add(passengerRepository.findById(id).get());
            }
         //4. Getting starting and ending destinations

            int startStation=-1;
            int endStation=-1;
            String []route=train.getRoute().split(",");
            for(int i=0;i<route.length;i++)
            {
                if(route[i].equals(bookTicketEntryDto.getFromStation()))
                {
                    startStation=i;
                    break;
                }
            }

        for(int i=0;i<route.length;i++)
        {
            if(route[i].equals(bookTicketEntryDto.getToStation()))
            {
                endStation=i;
                break;
            }
        }

        // station validation

        if(startStation==-1||endStation==-1||endStation-startStation<0)
        {
            throw new Exception("Invalid stations");
        }
        // 5.calculating the fare

        int distance=endStation-startStation;
        int noOfSeats=bookTicketEntryDto.getNoOfSeats();
        int totalFare=distance*300*noOfSeats;
        // 6. create a new Ticket and assign values to a ticket
        Ticket ticket=new Ticket();
        ticket.setPassengersList(passengerList);
        ticket.setFromStation(bookTicketEntryDto.getFromStation());
        ticket.setToStation(bookTicketEntryDto.getToStation());
        ticket.setTotalFare(totalFare);
        ticket.setTrain(train);
        // 7.Update the train
        train.getBookedTickets().add(ticket);
        train.setNoOfSeats(train.getNoOfSeats()-bookTicketEntryDto.getNoOfSeats());
        trainRepository.save(train);
        // 8. Update the passenger
        Passenger passenger=passengerRepository.findById(bookTicketEntryDto.getBookingPersonId()).get();
        passenger.getBookedTickets().add(ticket);
        Ticket savedTicket=ticketRepository.save(ticket);
        return savedTicket.getTicketId();

    }
}
