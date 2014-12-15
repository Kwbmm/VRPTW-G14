package com.TabuSearch;

import java.io.PrintStream;

import org.coinor.opents.TabuList;
import org.coinor.opents.TabuSearchEvent;
import org.coinor.opents.TabuSearchListener;

import com.mdvrp.Instance;

@SuppressWarnings("serial")
public class MySearchProgram implements TabuSearchListener {

	public MySearchProgram(Instance instance, MySolution initialSol,
			MyMoveManager moveManager, MyObjectiveFunction objFunc,
			TabuList tabuList, boolean b, PrintStream outPrintSream) {
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
