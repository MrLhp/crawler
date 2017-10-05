package com.lhp.crawler.model;

import com.lhp.crawler.enumns.MoviesStatus;
import lombok.Data;

import javax.persistence.*;

@Data
@Entity
public class Movies {
    @Id
    @GeneratedValue
    private Long id;

    @Column
    private String name;

    @Column
    private String code;

    @Column
    @Enumerated(EnumType.STRING)
    private MoviesStatus status;

    @Column
    private String url;

    @Column
    private String remarks;

    @Column
    private Boolean isDownload;


}
