package com.TabuSearch;

import org.coinor.opents.*;

import com.mdvrp.Instance;

@SuppressWarnings("serial")
public class MyTabuList extends ComplexTabuList implements TabuSearchListener{
	private int counter;
	private int reset=7;
	private Instance instance;

	public MyTabuList(int tenure, int[] attrDim, Instance instance) {
		
		super(tenure, attrDim);
		this.instance= instance;
		//  Auto-generated constructor stub
	}

	@Override
	public void tabuSearchStarted(TabuSearchEvent arg0) {
		//  Auto-generated method stub
		//counter=0;
		
		
	}

	@Override
	public void tabuSearchStopped(TabuSearchEvent arg0) {
		//  Auto-generated method stub
		
	}

	@Override
	public void newBestSolutionFound(TabuSearchEvent arg0) {
		//  Auto-generated method stub
		counter--;
		setTenure(getTenure()-2);
		if (getTenure()<1)
			setTenure(reset);
		
	}

	@Override
	public void newCurrentSolutionFound(TabuSearchEvent arg0) {
		//  Auto-generated method stub
		
	}

	@Override
	public void unimprovingMoveMade(TabuSearchEvent arg0) {
		//  Auto-generated method stub
		
		counter++;
		if (counter==20){
			setTenure(getTenure()+3);
			if (getTenure()>instance.getCustomersNr()*2)
				setTenure(reset);
			counter=0;
		}
		
		
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
