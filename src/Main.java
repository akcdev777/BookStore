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
        Object[] sellerArgs1 = new Object[]{"Cinderella", "150", "10", "Effective Java", "123", "5"};
        System.out.println("Starting BookSellerAgent1...");
        AgentController sellerAgent1 = mainContainer.createNewAgent(
                "seller1", // Agent name
                BookSellerAgent.class.getName(), // Agent class name
                sellerArgs1 // Agent arguments (bookTitle, bookPrice, quantity)
        );
        sellerAgent1.start(); // Start the seller agent

        // Optionally, launch another seller agent with a different catalog
        Object[] sellerArgs2 = new Object[]{"Harry Potter", "200", "3", "Effective Java", "180", "2"};
        System.out.println("Starting BookSellerAgent2...");
        AgentController sellerAgent2 = mainContainer.createNewAgent(
                "seller2", // Another seller agent
                BookSellerAgent.class.getName(),
                sellerArgs2 // Agent arguments (bookTitle, bookPrice, quantity)
        );
        sellerAgent2.start(); // Start the second seller agent


    }
}