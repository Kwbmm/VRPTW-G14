package com.vrp;

/**
 * Customer class stores information about one customer which implements the Vertex interface.
 * Stores the number of the customer, coordinates, service duration, capacity,
 */
public class Customer {
	private int number;
	private double xCoordinate;
	private double yCoordinate;
	private double serviceDuration;     // duration that takes to dispatch the delivery    
	private double load;                // capacity of the pack that is expecting
	private int startTw;                // beginning of time window (earliest time for start of service),if any
	private int endTw;                  // end of time window (latest time for start of service), if any
	private Depot assignedDepot;        // the depot from which the customer will be served
	private double arriveTime;          // time at which the car arrives to the customer
	private double waitingTime;         // time to wait until arriveTime equal start time window
	private double twViol;              // value of time window violation, 0 if none
	private double distanceFromDepot;
	private double angleFromDepot;
	private boolean isTaken=false;
	private double angleToCustomer;
	private Route assignedRoute;
	private int routeIndex;

	public Customer() {
		xCoordinate = 0;
		yCoordinate = 0;
		serviceDuration = 0;
		load = 0;
		number = 1;
		startTw = 0;
		endTw = 0;
		arriveTime = 0;
		waitingTime = 0;
		twViol = 0;
		distanceFromDepot = 0;
		routeIndex =-1;
	}

	public Customer(Customer customer) {
		this.number 			= customer.number;
		this.xCoordinate 		= customer.xCoordinate;
		this.yCoordinate 		= customer.yCoordinate;
		this.serviceDuration 	= customer.serviceDuration;
		this.load 				= customer.load;
		this.startTw 			= customer.startTw;
		this.endTw 				= customer.endTw;
		this.assignedDepot 		= customer.assignedDepot;
		this.arriveTime 		= new Double(customer.arriveTime);
		this.waitingTime 		= new Double(customer.waitingTime);
		this.twViol 			= new Double(customer.twViol);
		this.distanceFromDepot = customer.distanceFromDepot;
		this.angleToCustomer     =customer.angleToCustomer;
		this.assignedRoute       =customer.assignedRoute;
		this.routeIndex = customer.routeIndex;
	}

	/**
	 * This return a string with formated customer data
	 * @return
	 */
	public String print() {
		StringBuffer print = new StringBuffer();
		print.append("\n");
		print.append("\n" + "--- Customer " + number + " -----------------------------------");
		print.append("\n" + "| x=" + xCoordinate + " y=" + yCoordinate);
		print.append("\n" + "| ServiceDuration=" + serviceDuration + " Demand=" + load);
		print.append("\n" + "| AssignedDepot=" + assignedDepot.getNumber());
		print.append("\n" + "| StartTimeWindow=" + startTw + " EndTimeWindow=" + endTw);
		print.append("\n" + "--------------------------------------------------");
		return print.toString();

	}

	/**
	 * get the time at which the car arrives to the customer
	 * @return dispatchtime
	 */
	public double getArriveTime() {
		return arriveTime;
	}

	/**
	 * set the time at which the car arrives to the customer
	 * @param dispatchtime
	 */
	public void setArriveTime(double dispatchtime) {
		this.arriveTime = dispatchtime;
	}

	/**
	 * @return the customernumber
	 */
	public int getNumber() {
		return this.number;
	}

	public double getDistance(){
		return this.distanceFromDepot;
	}

	/**
	 * @param customernumber the customernumber to set
	 */
	public void setNumber(int customernumber) {
		this.number = customernumber;
	}

	/**
	 * @return the xcoordinate
	 */
	public double getXCoordinate() {
		return xCoordinate;
	}

	/**
	 * @param xcoordinate the xcoordinate to set
	 */
	public void setXCoordinate(double xcoordinate) {
		this.xCoordinate = xcoordinate;
	}

	/**
	 * @return the ycoordinate
	 */
	public double getYCoordinate() {
		return yCoordinate;
	}

	/**
	 * @param ycoordinate the ycoordinate to set
	 */

	public void setYCoordinate(double ycoordinate) {
		this.yCoordinate = ycoordinate;
	}

	public void setDistanceFromDepot(double distance){
		this.distanceFromDepot = distance;
	}

	public void setIsTaken(boolean value){
		this.isTaken = value;
	}

	public boolean getIsTaken(){
		return this.isTaken;
	}

	/**
	 * @return the serviceduration
	 */
	public double getServiceDuration() {
		return serviceDuration;
	}

	/**
	 * @param serviceduration the serviceduration to set
	 */
	public void setServiceDuration(double serviceduration) {
		this.serviceDuration = serviceduration;
	}

	/**
	 * @return the demand
	 */
	public double getCapacity() {
		return load;
	}


	/**
	 * @param demand the demand to set
	 */
	public void setCapacity(double demand) {
		this.load = demand;
	}


	/**
	 * @return the startTW
	 */
	public int getStartTw() {
		return startTw;
	}


	/**
	 * @param startTW the startTW to set
	 */
	public void setStartTw(int startTW) {
		this.startTw = startTW;
	}


	/**
	 * @return the endTW
	 */
	public int getEndTw() {
		return endTw;
	}


	/**
	 * @param endTW the endTW to set
	 */
	public void setEndTw(int endTW) {
		this.endTw = endTW;
	}

	/**
	 * @return the assigneddepot
	 */
	public Depot getAssignedDepot() {
		return assignedDepot;
	}


	/**
	 * @param assigneddepot the assigneddepot to set
	 */
	public void setAssignedDepot(Depot assigneddepot) {
		this.assignedDepot = assigneddepot;
	}

	/**
	 * @return the waitingTime
	 */
	public double getWaitingTime() {
		return waitingTime;
	}

	/**
	 * @param waitingTime the waitingTime to set
	 */
	public void setWaitingTime(double waitingTime) {
		this.waitingTime = waitingTime;
	}

	/**
	 * @return the twViol
	 */

	public double getTwViol() {
		return twViol;
	}

	/**
	 * @param twViol the twViol to set
	 */
	public void setTwViol(double twViol) {
		this.twViol = twViol;
	}

	public double getAngleToCustomer() {
		return angleToCustomer;
	}

	public void setAngleToCustomer(double angleToCustomer) {
		this.angleToCustomer = angleToCustomer;
	}

	public int getRouteIndex() {
		return routeIndex;
	}

	public void setRouteIndex(int routeIndex) {
		this.routeIndex = routeIndex;
	}

	public double getAngleFromDepot() {
		return angleFromDepot;
	}

	public void setAngleFromDepot(double angleFromDepot) {
		this.angleFromDepot = angleFromDepot;
	}
}