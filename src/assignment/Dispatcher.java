package assignment;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Random;
import java.util.stream.Collectors;

import jade.core.Agent;
import jade.core.behaviours.SimpleBehaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.domain.FIPAAgentManagement.AMSAgentDescription;
import jade.domain.FIPAAgentManagement.SearchConstraints;
import jade.domain.AMSService;

public class Dispatcher extends Agent
{
	class StandbyBehaviour extends SimpleBehaviour
	{
		public StandbyBehaviour()
		{
			super(Dispatcher.this);
		}

		@Override
		public void action()
		{
			Dispatcher.this.generatePayload();
			ACLMessage msg=this.myAgent.receive();

			if(msg==null)
			{
				return;
			}

			ACLMessage reply=msg.createReply();
			if(msg.getPerformative()==ACLMessage.REQUEST && Dispatcher.this.state==State.standby && !Dispatcher.this.buffer.isEmpty())
			{
				reply.setPerformative(ACLMessage.AGREE);
				final Payload payload=Dispatcher.this.startLoading(msg);
				reply.setContent(payload.toString());
				this.myAgent.send(reply);
				this.block(payload.weight);
				return;
			}

			reply.setPerformative(ACLMessage.REFUSE);
			reply.setContent(this.myAgent.getLocalName()+" is unavailable.");
			this.myAgent.send(msg);
		}

		@Override
		public boolean done()
		{
			return Dispatcher.this.readyToDelete();
		}
		
		@Override
		public void restart()
		{
			Dispatcher.this.finishLoading();
		}
	}

	class BusyBehaviour extends CyclicBehaviour
	{
		public BusyBehaviour()
		{
			super(Dispatcher.this);
		}

		@Override
		public void action()
		{
			ACLMessage msg=this.myAgent.receive();

			if(msg==null)
			{
				return;
			}

			ACLMessage reply=msg.createReply();
			reply.setPerformative(ACLMessage.REFUSE);
			reply.setContent(this.myAgent.getLocalName()+" is busy.");
			this.myAgent.send(reply);
		}
	}

	public static final String exitMessage="Dispatcher exit";
	private int restOfPayload=20;
	private ArrayDeque<Payload> buffer=new ArrayDeque<Payload>();
	private ArrayList<Payload> dispatched=new ArrayList<Payload>();
	private State state=State.standby;
	private Random random=new Random();
	private BusyBehaviour busyBehaviour=new BusyBehaviour();

	@Override
	protected void setup()
	{
		this.addBehaviour(new StandbyBehaviour());
		Recorder.println(this.getLocalName()+" has started.");
	}

	private void generatePayload()
	{
		if(this.buffer.size()>=this.restOfPayload || this.random.nextInt(10)>1)
		{
			return;
		}

		this.buffer.push(new Payload(random.nextInt(3), random.nextInt(3000)+1000));
	}

	private Payload startLoading(final ACLMessage msg)
	{
		Dispatcher.this.state=State.busy;
		this.addBehaviour(this.busyBehaviour);
		this.dispatched.add(this.buffer.peekFirst());
		Recorder.println(this.getLocalName()+" starts loading ["+this.buffer.peekFirst().toString()+"] to "+msg.getSender().getLocalName());
		return this.buffer.peekFirst();
	}

	private void finishLoading()
	{
		Recorder.println(this.getLocalName()+" finishes loading ["+this.buffer.pollFirst().toString()+"]");
		this.removeBehaviour(this.busyBehaviour);
		this.state=State.standby;
		--this.restOfPayload;
	}

	private boolean readyToDelete()
	{
		if(this.restOfPayload>0)
		{
			return false;
		}

		ACLMessage template=new ACLMessage(ACLMessage.INFORM);
		template.setContent(Dispatcher.exitMessage);
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

					while(true)
					{
						ACLMessage reply=this.receive();
						if(reply!=null && reply.getPerformative()==ACLMessage.CONFIRM)
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
