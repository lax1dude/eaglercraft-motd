package net.lax1dude.eaglercraft.eaglermotd;

import java.util.List;

import org.json.JSONObject;

public class MessagePoolEntry implements Comparable<MessagePoolEntry> {

	public final String name;
	public final int interval;
	public final int timeout;
	public final boolean random;
	public final boolean shuffle;
	public final float weight;
	public final String next;
	public final List<JSONObject> frames;
	
	public MessagePoolEntry(int interval, int timeout, boolean random, boolean shuffle, float weight, String next, List<JSONObject> frames, String name) {
		this.interval = interval;
		this.timeout = timeout;
		this.random = random;
		this.shuffle = shuffle;
		this.weight = weight;
		this.next = next;
		this.frames = frames;
		this.name = name;
	}

	@Override
	public int compareTo(MessagePoolEntry o) {
		return Float.compare(weight, o.weight);
	}
	
}
