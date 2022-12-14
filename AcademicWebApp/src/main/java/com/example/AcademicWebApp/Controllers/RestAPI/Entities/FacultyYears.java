package com.example.AcademicWebApp.Controllers.RestAPI.Entities;

import lombok.*;

import java.util.List;

@Data
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class FacultyYears {

    private String facultyName;
    private List<Integer> years;
}
