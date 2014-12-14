package com.TabuSearch;

import java.util.*;

import org.coinor.opents.*;

import com.mdvrp.Cost;
import com.mdvrp.Customer;
import com.mdvrp.Instance;
import com.mdvrp.Route;
import com.mdvrp.Vehicle;

@SuppressWarnings("serial")
public class MySolution extends SolutionAdapter{
	private static Instance instance;
	private static int iterationsDone;
	private Route[][] routes; // stores the routes to be modified on
	private Cost cost;		  // stores the total cost of the routes
	private double alpha;		// α
	private double beta;		// β
	private double gamma;		// γ
	private double delta;		// δ
	private double upLimit;
	private double resetValue;
	private int feasibleIndex;
	private int[][][] Bs;
	private List<MySwapMove> moves = new ArrayList<MySwapMove>();
	private List<Cost> costs = new ArrayList<Cost>();
	
	public MySolution() {} // Appease clone()

	public MySolution(Instance instance) {
		MySolution.setInstance(instance);
		cost = new Cost();
		initializeRoutes(instance);
		buildInitialRoutes1(instance);
		// used for input routes from file
		alpha 	= 1;
    	beta 	= 1;
    	gamma	= 1;
    	delta	= 0.005;
    	upLimit = 10000000;
    	resetValue = 0.1;
    	feasibleIndex = 0;
    	MySolution.setIterationsDone(0);
//    	Bs = new int[instance.getCustomersNr()][instance.getVehiclesNr()][1];
    	Bs = new int[instance.getCustomersNr()][instance.getVehiclesUsed()][1];
	}
	
	
	public Object clone()
    {   
        MySolution copy = (MySolution)super.clone();
        Route[][] routescopy = new Route[this.routes.length][];
        for (int i = 0; i < this.routes.length; ++i) {
        	routescopy[i] = new Route[this.routes[i].length];
        	for (int j = 0; j < this.routes[i].length; ++j)
        		routescopy[i][j] = new Route(this.routes[i][j]);
        }
        copy.routes        = routescopy;
        copy.cost          = new Cost(this.cost);
        copy.alpha         = this.alpha;
        copy.beta          = this.beta;
        copy.gamma         = this.gamma;
        copy.delta         = this.delta;
        copy.feasibleIndex = this.feasibleIndex;
        copy.Bs            = this.Bs;
        copy.moves         = this.moves;
        copy.costs         = this.costs;
        
        return copy;
    }   // end clone
		
	public void setParameters(double delta, double upLimit, double resetValue){
		this.delta =delta;
		this.upLimit = upLimit;
		this.resetValue = resetValue;
	}
	public void incrementBs(MySwapMove move){
		Bs[move.getCustomerNr()][move.getDeleteRouteNr()][move.getDeleteDepotNr()]++;
	}
	
	public int getBs(MySwapMove move){
		return Bs[move.getCustomerNr()][move.getDeleteRouteNr()][move.getDeleteDepotNr()];
	}
	
	public void addMove(MySwapMove move){
		moves.add(move);
	}
	
	public Route getRoute(int depot, int vehicle){
		return routes[depot][vehicle];
	}
	
	public Cost getCost(){
		return cost;
	}
	
	public int getBsOfMove(MySwapMove move) {
		return Bs[move.getCustomerNr()][move.getInsertRouteNr()][move.getInsertDepotNr()];
	}

	public void updateParameters(double a, double b, double g) {
    	// capacity violation test
    	if (a == 0) {
    		alpha = alpha / (1 + delta);
    	} else {
    		alpha = alpha * (1 + delta);
    		if(alpha > upLimit){
    			alpha = resetValue;
    		}
    	}
    	
    	// duration violation test    	
    	if (b == 0) {
    		beta = beta / (1 + delta);
    	} else {
    		beta = beta * (1 + delta);
    		if(beta > upLimit){
    			beta = resetValue;
    		}
    	}
    	
    	// time window violation test
    	if (g == 0) {
    		gamma = gamma / (1 + delta);
    	} else {
    		gamma = gamma * (1 + delta);
    		if(gamma > upLimit){
    			gamma = resetValue;
    		}
    	}
    	
    }
	
	/**
	 * Initialize the routes, assign vehicle and set the depot
	 * @param instance
	 */
	public void initializeRoutes(Instance instance) {
		routes = new Route[1][instance.getVehiclesUsed()];
		// Creation of the routes; each route starts at the depot
		for (int j = 0; j < instance.getVehiclesUsed(); ++j){
				// initialization of routes
				routes[0][j] = new Route();
				routes[0][j].setIndex(j);

				// add the depot as the first node to the route
				routes[0][j].setDepot(instance.getDepot());

				// set the cost of the route
				Cost cost = new Cost();
				routes[0][j].setCost(cost);

				// assign vehicle
				Vehicle vehicle = new Vehicle();
				vehicle.setCapacity(instance.getCapacity(0, 0));
				vehicle.setDuration(instance.getDuration(0, 0));
				routes[0][j].setAssignedVehicle(vehicle);		
			}
	}
	
	/**
	 * Build the initial routes
	 */
	public void buildInitialRoutes1(Instance instance) {
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
			route = routes[0][i];
			/*
			 * superCustomerPtr.getCapacity() returns the DEMAND of the CUSTOMER
			 * route.getCost().load is the total load we have so far (i.e the sum of the already served customer's load)
			 * route.getLoadAdmited() returns the CAPACITY of the VEHICLE (which is the same for every vehicle)
			 * superCustomerPtr.getServiceDuration() returns how much time we need to spend to serve the customer
			 * route.getDuration() is the time we have spent so far since when we departed from depot
			 * route.getDurationAdmited() is the maximum time we can travel before going back to depot.
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
			route = routes[0][i];
			for(int j=0; j < superCustomerPtr.getNeighbours().size();++j){
				customerChosenPtr = superCustomerPtr.getNeighbours().get(j);
				if (customerChosenPtr.getCapacity() + route.getCost().load <= route.getLoadAdmited() &&
					customerChosenPtr.getServiceDuration() + route.getDuration()  <= route.getDurationAdmited()){
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
				route = routes[0][randomRoute];
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
			
			// try between each customer
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

    /**
	 * this function calculates the cost of a route from scratch
	 * @param route
	 */
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
			route.getCost().setDurationViol(Math.max(0, route.getDuration() - route.getDurationAdmited()));
			
			// update total violation
			route.getCost().calculateTotalCostViol();
			
		} // end if route not empty
		
    } // end method evaluate route
	

	
	public String printMovesAndCosts(){
		StringBuffer print = new StringBuffer();
		print.append("------------------------<<--Solution Moves And Costs-----------------------");
		for (int i = 0; i < moves.size(); ++i){
			MySwapMove move = moves.get(i);
			Cost cost = costs.get(i);
			print.append("\n" + "Move: " + move.getDeleteRouteNr() + ", " + move.getCustomerNr() + ", " + move.getInsertRouteNr() + "\n");
			print.append(String.format("Cost: %7.2f, %7.2f, %7.2f, %7.2f, %7.2f\n", cost.travelTime, cost.loadViol, cost.durationViol, cost.waitingTime,  cost.twViol ));
			print.append("\n-------");
		}
		print.append("--------------------------Solution-->>-----------------------");
		return print.toString();

	}
	
	/**
	 * Print all the routes for each depots, day and vehicle
	 */
	public String printTimeWindows() {
		
		StringBuffer print = new StringBuffer();
		print.append("------------------------<<--Solution-----------------------");
		for (int i = 0; i < routes.length; ++i){
			print.append("\n" + "Depot: " + i + " ");
			for (int j = 0; j < routes[i].length; ++j){
				for(int k = 0; k < routes[i][j].getCustomersLength(); ++k){
					print.append(routes[i][j].getCustomerNr(k) + " " + routes[i][j].getCustomer(k).getEndTw() + " " + routes[i][j].getCustomer(k).getArriveTime() + " " + routes[i][j].getCustomer(k).getWaitingTime() + "\n");
				}
				print.append("D" + i + ": " + routes[i][j].getDepot().getEndTw()+ " " + routes[i][j].getReturnToDepotTime() + " " + routes[i][j].getDepotTwViol() + "\n");
				print.append("--------\n");
			}
			print.append("\n");
		}
		
		print.append("--------------------------Solution-->>-----------------------");
		return print.toString();
	}
	
	/**
	 * Print all the routes for each depots, day and vehicle
	 */
	public String toString() {
		
		StringBuffer print = new StringBuffer();
		print.append("------------------------<<--Solution-----------------------");
		for (int i = 0; i < routes.length; ++i){
			print.append("\n" + "Depot: " + i + "\n");
			for (int j = 0; j < routes[i].length; ++j){
				print.append(routes[i][j].printRoute());
			}
			print.append("\n");
		}
		
		print.append("Total Cost\n" + cost );
		for (int i = 0; i < routes.length; ++i){
			for (int j = 0; j < routes[i].length; ++j){
				print.append(routes[i][j].printRouteCost());
			}
			print.append("\n");
		}
		print.append("--------------------------Solution-->>-----------------------");
		return print.toString();
	}
	
	
	public void printParameters() {
		System.out.println("alpha=" + alpha + " beta=" + beta + " gamma=" + gamma);
	}

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

	public void addLoad(double load) {
		cost.load += load;
		
	}

	public void addServiceTime(double serviceTime) {
		cost.serviceTime += serviceTime;
		
	}

	public void addWaitingTime(double waitingTime) {
		cost.waitingTime += waitingTime;
		
	}

	public int getDepotsNr() {
		return routes.length;
	}

	public int getDepotVehiclesNr(int depot) {
		return routes[depot].length;
	}

	public Route[][] getRoutes() {
		return routes;
	}

	public void setCost(Cost cost) {
		this.cost = cost;
		
	}

	public void setRoutes(Route[][] routes) {
		this.routes = routes;
		
	}

	public void setFeasibleIndex(int feasibleIndex) {
		this.feasibleIndex = feasibleIndex;
		
	}

	public int getFeasibleIndex() {
		return feasibleIndex;
	}

	/**
	 * @return the instance
	 */
	public static Instance getInstance() {
		return instance;
	}

	/**
	 * @param instance the instance to set
	 */
	public static void setInstance(Instance instance) {
		MySolution.instance = instance;
	}

	/**
	 * @return the iterationsDone
	 */
	public static int getIterationsDone() {
		return iterationsDone;
	}
	
	/**
	 * @param iterationsDone the iterationsDone to set
	 */
	public static void setIterationsDone(int iterationsDone) {
		MySolution.iterationsDone = iterationsDone;
	}
	
	

}