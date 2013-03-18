package eu.orionos.build;

public class FailedException extends Exception {

	private String msg;
	/**
	 *
	 */
	private static final long serialVersionUID = 3275320329877934820L;

	public FailedException(String msg)
	{
		this.msg = msg;
	}

	public String getMsg()
	{
		return msg;
	}

}
