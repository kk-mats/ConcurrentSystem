package concurrentSystems;

import jade.core.behaviours.SimpleBehaviour;

public class TwoSteps extends SimpleBehaviour
{
	private int state=1;
	private boolean finished=false;

	@Override
	public void action()
	{
		switch(this.state)
		{
		case 1:
			block(250);
			break;

		case 2:
			System.out.println("---Message 1---");
			block(500);
			break;

		case 3:
			System.out.println("---Message 2---");
			this.finished=true;
			this.myAgent.doDelete();
		}

		++state;
	}

	@Override
	public boolean done()
	{
		return this.finished;
	}

}
