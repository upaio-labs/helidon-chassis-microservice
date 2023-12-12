package com.upaio.labs.openapi;

import org.eclipse.microprofile.openapi.OASFactory;
import org.eclipse.microprofile.openapi.OASModelReader;
import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.eclipse.microprofile.openapi.models.info.Contact;
import org.eclipse.microprofile.openapi.models.info.Info;

public class HelidonOpenApiReader implements OASModelReader {
    @Override
    public OpenAPI buildModel() {
        return OASFactory.createOpenAPI()
                .info(info());
    }

    private Info info() {
        return OASFactory
                .createInfo()
                .title("Helidon quickstart-mp")
                .description("Helidon quickstart-mp API")
                .version("1.1-SNAPSHOT")
                .contact(contact());
    }

    private Contact contact() {
        return OASFactory
                .createContact()
                .name("Jesus Aguirre")
                .url("https://jeaguirre-web.vercel.app/");
    }
}
