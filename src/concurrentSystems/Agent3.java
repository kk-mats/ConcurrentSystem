package concurrentSystems;

import jade.core.Agent;

public class Agent3 extends Agent
{
	@Override
	protected void setup()
	{
		this.addBehaviour(new TwoSteps());
		this.addBehaviour(new Looper(this, 300));
	}

	@Override
	protected void takeDown()
	{
		System.out.println("Finished");
		System.exit(0);
	}
}
