package concurrentSystems;

import jade.core.Agent;
import jade.core.behaviours.SimpleBehaviour;

public class Agent1 extends Agent
{
	protected void setup()
	{
		addBehaviour(new Looper(this, 300));
		addBehaviour(new Looper(this, 500));
	}
}
