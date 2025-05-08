package com.example.sparkyaisystem.service;

import com.example.sparkyaisystem.exception.LimitExceededException;
import com.example.sparkyaisystem.model.dto.limit.LimitRequest;
import com.example.sparkyaisystem.model.dto.limit.LimitResponse;
import com.example.sparkyaisystem.model.entity.AIModel;
import com.example.sparkyaisystem.model.entity.Company;
import com.example.sparkyaisystem.model.entity.Limit;
import com.example.sparkyaisystem.model.entity.Restriction;
import com.example.sparkyaisystem.model.entity.User;
import com.example.sparkyaisystem.repository.AIModelRepository;
import com.example.sparkyaisystem.repository.CompanyRepository;
import com.example.sparkyaisystem.repository.LimitRepository;
import com.example.sparkyaisystem.repository.RestrictionRepository;
import com.example.sparkyaisystem.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class LimitService {

    private final LimitRepository limitRepository;
    private final UserRepository userRepository;
    private final AIModelRepository aiModelRepository;
    private final CompanyRepository companyRepository;
    private final RestrictionRepository restrictionRepository;

    public LimitService(LimitRepository limitRepository,
                        UserRepository userRepository,
                        AIModelRepository aiModelRepository,
                        CompanyRepository companyRepository,
                        RestrictionRepository restrictionRepository) {
        this.limitRepository = limitRepository;
        this.userRepository = userRepository;
        this.aiModelRepository = aiModelRepository;
        this.companyRepository = companyRepository;
        this.restrictionRepository = restrictionRepository;
    }

    @Transactional
    public LimitResponse createLimit(Long companyId, LimitRequest limitRequest) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new RuntimeException("Company not found"));

        User user = userRepository.findByCompanyAndId(company, limitRequest.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found in this company"));

        AIModel model = aiModelRepository.findById(limitRequest.getModelId())
                .orElseThrow(() -> new RuntimeException("AI Model not found"));

        // Check if company has a restriction for this model
        Restriction restriction = restrictionRepository.findByCompanyAndModel(company, model)
                .orElseThrow(() -> new RuntimeException("Company does not have a restriction for this model"));

        // Check if limit already exists for this user and model
        if (limitRepository.existsByUserAndModel(user, model)) {
            throw new RuntimeException("Limit already exists for this user and model");
        }

        // Validate that user limit does not exceed company restriction
        if (limitRequest.getMaxRequestsPerWindow() > restriction.getMaxRequestsPerWindow()) {
            throw new RuntimeException("User request limit cannot exceed company restriction");
        }
        if (limitRequest.getMaxTokensPerWindow() > restriction.getMaxTokensPerWindow()) {
            throw new RuntimeException("User token limit cannot exceed company restriction");
        }

        // Create limit with appropriate window times
        Limit limit = new Limit();
        limit.setUser(user);
        limit.setModel(model);
        limit.setMaxRequestsPerWindow(limitRequest.getMaxRequestsPerWindow());
        limit.setMaxTokensPerWindow(limitRequest.getMaxTokensPerWindow());
        limit.setWindowType(limitRequest.getWindowType());
        limit.setUsedRequests(0);
        limit.setUsedTokens(0);

        // Set window times based on window type
        LocalDateTime now = LocalDateTime.now();
        limit.setWindowStartTime(now);
        limit.setWindowEndTime(calculateWindowEndTime(now, limitRequest.getWindowType()));

        user.addLimit(limit);

        return mapToLimitResponse(limitRepository.save(limit));
    }

    public List<LimitResponse> getLimitsByUser(Long companyId, Long userId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new RuntimeException("Company not found"));

        User user = userRepository.findByCompanyAndId(company, userId)
                .orElseThrow(() -> new RuntimeException("User not found in this company"));

        return limitRepository.findByUser(user).stream()
                .map(this::mapToLimitResponse)
                .collect(Collectors.toList());
    }

    public LimitResponse getLimitById(Long companyId, Long limitId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new RuntimeException("Company not found"));

        Limit limit = limitRepository.findById(limitId)
                .orElseThrow(() -> new RuntimeException("Limit not found"));

        // Ensure the limit belongs to a user in the company
        if (!limit.getUser().getCompany().getId().equals(company.getId())) {
            throw new RuntimeException("Limit does not belong to a user in this company");
        }

        return mapToLimitResponse(limit);
    }

    @Transactional
    public LimitResponse updateLimit(Long companyId, Long limitId, LimitRequest limitRequest) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new RuntimeException("Company not found"));

        Limit limit = limitRepository.findById(limitId)
                .orElseThrow(() -> new RuntimeException("Limit not found"));

        // Ensure the limit belongs to a user in the company
        if (!limit.getUser().getCompany().getId().equals(company.getId())) {
            throw new RuntimeException("Limit does not belong to a user in this company");
        }

        User user = userRepository.findByCompanyAndId(company, limitRequest.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found in this company"));

        AIModel model = aiModelRepository.findById(limitRequest.getModelId())
                .orElseThrow(() -> new RuntimeException("AI Model not found"));

        // Check if company has a restriction for this model
        Restriction restriction = restrictionRepository.findByCompanyAndModel(company, model)
                .orElseThrow(() -> new RuntimeException("Company does not have a restriction for this model"));

        // If user or model is being changed, check if a limit already exists
        if ((limit.getUser().getId() != user.getId() || limit.getModel().getId() != model.getId()) &&
                limitRepository.existsByUserAndModel(user, model)) {
            throw new RuntimeException("Limit already exists for this user and model");
        }

        // Validate that user limit does not exceed company restriction
        if (limitRequest.getMaxRequestsPerWindow() > restriction.getMaxRequestsPerWindow()) {
            throw new RuntimeException("User request limit cannot exceed company restriction");
        }
        if (limitRequest.getMaxTokensPerWindow() > restriction.getMaxTokensPerWindow()) {
            throw new RuntimeException("User token limit cannot exceed company restriction");
        }

        // Update limit
        limit.setUser(user);
        limit.setModel(model);
        limit.setMaxRequestsPerWindow(limitRequest.getMaxRequestsPerWindow());
        limit.setMaxTokensPerWindow(limitRequest.getMaxTokensPerWindow());

        // Only update window type if it's changed
        if (!limit.getWindowType().equals(limitRequest.getWindowType())) {
            limit.setWindowType(limitRequest.getWindowType());
            // Reset window times
            LocalDateTime now = LocalDateTime.now();
            limit.setWindowStartTime(now);
            limit.setWindowEndTime(calculateWindowEndTime(now, limitRequest.getWindowType()));
            // Reset usage counters
            limit.setUsedRequests(0);
            limit.setUsedTokens(0);
        }

        return mapToLimitResponse(limitRepository.save(limit));
    }

    @Transactional
    public void deleteLimit(Long companyId, Long limitId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new RuntimeException("Company not found"));

        Limit limit = limitRepository.findById(limitId)
                .orElseThrow(() -> new RuntimeException("Limit not found"));

        // Ensure the limit belongs to a user in the company
        if (!limit.getUser().getCompany().getId().equals(company.getId())) {
            throw new RuntimeException("Limit does not belong to a user in this company");
        }

        User user = limit.getUser();
        user.removeLimit(limit);
        limitRepository.delete(limit);
    }

    @Transactional
    public void resetExpiredLimits() {
        LocalDateTime now = LocalDateTime.now();
        List<Limit> expiredLimits = limitRepository.findAll().stream()
                .filter(limit -> limit.getWindowEndTime().isBefore(now))
                .collect(Collectors.toList());

        for (Limit limit : expiredLimits) {
            limit.setUsedRequests(0);
            limit.setUsedTokens(0);
            limit.setWindowStartTime(now);
            limit.setWindowEndTime(calculateWindowEndTime(now, limit.getWindowType()));
            limitRepository.save(limit);
        }
    }

    @Transactional
    public void checkAndUpdateLimit(User user, AIModel model, int tokensToConsume) {
        log.debug("Checking limit for user: {}, model: {}, tokens: {}", user.getEmail(), model.getName(), tokensToConsume);

        Limit limit = limitRepository.findByUserAndModel(user, model)
                .orElseThrow(() -> new RuntimeException("User does not have a limit for this model"));

        // Check if window has expired
        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(limit.getWindowEndTime())) {
            log.debug("Limit window has expired, resetting limit for user: {}, model: {}", user.getEmail(), model.getName());
            // Reset limit
            limit.setUsedRequests(0);
            limit.setUsedTokens(0);
            limit.setWindowStartTime(now);
            limit.setWindowEndTime(calculateWindowEndTime(now, limit.getWindowType()));
        }

        // Check if requests limit would be exceeded
        if (limit.getUsedRequests() + 1 > limit.getMaxRequestsPerWindow()) {
            log.warn("Request limit exceeded for user: {}, model: {}, current: {}, max: {}", 
                    user.getEmail(), model.getName(), limit.getUsedRequests(), limit.getMaxRequestsPerWindow());

            throw new LimitExceededException(
                    "requests", 
                    limit.getUsedRequests(), 
                    limit.getMaxRequestsPerWindow(),
                    limit.getWindowType(),
                    limit.getWindowEndTime()
            );
        }

        // Check if tokens limit would be exceeded
        if (limit.getUsedTokens() + tokensToConsume > limit.getMaxTokensPerWindow()) {
            log.warn("Token limit exceeded for user: {}, model: {}, current: {}, max: {}", 
                    user.getEmail(), model.getName(), limit.getUsedTokens(), limit.getMaxTokensPerWindow());

            throw new LimitExceededException(
                    "tokens", 
                    limit.getUsedTokens(), 
                    limit.getMaxTokensPerWindow(),
                    limit.getWindowType(),
                    limit.getWindowEndTime()
            );
        }

        // Update limit usage
        limit.setUsedRequests(limit.getUsedRequests() + 1);
        limit.setUsedTokens(limit.getUsedTokens() + tokensToConsume);
        limitRepository.save(limit);

        log.debug("Limit updated for user: {}, model: {}, new requests: {}, new tokens: {}", 
                user.getEmail(), model.getName(), limit.getUsedRequests(), limit.getUsedTokens());
    }

    public static LocalDateTime calculateWindowEndTime(LocalDateTime startTime, String windowType) {
        switch (windowType.toLowerCase()) {
            case "daily":
                return startTime.plusDays(1);
            case "weekly":
                return startTime.plusWeeks(1);
            case "monthly":
                return startTime.plusMonths(1);
            default:
                throw new IllegalArgumentException("Invalid window type: " + windowType);
        }
    }

    private LimitResponse mapToLimitResponse(Limit limit) {
        return LimitResponse.builder()
                .id(limit.getId())
                .userId(limit.getUser().getId())
                .userName(limit.getUser().getFirstName() + " " + limit.getUser().getLastName())
                .userEmail(limit.getUser().getEmail())
                .modelId(limit.getModel().getId())
                .modelName(limit.getModel().getName())
                .modelProvider(limit.getModel().getProvider())
                .modelType(limit.getModel().getType())
                .maxRequestsPerWindow(limit.getMaxRequestsPerWindow())
                .maxTokensPerWindow(limit.getMaxTokensPerWindow())
                .usedRequests(limit.getUsedRequests())
                .usedTokens(limit.getUsedTokens())
                .windowType(limit.getWindowType())
                .windowStartTime(limit.getWindowStartTime())
                .windowEndTime(limit.getWindowEndTime())
                .createdAt(limit.getCreatedAt())
                .updatedAt(limit.getUpdatedAt())
                .build();
    }
}
