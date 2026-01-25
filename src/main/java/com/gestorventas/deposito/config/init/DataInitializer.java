package com.gestorventas.deposito.config.init;

import com.gestorventas.deposito.enums.Role;
import com.gestorventas.deposito.models.*;
import com.gestorventas.deposito.models.producto.Categoria;
import com.gestorventas.deposito.models.producto.Producto;
import com.gestorventas.deposito.repositories.*;
import com.gestorventas.deposito.services.LineaPedidoService;
import com.gestorventas.deposito.services.ParametrosGlobalesService;
import com.gestorventas.deposito.services.PedidoService;
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
    private final CategoriaRepository categoriaRepository;
    private final PedidoService pedidoService;
    private final LineaPedidoService lineaPedidoService;
    private final ParametrosGlobalesService paramService;

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
                userService.add("prueba", "001", "1234", "prueba001@gmail.com", Role.USER);
            System.out.println("Admin creado: " + adminEmail);
            System.out.println("Admin creado: " + "gcholbi@gmail.com");
        }
        paramService.set(21,10, 4);
        CIF_usados = new ArrayList<>();
        createCategorias();
        randomProducts(100);
        randomVendedores(10);
        randomClientes(40);
        randomPedidos(10);
        randomLineaPeidos(5);
    }

    private void createCategorias() {
        String[] valores = {"PULSERA", "COLLAR", "ANILLO", "CORDAJE"};

        for (String v : valores){
            Categoria categoria = new Categoria();
            categoria.setNombre(v);
            categoriaRepository.save(categoria);
        }
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
        liena.setCantidad(random.nextInt(1,20));
        lineaPedidoService.add(pedido.getCliente().getVendedor().getId(), pedido.getCliente().getId(), pedido.getId(), producto.getId(), liena.getCantidad(), liena.getPrecio(), null);

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
        pedidoService.add(cliente.getId(),cliente.getVendedor().getId());
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
        List<Categoria> categorias = categoriaRepository.findAll();
        for (Categoria categoria : categorias) {
            System.out.println(categoria.getNombre());
        }
        for (int i = 0; i < cantidad; i++) {
            createProduct(faker.commerce().productName(),
                    Math.round(Double.parseDouble(faker.commerce().price(1.0, 200.0)) * 100.0) / 100.0,
                    categorias.get(random.nextInt(categorias.size()))
            );
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

    private void createProduct(String name, double price, Categoria categoriaProducto){
        productoRepository.save(Producto.builder()
                .descripcion(name)
                .precio(price)
                .categoria(categoriaProducto)
                .activo(true)
                .build());

    }
}
