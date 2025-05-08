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
import com.example.sparkyaisystem.service.LimitService;
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
                        createModel("gpt-4o", "OpenAI", "multimodal",
                                "OpenAI's GPT-4o model with multimodal capabilities"),
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
                RegisterRequest admin1 = new RegisterRequest();
                admin1.setFirstName("John");
                admin1.setLastName("Smith");
                admin1.setEmail("admin1@techinnovate.com");
                admin1.setPassword("password123"); // la contraseña será hasheada en el servicio
                authService.registerCompanyAdmin(admin1,company1);
                RegisterRequest userReq1 = new RegisterRequest();
                userReq1.setFirstName("Alice");
                userReq1.setLastName("Johnson");
                userReq1.setCompanyId(company1.getId());
                userReq1.setEmail("user1@techinnovate.com");
                userReq1.setPassword("password123"); // la contraseña será hasheada en el servicio
                User user1 = authService.registerUser(userReq1);

                // Company 2
                RegisterRequest admin2 = new RegisterRequest();
                admin2.setFirstName("Maria");
                admin2.setLastName("Garcia");
                admin2.setEmail("admin2@datasolutions.com");
                admin2.setPassword("password123"); // la contraseña será hasheada en el servicio
                authService.registerCompanyAdmin(admin2,company2);
                RegisterRequest userReq2 = new RegisterRequest();
                userReq2.setFirstName("David");
                userReq2.setLastName("Wilson");
                userReq2.setEmail("user2@datasolutions.com");
                userReq2.setPassword("password123"); // la contraseña será hasheada en el servicio
                userReq2.setCompanyId(company2.getId());
                User user2 = authService.registerUser(userReq2);

                // Company 3
                RegisterRequest admin3 = new RegisterRequest();
                admin3.setFirstName("Robert");
                admin3.setLastName("Chen");
                admin3.setEmail("admin3@aiventures.com");
                admin3.setPassword("password123"); // la contraseña será hasheada en el servicio
                authService.registerCompanyAdmin(admin3,company3);
                RegisterRequest userReq3 = new RegisterRequest();
                userReq3.setFirstName("Sophia");
                userReq3.setLastName("Lee");
                userReq3.setEmail("user3@aiventures.com");
                userReq3.setPassword("password123"); // la contraseña será hasheada en el servicio
                userReq3.setCompanyId(company3.getId());
                User user3 = authService.registerUser(userReq3);

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
        l.setWindowStartTime(LocalDateTime.now());
        l.setWindowEndTime(LimitService.calculateWindowEndTime(LocalDateTime.now(), windowType));
        l.setCreatedAt(LocalDateTime.now());
        return l;
    }
}