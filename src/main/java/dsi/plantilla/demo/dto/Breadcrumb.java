package dsi.plantilla.demo.dto;

import lombok.Data;

@Data
public class Breadcrumb {
    private String name;
    private String url;

    public Breadcrumb() {
    }

    public Breadcrumb(String name, String url) {
        this.name = name;
        this.url = url;
    }
}