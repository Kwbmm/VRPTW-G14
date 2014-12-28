package com.TabuSearch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Map.Entry;

import org.coinor.opents.Move;
import org.coinor.opents.MoveManager;
import org.coinor.opents.Solution;

import com.mdvrp.Customer;
import com.mdvrp.Instance;
import com.mdvrp.Route;

@SuppressWarnings("serial")
public class MyMoveManager implements MoveManager {
	private static Instance instance;
	private MovesType movesType;

	
	public MyMoveManager(Instance instance) {
		MyMoveManager.setInstance(instance);
	}


	@Override
	public Move[] getAllMoves(Solution solution) {
		MySolution sol = ((MySolution)solution);
		
			return getSwapMoves(sol);
		
    
	}
	
	
	
	public Move[] getSwapMoves(MySolution solution){
		ArrayList<Route> routes = solution.getRoutes();
		Move[] buffer = new Move[ getInstance().getCustomersNr() * getInstance().getVehiclesNr() ];
		int nextBufferPos = 0;
		
		

		/*
         	 // iterates depots
         for (int i = 0; i < routes.length; ++i) {
         	// iterates routes
         	for (int j = 0; j < routes[i].length; ++j) {
         		// iterates customers in the route
         		for (int k = 0; k < routes[i][j].getCustomersLength(); ++k) {
         			for(int l = 0; l < routes.length; ++l){
	         			// iterate each route for that deposit and generate move to it if is different from the actual route
	         			for (int r = 0; r < routes[l].length; ++r) { 
	         				if (!(r == j && i == l)) {
	         					Customer customer = routes[i][j].getCustomer(k);
	         					buffer[nextBufferPos++] = new MySwapMove(getInstance(), customer, i, j, k, l, r);
	         				}
	         			}
         			}
         		}
         	}
         	( Instance instance, Customer customer, int deleteDepotNr, int deleteRouteNr, int deletePositionIndex, int insertDepotNr , int insertRouteNr)
         }
         	}*/
		for (int j = 0; j < routes.size(); ++j) { // for each route 
			for (int k = 0; k < routes.get(j).getCustomersLength(); ++k) { // for each customer of the route
				for(int l = 0; l < routes.size(); ++l){ // for all the other routes 
					// if it's not the same route
					if(j!=l){
						Customer customer = routes.get(j).getCustomer(k);
						buffer[nextBufferPos++] = new MySwapMove(getInstance(), customer, j, k, l);
						//System.out.println("Move customer "+ customer.getNumber()+ "from route "+ k+ "to route "+ l + "\n");


					}


				}

			}

		}


		// Trim buffer
		Move[] moves = new Move[ nextBufferPos];
		System.arraycopy( buffer, 0, moves, 0, nextBufferPos );

		return moves;
	}
	 
	 
	private Move[] getRelocateMoves(MySolution sol) {
	   	 ArrayList<Route> routes = sol.getRoutes();
	   	 Move[] buffer  = new Move[routes.size()];
         int nextBufferPos = 0;
         int deletePositionIndex;
         int insertPositionIndex;
         
/*         for (int j=0; j<routes.size(); j++){

      		System.out.println("\nRotta "+ routes.get(j).getIndex());         
     	for (int i=0; i< routes.get(j).getCustomersLength(); i++){
     		
     		System.out.printf("%d  ",routes.get(j).getCustomer(i).getNumber());
     	}
         }*/
         
         for (int i=0 ; i< routes.size(); i++){
        	 if(!routes.get(i).isEmpty()){
        	 Customer insertedCustomer = new Customer();
        	 int evaluatedRouteIndex= routes.get(i).getIndex(); // route you are evaluating 
        	 
        	// if( routes.get(i).getCustomersLength() > 1){ // if the route is not composed by only one customer
        	 insertedCustomer = findCustomerToInsert(routes.get(i), 6); // customer you are going to insert in that route
        	 insertPositionIndex= insertBestTravel(routes.get(i), insertedCustomer);
        	 int nextRouteIndex = insertedCustomer.getRouteIndex(); // index of the route to which the customer found belongs
        	 
        	// System.out.println("\nCustomer selezionato: "+ insertedCustomer.getNumber() + " Rotta di appartenenza: "+ insertedCustomer.getRouteIndex());

        	 if(insertedCustomer!=null && nextRouteIndex!=evaluatedRouteIndex ){
        		 Customer deletedCustomer = insertNewCustomer(routes.get(i), sol.getRoute(nextRouteIndex), insertedCustomer); 
        		 // variables passed: 1)route you are evaluating from which you are going to delete a customer, 2) route where to put the customer,
        		 if (deletedCustomer != null){
        			 deletePositionIndex = getPositionInRoute(deletedCustomer, sol);
        			 //buffer[nextBufferPos++] =  new MyRelocateMove(instance, evaluatedRouteIndex ,nextRouteIndex, insertedCustomer, deletedCustomer, deletePositionIndex, insertPositionIndex);
        		 //System.out.println("MOSSA: " + buffer[i]);
        		//	 System.out.println("IndiceRottaProvenienza: " + insertedCustomer.getRouteIndex());
        		// System.out.println("IndiceRottaDestinazione: " + routes.get(i).getIndex());
        		
        		 }
        	 }
        	 //System.out.println("-----------------------------");
         }
         }
         
         
         Move[] moves = new Move[ nextBufferPos];
         System.arraycopy( buffer, 0, moves, 0, nextBufferPos );
         //for (int i =0 ; i< moves.length; i++)
        	 //System.out.println(moves[i]);

         return moves;
    }
	
	public int getPositionInRoute(Customer deletedCustomer, MySolution sol)
	{
		int position = 0;
		int routeIndex = deletedCustomer.getRouteIndex();
		
		if(routeIndex>0)
		{
			List<Customer> lista = sol.getRoute(routeIndex).getCustomers();
		
			for(Customer c : lista)
			{
				if(c.getNumber() == deletedCustomer.getNumber())
					break;
				
				position++;
			}
		}
		//System.out.println("\nPOSITION: " + position);
		return position;
	}
	 private int insertBestTravel(Route route, Customer customerChosenPtr) {
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
				return position;
	 }
	
	public Customer insertNewCustomer(Route routeD, Route routeI, Customer customer)
	{
		Random random = new Random();
		List<Customer> customersRouteI = routeI.getCustomers();
		Customer k = new Customer();
		int length, custIndex; 
		int a = 0;
		
		while(a==0)
		{
			length = customersRouteI.size();
			if(length==1)
				break;
			length = random.nextInt(length);
			custIndex = customersRouteI.get(length).getNumber();
			
			if(custIndex != customer.getNumber())
			{
				a++;
				k = customersRouteI.get(length);
			}
			
			
				
		}
		
		return k;
	}
	
	
	public Customer findCustomerToInsert(Route path, int n){
		Route route = path;
		ArrayList<Customer> list = new ArrayList<Customer>();
		
		ArrayList<Customer> cust= (ArrayList<Customer>) route.getCustomers();
		//System.out.println("\nRotta valutata: " + route.getIndex());
    	//for (int i=0; i< cust.size(); i++)
    		//System.out.printf("%d  ", cust.get(i).getNumber());
		
		for (int i=0; i<route.getCustomersLength();i++){ // per ogni customer nella rotta
			Customer k = cust.get(i);
			ArrayList<Customer> orderedList= instance.calculateAnglesToCustomer(k);
			list.addAll(orderedList.subList(0, n)); //prendi gli n customer vicini		
			}
		//System.out.println("\nCustomer vicini: ");
    	//for (int i=0; i< list.size(); i++)
    		//System.out.printf("%d  ", list.get(i).getNumber());

		HashMap<Customer, Integer> counters = new HashMap<Customer, Integer>();
		for(Customer c: list) {
		       Integer i = counters.get(c);
		       if (i ==  null) {
		           i = 0;
		       }
		       counters.put(c, i + 1);
		    }
		
		Entry<Customer, Integer> maxEntry = null;

		for(Entry<Customer, Integer> entry : counters.entrySet()) {
		    if (maxEntry==null || entry.getValue() > maxEntry.getValue()) {
		        maxEntry = entry;
		    }
		}

	    return maxEntry.getKey();
	}

	public static Instance getInstance() {
		return instance;
	}

	public static void setInstance(Instance instance) {
		MyMoveManager.instance = instance;
	}
	public MovesType getMovesType() {
		return movesType;
	}


	public void setMovesType(MovesType movesType) {
		this.movesType = movesType;
	}


}
