package com.TabuSearch;

import java.util.Random;
import java.util.ArrayList;

import org.coinor.opents.SolutionAdapter;

import com.mdvrp.Instance;
import com.mdvrp.Route;
import com.mdvrp.Customer;
import com.mdvrp.Cost;
import com.mdvrp.Vehicle;

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
		initializeRoutesBis(instance);
		buildInitialRoutesBis(instance);
//		initializeRoute(instance);
//		buildInitialRoute(instance);
		alpha 	= 1;
    	beta 	= 1;
    	gamma	= 1;
    	delta	= 0.005;
    	upLimit = 10000000;
    	resetValue = 0.1;
    	

	}
	public void initializeRoutesBis(Instance instance) {
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
	
	public void update (boolean a, boolean b){
		if(a == true){ //capacity violated 10 times
			alpha= alpha*2;
			if(alpha > upLimit){
    			alpha = resetValue;};}
		else {
			alpha= alpha/2;
			}
		
		if(b == true) {//tw violated 10 times;
			gamma = gamma*2;

			//System.out.println(gamma);
			}
		else {
			gamma = gamma/2;
			if(gamma > upLimit){
    			gamma = resetValue;
    		}
			//System.out.println(gamma);
			}
	}

	/*public void updateParameters(double a, double b) {
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
	*/
	/*
	public void buildInitialRoutesBis(Instance instance) 
	{
		Route r; // stores the pointer to the current route
		ArrayList<Customer> list = instance.getSortedCustomers();
		Customer customer;
		double totalDemand = 0;
		int customerNr = 0;
		int position;
		boolean good = false;
		int k;
		
		for(int i=0; i<route.size(); i++)
		{
			r = route.get(i);
			totalDemand = 0;
			customerNr = 0;
		//	System.out.println("RottaValutata: " + r.getIndex());
			for(int j=0; j<list.size(); j++)
			{
				customer = list.get(j);
				if(!customer.getIsTaken() && customerNr<=9)
				{
					if(totalDemand+customer.getCapacity()<=r.getLoadAdmited())
					{
						totalDemand = totalDemand + customer.getCapacity();
						customerNr++;
						//position = 
						insertBestTravel(instance, r, customer);
						evaluateRoute(r);
						good = evaluateRouteBis(instance, r, customer, position);
//						if(good)
//						{
//							customer.setRouteIndex(r.getIndex());
//							customer.setIsTaken(true);
//							r.addCustomer(customer, position);
//						}
					}
				}
			}
		}
		
//		for(Customer c : list)
//		{
//			System.out.println("Customer: " + c.getNumber() + "\tAngle: " + c.getAngleFromDepot());
//			System.out.println("Capacit�: " + c.getCapacity());
//		}
//		System.exit(-1);
	}*/
	public void buildInitialRoutesBis(Instance instance) 
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
	/*private boolean evaluateRouteBis(Instance instance, Route route, Customer customer, int position)
	{
		boolean good = false;
		
		return good;
	}*/
	
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
			//customerChosenPtr.setIsDistant();
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
		//return position;
//		System.out.println("Rotta: " + route.getIndex());
//		System.out.println("Customer: "+ customerChosenPtr.getNumber() + "Assegnazione Indice: " + customerChosenPtr.getRouteIndex());
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
	
	
	/*	public void initializeRoute(Instance instance) {
	Route tempRoute;
	// Creation of the routes; each route starts at the depot
	for (int i = 0; i < instance.getVehiclesUsed(); ++i){
		// initialization of routes
		this.route.add(new Route());
		tempRoute = this.route.get(route.size()-1);
		tempRoute.setIndex(i);
		// add the depot as the first node to the route
		tempRoute.setDepot(instance.getDepot());

		// set the cost of the route
		Cost cost = new Cost();
		tempRoute.setCost(cost);

		// assign vehicle
		Vehicle vehicle = new Vehicle();
		
		 * The following is a method that supposes that there are multiple depots, so instead of
		 * changing everything in the mdvrp package, we just pass 0 as depot.
		 
		vehicle.setCapacity(instance.getCapacity(0,0)); 
		tempRoute.setAssignedVehicle(vehicle);		
	}
}*/
	/*public void addNewSingleRoute(Instance instance){
	Route tempRoute;
	Route lastKnownRoute = this.route.get(this.route.size()-1);
	int oldVehiclesUsed = instance.getVehiclesUsed();
	this.route.add(new Route());
	tempRoute = this.route.get(route.size()-1);
	tempRoute.setIndex(lastKnownRoute.getIndex()+1);
	// add the depot as the first node to the route
	tempRoute.setDepot(instance.getDepot());

	// set the cost of the route
	Cost cost = new Cost();
	tempRoute.setCost(cost);

	// assign vehicle
	Vehicle vehicle = new Vehicle();
	instance.setVehiclesUsed(oldVehiclesUsed+1);
	
	 * The following is a method that supposes that there are multiple depots, so instead of
	 * changing everything in the mdvrp package, we just pass 0 as depot.
	 
	vehicle.setCapacity(instance.getCapacity(0,0)); 
	tempRoute.setAssignedVehicle(vehicle);
}*/

/*public void buildInitialRoute(Instance instance) {
	Route route; // stores the pointer to the current route
	Customer customerChosenPtr; // stores the pointer to the customer chosen from depots list assigned customers
	Customer superCustomerPtr; //Store the pointer to the super customers
	Random random = new Random();
	int i;
	
	 * Customers of the DEPOT are ordered from the most distant to the closet (closest is
	 * at the end of the array).
	 * After knowing how many starting vehicles we want to use, we assign to them the
	 * distant customers.
	 * Then for each distant customer (which from now on we will call "super customer")
	 * we create its neighbourhood of near customers that our vehicle will visit. 
	 
	for(i=0; i<instance.getVehiclesUsed();++i){
		superCustomerPtr = instance.getDepot().getAssignedCustomer(i);
		route = this.route.get(i);
		insertBestTravel(instance, route, superCustomerPtr);
		evaluateRoute(route);
		superCustomerPtr.generateNeighbours(instance.getDepot().getAssignedcustomers(), instance.getVehiclesUsed());
		for(int j=0; j < superCustomerPtr.getNeighbours().size();++j){ //Try to add the customer to the route
			customerChosenPtr = superCustomerPtr.getNeighbours().get(j);
			if (customerChosenPtr.getCapacity() + route.getCost().load <= route.getLoadAdmited()){
				Customer k= route.getCustomer(route.getCustomersLength()-1);
				if(customerChosenPtr.getStartTw() > k.getStartTw()){
				insertBestTravel(instance, route, customerChosenPtr);
				evaluateRoute(route);
			}}
			else{
				superCustomerPtr.getNeighbours().remove(customerChosenPtr);
				customerChosenPtr.setIsTaken(false); //Remove the marking
				customerChosenPtr.setDistanceFromSupercustomer(0);
			}
		}
	}
	
	 * At this point the customers left alone are:
	 * - Those not belonging to any neighbourhood bc they're to far from every superCustomer.
	 * - Those not belonging to any neighbourhood bc they're violating capacity constraints
	 * All these customers have IsTaken variable = false
	 * We try to merge them together
	 *
	 * Here down we try to add a new vehicles and a new route (if available).
	 * If we can, then customerChosenPtr becomes a superCustomer: we recompute the distance from
	 * the other supercustomers (instance.setSCustomersMeanDistance()), generate its new neighbourhood,
	 * insert it into our new route, evaluate the route and then for its neighbourhood
	 * we try to add each neighbour to the route. If this fails, we remove the customer from the
	 * neighbourhood and remove the marking.
	 
	for(i=instance.getVehiclesUsed();i<instance.getDepot().getAssignedCustomersNr(); ++i){
		customerChosenPtr = instance.getDepot().getAssignedCustomer(i);
		if(!customerChosenPtr.getIsTaken()){ //If the customer is not taken
			if(instance.getVehiclesUsed()< instance.getVehiclesNr()){ //If there are vehicles available, we generate new routes
				addNewSingleRoute(instance);
				route = this.route.get(this.route.size()-1);
				customerChosenPtr.setIsDistant(); //Mark the customer as supercustomer
				instance.setSCustomersMeanDistance();
				customerChosenPtr.generateNeighbours(instance.getDepot().getAssignedcustomers(), instance.getVehiclesUsed());
				insertBestTravel(instance,route,customerChosenPtr);
				evaluateRoute(route);
				//Loop through all the neighbours of this scustomer and try to add them to the route
				for(int j=0;j< customerChosenPtr.getNeighbours().size();++j){
					Customer subCustomerPtr = customerChosenPtr.getNeighbours().get(j);
					if(subCustomerPtr.getCapacity()+route.getCost().load <= route.getLoadAdmited()){
						//if(subCustomerPtr.getStartTw()> subCustomerPtr.ge){
						insertBestTravel(instance,route,subCustomerPtr);
						evaluateRoute(route);
						//}
					}
					else{ //These are very unlucky customers, they are all left alone... :(
						boolean returnVal;
						returnVal = customerChosenPtr.getNeighbours().remove(subCustomerPtr);
						subCustomerPtr.setIsTaken(false); //Remove the marking
						subCustomerPtr.setDistanceFromSupercustomer(0);
						System.out.println("2: "+returnVal);
					}
				}
			}
			else{ //We don't have any more vehicles, we add the customer to a random route
				int randomRouteNr = random.nextInt(this.route.size());
				route = this.route.get(randomRouteNr);
				customerChosenPtr.setIsTaken(true); //Mark it anyway even if it's not into a neighbourhood
				insertBestTravel(instance,route,customerChosenPtr);
				evaluateRoute(route);
			}
		}
	}
	
	
	 * Clearly we have an issue and we are forgetting, somewhere, to remove the marking isTaken
	 * from the customers.
	 * However, by using routeindex, we can easily add the missing customers to the routes
	 * even if they are marked as already taken.
	 
	for(i=0; i < instance.getDepot().getAssignedCustomersNr(); ++i){
		customerChosenPtr = instance.getDepot().getAssignedCustomer(i);
		if(customerChosenPtr.getRouteIndex() == -1){
			int randomRouteNr = random.nextInt(this.route.size());
			route = this.route.get(randomRouteNr);
//			System.out.println("taken before change: "+customerChosenPtr.getIsTaken());
//			System.out.println("Is distant: "+customerChosenPtr.getIsDistant());
			customerChosenPtr.setIsTaken(true); //Mark it anyway
			insertBestTravel(instance,route,customerChosenPtr);
			evaluateRoute(route);
		}
	}
	
	 * DEBUG starts
	 
	int totCRoutes=0;
	for(i=0; i < this.route.size();++i)
		totCRoutes+=this.route.get(i).getCustomersLength();
	
	System.out.println("Total customers in routes: "+totCRoutes);
	for(i=0; i< instance.getCustomersNr();++i){
		if(instance.getDepot().getAssignedCustomer(i).getRouteIndex() == -1)
			System.out.println("Customer "+instance.getDepot().getAssignedCustomer(i).getNumber()+" doesn't belong to any route");
	}
	System.out.println("");
	
	 * DEBUG ends
	 
}*/
}