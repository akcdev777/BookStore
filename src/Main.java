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

        // Optional: Add more agents if necessary
        // Step 3: Monitoring agents like RMA or sniffer can be added here

        System.out.println("Platform and agent initialized successfully.");

    }

}