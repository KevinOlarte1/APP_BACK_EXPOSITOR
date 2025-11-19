package com.gestorventas.deposito.config.init;

import com.gestorventas.deposito.enums.CategoriaProducto;
import com.gestorventas.deposito.enums.Role;
import com.gestorventas.deposito.models.*;
import com.gestorventas.deposito.repositories.*;
import com.gestorventas.deposito.services.VendedorService;
import lombok.RequiredArgsConstructor;
import net.datafaker.Faker;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final VendedorRepository userRepository;
    private final VendedorService userService;
    private final ProductoRepository productoRepository;
    private final VendedorRepository vendedorRepository;
    private static final Faker faker = new Faker();
    private static final Random random = new Random();
    private final PasswordEncoder passwordEncoder;
    private final ClienteRepository clienteRepository;
    private final PedidoRepository pedidoRepository;
    private final LineaPedidoRepository lineaPedidoRepository;

    @Value("${app.admin.email}")
    private String adminEmail;
    @Value("${app.admin.name}")
    private String adminName;
    @Value("${app.admin.password}")
    private String adminPassword;
    private List<String> CIF_usados;


    @Override
    public void run(String... args) throws Exception {
        if (userRepository.findByEmail(adminEmail).isEmpty()) {
            userService.add(adminName, "Olarte", adminPassword , adminEmail, Role.ADMIN);
            userService.add("Guillermo", "Cholbi", "1234" , "gcholbi@gmail.com", Role.ADMIN);
            System.out.println("Admin creado: " + adminEmail);
            System.out.println("Admin creado: " + "gcholbi@gmail.com");
        }
        CIF_usados = new ArrayList<>();
        randomProducts(100);
        randomVendedores(10);
        randomClientes(2);
        //randomPedidos(2);
        //randomLineaPeidos(5);
    }

    private void randomLineaPeidos(int cantidad) {
        pedidoRepository.findAll().forEach( pedido -> {
            for (int i = 0; i < cantidad; i++) {
                crearLineaPedido(pedido);
            }
        });
    }

    private void crearLineaPedido(Pedido pedido) {
        LineaPedido liena  = new LineaPedido();
        liena.setPedido(pedido);

        Producto producto = productoRepository.findById(random.nextInt(1,90));
        liena.setProducto(producto);
        liena.setPrecio(producto.getPrecio());
        liena.setCantidad(random.nextInt(20));
        lineaPedidoRepository.save(liena);

    }

    private void randomPedidos(int cantidad) {
        clienteRepository.findAll().forEach( cliente -> {
            for (int i = 0; i < cantidad; i++) {
                createPedido(cliente);
            }
        });
    }

    private void createPedido(Cliente cliente) {
        Pedido pedido = new Pedido();
        pedido.setCliente(cliente);
        pedido = pedidoRepository.save(pedido);
    }

    private void randomClientes(int cantidad) {
        vendedorRepository.findAll().forEach(vendedor -> {
            for (int i = 0; i < cantidad; i++){
                String CIF = faker.numerify("##########");
                while (CIF_usados.contains(CIF)){
                    CIF = faker.numerify("##########");
                }
                CIF_usados.add(CIF);
                createCliente(
                        faker.name().firstName().toLowerCase(Locale.ROOT),
                        CIF,
                        vendedor
                );
            }
        });
    }



    private void randomProducts(int cantidad){
        for (int i = 0; i < cantidad; i++) {
            createProduct(faker.commerce().productName(),
                    Math.round(Double.parseDouble(faker.commerce().price(1.0, 200.0)) * 100.0) / 100.0,
                          CategoriaProducto.values()[random.nextInt(CategoriaProducto.values().length)]);
        }
    }
    private void randomVendedores(int cantidad){
        for (int i = 0; i < cantidad; i++) {
            createVendedor(
                    faker.name().firstName().toLowerCase(Locale.ROOT),
                    faker.name().lastName().toLowerCase(Locale.ROOT),
                    passwordEncoder.encode("1234"),
                    "vendedor" + i + "@gmail.com",
                    Role.USER
            );
        }
    }

    private void createCliente(String nombre, String CIF, Vendedor Vendedor) {
        Cliente cliente = new Cliente(nombre, CIF);
        cliente.setVendedor(Vendedor);
        clienteRepository.save(cliente);
    }
    private void createVendedor(String name,String apellido, String password,String email,Role role){
        vendedorRepository.save(Vendedor.builder()
                .nombre(name)
                .apellido(apellido)
                .password(password)
                .email(email)
                .roles(Set.of(role))
                .build());
    }

    private void createProduct(String name, double price, CategoriaProducto categoriaProducto){
        productoRepository.save(Producto.builder()
                .descripcion(name)
                .precio(price)
                .categoria(categoriaProducto)
                .build());
    }
}
