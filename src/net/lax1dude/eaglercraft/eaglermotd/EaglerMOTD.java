package net.lax1dude.eaglercraft.eaglermotd;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONArray;
import org.json.JSONObject;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.config.ListenerInfo;
import net.md_5.bungee.api.event.WebsocketMOTDEvent;
import net.md_5.bungee.api.event.WebsocketQueryEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.command.ConsoleCommandSender;
import net.md_5.bungee.eaglercraft.BanList;
import net.md_5.bungee.event.EventHandler;

public class EaglerMOTD extends Plugin implements Listener {

	public static final Map<String,List<MessagePoolEntry>> messages = new HashMap();
	public static final Map<String,MessagePool> messagePools = new HashMap();
	public static final Map<String,JSONObject> framesCache = new HashMap();
	public static final Map<String,QueryType> queryTypes = new HashMap();
	public static EaglerMOTD instance = null;
	public static int close_socket_after = 1200;
	public static int max_sockets_per_ip = 10;
	public static int max_total_sockets = 256;
	public static boolean allow_banned_ips = false;
	
	public final Timer tickTimer = new Timer("MOTD Tick Timer");
	public final List<MOTDConnection> motdConnections = new LinkedList();
	
	public EaglerMOTD() {
		instance = this;
	}
	
	public void loadConfiguration(CommandSender cs) throws Exception {
		messages.clear();
		messagePools.clear();
		framesCache.clear();
		queryTypes.clear();
		
		BitmapFile.bitmapCache.clear();
		QueryCache.flush();
		
		synchronized(motdConnections) {
			if(motdConnections.size() > 0) {
				for(MOTDConnection con : motdConnections) {
					con.close();
				}
				motdConnections.clear();
			}
		}
		
		getDataFolder().mkdirs();
		
		byte[] damn = new byte[4096];
		int i;
		
		File msgs = new File(getDataFolder(), "messages.json");
		
		if(!msgs.exists()) {
			OutputStream msgsNew = new FileOutputStream(msgs);
			InputStream msgsDefault = EaglerMOTD.class.getResourceAsStream("/default_messages.json");
			while((i = msgsDefault.read(damn)) != -1) {
				msgsNew.write(damn, 0, i);
			}
			msgsNew.close();
			msgsDefault.close();
			File f2 = new File(getDataFolder(), "frames.json");
			if(!f2.exists()) {
				msgsNew = new FileOutputStream(f2);
				msgsDefault = EaglerMOTD.class.getResourceAsStream("/default_frames.json");
				while((i = msgsDefault.read(damn)) != -1) {
					msgsNew.write(damn, 0, i);
				}
				msgsNew.close();
				msgsDefault.close();
			}
			f2 = new File(getDataFolder(), "queries.json");
			if(!f2.exists()) {
				msgsNew = new FileOutputStream(f2);
				msgsDefault = EaglerMOTD.class.getResourceAsStream("/default_queries.json");
				while((i = msgsDefault.read(damn)) != -1) {
					msgsNew.write(damn, 0, i);
				}
				msgsNew.close();
				msgsDefault.close();
			}
			f2 = new File("server-animation.png");
			if(!f2.exists()) {
				msgsNew = new FileOutputStream(f2);
				msgsDefault = EaglerMOTD.class.getResourceAsStream("/server-icons-test.png");
				while((i = msgsDefault.read(damn)) != -1) {
					msgsNew.write(damn, 0, i);
				}
				msgsNew.close();
				msgsDefault.close();
			}
		}
		if(!msgs.exists()) {
			throw new NullPointerException("messages.json is missing and could not be created");
		}
		
		InputStream is = new FileInputStream(msgs);
		ByteArrayOutputStream bao = new ByteArrayOutputStream(is.available());
		while((i = is.read(damn)) != -1) {
			bao.write(damn, 0, i);
		}
		is.close();
		
		JSONObject msgsObj = new JSONObject(new String(bao.toByteArray(), StandardCharsets.UTF_8));
		framesCache.put("messages", msgsObj);
		close_socket_after = msgsObj.optInt("close_socket_after", 1200);
		max_sockets_per_ip = msgsObj.optInt("max_sockets_per_ip", 10);
		max_total_sockets = msgsObj.optInt("max_total_sockets", 256);
		allow_banned_ips = msgsObj.optBoolean("allow_banned_ips", false);
		msgsObj = msgsObj.getJSONObject("messages");
		
		for(String ss : msgsObj.keySet()) {
			try {
				List<MessagePoolEntry> poolEntries = new LinkedList();
				JSONArray arr = msgsObj.getJSONArray(ss);
				for(int j = 0, l = arr.length(); j < l; ++j) {
					JSONObject entry = arr.getJSONObject(j);
					List<JSONObject> frames = new LinkedList();
					JSONArray framesJSON = entry.getJSONArray("frames");
					for(int k = 0, l2 = framesJSON.length(); k < l2; ++k) {
						JSONObject frame = resolveFrame(framesJSON.getString(k), cs);
						if(frame != null) {
							frames.add(frame);
						}
					}
					if(frames.size() > 0) {
						poolEntries.add(new MessagePoolEntry(entry.optInt("interval", 0), entry.optInt("timeout", 500), 
								entry.optBoolean("random", false), entry.optBoolean("shuffle", false), entry.optFloat("weight", 1.0f),
								entry.optString("next", null), frames, entry.optString("name", null)));
					}else {
						cs.sendMessage(ChatColor.RED + "[EaglerMOTD] Message '" + ss + "' has no frames!");
					}
				}
				if(poolEntries.size() > 0) {
					List<MessagePoolEntry> existingList = messages.get(ss);
					if(existingList == null) {
						existingList = poolEntries;
						messages.put(ss, existingList);
					}else {
						existingList.addAll(poolEntries);
					}
				}
			}catch(Throwable t) {
				cs.sendMessage(ChatColor.RED + "[EaglerMOTD] Could not parse messages for '" + ss + "' " + t.toString());
			}
		}
		
		Collection<ListenerInfo> listeners = getProxy().getConfigurationAdapter().getListeners();
		
		String flag = null;
		for(String s : messages.keySet()) {
			if(!s.equals("all")) {
				boolean flag2 = false;
				for(ListenerInfo l : listeners) {
					if(s.equals(makeListenerString(l.getHost()))) {
						flag2 = true;
					}
				}
				if(!flag2) {
					flag = s;
					break;
				}
			}
		}
		
		if(flag != null) {
			cs.sendMessage(ChatColor.RED + "[EaglerMOTD] Listener '" + flag + "' does not exist!");
			String hostsString = "";
			for(ListenerInfo l : listeners) {
				if(hostsString.length() > 0) {
					hostsString += " ";
				}
				hostsString += makeListenerString(l.getHost());
			}
			cs.sendMessage(ChatColor.RED + "[EaglerMOTD] Listeners configured: " + ChatColor.YELLOW + hostsString);
		}
		
		for(ListenerInfo l : listeners) {
			String name = makeListenerString(l.getHost());
			MessagePool m = new MessagePool(name);
			List<MessagePoolEntry> e = messages.get("all");
			if(e != null) {
				m.messagePool.addAll(e);
			}
			e = messages.get(name);
			if(e != null) {
				m.messagePool.addAll(e);
			}
			if(m.messagePool.size() > 0) {
				cs.sendMessage(ChatColor.GREEN + "[EaglerMOTD] Loaded " + m.messagePool.size() + " messages for " + name);
				messagePools.put(name, m);
			}
		}
		
		msgs = new File(getDataFolder(), "queries.json");
		if(msgs.exists()) {
			try {
				is = new FileInputStream(msgs);
				bao = new ByteArrayOutputStream(is.available());
				while((i = is.read(damn)) != -1) {
					bao.write(damn, 0, i);
				}
				is.close();
				JSONObject queriesObject = new JSONObject(new String(bao.toByteArray(), StandardCharsets.UTF_8));
				JSONObject queriesQueriesObject = queriesObject.getJSONObject("queries");
				for(String s : queriesQueriesObject.keySet()) {
					queryTypes.put(s.toLowerCase(), new QueryType(s, queriesQueriesObject.getJSONObject(s)));
				}
				if(queryTypes.size() > 0) {
					cs.sendMessage(ChatColor.GREEN + "[EaglerMOTD] Loaded " + queryTypes.size() + " query types");
				}
			}catch(Throwable t) {
				cs.sendMessage(ChatColor.RED + "[EaglerMOTD] Queries were not loaded: " + t.toString());
			}
		}
		
	}
	
	public static String makeListenerString(InetSocketAddress addr) {
		InetAddress addrHost = addr.getAddress();
		if(addrHost instanceof Inet6Address) {
			return "[" + addrHost.getHostAddress() + "]:" + addr.getPort();
		}else {
			return addrHost.getHostAddress() + ":" + addr.getPort();
		}
	}
	
	public JSONObject resolveFrame(String s, CommandSender cs) {
		int i = s.indexOf('.');
		if(i == -1) {
			cs.sendMessage(ChatColor.RED + "[EaglerMOTD] Frame '" + s + "' cannot be found! (it does not specify a filename)");
			return null;
		}
		String f = s.substring(0, i);
		JSONObject fc = framesCache.get(f);
		if(fc == null) {
			File ff = new File(getDataFolder(), f + ".json");
			if(!ff.exists()) {
				cs.sendMessage(ChatColor.RED + "[EaglerMOTD] File '" + f + ".json' cannot be found!");
				return null;
			}
			try {
				byte[] damn = new byte[4096];
				InputStream is = new FileInputStream(ff);
				ByteArrayOutputStream bao = new ByteArrayOutputStream(is.available());
				int j;
				while((j = is.read(damn)) != -1) {
					bao.write(damn, 0, j);
				}
				is.close();
				fc = new JSONObject(new String(bao.toByteArray(), StandardCharsets.UTF_8));
				framesCache.put(f, fc);
			}catch(Throwable t) {
				cs.sendMessage(ChatColor.RED + "[EaglerMOTD] File '" + f + ".json' could not be loaded: " + t.toString());
				return null;
			}
		}
		f = s.substring(i + 1).trim();
		if(fc.has(f)) {
			return fc.getJSONObject(f);
		}else {
			cs.sendMessage(ChatColor.RED + "[EaglerMOTD] Frame '" + s + "' cannot be found!");
			return null;
		}
	}
	
	public void showRunCmd(CommandSender cs) {
		cs.sendMessage(ChatColor.YELLOW + "[EaglerMOTD] Use /motd-reload to reload your MOTD config files");
	}
	
	public void onLoad() {
		try {
			getProxy().getPluginManager().registerCommand(this, new CommandMOTDReload(this));
			loadConfiguration(ConsoleCommandSender.getInstance());
		} catch (Exception e) {
			System.err.println("[EaglerMOTD] Could not load!");
			e.printStackTrace();
		}
	}

	public void onEnable() {
		getProxy().getPluginManager().registerListener(this, this);
		tickTimer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				synchronized(motdConnections) {
					Iterator<MOTDConnection> itr = motdConnections.iterator();
					while(itr.hasNext()) {
						MOTDConnection c = itr.next();
						try {
							if(!c.tick()) {
								itr.remove();
							}
						}catch(Throwable t) {
							System.err.println("Error ticking MOTD '" + (c.currentMessage == null ? "null" : c.currentMessage.name) + "' on listener " + c.listenerName);
							t.printStackTrace();
							c.close();
							itr.remove();
						}
					}
				}
			}
			
		}, 0, 50l);
	}

	public void onDisable() {
		tickTimer.cancel();
	}
	
	@EventHandler
	public void onMOTD(WebsocketMOTDEvent evt) {
		if(!evt.getAccept().equalsIgnoreCase("motd") && !evt.getAccept().equalsIgnoreCase("motd.noicon")) {
			return;
		}
		MOTDConnection con = new MOTDConnection(evt.getListener(), evt.getMOTD());
		if(con.execute()) {
			if(max_total_sockets > 0) {
				synchronized(motdConnections) {
					while(motdConnections.size() >= max_total_sockets) {
						MOTDConnection c = motdConnections.remove(motdConnections.size() - 1);
						c.close();
					}
				}
			}
			InetAddress addr = con.motd.getRemoteAddress();
			boolean flag = false;
			for(BanList.IPBan b : BanList.blockedBans) {
				if(b.checkBan(addr)) {
					flag = true;
					break;
				}
			}
			if(flag) {
				synchronized(motdConnections) {
					motdConnections.add(0, con);
				}
			}else {
				if(allow_banned_ips || !BanList.checkIpBanned(addr).isBanned()) {
					synchronized(motdConnections) {
						if(max_sockets_per_ip > 0) {
							int i = 0;
							int c = 0;
							while(i < motdConnections.size()) {
								if(motdConnections.get(i).motd.getRemoteAddress().equals(addr)) {
									++c;
									if(c >= max_sockets_per_ip) {
										motdConnections.remove(i).close();
										--i;
									}
								}
								++i;
							}
						}
						motdConnections.add(0, con);
					}
				}else {
					con.motd.keepAlive(false);
				}
			}
		}
	}
	
	@EventHandler
	public void onQuery(WebsocketQueryEvent evt) {
		if(evt.getQuery().isClosed() || evt.getAccept().equalsIgnoreCase("MOTD")) {
			return;
		}
		QueryType t = queryTypes.get(evt.getAccept().toLowerCase());
		if(t != null) {
			t.doQuery(evt.getQuery());
		}
	}
	
}
