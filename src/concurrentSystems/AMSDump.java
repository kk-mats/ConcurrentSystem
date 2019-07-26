package concurrentSystems;

import jade.core.Agent;
import jade.core.AID;
import jade.domain.AMSService;
import jade.domain.FIPAAgentManagement.AMSAgentDescription;
import jade.domain.FIPAAgentManagement.SearchConstraints;

public class AMSDump extends Agent
{
	protected void setup()
	{
		AMSAgentDescription[] agents=null;
		try
		{
			SearchConstraints c=new SearchConstraints();
			c.setMaxResults(new Long(-1));
			agents=AMSService.search(this,  new AMSAgentDescription(), c);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}

		AID myID=this.getAID();
		for(int i=0; i<agents.length; ++i)
		{
			AID agentID=agents[i].getName();
			System.out.println((agentID.equals(myID) ? "***" : "   ")+i+":"+agentID.getName());
		}
	}
}
