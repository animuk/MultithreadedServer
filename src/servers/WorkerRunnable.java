package servers;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

/**

 */
public class WorkerRunnable implements Runnable {

	protected Socket clientSocket = null;
	protected String serverText = null;

	public WorkerRunnable(Socket clientSocket, String serverText) {
		this.clientSocket = clientSocket;
		this.serverText = serverText;
	}

	public void run() {
		try {
			BufferedReader input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			DataOutputStream output = new DataOutputStream(clientSocket.getOutputStream());
			long time = System.currentTimeMillis();
			handleHTTPTraffic(input, output);
			System.out.println("Request processed: " + time);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * This Method snoops the traffic and determine the resource type from the
	 * http header, then read the filestream write the stream in the output
	 * 
	 * @param input
	 * @param output
	 */
	private void handleHTTPTraffic(BufferedReader input, DataOutputStream output) {
		String strPath = "";
		String strInputStream = "";

		try {
			strInputStream = input.readLine();
			strPath = showFilePath(strInputStream);
			System.out.println("strPath----->" + strPath);
			if (validateFilePath(showFilePath(strInputStream))) {
				output.writeBytes((strPath.endsWith(".zip")) ? setHttpHeaders(200, 3)
						: (strPath.endsWith(".jpg") || strPath.endsWith(".jpeg")) ? setHttpHeaders(200, 1)
								: (strPath.endsWith(".gif") || strPath.endsWith(".png")) ? setHttpHeaders(200, 2)
										: (strPath.endsWith(".ico")) ? setHttpHeaders(200, 4) : setHttpHeaders(200, 5));

				if (strInputStream.toUpperCase().startsWith("GET"))
					callGETrequest(strPath, output);
				else if (strInputStream.toUpperCase().startsWith("HEAD"))
					callHEADrequest();
				else {
					output.writeBytes(setHttpHeaders(501, 0));
					output.close();
					return;
				}

			} else {
				output.writeBytes(setHttpHeaders(404, 0));
				// close the stream
				output.close();
				return;
			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (output != null)
					output.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}

	}

	/**
	 * This method is used to validate the File path exists or not.
	 * 
	 * @param strFilePath
	 * @return
	 */
	private boolean validateFilePath(String strFilePath) {
		FileInputStream requestedfile = null;
		try {
			System.out.println("\nClient requested:" + new File(strFilePath).getAbsolutePath() + "\n");
			requestedfile = new FileInputStream(strFilePath);
			return true;

		} catch (Exception e) {
			return false;
		} finally {
			try {
				if (requestedfile != null)
					requestedfile.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}

	/**
	 * This method snoops the input stream header and captures the resource name by cutting the get request
	 * @param strInputStream
	 * @return
	 */
	private String showFilePath(String strInputStream) {
		int start = 0;
		int end = 0;
		for (int a = 0; a < strInputStream.length(); a++) {
			if (strInputStream.charAt(a) == ' ' && start != 0) {
				end = a;
				break;
			}
			if (strInputStream.charAt(a) == ' ' && start == 0) {
				start = a;
			}
		}
		return strInputStream.substring(start + 2, end);
	}

	private void callHEADrequest() {
		// Do not print anything

	}

	/**
	 * This is the method where we read the filestream and write the output
	 * @param strFilePath
	 * @param output
	 */
	private void callGETrequest(String strFilePath, DataOutputStream output) {
		FileInputStream requestedfile = null;
		try {
			requestedfile = new FileInputStream(strFilePath);
			int content;
			while ((content = requestedfile.read()) != -1) {
				output.write(content);
			}

		} catch (IOException e) {
			e.printStackTrace();

		} finally {
			try {
				if (requestedfile != null)
					requestedfile.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}

	}

	/**
	 * This is the method where we set the appropriate the HTTP Header in the response
	 * @param statuscode
	 * @param multipart_type
	 * @return
	 */
	private String setHttpHeaders(int statuscode, int multipart_type) {

		System.out.println("statuscode----->" + statuscode);
		System.out.println("multipart_type----->" + multipart_type);
		StringBuilder strResHeaderBuilder = new StringBuilder();
		strResHeaderBuilder.append("HTTP/1.1 ");
		strResHeaderBuilder = (statuscode == 200) ? strResHeaderBuilder.append("200 OK")
				: (statuscode == 400) ? strResHeaderBuilder.append("400 Bad Request")
						: (statuscode == 403) ? strResHeaderBuilder.append("403 Forbidden")
								: (statuscode == 404) ? strResHeaderBuilder.append("404 Not Found")
										: (statuscode == 500) ? strResHeaderBuilder.append("500 Internal Server Error")
												: (statuscode == 501)
														? strResHeaderBuilder.append("501 Not Implemented")
														: strResHeaderBuilder.append("");
		strResHeaderBuilder = strResHeaderBuilder.append("\r\n");
		strResHeaderBuilder = strResHeaderBuilder.append("Connection: Close\r\n");
		strResHeaderBuilder = strResHeaderBuilder.append("Server: Multithreaded Server v0\r\n");

		strResHeaderBuilder = (multipart_type == 1) ? strResHeaderBuilder.append("Content-Type: image/jpeg\r\n")
				: (multipart_type == 2) ? strResHeaderBuilder.append("Content-Type: image/gif\r\n")
						: (multipart_type == 3)
								? strResHeaderBuilder.append("Content-Type: application/x-zip-compressed\r\n")
								: (multipart_type == 4) ? strResHeaderBuilder.append("Content-Type: image/x-icon\r\n")
										: strResHeaderBuilder.append("Content-Type: text/html\r\n");

		strResHeaderBuilder = strResHeaderBuilder.append("\r\n");
		return strResHeaderBuilder.toString();
	}

}