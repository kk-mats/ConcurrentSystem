package concurrentSystems;

import jade.core.Agent;
import jade.core.behaviours.SimpleBehaviour;

public class MyAgent extends Agent
{
	protected void setup()
	{
		this.addBehaviour(new myBehaviour(this));
	}

	class myBehaviour extends SimpleBehaviour
	{
		int n=0;

		public myBehaviour(Agent a)
		{
			super(a);
		}

		public void action()
		{
			System.out.println("Hello World");
			System.out.println("My name is "+getLocalName());
			++n;
		}

		public boolean done()
		{
			return n>=3;
		}
	}
}
