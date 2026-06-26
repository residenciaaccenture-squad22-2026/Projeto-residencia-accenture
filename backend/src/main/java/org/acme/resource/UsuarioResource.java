package org.acme.resource;

import java.net.URI;
import java.util.List;

import org.acme.dto.ApiMapper;
import org.acme.dto.UsuarioRequest;
import org.acme.dto.UsuarioResponse;
import org.acme.model.Usuario;
import org.acme.service.UsuarioService;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/usuarios")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UsuarioResource {

    @Inject
    UsuarioService usuarioService;

    @GET
    public List<UsuarioResponse> listarUsuarios() {
        return usuarioService.listarUsuarios().stream()
                .map(UsuarioResponse::from)
                .toList();
    }

    @POST
    public Response cadastrarUsuario(@Valid UsuarioRequest request) {
        Usuario usuarioCadastrado = usuarioService.cadastrarUsuario(ApiMapper.toUsuario(request));

        return Response
                .created(URI.create("/usuarios/" + usuarioCadastrado.getId()))
                .entity(UsuarioResponse.from(usuarioCadastrado))
                .build();
    }
}