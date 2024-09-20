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

    //private BookSellerDialog dialog;

    private class BookInfo{
        int price;
        int quantity;

        BookInfo(int price, int quantity){
            this.price = price;
            this.quantity = quantity;
        }

        @Override
        public String toString(){
            return "Price: " + price + ", Quantity: " + quantity;
        }
    }//end BookInfo

    private Map<String, BookInfo> catalog;

    protected void setup() {
        System.out.println("[" + getAID().getName() + "] agent is ready");

        catalog = new HashMap<>();

        Object[] args = getArguments();
        if (args != null && args.length > 0) {
            if (args.length % 3 != 0) {
                System.out.println("Invalid number of arguments. Each book should have a title, price, and quantity.");
                doDelete();
                return;
            }

            for (int i = 0; i < args.length; i += 3) {
                String bookTitle = (String) args[i];
                int bookPrice;
                int quantity;
                try {
                    bookPrice = Integer.parseInt((String) args[i + 1]);
                    quantity = Integer.parseInt((String) args[i + 2]);
                } catch (NumberFormatException e) {
                    System.out.println("Invalid price or quantity for book: " + bookTitle);
                    e.printStackTrace();
                    doDelete();
                    return;
                }
                catalog.put(bookTitle, new BookInfo(bookPrice, quantity));
                System.out.println("Added to catalog: " + bookTitle + " | Price: " + bookPrice + " | Quantity: " + quantity);
            }
        } else {
            System.out.println("No books provided. Terminating agent.");
            doDelete();
            return;
        }

        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("book-seller");
        sd.setName("JADE-book-trading");
        dfd.addServices(sd);

        try {
            DFService.register(this, dfd);
            System.out.println("[" + getAID().getName() + "] registered successfully with DF");
        } catch (FIPAException e) {
            System.out.println("[" + getAID().getName() + "] failed to register with DF");
            e.printStackTrace();
        }

        addBehaviour(new OfferRequestsServer());
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
                String bookTitle = msg.getContent();
                ACLMessage reply = msg.createReply();

                switch (msg.getPerformative()) {
                    case ACLMessage.CFP:
                        // Handle Call For Proposal (CFP)
                        BookInfo bookInfo = catalog.get(bookTitle);
                        if (bookInfo != null && bookInfo.quantity > 0) {
                            // The requested book is available and in stock
                            reply.setPerformative(ACLMessage.PROPOSE);
                            reply.setContent(String.valueOf(bookInfo.price));
                            System.out.println("[" + getAID().getName() + "] Proposing price " + bookInfo.price + " for book " + bookTitle);
                        } else {
                            // The requested book is not available or out of stock
                            reply.setPerformative(ACLMessage.REFUSE);
                            reply.setContent("not-available");
                            System.out.println("[" + getAID().getName() + "] Book " + bookTitle + " not available or out of stock.");
                        }
                        break;

                    case ACLMessage.ACCEPT_PROPOSAL:
                        // Handle ACCEPT_PROPOSAL (purchase confirmation)
                        bookInfo = catalog.get(bookTitle);
                        if (bookInfo != null && bookInfo.quantity > 0) {
                            // Decrease the quantity of the book
                            bookInfo.quantity--;
                            reply.setPerformative(ACLMessage.INFORM);
                            System.out.println("[" + getAID().getName() + "] Book " + bookTitle + " sold to " + msg.getSender().getLocalName() + ". Remaining quantity: " + bookInfo.quantity);

                            // If the quantity reaches 0, consider removing it from the catalog (optional)
                            if (bookInfo.quantity == 0) {
                                System.out.println("[" + getAID().getName() + "] Book " + bookTitle + " is now out of stock.");
                            }
                        } else {
                            reply.setPerformative(ACLMessage.FAILURE);
                            System.out.println("[" + getAID().getName() + "] Failed to sell " + bookTitle + ". Out of stock.");
                        }
                        break;

                    default:
                        // Ignore other message types
                        block();
                        return;
                }

                // Send the reply
                myAgent.send(reply);
            } else {
                block();
            }
        }
    }

}
