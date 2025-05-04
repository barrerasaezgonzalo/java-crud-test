package cl.gbarrera.demo.dto;

import lombok.Data;

@Data
public class SearchProductDTO {
  private String name;
  private Double minPrice;
  private Double maxPrice;
}
