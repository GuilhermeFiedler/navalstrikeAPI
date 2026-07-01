package com.projeto.navalstrikeAPI.common.exception.handler;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ErrorResponse {

    private String message;
    private Integer status;
    LocalDateTime timestamp;
}
