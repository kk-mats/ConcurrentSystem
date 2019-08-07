package assignment;

import java.util.ArrayList;
import java.util.stream.Collectors;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class Port extends Agent
{
	class Behaviour extends SimpleBehaviour
	{
		private boolean exitReceived=false;
		private PortState state=PortState.ready_to_receive;
		private MessageTemplate recevingFilter;
		private MessageTemplate exitFilter;

		public Behaviour()
		{
			super(Port.this);
		}

		@Override
		public void action()
		{
			switch(this.state)
			{
				case ready_to_receive:
				{
					ACLMessage msg=this.myAgent.receive();
					if(msg==null)
					{
						return;
					}

					if(msg.getPerformative()==ACLMessage.REQUEST)
					{
						this.state=PortState.receiving;
						this.recevingFilter=MessageTemplate.and(
							MessageTemplate.MatchSender(msg.getSender()),
							MessageTemplate.MatchPerformative(ACLMessage.CONFIRM)
						);
						this.exitFilter=MessageTemplate.and(
							MessageTemplate.MatchSender(new AID("Dispatcher", AID.ISLOCALNAME)),
							MessageTemplate.MatchPerformative(ACLMessage.INFORM)
						);

						ACLMessage reply=msg.createReply();
						reply.setPerformative(ACLMessage.AGREE);
						this.myAgent.send(reply);
						Port.this.buffer.add(Payload.fromString(msg.getContent()));
						Recorder.println(this.myAgent.getLocalName()+" starts receiving ["+msg.getContent()+"] from "+msg.getSender().getLocalName());
						return;
					}
					this.exitReceived=msg.getPerformative()==ACLMessage.INFORM;
					return;
				}

				case receiving:
				{
					ACLMessage msg=this.myAgent.blockingReceive(MessageTemplate.or(this.recevingFilter, this.exitFilter), Port.this.buffer.get(Port.this.buffer.size()-1).weight);

					if(msg==null)
					{
						msg=this.myAgent.blockingReceive(MessageTemplate.or(this.recevingFilter, this.exitFilter));
					}

					if(msg.getPerformative()==ACLMessage.CONFIRM)
					{
						Recorder.println(this.myAgent.getLocalName()+" finishes receiving ["+Port.this.buffer.get(Port.this.buffer.size()-1).toString()+"] from "+msg.getSender().getLocalName());
					}
					else if(msg.getPerformative()==ACLMessage.INFORM)
					{
						this.myAgent.blockingReceive(this.recevingFilter);
						this.exitReceived=true;
					}
					this.state=PortState.ready_to_receive;
					return;
				}
			}
		}

		@Override
		public boolean done()
		{
			if(this.state==PortState.ready_to_receive && this.exitReceived)
			{
				ACLMessage msg=new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
				msg.addReceiver(new AID("Dispatcher", AID.ISLOCALNAME));
				this.myAgent.send(msg);
				this.myAgent.doDelete();
				return true;
			}
			return false;
		}
	}

	private ArrayList<Payload> buffer=new ArrayList<Payload>();

	@Override
	protected void setup()
	{
		this.addBehaviour(new Behaviour());
		Recorder.println(this.getLocalName()+" has started.");
	}

	@Override
	protected void takeDown()
	{
		Recorder.println(this.getLocalName()+" has exited. received=[\n"+this.buffer.stream().map((Payload p)->"  ["+p.toString()+"]").collect(Collectors.joining(",\n"))+"\n]");
	}
}
