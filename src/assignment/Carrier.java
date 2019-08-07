package assignment;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class Carrier extends Agent
{
	class Behaviour extends SimpleBehaviour
	{
		private CarrierState state=CarrierState.ready_to_load;
		private boolean exitReceived=false;

		private MessageTemplate loadingFilter;
		private MessageTemplate unloadingFilter;
		private MessageTemplate exitFilter;


		public Behaviour()
		{
			super(Carrier.this);
		}

		@Override
		public void action()
		{
			switch(this.state)
			{
				case ready_to_load:
				{
					ACLMessage msg=new ACLMessage(ACLMessage.REQUEST);
					msg.addReceiver(new AID("Dispatcher", AID.ISLOCALNAME));
					this.myAgent.send(msg);

					this.loadingFilter=MessageTemplate.and(
						MessageTemplate.MatchSender(new AID("Dispatcher", AID.ISLOCALNAME)),
						MessageTemplate.MatchPerformative(ACLMessage.AGREE)
					);
					this.exitFilter=MessageTemplate.and(
						MessageTemplate.MatchSender(new AID("Dispatcher", AID.ISLOCALNAME)),
						MessageTemplate.MatchPerformative(ACLMessage.INFORM)
					);

					msg=this.myAgent.blockingReceive(MessageTemplate.or(this.loadingFilter, this.exitFilter));

					if(msg.getPerformative()==ACLMessage.AGREE && msg.getContent()!=null)
					{
						this.state=CarrierState.loading;
						Carrier.this.payload=Payload.fromString(msg.getContent());
						Carrier.this.portAID=new AID("Port"+Carrier.this.payload.destination, AID.ISLOCALNAME);
						this.block(Carrier.this.payload.weight);
						return;
					}

					this.exitReceived=msg.getPerformative()==ACLMessage.INFORM;
					return;
				}

				case loading:
				{
					ACLMessage msg=new ACLMessage(ACLMessage.CONFIRM);
					msg.addReceiver(new AID("Dispatcher", AID.ISLOCALNAME));
					this.myAgent.send(msg);
					this.state=CarrierState.ready_to_unload;
					return;
				}

				case ready_to_unload:
				{
					ACLMessage msg=new ACLMessage(ACLMessage.REQUEST);
					msg.addReceiver(Carrier.this.portAID);
					msg.setContent(Carrier.this.payload.toString());
					this.myAgent.send(msg);
					this.unloadingFilter=MessageTemplate.and(
							MessageTemplate.MatchSender(Carrier.this.portAID),
							MessageTemplate.MatchPerformative(ACLMessage.AGREE)
					);
					this.exitFilter=MessageTemplate.and(
						MessageTemplate.MatchSender(new AID("Dispatcher", AID.ISLOCALNAME)),
						MessageTemplate.MatchPerformative(ACLMessage.INFORM)
					);

					msg=this.myAgent.blockingReceive(MessageTemplate.or(this.unloadingFilter, this.exitFilter));

					if(msg.getPerformative()==ACLMessage.AGREE)
					{
						this.state=CarrierState.unloading;
						this.block(Carrier.this.payload.weight);
						return;
					}

					this.exitReceived=msg.getPerformative()==ACLMessage.INFORM;
					return;
				}

				case unloading:
				{
					ACLMessage msg=new ACLMessage(ACLMessage.CONFIRM);
					msg.addReceiver(Carrier.this.portAID);
					this.myAgent.send(msg);
					this.state=CarrierState.ready_to_load;
					return;
				}
			}
		}

		@Override
		public boolean done()
		{
			if(this.state==CarrierState.ready_to_load && this.exitReceived)
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

	private Payload payload=null;
	private AID portAID=null;

	@Override
	protected void setup()
	{
		this.addBehaviour(new Behaviour());
		Recorder.println(this.getLocalName()+" has started.");
	}

	@Override
	protected void takeDown()
	{
		Recorder.println(this.getLocalName()+" has exited.");
	}
}
