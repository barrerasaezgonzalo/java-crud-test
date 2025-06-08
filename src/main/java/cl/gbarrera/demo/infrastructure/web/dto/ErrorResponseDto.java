package cl.gbarrera.demo.infrastructure.web.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponseDto {

    private String error;
    private Object message;
    private int status;
}