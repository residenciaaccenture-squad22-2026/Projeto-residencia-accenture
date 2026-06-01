package org.acme.controller;

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
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/usuarios")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UsuarioController {

    @Inject
    UsuarioService usuarioService;

    @GET
    public List<UsuarioResponse> listarUsuarios() {
        return usuarioService.listarUsuarios().stream()
                .map(UsuarioResponse::from)
                .toList();
    }

    @GET
    @Path("/{id}")
    public UsuarioResponse buscarUsuario(@PathParam("id") Long id) {
        Usuario usuario = usuarioService.buscarPorId(id);

        if (usuario == null) {
            throw new NotFoundException("Usuario nao encontrado");
        }

        return UsuarioResponse.from(usuario);
    }

    @POST
    public Response cadastrarUsuario(@Valid UsuarioRequest request) {
        Usuario usuarioCadastrado = usuarioService.cadastrarUsuario(ApiMapper.toUsuario(request));

        return Response
                .created(URI.create("/usuarios/" + usuarioCadastrado.getId()))
                .entity(UsuarioResponse.from(usuarioCadastrado))
                .build();
    }

    @PUT
    @Path("/{id}")
    public UsuarioResponse atualizarUsuario(@PathParam("id") Long id, @Valid UsuarioRequest request) {
        Usuario usuarioAtualizado = usuarioService.atualizarUsuario(id, ApiMapper.toUsuario(request));

        if (usuarioAtualizado == null) {
            throw new NotFoundException("Usuario nao encontrado");
        }

        return UsuarioResponse.from(usuarioAtualizado);
    }

    @DELETE
    @Path("/{id}")
    public Response removerUsuario(@PathParam("id") Long id) {
        boolean removido = usuarioService.removerUsuario(id);

        if (!removido) {
            throw new NotFoundException("Usuario nao encontrado");
        }

        return Response.noContent().build();
    }
}
