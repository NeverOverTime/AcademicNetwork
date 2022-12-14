package com.example.AcademicWebApp.Controllers.RestAPI.Entities;

import lombok.*;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class StaffFilter {
    private String faculty;
    private String year;
    private String group;
}
