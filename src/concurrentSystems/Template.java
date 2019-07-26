package concurrentSystems;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class Template extends Agent
{
	private MessageTemplate mt1=MessageTemplate.and(
			MessageTemplate.MatchPerformative(ACLMessage.INFORM),
			MessageTemplate.MatchSender(new AID("a1", AID.ISLOCALNAME)));

	@Override
	protected void setup()
	{
		ACLMessage msg=new ACLMessage(ACLMessage.INFORM);
		msg.setContent("Ping");
		for(int i=1; i<=2; ++i)
		{
			msg.addReceiver(new AID("a"+i, AID.ISLOCALNAME));
		}
		send(msg);

		this.addBehaviour(new CyclicBehaviour(this)
		{
			@Override
			public void action()
			{
				System.out.print("Behaviour One: ");
				ACLMessage msg=receive(Template.this.mt1);
				if(msg!=null)
				{
					System.out.println("gets "+msg.getContent()+" from "+msg.getSender().getLocalName());
				}
				else
				{
					System.out.println("gets NULL");
				}
				this.block();
			}
		});

		this.addBehaviour(new CyclicBehaviour(this)
		{
			@Override
			public void action()
			{
				System.out.print("Behaviour TWO: ");
				ACLMessage msg=receive();
				if(msg!=null)
				{
					System.out.println("gets "+msg.getContent()+" from "+msg.getSender().getLocalName());
				}
				else
				{
					System.out.println("gets NULL");
				}
				this.block();
			}
		});
	}
}
