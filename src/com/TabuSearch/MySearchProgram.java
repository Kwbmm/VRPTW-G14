package com.TabuSearch;

import java.io.PrintStream;
import java.text.DecimalFormat;
import java.util.ArrayList;

import org.coinor.opents.*;

import com.mdvrp.*;

@SuppressWarnings("serial")
public class MySearchProgram implements TabuSearchListener{
	private static int iterationsDone;
	public TabuSearch tabuSearch;
	private MySolution sol;
	public Instance instance;
	public ArrayList<Route> feasibleRoutes; 	 // stores the routes of the feasible solution if any
	public Cost feasibleCost;		 // stores the total cost of feasible solution if any, otherwise totalcostviol = Double.Infinity
	public ArrayList<Route> bestRoutes;	 	 // stores the routes of with the best travel time
	public Cost bestCost;		     // stores the total cost of best travel time solution
	public ArrayList<Route> currentRoutes;	 // stores the routes of the current solution
	public Cost currentCost;		 // stores the total cost of current solution
	public int feasibleIndex;
	public int bestIndex;
	public int numberFeasibleSol;
	public MyMoveManager manager;
	public DecimalFormat df = new DecimalFormat("#.##");
	
	public MySearchProgram(Instance instance, Solution initialSol, MoveManager moveManager, ObjectiveFunction objFunc, TabuList tabuList, boolean minmax, PrintStream outPrintStream)
	{
		tabuSearch = new SingleThreadedTabuSearch(initialSol, moveManager, objFunc,tabuList,	new BestEverAspirationCriteria(), minmax );
		feasibleIndex = -1;
		bestIndex = 0;
		numberFeasibleSol = 0;
		this.instance = instance;
		MySearchProgram.setIterationsDone(0);
		tabuSearch.addTabuSearchListener( this );
		tabuSearch.addTabuSearchListener((MyTabuList)tabuList);
		this.manager = (MyMoveManager) moveManager;
	}

	public void improvingMoveMade(TabuSearchEvent event) {
	//	System.out.println("improve move");
	}

	/**
	 * when a new best solution event occur save and print it
	 */
	@Override
	public void newBestSolutionFound(TabuSearchEvent event) {
	//	System.out.println("best sol");
		sol = ((MySolution)tabuSearch.getBestSolution());
		bestCost 	= getCostFromObjective(sol.getObjectiveValue());
		bestRoutes 	= cloneRoutes(sol.getRoutes());
		bestIndex 	= tabuSearch.getIterationsCompleted() + 1; // plus the current one
	}

	/**
	 * when a new current solution is triggered do the following:
	 * - update the parameters alpha, beta, gamma
	 * - check to see if a new better feasible solution is found
	 * - if graphics is visible update panel components and repaint
	 */
	@Override
	public void newCurrentSolutionFound(TabuSearchEvent event) {
		//System.out.println("new current");
		ArrayList<Route> array =  sol.getRoutes();
    	/*for(int z=0;z<array.size(); z++){
    		if(!array.get(z).isEmpty()){
    			System.out.println("\nRotta " + array.get(z).getIndex());
    			for(int x=0;x< array.get(z).getCustomersLength(); x++)
    				System.out.printf("%d ", array.get(z).getCustomerNr(x) );
    		}
    		
    	}*/
		sol = ((MySolution)tabuSearch.getCurrentSolution());
		currentCost = getCostFromObjective(sol.getObjectiveValue());
		
		MySearchProgram.iterationsDone += 1;
	//	System.out.println("Iteration: " + iterationsDone);
	//	System.out.println("Precision: " + (feasibleCost.total-instance.getPrecision()));
		// Check to see if a new feasible solution is found
		// Checking with the current solution admits new feasible solution
		// that are worst than the best solution
		
		if(currentCost.checkFeasible() && currentCost.total < feasibleCost.total - instance.getPrecision())
		{
			feasibleCost = currentCost;
			feasibleRoutes = cloneRoutes(sol.getRoutes());
			// set the new best to the current one
			tabuSearch.setBestSolution(sol);
			System.out.println("It " + tabuSearch.getIterationsCompleted() +" - New solution " + sol.getCost().total+ " travel time: "+ sol.getCost().getTravelTime());
			numberFeasibleSol++;
			//System.out.println("FEASIBLE: " + numberFeasibleSol);
			
/*			if(iterationsDone>=4500 && numberFeasibleSol<10)
			{
				//System.out.println("CAMBIO MOSSA");
				//instance.getParameters().setMovesType(MovesType.SWAP);
				manager.setMovesType(MovesType.SWAP);
			}*/
		}
		
		sol.updateParameters(sol.getObjectiveValue()[2], sol.getObjectiveValue()[3], sol.getObjectiveValue()[4]);
	}

	@Override
	public void noChangeInValueMoveMade(TabuSearchEvent event) {}

	/**
	 * When tabu search starts initialize best cost and
	 * routes and feasible cost and routes and also if
	 * graphics enabled initialize them and print the 
	 * initial route
	 */
	@Override
	public void tabuSearchStarted(TabuSearchEvent event) {
		sol = ((MySolution)tabuSearch.getCurrentSolution());
		// initialize the feasible and best cost with the initial solution objective value
		bestCost = getCostFromObjective(sol.getObjectiveValue());
		feasibleCost = bestCost;
		if (!feasibleCost.checkFeasible()) {
			feasibleCost.total = Double.POSITIVE_INFINITY;
		}
		feasibleRoutes = cloneRoutes(sol.getRoutes());
		bestRoutes = feasibleRoutes;
		//System.out.println("FEASIBLE_COST: " + feasibleCost);
	}

	@Override
	public void tabuSearchStopped(TabuSearchEvent event) {
		sol    = ((MySolution)tabuSearch.getBestSolution());
		if (feasibleCost.total != Double.POSITIVE_INFINITY) {
			sol.setCost(feasibleCost);
			sol.setRoute(feasibleRoutes);
			//sol.setFeasibleIndex(feasibleIndex);
			tabuSearch.setBestSolution(sol);
			//System.out.println("BestSolution");
		}
	}

	@Override
	public void unimprovingMoveMade(TabuSearchEvent event) {
		if(iterationsDone>=3500)
		{
			//System.out.println("CAMBIO MOSSA");
			//instance.getParameters().setMovesType(MovesType.SWAP);
			manager.setMovesType(MovesType.RELOCATE);
		}
	}
	
	// return a new created cost from the objective vector passed as parameter
	private Cost getCostFromObjective(double[] objective) {
		Cost cost = new Cost();
		cost.total        = objective[1];
		cost.travelTime   = objective[2];
		cost.loadViol     = objective[3];
		cost.twViol       = objective[4];
		//System.out.printf("total: %f \n traveltime: %f\n loadviol: %f\n twviol: %f\n",objective[1], objective[2],objective[3], objective[4]);


		return cost;
	}
    
    // clone the routes passed as parameter
    public ArrayList<Route> cloneRoutes(ArrayList<Route> routes){
		ArrayList<Route> routescopy = new ArrayList<Route>();
        for (int i = 0; i < routes.size(); ++i) {
        	routescopy.add(new Route(routes.get(i)));
        }
        return routescopy;
	}

	/**
	 * @return the iterationsDone
	 */
	public static int getIterationsDone() {
		return iterationsDone;
	}

	/**
	 * @param iterationsDone the iterationsDone to set
	 */
	public static void setIterationsDone(int iterationsDone) {
		MySearchProgram.iterationsDone = iterationsDone;
	}

	public ArrayList<Route> getFeasibleRoutes() {
		return feasibleRoutes;
	}

	public void setFeasibleRoutes(ArrayList<Route> feasibleRoutes) {
		this.feasibleRoutes = feasibleRoutes;
	}

	public int getNumberFeasibleSol() {
		return numberFeasibleSol;
	}

	public void setNumberFeasibleSol(int numberFeasibleSol) {
		this.numberFeasibleSol = numberFeasibleSol;
	}
}
