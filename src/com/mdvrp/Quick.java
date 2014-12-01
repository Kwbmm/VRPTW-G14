package com.mdvrp;

import java.util.ArrayList;

public class Quick {

    /**
     * Rearranges the array in ascending order, using the angle with the depot
     * @param a the array to be sorted
     */
    public static void sort(ArrayList<Customer> customers, int index) {
        sort(customers, 0, customers.size() - 1, index);
        //show(customers, index);
    }

    // quicksort the subarray from customers[lo] to customers[hi]
    private static void sort(ArrayList<Customer> customers, int lo, int hi, int index) { 
        if (hi <= lo)
        	return;
        int j = partition(customers, lo, hi, index);
        sort(customers, lo, j-1, index);
        sort(customers, j+1, hi, index);
    }

    // partition the subarray customers[lo..hi] so that customers[lo..j-1] <= customers[j] <= customers[j+1..hi]
    // and return the index j.
    private static int partition(ArrayList<Customer> customers, int lo, int hi, int index) {
        int i = lo;
        int j = hi + 1;
        Customer v = customers.get(lo);
        while (true) { 

            // find item on lo to swap
            while (less(customers.get(++i), v, index))
                if (i == hi) break;

            // find item on hi to swap
            while (less(v, customers.get(--j), index))
                if (j == lo) break;      // redundant since a[lo] acts as sentinel

            // check if pointers cross
            if (i >= j) break;

            exch(customers, i, j);
        }

        // put partitioning item v at a[j]
        exch(customers, lo, j);

        // now, a[lo .. j-1] <= a[j] <= a[j+1 .. hi]
        return j;
    }

   /***********************************************************************
    *  Helper sorting functions
    ***********************************************************************/
    // is v < w ?
    private static boolean less(Customer c1, Customer c2, int index) {
    	if( c1.getDistance() <  c2.getDistance()) {
			return true;
		}
		else 
			return false;
    }
        
    // exchange a[i] and a[j]
    private static void exch(ArrayList<Customer> customers, int i, int j) {
        Customer swap = customers.get(i);
        customers.set(i, customers.get(j));
        customers.set(j, swap);
    }
}