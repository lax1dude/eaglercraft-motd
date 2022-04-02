package net.lax1dude.eaglercraft.eaglermotd;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class MessagePool {
	
	public final String poolName;
	public final List<MessagePoolEntry> messagePool = new LinkedList();
	public final Random random = new Random();
	
	public MessagePool(String s) {
		this.poolName = s;
	}
	
	public void sort() {
		Collections.sort(messagePool);
	}
	
	public MessagePoolEntry pickNew() {
		if(messagePool.size() <= 0) {
			return null;
		}
		float f = 0.0f;
		for(MessagePoolEntry m : messagePool) {
			f += m.weight;
		}
		f *= random.nextFloat();
		float f2 = 0.0f;
		for(MessagePoolEntry m : messagePool) {
			f2 += m.weight;
			if(f2 >= f) {
				return m;
			}
		}
		return messagePool.get(0);
	}

	public MessagePoolEntry pickDefault() {
		for(MessagePoolEntry m : messagePool) {
			if("default".equalsIgnoreCase(m.name)) {
				return m;
			}
		}
		return pickNew();
	}

}
