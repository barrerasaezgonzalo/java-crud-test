package cl.gbarrera.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class PagedResponse<T> {
    private List<T> results;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
    private boolean last;
}
