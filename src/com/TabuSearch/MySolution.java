package com.TabuSearch;

import java.util.Random;

import org.coinor.opents.SolutionAdapter;

import com.mdvrp.Instance;
import com.mdvrp.Route;
import com.mdvrp.Customer;
import com.mdvrp.Cost;
import com.mdvrp.Vehicle;

@SuppressWarnings("serial")
public class MySolution extends SolutionAdapter{

	private static Instance instance;
	private Route[] route;
	private Cost cost; //Save the tot cost of the route
	
	public MySolution(){} //This is needed otherwise java gives random errors.. YES we love java <3
	public MySolution(Instance instance) {
		MySolution.setInstance(instance);
		cost = new Cost();
		initializeRoute(instance);
		buildInitialRoute(instance);
	}
	
	//This is needed for tabu search
	public Object clone(){
		MySolution copy = (MySolution) super.clone();
		copy.cost = new Cost(this.cost);
		
		Route[] copyRoute = new Route[this.route.length];
		for(int i=0; i < this.route.length;++i)
			copyRoute[i] = new Route(this.route[i]);
		copy.route = copyRoute;
		
		return copy;
	}
	
	public void initializeRoute(Instance instance) {
		route = new Route[instance.getVehiclesUsed()];
		// Creation of the routes; each route starts at the depot
		for (int i = 0; i < instance.getVehiclesUsed(); ++i){
			// initialization of routes
			route[i] = new Route();
			route[i].setIndex(i);

			// add the depot as the first node to the route
			route[i].setDepot(instance.getDepot());

			// set the cost of the route
			Cost cost = new Cost();
			route[i].setCost(cost);

			// assign vehicle
			Vehicle vehicle = new Vehicle();
			/*
			 * The following is a method that supposes that there are multiple depots, so instead of
			 * changing everything in the mdvrp package, we just pass 0 as depot.
			 */
			vehicle.setCapacity(instance.getCapacity(0,0)); 
			route[i].setAssignedVehicle(vehicle);		
		}
	}
	
	public void buildInitialRoute(Instance instance) {
		Route route; // stores the pointer to the current route
		Customer customerChosenPtr; // stores the pointer to the customer chosen from depots list assigned customers
		Customer superCustomerPtr; //Store the pointer to the super customers
		Random random = new Random();
		int superCustomer;
		int i;
		/*
		 * Customers of the DEPOT are ordered from the most distant to the closet (closest is
		 * at the end of the array).
		 * After knowing how many starting vehicles we want to use, we assign to them the
		 * distant customers.
		 * Then for each distant customer (which from now on we will call "super customer")
		 * we create its neighbourhood of near customers that our vehicle will visit. 
		 */
		for(i=0; i<instance.getVehiclesUsed();++i){
			superCustomerPtr = instance.getDepot().getAssignedCustomer(i);
			route = this.route[i];
			/*
			 * superCustomerPtr.getCapacity() returns the DEMAND of the CUSTOMER
			 * route.getCost().load is the total load we have so far (i.e the sum of the already served customer's load)
			 * route.getLoadAdmited() returns the CAPACITY of the VEHICLE (which is the same for every vehicle)
			 * superCustomerPtr.getServiceDuration() returns how much time we need to spend to serve the customer
			 * route.getDuration() is the time we have spent so far since when we departed from depot
			 */
				insertBestTravel(instance, route, superCustomerPtr);
				evaluateRoute(route);
		}
		//For the most distant customers, compute their neighbourhood
		for(i=0;i<instance.getVehiclesUsed();++i){
			superCustomerPtr = instance.getDepot().getAssignedCustomer(i);
			superCustomerPtr.generateNeighbours(instance.getDepot().getAssignedcustomers(), instance.getVehiclesUsed());
		}
		//Check that all customers belong to a neighbourhood
		for(i=instance.getVehiclesUsed(); i < instance.getDepot().getAssignedCustomersNr();++i){
			customerChosenPtr = instance.getDepot().getAssignedCustomer(i);
			//If a customer is not taken by anyone, assign it randomly
			if(!customerChosenPtr.getIsTaken()){
				superCustomer = random.nextInt((instance.getVehiclesUsed() - 0) + 1);
				superCustomerPtr = instance.getDepot().getAssignedCustomer(superCustomer);
			}
		}
		//Now add the neighbours to the respective routes
		for(i=0;i<instance.getVehiclesUsed();++i){
			superCustomerPtr = instance.getDepot().getAssignedCustomer(i);
			route = this.route[i];
			for(int j=0; j < superCustomerPtr.getNeighbours().size();++j){
				customerChosenPtr = superCustomerPtr.getNeighbours().get(j);
				if (customerChosenPtr.getCapacity() + route.getCost().load <= route.getLoadAdmited() &&
					customerChosenPtr.getWaitingTime()+route.getDuration() <= route.getCost().returnToDepotTime){
					insertBestTravel(instance, route, customerChosenPtr);
					evaluateRoute(route);
				}				
			}
		}
		//Check if all customers belong to a neighbourhood. If not, add it to one route randomly
		int randomRoute;
		for(i=instance.getVehiclesUsed(); i<instance.getCustomersNr();++i){
			customerChosenPtr = instance.getDepot().getAssignedCustomer(i);
			if(!customerChosenPtr.getIsTaken()){
				randomRoute = random.nextInt(instance.getVehiclesUsed());
				route = this.route[randomRoute];
				insertBestTravel(instance,route,customerChosenPtr);
				evaluateRoute(route);
			}
		}
	}
	private void insertBestTravel(Instance instance, Route route, Customer customerChosenPtr) {
		double minCost = Double.MAX_VALUE;
		double tempMinCost = Double.MAX_VALUE;
		int position = 0;
		if(route.isEmpty()){
			// add on first position
			position = 0;		

			/*
			 * Mark the customer as one of the super customers.
			 * We do this because all the customers that are set as "distant"
			 * will generate their neighbours
			 */
			customerChosenPtr.setIsDistant();
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
//			route.getCost().setDurationViol(Math.max(0, route.getDuration() - route.getDurationAdmited()));
			
			// update total violation
			route.getCost().calculateTotalCostViol();
			
		} // end if route not empty
		
    } // end method evaluate route
	
	public static Instance getInstance(){
		return instance;
	}
	
	public static void setInstance(Instance instance){
		MySolution.instance = instance;
	}
	public Route[] getRoutes() {
		return route;
	}
	public Route getRoute(int index){
		return route[index];
	}
	public void setRoute(Route[] route) {
		this.route = route;
	}
}
