package com.tuan.exam.dto.response;

import lombok.*;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class SubjectResponse {
    private Long id;
    private String code;
    private String name;
    private String description;
}
