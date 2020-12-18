package icecream.service.resource;
  
import icecream.service.resource.model.Flavor;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.UUID;

@Path("/flavors")
public class Flavors {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Flavor[] listFlavors() {
	
	String[] flavors= { 
		"Chocolate", 
		"Vanilla", 
		"MintChocolate",
		"BubbleGum", 
		"Pistachio", 
		"RockyRoad",
		"Raspberry",
		"Mango",
		"CherryJubilee",
		"Lime"
       	};

        Flavor[] outputFlavors = new Flavor[flavors.length];

        for (int i = 0; i < flavors.length; i++) {
            Flavor f = new Flavor();
            f.setFlavor(flavors[i]);
            outputFlavors[i] = f;
        }


        return outputFlavors;
    }
}
