package concurrentSystems;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

public class Pong extends Agent
{
	@Override
	protected void setup()
	{
		this.addBehaviour(new CyclicBehaviour(this)
		{
			@Override
			public void action()
			{
				ACLMessage msg=receive();
				if(msg!=null)
				{
					System.out.println(" - "+this.myAgent.getLocalName()+" received: "+msg.getContent());
					ACLMessage reply=msg.createReply();
					reply.setPerformative(ACLMessage.INFORM);
					reply.setContent("Pong");
					send(reply);
				}
				block();
			}
		});
	}
}
