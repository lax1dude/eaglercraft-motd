package net.lax1dude.eaglercraft.eaglermotd;

import org.json.JSONObject;

import net.md_5.bungee.api.QueryConnection;

public class QueryType {
	
	public final String name;
	public final String type;

	public final String dataString;
	public final String dataJSONFile;
	public final JSONObject dataJSONObject;
	public final String dataTextFile;
	public final String dataBinaryFile;
	
	public QueryType(String name, JSONObject tag) {
		this.name = name;
		this.dataJSONObject = tag.optJSONObject("json", null);
		if(this.dataJSONObject == null) {
			this.dataJSONFile = tag.optString("json", null);
			if(this.dataJSONFile == null) {
				this.dataTextFile = tag.optString("txt", null);
				if(this.dataTextFile == null) {
					this.dataString = tag.optString("string", null);
				}else {
					this.dataString = null;
				}
			}else {
				this.dataTextFile = null;
				this.dataString = null;
			}
		}else {
			this.dataJSONFile = null;
			this.dataTextFile = null;
			this.dataString = null;
		}
		this.dataBinaryFile = tag.optString("file", null);
		String t = tag.optString("type", null);
		if(t == null) {
			if(this.dataJSONObject != null || this.dataJSONFile != null) {
				t = "json";
			}else if(this.dataString != null || this.dataTextFile != null) {
				t = "text";
			}else {
				t = "binary";
			}
		}
		this.type = t;
	}

	public void doQuery(QueryConnection query) {
		byte[] bin = null;
		if(dataBinaryFile != null) {
			bin = QueryCache.getBinaryFile(dataBinaryFile);
			if(bin == null) {
				query.setReturnType("error");
				query.writeResponse("Error: could not load binary file '" + dataBinaryFile + "' for query '" + type + "'");
				return;
			}
		}
		boolean flag = false;
		if(dataJSONObject != null) {
			query.setReturnType(type);
			query.writeResponse(dataJSONObject);
			flag = true;
		}else if(dataJSONFile != null) {
			JSONObject obj = QueryCache.getJSONFile(dataJSONFile);
			if(obj == null) {
				query.setReturnType("error");
				query.writeResponse("Error: could not load or parse JSON file '" + dataJSONFile + "' for query '" + type + "'");
				return;
			}else {
				query.setReturnType(type);
				query.writeResponse(obj);
				flag = true;
			}
		}else if(dataTextFile != null) {
			String txt = QueryCache.getStringFile(dataTextFile);
			if(txt == null) {
				query.setReturnType("error");
				query.writeResponse("Error: could not load text file '" + dataJSONFile + "' for query '" + type + "'");
				return;
			}else {
				query.setReturnType(type);
				query.writeResponse(txt);
				flag = true;
			}
		}else if(dataString != null) {
			query.setReturnType(type);
			query.writeResponse(dataString);
			flag = true;
		}
		if(!flag) {
			query.setReturnType(type);
			if(bin != null) {
				query.writeResponse((new JSONObject()).put("binary", true).put("file", dataBinaryFile).put("size", bin.length));
			}else {
				query.writeResponse("<No Content>");
			}
		}
		if(bin != null) {
			query.writeResponseBinary(bin);
		}
		query.close();
	}
	
}
