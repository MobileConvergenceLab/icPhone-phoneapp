/**
 * Callback interface to connect front-end and back-end
 */
public interface CCNServiceCallback {
	public void receiveData(String data);
	public void receiveMessage(String msg);
}
