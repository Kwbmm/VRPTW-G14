﻿package com.vrp;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.Scanner;

/**
 * Instance class holds all the information about the problem, customers, depots, vehicles.
 * It offers functions to grab all the data from a file print it formated and all the function
 * needed for the initial solution.
 */
public class Instance {
	private int vehiclesNr;
	private int customersNr;
	private int daysNr = 1;
	private ArrayList<Customer> customers 	= new ArrayList<>(); 		// vector of customers;
	private ArrayList<Customer> allCustomers 	= new ArrayList<>();
	private Depot depot;
	private double[][] capacities;
	private double[][] distances;
	private Random random 					= new Random();
	private Parameters parameters;

	public Instance(Parameters parameters) 
	{
		this.setParameters(parameters);
		// set the random seed if passed as parameter
		if(parameters.getRandomSeed() != -1)
			random.setSeed(parameters.getRandomSeed());
	}

	/**
	 * Returns the time necessary to travel from node 1 to node 2
	 * @param node1
	 * @param node2
	 * @return
	 */
	public double getTravelTime(int v1, int v2) {
		return this.distances[v1][v2];
	}

	/**
	 * Read from file the problem data: D and Q, customers data
	 * and depots data. After the variables are populated
	 * calculates the distances, assign customers to depot
	 * and calculates angles
	 * @param filename
	 */
	public void populateFromFile(String filename) {
		try {

			Scanner in = new Scanner(new FileReader(parameters.getCurrDir() + "/input/" + filename));

			// skip useless lines
			in.nextLine(); // skip filename
			in.nextLine(); // skip empty line
			in.nextLine(); // skip vehicle line
			in.nextLine();
			vehiclesNr	= in.nextInt(); //Amount of vehicles

			// read D and Q
			capacities	= new double[1][daysNr];
			capacities[0][0] = in.nextInt(); //Capacity of all the vehicles

			// skip useless lines (Labels and white lines)
			in.nextLine();
			in.nextLine();
			in.nextLine();
			in.nextLine();
			in.nextLine();

			// read depots data
			depot = new Depot();
			depot.setNumber(in.nextInt());
			depot.setXCoordinate(in.nextDouble());
			depot.setYCoordinate(in.nextDouble());
			in.nextDouble(); //Skip demand
			depot.setStartTW(in.nextInt());
			depot.setEndTW(in.nextInt());
			in.nextDouble(); //Skip service time

			// read customers data
			customersNr = 0;
			while(in.hasNextInt())
			{					
				Customer customer = new Customer();
				customer.setNumber(in.nextInt() - 1);
				customer.setXCoordinate(in.nextDouble());
				customer.setYCoordinate(in.nextDouble());
				customer.setCapacity(in.nextDouble());
				customer.setStartTw(in.nextInt());
				customer.setEndTw(in.nextInt());
				customer.setServiceDuration(in.nextDouble());

				// add customer to customers list
				customers.add(customer);
				allCustomers.add(customer);
				customersNr++;
			}// end for customers
			in.close();

			depot.setNumber(customersNr);

			if(parameters.getTabuTenure() == -1)
				parameters.setTabuTenure((int)(Math.sqrt(getCustomersNr())));

			calculateDistances();
			assignCustomersToDepots();	
			calculateAngles();
			Collections.sort(customers, new CompareAngle());
		} catch (FileNotFoundException e) {
			// File not found
			System.out.println("File not found!");
			System.exit(-1);
		}
	}

	public void calculateAngles()
	{
		for (int i = 0; i < customers.size(); ++i) {
			double angle = Math.atan2(customers.get(i).getYCoordinate() - depot.getYCoordinate(), customers.get(i).getXCoordinate() - depot.getXCoordinate());
			customers.get(i).setAngleFromDepot(angle);
		}
	}

	/**
	 * Get the depot number found at the passed position
	 * @param index
	 * @return
	 */
	public int getNumberOfDepotAt(int index) {
		return depot.getNumber();
	}

	/**
	 * Get the customer number found at the passed position
	 * @param index
	 * @return
	 */
	public int getNumberOfCustomerAt(int index) {
		return customers.get(index).getNumber();
	}


	/**
	 * For each customer set the depot and assign to the depot the customers
	 */
	public void assignCustomersToDepots() {
		for (int i = 0; i < customersNr; ++i){
			customers.get(i).setAssignedDepot(depot);
			depot.addAssignedCustomer(customers.get(i));
		}
	}

	/**
	 * Print for the list of depots their number on a row separated by space
	 * Used for debugging
	 */
	public String printDepotsNumber(ArrayList<Depot> depots) {
		StringBuffer print = new StringBuffer();
		print.append("Depots:");
		for (int i = 0; i < depots.size(); ++i) {
			print.append(" " + depots.get(i).getNumber());
		}
		print.append("\n");
		return print.toString();
	}

	/**
	 * Calculate the symmetric euclidean matrix of costs
	 */
	public void calculateDistances() 
	{
		distances = new double[customersNr + 1][customersNr + 1];
		for (int i = 0; i  < customersNr + 1; ++i)
		{
			for (int j = 0; j < customersNr +  1; ++j)
			{
				//case both customers
				if(i < customersNr && j < customersNr)
				{
					distances[i][j] = Math.sqrt(Math.pow(customers.get(i).getXCoordinate() - customers.get(j).getXCoordinate(), 2)
							+ Math.pow(customers.get(i).getYCoordinate() - customers.get(j).getYCoordinate(), 2));
					distances[i][j] = Math.floor(distances[i][j] * 10) / 10;
					distances[j][i] = distances[i][j];

					// case customer and depot
				}else if(i < customersNr && j >= customersNr)
				{
					distances[i][j] = Math.sqrt(Math.pow(customers.get(i).getXCoordinate() - depot.getXCoordinate(), 2)
							+ Math.pow(customers.get(i).getYCoordinate() - depot.getYCoordinate(), 2));
					distances[i][j] = Math.floor(distances[i][j] * 10) / 10;
					distances[j][i] = distances[i][j];
					customers.get(i).setDistanceFromDepot(distances[i][j]);
				}
			}
		}
	}
	public ArrayList<Customer> calculateAnglesToCustomer( Customer c) {

		@SuppressWarnings("unchecked")
		ArrayList<Customer> list = (ArrayList<Customer>)allCustomers.clone(); //get all customers

		Customer swap = new Customer();
		for(int i=0; i<list.size(); i++)
		{
			Customer swap1 = list.get(i);
			if(swap1.getNumber() == c.getNumber())
			{
				swap = swap1;
				list.set(i, list.get(0));
				list.set(0, swap); 	// set the customer considered in the first position
			}
		}

		for (int i = 1; i < list.size(); ++i) {
			double angle = Math.atan2(list.get(i).getYCoordinate() - c.getYCoordinate(), list.get(i).getXCoordinate() - c.getXCoordinate());
			list.get(i).setAngleToCustomer(angle);
		}

		Quick.sort(list);
		return list;
	}

	/**
	 * @param costs the costs to set
	 */
	public void setCosts(double[][] costs) {
		this.distances = costs;
	}


	/**
	 * @return the parameters
	 */
	public Parameters getParameters() {
		return parameters;
	}


	/**
	 * @param parameters the parameters to set
	 */
	public void setParameters(Parameters parameters) {
		this.parameters = parameters;
	}


	/**
	 * @return the vehiclesNr
	 */
	public int getVehiclesNr() {
		return vehiclesNr;
	}


	/**
	 * @param vehiclesNr the vehiclesNr to set
	 */
	public void setVehiclesNr(int vehiclesNr) {
		this.vehiclesNr = vehiclesNr;
	}

	/**
	 * @return the customersNr
	 */
	public int getCustomersNr() {
		return customersNr;
	}


	/**
	 * @param customersNr the customersNr to set
	 */
	public void setCustomersNr(int customersNr) {
		this.customersNr = customersNr;
	}

	/**
	 * @return the daysNr
	 */
	public int getDaysNr() {
		return daysNr;
	}


	/**
	 * @param daysNr the daysNr to set
	 */
	public void setDaysNr(int daysNr) {
		this.daysNr = daysNr;
	}

	public ArrayList<Customer> getCustomers() {
		return allCustomers;
	}

	public void setCustomers(ArrayList<Customer> customers) {
		this.allCustomers = customers;
	}

	public ArrayList<Customer> getSortedCustomers()
	{
		return customers;
	}

	public Depot getDepot(){
		return depot;
	}


	public double getCapacity(int i, int j) {
		return capacities[i][j];
	}


	/**
	 * @return the random
	 */
	public Random getRandom() {
		return random;
	}

	public double[][] getDistances() {
		return distances;
	}

	public void setDistances(double[][] distances) {
		this.distances = distances;
	}

	/**
	 * @param random the random to set
	 */
	public void setRandom(Random random) {
		this.random = random;
	}

	/**
	 * @return the precision
	 */
	public double getPrecision(){
		return parameters.getPrecision();
	}

	/**
	 * @return all the customers as string
	 */

	public String printCustomers() {
		StringBuffer print = new StringBuffer();
		for (int i = 0; i < customersNr; ++i) {
			print.append(customers.get(i));
		}
		return print.toString();
	}
}
