import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;

import java.util.HashMap;
import java.util.Map;

public class BookSellerAgent extends Agent {
    private Map<String, Integer> catalog;
    //private BookSellerDialog dialog;

    protected void setup() {
        System.out.println("[" + getAID().getName() + "] agent is ready");

        catalog = new HashMap<>();
        catalog.put("Cindarella",150);
        catalog.put("Effective Java",123);

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
}
