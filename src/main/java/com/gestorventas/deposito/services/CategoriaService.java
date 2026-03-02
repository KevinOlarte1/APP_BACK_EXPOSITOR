package com.gestorventas.deposito.services;

import com.gestorventas.deposito.config.exceptions.ImportException;
import com.gestorventas.deposito.dto.out.CategoriaResponseDto;
import com.gestorventas.deposito.dto.out.ImportErrorResponseDto;
import com.gestorventas.deposito.models.Cliente;
import com.gestorventas.deposito.models.Pedido;
import com.gestorventas.deposito.models.Vendedor;
import com.gestorventas.deposito.models.producto.Categoria;
import com.gestorventas.deposito.repositories.CategoriaRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
            throw new IllegalArgumentException("La categoria no existe");
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

    @Transactional(rollbackFor = { ImportException.class, Exception.class })
    public int importarCsvCategorias(MultipartFile file) throws Exception {

        if (file == null || file.isEmpty()) {
            throw new ImportException(new ImportErrorResponseDto("0", "El archivo CSV está vacío"));
        }

        int importadas = 0;

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {

            // ================================
            // 1) CABECERA OBLIGATORIA
            // ================================
            String header = br.readLine();
            if (header == null) {
                throw new ImportException(new ImportErrorResponseDto("0", "El CSV no contiene cabecera"));
            }

            String headerEsperado = "id;nombre";
            if (!header.trim().equalsIgnoreCase(headerEsperado)) {
                throw new ImportException(new ImportErrorResponseDto(
                        "0",
                        "Cabecera incorrecta. Debe ser: " + headerEsperado
                ));
            }

            // ================================
            // 2) PROCESAR LÍNEAS
            // ================================
            String linea;
            int numLinea = 1;

            while ((linea = br.readLine()) != null) {
                numLinea++;

                if (linea.trim().isEmpty()) continue;

                String[] campos = linea.split(";", -1);
                if (campos.length != 2) {
                    throw new ImportException(new ImportErrorResponseDto(
                            safe(campos, 0),
                            "Formato incorrecto en línea " + numLinea + " (se esperaban 2 campos)"
                    ));
                }

                String idStr = campos[0].trim();
                String nombre = campos[1].trim();

                if (nombre.isEmpty()) {
                    throw new ImportException(new ImportErrorResponseDto(
                            idStr,
                            "Nombre vacío en línea " + numLinea
                    ));
                }

                String nombreUpper = nombre.toUpperCase().trim();

                // Unicidad (ajusta el método si tu repo se llama distinto)
                if (categoriaRepository.existsByNombreIgnoreCase(nombreUpper)) {
                    throw new ImportException(new ImportErrorResponseDto(
                            idStr,
                            "Ya existe una categoría con nombre '" + nombreUpper + "'"
                    ));
                }

                // Crear categoría (como me pediste)
                Categoria c = new Categoria();
                c.setNombre(nombreUpper);
                c.setActivo(true);

                try {
                    categoriaRepository.save(c);
                } catch (Exception e) {
                    throw new ImportException(new ImportErrorResponseDto(
                            idStr,
                            "Error guardando categoría en línea " + numLinea + ": " + e.getMessage()
                    ));
                }

                importadas++;
            }
        }

        return importadas;
    }

    private String safe(String[] arr, int index) {
        if (arr == null || index >= arr.length) return "";
        return arr[index] == null ? "" : arr[index].trim();
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
