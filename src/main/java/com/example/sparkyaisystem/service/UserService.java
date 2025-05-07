package com.example.sparkyaisystem.service;

import com.example.sparkyaisystem.model.dto.auth.RegisterRequest;
import com.example.sparkyaisystem.model.dto.user.UserConsumptionResponse;
import com.example.sparkyaisystem.model.dto.user.UserRequest;
import com.example.sparkyaisystem.model.dto.user.UserResponse;
import com.example.sparkyaisystem.model.entity.AIModel;
import com.example.sparkyaisystem.model.entity.Company;
import com.example.sparkyaisystem.model.entity.Limit;
import com.example.sparkyaisystem.model.entity.Request;
import com.example.sparkyaisystem.model.entity.User;
import com.example.sparkyaisystem.repository.CompanyRepository;
import com.example.sparkyaisystem.repository.LimitRepository;
import com.example.sparkyaisystem.repository.RequestRepository;
import com.example.sparkyaisystem.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final RequestRepository requestRepository;
    private final LimitRepository limitRepository;
    private final AuthService authService;

    public UserService(UserRepository userRepository, CompanyRepository companyRepository,
                       RequestRepository requestRepository, LimitRepository limitRepository,
                       AuthService authService) {
        this.userRepository = userRepository;
        this.companyRepository = companyRepository;
        this.requestRepository = requestRepository;
        this.limitRepository = limitRepository;
        this.authService = authService;
    }

    @Transactional
    public UserResponse createUser(UserRequest userRequest, Long companyId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new RuntimeException("Company not found"));

        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setFirstName(userRequest.getFirstName());
        registerRequest.setLastName(userRequest.getLastName());
        registerRequest.setEmail(userRequest.getEmail());
        registerRequest.setPassword(userRequest.getPassword());
        registerRequest.setRole(userRequest.getRole());
        registerRequest.setCompanyId(companyId);

        User user = authService.registerUser(registerRequest);
        return mapToUserResponse(user);
    }

    public List<UserResponse> getAllUsersByCompany(Long companyId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new RuntimeException("Company not found"));

        return userRepository.findByCompany(company).stream()
                .map(this::mapToUserResponse)
                .collect(Collectors.toList());
    }

    public UserResponse getUserById(Long companyId, Long userId) {
        User user = userRepository.findByCompanyAndId(
                companyRepository.findById(companyId)
                        .orElseThrow(() -> new RuntimeException("Company not found")),
                userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return mapToUserResponse(user);
    }

    @Transactional
    public UserResponse updateUser(Long companyId, Long userId, UserRequest userRequest) {
        User user = userRepository.findByCompanyAndId(
                companyRepository.findById(companyId)
                        .orElseThrow(() -> new RuntimeException("Company not found")),
                userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Check if email is being changed and if it's already taken
        if (!user.getEmail().equals(userRequest.getEmail()) &&
                userRepository.existsByEmail(userRequest.getEmail())) {
            throw new RuntimeException("Email is already in use");
        }

        user.setFirstName(userRequest.getFirstName());
        user.setLastName(userRequest.getLastName());
        user.setEmail(userRequest.getEmail());
        // Only update password if it's provided
        if (userRequest.getPassword() != null && !userRequest.getPassword().isEmpty()) {
            // Password should be encoded in a real application
            user.setPassword(userRequest.getPassword());
        }

        return mapToUserResponse(userRepository.save(user));
    }

    public UserConsumptionResponse getUserConsumption(Long companyId, Long userId) {
        User user = userRepository.findByCompanyAndId(
                companyRepository.findById(companyId)
                        .orElseThrow(() -> new RuntimeException("Company not found")),
                userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Request> requests = requestRepository.findByUser(user);
        List<Limit> limits = limitRepository.findByUser(user);

        int totalRequests = requests.size();
        int totalTokensConsumed = 0;

        Map<AIModel, ModelConsumptionData> modelConsumptionMap = new HashMap<>();

        // Initialize with limits
        for (Limit limit : limits) {
            AIModel model = limit.getModel();
            ModelConsumptionData data = new ModelConsumptionData(model);
            data.maxRequestsAllowed = limit.getMaxRequestsPerWindow();
            data.maxTokensAllowed = limit.getMaxTokensPerWindow();
            modelConsumptionMap.put(model, data);
        }

        // Add request data
        for (Request request : requests) {
            totalTokensConsumed += request.getTokensConsumed();

            AIModel model = request.getModel();
            ModelConsumptionData consumptionData = modelConsumptionMap.getOrDefault(model, new ModelConsumptionData(model));
            consumptionData.requestsCount++;
            consumptionData.tokensConsumed += request.getTokensConsumed();
            modelConsumptionMap.put(model, consumptionData);
        }

        List<UserConsumptionResponse.ModelConsumption> modelConsumptions = modelConsumptionMap.values().stream()
                .map(data -> UserConsumptionResponse.ModelConsumption.builder()
                        .modelId(data.model.getId())
                        .modelName(data.model.getName())
                        .modelProvider(data.model.getProvider())
                        .requestsCount(data.requestsCount)
                        .tokensConsumed(data.tokensConsumed)
                        .maxRequestsAllowed(data.maxRequestsAllowed)
                        .maxTokensAllowed(data.maxTokensAllowed)
                        .build())
                .collect(Collectors.toList());

        return UserConsumptionResponse.builder()
                .userId(user.getId())
                .userName(user.getFirstName() + " " + user.getLastName())
                .email(user.getEmail())
                .companyId(user.getCompany().getId())
                .companyName(user.getCompany().getName())
                .totalRequests(totalRequests)
                .totalTokensConsumed(totalTokensConsumed)
                .modelConsumptions(modelConsumptions)
                .build();
    }

    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .role(user.getRole())
                .companyId(user.getCompany() != null ? user.getCompany().getId() : null)
                .companyName(user.getCompany() != null ? user.getCompany().getName() : null)
                .limitsCount(user.getLimits().size())
                .requestsCount(user.getRequests().size())
                .build();
    }

    // Helper class for aggregating model consumption data
    private static class ModelConsumptionData {
        private final AIModel model;
        private int requestsCount;
        private int tokensConsumed;
        private int maxRequestsAllowed;
        private int maxTokensAllowed;

        public ModelConsumptionData(AIModel model) {
            this.model = model;
            this.requestsCount = 0;
            this.tokensConsumed = 0;
            this.maxRequestsAllowed = 0;
            this.maxTokensAllowed = 0;
        }
    }
}