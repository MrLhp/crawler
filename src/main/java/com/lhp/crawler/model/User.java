package com.lhp.crawler.model;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.NotBlank;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Data
@Entity
public class User {
    @Id
    @GeneratedValue
    private Long id;

    @NotBlank
    @Column(length = 512, nullable = false)
    private String name;

    @NotBlank
    @Column(length = 512, nullable = false)
    private String age;
}
