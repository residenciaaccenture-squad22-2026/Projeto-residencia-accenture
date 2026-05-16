package org.acme.exception;

import org.acme.dto.ErroResponse;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class ApiExceptionMapper implements ExceptionMapper<Throwable> {

    @Context
    UriInfo uriInfo;

    @Override
    public Response toResponse(Throwable exception) {
        int status = Response.Status.INTERNAL_SERVER_ERROR.getStatusCode();
        String erro = Response.Status.INTERNAL_SERVER_ERROR.getReasonPhrase();
        String mensagem = "Erro interno no servidor";

        if (exception instanceof WebApplicationException webApplicationException) {
            status = webApplicationException.getResponse().getStatus();
            Response.Status responseStatus = Response.Status.fromStatusCode(status);
            erro = responseStatus != null ? responseStatus.getReasonPhrase() : "Erro HTTP";

            if (exception.getMessage() != null && !exception.getMessage().isBlank()) {
                mensagem = exception.getMessage();
            } else {
                mensagem = erro;
            }
        }

        ErroResponse response = new ErroResponse(status, erro, mensagem, uriInfo.getPath());

        return Response
                .status(status)
                .type(MediaType.APPLICATION_JSON)
                .entity(response)
                .build();
    }
}
