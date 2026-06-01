package org.acme.service;

import java.util.List;
import java.util.Set;

import org.acme.model.Usuario;
import org.acme.repository.UsuarioRepository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;

@ApplicationScoped
public class UsuarioService {

    private static final Set<String> ROLES_VALIDAS = Set.of("FUNCIONARIO", "GESTOR", "ADMIN");

    @Inject
    UsuarioRepository usuarioRepository;

    public List<Usuario> listarUsuarios() {
        return usuarioRepository.listAll();
    }

    public Usuario buscarPorId(Long id) {
        return usuarioRepository.findById(id);
    }

    @Transactional
    public Usuario cadastrarUsuario(Usuario usuario) {
        normalizarRole(usuario);
        usuarioRepository.persist(usuario);
        return usuario;
    }

    @Transactional
    public Usuario atualizarUsuario(Long id, Usuario dadosAtualizados) {
        Usuario usuario = usuarioRepository.findById(id);

        if (usuario == null) {
            return null;
        }

        usuario.setNome(dadosAtualizados.getNome());
        usuario.setRole(dadosAtualizados.getRole());
        usuario.setCargo(dadosAtualizados.getCargo());
        normalizarRole(usuario);

        return usuario;
    }

    @Transactional
    public boolean removerUsuario(Long id) {
        return usuarioRepository.deleteById(id);
    }

    private void normalizarRole(Usuario usuario) {
        if (usuario.getRole() == null || usuario.getRole().isBlank()) {
            throw new BadRequestException("Role do usuario e obrigatoria");
        }

        usuario.setRole(usuario.getRole().trim().toUpperCase());
        if (!ROLES_VALIDAS.contains(usuario.getRole())) {
            throw new BadRequestException("Role do usuario invalida");
        }
    }
}
