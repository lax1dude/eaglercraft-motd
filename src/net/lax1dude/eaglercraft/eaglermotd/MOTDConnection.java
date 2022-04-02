package net.lax1dude.eaglercraft.eaglermotd;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

import org.json.JSONArray;
import org.json.JSONObject;

import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.MOTD;
import net.md_5.bungee.api.config.ListenerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class MOTDConnection {
	
	public final ListenerInfo listener;
	public final String listenerName;
	public final MOTD motd;
	
	public MessagePoolEntry currentMessage = null;
	public int messageTimeTimer = 0;
	public int messageIntervalTimer = 0;
	public int currentFrame = 0;
	public int ageTimer = 0;

	public BitmapFile bitmap = null;
	public int spriteX = 0;
	public int spriteY = 0;
	public float[] color = new float[] { 0.0f, 0.0f, 0.0f, 0.0f };
	public float[] tint = new float[] { 0.0f, 0.0f, 0.0f, 0.0f };
	
	private Random rand = null;
	
	public MOTDConnection(ListenerInfo l, MOTD m) {
		this.motd = m;
		this.listener = l;
		this.listenerName = EaglerMOTD.makeListenerString(l.getHost());
	}
	
	public boolean execute() {
		MessagePool p = EaglerMOTD.messagePools.get(listenerName);
		if(p == null) {
			return false;
		}
		
		messageTimeTimer = 0;
		messageIntervalTimer = 0;
		currentMessage = p.pickDefault();
		if(currentMessage.random || currentMessage.shuffle) {
			rand = new Random();
		}
		
		currentFrame = currentMessage.random ? rand.nextInt(currentMessage.frames.size()) : 0;
		
		applyFrame(currentMessage.frames.get(currentFrame));
		if(currentMessage.interval > 0 || currentMessage.next != null) {
			this.motd.keepAlive(true);
			return true;
		}else {
			this.motd.keepAlive(false);
			return false;
		}
	}
	
	public boolean tick() {
		ageTimer++;
		if(this.motd.isClosed()) {
			return false;
		}
		if(ageTimer > EaglerMOTD.close_socket_after) {
			this.motd.close();
			return false;
		}
		messageTimeTimer++;
		if(messageTimeTimer >= currentMessage.timeout) {
			if(currentMessage.next != null) {
				if(currentMessage.next.equalsIgnoreCase("any") || currentMessage.next.equalsIgnoreCase("random")) {
					MessagePool p = EaglerMOTD.messagePools.get(listenerName);
					if(p == null) {
						this.motd.close();
						return false;
					}
					if(p.messagePool.size() > 1) {
						MessagePoolEntry m;
						do {
							m = p.pickNew();
						}while(m == currentMessage);
						currentMessage = m;
					}
				}else {
					if(!changeMessageTo(listenerName, currentMessage.next)) {
						boolean flag = false;
						for(String s : EaglerMOTD.messages.keySet()) {
							if(!s.equalsIgnoreCase(listenerName) && changeMessageTo(s, currentMessage.next)) {
								flag = true;
								break;
							}
						}
						if(!flag) {
							this.motd.close();
							return false;
						}
					}
				}
				if(currentMessage == null) {
					this.motd.close();
					return false;
				}
				messageTimeTimer = 0;
				messageIntervalTimer = 0;
				if(rand == null && (currentMessage.random || currentMessage.shuffle)) {
					rand = new Random();
				}
				currentFrame = currentMessage.random ? rand.nextInt(currentMessage.frames.size()) : 0;
				applyFrame(currentMessage.frames.get(currentFrame));
				motd.sendToUser();
				return true;
			}else {
				this.motd.close();
				return false;
			}
		}else {
			messageIntervalTimer++;
			if(currentMessage.interval > 0 && messageIntervalTimer >= currentMessage.interval) {
				messageIntervalTimer = 0;
				if(currentMessage.frames.size() > 1) {
					if(currentMessage.shuffle) {
						int i;
						do {
							i = rand.nextInt(currentMessage.frames.size());
						}while(i == currentFrame);
						currentFrame = i;
					}else {
						++currentFrame;
						if(currentFrame >= currentMessage.frames.size()) {
							currentFrame = 0;
						}
					}
					applyFrame(currentMessage.frames.get(currentFrame));
					motd.sendToUser();
				}
			}
			return true;
		}
	}
	
	private boolean changeMessageTo(String group, String s) {
		List<MessagePoolEntry> lst = EaglerMOTD.messages.get(group);
		if(lst == null) {
			return false;
		}
		for(MessagePoolEntry m : lst) {
			if(m.name.equalsIgnoreCase(s)) {
				currentMessage = m;
				return true;
			}
		}
		return false;
	}
	
	public void applyFrame(JSONObject frame) {
		boolean shouldPush = false;
		Object v = frame.opt("online");
		if(v != null) {
			if(v instanceof Number) {
				motd.setOnlinePlayers(((Number)v).intValue());
			}else {
				motd.setOnlinePlayers(BungeeCord.getInstance().getPlayers().size());
			}
			shouldPush = true;
		}
		v = frame.opt("max");
		if(v != null) {
			if(v instanceof Number) {
				motd.setMaxPlayers(((Number)v).intValue());
			}else {
				motd.setMaxPlayers(listener.getMaxPlayers());
			}
			shouldPush = true;
		}
		v = frame.opt("players");
		if(v != null) {
			if(v instanceof JSONArray) {
				List<String> players = new ArrayList();
				JSONArray vv = (JSONArray) v;
				for(int i = 0, l = vv.length(); i < l; ++i) {
					players.add(ChatColor.translateAlternateColorCodes('&', vv.getString(i)));
				}
				motd.setPlayerList(players);
			}else {
				List<String> players = new ArrayList();
				Collection<ProxiedPlayer> ppl = BungeeCord.getInstance().getPlayers();
				for(ProxiedPlayer pp : ppl) {
					players.add(pp.getDisplayName());
					if(players.size() >= 9) {
						players.add("" + ChatColor.GRAY + ChatColor.ITALIC + "(" + (ppl.size() - players.size()) + " more)");
						break;
					}
				}
				motd.setPlayerList(players);
			}
			shouldPush = true;
		}
		String line = frame.optString("text0", frame.optString("text", null));
		if(line != null) {
			int ix = line.indexOf('\n');
			if(ix != -1) {
				motd.setLine1(ChatColor.translateAlternateColorCodes('&', line.substring(0, ix)));
				motd.setLine2(ChatColor.translateAlternateColorCodes('&', line.substring(ix + 1)));
			}else {
				motd.setLine1(ChatColor.translateAlternateColorCodes('&', line));
			}
			line = frame.optString("text1", null);
			if(line != null) {
				motd.setLine2(ChatColor.translateAlternateColorCodes('&', line));
			}
			shouldPush = true;
		}
		boolean shouldRenderIcon = false;
		String icon = frame.optString("icon", null);
		if(icon != null) {
			shouldRenderIcon = true;
			if(icon.equalsIgnoreCase("none") || icon.equalsIgnoreCase("default") || icon.equalsIgnoreCase("null") || icon.equalsIgnoreCase("color")) {
				bitmap = null;
			}else {
				bitmap = BitmapFile.getCachedIcon(icon);
			}
			spriteX = spriteY = 0;
			color = new float[] { 0.0f, 0.0f, 0.0f, 0.0f };
			tint = new float[] { 1.0f, 1.0f, 1.0f, 1.0f };
		}
		int sprtX = frame.optInt("icon_spriteX", -1);
		if(sprtX >= 0 && sprtX != spriteX) {
			shouldRenderIcon = true;
			spriteX = sprtX;
		}
		int sprtY = frame.optInt("icon_spriteY", -1);
		if(sprtY >= 0 && sprtY != spriteY) {
			shouldRenderIcon = true;
			spriteY = sprtY;
		}
		JSONArray colorF = frame.optJSONArray("icon_color");
		if(colorF != null && colorF.length() > 0) {
			shouldRenderIcon = true;
			color[0] = colorF.getFloat(0);
			color[1] = colorF.length() > 1 ? colorF.getFloat(1) : color[1];
			color[2] = colorF.length() > 2 ? colorF.getFloat(2) : color[2];
			color[3] = colorF.length() > 3 ? colorF.getFloat(3) : 1.0f;
		}
		colorF = frame.optJSONArray("icon_tint");
		if(colorF != null && colorF.length() > 0) {
			shouldRenderIcon = true;
			tint[0] = colorF.getFloat(0);
			tint[1] = colorF.length() > 1 ? colorF.getFloat(1) : tint[1];
			tint[2] = colorF.length() > 2 ? colorF.getFloat(2) : tint[2];
			tint[3] = colorF.length() > 3 ? colorF.getFloat(3) : 1.0f;
		}
		if(shouldRenderIcon) {
			int[] newIcon = null;
			if(bitmap != null) {
				newIcon = bitmap.getSprite(sprtX, sprtY);
			}
			if(newIcon == null) {
				newIcon = new int[64*64];
			}
			newIcon = BitmapFile.applyTint(newIcon, tint[0], tint[1], tint[2], tint[3]);
			if(color[3] > 0.0f) {
				newIcon = BitmapFile.applyColor(newIcon, color[0], color[1], color[2], color[3]);
			}
			motd.setBitmap(newIcon);
			shouldPush = true;
		}
		if(shouldPush) {
			motd.sendToUser();
		}
	}
	
	public void close() {
		motd.close();
	}

}
