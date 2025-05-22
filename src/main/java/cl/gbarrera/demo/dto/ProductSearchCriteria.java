package cl.gbarrera.demo.dto;

import lombok.Data;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = false)
@Data
public class ProductSearchCriteria {

    private String name;

    @Min(0)
    private Long minPrice;

    @Min(0)
    private Long maxPrice;

    @NotNull
    @Min(0)
    private Integer page;

    @NotNull
    @Min(1)
    private Integer size;

    private String sortBy = "name";

    private String sortDir = "ASC";
}
