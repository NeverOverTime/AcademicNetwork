import { HttpClient } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import {FormGroup, FormBuilder} from '@angular/forms'
import { Router } from '@angular/router';
import {CookieService} from "ngx-cookie-service";
import {ApisService} from "../apis/apis.service";


@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})

export class LoginComponent implements OnInit {

  logoWidth = 325;

  public loginForm !: FormGroup;
  constructor(private formBuilder : FormBuilder, private http : HttpClient, private router : Router,
              private cookieService:CookieService, private apisService: ApisService) { }

  ngOnInit(): void {
    this.loginForm = this.formBuilder.group ({
      username: [''],
      password: ['']
    });

  }

  async login() {
    const typeUser = ["student", "teacher", "staff", "chief"];
    var found = false;
    let newUser;

    let role: string;
    this.http.get<IUser>(`http://localhost:8080/user/${this.loginForm.value.username}?password=` +  this.encrypt(this.loginForm.value.password).replace("+", " "))
      .subscribe((v:IUser) => {
        this.apisService.getAllFaculties().subscribe((faculties) => {

          if(v.role == 'student'){
            role = 'student';
            this.cookieService.set("role", v.role);
          }
          else if(v.role != "teacher"){
            role = 'staff';
            this.cookieService.set("role", v.role);
          }
          else if(v.role == 'teacher'){
            role = 'teacher';
            this.cookieService.set("role", v.role);
          }


          for(let fac of faculties){
            if(fac.chief == this.loginForm.value.username){
              this.cookieService.set("role", "chief");
              break;
            }
          }
          if(v.role == 'null'){
            alert("Login failed!")
          }
          else{
            this.cookieService.set("username", this.loginForm.value.username);
            this.loginForm.reset();
            this.router.navigate([`${role}-dashboard`]);
          }

          if (v.role == "null") {
            alert("Login failed");
          }
          else {
            this.loginForm.reset();
          }
        })

      })

  }

  encrypt(word: string): string{
    const keyBase64 = "o9szYIOq1rRMiouNhNvaq96lqUvCekxR";
    var key = CryptoJS.enc.Base64.parse(keyBase64);
    var srcs = CryptoJS.enc.Utf8.parse(word);
    var encrypted = CryptoJS.AES.encrypt(srcs, key, {mode:CryptoJS.mode.ECB,padding: CryptoJS.pad.Pkcs7});
    return encrypted.toString();
  }
}

export default interface IUser {
  role: string,
  message: string
}

export async function http(request: RequestInfo): Promise<any> {
  const response = await fetch(request);
  const body = await response.json();
  return body;
}
