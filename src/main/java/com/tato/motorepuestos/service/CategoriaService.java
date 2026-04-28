package com.tato.motorepuestos.service;

import com.tato.motorepuestos.model.Categoria;
import com.tato.motorepuestos.repository.CategoriaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoriaService {

    @Autowired
    private CategoriaRepository categoriaRepository;

    public List<Categoria> listarTodas() {
        return categoriaRepository.findAll();
    }

    public List<Categoria> listarActivas() {
        return categoriaRepository.findByActivoTrue();
    }

    public Categoria obtenerPorId(Long id) {
        return categoriaRepository.findById(id).orElse(null);
    }

    public Categoria guardarCategoria(Categoria categoria) {

        if (categoria.getId() == null) {
            if (categoriaRepository.existsByNombre(categoria.getNombre())) {
                throw new RuntimeException("Ya existe una categoría con el nombre: " + categoria.getNombre());
            }
        } else {

            if (categoriaRepository.existsByNombreAndIdNot(categoria.getNombre(), categoria.getId())) {
                throw new RuntimeException("Ya existe una categoría con el nombre: " + categoria.getNombre());
            }
        }
        return categoriaRepository.save(categoria);
    }

    public void cambiarEstado(Long id, boolean estado) {
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));

        if (!estado && categoriaRepository.existsProductoConCategoria(id)) {
            throw new RuntimeException("No se puede desactivar una categoría que tiene productos asignados");
        }

        categoria.setActivo(estado);
        categoriaRepository.save(categoria);
    }

    public void eliminarLogico(Long id) {
        cambiarEstado(id, false);
    }
}