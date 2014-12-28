package com.TabuSearch;

import org.coinor.opents.*;
import org.coinor.opents.TabuSearchEvent;
import org.coinor.opents.TabuSearchListener;

@SuppressWarnings("serial")
public class MyTabuList extends ComplexTabuList implements TabuSearchListener{

	public MyTabuList(int tenure, int[] attrDim) {
		super(tenure, attrDim);
		//  Auto-generated constructor stub
	}

	@Override
	public void tabuSearchStarted(TabuSearchEvent arg0) {
		//  Auto-generated method stub
		
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
		
	}

	@Override
	public void improvingMoveMade(TabuSearchEvent arg0) {
		//  Auto-generated method stub
		
	}

	@Override
	public void noChangeInValueMoveMade(TabuSearchEvent arg0) {
		//  Auto-generated method stub
		
	}

}
