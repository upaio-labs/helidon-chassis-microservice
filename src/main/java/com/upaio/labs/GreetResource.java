
package com.upaio.labs;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;

/**
 * A simple JAX-RS resource to greet you. Examples:
 *
 * Get default greeting message:
 * curl -X GET http://localhost:8080/greet
 *
 * Get greeting message for Joe:
 * curl -X GET http://localhost:8080/greet/Joe
 *
 * Change greeting
 * curl -X PUT -H "Content-Type: application/json" -d '{"greeting" : "Howdy"}' http://localhost:8080/greet/greeting
 *
 * The message is returned as a JSON object.
 */
@Path("/greet")
@RequestScoped
public class GreetResource {

    private static final Logger LOGGER = Logger.getLogger(GreetResource.class.getName());

    /**
     * The greeting message provider.
     */
    private final GreetingProvider greetingProvider;

    /**
     * Using constructor injection to get a configuration property.
     * By default this gets the value from META-INF/microprofile-config
     *
     * @param greetingConfig the configured greeting message
     */
    @Inject
    public GreetResource(GreetingProvider greetingConfig) {
        this.greetingProvider = greetingConfig;
    }

    /**
     * Return a worldly greeting message.
     *
     * @return {@link Message}
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Message getDefaultMessage() {
        // Simular un retraso de 15 segundos
        try {
            TimeUnit.SECONDS.sleep(15);
        } catch (InterruptedException e) {
            // Manejar interrupciones si es necesario
            e.printStackTrace();
        }        

        return createResponse("World");
    }

    /**
     * Return a greeting message using the name that was provided.
     *
     * @param name the name to greet
     * @return {@link Message}
     */
    @Path("/{name}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Message getMessage(@PathParam("name") String name) {
        return createResponse(name);
    }

    /**
     * Set the greeting to use in future messages.
     *
     * @param message Message containing the new greeting
     * @return {@link Response}
     */
    @Path("/greeting")
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @RequestBody(name = "greeting",
            required = true,
            content = @Content(mediaType = "application/json",
                    schema = @Schema(type = SchemaType.OBJECT, requiredProperties = { "greeting" })))
    @APIResponses({
            @APIResponse(name = "normal", responseCode = "204", description = "Greeting updated"),
            @APIResponse(name = "missing 'greeting'", responseCode = "400",
                    description = "JSON did not contain setting for 'greeting'")})
    public Response updateGreeting(Message message) {

        if (message.getGreeting() == null || message.getGreeting().isEmpty()) {
            Message error = new Message();
            error.setMessage("No greeting provided");
            return Response.status(Response.Status.BAD_REQUEST).entity(error).build();
        }

        greetingProvider.setMessage(message.getGreeting());
        return Response.status(Response.Status.NO_CONTENT).build();
    }

    private Message createResponse(String who) {
        String msg = String.format("%s %s!", greetingProvider.getMessage(), who);

        return new Message(msg);
    }

    @Path("/client")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @APIResponse(name = "OK", responseCode = "200", description = "OK")
    @APIResponse(name = "Server error", responseCode = "500", description = "Server error") 
    public Response getMessageForClientRest() {
        LOGGER.log(Level.INFO, "Ejecutando el método getMessageForClientRest");
        
        // Crear una configuración del cliente
        ClientConfig clientConfig = new ClientConfig();

        // Configurar el timeout de conexión y lectura en milisegundos
        clientConfig.property(ClientProperties.CONNECT_TIMEOUT, 5000); // 5 segundos
        clientConfig.property(ClientProperties.READ_TIMEOUT, 5000);    // 5 segundos

        try (Client client = ClientBuilder.newClient(clientConfig)) {
            return client
                    .target("http://localhost:8080/greet")
                    .request("application/json")
                    .get();
        } catch (ProcessingException e) {
            // Manejar la excepción de timeout u otros problemas de conexión
            LOGGER.log(Level.SEVERE, "Error al realizar la solicitud: " + e.getMessage(), e);
            //TODO: Se puede manejar la respuesta del error mediante una clase especifica. 
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error al realizar la solicitud: " + e.getMessage())
                    .build();
        }
    }
}
