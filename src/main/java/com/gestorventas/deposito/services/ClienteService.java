package com.gestorventas.deposito.services;

import com.gestorventas.deposito.config.exceptions.ImportException;
import com.gestorventas.deposito.dto.out.ClienteResponseDto;
import com.gestorventas.deposito.dto.out.ImportErrorResponseDto;
import com.gestorventas.deposito.models.Cliente;
import com.gestorventas.deposito.models.Vendedor;
import com.gestorventas.deposito.repositories.ClienteRepository;
import com.gestorventas.deposito.repositories.PedidoRepository;
import com.gestorventas.deposito.repositories.VendedorRepository;
import com.gestorventas.deposito.specifications.ClienteSpecifications;
import io.swagger.v3.oas.models.links.Link;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.*;
import java.util.regex.Pattern;

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
    public ClienteResponseDto add(String nombre,String cif, long vendedorId, String telefono, String email) {

        if (nombre == null || nombre.isEmpty())
            throw new IllegalArgumentException("El nombre no puede estar vacío");
        if (cif == null || cif.isEmpty())
            throw new IllegalArgumentException("El cif no puede estar vacio");

        if(clienteRepository.existsByCif(cif))
            throw new IllegalArgumentException("El cif ya existe");

        if (telefono == null || !esTelefonoValido(telefono))
            throw new IllegalArgumentException("El teléfono no es válido");

        if (email == null || !esEmailValido(email))
            throw new IllegalArgumentException("El email no es válido");

        Vendedor vendedor = vendedorRepository.findById(vendedorId);
        if (vendedor == null)
            throw new RuntimeException("Vendedor no encontrado");



        Cliente cliente = new Cliente();
        cliente.setNombre(nombre);
        cliente.setVendedor(vendedor);
        cliente.setCif(cif);
        cliente.setTelefono(telefono);
        cliente.setEmail(email);

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
    public ClienteResponseDto update(long id, String nombre, String cif, Long idVededor,String telefono, String email, Long vendedorId) {
        Cliente cliente = clienteRepository.findById(id);
        if (cliente == null)
            throw new RuntimeException("Cliente no encontrado");

        if (vendedorId != null)
            if (!Objects.equals(vendedorId, cliente.getVendedor().getId()))
                throw new IllegalArgumentException("No tienes este cliente asociado.");


        if (nombre != null && !nombre.isEmpty()) {
            cliente.setNombre(nombre);
        }

        if (cif != null && !cif.isEmpty()){
            if(!clienteRepository.existsByCif(cif))
                cliente.setCif(cif);
        }

        if (telefono != null && esTelefonoValido(telefono))
            cliente.setTelefono(telefono);

        if (email != null && esEmailValido(email))
            cliente.setEmail(email);


        vendedorRepository.findById(idVededor).ifPresent(cliente::setVendedor);

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
        List<Object[]> pedidos = pedidoRepository.getEstadisticaPorCliente(idCliente);
        if (pedidos.isEmpty()){
            total.put(String.valueOf(LocalDate.now().getYear()),0.0);
        }
        else{
            for (Object[] row: pedidos){
                String year = String.valueOf(((Number) row[0]).intValue());
                Double totalPedido = ((Number) row[1]).doubleValue();
                total.put(year, totalPedido);
            }
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
        List<Object[]> pedidos = pedidoRepository.getTotalesPorClientesDeVendedor(idVendedor,idCliente);
        if (pedidos.isEmpty()){
            total.put(String.valueOf(LocalDate.now().getYear()),0.0);
        }
        else{
            for (Object[] row: pedidos){
                String year = String.valueOf(((Number) row[0]).intValue());
                Double totalPedido = ((Number) row[1]).doubleValue();
                total.put(year, totalPedido);
            }
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

    /**
     * Importa clientes desde un archivo CSV.
     *
     * <p><strong>Formato obligatorio del CSV:</strong></p>
     * <pre>
     * id;nombre;cif;correoVendedor
     * </pre>
     *
     * <ul>
     *     <li>La primera línea debe ser obligatoriamente la cabecera exacta:
     *         <code>id;nombre;cif;correoVendedor</code>.</li>
     *     <li>El delimitador utilizado debe ser el carácter <code>;</code>.</li>
     *     <li>Cada línea debe contener exactamente 4 campos.</li>
     * </ul>
     *
     * <p><strong>Validaciones aplicadas por cada registro:</strong></p>
     * <ul>
     *     <li>El ID debe ser numérico válido.</li>
     *     <li>El nombre no puede estar vacío.</li>
     *     <li>El CIF debe ser válido (NIF/NIE/CIF español) y único en la base de datos.</li>
     *     <li>El correo del vendedor debe existir en la base de datos.</li>
     *     <li>El cliente no debe existir previamente (según ID o CIF).</li>
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
     * @return número total de clientes importados correctamente
     * @throws ImportException si ocurre cualquier error de validación o persistencia.
     * @throws Exception si ocurre un error de lectura del archivo.
     */

    @Transactional(rollbackFor = { ImportException.class, Exception.class })
    public int importarCsvClientes(MultipartFile file) throws Exception {

        if (file == null || file.isEmpty()) {
            throw new ImportException(new ImportErrorResponseDto("0", "El archivo CSV está vacío"));
        }

        int importados = 0;

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {

            // ================================
            // 1) CABECERA OBLIGATORIA
            // ================================
            String header = br.readLine();
            if (header == null) {
                throw new ImportException(new ImportErrorResponseDto("0", "El CSV no contiene cabecera"));
            }

            String headerEsperado = "id;nombre;cif;correoVendedor";
            if (!header.trim().equalsIgnoreCase(headerEsperado)) {
                throw new ImportException(new ImportErrorResponseDto(
                        "0",
                        "Cabecera incorrecta. Debe ser: " + headerEsperado
                ));
            }

            // ================================
            // 2) PROCESAR FILAS
            // ================================
            String linea;
            int numLinea = 1;

            while ((linea = br.readLine()) != null) {
                numLinea++;

                if (linea.trim().isEmpty()) continue;

                String[] campos = linea.split(";", -1);
                if (campos.length != 4) {
                    throw new ImportException(new ImportErrorResponseDto(
                            safe(campos, 0),
                            "Formato incorrecto en línea " + numLinea + " (se esperaban 4 campos)"
                    ));
                }

                String idStr = campos[0].trim();
                String nombre = campos[1].trim();
                String cifRaw = campos[2].trim();
                String correoVendedor = campos[3].trim();

                // ---------- Validaciones ----------
                Long id = parseLongStrict(idStr, numLinea);

                if (nombre.isEmpty()) {
                    throw new ImportException(new ImportErrorResponseDto(idStr,
                            "Nombre vacío en línea " + numLinea));
                }

                String cif = normalizarCif(cifRaw);
                if (cif.isEmpty()) {
                    throw new ImportException(new ImportErrorResponseDto(idStr,
                            "CIF vacío en línea " + numLinea));
                }

                if (!isCifValido(cif)) {
                    throw new ImportException(new ImportErrorResponseDto(idStr,
                            "CIF inválido '" + cif + "' en línea " + numLinea));
                }

                // Unicidad CIF
                // Ajusta el método a tu repo real (existsByCifIgnoreCase, existsByCif, etc.)
                if (clienteRepository.existsByCifIgnoreCase((cif))){
                    throw new ImportException(new ImportErrorResponseDto(idStr,
                            "Ya existe un cliente con CIF '" + cif + "'"));
                }



                if (correoVendedor.isEmpty()) {
                    throw new ImportException(new ImportErrorResponseDto(idStr,
                            "CorreoVendedor vacío en línea " + numLinea));
                }

                // Vendedor debe existir por correo
                // Ajusta el método a tu repo real (findByCorreoIgnoreCase / findByEmail / etc.)
                Vendedor vendedor = vendedorRepository.findByEmailIgnoreCase(correoVendedor)
                        .orElseThrow(() -> new ImportException(new ImportErrorResponseDto(
                                idStr,
                                "No existe vendedor con correo '" + correoVendedor + "'"
                        )));

                // ---------- Construcción ----------
                Cliente cliente = new Cliente();
                cliente.setNombre(nombre);
                cliente.setCif(cif);
                cliente.setVendedor(vendedor);

                try {
                    clienteRepository.save(cliente);
                } catch (Exception e) {
                    // Cualquier fallo cancela toda la importación por @Transactional
                    throw new ImportException(new ImportErrorResponseDto(
                            idStr,
                            "Error guardando cliente en línea " + numLinea + ": " + e.getMessage()
                    ));
                }

                importados++;
            }
        }

        return importados;
    }

    /* ===================== HELPERS ===================== */

    private static String safe(String[] arr, int idx) {
        if (arr == null || idx < 0 || idx >= arr.length) return "";
        return arr[idx] == null ? "" : arr[idx].trim();
    }

    private static Long parseLongStrict(String idStr, int numLinea) throws ImportException {
        try {
            return Long.parseLong(idStr.trim());
        } catch (Exception e) {
            throw new ImportException(new ImportErrorResponseDto(idStr,
                    "ID inválido en línea " + numLinea));
        }
    }

    private static String normalizarCif(String cif) {
        return cif == null ? "" : cif.trim().toUpperCase().replaceAll("\\s+", "");
    }

    /**
     * Valida CIF/NIF/NIE (España) de forma robusta:
     * - NIF: 8 dígitos + letra
     * - NIE: X/Y/Z + 7 dígitos + letra
     * - CIF: Letra + 7 dígitos + control (dígito o letra según tipo)
     */
    private static boolean isCifValido(String value) {
        if (value == null) return false;
        String v = value.toUpperCase();

        // NIF: 8 dígitos + letra
        if (v.matches("^\\d{8}[A-Z]$")) {
            return validarNif(v);
        }

        // NIE: X/Y/Z + 7 dígitos + letra
        if (v.matches("^[XYZ]\\d{7}[A-Z]$")) {
            return validarNie(v);
        }

        // CIF: Letra + 7 dígitos + control
        if (v.matches("^[ABCDEFGHJNPQRSUVW]\\d{7}[0-9A-J]$")) {
            return validarCifEmpresa(v);
        }

        return false;
    }

    private static boolean validarNif(String nif) {
        final String letras = "TRWAGMYFPDXBNJZSQVHLCKE";
        int num = Integer.parseInt(nif.substring(0, 8));
        char letra = nif.charAt(8);
        return letras.charAt(num % 23) == letra;
    }

    private static boolean validarNie(String nie) {
        // Sustituir X/Y/Z por 0/1/2 y validar como NIF
        char first = nie.charAt(0);
        String prefix = switch (first) {
            case 'X' -> "0";
            case 'Y' -> "1";
            case 'Z' -> "2";
            default -> "";
        };
        String numStr = prefix + nie.substring(1, 8); // 8 dígitos
        String nifEquivalente = numStr + nie.charAt(8);
        return validarNif(nifEquivalente);
    }

    private static boolean validarCifEmpresa(String cif) {
        // CIF: L D1 D2 D3 D4 D5 D6 D7 C
        char tipo = cif.charAt(0);
        String numeros = cif.substring(1, 8);
        char control = cif.charAt(8);

        int sumaPares = 0;
        int sumaImpares = 0;

        for (int i = 0; i < numeros.length(); i++) {
            int n = numeros.charAt(i) - '0';
            if ((i % 2) == 0) { // posiciones impares (1,3,5,7) -> i=0,2,4,6
                int doble = n * 2;
                sumaImpares += (doble / 10) + (doble % 10);
            } else { // pares (2,4,6)
                sumaPares += n;
            }
        }

        int total = sumaPares + sumaImpares;
        int unidad = total % 10;
        int digitoControl = (unidad == 0) ? 0 : (10 - unidad);

        // Control letra equivalente (0->J, 1->A, ... 9->I)
        final String controlLetras = "JABCDEFGHI";
        char letraControl = controlLetras.charAt(digitoControl);

        // Tipos que obligan a control letra
        boolean obligaLetra = "PQRSNW".indexOf(tipo) >= 0;
        // Tipos que obligan a control dígito
        boolean obligaDigito = "ABEH".indexOf(tipo) >= 0;

        if (obligaLetra) return control == letraControl;
        if (obligaDigito) return control == (char) ('0' + digitoControl);

        // Resto: puede ser letra o dígito
        return control == letraControl || control == (char) ('0' + digitoControl);
    }


    private boolean esTelefonoValido(String telefono) {
        // Acepta números con o sin prefijo internacional (+)
        String regex = "^\\+?[0-9]{9,15}$";
        return Pattern.matches(regex, telefono);
    }

    private boolean esEmailValido(String email) {
        String regex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
        return Pattern.matches(regex, email);
    }


}
