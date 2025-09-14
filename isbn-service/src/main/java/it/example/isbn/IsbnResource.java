package it.example.isbn;

import it.example.common.dto.BookDTO;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/isbn")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class IsbnResource {
    @Inject IsbnValidator validator;

    @GET @Path("/validate/{isbn}")
    public Response validate(@PathParam("isbn") String isbn) {
        return Response.ok(new ValidationResult(isbn, validator.isValid(isbn))).build();
    }

    @POST @Transactional @Path("/books")
    public BookDTO create(BookDTO dto) {
        if (dto == null) throw new BadRequestException("Body mancante");
        if (dto.isbn() == null || dto.isbn().isBlank()) throw new BadRequestException("ISBN mancante");
        if (dto.title() == null || dto.title().isBlank()) throw new BadRequestException("Titolo mancante");
        if (dto.author() == null || dto.author().isBlank()) throw new BadRequestException("Autore mancante");
        if (!validator.isValid(dto.isbn())) 
            throw new BadRequestException("ISBN non valido");
        Book b=new Book(); 
        b.isbn=dto.isbn(); 
        b.title=dto.title(); 
        b.author=dto.author(); 
        b.persist();
        /*
        return Response.status(Response.Status.CREATED)
            .entity(new BookDTO(b.isbn, b.title, b.author))
            .build();
        */
        return new BookDTO(b.isbn,b.title,b.author);
    }

    @GET @Path("/books/{isbn}")
    public BookDTO get(@PathParam("isbn") String isbn) {
        Book b = Book.find("isbn", isbn).firstResult();
        if (b==null) 
            throw new NotFoundException();
        return new BookDTO(b.isbn,b.title,b.author);
    }

    public record ValidationResult(String isbn, boolean valid) {}
}