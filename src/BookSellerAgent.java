import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.HashMap;
import java.util.Map;

public class BookSellerAgent extends Agent {
    private Map<String, Integer> catalog;
    //private BookSellerDialog dialog;

    protected void setup() {
        System.out.println("[" + getAID().getName() + "] agent is ready");

        catalog = new HashMap<>();

        Object[] args = getArguments();
        if (args != null && args.length > 0) {
            for (int i = 0; i < args.length; i += 2) {
                String bookTitle = (String) args[i];
                int bookPrice = Integer.parseInt((String) args[i + 1]);
                catalog.put(bookTitle, bookPrice);
            }
        }else{
            doDelete();
        }

        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("book-seller");
        sd.setName("JADE-book-trading");
        dfd.addServices(sd);

        try{
            DFService.register(this, dfd);
        } catch (FIPAException e) {
            e.printStackTrace();
        }
    }

    protected void takeDown() {
        // Deregister the seller agent from the DF
        try {
            DFService.deregister(this);
            System.out.println("[" + getAID().getName() + "] successfully deregistered from the DF.");
        } catch (FIPAException e) {
            System.out.println("[" + getAID().getName() + "] failed to deregister from the DF: " + e.getMessage());
        }
        System.out.println("[" + getAID().getName() + "] agent terminated.");
    }

    // Behaviour to handle incoming CFP messages from buyers
    private class OfferRequestsServer extends CyclicBehaviour {
        @Override
        public void action() {
            // Template to receive CFP (Call for Proposal) messages from buyers
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.CFP);
            ACLMessage msg = myAgent.receive(mt);

            if (msg != null) {
                // CFP message received. Process it
                String bookTitle = msg.getContent();
                ACLMessage reply = msg.createReply();

                // Check if the requested book is available in the catalog
                Integer price = catalog.get(bookTitle);
                if (price != null) {
                    // The book is available. Send a PROPOSE response with the price
                    reply.setPerformative(ACLMessage.PROPOSE);
                    reply.setContent(String.valueOf(price));
                    System.out.println(getAID().getLocalName() + ": Proposing price " + price + " for book " + bookTitle);
                } else {
                    // The book is not available. Send a REFUSE response
                    reply.setPerformative(ACLMessage.REFUSE);
                    reply.setContent("not-available");
                    System.out.println(getAID().getLocalName() + ": Book " + bookTitle + " is not available.");
                }
                myAgent.send(reply);
            } else {
                block(); // Wait for incoming messages
            }
        }
    }

}
