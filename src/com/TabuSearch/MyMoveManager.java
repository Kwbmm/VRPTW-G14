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
		
			return getRelocateMoves(sol);
		
    
	}
	private Move[] getRelocateMoves(MySolution sol) {
	   	 ArrayList<Route> routes = sol.getRoutes();
	   	 Move[] buffer  = new Move[routes.size()];
         int nextBufferPos = 0;
         
         for (int j=0; j<routes.size(); j++){

      		System.out.println("\nRotta "+ routes.get(j).getIndex());         
     	for (int i=0; i< routes.get(j).getCustomersLength(); i++){
     		System.out.printf("%d  ",routes.get(j).getCustomer(i).getNumber());
     	}
         }
         
         for (int i=0 ; i< routes.size(); i++){
        	 
        	 Customer insertedCustomer = new Customer();
        	 int evaluatedRouteIndex= routes.get(i).getIndex(); // route you are evaluating 
        	 
        	 if( routes.get(i).getCustomersLength() > 1){ // if the route is not composed by only one customer
        	 insertedCustomer = findCustomerToInsert(routes.get(i), 3); // customer you are going to insert in that route
        	 int nextRouteIndex = insertedCustomer.getRouteIndex(); // index of the route to which the customer found belongs
        	 
        	 System.out.println("\nCustomer selezionato: "+ insertedCustomer.getNumber() + " Rotta di appartenenza: "+ insertedCustomer.getRouteIndex());

        	 if(insertedCustomer!=null && nextRouteIndex!=evaluatedRouteIndex ){
        		 Customer deletedCustomer = insertNewCustomer(routes.get(i), sol.getRoute(nextRouteIndex), insertedCustomer); 
        		 // variables passed: 1)route you are evaluating from which you are going to delete a customer, 2) route where to put the customer,
        		 if (deletedCustomer != null){
        			 buffer[nextBufferPos++] =  new MyRelocateMove(instance, evaluatedRouteIndex ,nextRouteIndex, insertedCustomer, deletedCustomer);
        		 System.out.println("MOSSA: " + buffer[i]);
        		 System.out.println("IndiceRottaDestinazione: " + routes.get(i).getIndex());
        		 System.out.println("IndiceRottaProvenienza: " + insertedCustomer.getRouteIndex());
        		 }
        	 }
        	 System.out.println("-----------------------------");
         }
         }
         
         
         Move[] moves = new Move[ nextBufferPos];
         System.arraycopy( buffer, 0, moves, 0, nextBufferPos );

         return moves;
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
		System.out.println("\nRotta valutata: " + route.getIndex());
    	for (int i=0; i< cust.size(); i++)
    		System.out.printf("%d  ", cust.get(i).getNumber());
		
		for (int i=0; i<route.getCustomersLength();i++){ // per ogni customer nella rotta
			Customer k = cust.get(i);
			ArrayList<Customer> orderedList= instance.calculateAnglesToCustomer(k);
			list.addAll(orderedList.subList(0, n)); //prendi gli n customer vicini		
			}
		System.out.println("\nCustomer vicini: ");
    	for (int i=0; i< list.size(); i++)
    		System.out.printf("%d  ", list.get(i).getNumber());

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
