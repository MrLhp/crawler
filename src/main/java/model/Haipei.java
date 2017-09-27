package model;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.NotBlank;

import javax.persistence.Column;
import javax.persistence.Entity;


@Entity
public class Haipei {
    @NotBlank
    @Column(length = 512, nullable = false)
    private String name;

    @NotBlank
    @Column(length = 512, nullable = false)
    private String age;
}
