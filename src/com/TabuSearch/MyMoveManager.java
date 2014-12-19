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
         Move[] moves = new Move[routes.size()];
         
         
         for (int j=0; j<routes.size(); j++){
         
     	/*for (int i=0; i< routes.get(j).getCustomersLength(); i++){
     		System.out.println("\nRotta "+ routes.get(j).getIndex());
     		System.out.printf("%d  ",routes.get(j).getCustomer(i).getNumber());
     	}*/
         }
         
         for (int i=0 ; i< routes.size(); i++){
        	 Customer insertedCustomer = new Customer();
        	 int evaluatedRouteIndex= routes.get(i).getIndex();
        	 insertedCustomer = findCustomerToInsert(routes.get(i), 8);
        	 int nextRouteIndex = insertedCustomer.getRouteIndex();
        	 
        	 //System.out.println("\nCustomer selezionato: "+ k.getNumber() + " Rotta di appartenenza: "+ k.getRouteIndex());

        	 if(insertedCustomer!=null){
        		 Customer deletedCustomer = insertNewCustomer(routes.get(i), sol.getRoute(nextRouteIndex), insertedCustomer);
        		 moves [i] = new MyRelocateMove(instance, evaluatedRouteIndex ,nextRouteIndex, insertedCustomer, deletedCustomer);
        		 System.out.println("MOSSA: " + i);
        		 System.out.println("IndiceRotta: " + routes.get(i).getIndex());
        		 System.out.println("IndiceRottaCustomerdaInserire: " + insertedCustomer.getRouteIndex());
        	 }
         }
         
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
