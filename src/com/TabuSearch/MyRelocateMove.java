package com.TabuSearch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;

import org.coinor.opents.ComplexMove;
import org.coinor.opents.Solution;

import com.mdvrp.Cost;
import com.mdvrp.Customer;
import com.mdvrp.Instance;
import com.mdvrp.Route;

@SuppressWarnings("serial")
public class MyRelocateMove implements ComplexMove{
	
	private Instance instance;
	private int routeIndexInsert;
	private int routeIndexDelete;
	private int insertedCustomerIndex;
	private int deletedCustomerIndex;
	private Customer insertedCustomer;
	private Customer deletedCustomer;

    private int deletePositionIndex;
    private int insertPositionIndex;
    
    public MyRelocateMove( Instance instance, int routeIndexInsert, int routeIndexDelete, Customer insertedCustomer, Customer deletedCustomer){
    	this.routeIndexInsert = routeIndexInsert;
    	this.routeIndexDelete = routeIndexDelete;
    	this.insertedCustomerIndex = insertedCustomer.getNumber();
    	this.deletedCustomerIndex = deletedCustomer.getNumber();
    	this.insertedCustomer = insertedCustomer;
    	this.deletedCustomer = deletedCustomer;
    	this.instance = instance;  	
        this.deletePositionIndex = deletePositionIndex;

    	
    	
    }   // end constructor
    


	@Override
	public void operateOn(Solution soln) {
		MySolution sol = (MySolution)soln;
    	Route routeInsert = sol.getRoute(routeIndexInsert);
    	Route routeDelete = sol.getRoute(routeIndexDelete);
    	
    	
    	System.out.println(this + "\nRotta iniziale prima della mossa: ");
    	for (int i=0; i< routeInsert.getCustomersLength(); i++)
    		System.out.printf("%d  ",routeInsert.getCustomer(i).getNumber());
    	System.out.println("\nRotta vicina prima della mossa: ");
    	for (int i=0; i< routeDelete.getCustomersLength(); i++)
    		System.out.printf("%d  ", routeDelete.getCustomer(i).getNumber());
    	
    	if(routeIndexInsert != routeIndexDelete)
    	{
    		//Insert the customer in a new route
    		insertBestTravel(routeInsert, insertedCustomer);
    		
    		//Delete the customer from the actual route
    		List<Customer> lista = routeDelete.getCustomers();
    		
    		boolean verifica = lista.remove(insertedCustomer);
    		System.out.println("\nCancellato dalla rotta 1?? " + verifica);
    		
    		//If a customer is deleted from one route, then one customer from the new route will be moved to the old one
    		if(verifica==true )
    		{
    			insertBestTravel(routeDelete, deletedCustomer);
    			List<Customer> lista2 = routeInsert.getCustomers();
    			boolean verifica2 = lista2.remove(deletedCustomer);
    			System.out.println("\nCancellato dalla rotta 2?? " + verifica2);
    		}
    	}
    			
    	System.out.println("Rotta vicina dopo mossa: ");
    	for (int i=0; i< routeDelete.getCustomersLength(); i++)
    		System.out.printf("%d  ", routeDelete.getCustomer(i).getNumber());

    	System.out.println("\nRotta iniziale dopo mossa: ");
    	for (int i=0; i< routeInsert.getCustomersLength(); i++)
    		System.out.printf("%d  ",routeInsert.getCustomer(i).getNumber());
		
	}

	@Override
	public int[] attributesDelete() {
		
		return new int[]{ 0, routeIndexDelete, insertedCustomerIndex, 0, 0};
	}

	@Override
	public int[] attributesInsert() {
		return new int[]{ 0, routeIndexInsert, insertedCustomerIndex, 0, 0};
	
		
	}
    private void insertBestTravel(Route route, Customer customerChosenPtr) {
		double minCost = Double.MAX_VALUE;
		double tempMinCost = Double.MAX_VALUE;
		int position = 0;
		
		// first position
		//verifica se è possibile mettere questo customer in prima posizione
		//se la finestra temporale del nuovo customer è minore di quella del customer in posizione zero
		//aggiorna il valore di "TEMPOMINCOST" e gli assegna:
		// tempo per andare dal deposito al customer attuale + tempo per andare dal customer attuale al customer in prima posizione
		// meno il tempo per andare dal deposito al customer in prima posizione
		if(customerChosenPtr.getEndTw() <= route.getCustomer(0).getEndTw()) 
		{
			tempMinCost = instance.getTravelTime(route.getDepotNr(), customerChosenPtr.getNumber()) 
						+ instance.getTravelTime(customerChosenPtr.getNumber(), route.getFirstCustomerNr()) 
						- instance.getTravelTime(route.getDepotNr(), route.getFirstCustomerNr());
			if(minCost > tempMinCost) {
				minCost = tempMinCost;
				position = 0;
			}
		}
			
		// at the end
		//se la timewindow dell'ultimo customer della rotta fin'ora inserito è minore della finestra attuale
		//poi come su il ragionamento
		if(route.getCustomer(route.getCustomersLength() - 1).getEndTw() <= customerChosenPtr.getEndTw()){
				tempMinCost = instance.getTravelTime(route.getLastCustomerNr(), customerChosenPtr.getNumber()) 
						+ instance.getTravelTime(customerChosenPtr.getNumber(), route.getDepotNr()) 
						- instance.getTravelTime(route.getLastCustomerNr(), route.getDepotNr());
				if(minCost > tempMinCost) {
					minCost = tempMinCost;
					position = route.getCustomersLength();
				}
			}
			
		// try between each customer
		// controlla le time window a coppie tra precedente e successivo confrontandole con le mie
		for(int i = 0; i < route.getCustomersLength() - 1; ++i) 
		{
			if(route.getCustomer(i).getEndTw() <= customerChosenPtr.getEndTw() && customerChosenPtr.getEndTw() <= route.getCustomer(i + 1).getEndTw()) {
				tempMinCost = instance.getTravelTime(route.getCustomerNr(i), customerChosenPtr.getNumber()) 
						+ instance.getTravelTime(customerChosenPtr.getNumber(), route.getCustomerNr(i + 1)) 
						- instance.getTravelTime(route.getCustomerNr(i), route.getCustomerNr(i + 1));
				if(minCost > tempMinCost) {
					minCost = tempMinCost;
					position = i + 1;
				}
			}
		}
//			return position;
		
		//inserisce la rotta nella posizione assegnata e in automatico gli altri elementi vengono shiftati
		route.addCustomer(customerChosenPtr, position);
		customerChosenPtr.setRouteIndex(route.getIndex());
		
	}

    /**
     * Set the insert position index of the move
     * (is done in objective function, for performance factor)
     * @param index
     */
    
    public void setInsertPositionIndex(int index) {
    	this.insertPositionIndex = index;
    }
    
	/**
	 * @return the customer
	 */
	public Customer getCustomer() {
		return insertedCustomer;
	}
	
	/**
	 * @return the customer number
	 */
	public int getCustomerNr() {
		return insertedCustomer.getNumber();
	}

	/**
	 * @param customer the customer to set
	 */
	public void setCustomer(Customer customer) {
		this.insertedCustomer = customer;
	}
    

	/**
	 * @return the deletePositionIndex
	 */
	public int getDeletePositionIndex() {
		return deletePositionIndex;
	}

	/**
	 * @param deletePositionIndex the deletePositionIndex to set
	 */
	public void setDeletePositionIndex(int deletePositionIndex) {
		this.deletePositionIndex = deletePositionIndex;
	}
    
	public int getRouteIndexInsert() {
		return routeIndexInsert;
	}



	public void setRouteIndexInsert(int routeIndexInsert) {
		this.routeIndexInsert = routeIndexInsert;
	}



	public int getRouteIndexDelete() {
		return routeIndexDelete;
	}



	public void setRouteIndexDelete(int routeIndexDelete) {
		this.routeIndexDelete = routeIndexDelete;
	}



	public Customer getDeletedCustomer() {
		return deletedCustomer;
	}



	public void setDeletedCustomer(Customer deletedCustomer) {
		this.deletedCustomer = deletedCustomer;
	}



	public int getDeletedCustomerIndex() {
		return deletedCustomerIndex;
	}



	public void setDeletedCustomerIndex(int deletedCustomerIndex) {
		this.deletedCustomerIndex = deletedCustomerIndex;
	}
    
    
    
}
