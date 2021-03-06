package com.intellectualcrafters.plot.logger;

public class DelegateLogger implements ILogger {

	private final ILogger parent;

	public DelegateLogger(ILogger parent)
	{
		this.parent = parent;
	}

	public ILogger getParent()
	{
		return this.parent;
	}

	@Override
	public void log(String message)
	{
		this.parent.log(message);
	}
}
