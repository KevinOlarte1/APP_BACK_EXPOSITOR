package com.gestorventas.deposito.services;

import com.gestorventas.deposito.dto.out.CategoriaResponseDto;
import com.gestorventas.deposito.models.Cliente;
import com.gestorventas.deposito.models.Pedido;
import com.gestorventas.deposito.models.Vendedor;
import com.gestorventas.deposito.models.producto.Categoria;
import com.gestorventas.deposito.repositories.CategoriaRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
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

    public int importarCsvCategorias(MultipartFile file) throws IOException {
        int insertados = 0;
        List<Categoria> categorias = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {

            // Leer cabecera
            br.readLine();

            String linea;

            while ((linea = br.readLine()) != null) {

                String[] campos = linea.split(";");
                if (campos.length < 2) continue;

                String nombre = campos[1].trim().toUpperCase();

                Categoria categoria = categoriaRepository.findByNombre(nombre);

                if (categoria != null) throw new RuntimeException("La categoria ya existe");
                categoria = new Categoria();
                categoria.setNombre(nombre);
                categorias.add(categoria);
                insertados++;
            }
        }
        categoriaRepository.saveAll(categorias);
        return insertados;
    }

    public byte[] exportCategoriasCsv() {
        StringBuilder csv = new StringBuilder();
        csv.append("ID;NOMBRE\n");
        List<Categoria> categorias = categoriaRepository.findAll();
        for (Categoria categoria : categorias) {
            csv.append(categoria.getId()).append(";").append(categoria.getNombre()).append("\n");
        }
        return ("\uFEFF" + csv).getBytes(StandardCharsets.UTF_8);
    }
}
