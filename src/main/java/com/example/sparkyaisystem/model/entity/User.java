package com.example.sparkyaisystem.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @ManyToOne
    @JoinColumn(name = "company_id")
    private Company company;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Limit> limits = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Request> requests = new ArrayList<>();

    // Helper methods to maintain bidirectional relationships
    public void addLimit(Limit limit) {
        limits.add(limit);
        limit.setUser(this);
    }

    public void removeLimit(Limit limit) {
        limits.remove(limit);
        limit.setUser(null);
    }

    public void addRequest(Request request) {
        requests.add(request);
        request.setUser(this);
    }

    public void removeRequest(Request request) {
        requests.remove(request);
        request.setUser(null);
    }
}