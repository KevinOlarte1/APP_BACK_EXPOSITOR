package com.gestorventas.deposito.services;

import com.gestorventas.deposito.dto.out.CategoriaResponseDto;
import com.gestorventas.deposito.models.producto.Categoria;
import com.gestorventas.deposito.repositories.CategoriaRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;


    /**
     * Guardar una nueva categoria en el sistema.
     * @param nombre nombre de la categoria
     * @return Dto Response con la info de la categoria creada.
     */
    public CategoriaResponseDto add (String nombre){
        if (nombre == null || nombre.isEmpty())
            throw new IllegalArgumentException("El nombre de la categoria no puede ser nulo o vacio");
        nombre =nombre.trim().toUpperCase();
        if (categoriaRepository.findByNombre(nombre) != null)
            throw new IllegalArgumentException("La categoria ya existe");

        Categoria categoria = new Categoria();
        categoria.setNombre(nombre);
        return new CategoriaResponseDto(categoriaRepository.save(categoria));
    }

    /**
     * Listado con todas las categorias del sistema.
     * @return Listado de categorias.
     */
    public List<CategoriaResponseDto> getAll(){
        return categoriaRepository.findAll().stream().map(CategoriaResponseDto::new).toList();
    }

    /**
     * Obtener una categoria por su id.
     * @param id identificador de la categoria
     * @return Dto Response con la info de la categoria.
     */
    public CategoriaResponseDto get(long id){
        Categoria categoria = categoriaRepository.findById(id);
        if (categoria == null)
            return null;
        return new CategoriaResponseDto(categoria);
    }

    /**
     * Actualizar una categoria ya registrada.
     * @param id identificador de la categoria a actualizar
     * @param nombre nuevo nombre de la categoria
     * @return Dto Response con la info de la categoria actualizada.
     */
    public CategoriaResponseDto update(long id, String nombre){
        Categoria categria = categoriaRepository.findById(id);
        if (categria == null)
            throw new IllegalArgumentException("La categoria no existe");

        if (nombre != null && !nombre.isEmpty()) {
            nombre =nombre.trim().toUpperCase();
            if (categoriaRepository.findByNombre(nombre) != null)
                throw new IllegalArgumentException("La categoria ya existe");
            categria.setNombre(nombre);
            System.out.println("Categoria actualizada");
            return new CategoriaResponseDto(categoriaRepository.save(categria));
        }
        return  new CategoriaResponseDto(categria);
    }

    public void delete(long id){
        Categoria categoria = categoriaRepository.findById(id);
        if (categoria == null)
            throw new IllegalArgumentException("La categoria no existe");
        categoriaRepository.delete(categoria);
    }
}
