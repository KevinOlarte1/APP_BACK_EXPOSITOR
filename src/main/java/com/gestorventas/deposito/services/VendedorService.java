package com.gestorventas.deposito.services;

import com.gestorventas.deposito.dto.out.VendedorResponseDto;
import com.gestorventas.deposito.enums.Role;
import com.gestorventas.deposito.interfaces.GastosCliente;
import com.gestorventas.deposito.interfaces.ProductoCount;
import com.gestorventas.deposito.models.Vendedor;
import com.gestorventas.deposito.repositories.ClienteRepository;
import com.gestorventas.deposito.repositories.PedidoRepository;
import com.gestorventas.deposito.repositories.VendedorRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Servicio encargado de gestionar la logica del negocio relacionado con los vendedores.
 * <p>
 *     Permite registrar, consultar, actualizar y eliminar vendedores.
 * </p>
 * @author Kevin William Olarte Braun
 */
@Service
@AllArgsConstructor
public class VendedorService {

    private final PedidoRepository pedidoRepository;
    private final ClienteRepository clienteRepository;
    private VendedorRepository vendedorRepository;
    private PasswordEncoder passwordEncoder;


    /**
     * Guardar un nuevo vendedor en el sistema.
     * @param nombre nombre que recibira el vendedor
     * @param password contraseña para seguridad
     * @param email email del vendedor
     * @param role rol que tendra el vendedor
     * @return DTO con los datos guardados visibles.
     * @throws IllegalArgumentException valores invalidos.
     */
    public VendedorResponseDto add(String nombre,String apellido, String password, String email, Role role) {
        if (nombre == null || apellido == null || password == null || nombre.isEmpty() || password.isEmpty() || email == null || email.isEmpty())
            throw new IllegalArgumentException();

        if (!MailService.esEmailValido(email))
            throw new IllegalArgumentException("El email no es valido");

        var vendedor =Vendedor.builder()
                .nombre(nombre)
                .apellido(apellido)
                .email(email)
                .password(passwordEncoder.encode(password))
                .roles(Set.of(role))
                .build();
        return new VendedorResponseDto(vendedorRepository.save(vendedor));

    }

    /**
     * Obtener un vendedor por su id.
     * @param id id que representa el identificador unico
     * @return DTO con los datos guardados visibles.
     */
    public VendedorResponseDto get(long id) {
        Vendedor vendedor = vendedorRepository.findById(id);
        if (vendedor == null)
            return null;
        return new VendedorResponseDto(vendedor);
    }

    /**
     * Obtener listado de todos los vendedores registrados en el sistema
     * @return listado con los vendedores en formato DTO.
     */
    public List<VendedorResponseDto> getAll() {
        return vendedorRepository.findAll().stream()
                .map(VendedorResponseDto::new)
                .toList();
    }

    /**
     * Actualizar los datos de un vendedor segun el identificador id
     * @param id identificador que se encargara cual vendedor se actualizara
     * @param nombre nombre nuevo a actualizar
     * @param password cambio de la contraseña
     * @return DTO con los datos guardados visibles.
     * @throws RuntimeException no existe en el sistema un vendedor con ese id.
     */
    public VendedorResponseDto update(long id, String nombre,String apellido, String password, String email) {

        Vendedor vendedor = vendedorRepository.findById(id);
        if (vendedor == null)
            return  null;

        if (nombre != null && !nombre.isEmpty()) {
            vendedor.setNombre(nombre);
        }
        if (apellido != null && !apellido.isEmpty()) {
            vendedor.setApellido(apellido);
        }
        if (password != null && !password.isEmpty()) {
            vendedor.setPassword(password);
        }
        if (email != null && !email.isEmpty()) {
            if (MailService.esEmailValido(email))
                vendedor.setEmail(email);
        }
        return new VendedorResponseDto(vendedorRepository.save(vendedor));
    }

    /**
     * Borrar un vendedor del sistema en cascada con sus relaciones
     * @param id id del vendedor a borrar.
     */
    public void delete(long id) {

        vendedorRepository.deleteById(id);
    }

    /**
     * Metodo para obtener los datos de un usuario en formato JSON.Para el uso en el JWT
     * @param u vendedor que se desea obtener los datos.
     * @return Map con los datos del usuario.
     */
    public Map<String, Object> userClaims(Vendedor u) {
        return Map.of(
                "name", u.getNombre(),
                "roles", u.getRoles().stream().map(Enum::name).toList()
        );
    }

    public Map<String, Double> getStats() {
        Map<String, Double> result = new LinkedHashMap<>();
        for (Object[] row: pedidoRepository.getEstadisticaGlobal()){
            String year = String.valueOf(((Number) row[0]).intValue());
            Double total = ((Number) row[1]).doubleValue();
            result.put(year, total);
        }
        return result;
    }

    public Map<String, Double> getStats(Long idVendedor) {
        Map<String, Double> result = new LinkedHashMap<>();
        for (Object[] row: pedidoRepository.getEstadisticaPorVendedor(idVendedor)){
            String year = String.valueOf(((Number) row[0]).intValue());
            Double total = ((Number) row[1]).doubleValue();
            result.put(year, total);
        }
        return result;
    }

    /**
     * Obtener el numero de pedidos abiertos y cerrados por vendedor.
     * @param idVendedor identificador del vendedor
     * @return Map con los datos.
     */
    public Map<String, Long> getNumPedidos(Long idVendedor) {
        if (idVendedor == null){
            return null;
        }
        Map<String, Long> result = new LinkedHashMap<>();
        Vendedor vendedor = vendedorRepository.findById((long)idVendedor);
        if (vendedor == null){
            return null;
        }
        result.put("abierrtos", pedidoRepository.countNotFinalizadosByVendedor(idVendedor));
        result.put("cerrados", pedidoRepository.countFinalizadosByVendedor(idVendedor));
        return result;

    }

    /**
     * Obtener los gastos totales por cliente.
     * @param idVendedor identificador del vendedor
     * @return Map con los datos.
     */
    public List<GastosCliente> getGastosClientes(Long idVendedor) {
        if (idVendedor == null){
            return new ArrayList<>();
        }
        Vendedor vendedor = vendedorRepository.findById((long)idVendedor);
        if (vendedor == null){
            return new ArrayList<>();
        }
        System.out.println("BIEN");
        return clienteRepository.getGastosClientesByVendedor(idVendedor);

    }

    public List<ProductoCount> getTopProducts(Long idVendedor, Long numProductos) {
        System.out.println("asdasda");
        if (idVendedor == null){
            return new ArrayList<>();
        }
        Vendedor vendedor = vendedorRepository.findById((long)idVendedor);
        if (vendedor == null){
            return new ArrayList<>();
        }
        if (numProductos == null){
            numProductos = 10L;
        }
        System.out.println("BIEN");
        List<ProductoCount> result = pedidoRepository.countTopProductsByVendedor(idVendedor);
        for (ProductoCount pc: result){
            System.out.println(pc.getProductoId() + " " + pc.getTotal());
        }
        return  result.stream().limit(numProductos).toList();
    }
}
