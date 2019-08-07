package assignment;

import java.util.ArrayList;
import java.util.stream.Collectors;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.SimpleBehaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

public class Port extends Agent
{
	class StandbyBehaviour extends SimpleBehaviour
	{
		public StandbyBehaviour()
		{
			super(Port.this);
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
			if(msg.getPerformative()==ACLMessage.REQUEST && Port.this.state==State.standby)
			{
				reply.setPerformative(ACLMessage.AGREE);
				reply.setContent(this.myAgent.getLocalName()+" is available.");
				this.myAgent.send(reply);
				this.block(Port.this.startUnloading(msg));
				return;
			}
			else if(msg.getPerformative()==ACLMessage.INFORM)
			{
				Port.this.exitReceived=true;
				return;
			}

			reply.setPerformative(ACLMessage.REFUSE);
			reply.setContent(this.myAgent.getLocalName()+" is unavailable.");
			this.myAgent.send(reply);
		}

		@Override
		public boolean done()
		{
			if(Port.this.exitReceived)
			{
				ACLMessage msg=new ACLMessage(ACLMessage.CONFIRM);
				msg.addReceiver(new AID("Dispatcher", AID.ISLOCALNAME));
				Port.this.send(msg);

				Port.this.doDelete();
				return true;
			}
			return false;
		}

		@Override
		public void restart()
		{
			Port.this.finishUnloading();
		}
	};

	class BusyBehaviour extends CyclicBehaviour
	{
		public BusyBehaviour()
		{
			super(Port.this);
		}

		@Override
		public void action()
		{
			ACLMessage msg=this.myAgent.receive();

			if(msg==null)
			{
				return;
			}

			if(msg.getPerformative()==ACLMessage.INFORM)
			{
				Port.this.exitReceived=true;
				return;
			}

			ACLMessage reply=msg.createReply();
			reply.setPerformative(ACLMessage.REFUSE);
			reply.setContent(this.myAgent.getLocalName()+" is busy.");

			this.myAgent.send(reply);
		}
	}

	private State state=State.standby;
	private ArrayList<Payload> buffer=new ArrayList<Payload>();
	private BusyBehaviour busyBehaviour=new BusyBehaviour();
	private boolean exitReceived=false;

	@Override
	protected void setup()
	{
		this.addBehaviour(new StandbyBehaviour());
		Recorder.println(this.getLocalName()+" has started.");
	}

	private int startUnloading(final ACLMessage msg)
	{
		this.state=State.busy;
		Payload p=Payload.fromString(msg.getContent());
		this.buffer.add(p);
		this.addBehaviour(this.busyBehaviour);
		Recorder.println(this.getLocalName()+" starts unloading ["+p.toString()+"] from "+msg.getSender().getLocalName());
		return p.weight;
	}

	private void finishUnloading()
	{
		if(this.buffer.size()>0)
		{
			Recorder.println(this.getLocalName()+" finishes unloading ["+this.buffer.get(this.buffer.size()-1).toString()+"]");
			this.removeBehaviour(this.busyBehaviour);
			this.state=State.standby;
		}
	}

	@Override
	protected void takeDown()
	{
		Recorder.println(this.getLocalName()+" has exited. received=[\n"+this.buffer.stream().map((Payload p)->"  ["+p.toString()+"]").collect(Collectors.joining(",\n"))+"\n]");
	}
}
