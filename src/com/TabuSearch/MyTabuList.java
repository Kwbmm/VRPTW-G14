package com.TabuSearch;

import org.coinor.opents.ComplexTabuList;
import org.coinor.opents.TabuSearchEvent;
import org.coinor.opents.TabuSearchListener;

@SuppressWarnings("serial")
public class MyTabuList extends ComplexTabuList implements TabuSearchListener{

	public MyTabuList(int tenure, int[] attrDim) {
		super(tenure, attrDim);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void tabuSearchStarted(TabuSearchEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void tabuSearchStopped(TabuSearchEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void newBestSolutionFound(TabuSearchEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void newCurrentSolutionFound(TabuSearchEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void unimprovingMoveMade(TabuSearchEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void improvingMoveMade(TabuSearchEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void noChangeInValueMoveMade(TabuSearchEvent e) {
		// TODO Auto-generated method stub
		
	}

}
