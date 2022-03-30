package planespotter.model;

import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import com.google.gson.*;

import planespotter.dataclasses.*;

public class Deserializer {
	
	public Deserializer() {
		
	}
	
	// Handle Response Data, this basically turns the output to CSV
	// TODO REPLACE WITH PROPER JSON HANDLING
	// This is an absolute mess...
	public List<String> stringMagic(HttpResponse<String> resp){

		String respdata = resp.body();
		String[] array = respdata.split(",", 3);

		List<String> datalist = new ArrayList<String>();
		for (String a : array) datalist.add(a);

		// Remove fr24 internal version info and such
		datalist.remove(1);
		datalist.remove(0);

		// RegEx Magic to remove fr24 internal referencing (will be replaced by my own)
		String data;
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
	public List<Frame> deserializeJSON(List<String> data) {
		List<Frame> frames = new ArrayList<Frame>();
		Gson gson = new Gson();
		for(String s : data) {
			Frame frame = gson.fromJson(s, Frame.class);
			frames.add(frame);
		}
		return frames;
	}
	
	//Convert String to Frame-Objects
	public List<Frame> deserialize(List<String> data) {
		List<Frame> frames = new ArrayList<Frame>();
		for(String row : data) {
			String[] r = row.split(",");
			
			String field;
			field = r[6];
			field = field.replaceAll("\"\"", "40401");
			r[6] = field;
			
			Frame frame = new Frame(r[0], Double.parseDouble(r[1]), Double.parseDouble(r[2]), Integer.parseInt(r[3]), Integer.parseInt(r[4]), Integer.parseInt(r[5]), Integer.parseInt(r[6]),
					r[7], r[8], r[9], Integer.parseInt(r[10]), r[11], r[12], r[13], r[14], r[15], r[16], r[17], r[18]);
			frames.add(frame);
		}
		return frames;
	}
}
