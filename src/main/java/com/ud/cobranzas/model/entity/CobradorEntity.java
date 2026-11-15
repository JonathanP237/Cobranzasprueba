package com.ud.cobranzas.model.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "cobradores")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CobradorEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "nombre_cobrador", nullable = false, length = 150)
    private String nombreCobrador;

    @Column(name = "telefono_cobrador", length = 30)
    private String telefonoCobrador;

    @Column(name = "username", nullable = false, unique = true, length = 50)
    private String username;

    @Column(name = "password", nullable = false, length = 255)
    private String password;

    @Column(name = "activo", nullable = false)
    private Boolean activo = true;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;
}