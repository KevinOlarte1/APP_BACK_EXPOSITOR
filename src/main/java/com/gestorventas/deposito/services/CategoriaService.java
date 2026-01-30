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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
        categoria.setActivo(true);
        return new CategoriaResponseDto(categoriaRepository.save(categoria));
    }

    /**
     * Listado con todas las categorias del sistema.
     * @return Listado de categorias.
     */
    public List<CategoriaResponseDto> getAll(){
        return categoriaRepository.findAllByActivo(true).stream().map(CategoriaResponseDto::new).toList();
    }

    /**
     * Obtener una categoria por su id.
     * @param id identificador de la categoria
     * @return Dto Response con la info de la categoria.
     */
    public CategoriaResponseDto get(long id){
        Categoria categoria = categoriaRepository.findById(id);
        if (categoria == null || !categoria.isActivo())
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
        if (categria == null || !categria.isActivo())
            throw new IllegalArgumentException("La categoria no existe");

        if (nombre != null && !nombre.isEmpty()) {
            nombre =nombre.trim().toUpperCase();
            if (categoriaRepository.findByNombre(nombre) != null)
                throw new IllegalArgumentException("La categoria ya existe");
            categria.setNombre(nombre);
            return new CategoriaResponseDto(categoriaRepository.save(categria));
        }
        return  new CategoriaResponseDto(categria);
    }

    public void delete(long id){
        Categoria categoria = categoriaRepository.findById(id);
        if (categoria == null)
            throw new IllegalArgumentException("La categoria no existe");
        categoria.setActivo(false);
        categoriaRepository.save(categoria);
    }

    public int importarCsvCategorias(MultipartFile file) throws IOException {

        if (file == null || file.isEmpty()) {
            throw new RuntimeException("El archivo CSV está vacío");
        }

        // 1) Parse + validación SIN tocar BD
        List<Categoria> nuevas = new ArrayList<>();
        Set<String> nombresEnCsv = new HashSet<>();

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {

            // Cabecera
            String header = br.readLine();
            if (header == null) {
                throw new RuntimeException("El archivo CSV está vacío");
            }

            String linea;
            int numLinea = 1; // header = 1

            while ((linea = br.readLine()) != null) {
                numLinea++;

                // Opcional: saltar líneas en blanco
                if (linea.trim().isEmpty()) continue;

                String[] campos = linea.split(";", -1); // -1 mantiene vacíos
                if (campos.length < 2) {
                    throw new RuntimeException("Formato inválido en línea " + numLinea + " (se esperaban al menos 2 columnas)");
                }

                String nombre = campos[1] == null ? "" : campos[1].trim().toUpperCase();

                if (nombre.isEmpty()) {
                    throw new RuntimeException("El nombre de la categoría no puede estar vacío (línea " + numLinea + ")");
                }

                // Unicidad dentro del CSV
                if (!nombresEnCsv.add(nombre)) {
                    throw new RuntimeException("Nombre de categoría duplicado en el CSV: '" + nombre + "' (línea " + numLinea + ")");
                }

                Categoria c = new Categoria();
                c.setNombre(nombre);
                c.setActivo(true);
                nuevas.add(c);
            }
        }

        if (nuevas.isEmpty()) {
            throw new RuntimeException("El CSV no contiene categorías válidas para importar");
        }

        // 2) Replace total (solo si todo validó)
        categoriaRepository.deleteAll();
        categoriaRepository.saveAll(nuevas);

        return nuevas.size();
    }

    public byte[] exportCategoriasCsv() {
        StringBuilder csv = new StringBuilder();
        csv.append("ID;NOMBRE\n");
        List<Categoria> categorias = categoriaRepository.findAll();
        for (Categoria categoria : categorias) {
            csv.append(categoria.getId()).append(";").append(categoria.getNombre()).append(";").append(categoria.isActivo()).append("\n");
        }
        return ("\uFEFF" + csv).getBytes(StandardCharsets.UTF_8);
    }
}
