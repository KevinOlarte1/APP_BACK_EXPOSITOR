package com.gestorventas.deposito.controllers;

import com.gestorventas.deposito.dto.in.VendedorDto;
import com.gestorventas.deposito.dto.out.VendedorResponseDto;
import com.gestorventas.deposito.enums.CategoriaProducto;
import com.gestorventas.deposito.enums.Role;
import com.gestorventas.deposito.interfaces.CategoriaCount;
import com.gestorventas.deposito.interfaces.GastosCliente;
import com.gestorventas.deposito.interfaces.ProductoCount;
import com.gestorventas.deposito.models.Vendedor;
import com.gestorventas.deposito.repositories.VendedorRepository;
import com.gestorventas.deposito.services.ProductoService;
import com.gestorventas.deposito.services.VendedorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controlador REST encargado de exponer los endpoints relacionados con los Vendedores.
 * <p>
 * Permite crear, consultar, actualizar y eliminar vendedores en el sistema.
 * </p>
 *
 * @author Kevin William Olarte Braun
 */
@RestController
@RequestMapping("/api/vendedor")
@AllArgsConstructor
public class VendedorController {

    private final VendedorService vendedorService;
    private final VendedorRepository vendedorRepository;
    private final ProductoService productoService;

    /**
     * Crear un nuevo vendedor.
     *
     * @param dto datos de entrada para crear el vendedor
     * @return DTO con los datos del vendedor creado
     */
    @PostMapping
    @Operation(summary = "Crear un nuevo vendedor", description = "Crea un nuevo vendedor en el sistema")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Vendedor creado correctamente"),
            @ApiResponse(responseCode = "500", description = "Error interno", content = @Content)
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<VendedorResponseDto> add(@RequestBody VendedorDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(vendedorService.add(dto.getNombre(), dto.getApellido(), dto.getPassword(), dto.getEmail(), Role.USER));
    }

    /**
     * Obtener un vendedor por su ID.
     *
     * @param id identificador del vendedor
     * @return DTO con los datos del vendedor
     */
    @GetMapping("/{id}")
    @Operation(summary = "Obtener un vendedor por su ID", description = "Obtener un vendedor por su ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Vendedor encontrado"),
            @ApiResponse(responseCode = "404", description = "Vendedor no encontrado", content = @Content)
    })
    public ResponseEntity<VendedorResponseDto> get(@PathVariable long id) {
        VendedorResponseDto vendedor = vendedorService.get(id);
        if (vendedor == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(vendedor);
    }

    /**
     * Listar todos los vendedores.
     *
     * @return lista de DTOs con todos los vendedores
     */
    @GetMapping
    @Operation(summary = "Listar todos los vendedores", description = "Listar todos los vendedores")
    @ApiResponse(responseCode = "200", description = "Lista de vendedores encontrados")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<VendedorResponseDto>> getAll() {
        return ResponseEntity.ok(vendedorService.getAll());
    }

    /**
     * Actualizar los datos de un vendedor existente.
     *
     * @param id  identificador del vendedor
     * @param dto datos actualizados
     * @return DTO con los datos del vendedor actualizado
     */
    @Operation(summary = "Actualizar los datos de un vendedor", description = "Actualiza los datos de un vendedor")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Vendedor actualizado"),
            @ApiResponse(responseCode = "404", description = "Vendedor no encontrado/ datos incorrectos a actualizar", content = @Content)
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<VendedorResponseDto> update(
            @PathVariable long id,
            @RequestBody VendedorDto dto
    ) {
        VendedorResponseDto vendedor =  vendedorService.update(id, dto.getNombre(), dto.getApellido(), dto.getPassword(), dto.getEmail());
        if (vendedor == null)
            return ResponseEntity.notFound().build();
        return ResponseEntity.ok(vendedor);
    }

    /**
     * Eliminar un vendedor por su ID.
     *
     * @param id identificador del vendedor
     * @return código 204 si se eliminó correctamente
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar un vendedor por su ID", description = "Elimina un vendedor por su ID")
    @ApiResponse(responseCode = "204", description = "Vendedor eliminado correctamente")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable long id) {
        vendedorService.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Metod par ver la infromacion del usuario actual
     * @param auth credenciales del usuario actual
     * @return un dto con la informacion del usuario actual
     */
    @GetMapping("/me")
    @Operation(summary = "Obtener info del  Vendedor", description = "Informa sobre el vendedor usado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Vendedor encontrado"),
            @ApiResponse(responseCode = "404", description = "Vendedor no encontrado/ datos incorrectos a actualizar", content = @Content)
    })
    public ResponseEntity<VendedorResponseDto> me(Authentication auth) {
        var email = auth.getName();
        Vendedor u = vendedorRepository.findByEmail(email).orElseThrow();
        return ResponseEntity.ok(
                new VendedorResponseDto(u)
        );
    }

    /**
     * Metodo para obtener las estadisticas globales de la empresa
     * @return map con los datos de estadisticas
     */
    @GetMapping("/stats/ingresoAnual")
    @Operation(summary = "Obtener las estadisticas globales", description = "ingresos por año de toda la empresa")
    @ApiResponse(responseCode = "200", description = "Mapeo por año de los ingresos")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Double>> getStats() {
        return ResponseEntity.ok( vendedorService.getStats());
    }

    /**
     * Metodo para obtener las estadisticas de un vendedor especifico
     * @param idVendedor identificador del vendedor
     * @return map con los datos de estadisticas
     */
    @GetMapping("/{idVendedor}/stats/ingresoAnual")
    @Operation(summary = "Obtener las estadisticas de un vendedor", description = "ingresos por año de un vendededor especifico")
    @ApiResponse(responseCode = "200", description = "Mapeo por año de los ingresos de ese vendedor.")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Double>> getStatsByVendedor(Long idVendedor) {
        return ResponseEntity.ok( vendedorService.getStats(idVendedor));
    }

    /**
     * Metodo para obtener las estadisticas de un vendedor especifico
     * @param auth credenciales del usuario actual
     * @return map con los datos de estadisticas
     */
    @GetMapping("/stats/numProductsCategoria")
    @Operation(summary = "Obtenen un Map con la cant de product x categoria.", description = "map con num de productos de vendedor, vendidos por categoria")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Map calculado"),
            @ApiResponse(responseCode = "404", description = "Vendedor no encontrado")

    })
    public ResponseEntity<List<CategoriaCount>> getStatsProductsByCategory(
            Authentication auth) {
        var email = auth.getName();
        Vendedor u = vendedorRepository.findByEmail(email).orElseThrow();
        Long idVendedor = u.getId();
        List<CategoriaCount> categorianum = productoService.getNumProductosCategoriaByVendedor(idVendedor);
        return categorianum == null? ResponseEntity.notFound().build() : ResponseEntity.ok(categorianum);

    }

    /**
     * Metodo para obtener las estadisticas de un vendedor especifico
     * @param auth credenciales del usuario actual
     * @return map con los datos de estadisticas
     */
    @GetMapping("/{idVendedor}/stats/numProductsCategoria")
    @Operation(summary = "Obtenen un Map con la cant de product x categoria.", description = "map con num de productos de vendedor, vendidos por categoria")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Map calculado"),
            @ApiResponse(responseCode = "404", description = "Vendedor no encontrado")

    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<CategoriaCount>> getStatsProductsByCategory(
            Authentication auth,
            @PathVariable Long idVendedor) {
        List<CategoriaCount> categorianum = productoService.getNumProductosCategoriaByVendedor(idVendedor);
        return categorianum == null? ResponseEntity.notFound().build() : ResponseEntity.ok(categorianum);

    }
    /**
     * Metodo para obtener las estadisticas de un vendedor especifico
     * @param auth credenciales del usuario actual
     * @return map con los datos de estadisticas
     */
    @GetMapping("/me/stats")
    @Operation(summary = "Obtener las estadisticas del usuario actual", description = "ingresos por año de las ventas del vendedor en uso")
    @ApiResponse(responseCode = "200", description = "Mapeo por año de los ingresos")
    public ResponseEntity<Map<String, Double>> getStats(Authentication auth) {
        var email = auth.getName();
        Vendedor u = vendedorRepository.findByEmail(email).orElseThrow();
        Long idVendedor = u.getId();
        return ResponseEntity.ok( vendedorService.getStats(idVendedor));
    }

    /**
     * Obtener un map Conm el numero de pedidos generados por cada vendedor.
     * @param auth credenciales del usuario actual
     * @return map con los datos de estadisticas
     */
    @GetMapping(value = "/stats/numPedido")
    @Operation(summary = "Obtener un map con el numero de pedidos generados por un vendedor", description = "map con num de pedidos generados por un vendedor")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Map calculado"),
            @ApiResponse(responseCode = "404", description = "Vendedor no encontrado")
    })
    public ResponseEntity<Map<String, Long>> getNumPedidoGeneral(
            Authentication auth
    ){
        var email = auth.getName();
        Long idVendedor = vendedorRepository.findByEmail(email).orElseThrow().getId();
        return ResponseEntity.ok(vendedorService.getNumPedidos(idVendedor));
    }

    /**
     * Obtener un map Conm el numero de pedidos generados por cada vendedor.
     * @param auth credenciales del usuario actual
     * @param idVendedor identificador del vendedor
     * @return map con los datos de estadisticas
     */
    @GetMapping(value = "/{idVendedor}/stats/numPedido")
    @Operation(summary = "Obtener un map con el numero de pedidos generados por un vendedor", description = "map con num de pedidos generados por un vendedor")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Map calculado"),
            @ApiResponse(responseCode = "404", description = "Vendedor no encontrado")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Long>> getNumPedidoPorCliente(
            Authentication auth,
            @PathVariable Long idVendedor
    ){
        return ResponseEntity.ok(vendedorService.getNumPedidos(idVendedor));
    }

    /**
     * Obtener un map con los gastos por cada cliente.
     * @param idVendedor identificador del vendedor
     * @return map con los datos de estadisticas
     */
    @GetMapping("/{idVendedor}/stats/gastoPorCliente")
    @Operation(summary = "Obtener un map con los gastos por cada cliente", description = "map con los gastos por cada cliente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Map calculado"),
            @ApiResponse(responseCode = "404", description = "Vendedor no encontrado")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<GastosCliente>> getGastoPorCliente(
            @PathVariable Long idVendedor
    ){
        return ResponseEntity.ok(vendedorService.getGastosClientes(idVendedor));
    }

    /**
     * Obtener un map con los gastos por cada cliente.
     * @param auth credenciales del usuario actual
     * @return map con los datos de estadisticas
     */
    @GetMapping("/stats/gastoPorCliente")
    @Operation(summary = "Obtener un map con los gastos por cada cliente", description = "map con los gastos por cada cliente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Map calculado"),
            @ApiResponse(responseCode = "404", description = "Vendedor no encontrado")
    })
    public ResponseEntity<List<GastosCliente>> getGastoPorCliente(
            Authentication auth

    ){
        var email = auth.getName();
        Long idVendedor = vendedorRepository.findByEmail(email).orElseThrow().getId();
        return ResponseEntity.ok(vendedorService.getGastosClientes(idVendedor));
    }

    /**
     * lista con los prodctos mas vendidos mas su cantidad .
     * @param numProductos num de prodicutos como limite
     * @param auth credenciales del usuario actual
     * @return lista de productos mas vendidos.
     */
    @GetMapping(value = "/stats/topProductos")
    @Operation(summary = "Obtener los top productos", description = "lista con los productos mas vendidos mas su cantidad")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de productos mas vendidos"),
            @ApiResponse(responseCode = "404", description = "Vendedor no encontrado")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ProductoCount>> topProductos(
            @RequestParam(required = false) Long numProductos,
            Authentication auth
    ) {
        var email = auth.getName();
        Long idVendedor = vendedorRepository.findByEmail(email).orElseThrow().getId();
        List<ProductoCount> list =  vendedorService.getTopProducts(idVendedor, numProductos);

        return ResponseEntity.ok(list);
    }

    /**
     * lista con los prodctos mas vendidos mas su cantidad .
     * @param numProductos num de prodicutos como limite
     * @param idVendedor ID del vendedor a busacar
     * @return lista de productos mas vendidos.
     */
    @GetMapping(value = "/{idVendedor}/stats/topProductos")
    @Operation(summary = "Obtener los top productos", description = "lista con los productos mas vendidos mas su cantidad")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de productos mas vendidos"),
            @ApiResponse(responseCode = "404", description = "Vendedor no encontrado")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ProductoCount>> topProductos(
            @PathVariable Long idVendedor,
            @RequestParam(required = false) Long numProductos
    ) {
        List<ProductoCount> list =  vendedorService.getTopProducts(idVendedor, numProductos);

        return ResponseEntity.ok(list);
    }






}

