package com.gestorventas.deposito.services;

import com.gestorventas.deposito.config.exceptions.ImportException;
import com.gestorventas.deposito.dto.in.ProductoDto;
import com.gestorventas.deposito.dto.out.ImportErrorResponseDto;
import com.gestorventas.deposito.dto.out.ProductoResponseDto;
import com.gestorventas.deposito.models.producto.Categoria;
import com.gestorventas.deposito.models.producto.Producto;
import com.gestorventas.deposito.models.Vendedor;
import com.gestorventas.deposito.repositories.CategoriaRepository;
import com.gestorventas.deposito.repositories.ProductoRepository;
import com.gestorventas.deposito.repositories.VendedorRepository;
import com.gestorventas.deposito.specifications.ProductosSpecifications;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Servicio encargado de gestionar la logica del negocio relacionado con los Productos.
 * <p>
 *     Permite registrar, consultar, actualizar y eliminar productos.
 * </p>
 * @author Kevin William Olarte Braun
 */
@Service
@AllArgsConstructor
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final VendedorRepository vendedorRepository;
    private final CategoriaRepository categoriaRepository;

    /**
     * Guardar un nuevo producto en el sistema.
     * @param descripcion breve descrpcion del producto, puede ser tanto el nombre como una descripción.
     * @param precio precio base del producto, luego varuia en la linea del pedido
     * @return DTO con los datos guardados visibles.
     * @throws RuntimeException entidades inexistentes.
     */
    public ProductoResponseDto add(String descripcion, BigDecimal precio, Long idCategoria) {
        if (descripcion == null || descripcion.isEmpty()) {
            throw new IllegalArgumentException("El descripcion es obligatorio");
        }
        if (precio == null || precio.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El precio es obligatorio");
        }

        if (idCategoria == null) {
            throw new IllegalArgumentException("La categoria es obligatoria");
        }
        Categoria categoria = categoriaRepository.findById((long)idCategoria);
        if (categoria == null || !categoria.isActivo()) {
            throw new RuntimeException("Categoria inexistente");
        }


        Producto producto = Producto.builder()
                .categoria(categoria)
                .descripcion(descripcion)
                .precio(precio)
                .activo(true)
                .build();
        producto = productoRepository.save(producto);
        return new ProductoResponseDto(producto);
    }

    /**
     * Listado con todos los priductos del sistema.
     * @return listado con los productos
     */
    public List<ProductoResponseDto> getAll(Long idCategoria) {
        var spec = ProductosSpecifications.withFilter(idCategoria);
        return productoRepository.findAll(spec).stream()
                .map(ProductoResponseDto::new)
                .toList();
    }

    /**
     * Devuelve un producto especifico, mediante su identificador
     * @param id identificador numerico que se usara para buscar
     * @return producto buscado mediante su identificado.
     */
    public ProductoResponseDto get(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("El id es obligatorio");
        }
        Producto producto = productoRepository.findById((long) id);
        if (producto == null) {
            return null;
        }
        return new ProductoResponseDto(producto);
    }


    /**
     * Metodo para eliminar un producto del sistema.
     * @param id identificador numerico que se usara para buscar
     */
    public void remove(Long id) {
        Producto producto = productoRepository.findById((long) id);
        if (producto == null) {
            return;
        }
        producto.setActivo(false);
        productoRepository.save(producto);

    }

    public ProductoResponseDto update(long id, ProductoDto productoDto) {

        Producto producto = productoRepository.findById(id);
        if (producto == null) {
            throw new IllegalArgumentException("Producto no encontrado");
        }
        if (!producto.isActivo()) {
            throw  new IllegalArgumentException("Producto no encontrado.");
        }
        if (productoDto.getDescripcion() != null) {
            producto.setDescripcion(productoDto.getDescripcion());
        }
        if (productoDto.getPrecio() != null) {
            producto.setPrecio(productoDto.getPrecio());
        }
        if (productoDto.getIdCategoria() != null) {
            Categoria categoria  = categoriaRepository.findById((long) productoDto.getIdCategoria());
            if(categoria != null)
                producto.setCategoria(categoria);
        }
        producto = productoRepository.save(producto);
        return new ProductoResponseDto(producto);
    }

    public Map<String, Long> getNumProductosCategoriaByVendedor(Long idVendedor) {
        if(idVendedor == null){
            throw new IllegalArgumentException("El id vendedor es obligatorio");
        }
        Vendedor vendedor = vendedorRepository.findById((long) idVendedor);
        if(vendedor == null){
            throw new IllegalArgumentException("El id vendedor es obligatorio");
        }
        Map<String, Long> resultado = new LinkedHashMap<>();

        for (Categoria categoria : categoriaRepository.findAll()) {
            Long total = productoRepository.findVentasPorCategoria(idVendedor, categoria.getId());

            // Si no hay ventas → poner 0
            if (total == null) {
                total = 0L;
            }

            resultado.put(categoria.getNombre(), total);
        }


        return resultado;
    }

    public byte[] exportProductosCsv() {

        StringBuilder csv = new StringBuilder();
        csv.append("ID;Nombre;Precio;Categoria\n");

        List<Producto> productos = productoRepository.findAllByActivo(true);

        for (Producto p : productos) {
            csv.append(p.getId()).append(";")
                    .append(p.getDescripcion()).append(";")
                    .append(p.getPrecio()).append(";")
                    .append(p.getCategoria() != null ? p.getCategoria().getNombre() : "")
                    .append("\n");
        }

        return ("\uFEFF" + csv).getBytes(StandardCharsets.UTF_8);
    }

    /**
     * Importa productos desde un archivo CSV.
     *
     * <p><strong>Formato obligatorio del CSV:</strong></p>
     * <pre>
     * id;descripcion;precio;categoria
     * </pre>
     *
     * <ul>
     *     <li>La primera línea debe ser obligatoriamente la cabecera exacta:
     *         <code>id;descripcion;precio;categoria</code>.</li>
     *     <li>El delimitador utilizado debe ser el carácter <code>;</code>.</li>
     *     <li>Cada línea debe contener exactamente 4 campos.</li>
     * </ul>
     *
     * <p><strong>Validaciones aplicadas por cada registro:</strong></p>
     * <ul>
     *     <li>El ID debe ser numérico válido.</li>
     *     <li>La descripción no puede estar vacía.</li>
     *     <li>El precio debe ser un valor numérico mayor que 0.</li>
     *     <li>El producto no debe existir previamente en la base de datos.</li>
     *     <li>La categoría indicada debe existir en la base de datos.</li>
     * </ul>
     *
     * <p><strong>Comportamiento transaccional:</strong></p>
     * <ul>
     *     <li>La operación es atómica (transaccional).</li>
     *     <li>Si cualquier registro produce un error, se cancela completamente la importación.</li>
     *     <li>En caso de error se lanza {@link ImportException} con información
     *         del registro que causó el fallo.</li>
     * </ul>
     *
     * @param file archivo CSV recibido como {@link MultipartFile}
     * @return número total de productos importados correctamente
     * @throws ImportException si ocurre cualquier error de validación o persistencia.
     * @throws Exception si ocurre un error de lectura del archivo.
     */

    @Transactional(rollbackFor = {ImportException.class, IOException.class})
    public int importarCsvProductos(MultipartFile file) throws IOException {

        if (file == null || file.isEmpty()) {
            throw new ImportException(
                    new ImportErrorResponseDto("0", "El archivo CSV está vacío")
            );
        }

        int importados = 0;

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {

            // ================================
            // 1️⃣ CABECERA OBLIGATORIA
            // ================================
            String header = br.readLine();
            if (header == null) {
                throw new ImportException(
                        new ImportErrorResponseDto("0", "El CSV no contiene cabecera")
                );
            }

            String headerEsperado = "id;descripcion;precio;categoria";

            if (!header.trim().equalsIgnoreCase(headerEsperado)) {
                throw new ImportException(
                        new ImportErrorResponseDto("0",
                                "Cabecera incorrecta. Debe ser: " + headerEsperado)
                );
            }

            // ================================
            // 2️⃣ PROCESAR LÍNEAS
            // ================================
            String linea;
            int numLinea = 1;

            while ((linea = br.readLine()) != null) {
                numLinea++;

                if (linea.trim().isEmpty()) continue;

                String[] campos = linea.split(";", -1);

                if (campos.length != 4) {
                    throw new ImportException(
                            new ImportErrorResponseDto(
                                    safe(campos, 0),
                                    "Formato incorrecto en línea " + numLinea
                            )
                    );
                }

                String idStr = campos[0].trim();
                String descripcion = campos[1].trim();
                String precioStr = campos[2].trim();
                String categoriaStr = campos[3].trim();

                // ================================
                // VALIDACIONES
                // ================================

                Long id;
                try {
                    id = Long.parseLong(idStr);
                } catch (Exception e) {
                    throw new ImportException(
                            new ImportErrorResponseDto(idStr,
                                    "ID inválido en línea " + numLinea)
                    );
                }

                if (descripcion.isEmpty()) {
                    throw new ImportException(
                            new ImportErrorResponseDto(idStr,
                                    "Descripción vacía en línea " + numLinea)
                    );
                }

                BigDecimal precio;
                try {
                    precio = new BigDecimal(precioStr.replace(",", "."));
                } catch (Exception e) {
                    throw new ImportException(
                            new ImportErrorResponseDto(idStr,
                                    "Precio inválido en línea " + numLinea)
                    );
                }

                if (precio.compareTo(BigDecimal.ZERO) <= 0) {
                    throw new ImportException(
                            new ImportErrorResponseDto(idStr,
                                    "El precio debe ser mayor que 0 en línea " + numLinea)
                    );
                }

                if (categoriaStr.isEmpty()) {
                    throw new ImportException(
                            new ImportErrorResponseDto(idStr,
                                    "Categoría vacía en línea " + numLinea)
                    );
                }

                // ================================
                // PRODUCTO NO EXISTE
                // ================================
                if (productoRepository.existsById(id)) {
                    throw new ImportException(
                            new ImportErrorResponseDto(idStr,
                                    "El producto ya existe en la base de datos")
                    );
                }

                // ================================
                // CATEGORÍA DEBE EXISTIR
                // ================================
                Categoria categoria = categoriaRepository
                        .findByNombreIgnoreCase(categoriaStr)
                        .orElseThrow(() ->
                                new ImportException(
                                        new ImportErrorResponseDto(idStr,
                                                "La categoría '" + categoriaStr + "' no existe")
                                )
                        );

                // ================================
                // CREAR PRODUCTO CON BUILDER
                // ================================
                Producto producto = Producto.builder()
                        .descripcion(descripcion)
                        .precio(precio)
                        .categoria(categoria)
                        .activo(true)
                        .build();

                try {
                    productoRepository.save(producto);
                } catch (Exception e) {
                    throw new ImportException(
                            new ImportErrorResponseDto(idStr,
                                    "Error guardando producto: " + e.getMessage())
                    );
                }

                importados++;
            }
        }

        return importados;
    }

    private String safe(String[] arr, int index) {
        if (arr == null || index >= arr.length) return "";
        return arr[index] == null ? "" : arr[index].trim();
    }

}
