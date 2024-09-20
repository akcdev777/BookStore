import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;

public class Main {
    public static void main(String[] args) throws StaleProxyException {
        Runtime rt = Runtime.instance();

        // Create a profile for the main container (includes DF and AMS)
        Profile pMain = new ProfileImpl("localhost", 1099, "MyPlatform");  // Set your IP, port, and platform name
        System.out.println("Launching the main container... " + pMain);

        // Create the main container
        ContainerController mainContainer = rt.createMainContainer(pMain);

        // Optional: Create additional containers if needed (for distributed agents)
        // ProfileImpl pContainer = new ProfileImpl("localhost", 1099, "MyPlatform");
        // pContainer.setParameter(Profile.CONTAINER_NAME, "Mycontainer1");
        // ContainerController container = rt.createAgentContainer(pContainer);

        // Step 2: Deploy your BookBuyerAgent
        String bookToBuy = "Effective Java"; // The book the agent is looking for
        Object[] agentArgs = new Object[]{bookToBuy};

        System.out.println("Starting BookBuyerAgent...");
        AgentController agentController = mainContainer.createNewAgent(
                "buyer", // Agent name
                BookBuyerAgent.class.getName(), // Agent class name
                agentArgs // Agent arguments (e.g., book to buy)
        );
        agentController.start();

        // Launch BookSellerAgent
        System.out.println("Starting BookSellerAgent...");
        // Launch BookSellerAgent with a catalog of books and prices
        Object[] seller1Args = new Object[]{"Cindarella", "150", "Effective Java", "123"};
        AgentController seller1 = mainContainer.createNewAgent(
                "seller1",
                BookSellerAgent.class.getName(),
                seller1Args
        );
        seller1.start();


        // Launch BookDistributorAgent
        // Launch BookSellerAgent with a catalog of books and prices
        Object[] seller2Args = new Object[]{"John Wick", "150", "Effective Python", "123"};
        AgentController seller2 = mainContainer.createNewAgent(
                "seller2",
                BookSellerAgent.class.getName(),
                seller2Args
        );
        seller2.start();


    }
}