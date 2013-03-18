package eu.orionos.build;

public class DisabledException extends Exception {

	private String msg;

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	public DisabledException(String msg) {
		this.msg = msg;
	}

	public String getMsg()
	{
		return this.msg;
	}

}
