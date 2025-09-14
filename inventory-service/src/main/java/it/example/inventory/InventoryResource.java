package it.example.inventory;

import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@Path("/inventory")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class InventoryResource {
    @Inject @RestClient IsbnClient isbn;

    @GET @Path("/{isbn}")
    public InventoryItem get(@PathParam("isbn") String isbnCode) {
        InventoryItem item = InventoryItem.find("isbn", isbnCode).firstResult();
        if (item == null) throw new NotFoundException();
        return item;
    }

    public record AdjustRequest(int delta, String location) {}

    @POST @Transactional @Path("/{isbn}/adjust")
    public InventoryItem adjust(@PathParam("isbn") String isbnCode, AdjustRequest req) {
        if (!isbn.validate(isbnCode).valid()) throw new BadRequestException("ISBN non valido");
        InventoryItem item = InventoryItem.find("isbn", isbnCode).firstResult();
        if (req == null) throw new BadRequestException("Body mancante");
        if (req.delta() == 0) throw new BadRequestException("Delta deve essere diverso da 0");
        if (item == null && (req.location() == null || req.location().isBlank()))
            throw new BadRequestException("Location obbligatoria alla creazione");
        if (item == null) { 
            item = new InventoryItem(); 
            item.isbn = isbnCode; 
            item.location = req.location(); 
            item.quantity = 0; 
        } else if (req.location() != null && !req.location().isBlank()) {
            item.location = req.location();
        }
        item.quantity += req.delta();
        if (item.quantity < 0) 
            throw new BadRequestException("Quantità negativa");
        item.persist();
        return item;
    }
}