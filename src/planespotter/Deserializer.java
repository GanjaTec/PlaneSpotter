package planespotter;

import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import com.google.gson.*;

public class Deserializer {
	
	public Deserializer() {
		
	}
	
	// Handle Response Data, this basically turns the output to CSV
	// TODO REPLACE WITH PROPER JSON HANDLING
	// This is an absolute mess...
	
	//Gson gson = new Gson();
	//Fr24Data FRdata = gson.fromJson(response.body(), Fr24Data.class);
	
	public List<String> stringMagic(HttpResponse<String> resp){

		String data = resp.body();
		String[] array = data.split(",", 3);

		List<String> datalist = new ArrayList<String>();
		for (String a : array) datalist.add(a);

		// Remove fr24 internal version info and such
		datalist.remove(1);
		datalist.remove(0);

		// RegEx Magic to remove fr24 internal referencing (will be replaced by my own)
		data = datalist.get(0);
		data = data.replaceAll("\\[", "");
		data = data.replaceAll("]\n,\"........\"", "\n");
		data = data.replaceAll("]", "");
		array = data.split(":");
		for (String a : array)
			datalist.add(a);

		// remove last unneeded entrys from list
		datalist.remove(1);
		datalist.remove(0);

		// return the cleaned list
		return datalist;
	}

	
	//Convert Json to Frame-Objects
	public List<Frame> deserialize(List<String> data) {
		List<Frame> frames = new ArrayList<Frame>();
		Gson gson = new Gson();
		for(String s : data) {
			Frame frame = gson.fromJson(s, Frame.class);
			frames.add(frame);
		}
		return frames;
	}
}
