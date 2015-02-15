package com.shopper.domain;

import java.io.Serializable;

/**
 * Created by Ihar_Yudziankou on 2/15/2015.
 */
public class Store implements Serializable {
    private Long id;
    private String name;
    private String description;

    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }
}
