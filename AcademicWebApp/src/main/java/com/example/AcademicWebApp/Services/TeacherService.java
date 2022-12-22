package com.example.AcademicWebApp.Services;

import com.example.AcademicWebApp.Controllers.RestAPI.Entities.Message;
import com.example.AcademicWebApp.Controllers.RestAPI.Entities.StudentGrade;
import com.example.AcademicWebApp.Models.*;
import com.example.AcademicWebApp.Repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service("teacherService")
public class TeacherService {

    @Autowired
    UsersRepo usersRepo;

    @Autowired
    TeacherRepo teacherRepo;

    @Autowired
    CourseRepo coursesRepo;

    @Autowired
    OptionalCourseRepo optionalCourseRepo;

    @Autowired
    FacultyRepo facultiesRepo;

    @Autowired
    UserDataRepo userDataRepo;

    @Autowired
    GradeRepo gradeRepo;

    @Autowired
    GroupRepo groupRepo;

    @Autowired
    StudentRepo studentRepo;

    @Autowired
    StudentEnrollmentRepo studentEnrollmentRepo;

    @Autowired
    UserDataService userDataService;

    public List<Course> getOptionalCoursesOfTeacher(String username){
        return optionalCourseRepo.findOptionalCoursesByUsername(username);
    }

    public Message proposeOptional(String username, Course course){
        // checking if he can add another optional course (ge can update it tho, and that will be okay)
        List<Course> optionalCoursesAlreadyAdded = this.getOptionalCoursesOfTeacher(username);

        int nrOfCoursesDifferent = 0;
        boolean updateCourse = false;

        for(Course c : optionalCoursesAlreadyAdded){
            if(c.getCid() != course.getCid()) {
                nrOfCoursesDifferent = nrOfCoursesDifferent + 1;
            }
            if(c.getCid() == course.getCid()){
                updateCourse = true;
            }
        }

        // if there are more than 1 course that is different than the one sent rn
        // it means that it s neither updating, and it is adding a 3rd optional course, which he isnt allowed.
        if(nrOfCoursesDifferent > 1){
            return new Message("You already added 2 optional courses.");
        }

        if(!updateCourse){
            course.setCid(this.generateCid());
        }
        try{
            coursesRepo.save(course);
            OptionalCourse oc = new OptionalCourse(course.getCid(), username);
            optionalCourseRepo.save(oc);
        }
        catch (Exception e){
            return new Message("Sth bad happened: " + Arrays.toString(e.getStackTrace()));
        }

        return new Message("All g. Optional course added/updated successfully.");
    }

    public List<Course> getCoursesOfTeacher(String username){
        return this.teacherRepo.getAllCoursesForTeacher(username);
    }

    public List<StudentGrade> getStudentsGrades(Course course){
        List<StudentEnrollment> studentEnrollments = this.getAllStudentEnrollmentsEnrolledToCid(course.getCid());
        List<StudentGrade> studentGrades = new ArrayList<>();


        for(StudentEnrollment se : studentEnrollments){
            Grade g = null;
            Optional<Grade> og = gradeRepo.findById(new GradeId(se.getUsername(), course.getCid()));
            if(og.isPresent()){
                g = og.get();
            }

            List<Integer> groups = groupRepo.findAllGidsByFacultyAndYear(course.getFid(), course.getYear());
            Student student = studentRepo.getById(se.getUsername());

            int group = 0;
            for(int i : groups){
                if(student.getGroup1() == i){
                    group = i;
                    break;
                }
                if(student.getGroup2() != null && student.getGroup2() == i){
                    group = i;
                    break;
                }
            }


            if(g != null){
                studentGrades.add(new StudentGrade(se.getUsername(), this.getFullName(se.getUsername()), group, course.getCid(), g.getGradevalue()));
            }
            else{
                studentGrades.add(new StudentGrade(se.getUsername(), this.getFullName(se.getUsername()), group, course.getCid(), -1));
            }
        }

        return studentGrades;
    }

    public Message saveStudentsGrades(List<StudentGrade> studentGrades){

        for(StudentGrade studentGrade : studentGrades){
            Grade g = new Grade();
            g.setCid(studentGrade.getCid());
            g.setUsername(studentGrade.getUsername());
            g.setGradevalue(studentGrade.getGradeValue());
            gradeRepo.save(g);
        }

        return new Message("Every student's grade was updated successfully.");
    }

    private int generateCid(){
        List<Course> courses = coursesRepo.findAll();

        int maxCidExistent = -1;

        for(Course c : courses){
            if(c.getCid() > maxCidExistent){
                maxCidExistent = c.getCid();
            }
        }

        return maxCidExistent + 1;
    }

    private String getFullName(String username){

        UserData userData = userDataRepo.getById(username);

        return userData.getName() + " " + userData.getSurname();
    }

    private List<StudentEnrollment> getAllStudentEnrollmentsEnrolledToCid(int cid){
        Course course = coursesRepo.getById(cid);
        return studentEnrollmentRepo.getAllStudentEnrollmentsForFidAndYear(course.getFid(), course.getYear());
    }

}
