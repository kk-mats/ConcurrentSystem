package concurrentSystems;

import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.core.behaviours.CyclicBehaviour;

public class Responder extends Agent
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
					ACLMessage reply=msg.createReply();
					reply.setPerformative(ACLMessage.INFORM);
					reply.setContent("INFORMATION");
					send(reply);
					reply.setPerformative(ACLMessage.PROPOSE);
					reply.setContent("PROPOSITION");
					send(reply);
				}
				this.block();
			}
		});
	}
}
