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
        // Admin principar -- PROPIETARIO
        if (userRepository.findByEmail("josepfornes@gmail.com").isEmpty()){
            userService.add("Josep", "Fornes", "1234", "josepfornes@gmail.com", Role.ADMIN);
            System.out.println("Admin creado: josepfornes@gmail.com");
        }
        // Admin tmp -- PROGRAMADOR
        if (userRepository.findByEmail(adminEmail).isEmpty()) {
            userService.add(adminName, "Olarte", adminPassword, adminEmail, Role.ADMIN);
            System.out.println("Admin creado: " + adminEmail);
        }

        // Admin fijo
        if (userRepository.findByEmail("gcholbi@gmail.com").isEmpty()) {
            userService.add("Guillermo", "Cholbi", "1234", "gcholbi@gmail.com", Role.ADMIN);
            System.out.println("Admin creado: gcholbi@gmail.com");
        }

        // User de prueba
        if (userRepository.findByEmail("prueba001@gmail.com").isEmpty()) {
            userService.add("prueba", "001", "1234", "prueba001@gmail.com", Role.USER);
            System.out.println("User creado: prueba001@gmail.com");
        }

        paramService.ensureDefaults();
        //CIF_usados = new ArrayList<>();
        //createCategorias();
        //randomProducts(100);
        //randomVendedores(10);
        //randomClientes(40);
        //randomPedidos(10);
        //randomLineaPeidos(5);
    }









}
