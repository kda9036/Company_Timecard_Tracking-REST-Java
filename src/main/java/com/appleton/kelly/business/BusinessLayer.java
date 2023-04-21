package com.appleton.kelly.business;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.io.StringReader;
import java.sql.Date;
import java.time.DayOfWeek;
import java.util.*;
import java.time.Instant;
import java.time.ZoneId;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.time.Duration;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import com.google.gson.Gson;
import jakarta.json.*;

import companydata.*;

public class BusinessLayer {

    public String createDepartmentJson(Department dept) {
        // Example department object:
        // {
        // "dept_id":1,
        // "company":"rituserid",
        // "dept_name":"accounting",
        // "dept_no":"d10",
        // "location":"new york"
        // }

        Gson gson = new Gson();
        String json = gson.toJson(dept);

        return json;

    }// createDepartmentJson

    public String createEmployeeJson(Employee emp) {
        // Example employee object:
        // {
        // "emp_id":5,
        // "emp_name":"blake",
        // "emp_no":"e5",
        // "hire_date":"1981-04-30",
        // "job":"manager",
        // "salary":2850.0,
        // "dept_id":3,
        // "mng_id":1
        // }

        Gson gson = new Gson();
        String json = gson.toJson(emp);

        return json;

    }// createEmployeeJson

    public String createTimecardJson(Timecard card) {
        // Example timecard object:
        // {
        // "timecard":{
        // "timecard_id":1,
        // "start_time":"2018-06-14 11:30:00",
        // "end_time":"2018-06-14 15:30:00",
        // "emp_id":2
        // }
        // }

        Gson gson = new Gson();
        String json = gson.toJson(card);

        return json;

    }// createTimecardJson

    // Error Message Creation
    public String errorMsg(String msg) {
        String err = "";
        err = ("error: " + msg);
        return err;
    }

    // Convert Date to LocalDate
    // https://beginnersbook.com/2017/10/java-convert-date-to-localdate/
    public LocalDate getLocalDate(Date date) {

        // Getting the default zone id
        ZoneId defaultZoneId = ZoneId.systemDefault();

        // Converting the date to Instant
        Instant instant = date.toInstant();

        // Converting the Date to LocalDate
        LocalDate localDate = instant.atZone(defaultZoneId).toLocalDate();
        return localDate;
    }

    // 3 - delete all department, employee, and timecard records for a company
    public String deleteCompany(String company) {
        DataLayer dl = null;
        String result;

        try {

            dl = new DataLayer(company);

            // company – must be your RIT username (kda9036)
            if (!company.equals("kda9036")) {
                return errorMsg("Company name must be your RIT username - kda9036");
            }

            // get all employees for the company
            List<Employee> allEmployees = dl.getAllEmployee(company);

            for (Employee employee : allEmployees) {
                // get all the timecards for each employee
                int empId = employee.getId();
                List<Timecard> allEmpTimecards = dl.getAllTimecard(empId);
                for (Timecard timecard : allEmpTimecards) {
                    // delete each timecard
                    dl.deleteTimecard(timecard.getId());
                }
                // delete the employee
                dl.deleteEmployee(empId);
            }

            // get the departments for the company
            List<Department> allDepts = dl.getAllDepartment(company);
            // delete each department
            for (Department department : allDepts) {
                dl.deleteDepartment(company, department.getId());
            }

            result = company + "'s information deleted.";

        } catch (Exception e) {
            result = e.getMessage();
        } finally {
            // close DataLayer
            dl.close();
        }

        return result;
    }// deleteCompany

    // 4 - get requested department
    public String getDepartment(String company, int deptId) {
        DataLayer dl = null;
        String result;

        try {
            dl = new DataLayer(company);
            // company – must be your RIT username (kda9036)
            if (!company.equals("kda9036")) {
                return errorMsg("Company name must be your RIT username - kda9036");
            }
            result = "";
            Department dept = dl.getDepartment(company, deptId);
            if (dept == null) {
                return errorMsg("Department ID error.  dept_id must be an existing record number for a department.");
            }

            result = createDepartmentJson(dept);

        } catch (Exception e) {
            result = errorMsg("Cannot get department.");
        } finally {
            // close DataLayer
            dl.close();
        }

        return result;
    }// getDepatment

    // 5 - get requested list of departments
    public String getDepartments(String company) {
        DataLayer dl = null;
        String result;

        try {
            dl = new DataLayer(company);
            // company – must be your RIT username (kda9036)
            if (!company.equals("kda9036")) {
                return errorMsg("Company name must be your RIT username - kda9036");
            }
            result = "";
            List<Department> depts = dl.getAllDepartment(company);

            for (Department dept : depts) {
                result += createDepartmentJson(dept);
            }

        } catch (Exception e) {
            result = errorMsg("Cannot get all departments.");
        } finally {
            // close DataLayer
            dl.close();
        }

        return result;
    }// getDepatments

    // 6 - Update a department
    public String updateDepartment(String deptIn) {
        DataLayer dl = null;
        String result;
        JsonReader rdr = Json.createReader(new StringReader(deptIn));

        try (rdr) {
            JsonObject obj = rdr.readObject();

            // company and dept_id will be populated, plus variable number of other fields
            // to update
            String company = obj.getString("company");
            // use companyName ('kda9036') to create datalayer
            dl = new DataLayer(company);
            // company – must be your RIT username (kda9036)
            if (!company.equals("kda9036")) {
                return errorMsg("Company name must be your RIT username - kda9036");
            }

            // dept_id must exist, create department obj
            Department dept = dl.getDepartment(company, obj.getInt("dept_id"));
            if (dept == null) {
                return errorMsg("Department ID error.  dept_id must be an existing record number for a department.");
            }

            Set<String> keyset = obj.keySet();
            // obj.keySet().forEach(key -> {
            for (String key : keyset) {
                String value = "";

                // dept_name
                if (key.equals("dept_name")) {
                    value = obj.getString(key);
                    // format value and set
                    dept.setDeptName(value.replace("\"", ""));
                }

                // dept_no
                if (key.equals("dept_no")) {
                    value = obj.getString(key);
                    // validation - dept_no must be unique among all companies
                    List<Department> deptsInDb = dl.getAllDepartment(company);
                    for (Department d : deptsInDb) {
                        if (d.getDeptNo().equals(value)) {
                            // return doesn't work if using .forEach
                            return errorMsg(
                                    "dept_no must be unique among all companies.  If you are not updating the dept_no, do not include the key-value pair in your update.");
                        }
                    }

                    // format value and set
                    dept.setDeptNo((value.replace("\"", "")));
                }

                // location
                if (key.equals("location")) {
                    value = obj.getString(key);
                    // format value and set
                    dept.setLocation((value.replace("\"", "")));
                }
            }
            ;

            dl.updateDepartment(dept);

            result = createDepartmentJson(dept);

        } catch (Exception e) {
            result = e.getMessage();
        } finally {
            // close DataLayer
            dl.close();
        }

        return result;
    }// updateDepartment

    // 7 - Insert a department
    public String insertDepartment(String company, String deptName, String deptNo, String location) {
        DataLayer dl = null;
        String result;

        try {
            dl = new DataLayer(company);
            // company – must be your RIT username (kda9036)
            if (!company.equals("kda9036")) {
                return errorMsg("Company name must be your RIT username - kda9036");
            }

            Department dept = new Department(company, deptName, deptNo, location);

            // validation - dept_no must be unique among all companies
            List<Department> deptsInDb = dl.getAllDepartment(company);
            for (Department d : deptsInDb) {
                if (d.getDeptNo().equals(deptNo)) {
                    return errorMsg("dept_no must be unique among all companies.");
                }
            }

            Department dept1 = dl.insertDepartment(dept);

            result = createDepartmentJson(dept1);

        } catch (Exception e) {
            result = e.getMessage();
        } finally {
            // close DataLayer
            dl.close();
        }

        return result;
    }// insertDepartment

    // 8 - delete a department
    public String deleteDepartment(String company, int deptId) {
        DataLayer dl = null;
        String result;

        try {
            dl = new DataLayer(company);
            // company – must be your RIT username (kda9036)
            if (!company.equals("kda9036")) {
                return errorMsg("Company name must be your RIT username - kda9036");
            }

            // delete the department
            int rows = dl.deleteDepartment(company, deptId);

            if (rows == 0) {
                return errorMsg(
                        "Cannot delete department. Check that dept_id is an existing record number for a department.");
            } else {
                result = "Department " + deptId + " from " + company + " deleted.";
            }

        } catch (Exception e) {
            result = errorMsg(
                    "Cannot delete department. Check that dept_id is an existing record number for a department.");
        } finally {
            // close DataLayer
            dl.close();
        }

        return result;
    }// deleteDepartment

    // 9 - get an employee
    public String getEmployee(String company, int empId) {
        DataLayer dl = null;
        String result;

        try {
            dl = new DataLayer(company);
            // company – must be your RIT username (kda9036)
            if (!company.equals("kda9036")) {
                return errorMsg("Company name must be your RIT username - kda9036");
            }
            Employee emp = dl.getEmployee(empId);
            if (emp == null) {
                return errorMsg("Employee ID error.  emp_id must be an existing record number for an employee.");
            }

            result = createEmployeeJson(emp);

        } catch (Exception e) {
            result = errorMsg("Cannot get employee.");
        } finally {
            // close DataLayer
            dl.close();
        }

        return result;
    }// getEmployee

    // 10 - get list of employees
    public String getEmployees(String company) {
        DataLayer dl = null;
        String result = "";

        try {
            dl = new DataLayer(company);
            // company – must be your RIT username (kda9036)
            if (!company.equals("kda9036")) {
                return errorMsg("Company name must be your RIT username - kda9036");
            }
            List<Employee> employees = dl.getAllEmployee(company);

            for (Employee emp : employees) {
                result += createEmployeeJson(emp);
            }

        } catch (Exception e) {
            result = errorMsg("Cannot get employees.");
        } finally {
            // close DataLayer
            dl.close();
        }

        return result;
    }// getEmployees

    // 11 - insert an employee
    public String insertEmployee(String company, String empName, String empNo, String hire_date, String job,
            double salary, int deptId, int mngId) {
        DataLayer dl = null;
        String result;

        try {
            dl = new DataLayer(company);

            Date hireDate = Date.valueOf(hire_date);

            // validation:

            // company – must be your RIT username (kda9036)
            if (!company.equals("kda9036")) {
                return errorMsg("Company name must be your RIT username - kda9036");
            }

            // dept_id must exist as a Department in your company
            if (dl.getDepartment(company, deptId) == null) {
                return errorMsg("dept_id must exist as a Department in your company.");
            }

            // mng_id must be the record id of an existing Employee in your company.
            // Use 0 if the first employee or any other employee that doesn’t have a
            // manager.
            if (mngId != 0) {
                // get employee/manager by emp_id
                if (dl.getEmployee(mngId) == null) {
                    return errorMsg("mng_id must be the record id of an existing Employee in your company.");
                }
            }

            // hire_date must be a valid date equal to the current date or earlier (e.g.
            // current date or in the past)
            Date now = Date.valueOf(LocalDate.now());
            if (hireDate.compareTo(now) > 0) {
                // hireDate occurs after today/current date
                return errorMsg("hire_date must be a valid date equal to the current date or earlier.");
            }

            // hire_date must be a Monday, Tuesday, Wednesday, Thursday or a Friday. It
            // cannot be Saturday or Sunday.
            LocalDate localDate = hireDate.toLocalDate();

            DayOfWeek day = localDate.getDayOfWeek();
            int dayNum = day.getValue();
            // Monday is 1, Sunday is 7
            if (dayNum == 6 || dayNum == 7) {
                return errorMsg("hire_date cannot be Saturday or Sunday.");
            }

            // emp_no must be unique amongst all employees in the database,
            // including those of other companies
            List<Employee> empsInDb = dl.getAllEmployee(company);
            for (Employee e : empsInDb) {
                if (e.getEmpNo().equals(empNo)) {
                    return errorMsg("emp_no must be unique amongst all employees in the database.");
                }
            }

            if (job == null || job.equals("") || job.trim().equals("")) {
                return errorMsg("job must be populated.");
            }

            Employee emp = new Employee(empName, empNo, hireDate, job, salary, deptId,
                    mngId);

            Employee emp1 = dl.insertEmployee(emp);

            result = createEmployeeJson(emp1);

        } catch (Exception e) {
            result = e.getMessage();
        } finally {
            // close DataLayer
            dl.close();
        }

        return result;
    }// insertEmployee

    // 12 - Update an employee
    public String updateEmployee(String empIn) {
        DataLayer dl = null;
        String result;
        JsonReader rdr = Json.createReader(new StringReader(empIn));

        try (rdr) {
            JsonObject obj = rdr.readObject();

            // company and emp_id will be populated, plus variable number of other fields
            // to update

            String company = obj.getString("company");

            // use companyName ('kda9036') to create datalayer
            dl = new DataLayer(company);

            // validation:
            // company – must be your RIT username (kda9036)
            if (!company.equals("kda9036")) {
                return errorMsg("Company name must be your RIT username - kda9036");
            }

            // emp_id must exist, create employee obj
            Employee emp = dl.getEmployee(obj.getInt("emp_id"));
            if (emp == null) {
                return errorMsg("Employee ID error.  emp_id must be a valid record in the database.");
            }

            Set<String> keyset = obj.keySet();
            for (String key : keyset) {
                String value = "";

                // emp_name
                if (key.equals("emp_name")) {
                    value = obj.getString(key);
                    // format value and set
                    emp.setEmpName(value.replace("\"", ""));
                }

                // emp_no
                if (key.equals("emp_no")) {
                    value = obj.getString(key);
                    // validation
                    // emp_no must be unique amongst all employees in the database,
                    // including those of other companies
                    List<Employee> empsInDb = dl.getAllEmployee(company);
                    for (Employee e : empsInDb) {
                        if (e.getEmpNo().equals(value)) {
                            return errorMsg(
                                    "emp_no must be unique amongst all employees in the database.  If you are keeping the current emp_id the same, do not include the key-value pair in your update.");
                        }
                    }

                    // format value and set
                    emp.setEmpNo((value.replace("\"", "")));
                }

                // hire_date
                if (key.equals("hire_date")) {
                    value = obj.getString(key);
                    // validation
                    // hire_date must be a valid date equal to the current date or earlier (e.g.
                    // current date or in the past)
                    Date now = Date.valueOf(LocalDate.now());
                    Date hireDate = Date.valueOf(value);
                    if (hireDate == null) {
                        return errorMsg("hire_date is invalid.");
                    }
                    if (hireDate.compareTo(now) > 0) {
                        // hireDate occurs after today/current date
                        return errorMsg("hire_date must be a valid date equal to the current date or earlier.");
                    }

                    // // hire_date must be a Monday, Tuesday, Wednesday, Thursday or a Friday. It
                    // // cannot be Saturday or Sunday.
                    LocalDate localDate = hireDate.toLocalDate();

                    DayOfWeek day = localDate.getDayOfWeek();
                    int dayNum = day.getValue();
                    // Monday is 1, Sunday is 7
                    if (dayNum == 6 || dayNum == 7) {
                        return errorMsg("hire_date cannot be Saturday or Sunday.");
                    }

                    hireDate = Date.valueOf(value.replace("\"", ""));

                    // format value and set
                    emp.setHireDate(hireDate);
                }

                // job
                if (key.equals("job")) {
                    value = obj.getString(key);
                    // format value and set
                    emp.setJob(value.replace("\"", ""));
                }

                // salary
                if (key.equals("salary")) {
                    JsonNumber num = obj.getJsonNumber(key);
                    double salary = num.doubleValue();

                    // format value and set
                    emp.setSalary(salary);
                }

                // dept_id
                if (key.equals("dept_id")) {
                    int deptId = obj.getInt(key);
                    // validation
                    // dept_id must exist as a Department in your company
                    if (dl.getDepartment(company, deptId) == null) {
                        return errorMsg("dept_id must exist as a Department in your company.");
                    }

                    // format value and set
                    emp.setDeptId(deptId);
                }

                // mng_id
                if (key.equals("mng_id")) {
                    int mngId = obj.getInt(key);
                    // validation
                    // mng_id must be the record id of an existing Employee in your company.
                    // Use 0 if the first employee or any other employee that doesn’t have a
                    // manager.
                    if (mngId != 0) {
                        // get employee/manager by emp_id
                        if (dl.getEmployee(mngId) == null) {
                            return errorMsg("mng_id must be the record id of an existing Employee in your company.");
                        }
                    }

                    // format value and set
                    emp.setMngId(mngId);
                }
            }
            ;

            dl.updateEmployee(emp);

            result = createEmployeeJson(emp);

        } catch (Exception e) {
            result = e.getMessage();
        } finally {
            // close DataLayer
            dl.close();
        }

        return result;
    }// updateEmployee

    // 13 - Delete an employee
    public String deleteEmployee(String company, int empId) {
        DataLayer dl = null;
        String result;

        try {
            dl = new DataLayer(company);
            // company – must be your RIT username (kda9036)
            if (!company.equals("kda9036")) {
                return errorMsg("Company name must be your RIT username - kda9036");
            }

            // get the employee to delete
            Employee emp = dl.getEmployee(empId);
            if (emp == null) {
                return errorMsg("Employee ID error.  emp_id must be a valid record in the database.");
            }

            // delete the employee
            dl.deleteEmployee(empId);

            result = "Employee " + empId + " deleted.";

        } catch (Exception e) {
            result = e.getMessage();
        } finally {
            // close DataLayer
            dl.close();
        }

        return result;
    }// deleteDepartment

    // 14 - Get a timecard
    public String getTimecard(String company, int timecardId) {
        DataLayer dl = null;
        String result = null;

        try {
            dl = new DataLayer(company);
            // company – must be your RIT username (kda9036)
            if (!company.equals("kda9036")) {
                return errorMsg("Company name must be your RIT username - kda9036");
            }

            Timecard card = dl.getTimecard(timecardId);
            if (card == null) {
                return errorMsg("Timecard ID error.  timecard_id must be a valid record in the database.");
            }

            result = createTimecardJson(card);

        } catch (Exception e) {
            result = e.getMessage();
        } finally {
            // close DataLayer
            dl.close();
        }

        return result;
    }// getTimecard

    // 15 - Get all timecards for an employee
    public String getAllTimecards(String company, int empId) {
        DataLayer dl = null;
        String result = "";

        try {
            dl = new DataLayer(company);
            // company – must be your RIT username (kda9036)
            if (!company.equals("kda9036")) {
                return errorMsg("Company name must be your RIT username - kda9036");
            }

            Employee emp = dl.getEmployee(empId);
            if (emp == null) {
                return errorMsg("emp_id must exist as the record id of an Employee in your company.");
            }

            List<Timecard> cards = dl.getAllTimecard(empId);

            for (Timecard tc : cards) {
                result += createTimecardJson(tc);
            }

        } catch (Exception e) {
            result = e.getMessage();
        } finally {
            // close DataLayer
            dl.close();
        }

        return result;
    }// getAllTimecards

    // 16 - Insert a timecard
    public String insertTimecard(String company, int empId, String start, String end) {
        DataLayer dl = null;
        String result;

        try {

            dl = new DataLayer(company);

            // validation:
            // company – must be your RIT username (kda9036)
            if (!company.equals("kda9036")) {
                return errorMsg("Company name must be your RIT username - kda9036");
            }

            // emp_id must exist as the record id of an Employee in your company.
            Employee emp = dl.getEmployee(empId);
            if (emp == null) {
                return errorMsg("emp_id must exist as the record id of an Employee in your company.");
            }
            // format "2018-06-15 12:30:00"
            Timestamp startTime = Timestamp.valueOf(start);

            Timestamp endTime = Timestamp.valueOf(end);

            // start_time must be a valid date and time equal to the current date or back to
            // the Monday prior to the current date if the current date is not a Monday
            // cannot be Saturday or Sunday.
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime localDateStart = startTime.toLocalDateTime();
            LocalDateTime localDateEnd = endTime.toLocalDateTime();
            LocalDateTime lastMonday = null;
            DayOfWeek day = localDateStart.getDayOfWeek();
            int dayNum = day.getValue();
            DayOfWeek dayEnd = localDateEnd.getDayOfWeek();
            int dayNumEnd = dayEnd.getValue();
            if (dayNum == 6 || dayNum == 7) {
                return errorMsg("start_time cannot be Saturday or Sunday.");
            }
            // If start is not a Monday, get the previous monday
            if (dayNum != 1) {
                lastMonday = now.with(TemporalAdjusters.previous(DayOfWeek.MONDAY));
                if ((localDateStart.compareTo(lastMonday) < 0)) {
                    // start occurs before the last monday
                    return errorMsg("start_time must be a valid date that cannot be before the most recent Monday.");
                }
            }

            if ((localDateStart.compareTo(now) > 0)) {
                // start occurs after today/current date
                return errorMsg("start_time must be a valid date equal to the current date or earlier.");
            }

            if (dayNumEnd == 6 || dayNumEnd == 7) {
                return errorMsg("end_time cannot be Saturday or Sunday.");
            }

            // start_time and end_time must be between the hours (in 24 hour format) of
            // 08:00:00 and 18:00:00 inclusive.
            int startHour = localDateStart.getHour();
            int endHour = localDateEnd.getHour();
            if (startHour < 8 || startHour > 18) {
                return errorMsg("start_time must be between 8:00:00 and 18:00:00, inclusive.");
            }
            if (startHour == 18) {
                // if hour is 18, check if minutes are greater than 0 or seconds are greater
                // than 0
                int startMinute = localDateStart.getMinute();
                int startSecond = localDateStart.getSecond();
                if (startMinute > 0 || startSecond > 0) {
                    return errorMsg("start_time cannot be past 18:00:00.");
                }
            }

            if (endHour < 8 || endHour > 18) {
                return errorMsg("end_time must be between 8:00:00 and 18:00:00, inclusive.");
            }
            if (endHour == 18) {
                // if hour is 18, check if minutes are greater than 0 or seconds are greater
                // than 0
                int endMinute = localDateEnd.getMinute();
                int endSecond = localDateEnd.getSecond();
                if (endMinute > 0 || endSecond > 0) {
                    return errorMsg("end_time cannot be past 18:00:00.");
                }
            }

            // start and end must be on same day
            if ((localDateStart.getDayOfMonth() != localDateEnd.getDayOfMonth())) {
                return errorMsg("start_time and end_time must be on the same day");
            }

            // end_time must be at least 1 hour greater than the start_time
            long noOfHours = Duration.between(localDateStart, localDateEnd).toHours();
            if (noOfHours < 1) {
                return errorMsg("end_time must be at least 1 hour greater than the start_time");
            }

            // start_time must not be on the same day as any other start_time for that
            // employee.
            // get all Timecards and check all start_time dates
            DateFormat f = new SimpleDateFormat("yyyy-MM-dd");
            Date d = new Date(startTime.getTime());
            String dStr = f.format(d);
            List<Timecard> tcs = dl.getAllTimecard(empId);
            for (Timecard tc : tcs) {
                Date date = new Date(tc.getStartTime().getTime());
                String dateStr = f.format(date);
                if (dStr.equals(dateStr)) {
                    return errorMsg("start_time must not be on the same day as any other start_time for the employee.");
                }
            }

            Timecard card = new Timecard(startTime, endTime, empId);

            Timecard tc = dl.insertTimecard(card);

            result = createTimecardJson(tc);

        } catch (Exception e) {
            result = e.getMessage();
        } finally {
            // close DataLayer
            dl.close();
        }

        return result;
    }// insertTimecard

    // 17 - Update a timecard
    public String updateTimecard(String cardIn) {
        DataLayer dl = null;
        String result;
        JsonReader rdr = Json.createReader(new StringReader(cardIn));

        try (rdr) {
            JsonObject obj = rdr.readObject();
            int empId = 0;

            // company and timecard_id will be populated, plus variable number of other
            // fields to update
            String company = obj.getString("company");

            // use companyName ('kda9036') to create datalayer
            dl = new DataLayer(company);

            if (!company.equals("kda9036")) {
                return errorMsg("Company name must be your RIT username - kda9036");
            }

            // timecard_id must exist, create timecard obj
            Timecard card = dl.getTimecard(obj.getInt("timecard_id"));
            if (card == null) {
                return errorMsg("Timecard ID error.  timecard_id must be a valid record in the database.");
            }

            Set<String> keyset = obj.keySet();
            for (String key : keyset) {
                String value = "";

                // start_time
                if (key.equals("start_time")) {
                    value = obj.getString(key);
                    Timestamp start = Timestamp.valueOf(value.replace("\"", ""));

                    // format value and set
                    card.setStartTime(start);
                }

                // end_time
                if (key.equals("end_time")) {
                    value = obj.getString(key);
                    Timestamp end = Timestamp.valueOf(value.replace("\"", ""));

                    // format value and set
                    card.setEndTime(end);
                }

                // emp_id
                if (key.equals("emp_id")) {
                    empId = obj.getInt(key);
                    // validation
                    // emp_id must exist as an Employee in your company
                    Employee emp = dl.getEmployee(empId);
                    if (emp == null) {
                        return errorMsg("emp_id must exist as the record id of an Employee in your company.");
                    }
                    // format value and set
                    card.setEmpId(empId);
                }
            }
            ;

            // validation before updating:
            // start_time
            Timestamp startTime = card.getStartTime();
            // end_time
            Timestamp endTime = card.getEndTime();

            // start_time must be a valid date and time equal to the current date or back to
            // the Monday prior to the current date if the current date is not a Monday
            // cannot be Saturday or Sunday.
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime localDateStart = startTime.toLocalDateTime();
            LocalDateTime localDateEnd = endTime.toLocalDateTime();
            LocalDateTime lastMonday = null;
            DayOfWeek day = localDateStart.getDayOfWeek();
            int dayNum = day.getValue();
            DayOfWeek dayEnd = localDateEnd.getDayOfWeek();
            int dayNumEnd = dayEnd.getValue();
            if (dayNum == 6 || dayNum == 7) {
                return errorMsg("start_time cannot be Saturday or Sunday.");
            }
            // If start is not a Monday, get the previous monday
            if (dayNum != 1) {
                lastMonday = now.with(TemporalAdjusters.previous(DayOfWeek.MONDAY));
                if ((localDateStart.compareTo(lastMonday) < 0)) {
                    // start occurs before the last monday
                    return errorMsg("start_time must be a valid date that cannot be before the most recent Monday.");
                }
            }

            if ((localDateStart.compareTo(now) > 0)) {
                // start occurs after today/current date
                return errorMsg("start_time must be a valid date equal to the current date or earlier.");
            }

            if (dayNumEnd == 6 || dayNumEnd == 7) {
                return errorMsg("end_time cannot be Saturday or Sunday.");
            }

            // start_time and end_time must be between the hours (in 24 hour format) of
            // 08:00:00 and 18:00:00 inclusive.
            int startHour = localDateStart.getHour();
            int endHour = localDateEnd.getHour();
            if (startHour < 8 || startHour > 18) {
                return errorMsg("start_time must be between 8:00:00 and 18:00:00, inclusive.");
            }
            if (startHour == 18) {
                // if hour is 18, check if minutes are greater than 0 or seconds are greater
                // than 0
                int startMinute = localDateStart.getMinute();
                int startSecond = localDateStart.getSecond();
                if (startMinute > 0 || startSecond > 0) {
                    return errorMsg("start_time cannot be past 18:00:00.");
                }
            }

            if (endHour < 8 || endHour > 18) {
                return errorMsg("end_time must be between 8:00:00 and 18:00:00, inclusive.");
            }
            if (endHour == 18) {
                // if hour is 18, check if minutes are greater than 0 or seconds are greater
                // than 0
                int endMinute = localDateEnd.getMinute();
                int endSecond = localDateEnd.getSecond();
                if (endMinute > 0 || endSecond > 0) {
                    return errorMsg("end_time cannot be past 18:00:00.");
                }
            }

            // start and end must be on same day
            if ((localDateStart.getDayOfMonth() != localDateEnd.getDayOfMonth())) {
                return errorMsg("start_time and end_time must be on the same day");
            }

            // end_time must be at least 1 hour greater than the start_time
            long noOfHours = Duration.between(localDateStart, localDateEnd).toHours();
            if (noOfHours < 1) {
                return errorMsg("end_time must be at least 1 hour greater than the start_time");
            }

            // start_time must not be on the same day as any other start_time for that
            // employee.
            // get all Timecards and check all start_time dates
            DateFormat f = new SimpleDateFormat("yyyy-MM-dd");
            Date d = new Date(startTime.getTime());
            String dStr = f.format(d);
            List<Timecard> tcs = dl.getAllTimecard(empId);
            for (Timecard tc : tcs) {
                Date date = new Date(tc.getStartTime().getTime());
                String dateStr = f.format(date);
                if (dStr.equals(dateStr)) {
                    return errorMsg("start_time must not be on the same day as any other start_time for the employee.");
                }
            }

            dl.updateTimecard(card);

            result = createTimecardJson(card);

        } catch (Exception e) {
            result = e.getMessage();
        } finally {
            // close DataLayer
            dl.close();
        }

        return result;
    }// updateTimecard

    // 18 - Delete a timecard
    public String deleteTimecard(String company, int timecardId) {
        DataLayer dl = null;
        String result;

        try {
            dl = new DataLayer(company);

            if (!company.equals("kda9036")) {
                return errorMsg("Company name must be your RIT username - kda9036");
            }

            // get the timecard to delete
            Timecard card = dl.getTimecard(timecardId);
            if (card == null) {
                return errorMsg("Timecard ID error.  timecard_id must be a valid record in the database.");
            }

            // delete the employee
            dl.deleteTimecard(timecardId);

            result = "Timecard " + timecardId + " deleted.";

        } catch (Exception e) {
            result = e.getMessage();
        } finally {
            // close DataLayer
            dl.close();
        }

        return result;
    }// deleteTimecard

}// BusinessLayer