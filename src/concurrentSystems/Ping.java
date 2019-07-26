package concurrentSystems;

import jade.core.Agent;
import jade.core.AID;
import jade.domain.AMSService;
import jade.domain.FIPAAgentManagement.AMSAgentDescription;
import jade.domain.FIPAAgentManagement.SearchConstraints;
import jade.lang.acl.ACLMessage;
import jade.core.behaviours.CyclicBehaviour;

public class Ping extends Agent
{
	@Override
	protected void setup()
	{
		AMSAgentDescription[] agents=null;
		try
		{
			SearchConstraints c=new SearchConstraints();
			c.setMaxResults(new Long(-1));
			agents=AMSService.search(this, new AMSAgentDescription(), c);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}

		ACLMessage msg=new ACLMessage(ACLMessage.INFORM);
		msg.setContent("Ping");

		for(int i=0; i<agents.length; ++i)
		{
			msg.addReceiver(agents[i].getName());
		}
		send(msg);

		this.addBehaviour(new CyclicBehaviour(this)
		{
			@Override
			public void action()
			{
				ACLMessage msg=receive();
				if(msg!=null)
				{
					System.out.println("==Answer <-"+msg.getContent()+" from "+msg.getSender().getName());
				}
				block();
			}
		});
	}
}
