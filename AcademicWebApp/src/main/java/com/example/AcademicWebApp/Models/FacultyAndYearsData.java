package com.example.AcademicWebApp.Models;
import lombok.*;

import javax.persistence.Column;
import javax.persistence.Id;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FacultyAndYearsData {
    String name;
    int noYears;
}
