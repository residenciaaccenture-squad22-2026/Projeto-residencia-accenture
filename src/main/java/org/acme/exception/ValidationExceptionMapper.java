package org.acme.exception;

import org.acme.dto.ErroResponse;

import io.quarkus.hibernate.validator.runtime.jaxrs.ResteasyReactiveViolationException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class ValidationExceptionMapper implements ExceptionMapper<ResteasyReactiveViolationException> {

    @Context
    UriInfo uriInfo;

    @Override
    public Response toResponse(ResteasyReactiveViolationException exception) {
        String mensagem = exception.getConstraintViolations().stream()
                .map(violation -> violation.getMessage())
                .sorted()
                .findFirst()
                .orElse("Dados invalidos");

        ErroResponse response = new ErroResponse(
                Response.Status.BAD_REQUEST.getStatusCode(),
                Response.Status.BAD_REQUEST.getReasonPhrase(),
                mensagem,
                uriInfo.getPath());

        return Response
                .status(Response.Status.BAD_REQUEST)
                .type(MediaType.APPLICATION_JSON)
                .entity(response)
                .build();
    }
}
