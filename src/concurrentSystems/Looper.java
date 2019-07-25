package concurrentSystems;

import jade.core.Agent;
import jade.core.behaviours.SimpleBehaviour;


public class Looper extends SimpleBehaviour
{
	static private String offset="";
	static private long t0=System.currentTimeMillis();

	private String tab="";
	private int n=1;
	private long dt;

	public Looper(Agent a, long dt)
	{
		super(a);
		this.dt=dt;
		offset+="\t";
		tab=new String(offset);
	}

	@Override
	public void action()
	{
		System.out.println(this.tab+(System.currentTimeMillis()-this.t0)+":"+this.myAgent.getLocalName());
		this.block(this.dt);
		++n;
	}

	@Override
	public boolean done()
	{
		return n>6;
	}

}
