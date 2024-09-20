
import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class BookBuyerAgent extends Agent{

    private String targetBook;
    private AID[] sellerAgents = { new AID("seller1", AID.ISLOCALNAME), new AID("seller2", AID.ISLOCALNAME)};

    protected void setup(){
        System.out.println("Hello from " + getAID().getName() + "!");

        Object[] args = getArguments();
        if (args != null && args.length > 0){
            targetBook = (String) args[0];
            System.out.println("I am looking for the book: " + targetBook);

            addBehaviour(new TickerBehaviour(this, 5000) {
                @Override
                protected void onTick() {
                    System.out.println("Buyer agent on ticker " + "");

                    //Update list of seller agents
                    DFAgentDescription template = new DFAgentDescription();
                    ServiceDescription sd = new ServiceDescription();
                    sd.setType("book seller");
                    template.addServices(sd);

                    try{
                        DFAgentDescription[] result = DFService.search(myAgent, template);
                        System.out.println("Found the following seller agents:");
                        sellerAgents = new AID[result.length];
                        for (int i = 0; i < result.length; i++){
                            sellerAgents[i] = result[i].getName();
                            System.out.println(sellerAgents[i].getName());
                        }
                    } catch (FIPAException e) {
                        e.printStackTrace();
                    }

                    myAgent.addBehaviour(new RequestPerformer());

                }
            });
        }
        else{
            System.out.println("No book specified in the agent's arguments");
            doDelete();
        }
    }

    protected void takeDown(){
        System.out.println("Goodbye from " + getAID().getName() + "!");
    }


    private class RequestPerformer extends Behaviour {
        private static final String CONVERSATION_ID = "book-trade";

        private AID bestSeller;
        private int bestPrice;
        private int repliesCount = 0;
        private MessageTemplate msgTemplate;
        private int step = 0;


        @Override
        public void action() {
            System.out.println("RequestPerformer.action: step=" + step);
            switch (step) {
                case 0:
                    ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
                    for (AID sellerAgent : sellerAgents) {
                        cfp.addReceiver(sellerAgent);
                    }
                    cfp.setContent(targetBook);
                    cfp.setConversationId(CONVERSATION_ID);
                    cfp.setReplyWith("cfp" + System.currentTimeMillis());
                    myAgent.send(cfp);

                    msgTemplate = MessageTemplate.and(
                            MessageTemplate.MatchConversationId(CONVERSATION_ID),
                            MessageTemplate.MatchInReplyTo(cfp.getReplyWith()));
                    step = 1;
                    break;
                case 1:
                    ACLMessage reply = myAgent.receive(msgTemplate);
                    if (reply != null) {
                        if (reply.getPerformative() == ACLMessage.PROPOSE) {
                            int price = Integer.parseInt(reply.getContent());
                            if (bestSeller == null || price < bestPrice) {
                                bestPrice = price;
                                bestSeller = reply.getSender();
                            }
                        }
                        repliesCount++;
                        if (repliesCount >= sellerAgents.length) {
                            step = 2;
                        }
                    } else {
                        block();
                    }
                    break;
                case 2:
                    ACLMessage order = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
                    order.addReceiver(bestSeller);
                    order.setContent(targetBook);
                    order.setConversationId(CONVERSATION_ID);
                    order.setReplyWith("order" + System.currentTimeMillis());
                    myAgent.send(order);
                    msgTemplate = MessageTemplate.and(
                            MessageTemplate.MatchConversationId(CONVERSATION_ID),
                            MessageTemplate.MatchInReplyTo(order.getReplyWith()));
                    step = 3;
                    break;
                case 3:
                    reply = myAgent.receive(msgTemplate);
                    if (reply != null) {
                        if (reply.getPerformative() == ACLMessage.INFORM) {
                            System.out.println(targetBook + " successfully purchased at price " + bestPrice + " UAH");
                            myAgent.doDelete();
                        }
                        step = 4;
                    } else {
                        block();
                    }
                    break;
            }

        }

        @Override
        public boolean done() {
            return ((step == 2 && bestSeller == null) || step == 4);
        }
    }
}
