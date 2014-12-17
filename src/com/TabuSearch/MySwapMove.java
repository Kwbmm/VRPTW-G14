package com.TabuSearch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.coinor.opents.ComplexMove;
import org.coinor.opents.Solution;

import com.mdvrp.Cost;
import com.mdvrp.Customer;
import com.mdvrp.Instance;
import com.mdvrp.Route;

@SuppressWarnings("serial")
public class MySwapMove implements ComplexMove{
	
	private Instance instance;
	private int insertDepotNr;
	private int insertRouteNr;

    
    
    public MySwapMove( Instance instance){
    	this.instance = instance;
    	
    }   // end constructor
    

	@Override
	public void operateOn(Solution soln) {
		MySolution sol = (MySolution)soln;
    	Route route = sol.getRoute(insertDepotNr, insertRouteNr);
    	Cost initialInsertCost = new Cost(route.getCost());
		
	}

	@Override
	public int[] attributesDelete() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int[] attributesInsert() {
		// TODO Auto-generated method stub
		return null;
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

	
}
