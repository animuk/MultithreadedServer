package servers;

public class MainServer {

	public static void main(String[] args) {
		MultiThreadedServer server = new MultiThreadedServer(9000);
		new Thread(server).start();

		try {
			//We are keeping the server alive for 200 second before we call the stop method to terminate the server
		    Thread.sleep(200 * 1000);
		} catch (InterruptedException e) {
		    e.printStackTrace();
		}
		System.out.println("Stopping Server");
		server.stop();

	}

}
