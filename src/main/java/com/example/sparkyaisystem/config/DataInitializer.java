package com.example.sparkyaisystem.config;

import com.example.sparkyaisystem.model.dto.auth.RegisterRequest;
import com.example.sparkyaisystem.model.entity.AIModel;
import com.example.sparkyaisystem.model.entity.Company;
import com.example.sparkyaisystem.model.entity.Limit;
import com.example.sparkyaisystem.model.entity.Restriction;
import com.example.sparkyaisystem.model.entity.User;
import com.example.sparkyaisystem.model.entity.Role;
import com.example.sparkyaisystem.repository.AIModelRepository;
import com.example.sparkyaisystem.repository.CompanyRepository;
import com.example.sparkyaisystem.repository.LimitRepository;
import com.example.sparkyaisystem.repository.RestrictionRepository;
import com.example.sparkyaisystem.repository.UserRepository;
import com.example.sparkyaisystem.service.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Configuration
@Slf4j
public class DataInitializer {

    @Bean
    @Profile("!test") // No correr en tests
    public CommandLineRunner initDatabase(
            AIModelRepository aiModelRepository,
            UserRepository userRepository,
            AuthService authService,
            CompanyRepository companyRepository,
            RestrictionRepository restrictionRepository,
            LimitRepository limitRepository
    ) {
        return args -> {
            // 1) Modelos AI
            List<AIModel> models;
            if (aiModelRepository.count() == 0) {
                log.info("Inicializando modelos AI...");
                models = Arrays.asList(
                        createModel("o4-mini", "OpenAI", "multimodal",
                                "OpenAI's efficient multimodal model capaz de procesar texto e imágenes"),
                        createModel("DeepSeek-V3-0324", "DeepSeek", "chat",
                                "DeepSeek's advanced language model optimizado para razonamiento"),
                        createModel("Llama-4-Scout-17B-16E-Instruct", "Meta", "chat",
                                "Modelo instructivo de Meta con 17B parámetros y 16 expertos")
                );
                aiModelRepository.saveAll(models);
                log.info("Modelos AI inicializados correctamente");
            } else {
                models = aiModelRepository.findAll();
            }

            // 2) Usuario admin por defecto
            if (userRepository.count() == 0) {
                log.info("Creando usuario admin por defecto...");
                RegisterRequest adminReq = new RegisterRequest();
                adminReq.setFirstName("Admin");
                adminReq.setLastName("User");
                adminReq.setEmail("admin@sparky.com");
                adminReq.setPassword("admin123");
                try {
                    authService.registerSparkyAdmin(adminReq);
                    log.info("Admin creado exitosamente");
                } catch (Exception e) {
                    log.error("Error al crear admin: {}", e.getMessage());
                }

                // 3) Compañías + usuarios
                Company company1 = createCompany("TechInnovate", "20123456789");
                Company company2 = createCompany("DataSolutions", "20987654321");
                Company company3 = createCompany("AIVentures", "20456789123");
                companyRepository.saveAll(Arrays.asList(company1, company2, company3));

                // Company 1
                User admin1 = createUser("John", "Smith", "admin1@techinnovate.com",
                        "password123", Role.ROLE_COMPANY_ADMIN, company1);
                User user1  = createUser("Alice", "Johnson", "user1@techinnovate.com",
                        "password123", Role.ROLE_USER, company1);
                userRepository.saveAll(Arrays.asList(admin1, user1));
                company1.setAdmin(admin1);
                company1.addUser(user1);

                // Company 2
                User admin2 = createUser("Maria", "Garcia", "admin2@datasolutions.com",
                        "password123", Role.ROLE_COMPANY_ADMIN, company2);
                User user2  = createUser("David", "Wilson", "user2@datasolutions.com",
                        "password123", Role.ROLE_USER, company2);
                userRepository.saveAll(Arrays.asList(admin2, user2));
                company2.setAdmin(admin2);
                company2.addUser(user2);

                // Company 3
                User admin3 = createUser("Robert", "Chen", "admin3@aiventures.com",
                        "password123", Role.ROLE_COMPANY_ADMIN, company3);
                User user3  = createUser("Sophia", "Lee", "user3@aiventures.com",
                        "password123", Role.ROLE_USER, company3);
                userRepository.saveAll(Arrays.asList(admin3, user3));
                company3.setAdmin(admin3);
                company3.addUser(user3);

                companyRepository.saveAll(Arrays.asList(company1, company2, company3));
                log.info("Compañías y usuarios creados");

                // 4) Restricciones por compañía
                Restriction r1 = createRestriction(company1, models.get(0), 1_000, 50_000, "daily");
                Restriction r2 = createRestriction(company2, models.get(1), 2_000,100_000, "weekly");
                Restriction r3 = createRestriction(company3, models.get(2), 5_000,200_000, "monthly");
                restrictionRepository.saveAll(Arrays.asList(r1, r2, r3));
                log.info("Restricciones creadas");

                // 5) Límites por usuario
                Limit l1 = createLimit(user1, models.get(0), 200, 10_000, "daily");
                Limit l2 = createLimit(user2, models.get(1), 500, 25_000, "weekly");
                Limit l3 = createLimit(user3, models.get(2),1_000, 50_000, "monthly");
                limitRepository.saveAll(Arrays.asList(l1, l2, l3));
                log.info("Límites creados");
            }
        };
    }

    // ---------- Helpers a nivel de clase ----------

    private AIModel createModel(String name, String provider, String type, String description) {
        AIModel m = new AIModel();
        m.setName(name);
        m.setProvider(provider);
        m.setType(type);
        m.setDescription(description);
        m.setActive(true);
        return m;
    }

    private Company createCompany(String name, String ruc) {
        Company c = new Company();
        c.setName(name);
        c.setRuc(ruc);
        c.setAffiliationDate(LocalDateTime.now());
        c.setCreatedAt(LocalDateTime.now());
        c.setActive(true);
        return c;
    }

    private User createUser(String firstName, String lastName, String email,
                            String password, Role role, Company company) {
        User u = new User();
        u.setFirstName(firstName);
        u.setLastName(lastName);
        u.setEmail(email);
        u.setPassword(password); // recuerda hashear en producción
        u.setRole(role);
        u.setCompany(company);
        return u;
    }

    private Restriction createRestriction(Company company, AIModel model,
                                          int maxReq, int maxTok, String windowType) {
        Restriction r = new Restriction();
        r.setCompany(company);
        r.setModel(model);
        r.setMaxRequestsPerWindow(maxReq);
        r.setMaxTokensPerWindow(maxTok);
        r.setWindowType(windowType);
        r.setCreatedAt(LocalDateTime.now());
        return r;
    }

    private Limit createLimit(User user, AIModel model,
                              int maxReq, int maxTok, String windowType) {
        Limit l = new Limit();
        l.setUser(user);
        l.setModel(model);
        l.setMaxRequestsPerWindow(maxReq);
        l.setMaxTokensPerWindow(maxTok);
        l.setWindowType(windowType);
        l.setCreatedAt(LocalDateTime.now());
        return l;
    }
}