package planespotter.unused;

import com.google.gson.Gson;
import planespotter.dataclasses.Fr24Frame;
import planespotter.model.nio.AbstractDeserializer;

import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Deprecated(since = "new Deserializer with correct data")
/* @unused */												// could be turned into ADSBDeserializer
public class Deserializer implements AbstractDeserializer<HttpResponse<String>> {
	
	public Deserializer() {
	}
	
	
	//Now this is an awful way to handle things... dont do this:
	
	public String stripString (String in) {
		if (in == null) return "";
		String temp = in.replaceAll("\"", "");
		String out = temp.replace("\n", "");
		return out.replaceAll("\"", "");
	}
	
	// Handle Response Data, this basically turns the output to CSV
	// TODO REPLACE WITH PROPER JSON HANDLING
	// This is an absolute mess...
	public List<String> stringMagic(HttpResponse<String> resp){

		String respdata = resp.body();
		String[] array = respdata.split(",", 3);

		List<String> datalist = new ArrayList<>(Arrays.asList(array));

		// Remove fr24 internal version info and such
		datalist.remove(1);
		datalist.remove(0);

		// RegEx Magic to remove fr24 internal referencing (will be replaced by my own)
		String data;
		data = datalist.get(0);
		data = data.replaceAll("\\[", "");
		data = data.replaceAll("]\n,\"........\"", "\n"); // TODO hier kann man data.replaceAll(...).replaceAll(...).replaceAll(...) machen (untereinander)
		data = data.replaceAll("]", "");
		array = data.split(":");
		datalist.addAll(Arrays.asList(array));

		// remove last unneeded entrys from list
		datalist.remove(1);
		datalist.remove(0);

		// return the cleaned list
		return datalist;
	}

	
	//Convert Json to Frame-Objects
	public List<Fr24Frame> deserializeJSON(List<String> data) {
		List<Fr24Frame> fr24Frames = new ArrayList<>();
		Gson gson = new Gson();
		for(String s : data) {
			Fr24Frame fr24Frame = gson.fromJson(s, Fr24Frame.class);
			fr24Frames.add(fr24Frame);
		}
		return fr24Frames;
	}
	
	//Convert String to Frame-Objects
	@Override
	public List<Fr24Frame> deserialize(HttpResponse<String> resp) {
		List<Fr24Frame> fr24Frames = new ArrayList<>();
		for(String row : stringMagic(resp)) {
			String[] r = row.split(",");
			
			String field;
			field = r[6];
			field = field.replaceAll("\"\"", "40401");
			r[6] = field;
			
			Fr24Frame fr24Frame = new Fr24Frame(r[0], Double.parseDouble(r[1]), Double.parseDouble(r[2]), Integer.parseInt(r[3]), Integer.parseInt(r[4]), Integer.parseInt(r[5]), Integer.parseInt(r[6]),
					r[7], r[8], r[9], Integer.parseInt(r[10]), r[11], r[12], r[13], r[14], r[15], r[16], r[17], r[18]);
			fr24Frames.add(fr24Frame);
		}
		return fr24Frames;
	}
}
