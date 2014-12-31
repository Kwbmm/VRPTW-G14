package com.TabuSearch;

import org.coinor.opents.*;

@SuppressWarnings("serial")
public class MyTabuList extends ComplexTabuList implements TabuSearchListener{
	private int counter;

	public MyTabuList(int tenure, int[] attrDim) {
		super(tenure, attrDim);
		//  Auto-generated constructor stub
	}

	@Override
	public void tabuSearchStarted(TabuSearchEvent arg0) {
		//  Auto-generated method stub
		counter=0;
		
		
	}

	@Override
	public void tabuSearchStopped(TabuSearchEvent arg0) {
		//  Auto-generated method stub
		
	}

	@Override
	public void newBestSolutionFound(TabuSearchEvent arg0) {
		//  Auto-generated method stub
		
	}

	@Override
	public void newCurrentSolutionFound(TabuSearchEvent arg0) {
		//  Auto-generated method stub
		
	}

	@Override
	public void unimprovingMoveMade(TabuSearchEvent arg0) {
		//  Auto-generated method stub
		
		counter++;
		if (counter==50){
			
			System.out.println("counter50");
			counter=0;
		}
		
		
	}

	@Override
	public void improvingMoveMade(TabuSearchEvent arg0) {
		//  Auto-generated method stub
		counter--;
		
	}

	@Override
	public void noChangeInValueMoveMade(TabuSearchEvent arg0) {
		//  Auto-generated method stub
		
	}

}
