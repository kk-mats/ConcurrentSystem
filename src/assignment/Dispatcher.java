package assignment;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Random;
import java.util.stream.Collectors;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.SimpleBehaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.domain.FIPAAgentManagement.AMSAgentDescription;
import jade.domain.FIPAAgentManagement.SearchConstraints;
import jade.domain.AMSService;

public class Dispatcher extends Agent
{
	class Behaviour extends SimpleBehaviour
	{
		private DispatcherState state=DispatcherState.ready_to_dispatch;
		private MessageTemplate dispatchingFilter;

		public Behaviour()
		{
			super(Dispatcher.this);
		}

		@Override
		public void action()
		{
			switch(this.state)
			{
				case ready_to_dispatch:
				{
					ACLMessage msg=this.myAgent.receive();
					if(msg==null)
					{
						return;
					}

					if(msg.getPerformative()==ACLMessage.REQUEST && !Dispatcher.this.buffer.isEmpty())
					{
						this.state=DispatcherState.dispatching;
						this.dispatchingFilter=MessageTemplate.and(
							MessageTemplate.MatchSender(msg.getSender()),
							MessageTemplate.MatchPerformative(ACLMessage.CONFIRM)
						);

						ACLMessage reply=msg.createReply();
						reply.setPerformative(ACLMessage.AGREE);
						reply.setContent(Dispatcher.this.buffer.peekFirst().toString());
						this.myAgent.send(reply);
						Recorder.println(this.myAgent.getLocalName()+" starts dispatching ["+Dispatcher.this.buffer.peekFirst().toString()+"] to "+msg.getSender().getLocalName());
					}
					return;
				}

				case dispatching:
				{
					ACLMessage msg=this.myAgent.blockingReceive(this.dispatchingFilter);
					Recorder.println(this.myAgent.getLocalName()+" finishes dispatching ["+Dispatcher.this.buffer.peekFirst().toString()+"] to "+msg.getSender().getLocalName());
					this.state=DispatcherState.ready_to_dispatch;
					Dispatcher.this.dispatched.add(Dispatcher.this.buffer.pollFirst());
				}
			}
		}

		@Override
		public boolean done()
		{
			return Dispatcher.this.readyToDelete();
		}
	}

	private int restOfPayload=20;
	private ArrayDeque<Payload> buffer=new ArrayDeque<Payload>();
	private ArrayList<Payload> dispatched=new ArrayList<Payload>();
	private Random random=new Random();

	@Override
	protected void setup()
	{
		this.addBehaviour(new Behaviour());
		this.generatePayload();
		Recorder.println(this.getLocalName()+" has started.");
	}

	private void generatePayload()
	{
		for(int i=0; i<this.restOfPayload; ++i)
		{
			this.buffer.push(new Payload(random.nextInt(3), random.nextInt(3000)+1000));
		}
	}


	private boolean readyToDelete()
	{
		if(this.restOfPayload>0)
		{
			return false;
		}

		ACLMessage template=new ACLMessage(ACLMessage.INFORM);
		SearchConstraints c=new SearchConstraints();
		c.setMaxResults(new Long(-1));
		try
		{
			AMSAgentDescription[] agents=AMSService.search((Agent)this, new AMSAgentDescription(), c);
			for(AMSAgentDescription agent:agents)
			{
				if(agent.getName().toString().contains("Port") || agent.getName().toString().contains("Carrier"))
				{
					ACLMessage msg=template;
					msg.addReceiver(agent.getName());
					this.send(msg);
					System.out.println("send exit to "+agent.getName().getLocalName());

					while(true)
					{
						ACLMessage reply=this.receive();
						if(reply!=null && reply.getPerformative()==ACLMessage.ACCEPT_PROPOSAL)
						{
							break;
						}
					}
				}
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		this.doDelete();
		return true;
	}

	@Override
	protected void takeDown()
	{
		Recorder.println(this.getLocalName()+" has exited. dispatched=[\n"+this.dispatched.stream().map((Payload p)->"  ["+p.toString()+"]").collect(Collectors.joining(",\n"))+"\n]");
		Recorder.dump();
	}
}
