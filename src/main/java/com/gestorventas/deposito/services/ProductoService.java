package com.gestorventas.deposito.services;

import com.gestorventas.deposito.dto.in.ProductoDto;
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
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
    public ProductoResponseDto add(String descripcion, double precio, Long idCategoria) {
        if (descripcion == null || descripcion.isEmpty()) {
            throw new IllegalArgumentException("El descripcion es obligatorio");
        }
        if (precio <= 0) {
            throw new IllegalArgumentException("El precio es obligatorio");
        }

        if (idCategoria == null) {
            throw new IllegalArgumentException("La categoria es obligatoria");
        }
        Categoria categoria = categoriaRepository.findById((long)idCategoria);
        if (categoria == null) {
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

    public ProductoResponseDto update(Long id, ProductoDto productoDto) {
        if (id == null) {
            return null;
        }
        Producto producto = productoRepository.findById((long)id);
        if (producto == null) {
            return null;
        }
        if (!producto.isActivo()) {
            return null;
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
     * Exporta a la base de datos productos a partir de un archivo CSV.
     * @param file fichero csv con los productos.
     * @return numero de productos insertados.
     * @throws IOException problemas con la lectura del fichero.
     */
    public int importarCsvProductos(MultipartFile file) throws IOException {
        int insertados = 0;
        System.out.println("Entra");

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {

            String header = br.readLine();
            String linea;

            while ((linea = br.readLine()) != null){
                System.out.println(linea);
                String[] campos = linea.split(";");

                if (campos.length < 4) continue;

                // --- PARSEO ---
                String idStr = campos[0].trim();
                String nombre = campos[1].trim();
                String precioStr = campos[2].trim();
                String categoria = campos[3].trim();

                double precio;

                try {
                    precio = Double.parseDouble(precioStr.replace(",", "."));
                } catch (NumberFormatException e) {
                    // si hay error de formato, saltamos la fila
                    continue;
                }
                Producto producto = Producto.builder()
                        .descripcion(nombre)
                        .precio(precio)
                        .categoria(categoriaRepository.findByNombre(categoria))
                        .build();
                productoRepository.save(producto);
                insertados++;
            }
        }
        return insertados;
    }
}
