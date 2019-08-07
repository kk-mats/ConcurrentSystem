package assignment;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;

public class Carrier extends Agent
{
	class Behaviour extends SimpleBehaviour
	{
		private boolean exitReceived=false;
		private boolean requestSent=false;

		public Behaviour()
		{
			super(Carrier.this);
		}

		@Override
		public void action()
		{
			if(Carrier.this.state==State.standby)
			{
				if(!this.requestSent)
				{
					ACLMessage msg=new ACLMessage(ACLMessage.REQUEST);
					msg.addReceiver(new AID("Dispatcher", AID.ISLOCALNAME));
					this.myAgent.send(msg);
					this.requestSent=true;
				}

				ACLMessage msg=this.myAgent.receive();
				if(msg==null)
				{
					return;
				}
				if(msg.getPerformative()==ACLMessage.AGREE)
				{
					this.requestSent=false;
					this.block(Carrier.this.load(Payload.fromString(msg.getContent())));
					return;
				}

				this.exitReceived=msg.getPerformative()==ACLMessage.INFORM;
			}
			else
			{
				if(!this.requestSent && Carrier.this.portAID!=null)
				{
					ACLMessage msg=new ACLMessage(ACLMessage.REQUEST);
					msg.addReceiver(Carrier.this.portAID);
					msg.setContent(Carrier.this.payload.toString());
					this.myAgent.send(msg);
					this.requestSent=true;
				}

				ACLMessage msg=this.myAgent.receive();
				if(msg==null)
				{
					return;
				}

				if(msg.getPerformative()==ACLMessage.AGREE)
				{
					this.requestSent=false;
					this.block(Carrier.this.unload());
					return;
				}

				this.exitReceived=msg.getPerformative()==ACLMessage.INFORM;
			}
		}

		@Override
		public boolean done()
		{
			if(this.exitReceived && Carrier.this.payload==null)
			{
				ACLMessage msg=new ACLMessage(ACLMessage.CONFIRM);
				msg.addReceiver(new AID("Dispatcher", AID.ISLOCALNAME));
				Carrier.this.send(msg);
				Carrier.this.doDelete();
				return true;
			}
			return false;
		}
	}

	private State state=State.standby;
	private Payload payload=null;
	private AID portAID=null;

	@Override
	protected void setup()
	{
		this.addBehaviour(new Behaviour());
		Recorder.println(this.getLocalName()+" has started.");
	}

	private int load(final Payload payload)
	{
		this.payload=payload;
		this.portAID=new AID("Port"+this.payload.destination, AID.ISLOCALNAME);
		this.state=State.busy;
		return this.payload.weight;
	}

	private int unload()
	{
		final int weight=this.payload.weight;
		this.payload=null;
		this.portAID=null;
		this.state=State.standby;

		return weight;
	}

	@Override
	protected void takeDown()
	{
		Recorder.println(this.getLocalName()+" has exited.");
	}
}
