package com.TabuSearch;

import java.util.ArrayList;
import java.util.HashMap;
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
	   	 Route[] routes = sol.getRoutes();
         Move[] moves = new Move[routes.length];
         
         
         for (int i=0 ; i< routes.length; i++){
        	 Customer k = findCustomerToInsert(routes[i], 5);
        	 moves [i] = new MyRelocateMove(instance, routes[i].getIndex(), k.getAssignedRoute().getIndex(), k); 
         }
         
         return moves;
    }
	
	
	public Customer findCustomerToInsert(Route path, int n){
		Route route = path;
		ArrayList<Customer> list = new ArrayList<Customer>();
		ArrayList<Customer> cust= (ArrayList<Customer>) route.getCustomers();
		for (int i=0; i<route.getCustomersLength();i++){ // per ogni customer nella rotta
			Customer k = cust.get(i);
			ArrayList<Customer> orderedList= instance.calculateAnglesToCustomer(k);
			list.addAll(orderedList.subList(0, n)); //prendi gli n customer vicini		
			}

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
		    if (maxEntry == null || entry.getValue() > maxEntry.getValue()) {
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
