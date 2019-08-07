package assignment;

public class Payload
{
	public final int destination;
	public final int weight;

	Payload(final int destination, final int weight)
	{
		this.destination=destination;
		this.weight=weight;
	}

	public String toString()
	{
		return this.destination+":"+this.weight;
	}

	static public Payload fromString(final String string)
	{
		final String[] values=string.split(":");
		return new Payload(Integer.valueOf(values[0]), Integer.valueOf(values[1]));
	}
}
