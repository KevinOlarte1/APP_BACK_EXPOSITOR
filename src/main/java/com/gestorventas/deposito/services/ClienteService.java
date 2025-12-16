package com.gestorventas.deposito.services;

import com.gestorventas.deposito.dto.out.ClienteResponseDto;
import com.gestorventas.deposito.models.Cliente;
import com.gestorventas.deposito.models.Vendedor;
import com.gestorventas.deposito.repositories.ClienteRepository;
import com.gestorventas.deposito.repositories.PedidoRepository;
import com.gestorventas.deposito.repositories.VendedorRepository;
import com.gestorventas.deposito.specifications.ClienteSpecifications;
import io.swagger.v3.oas.models.links.Link;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Servicio encargado de gestionar la logica del negocio relacionado con los clientes.
 * <p>
 *     Permite registrar, consultar, actualizar y eliminar clientes.
 * </p>
 * @author Kevin William Olarte Braun
 */
@Service
@AllArgsConstructor
public class ClienteService {
    private final ClienteRepository clienteRepository;
    private final VendedorRepository vendedorRepository;
    private final PedidoRepository pedidoRepository;

    /**
     * Guardar un cliente nuevo en el sistema.
     * @param nombre nombre del cleinte
     * @param vendedorId a quien  le pertenece ese cleinte
     * @return DTO con los datos guardados visibles.
     * @throws IllegalArgumentException datos erroneos.
     */
    public ClienteResponseDto add(String nombre,String cif, long vendedorId) {

        if (nombre == null || nombre.isEmpty())
            throw new IllegalArgumentException("El nombre no puede estar vacío");
        if (cif == null || cif.isEmpty())
            throw new IllegalArgumentException("El cif no puede estar vacio");

        if(clienteRepository.existsByCif(cif))
            throw new IllegalArgumentException("El cif ya existe");

        Vendedor vendedor = vendedorRepository.findById(vendedorId);
        if (vendedor == null)
            throw new RuntimeException("Vendedor no encontrado");

        Cliente cliente = new Cliente();
        cliente.setNombre(nombre);
        cliente.setVendedor(vendedor);

        return new ClienteResponseDto(clienteRepository.save(cliente));
    }

    /**
     * Obtener una linea de pedudo por su id.
     * @param id identificador del cliente a buscar
     * @return DTO con los datos guardados visibles.
     */
    public ClienteResponseDto get(long id) {
        Cliente cliente = clienteRepository.findById(id);
        if (cliente == null)
            return null;
        return new ClienteResponseDto(cliente);
    }

    /**
     * Obtener una linea de pedudo por su id.
     * @param id identificador del cliente a buscar
     * @return DTO con los datos guardados visibles.
     */
    public ClienteResponseDto get(long idVendedor, long id) {
        Cliente cliente = clienteRepository.findById(id);
        if (cliente == null)
            return null;
        if (cliente.getVendedor().getId() != idVendedor)
            return null;
        return new ClienteResponseDto(cliente);
    }

    /**
     * Obtener listado de todos los lineasdePedido registrados en el sistema.
     * se le puede añadir los siguentes idVendedor
     * @param idVendedor vendedor al que pertenece
     * @return Listado DTO con todos los clientes.
     */
    public List<ClienteResponseDto> getAll(Long idVendedor) {
        return clienteRepository.findAll(ClienteSpecifications.filter(idVendedor)).stream().map(ClienteResponseDto::new).toList();
    }

    /**
     * Obtener listado de todos los clientes registrados en el sistema.
     * @return Listado DTO con todos los clientes.
     */
    public List<ClienteResponseDto> getAll() {
        return clienteRepository.findAll().stream().map(ClienteResponseDto::new).toList();
    }

    /**
     * Actualizar los datos de un cliente existente.
     *
     * @param id         identificador del cliente a actualizar
     * @param nombre     nuevo nombre del cliente (opcional)
     * @param vendedorId identificador del nuevo vendedor (opcional)
     * @return la entidad {@link Cliente} actualizada
     * @throws RuntimeException si el cliente o vendedor no existen
     */
    public ClienteResponseDto update(long id, String nombre, String cif, long vendedorId) {
        Cliente cliente = clienteRepository.findById(id);
        if (cliente == null)
            return null;

        if (cliente.getVendedor().getId() != vendedorId)
            return null;


        if (nombre != null && !nombre.isEmpty()) {
            cliente.setNombre(nombre);
        }

        if (cif != null && !cif.isEmpty()){
            if(!clienteRepository.existsByCif(cif))
                cliente.setCif(cif);
        }

        return new ClienteResponseDto(clienteRepository.save(cliente));
    }

    /**
     * Eliminar un cliente del sistema por su identificador único.
     *
     * @param id identificador del cliente a eliminar
     */
    public void delete(long id) {
        clienteRepository.deleteById(id);
    }

    public void delete(long id, long idVendedor) {
        Cliente cliente = clienteRepository.findById(id);
        if (cliente != null){
            if (cliente.getVendedor().getId() == idVendedor)
                clienteRepository.deleteById(id);
        }
    }

    /**
     * Obtener estadisticas de los gastos anuelaes del cliente
     * @param idCliente identificador del cliente
     * @return {@return Map<String, Double> total} mapeo con los gastos.
     */
    public Map<String, Double> getStats(Long idCliente) {
        Map<String,Double> total = new LinkedHashMap<>();
        for (Object[] row: pedidoRepository.getEstadisticaPorCliente(idCliente)){
            String year = String.valueOf(((Number) row[0]).intValue());
            Double totalPedido = ((Number) row[1]).doubleValue();
            total.put(year, totalPedido);
        }
        return total;
    }

    /**
     * Obtener estadisticas de los gastos anuelaes del cliente
     * @param idCliente identificador del cliente
     * @param idVendedor identificador del vendedor
     * @return {@return Map<String, Double> total} mapeo con los gastos.
     */
    public Map<String, Double> getStats(Long idCliente, Long idVendedor) {
        Map<String,Double> total = new LinkedHashMap<>();
        for (Object[] row: pedidoRepository.getTotalesPorClientesDeVendedor(idVendedor,idCliente)){
            String year = String.valueOf(((Number) row[0]).intValue());
            Double totalPedido = ((Number) row[1]).doubleValue();
            total.put(year, totalPedido);
        }
        return total;
    }

    public byte[] exportClientesCsv() {

        StringBuilder csv = new StringBuilder();
        csv.append("ID;Nombre;CIF;Vendedor\n");

        List<Cliente> clientes = clienteRepository.findAll();

        for (Cliente c : clientes) {
            csv.append(c.getId()).append(";")
                    .append(c.getNombre()).append(";")
                    .append(c.getCif()).append(";")
                    .append(c.getVendedor().getEmail())
                    .append("\n");
        }

        // UTF-8 BOM para Excel (opcional)
        return ("\uFEFF" + csv).getBytes(StandardCharsets.UTF_8);
    }

    public int importarCsvClientes(MultipartFile file) throws Exception {

        int insertados = 0;

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {

            // Leer cabecera
            br.readLine();

            String linea;

            while ((linea = br.readLine()) != null) {

                String[] campos = linea.split(";");
                if (campos.length < 4) continue;

                String nombre = campos[1].trim();
                String cif = campos[2].trim();
                String vendedorEmail = campos[3].trim();

                // Buscar vendedor por email
                Vendedor vendedor = vendedorRepository.findByEmail(vendedorEmail)
                        .orElse(null);

                if (vendedor == null) {
                    System.out.println("⚠️ No existe vendedor con email: " + vendedorEmail + " — Cliente ignorado.");
                    continue; // No insertamos clientes sin vendedor válido
                }

                // 2️⃣ Comprobar SI YA EXISTE el CIF
                boolean existe = clienteRepository.findByCif(cif).isPresent();

                if (existe) {
                    System.out.println("⚠️ CIF duplicado: " + cif + " — Cliente ignorado.");
                    continue;
                }


                Cliente cliente = new Cliente();
                cliente.setNombre(nombre);
                cliente.setCif(cif);
                cliente.setVendedor(vendedor);

                clienteRepository.save(cliente);
                insertados++;
            }
        }

        return insertados;
    }


}
