package com.TabuSearch;

import java.util.Random;
import java.util.ArrayList;

import org.coinor.opents.SolutionAdapter;

import com.vrp.Cost;
import com.vrp.Customer;
import com.vrp.Instance;
import com.vrp.Route;
import com.vrp.Vehicle;

@SuppressWarnings("serial")
public class MySolution extends SolutionAdapter{

	private static Instance instance;
	private ArrayList<Route> route;
	private Cost cost; //Save the tot cost of the route
	private double alpha;		
	private double beta;		
	private double gamma;		
	private double delta;		
	private double upLimit;
	private double resetValue;

	public MySolution(){} //This is needed otherwise java gives random errors.. YES we love java <3

	public MySolution(Instance instance) {
		MySolution.setInstance(instance);
		cost = new Cost();
		route = new ArrayList<Route>();
		initializeRoutes(instance);
		buildInitialRoutes(instance);
		alpha 	= 1;
		gamma	= 1;
		delta	= 0.005;
		upLimit = 10000000;
		resetValue = 0.1;
	}

	public void initializeRoutes(Instance instance) {
		route = new ArrayList<Route>(instance.getVehiclesNr());
		for (int j = 0; j < instance.getVehiclesNr(); ++j)
		{
			// initialization of routes
			Route r = new Route();
			r.setIndex(j);
			// add the depot as the first node to the route
			r.setDepot(instance.getDepot());
			// set the cost of the route
			Cost cost = new Cost();
			r.setCost(cost);
			// assign vehicle
			Vehicle vehicle = new Vehicle();
			vehicle.setCapacity(instance.getCapacity(0, 0));
			r.setAssignedVehicle(vehicle);
			//add the new route into the arrayList
			route.add(r);
		}
	}

	//This is needed for tabu search
	public Object clone(){
		MySolution copy = (MySolution) super.clone();
		copy.cost = new Cost(this.cost);
		copy.route = new ArrayList<Route>(this.route);
		copy.alpha         = this.alpha;
		copy.beta          = this.beta;
		copy.gamma         = this.gamma;
		copy.delta         = this.delta;
		return copy;
	}

	public Cost getCost(){
		return cost;
	}

	public void updateParameters(double a, double b) {
		// capacity violation test
		if (a == 0) {
			alpha = alpha / (1 + delta);
		} else {
			alpha = alpha * (1 + delta);
			if(alpha > upLimit){
				alpha = resetValue;
			}
		}
		// time window violation test
		if (b == 0) {
			gamma = gamma / (1 + delta);
		} else {
			gamma = gamma * (1 + delta);
			if(gamma > upLimit){
				gamma = resetValue;
			}
		}
	}

	public void buildInitialRoutes(Instance instance) 
	{
		Route r; // stores the pointer to the current route
		ArrayList<Customer> list = instance.getSortedCustomers();
		Customer customer;
		double totalDemand = 0;
		int customerNr = 0;
		Random random = new Random();

		int minCustomersPerRoute = 4;
		int maxCustomersPerRoute = 9;
		int customerNrThreshold = random.nextInt(maxCustomersPerRoute-minCustomersPerRoute+1)+minCustomersPerRoute;
		for(int i=0; i<route.size(); i++)
		{
			r = route.get(i);
			totalDemand = 0;
			customerNr = 0;
			for(int j=0; j<list.size(); j++)
			{
				customer = list.get(j);
				if(!customer.getIsTaken() && customerNr<customerNrThreshold)
				{
					if(totalDemand+customer.getCapacity()<=r.getLoadAdmited())
					{
						totalDemand = totalDemand + customer.getCapacity();
						customerNr++;
						insertBestTravel(instance, r, customer);
						evaluateRoute(r);
					}
				}
			}
		}
		trimRoutes(this.route);
	}

	private void trimRoutes(ArrayList<Route> route){
		Route r;
		for(int i=0; i < route.size();i++){
			r = route.get(i);
			if(r.getCustomersLength() == 0){
				route.remove(i);
				i--;
			}
		}
		route.trimToSize();
	}

	private void insertBestTravel(Instance instance, Route route, Customer customerChosenPtr) {
		double minCost = Double.MAX_VALUE;
		double tempMinCost = Double.MAX_VALUE;
		int position = 0;
		if(route.isEmpty()){
			// add on first position
			position = 0;
		}else {
			// first position
			if(customerChosenPtr.getEndTw() <= route.getCustomer(0).getEndTw()) {
				tempMinCost = instance.getTravelTime(route.getDepotNr(), customerChosenPtr.getNumber()) 
						+ instance.getTravelTime(customerChosenPtr.getNumber(), route.getFirstCustomerNr()) 
						- instance.getTravelTime(route.getDepotNr(), route.getFirstCustomerNr());
				if(minCost > tempMinCost) {
					minCost = tempMinCost;
					position = 0;
				}
			}
			// at the end
			if(route.getCustomer(route.getCustomersLength() - 1).getEndTw() <= customerChosenPtr.getEndTw()){
				tempMinCost = instance.getTravelTime(route.getLastCustomerNr(), customerChosenPtr.getNumber()) 
						+ instance.getTravelTime(customerChosenPtr.getNumber(), route.getDepotNr()) 
						- instance.getTravelTime(route.getLastCustomerNr(), route.getDepotNr());
				if(minCost > tempMinCost) {
					minCost = tempMinCost;
					position = route.getCustomersLength();
				}
			}
			// Try between customers but exclude the last one (case done above)
			for(int i = 0; i < route.getCustomersLength() - 1; ++i) {
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
		}
		customerChosenPtr.setRouteIndex(route.getIndex());
		customerChosenPtr.setIsTaken(true);
		route.addCustomer(customerChosenPtr, position);
	}

	private void evaluateRoute(Route route) {
		double totalTime = 0;
		double waitingTime = 0;
		double twViol = 0;
		Customer customerK;
		route.initializeTimes();
		// do the math only if the route is not empty
		if(!route.isEmpty()){
			// sum distances between each node in the route
			for (int k = 0; k < route.getCustomersLength(); ++k){
				// get the actual customer
				customerK = route.getCustomer(k);
				// add travel time to the route
				if(k == 0){
					route.getCost().travelTime += getInstance().getTravelTime(route.getDepotNr(), customerK.getNumber());
					totalTime += getInstance().getTravelTime(route.getDepotNr(), customerK.getNumber());
				}else{
					route.getCost().travelTime += getInstance().getTravelTime(route.getCustomerNr(k -1), customerK.getNumber());
					totalTime += getInstance().getTravelTime(route.getCustomerNr(k -1), customerK.getNumber());
				} // end if else			
				customerK.setArriveTime(totalTime);
				// add waiting time if any
				waitingTime = Math.max(0, customerK.getStartTw() - totalTime);
				route.getCost().waitingTime += waitingTime;
				// update customer timings information
				customerK.setWaitingTime(waitingTime);
				totalTime = Math.max(customerK.getStartTw(), totalTime);
				// add time window violation if any
				twViol = Math.max(0, totalTime - customerK.getEndTw());
				route.getCost().addTWViol(twViol);
				customerK.setTwViol(twViol);
				// add the service time to the total
				totalTime += customerK.getServiceDuration();
				// add service time to the route
				route.getCost().serviceTime += customerK.getServiceDuration();
				// add capacity to the route
				route.getCost().load += customerK.getCapacity();
			} // end for customers
			// add the distance to return to depot: from last node to depot
			totalTime += getInstance().getTravelTime(route.getLastCustomerNr(), route.getDepotNr());
			route.getCost().travelTime += getInstance().getTravelTime(route.getLastCustomerNr(), route.getDepotNr());
			// add the depot time window violation if any
			twViol = Math.max(0, totalTime - route.getDepot().getEndTw());
			route.getCost().addTWViol(twViol);
			// update route with timings of the depot
			route.setDepotTwViol(twViol);
			route.setReturnToDepotTime(totalTime);
			route.getCost().setLoadViol(Math.max(0, route.getCost().load - route.getLoadAdmited()));
			// update total violation
			route.getCost().calculateTotalCostViol();			
		} // end if route not empty
	} // end method evaluate route

	public double getAlpha() {
		return alpha;
	}

	public double getBeta() {
		return beta;
	}

	public double getGamma() {
		return gamma;
	}

	public void addTravelTime(double travelTime){
		cost.travelTime += travelTime;
	}

	public void addServiceTime(double serviceTime) {
		cost.serviceTime += serviceTime;	
	}

	public void addWaitingTime(double waitingTime) {
		cost.waitingTime += waitingTime;
	}

	public void setCost(Cost cost) {
		this.cost = cost;	
	}

	public static Instance getInstance(){
		return instance;
	}

	public static void setInstance(Instance instance){
		MySolution.instance = instance;
	}

	public ArrayList<Route> getRoutes() {
		return route;
	}

	public Route getRoute(int index){
		return route.get(index);
	}

	public int getRouteNr(){
		return route.size();
	}

	public void setRoute(ArrayList<Route> route) {
		this.route = route;
	}

	public void deleteFromSolution (Route route){
		this.route.remove(route);
	}
}