package assignment;

import java.util.ArrayList;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Recorder
{
	private static ArrayList<String> buffer=new ArrayList<String>();
	private static final DateTimeFormatter format=DateTimeFormatter.ofPattern("@HH:mm:ss:SSSS  ");

	synchronized public static void println(final String line)
	{
		Recorder.record(line);
		System.out.println(line);
	}

	synchronized public static void trace(final String line)
	{
		Recorder.record(line);
	}

	synchronized public static void dump()
	{
		Recorder.buffer.stream().forEach(System.out::println);
	}

	private static void record(final String line)
	{
		Recorder.buffer.add(Recorder.format.format(LocalDateTime.now().toLocalTime())+line);
	}
}
